package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.FlatSignedLinearCombination;

import java.util.function.Function;

import static com.rb.nonbiz.collections.FlatSignedLinearCombination.flatSignedLinearCombination;
import static com.rb.nonbiz.json.RBJsonArrays.iteratorToJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArrayToList;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentation.JsonApiDocumentationBuilder.jsonApiDocumentationBuilder;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.Strings.asSingleLine;

/**
 * Converts a {@link FlatSignedLinearCombination} back and forth to a JSON array for our public API.
 *
 * <p> This does not create a JSON Object as most converters do, but instead creates a JSON Array.
 * It does so because a FlatSignedLinearCombinartion is just a thin wrapper for a
 * {@code List<WeightedBySignedFraction<T>>}, and lists are more naturally represented by arrays.
 * Furthermore, if we were to create a JsonObject, it's not clear what the keys would be. </p>
 *
 * <p> This does not implement JsonRoundTripConverter because we need to supply serializers
 * and deserializers. </p>
 */
public class FlatSignedLinearCombinationJsonApiConverter implements HasJsonApiDocumentation {

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
                // jsonArrayToList() is general and can work with an array of general JSON elements. However, in this
                // case we need the array to consist of JSON objects with keys "weight" and "item".
                // Therefor we cast the JsonElement to a JsonObject. An error will be thrown if it's not a JsonObject.
                jsonElement.getAsJsonObject(),
                deserializer)));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiDocumentationBuilder()
        .setClass(FlatSignedLinearCombinationJsonApiConverter.class)
        .setSingleLineSummary(label(asSingleLine(
            "A collection of weighted items, similar to FlatLinearCombination ",
            "except that it allows both positive and negative weights (but not zero).")))
        .hasChildNode(weightedBySignedFractionJsonApiConverter)
        .setDocumentationHtml("FIXME IAK / FIXME SWA JSONDOC")
        .noTrivialSampleJsonSupplied()
        .noNontrivialSampleJsonSupplied()
        .build();
  }

}
