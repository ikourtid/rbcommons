package com.rb.nonbiz.jsonapi;

import com.google.common.collect.Range;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.types.RBNumeric;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;

import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonPrimitiveOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getOptionalJsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getOptionalJsonElement;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentation.JsonApiDocumentationBuilder.intermediateJsonApiDocumentationWithFixme;

/**
 * Convert a Range back and forth to JSON for our public API.
 */
public class RangeJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .hasNoRequiredProperties()
      .setOptionalProperties("min", "max")
      .build();

  @Inject JsonValidator jsonValidator;

  public <T extends RBNumeric<? super T>> JsonObject toJsonObject(
      Range<T> range,
      Function<T, JsonPrimitive> serializer) {
    return null;
  }

  public <T extends RBNumeric<? super T>> Range<T> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonPrimitive, T> deserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);


    boolean hasMin = jsonObject.has("min");
    boolean hasMax = jsonObject.has("max");

    if (hasMin) {
      T minValue = deserializer.apply(getJsonPrimitiveOrThrow(jsonObject, "min") );
      if (hasMax) {
        // has both min and max
        T maxValue = deserializer.apply(getJsonPrimitiveOrThrow(jsonObject, "max"));
        return Range.closed(minValue, maxValue);
      } else {
        // has min but no max
        return Range.atLeast(minValue);
      }
    } else {
      if (hasMax) {
        // no min but has max
        T maxValue = deserializer.apply(getJsonPrimitiveOrThrow(jsonObject, "max"));
        return Range.atMost(maxValue);
      } else {
        // no min or max
        return Range.all();
      }
    }
  }


  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return intermediateJsonApiDocumentationWithFixme(Range.class);
  }

}
