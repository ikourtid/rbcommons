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
 * <p> This does not create a JSON Object as most converters do, but instead creates a JSON Array.
 * It does so because a FlatSignedLinearCombinartion is just a thin wrapper for a
 * {@code List<WeightedBySignedFraction<T>>}. Lists are more naturally represented by arrays.
 * Furthermore, if we were to use a JsonObject, it's not clear what the keys would be. </p>
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
