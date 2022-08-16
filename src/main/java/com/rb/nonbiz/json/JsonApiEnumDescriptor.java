package com.rb.nonbiz.json;

import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.EnumMap;

import static com.rb.biz.types.StringFunctions.isAllWhiteSpace;

/**
 * We often serialize a Java enum by using strings that are similar in meaning to the Java identifier, but
 * possibly looking different (e.g. JSON API tends to use camelcase instead of capitalized snake case in Java).
 * We have to do this, because if we ever rename the Java enum class or its enum values, we
 * don't want the API to change, as others may be relying on those specific strings.
 * <p>
 * For those JSON properties, we should be using this.
 */
public class JsonApiEnumDescriptor<E extends Enum<E>> {

  /**
   * Not all enum values are guaranteed to be serializable. Ideally the API would support everything, but there are
   * sometimes old deprecated enum values that we expressly do not want to be part of the API.
   * <p>
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

  private JsonApiEnumDescriptor(
      Class<E> enumClass,
      EnumMap<E, JavaEnumSerializationAndExplanation> validValuesToExplanations) {
    this.enumClass = enumClass;
    this.validValuesToExplanations = validValuesToExplanations;
  }

  public static <E extends Enum<E>> JsonApiEnumDescriptor<E> jsonApiEnumDescriptor(
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
    return new JsonApiEnumDescriptor<>(enumClass, validValuesToExplanations);
  }

  public Class<E> getEnumClass() {
    return enumClass;
  }

  public EnumMap<? extends Enum<?>, JavaEnumSerializationAndExplanation> getValidValuesToExplanations() {
    return validValuesToExplanations;
  }

  @Override
  public String toString() {
    // Can't use formatMap because this is an EnumMap, not a RBMap.
    return Strings.format("[JEJAPD %s %s JEJAPD]", enumClass, validValuesToExplanations);
  }

}
