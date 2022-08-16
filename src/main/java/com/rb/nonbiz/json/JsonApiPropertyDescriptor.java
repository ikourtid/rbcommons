package com.rb.nonbiz.json;

import com.google.common.base.Joiner;
import com.rb.biz.types.OnesBasedReturn;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.IidSet;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.RBSets;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import static com.rb.biz.types.StringFunctions.isAllWhiteSpace;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.Strings.formatMapInKeyOrder;

/**
 * <p> This is helpful in the JSON API documentation (OpenAPI / Swagger). It gives us type information for a property of a
 * JSON object in the JSON API serialization. </p>
 *
 * <p> It is a bit like a generalization of the Java concept of a {@link Class} object, except tailored for
 * purposes of serialization and generating documentation. For example, it records information about generic classes,
 * whereas a simple {@link Class} object does not (due to Java type erasure). </p>
 */
public abstract class JsonApiPropertyDescriptor {

  public interface Visitor<T> {

    T visitSimpleClassJsonApiPropertyDescriptor(
        SimpleClassJsonApiPropertyDescriptor simpleClassJsonApiPropertyDescriptor);
    T visitIidMapJsonApiPropertyDescriptor(
        IidMapJsonApiPropertyDescriptor iidMapJsonApiPropertyDescriptor);
    T visitRBMapJsonApiPropertyDescriptor(
        RBMapJsonApiPropertyDescriptor rbMapJsonApiPropertyDescriptor);
    T visitCollectionJsonApiPropertyDescriptor(
        CollectionJsonApiPropertyDescriptor collectionJsonApiPropertyDescriptor);
    T visitJavaGenericJsonApiPropertyDescriptor(
        JavaGenericJsonApiPropertyDescriptor javaGenericJsonApiPropertyDescriptor);
    T visitPseudoEnumJsonApiPropertyDescriptor(
        PseudoEnumJsonApiPropertyDescriptor pseudoEnumJsonApiPropertyDescriptor);
    T visitJavaEnumJsonApiPropertyDescriptor(
        JavaEnumJsonApiPropertyDescriptor<? extends Enum<?>> javaEnumJsonApiPropertyDescriptor);

  }

  public abstract <T> T visit(Visitor<T> visitor);

  private static Optional<Class<?>> getClassIfSimpleClassJsonApiPropertyDescriptor(
      JsonApiPropertyDescriptor jsonApiPropertyDescriptor) {
    // A bit ugly, but handy for certain preconditions in this file. It's private anyway.
    // We could use a visitor here, but since we expressly only care about one case, instanceof & a cast is fine.
    return jsonApiPropertyDescriptor instanceof SimpleClassJsonApiPropertyDescriptor
        ? Optional.of( ((SimpleClassJsonApiPropertyDescriptor) jsonApiPropertyDescriptor).getClassBeingDescribed())
        : Optional.empty();
  }

  protected static RBSet<Class<?>> getInvalidJsonApiPropertyDescriptorClasses() {
    return rbSetOf(
        BigDecimal.class,     // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class,   // we always want to describe a specific subclass of PreciseValue
        ImpreciseValue.class, // same

        // The JSON API represents returns as zero-based, and as percentages.
        // For example, onesBasedReturn(1.02) will be '2' in the JSON API, and onesBasedReturn(0.97) will be '-3'.
        // Therefore, we should not confuse the JSON API documentation with the term 'OnesBasedReturn',
        // which is not an accurate term. We will create a different class ZeroBasedPercentageReturn,
        // with its own JsonApiDocumentation.
        OnesBasedReturn.class,

        // Add this back, but make sure you can generate the .yaml,
        // because adding it generates an exception currently. Issue #1288
        // Enum.class,
        Class.class,

        Strings.class,        // a common misspelling of String.class
        InstrumentId.class,   // sounds weird to have an IidMap that maps to an instrument
        Symbol.class);        // same as above
  }


