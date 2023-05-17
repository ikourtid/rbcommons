package com.rb.nonbiz.jsonapi;

import com.google.common.primitives.Ints;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.MultiDimensionalArray;
import com.rb.nonbiz.collections.MutableMultiDimensionalArray;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;

import java.util.function.Function;

import static com.rb.nonbiz.collections.MultiDimensionalArray.newMultiDimensionalArray;
import static com.rb.nonbiz.collections.MutableMultiDimensionalArray.mutableMultiDimensionalArray;
import static com.rb.nonbiz.collections.RBIterators.forEachPair;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMultidimensionalCounters.flatMultidimensionalIterator;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.CollectionJsonApiPropertyDescriptor.collectionJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArrayToIntArray;
import static com.rb.nonbiz.json.RBJsonArrays.listToJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.streamToJsonArray;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonArrayOrThrow;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts a {@link MultiDimensionalArray} to / from JSON for our JSON APIs.
 */
public class MultiDimensionalArrayJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "dimensions", collectionJsonApiPropertyDescriptor(
              simpleClassJsonApiPropertyDescriptor(Integer.class),
              jsonPropertySpecificDocumentation(asSingleLineWithNewlines(
                  "The size of the N-dimensional space in each dimension. For example, [40, 10, 30, 20] means that the",
                  "last dimension (a bit like the least important digit) in this 4-dimensional space has a size of 20."))),
          "items", collectionJsonApiPropertyDescriptor(
              UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
              jsonPropertySpecificDocumentation(asSingleLineWithNewlines(
                  "The data stored as a 1-dimensional JSON array, where the numeric array index behaves like",
                  "the Apache Java MultidimensionalCounter.")))))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public <T> JsonObject toJsonObject(
      MultiDimensionalArray<T> multiDimensionalArray,
      Function<T, JsonElement> arrayItemSerializer) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setJsonArray("dimensions", listToJsonArray(
                Ints.asList(multiDimensionalArray.getMultidimensionalCounter().getSizes()),
                dimensionSize -> jsonInteger(dimensionSize)))
            .setJsonArray("items", streamToJsonArray(
                multiDimensionalArray.getMultidimensionalCounter().getSize(),
                multiDimensionalArray.stream(),
                v -> arrayItemSerializer.apply(v)))
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <T> MultiDimensionalArray<T> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonElement, T> arrayItemDeserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    // Unfortunately there's no nice fluent way to do this, so we should just iterate over the array.
    MutableMultiDimensionalArray<T> mutableArray = mutableMultiDimensionalArray(
        jsonArrayToIntArray(
            getJsonArrayOrThrow(jsonObject, "dimensions"),
            v -> v.getAsInt()));
    JsonArray itemsJsonArray = getJsonArrayOrThrow(jsonObject, "items");

    forEachPair(
        itemsJsonArray.iterator(),
        flatMultidimensionalIterator(mutableArray.getMultidimensionalCounter()),
        (jsonElement, coordinates) -> mutableArray.setAssumingAbsent(
            arrayItemDeserializer.apply(jsonElement), coordinates));

    return newMultiDimensionalArray(mutableArray);
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return null; // FIXME SWA BACKTEST JSON API
  }

}
