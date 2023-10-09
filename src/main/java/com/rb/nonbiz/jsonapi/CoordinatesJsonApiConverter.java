package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonArray;
import com.rb.nonbiz.collections.Coordinates;

import java.util.Arrays;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBJsonArrays.iteratorToJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArrayToIntArray;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;

/**
 * Converts {@link Coordinates} back and forth to JSON for our public API.
 */
public class CoordinatesJsonApiConverter implements HasJsonApiDocumentation {

  public JsonArray toJsonArray(Coordinates coordinates) {
    return iteratorToJsonArray(
        coordinates.getNumDimensions(),
        Arrays.stream(coordinates.getRawCoordinatesArray()).iterator(),
        i -> jsonInteger(i));
  }

  public Coordinates fromJsonArray(JsonArray jsonArray) {
    return coordinates(
        jsonArrayToIntArray(
            jsonArray,
            jsonElement -> jsonElement.getAsInt()));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .build();
  }

}
