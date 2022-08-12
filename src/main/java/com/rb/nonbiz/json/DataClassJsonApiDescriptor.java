package com.rb.nonbiz.json;

import com.google.common.base.Joiner;
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

import static com.rb.biz.types.StringFunctions.isAllWhiteSpace;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.Strings.formatMapInKeyOrder;

/**
 * This is helpful in the JSON API documentation (OpenAPI / Swagger). It gives us type information for a property of a
 * JSON object in the JSON API serialization.
 */
public abstract class DataClassJsonApiDescriptor {

  public interface Visitor<T> {

    T visitSimpleClassJsonApiDescriptor(SimpleClassJsonApiDescriptor simpleClassJsonApiDescriptor);
    T visitIidMapJsonApiDescriptor(IidMapJsonApiDescriptor iidMapJsonApiDescriptor);
    T visitRBMapJsonApiDescriptor(RBMapJsonApiDescriptor rbMapJsonApiDescriptor);
    T visitCollectionJsonApiDescriptor(CollectionJsonApiDescriptor collectionJsonApiDescriptor);
    T visitJavaGenericJsonApiDescriptor(JavaGenericJsonApiDescriptor javaGenericJsonApiDescriptor);
    T visitPseudoEnumJsonApiDescriptor(PseudoEnumJsonApiDescriptor pseudoEnumJsonApiDescriptor);
    T visitJavaEnumJsonApiDescriptor(JavaEnumJsonApiDescriptor javaEnumJsonApiDescriptor);

  }

  public abstract <T> T visit(Visitor<T> visitor);

