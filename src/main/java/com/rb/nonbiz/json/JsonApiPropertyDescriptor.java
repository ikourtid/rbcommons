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
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.rb.biz.types.StringFunctions.isAllWhiteSpace;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.formatMapInKeyOrder;
import static com.rb.nonbiz.text.Strings.formatOptional;

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

  }


  public abstract Optional<JsonPropertySpecificDocumentation> getPropertySpecificDocumentation();

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
    private final Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation;

    private SimpleClassJsonApiPropertyDescriptor(
        Class<?> clazz,
        Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
      this.clazz = clazz;
      this.jsonPropertySpecificDocumentation  = jsonPropertySpecificDocumentation;
    }

    private static SimpleClassJsonApiPropertyDescriptor simpleClassJsonApiPropertyDescriptor(
        Class<?> clazz,
        Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
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
      return new SimpleClassJsonApiPropertyDescriptor(clazz, jsonPropertySpecificDocumentation);
    }

    public static SimpleClassJsonApiPropertyDescriptor simpleClassJsonApiPropertyDescriptor(
        Class<?> clazz) {
      return simpleClassJsonApiPropertyDescriptor(clazz, Optional.empty());
    }

    public static SimpleClassJsonApiPropertyDescriptor simpleClassJsonApiPropertyDescriptor(
        Class<?> clazz, JsonPropertySpecificDocumentation jsonPropertySpecificDocumentation) {
      return simpleClassJsonApiPropertyDescriptor(clazz, Optional.of(jsonPropertySpecificDocumentation));
    }

    /**
     * This is just a typesafe & explicit wrapper for the case of enums, which is just a special case.
     */
    public static <E extends Enum<E>> SimpleClassJsonApiPropertyDescriptor enumJsonApiPropertyDescriptor(
        Class<E> enumClass) {
      // We can use Optional.empty() for the property-specific documentation, because enums only have
      // class-specific documentation; they don't have properties. An *object* that has a property whose type
      // is this enum *could* have its own documentation, but that's different.
      return simpleClassJsonApiPropertyDescriptor(enumClass, Optional.empty());
    }

    /**
     * This cannot be called getClass because it's an existing method in java.lang.Object.
     */
    public Class<?> getClassBeingDescribed() {
      return clazz;
    }

    @Override
    public Optional<JsonPropertySpecificDocumentation> getPropertySpecificDocumentation() {
      return jsonPropertySpecificDocumentation;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitSimpleClassJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[SCJAPD %s %s SCJAPD]",
          clazz, formatOptional(jsonPropertySpecificDocumentation));
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
    private final Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation;

    private IidMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor valueClassDescriptor,
        Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
      this.valueClassDescriptor = valueClassDescriptor;
      this.jsonPropertySpecificDocumentation  = jsonPropertySpecificDocumentation;
    }

    private static IidMapJsonApiPropertyDescriptor iidMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor valueClassDescriptor,
        Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
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
      return new IidMapJsonApiPropertyDescriptor(valueClassDescriptor, jsonPropertySpecificDocumentation);
    }

    public static IidMapJsonApiPropertyDescriptor iidMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor valueClassDescriptor) {
      return iidMapJsonApiPropertyDescriptor(valueClassDescriptor, Optional.empty());
    }

    public static IidMapJsonApiPropertyDescriptor iidMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor valueClassDescriptor,
        JsonPropertySpecificDocumentation jsonPropertySpecificDocumentation) {
      return iidMapJsonApiPropertyDescriptor(valueClassDescriptor, Optional.of(jsonPropertySpecificDocumentation));
    }

    public JsonApiPropertyDescriptor getValueClassDescriptor() {
      return valueClassDescriptor;
    }

    @Override
    public Optional<JsonPropertySpecificDocumentation> getPropertySpecificDocumentation() {
      return jsonPropertySpecificDocumentation;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitIidMapJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[IMJAPD %s %s IMJAPD]",
          valueClassDescriptor, formatOptional(jsonPropertySpecificDocumentation));
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
    private final Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation;

    private RBMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor keyClassDescriptor,
        JsonApiPropertyDescriptor valueClassDescriptor,
        Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
      this.keyClassDescriptor = keyClassDescriptor;
      this.valueClassDescriptor = valueClassDescriptor;
      this.jsonPropertySpecificDocumentation  = jsonPropertySpecificDocumentation;
    }

    private static RBMapJsonApiPropertyDescriptor rbMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor keyClassDescriptor,
        JsonApiPropertyDescriptor valueClassDescriptor,
        Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
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
      return new RBMapJsonApiPropertyDescriptor(
          keyClassDescriptor, valueClassDescriptor, jsonPropertySpecificDocumentation);
    }

    public static RBMapJsonApiPropertyDescriptor rbMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor keyClassDescriptor,
        JsonApiPropertyDescriptor valueClassDescriptor,
        JsonPropertySpecificDocumentation jsonPropertySpecificDocumentation) {
      return rbMapJsonApiPropertyDescriptor(
          keyClassDescriptor, valueClassDescriptor, Optional.of(jsonPropertySpecificDocumentation));
    }

    public static RBMapJsonApiPropertyDescriptor rbMapJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor keyClassDescriptor,
        JsonApiPropertyDescriptor valueClassDescriptor) {
      return rbMapJsonApiPropertyDescriptor(
          keyClassDescriptor, valueClassDescriptor, Optional.empty());
    }

    public JsonApiPropertyDescriptor getKeyClassDescriptor() {
      return keyClassDescriptor;
    }

    public JsonApiPropertyDescriptor getValueClassDescriptor() {
      return valueClassDescriptor;
    }

    @Override
    public Optional<JsonPropertySpecificDocumentation> getPropertySpecificDocumentation() {
      return jsonPropertySpecificDocumentation;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitRBMapJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[RMJAPD %s -> %s %s RMJAPD]",
          keyClassDescriptor, valueClassDescriptor, formatOptional(jsonPropertySpecificDocumentation));
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
    private final Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation;

    private CollectionJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor collectionValueClassDescriptor,
        Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
      this.collectionValueClassDescriptor = collectionValueClassDescriptor;
      this.jsonPropertySpecificDocumentation  = jsonPropertySpecificDocumentation;
    }

    private static CollectionJsonApiPropertyDescriptor collectionJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor arrayValueClassDescriptor,
        Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
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
      return new CollectionJsonApiPropertyDescriptor(arrayValueClassDescriptor,jsonPropertySpecificDocumentation);
    }

    public static CollectionJsonApiPropertyDescriptor collectionJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor arrayValueClassDescriptor,
        JsonPropertySpecificDocumentation jsonPropertySpecificDocumentation) {
      return collectionJsonApiPropertyDescriptor(
          arrayValueClassDescriptor, Optional.of(jsonPropertySpecificDocumentation));
    }

    public static CollectionJsonApiPropertyDescriptor collectionJsonApiPropertyDescriptor(
        JsonApiPropertyDescriptor arrayValueClassDescriptor) {
      return collectionJsonApiPropertyDescriptor(
          arrayValueClassDescriptor, Optional.empty());
    }

    public JsonApiPropertyDescriptor getCollectionValueClassDescriptor() {
      return collectionValueClassDescriptor;
    }

    @Override
    public Optional<JsonPropertySpecificDocumentation> getPropertySpecificDocumentation() {
      return jsonPropertySpecificDocumentation;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitCollectionJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[CJAPD %s %s CJAPD]",
          collectionValueClassDescriptor, formatOptional(jsonPropertySpecificDocumentation));
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
    private final Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation;

    private JavaGenericJsonApiPropertyDescriptor(
        Class<?> outerClass,
        List<JsonApiPropertyDescriptor> genericArgumentClassDescriptors,
        Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
      this.outerClass = outerClass;
      this.genericArgumentClassDescriptors = genericArgumentClassDescriptors;
      this.jsonPropertySpecificDocumentation  = jsonPropertySpecificDocumentation;
    }

    private static JavaGenericJsonApiPropertyDescriptor javaGenericJsonApiPropertyDescriptor(
        Class<?> outerClass,
        List<JsonApiPropertyDescriptor> genericArgumentClassDescriptors,
        Optional<JsonPropertySpecificDocumentation> jsonPropertySpecificDocumentation) {
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
      return new JavaGenericJsonApiPropertyDescriptor(
          outerClass, genericArgumentClassDescriptors,jsonPropertySpecificDocumentation);
    }

    public static JavaGenericJsonApiPropertyDescriptor javaGenericJsonApiPropertyDescriptor(
        Class<?> outerClass,
        List<JsonApiPropertyDescriptor> genericArgumentClassDescriptors,
        JsonPropertySpecificDocumentation jsonPropertySpecificDocumentation) {
      return javaGenericJsonApiPropertyDescriptor(
          outerClass, genericArgumentClassDescriptors, Optional.of(jsonPropertySpecificDocumentation));
    }

    public static JavaGenericJsonApiPropertyDescriptor javaGenericJsonApiPropertyDescriptor(
        Class<?> outerClass,
        List<JsonApiPropertyDescriptor> genericArgumentClassDescriptors) {
      return javaGenericJsonApiPropertyDescriptor(
          outerClass, genericArgumentClassDescriptors, Optional.empty());
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
    public Optional<JsonPropertySpecificDocumentation> getPropertySpecificDocumentation() {
      return jsonPropertySpecificDocumentation;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitJavaGenericJsonApiPropertyDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[JGJAPD %s < %s > %s JGJAPD]",
          outerClass,
          Joiner.on(" , ").join(genericArgumentClassDescriptors),
          formatOptional(jsonPropertySpecificDocumentation));
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
   * <p> Note that this does not represent an actual enum; for that, see {@link JsonApiEnumDescriptor}. </p>
   */
  public static class PseudoEnumJsonApiPropertyDescriptor extends JsonApiPropertyDescriptor {

    // This should be removed once we have human-readable descriptions for all instances where we use it.
    // The reason it exists is that it helps us avoid having fixmes in multiple places.
    public static HumanReadableDocumentation undefinedPseudoEnumJsonApiPropertyDescription() {
      return documentation("FIXME SWA JSONDOC");
    }

    private final RBMap<String, HumanReadableDocumentation> validValuesToExplanations;

    private PseudoEnumJsonApiPropertyDescriptor(
        RBMap<String, HumanReadableDocumentation> validValuesToExplanations) {
      this.validValuesToExplanations = validValuesToExplanations;
    }

    public static PseudoEnumJsonApiPropertyDescriptor pseudoEnumJsonApiPropertyDescriptor(
        RBMap<String, HumanReadableDocumentation> validValuesToExplanations) {
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
                !isAllWhiteSpace(explanationLabel.getAsString()),
                "No explanation may be all whitespace (which includes the empty string): %s",
                validValuesToExplanations);
          });
      return new PseudoEnumJsonApiPropertyDescriptor(validValuesToExplanations);
    }

    public RBMap<String, HumanReadableDocumentation> getValidValuesToExplanations() {
      return validValuesToExplanations;
    }

    /**
     * Because properties described by {@link PseudoEnumJsonApiPropertyDescriptor} never refer to some other class
     * that's defined elsewhere (and can therefore be linked to using $ref in the yaml file), there is no point
     * in having additional per-property documentation. That is, there's no separate per-class documentation to
     * distinguish from.
     */
    @Override
    public Optional<JsonPropertySpecificDocumentation> getPropertySpecificDocumentation() {
      return Optional.empty();
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


}