  /**
   * <p> Tells us the type of a property of a JsonObject in the JSON API, in the simplest case
   * where it is a single JSON API data class (e.g. not some collection). </p>
   */
  public static class SimpleClassJsonApiPropertyDescriptor extends JsonApiPropertyDescriptor {

    private final Class<?> clazz;

    private SimpleClassJsonApiPropertyDescriptor(Class<?> clazz) {
      this.clazz = clazz;
    }

    public static SimpleClassJsonApiPropertyDescriptor simpleClassJsonApiPropertyDescriptor(Class<?> clazz) {
      RBSets.union(
              getInvalidJsonApiPropertyDescriptorClasses(),
              rbSetOf(
                  // These 4 have their own JsonApiPropertyDescriptor classes, which we should be using.
                  UniqueId.class,
                  IidMap.class,
                  RBMap.class,
                  RBSet.class))
          .forEach(badClass ->
              RBPreconditions.checkArgument(
                  !clazz.equals(badClass),
                  "SimpleClassJsonApiPropertyDescriptor has bad class %s",
                  clazz));
      return new SimpleClassJsonApiPropertyDescriptor(clazz);
    }

    /**
     * This cannot be called getClass because it's an existing method in java.lang.Object.
     */
    public Class<?> getClassBeingDescribed() {
      return clazz;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitSimpleClassJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[SCJAPD %s SCJAPD]", clazz);
    }

  }


  /**
   * <p> Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an IidMap of some Java data class. </p>
   *
   * <p> Instead of just using a simple {@link Class} object, this stores a {@link JsonApiPropertyDescriptor},
   * which is more general, so that we can also represent things such as {@literal IidMap<List<Double>>}.</p>
   */
  public static class IidMapJsonApiPropertyDescriptor extends JsonApiPropertyDescriptor {

    private final JsonApiPropertyDescriptor valueClassDescriptor;

    private IidMapJsonApiPropertyDescriptor(JsonApiPropertyDescriptor valueClassDescriptor) {
      this.valueClassDescriptor = valueClassDescriptor;
    }

    public static IidMapJsonApiPropertyDescriptor iidMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor valueClassDescriptor) {
      getClassIfSimpleClassJsonApiPropertyDescriptor(valueClassDescriptor).ifPresent(valueClass ->
          RBSets.union(
                  getInvalidJsonApiPropertyDescriptorClasses(),
                  rbSetOf(
                      // These 2 have their own JsonApiPropertyDescriptor classes, which we should be using.
                      UniqueId.class,
                      RBSet.class,

                      // It's unlikely that we'll be mapping to another map, although not impossible.
                      IidMap.class,
                      RBMap.class))
              .forEach(invalidClass ->
                  RBPreconditions.checkArgument(
                      !valueClass.equals(invalidClass),
                      "IidMapJsonApiPropertyDescriptor uses an invalid class of %s",
                      invalidClass)));
      return new IidMapJsonApiPropertyDescriptor(valueClassDescriptor);
    }