  protected static RBSet<Class<?>> getInvalidJsonApiDescriptorClasses() {
    return rbSetOf(
        BigDecimal.class,     // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class,   // we always want to describe a specific subclass of PreciseValue
        ImpreciseValue.class, // same

        Strings.class,        // a common misspelling of String.class
        InstrumentId.class,   // sounds weird to have an IidMap that maps to an instrument
        Symbol.class);        // same as above
  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the simplest case
   * where it is a single JSON API data class (e.g. not some collection).
   */
  public static class SimpleClassJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> clazz;

    private SimpleClassJsonApiDescriptor(Class<?> clazz) {
      this.clazz = clazz;
    }

    public static SimpleClassJsonApiDescriptor simpleClassJsonApiDescriptor(Class<?> clazz) {
      RBSets.union(
              getInvalidJsonApiDescriptorClasses(),
              rbSetOf(
                  // These 4 have their own JsonApiDescriptor classes, which we should be using.
                  UniqueId.class,
                  IidMap.class,
                  RBMap.class,
                  RBSet.class))
          .forEach(badClass ->
              RBPreconditions.checkArgument(
                  !clazz.equals(badClass),
                  "SimpleClassJsonApiDescriptor has bad class %s",
                  clazz));
      return new SimpleClassJsonApiDescriptor(clazz);
    }

    /**
     * This cannot be called getClass because it's an existing method in java.lang.Object.
     */
    public Class<?> getClassBeingDescribed() {
      return clazz;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitSimpleClassJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[SCJAD %s SCJAD]", clazz);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an IidMap of some Java data class.
   */
  public static class IidMapJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final DataClassJsonApiDescriptor valueClassDescriptor;

    private IidMapJsonApiDescriptor(DataClassJsonApiDescriptor valueClassDescriptor) {
      this.valueClassDescriptor = valueClassDescriptor;
    }

    public static IidMapJsonApiDescriptor iidMapJsonApiDescriptor(DataClassJsonApiDescriptor valueClass) {
      /* FIXME IAK YAML
      RBSets.union(
              getInvalidJsonApiDescriptorClasses(),
              rbSetOf(
                  // These 2 have their own JsonApiDescriptor classes, which we should be using.
                  UniqueId.class,
                  RBSet.class,

                  // It's unlikely that we'll be mapping to another map, although not impossible.
                  IidMap.class,
                  RBMap.class))
          .forEach(clazz ->
              RBPreconditions.checkArgument(
                  !valueClass.equals(clazz),
                  "IidMapJsonApiDescriptor uses an invalid class of %s",
                  clazz));

       */
      return new IidMapJsonApiDescriptor(valueClass);
    }

    public DataClassJsonApiDescriptor getValueClassDescriptor() {
      return valueClassDescriptor;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitIidMapJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[IMJAD %s IMJAD]", valueClassDescriptor);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an RBMap of some Java data class.
   */
  public static class RBMapJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final DataClassJsonApiDescriptor keyClassDescriptor;
    private final DataClassJsonApiDescriptor valueClassDescriptor;

    private RBMapJsonApiDescriptor(
        DataClassJsonApiDescriptor keyClassDescriptor,
        DataClassJsonApiDescriptor valueClassDescriptor) {
      this.keyClassDescriptor = keyClassDescriptor;
      this.valueClassDescriptor = valueClassDescriptor;
    }

    public static RBMapJsonApiDescriptor rbMapJsonApiDescriptor(
        DataClassJsonApiDescriptor keyClass,
        DataClassJsonApiDescriptor valueClass) {
      /* FIXME IAK YAML
      RBSets.union(
              getInvalidJsonApiDescriptorClasses(),
              rbSetOf(
                  // These 3 have their own JsonApiDescriptor classes, which we should be using.
                  IidMap.class,
                  RBMap.class,
                  RBSet.class))
          .forEach(clazz -> {
            RBPreconditions.checkArgument(
                !keyClass.equals(clazz),
                "RBMapJsonApiDescriptor for %s -> %s : invalid key class",
                keyClass, valueClass);
            RBPreconditions.checkArgument(
                !valueClass.equals(clazz),
                "RBMapJsonApiDescriptor for %s -> %s : invalid value class",
                keyClass, valueClass);
          });

       */
      return new RBMapJsonApiDescriptor(keyClass, valueClass);
    }

    public DataClassJsonApiDescriptor getKeyClassDescriptor() {
      return keyClassDescriptor;
    }

    public DataClassJsonApiDescriptor getValueClassDescriptor() {
      return valueClassDescriptor;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitRBMapJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[RMJAD %s -> %s RMJAD]", keyClassDescriptor, valueClassDescriptor);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an collection (Set, List, or array) of some Java data class.
   */
  public static class CollectionJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> collectionValueClass;

    private CollectionJsonApiDescriptor(Class<?> collectionValueClass) {
      this.collectionValueClass = collectionValueClass;
    }

    public static CollectionJsonApiDescriptor collectionJsonApiDescriptor(Class<?> arrayValueClass) {
      RBSets.union(
              getInvalidJsonApiDescriptorClasses(),
              singletonRBSet(UniqueId.class))
          .forEach(clazz ->
              RBPreconditions.checkArgument(
                  !arrayValueClass.equals(clazz),
                  "CollectionJsonApiDescriptor uses an invalid class of %s",
                  clazz));
      return new CollectionJsonApiDescriptor(arrayValueClass);
    }

    public Class<?> getCollectionValueClass() {
      return collectionValueClass;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitCollectionJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[CJAD %s CJAD]", collectionValueClass);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of a java generic such as {@code Foo<T>}.
   *
   * It should only be used when T is an actual data class that has a JSON serialization. Example:
   * {@code UniqueId<NamedFactor>}. It should not be used for 'marker interface' classes, such as
   * {@code Portfolio<HeldByUs>}. This makes sense, because HeldByUs is not something that gets serialized.
   */
  public static class JavaGenericJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> outerClass;
    private final List<Class<?>> genericArgumentClasses;

    private JavaGenericJsonApiDescriptor(Class<?> outerClass, List<Class<?>> genericArgumentClasses) {
      this.outerClass = outerClass;
      this.genericArgumentClasses = genericArgumentClasses;
    }

    private static JavaGenericJsonApiDescriptor javaGenericJsonApiDescriptor(
        Class<?> outerClass, List<Class<?>> genericArgumentClasses) {
      for (Class<?> innerClass : genericArgumentClasses) {
        RBPreconditions.checkArgument(
            !outerClass.equals(innerClass),
            "Outer and generic argument class of generic shouldn't be the same: %s vs. %s : %s",
            outerClass, innerClass, genericArgumentClasses);
      }
      // Ideally, we want to restrict the usage of this class (JavaGenericJsonApiDescriptor)
      // to cases where the 'outer' class is generic on one or more 'generic argument' classes.
      // However, due to Java type erasure, we don't know at runtime what's generic and what's not.
      // But let's just add a few obvious exceptions that we know will never be true.
      RBSets.union(
              getInvalidJsonApiDescriptorClasses(),
              rbSetOf(
                  // These have their own JsonApiDescriptor classes, which we should be using.
                  RBSet.class,
                  IidSet.class,
                  IidMap.class,
                  RBMap.class))
          .forEach(badOuterClass -> RBPreconditions.checkArgument(
              !outerClass.equals(badOuterClass),
              "Outer class %s is invalid",
              outerClass));
      // We can't test the following, because this is a private static constructor, and the only way to construct a
      // JavaGenericJsonApiDescriptor is through some other static constructors that can't allow this to happen,
      // but let's keep it anyway.
      RBPreconditions.checkArgument(
          !genericArgumentClasses.isEmpty(),
          "JavaGenericJsonApiDescriptor describes a generic class '%s' without generic arguments",
          outerClass);
      return new JavaGenericJsonApiDescriptor(outerClass, genericArgumentClasses);
    }

    /**
     * Represents e.g. a {@code NetGain<LongTerm>}, where NetGain is the outer class, and LongTerm is the inner class
     * (2nd argument).
     *
     * We normally use a builder when there can be two arguments of the same type, but this is meant to be used
     * inline in the various definitions of JSON_VALIDATION_INSTRUCTIONS in the JSON API converter verb classes,
     * so we want its invocation to look short.
     */
    public static JavaGenericJsonApiDescriptor javaGenericJsonApiDescriptor(
        Class<?> outerClass, Class<?> first, Class<?> ... rest) {
      return javaGenericJsonApiDescriptor(outerClass, concatenateFirstAndRest(first, rest));
    }

    /**
     * A shorthand for the case of {@link UniqueId}.
     */
    public static JavaGenericJsonApiDescriptor uniqueIdJsonApiDescriptor(Class<?> innerClass) {
      return javaGenericJsonApiDescriptor(UniqueId.class, innerClass);
    }

    public Class<?> getOuterClass() {
      return outerClass;
    }

    public List<Class<?>> getGenericArgumentClasses() {
      return genericArgumentClasses;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitJavaGenericJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[JGJAD %s < %s > JGJAD]",
          outerClass,
          Joiner.on(" , ").join(genericArgumentClasses));
    }

  }


  /**
   * We often serialize a base class with multiple subclasses by using a string key in the JSON to represent the
   * subclass's type. Example: NaiveSubObjectiveFormulationDetailsJsonApiConverter. The strings in the JSON may not
   * be exact matches to Java classes - and anyway, they shouldn't be, because if we rename Java classes, we
   * don't want the API to change, as others may be relying on those specific strings.
   * Also, there are other cases where we use a special string in the JSON API
   * to represent some special values (e.g. GlobalObjectiveThreshold where the threshold always passes).
   *
   * For those JSON properties, we should be using this.
   *
   * Note that this does not represent an actual enum.
   */
  public static class PseudoEnumJsonApiDescriptor extends DataClassJsonApiDescriptor {

    // This should be removed once we have human-readable descriptions for all instances where we use it.
    // The reason it exists is that it helps us avoid having fixmes in multiple places.
    public static HumanReadableLabel undefinedPseudoEnumJsonApiDescription() {
      return label("FIXME SWA JSONDOC");
    }

    private final RBMap<String, HumanReadableLabel> validValuesToExplanations;

    private PseudoEnumJsonApiDescriptor(RBMap<String, HumanReadableLabel> validValuesToExplanations) {
      this.validValuesToExplanations = validValuesToExplanations;
    }

    public static PseudoEnumJsonApiDescriptor pseudoEnumJsonApiDescriptor(
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
      return new PseudoEnumJsonApiDescriptor(validValuesToExplanations);
    }

    public RBMap<String, HumanReadableLabel> getValidValuesToExplanations() {
      return validValuesToExplanations;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitPseudoEnumJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[PEJAD %s PEJAD]",
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
  public static class JavaEnumJsonApiDescriptor extends DataClassJsonApiDescriptor {

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
        return Strings.format("[JESAE %s %s JESAE]", jsonSerialization, explanation);
      }

    }


    private final EnumMap<? extends Enum<?>, JavaEnumSerializationAndExplanation> validValuesToExplanations;

    private JavaEnumJsonApiDescriptor(
        EnumMap<? extends Enum<?>, JavaEnumSerializationAndExplanation> validValuesToExplanations) {
      this.validValuesToExplanations = validValuesToExplanations;
    }

    public static JavaEnumJsonApiDescriptor javaEnumJsonApiDescriptor(
        EnumMap<? extends Enum<?>, JavaEnumSerializationAndExplanation> validValuesToExplanations) {
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
      return new JavaEnumJsonApiDescriptor(validValuesToExplanations);
    }

    public EnumMap<? extends Enum<?>, JavaEnumSerializationAndExplanation> getValidValuesToExplanations() {
      return validValuesToExplanations;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitJavaEnumJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      // Can't use formatMap because this is an EnumMap, not a RBMap.
      return Strings.format("[JEJAD %s JEJAD]", validValuesToExplanations);
    }

  }

}
