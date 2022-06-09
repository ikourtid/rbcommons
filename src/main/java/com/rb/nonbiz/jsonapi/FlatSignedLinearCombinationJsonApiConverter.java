package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.FlatSignedLinearCombination;

import java.util.function.Function;

import static com.rb.nonbiz.collections.FlatSignedLinearCombination.flatSignedLinearCombination;
import static com.rb.nonbiz.json.RBJsonArrays.iteratorToJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArrayToList;

/**
 * Converts a {@link FlatSignedLinearCombination} back and forth to a JSON array for our public API.
 *
 * <p> This does not implement JsonRoundTripConverter because we need to supply serializers
 * and deserializers. </p>
 */
public class FlatSignedLinearCombinationJsonApiConverter {

  @Inject WeightedBySignedFractionJsonApiConverter weightedBySignedFractionJsonApiConverter;

  public <T> JsonArray toJsonArray(
      FlatSignedLinearCombination<T> flatSignedLinearCombination,
      Function<T, JsonElement> serializer) {
    return iteratorToJsonArray(
        flatSignedLinearCombination.size(),
        flatSignedLinearCombination.iterator(),
        weightedBySignedFraction -> weightedBySignedFractionJsonApiConverter.toJsonObject(
            weightedBySignedFraction,
            serializer));
  }

  public <T> FlatSignedLinearCombination<T> fromJsonArray(
      JsonArray jsonArray,
      Function<JsonElement, T> deserializer) {
    return flatSignedLinearCombination(
        jsonArrayToList(
            jsonArray,
            jsonElement -> weightedBySignedFractionJsonApiConverter.fromJsonObject(
                jsonElement.getAsJsonObject(),
                deserializer)));
  }

}
