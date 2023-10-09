package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.rb.nonbiz.collections.Coordinates;
import com.rb.nonbiz.collections.RBCategoryMap;
import com.rb.nonbiz.collections.RBOptionals;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.collections.CoordinatesTest.coordinatesMatcher;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.jsonapi.JsonArrayApiPair.jsonArrayApiPair;
import static com.rb.nonbiz.jsonapi.JsonArrayApiTestData.jsonArrayApiTestData;

public class CoordinatesJsonApiConverterTest extends RBCommonsIntegrationTest<CoordinatesJsonApiConverter> {

  @Test
  public void testRoundTripConversions() {
    JsonArrayApiTestData<Coordinates> jsonArrayApiTestData =
        jsonArrayApiTestData(
            f -> coordinatesMatcher(f),

            jsonArrayApiPair(
                coordinates(1, 0, 3, 0, 4),
                jsonArray(
                    jsonInteger(1),
                    jsonInteger(0),
                    jsonInteger(3),
                    jsonInteger(0),
                    jsonInteger(4))));

    jsonArrayApiTestData.testRoundTripConversions(
        coordinates -> makeRealObject().toJsonArray(coordinates),
        jsonArray   -> makeRealObject().fromJsonArray(jsonArray));
  }

  @Test
  public void testValidSampleJson() {
    CoordinatesJsonApiConverter realObject = makeRealObject(CoordinatesJsonApiConverter.class);

    JsonElement sampleJson = RBOptionals.getOrThrow(
        // have to cast because not all JsonApiDocumentation classes have NontrivialSampleJson
        ((JsonApiClassDocumentation) realObject.getJsonApiDocumentation()).getNontrivialSampleJson(),
        "Internal error - should have a sample JSON");

    // Check that the sample JSON can be successfully processed by fromJsonObject().
    Coordinates doesNotThrow = realObject.fromJsonArray(sampleJson.getAsJsonArray());
  }

  @Override
  protected Class<CoordinatesJsonApiConverter> getClassBeingTested() {
    return CoordinatesJsonApiConverter.class;
  }

}