    public JsonApiPropertyDescriptor getValueClassDescriptor() {
      return valueClassDescriptor;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitIidMapJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[IMJAPD %s IMJAPD]", valueClassDescriptor);
    }

  }


  /**
   * <p> Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an RBMap of some Java data class. </p>
   *
   * <p> Instead of just using simple {@link Class} objects, this stores {@link JsonApiPropertyDescriptor} objects,
   * which are more general, so that we can also represent things such as
   * {@literal RBMap<UniqueId<NamedFactor>, List<Double>>}.</p>
   */
  public static class RBMapJsonApiPropertyDescriptor extends JsonApiPropertyDescriptor {

    private final JsonApiPropertyDescriptor keyClassDescriptor;
    private final JsonApiPropertyDescriptor valueClassDescriptor;

    private RBMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor keyClassDescriptor,
        JsonApiPropertyDescriptor valueClassDescriptor) {
      this.keyClassDescriptor = keyClassDescriptor;
      this.valueClassDescriptor = valueClassDescriptor;
    }

    public static RBMapJsonApiPropertyDescriptor rbMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor keyClassDescriptor,
        JsonApiPropertyDescriptor valueClassDescriptor) {
      RBSet<Class<?>> invalidClasses = RBSets.union(
          getInvalidJsonApiPropertyDescriptorClasses(),
          rbSetOf(
              // These 3 have their own JsonApiPropertyDescriptor classes, which we should be using.
              IidMap.class,
              RBMap.class,
              RBSet.class));
      getClassIfSimpleClassJsonApiPropertyDescriptor(keyClassDescriptor)
          .ifPresent(keyClass ->
              invalidClasses.forEach(invalidClass ->
                  RBPreconditions.checkArgument(
                      !keyClass.equals(invalidClass),
                      "RBMapJsonApiPropertyDescriptor for %s -> %s : invalid key class",
                      keyClassDescriptor, valueClassDescriptor)));
      getClassIfSimpleClassJsonApiPropertyDescriptor(valueClassDescriptor)
          .ifPresent(valueClass ->
              invalidClasses.forEach(invalidClass ->
                  RBPreconditions.checkArgument(
                      !valueClass.equals(invalidClass),
                      "RBMapJsonApiPropertyDescriptor for %s -> %s : invalid value class",
                      keyClassDescriptor, valueClassDescriptor)));
      return new RBMapJsonApiPropertyDescriptor(keyClassDescriptor, valueClassDescriptor);
    }

    public JsonApiPropertyDescriptor getKeyClassDescriptor() {
      return keyClassDescriptor;
    }

    public JsonApiPropertyDescriptor getValueClassDescriptor() {
      return valueClassDescriptor;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitRBMapJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[RMJAPD %s -> %s RMJAPD]", keyClassDescriptor, valueClassDescriptor);
    }

  }


  /**
   * <p> Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an collection (Set, List, or array) of some Java data class. </p>
   *
   * <p> We use a {@link JsonApiPropertyDescriptor} instead of just a raw {@link Class} so that we can represent
   * things such as {@literal List<UniqueId<NamedFactor>> }, i.e. where the value class inside the collection is not
   * a simple class and is instead a generic, a map, etc. </p>
   */
  public static class CollectionJsonApiPropertyDescriptor extends JsonApiPropertyDescriptor {

    private final JsonApiPropertyDescriptor collectionValueClassDescriptor;

    private CollectionJsonApiPropertyDescriptor(JsonApiPropertyDescriptor collectionValueClassDescriptor) {
      this.collectionValueClassDescriptor = collectionValueClassDescriptor;
    }

    public static CollectionJsonApiPropertyDescriptor collectionJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor arrayValueClassDescriptor) {
      RBSet<Class<?>> invalidClasses = RBSets.union(
          getInvalidJsonApiPropertyDescriptorClasses(),
          singletonRBSet(UniqueId.class));
      getClassIfSimpleClassJsonApiPropertyDescriptor(arrayValueClassDescriptor)
          .ifPresent(arrayValueClass ->
              invalidClasses.forEach(invalidClass ->
                  RBPreconditions.checkArgument(
                      !arrayValueClass.equals(invalidClass),
                      "CollectionJsonApiPropertyDescriptor uses an invalid class of %s",
                      invalidClass)));
      return new CollectionJsonApiPropertyDescriptor(arrayValueClassDescriptor);
    }

    public JsonApiPropertyDescriptor getCollectionValueClassDescriptor() {
      return collectionValueClassDescriptor;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitCollectionJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[CJAPD %s CJAPD]", collectionValueClassDescriptor);
    }

  }


  /**
   * <p> Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of a java generic such as {@code Foo<T>}. </p>
   *
   * <p> It should only be used when T is an actual data class that has a JSON serialization. Example:
   * {@code UniqueId<NamedFactor>}. It should not be used for 'marker interface' classes, such as
   * {@code Portfolio<HeldByUs>}. This makes sense, because HeldByUs is not something that gets serialized. </p>
   *
   * <p> For the inner classes, we use the more general {@link JsonApiPropertyDescriptor} instead of a raw
   * {@link Class}. This allows us to support things like {@code UniqueId<List<Double>>}, i.e. situations
   * where the generic argument class is not a 'simple' class. (This is an unrealistic example, as we'd never
   * really need a unique ID of a list, but it should illustrate the point. </p>
   */
  public static class JavaGenericJsonApiPropertyDescriptor extends JsonApiPropertyDescriptor {

    private final Class<?> outerClass;
    private final List<JsonApiPropertyDescriptor> genericArgumentClassDescriptors;

    private JavaGenericJsonApiPropertyDescriptor(
        Class<?> outerClass,
        List<JsonApiPropertyDescriptor> genericArgumentClassDescriptors) {
      this.outerClass = outerClass;
      this.genericArgumentClassDescriptors = genericArgumentClassDescriptors;
    }

    private static JavaGenericJsonApiPropertyDescriptor javaGenericJsonApiPropertyDescriptor(
        Class<?> outerClass,
        List<JsonApiPropertyDescriptor> genericArgumentClassDescriptors) {
      for (JsonApiPropertyDescriptor innerClassDescriptor : genericArgumentClassDescriptors) {
        getClassIfSimpleClassJsonApiPropertyDescriptor(innerClassDescriptor)
            .ifPresent(innerClass ->
                RBPreconditions.checkArgument(
                    !outerClass.equals(innerClass),
                    "Outer and generic argument class of generic shouldn't be the same: %s vs. %s : %s",
                    outerClass, innerClassDescriptor, genericArgumentClassDescriptors));
      }
      // Ideally, we want to restrict the usage of this class (JavaGenericJsonApiPropertyDescriptor)
      // to cases where the 'outer' class is generic on one or more 'generic argument' classes.
      // However, due to Java type erasure, we don't know at runtime what's generic and what's not.
      // But let's just add a few obvious exceptions that we know will never be true.
      RBSets.union(
              getInvalidJsonApiPropertyDescriptorClasses(),
              rbSetOf(
                  // These have their own JsonApiPropertyDescriptor classes, which we should be using.
                  RBSet.class,
                  IidSet.class,
                  IidMap.class,
                  RBMap.class))
          .forEach(badOuterClass -> RBPreconditions.checkArgument(
              !outerClass.equals(badOuterClass),
              "Outer class %s is invalid",
              outerClass));
      // We can't test the following, because this is a private static constructor, and the only way to construct a
      // JavaGenericJsonApiPropertyDescriptor is through some other static constructors that can't allow this to happen,
      // but let's keep it anyway.
      RBPreconditions.checkArgument(
          !genericArgumentClassDescriptors.isEmpty(),
          "JavaGenericJsonApiPropertyDescriptor describes a generic class '%s' without generic arguments",
          outerClass);
      return new JavaGenericJsonApiPropertyDescriptor(outerClass, genericArgumentClassDescriptors);
    }

    /**
     * Represents e.g. a {@code NetGain<LongTerm>}, where NetGain is the outer class, and LongTerm is the inner class
     * (2nd argument).
     *
     * We normally use a builder when there can be two arguments of the same type, but this is meant to be used
     * inline in the various definitions of JSON_VALIDATION_INSTRUCTIONS in the JSON API converter verb classes,
     * so we want its invocation to look short.
     */
    public static JavaGenericJsonApiPropertyDescriptor javaGenericJsonApiPropertyDescriptor(
        Class<?> outerClass, JsonApiPropertyDescriptor first, JsonApiPropertyDescriptor... rest) {
      return javaGenericJsonApiPropertyDescriptor(outerClass, concatenateFirstAndRest(first, rest));
    }

    /**
     * A shorthand for the case of {@link UniqueId}.
     */
    public static JavaGenericJsonApiPropertyDescriptor uniqueIdJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor innerClassDescriptor) {
      return javaGenericJsonApiPropertyDescriptor(UniqueId.class, innerClassDescriptor);
    }

    public Class<?> getOuterClass() {
      return outerClass;
    }

    public List<JsonApiPropertyDescriptor> getGenericArgumentClassDescriptors() {
      return genericArgumentClassDescriptors;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitJavaGenericJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[JGJAPD %s < %s > JGJAPD]",
          outerClass,
          Joiner.on(" , ").join(genericArgumentClassDescriptors));
    }

  }


  /**
   * <p> We often serialize a base class with multiple subclasses by using a string key in the JSON to represent the
   * subclass's type. Example: NaiveSubObjectiveFormulationDetailsJsonApiConverter. The strings in the JSON may not
   * be exact matches to the Java class names - and anyway, they shouldn't be, because if we rename Java classes, we
   * don't want the API to change, as others may be relying on those specific strings.
   * Also, there are other cases where we use a special string in the JSON API
   * to represent some special values (e.g. GlobalObjectiveThreshold where the threshold always passes). </p>
   *
   * <p> For those JSON properties, we should be using this. </p>
   *
   * <p> Note that this does not represent an actual enum; for that, see {@link JavaEnumJsonApiPropertyDescriptor}. </p>
   */
  public static class PseudoEnumJsonApiPropertyDescriptor extends JsonApiPropertyDescriptor {

    // This should be removed once we have human-readable descriptions for all instances where we use it.
    // The reason it exists is that it helps us avoid having fixmes in multiple places.
    public static HumanReadableLabel undefinedPseudoEnumJsonApiPropertyDescription() {
      return label("FIXME SWA JSONDOC");
    }

    private final RBMap<String, HumanReadableLabel> validValuesToExplanations;

    private PseudoEnumJsonApiPropertyDescriptor(RBMap<String, HumanReadableLabel> validValuesToExplanations) {
      this.validValuesToExplanations = validValuesToExplanations;
    }

    public static PseudoEnumJsonApiPropertyDescriptor pseudoEnumJsonApiPropertyDescriptor(
        RBMap<String, HumanReadableLabel> validValuesToExplanations) {
      // One reasonable question is:
      // The variable name is validValuesToExplanations, but is there a way to make sure the String keys are valid?
      // Or does this happen somewhere else?
      // The answer is that these are valid as per whatever portion of the JSON API uses this infrastructure.
      // We don't know what 'pseudo-enum' is being represented here, and what the valid strings in the serialization are.
      RBPreconditions.checkArgument(
          !validValuesToExplanations.isEmpty(),
          "There must be at least 1 item here: %s",
          validValuesToExplanations);
      validValuesToExplanations
          .forEachEntry( (pseudoEnumString, explanationLabel) -> {
            RBPreconditions.checkArgument(
                !isAllWhiteSpace(pseudoEnumString),
                "No 'pseudo-enum' string key may be all whitespace (which includes the empty string): %s",
                validValuesToExplanations);
            RBPreconditions.checkArgument(
                !isAllWhiteSpace(explanationLabel.getLabelText()),
                "No explanation may be all whitespace (which includes the empty string): %s",
                validValuesToExplanations);
          });
      return new PseudoEnumJsonApiPropertyDescriptor(validValuesToExplanations);
    }

    public RBMap<String, HumanReadableLabel> getValidValuesToExplanations() {
      return validValuesToExplanations;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitPseudoEnumJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[PEJAPD %s PEJAPD]",
          formatMapInKeyOrder(validValuesToExplanations, String::compareTo, " ; "));
    }

  }


  /**
   * We often serialize a Java enum by using strings that are similar in meaning to the Java identifier, but
   * possibly looking different (e.g. JSON API tends to use camelcase instead of capitalized snake case in Java).
   * We have to do this, because if we ever rename the Java enum class or its enum values, we
   * don't want the API to change, as others may be relying on those specific strings.
   *
   * For those JSON properties, we should be using this.
   */
  public static class JavaEnumJsonApiPropertyDescriptor<E extends Enum<E>> extends JsonApiPropertyDescriptor {

    /**
     * Not all enum values are guaranteed to be serializable. Ideally the API would support everything, but there are
     * sometimes old deprecated enum values that we expressly do not want to be part of the API.
     *
     * For those enum values that get serialized, this will store the string value to use in the JSON API,
     * as well as the human-readable explanation.
     */
    public static class JavaEnumSerializationAndExplanation {

      private final String jsonSerialization;
      private final HumanReadableLabel explanation;

      private JavaEnumSerializationAndExplanation(String jsonSerialization, HumanReadableLabel explanation) {
        this.jsonSerialization = jsonSerialization;
        this.explanation = explanation;
      }

      public static JavaEnumSerializationAndExplanation javaEnumSerializationAndExplanation(
          String jsonSerialization, HumanReadableLabel explanation) {
        RBPreconditions.checkArgument(
            !jsonSerialization.isEmpty(),
            "We can't use an empty string for serialization; explanation is: %s",
            explanation);
        RBPreconditions.checkArgument(
            !explanation.getLabelText().isEmpty(),
            "Explanation can't be empty for enum value= %s",
            jsonSerialization);
        return new JavaEnumSerializationAndExplanation(jsonSerialization, explanation);
      }

      public String getJsonSerialization() {
        return jsonSerialization;
      }

      public HumanReadableLabel getExplanation() {
        return explanation;
      }

      @Override
      public String toString() {
        return Strings.format("[JESAPE %s %s JESAPE]", jsonSerialization, explanation);
      }

    }


    private final Class<E> enumClass;
    private final EnumMap<E, JavaEnumSerializationAndExplanation> validValuesToExplanations;

    private JavaEnumJsonApiPropertyDescriptor(
        Class<E> enumClass,
        EnumMap<E, JavaEnumSerializationAndExplanation> validValuesToExplanations) {
      this.enumClass = enumClass;
      this.validValuesToExplanations = validValuesToExplanations;
    }

    public static <E extends Enum<E>> JavaEnumJsonApiPropertyDescriptor<E> javaEnumJsonApiPropertyDescriptor(
        Class<E> enumClass,
        EnumMap<E, JavaEnumSerializationAndExplanation> validValuesToExplanations) {
      // The 1 in the following precondition (vs. e.g. 2+)
      // is for the rare cases where we only allow one value of the enum in the JSON API.
      // You might wonder - why bother serializing such an enum in the first place? Well, this would allow us to
      // support more enum values later if we decide to.
      RBPreconditions.checkArgument(
          !validValuesToExplanations.isEmpty(),
          "There must be at least 1 item here: %s",
          validValuesToExplanations);
      validValuesToExplanations
          .values()
          .forEach(javaEnumSerializationAndExplanation -> {
            RBPreconditions.checkArgument(
                !isAllWhiteSpace(javaEnumSerializationAndExplanation.getJsonSerialization()),
                "No Java enum serialized string representation in the API may be all whitespace (which includes the empty string): %s",
                validValuesToExplanations);
            RBPreconditions.checkArgument(
                !isAllWhiteSpace(javaEnumSerializationAndExplanation.getExplanation().getLabelText()),
                "No explanation may be all whitespace (which includes the empty string): %s",
                validValuesToExplanations);
          });
      return new JavaEnumJsonApiPropertyDescriptor<>(enumClass, validValuesToExplanations);
    }

    public Class<E> getEnumClass() {
      return enumClass;
    }

    public EnumMap<? extends Enum<?>, JavaEnumSerializationAndExplanation> getValidValuesToExplanations() {
      return validValuesToExplanations;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitJavaEnumJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      // Can't use formatMap because this is an EnumMap, not a RBMap.
      return Strings.format("[JEJAPD %s %s JEJAPD]", enumClass, validValuesToExplanations);
    }

  }

}
