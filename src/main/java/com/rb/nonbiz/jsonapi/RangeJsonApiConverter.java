package com.rb.nonbiz.jsonapi;

import com.google.common.collect.Range;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.json.RBJsonObjectBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBRanges.hasEitherBoundOpen;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getOptionalJsonPrimitive;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentation.JsonApiDocumentationBuilder.intermediateJsonApiDocumentationWithFixme;

/**
 * Convert a Range back and forth to JSON for our public API.
 *
 * <p> Note that this only returns closed ranges. That is, the ranges include their endpoints. </p>
 */
public class RangeJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .hasNoRequiredProperties()
      .setOptionalProperties("min", "max")
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

    RBJsonObjectBuilder builder = rbJsonObjectBuilder();
    if (range.hasLowerBound()) {
      builder.setJsonPrimitive("min", serializer.apply(range.lowerEndpoint()));
    }
    if (range.hasUpperBound()) {
      builder.setJsonPrimitive("max", serializer.apply(range.upperEndpoint()));
    }

    return jsonValidator.validate(
        builder.build(),
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
    return intermediateJsonApiDocumentationWithFixme(Range.class);
  }

}
