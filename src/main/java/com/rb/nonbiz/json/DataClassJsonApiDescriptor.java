package com.rb.nonbiz.json;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.EnumMap;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.text.Strings.formatMapInKeyOrder;

/**
 * This is helpful in the JSON API documentation (OpenAPI / Swagger). It gives us type information for a property of a
 * JSON object in the JSON API serialization.
 */
public abstract class DataClassJsonApiDescriptor {

  public interface Visitor<T> {

    T visitSimpleClassJsonApiDescriptor(SimpleClassJsonApiDescriptor simpleClassJsonApiDescriptor);
    T visitUniqueIdJsonApiDescriptor(UniqueIdJsonApiDescriptor uniqueIdJsonApiDescriptor);
    T visitIidMapJsonApiDescriptor(IidMapJsonApiDescriptor iidMapJsonApiDescriptor);
    T visitRBMapJsonApiDescriptor(RBMapJsonApiDescriptor rbMapJsonApiDescriptor);
    T visitCollectionJsonApiDescriptor(CollectionJsonApiDescriptor collectionJsonApiDescriptor);
    T visitYearlyTimeSeriesJsonApiDescriptor(YearlyTimeSeriesJsonApiDescriptor yearlyTimeSeriesJsonApiDescriptor);
    T visitPseudoEnumJsonApiDescriptor(PseudoEnumJsonApiDescriptor pseudoEnumJsonApiDescriptor);
    T visitJavaEnumJsonApiDescriptor(JavaEnumJsonApiDescriptor javaEnumJsonApiDescriptor);

  }

  public abstract <T> T visit(Visitor<T> visitor);


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
      rbSetOf(
          BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
          PreciseValue.class, // we always want to describe a specific subclass of PreciseValue

          Strings.class,      // a common misspelling of String.class,
          InstrumentId.class, // should use JsonTicker instead
          Symbol.class,       // should use JsonTicker instead

          // These 4 have their own JsonApiDescriptor classes, which we should be using.
          UniqueId.class,
          IidMap.class,
          RBMap.class,
          RBSet.class)
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
   * Tells us the type of a property of a JsonObject in the JSON API, in the simplest case
   * where it is a single JSON API data class (e.g. not some collection).
   */
  public static class UniqueIdJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> clazz;

    private UniqueIdJsonApiDescriptor(Class<?> clazz) {
      this.clazz = clazz;
    }

    public static UniqueIdJsonApiDescriptor uniqueIdJsonApiDescriptor(Class<?> clazz) {
      return new UniqueIdJsonApiDescriptor(clazz);
    }

    /**
     * This cannot be called getClass because it's an existing method in java.lang.Object.
     */
    public Class<?> getClassOfId() {
      return clazz;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitUniqueIdJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[UIJAD %s UIJAD]", clazz);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an IidMap of some Java data class.
   */
  public static class IidMapJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> iidMapValueClass;

    private IidMapJsonApiDescriptor(Class<?> iidMapValueClass) {
      this.iidMapValueClass = iidMapValueClass;
    }

    public static IidMapJsonApiDescriptor iidMapJsonApiDescriptor(Class<?> iidMapValueClass) {
      rbSetOf(
          BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
          PreciseValue.class, // we always want to describe a specific subclass of PreciseValue

          Strings.class,      // a common misspelling of String.class,
          InstrumentId.class, // sounds weird to have an IidMap that maps to an instrument
          Symbol.class,       // same as above

          // These 2 have their own JsonApiDescriptor classes, which we should be using.
          UniqueId.class,
          RBSet.class,

          // It's unlikely that we'll be mapping to another map, although not impossible.
          IidMap.class,
          RBMap.class)
          .forEach(clazz ->
              RBPreconditions.checkArgument(
                  !iidMapValueClass.equals(clazz),
                  "CollectionJsonApiDescriptor uses an invalid class of %s",
                  clazz));
      return new IidMapJsonApiDescriptor(iidMapValueClass);
    }

    public Class<?> getIidMapValueClass() {
      return iidMapValueClass;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitIidMapJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[IMJAD %s IMJAD]", iidMapValueClass);
    }

  }


  /**
   * Tells us the type of a property of a JsonObject in the JSON API, in the case
   * where it is the JSON representation of an RBMap of some Java data class.
   */
  public static class RBMapJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> rbMapKeyClass;
    private final Class<?> rbMapValueClass;

    private RBMapJsonApiDescriptor(Class<?> rbMapKeyClass, Class<?> rbMapValueClass) {
      this.rbMapKeyClass = rbMapKeyClass;
      this.rbMapValueClass = rbMapValueClass;
    }

