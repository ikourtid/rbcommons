package com.rb.nonbiz.jsonapi;

import com.google.common.collect.Range;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.json.RBJsonObjects;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBRanges.hasEitherBoundOpen;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getOptionalJsonPrimitive;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLine;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Convert a {@link Range} back and forth to JSON for our public API.
 *
 * <p> Note that this only converts ranges that *include* any endpoints they may have. That is,
 * (-inf, inf), [1, inf), (-inf, 10] and [1, 10] can all be converted because their boundary points
 * are included. </p>
 *
 * <p> Conversely, ranges that *exclude* an endpoint cannot be converted here. E.g. converting ranges
 * such as (-inf, 10), (1, inf), (1, 10], [1, 10) is not supported here. </p>
 *
 * <p> Note that our notation is not entirely clear, because we use "closed" when refering to both ranges
 * and individual boundaries. We use {@link ClosedRange} to mean a range with both
 * a lower bound and an upper bound, with both bounds being "closed" (e.g. inclusive). We also refer to a
 * range with only one boundary (e.g. Range.atLeast(1), Range.atMost(10)) as having a "closed" bound. </p>
 *
 * <p> In order to convert a {@link ClosedRange} to JSON, use {@link RBJsonObjects#closedRangeToJsonObject}. </p>
 *
 * @see ClosedRange
 * @see RBJsonObjects#closedRangeToJsonObject
 */
public class RangeJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .hasNoRequiredProperties()
      .setOptionalProperties(rbMapOf(
          "min", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
          "max", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR))
      .build();

  @Inject JsonValidator jsonValidator;

  public <C extends Comparable<? super C>> JsonObject toJsonObject(
      Range<C> range,
      Function<C, JsonPrimitive> serializer) {
    // Since fromJsonObject() will create ranges with closed bounds, make sure
    // that we aren't converting a range with open bounds here.
    RBPreconditions.checkArgument(
        !hasEitherBoundOpen(range),
        "Neither bound may be both present and open: %s",
        range);

    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setIf("min", range, r -> r.hasLowerBound(), r -> serializer.apply(r.lowerEndpoint()))
            .setIf("max", range, r -> r.hasUpperBound(), r -> serializer.apply(r.upperEndpoint()))
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <C extends Comparable<? super C>> Range<C> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonPrimitive, C> deserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    Optional<JsonPrimitive> maybeMin = getOptionalJsonPrimitive(jsonObject, "min");
    Optional<JsonPrimitive> maybeMax = getOptionalJsonPrimitive(jsonObject, "max");

    if (maybeMin.isPresent()) {
      C minValue = deserializer.apply(maybeMin.get());
      if (maybeMax.isPresent()) {
        // has both min and max
        C maxValue = deserializer.apply(maybeMax.get());
        return Range.closed(minValue, maxValue);
      } else {
        // has min but no max
        return Range.atLeast(minValue);
      }
    } else {
      if (maybeMax.isPresent()) {
        // no min but has max
        C maxValue = deserializer.apply(maybeMax.get());
        return Range.atMost(maxValue);
      } else {
        // no min or max; return the open range with no bounds
        return Range.all();
      }
    }
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(Range.class)
        .setSingleLineSummary(documentation("A range holds an optional lower bound and an optional upper bound."))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "<p> This only supports ranges for which either endpoint, if present, is part of the range.",
            "That is, the closed range [1, 10] is supported, but the semi-open range (1, 10] (which excludes the point 1)",
            "is not. </p>",
            "<p> Omit the 'min' property to signify a range extending down to -inf, and omit the 'max' property to ",
            "signify a range extending up to +inf. </p>",
            "<p> Both 'min' and 'max' can be omitted to specify an unlimited range. </p>")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "min", jsonDouble(-1.1),
            "max", jsonDouble( 9.9)))
        .build();
  }

}
