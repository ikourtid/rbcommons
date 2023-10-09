package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonArray;
import com.rb.nonbiz.collections.Coordinates;

import java.util.Arrays;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBJsonArrays.iteratorToJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArrayToIntArray;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

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
        .setClass(Coordinates.class)
        .setSingleLineSummary(documentation(
            "Holds the coordinates of a point in an N-dimensional grid."))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "Each coordinate must be a non-negative integer.")))
        .hasNoJsonValidationInstructions()
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(
            jsonArray(jsonInteger(4), jsonInteger(0), jsonInteger(1), jsonInteger(0)))
        .build();
  }

}
