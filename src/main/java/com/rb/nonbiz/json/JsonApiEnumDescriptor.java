package com.rb.nonbiz.json;

import com.rb.nonbiz.collections.RBEnumMap;
import com.rb.nonbiz.text.HasHumanReadableDocumentation;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.JsonRoundTripStringConvertibleEnum;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.biz.types.StringFunctions.isAllWhiteSpace;
import static com.rb.nonbiz.util.RBEnumMaps.rbEnumMapCoveringAllEnumValues;

/**
 * We often serialize a Java enum by using strings that are similar in meaning to the Java identifier, but
 * possibly looking different (e.g. JSON API tends to use camelcase instead of capitalized snake case in Java).
 * We have to do this, because if we ever rename the Java enum class or its enum values, we
 * don't want the API to change, as others may be relying on those specific strings.
 *
 * <p> For those JSON properties, we should be using this. </p>
 */
public class JsonApiEnumDescriptor<E extends Enum<E> & JsonRoundTripStringConvertibleEnum<E>> {

  private final Class<E> enumClass;
  private final RBEnumMap<E, HumanReadableDocumentation> validValuesToExplanations;

  private JsonApiEnumDescriptor(
      Class<E> enumClass,
      RBEnumMap<E, HumanReadableDocumentation> validValuesToExplanations) {
    this.enumClass = enumClass;
    this.validValuesToExplanations = validValuesToExplanations;
  }

  public static <E extends Enum<E> & JsonRoundTripStringConvertibleEnum<E>> JsonApiEnumDescriptor<E> jsonApiEnumDescriptor(
      Class<E> enumClass,
      RBEnumMap<E, HumanReadableDocumentation> validValuesToExplanations) {
    // The 1 in the following precondition (vs. e.g. 2+)
    // is for the rare cases where we only allow one value of the enum in the JSON API.
    // You might wonder - why bother serializing such an enum in the first place? Well, this would allow us to
    // support more enum values later if we decide to.
    RBPreconditions.checkArgument(
        !validValuesToExplanations.isEmpty(),
        "There must be at least 1 item here: %s",
        validValuesToExplanations);
    validValuesToExplanations
        .forEachEntryInKeyOrder( (enumConstant, documentation) -> {
          RBPreconditions.checkArgument(
              !isAllWhiteSpace(enumConstant.toUniqueStableString()),
              "No Java enum serialized string representation in the API may be all whitespace (which includes the empty string): %s",
              validValuesToExplanations);
          RBPreconditions.checkArgument(
              !isAllWhiteSpace(documentation.getAsString()),
              "No explanation may be all whitespace (which includes the empty string): %s",
              validValuesToExplanations);
        });

    RBPreconditions.checkArgument(
        enumClass.isEnum(),
        // 'internal error' because this should never happen due to the builder having <E extends Enum<E>>.
        // But let's check it, just in case.
        "Internal error: class %s must be an enum!",
        enumClass);
    return new JsonApiEnumDescriptor<>(enumClass, validValuesToExplanations);
  }

  /**
   * Creates a {@link JsonApiEnumDescriptor} for the cases where the enum class themselves specify their own
   * documentation, and when the JSON API semantics are such that we want to expose all enum values, not just a subset.
   */
  public static <E extends Enum<E> & JsonRoundTripStringConvertibleEnum<E> & HasHumanReadableDocumentation>
  JsonApiEnumDescriptor<E> simpleJsonApiEnumDescriptor(
      Class<E> enumClass) {
    return jsonApiEnumDescriptor(enumClass, rbEnumMapCoveringAllEnumValues(
        enumClass,
        enumValue -> enumValue.getDocumentation()));
  }

  public Class<E> getEnumClass() {
    return enumClass;
  }

  public RBEnumMap<E, HumanReadableDocumentation> getValidValuesToExplanations() {
    return validValuesToExplanations;
  }

  @Override
  public String toString() {
    // Can't use formatMap because this is an EnumMap, not a RBMap.
    return Strings.format("[JAEDesc %s %s JAEDesc]",
        enumClass.getSimpleName(), validValuesToExplanations);
  }

}
