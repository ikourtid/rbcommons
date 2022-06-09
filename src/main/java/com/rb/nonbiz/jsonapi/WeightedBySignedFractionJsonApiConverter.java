package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.types.WeightedBySignedFraction;

import java.util.function.Function;

import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonDoubleOrThrow;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.WeightedBySignedFraction.weightedBySignedFraction;

/**
 * Converts {@link WeightedBySignedFraction} back and forth to JSON for our public API.
 *
 * This does not implement JsonRoundTripConverter because we need to supply serializers and deserializers.
 */
public class WeightedBySignedFractionJsonApiConverter {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties("item", "weight")
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public <T> JsonObject toJsonObject(
      WeightedBySignedFraction<T> weightedBySignedFraction,
      Function<T, JsonElement> serializer) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setDouble(
                "weight",
                weightedBySignedFraction.getWeight().doubleValue())
            // translates a <T> to a JsonElement (a JSON string, double, object, etc.)
            .set(
                "item",
                serializer.apply(weightedBySignedFraction.getItem()))
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <T> WeightedBySignedFraction<T> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonElement, T> deserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    return weightedBySignedFraction(
        // translates a JsonElement (a JSON string, double, object, etc.) to a <T>
        deserializer.apply(jsonObject.get("item")),
        signedFraction(getJsonDoubleOrThrow(jsonObject, "weight")));
  }

}