    public static RBMapJsonApiDescriptor rbMapJsonApiDescriptor(Class<?> rbMapKeyClass, Class<?> rbMapValueClass) {
      rbSetOf(
          BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
          PreciseValue.class, // we always want to describe a specific subclass of PreciseValue

          Strings.class,      // a common misspelling of String.class,
          InstrumentId.class, // should use JsonTicker instead
          Symbol.class,       // should use JsonTicker instead

          // These 3 have their own JsonApiDescriptor classes, which we should be using.
          IidMap.class,
          RBMap.class,
          RBSet.class)
          .forEach(clazz -> {
            RBPreconditions.checkArgument(
                !rbMapKeyClass.equals(clazz),
                "RBMapJsonApiDescriptor for %s -> %s : invalid key class",
                rbMapKeyClass, rbMapValueClass);
            RBPreconditions.checkArgument(
                !rbMapValueClass.equals(clazz),
                "RBMapJsonApiDescriptor for %s -> %s : invalid value class",
                rbMapKeyClass, rbMapValueClass);
          });
      return new RBMapJsonApiDescriptor(rbMapKeyClass, rbMapValueClass);
    }

    public Class<?> getRbMapKeyClass() {
      return rbMapKeyClass;
    }

    public Class<?> getRBMapValueClass() {
      return rbMapValueClass;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitRBMapJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[RMJAD %s -> %s RMJAD]", rbMapKeyClass, rbMapValueClass);
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
      rbSetOf(
          BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
          PreciseValue.class, // we always want to describe a specific subclass of PreciseValue

          Strings.class,      // a common misspelling of String.class,
          InstrumentId.class, // should use JsonTicker instead
          Symbol.class,       // should use JsonTicker instead

          UniqueId.class)
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
   * where it is the JSON representation of a YearlyTimeSeries of some Java data class.
   */
  public static class YearlyTimeSeriesJsonApiDescriptor extends DataClassJsonApiDescriptor {

    private final Class<?> yearlyTimeSeriesValueClass;

    private YearlyTimeSeriesJsonApiDescriptor(Class<?> yearlyTimeSeriesValueClass) {
      this.yearlyTimeSeriesValueClass = yearlyTimeSeriesValueClass;
    }

    public static YearlyTimeSeriesJsonApiDescriptor yearlyTimeSeriesJsonApiDescriptor(Class<?> arrayValueClass) {
      return new YearlyTimeSeriesJsonApiDescriptor(arrayValueClass);
    }

    public Class<?> getYearlyTimeSeriesValueClass() {
      return yearlyTimeSeriesValueClass;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return visitor.visitYearlyTimeSeriesJsonApiDescriptor(this);
    }

    @Override
    public String toString() {
      return Strings.format("[CJAD %s CJAD]", yearlyTimeSeriesValueClass);
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

    private final RBMap<String, HumanReadableLabel> validValuesToExplanations;

    private PseudoEnumJsonApiDescriptor(RBMap<String, HumanReadableLabel> validValuesToExplanations) {
      this.validValuesToExplanations = validValuesToExplanations;
    }

    public static PseudoEnumJsonApiDescriptor pseudoEnumJsonApiDescriptor(
        RBMap<String, HumanReadableLabel> validValuesToExplanations) {
      RBPreconditions.checkArgument(
          validValuesToExplanations.size() >= 2,
          "There must be at least 2 items here: %s",
          validValuesToExplanations);
      validValuesToExplanations
          .forEachEntry( (pseudoEnumString, explanationLabel) -> {
            RBPreconditions.checkArgument(
                !pseudoEnumString.equals(""),
                "No 'pseudo-enum' string key may be empty: %s",
                validValuesToExplanations);
            RBPreconditions.checkArgument(
                !explanationLabel.getLabelText().equals(""),
                "No explanation may be empty: %s",
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

    public JavaEnumJsonApiDescriptor(
        EnumMap<? extends Enum<?>, JavaEnumSerializationAndExplanation> validValuesToExplanations) {
      this.validValuesToExplanations = validValuesToExplanations;
    }

    public static JavaEnumJsonApiDescriptor javaEnumJsonApiDescriptor(
        EnumMap<? extends Enum<?>, JavaEnumSerializationAndExplanation> validValuesToExplanations) {
      RBPreconditions.checkArgument(
          validValuesToExplanations.size() >= 2,
          "There must be at least 2 items here: %s",
          validValuesToExplanations);
      validValuesToExplanations
          .values()
          .forEach(javaEnumSerializationAndExplanation -> {
            RBPreconditions.checkArgument(
                !javaEnumSerializationAndExplanation.getJsonSerialization().equals(""),
                "No 'pseudo-enum' string key may be empty: %s",
                validValuesToExplanations);
            RBPreconditions.checkArgument(
                !javaEnumSerializationAndExplanation.getExplanation().getLabelText().equals(""),
                "No explanation may be empty: %s",
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
