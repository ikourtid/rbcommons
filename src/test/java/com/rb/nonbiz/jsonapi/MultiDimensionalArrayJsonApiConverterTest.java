package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.collections.MultiDimensionalArray;
import com.rb.nonbiz.collections.MutableMultiDimensionalArray;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.collections.MultiDimensionalArray.newMultiDimensionalArray;
import static com.rb.nonbiz.collections.MultiDimensionalArrayTest.multiDimensionalArrayMatcher;
import static com.rb.nonbiz.collections.MultiDimensionalArrayTest.singleDimensionMultiDimensionalArray;
import static com.rb.nonbiz.collections.MultiDimensionalArrayTest.singleItemMultiDimensionalArray;
import static com.rb.nonbiz.collections.MultiDimensionalArrayTest.twoDimensionalMultiDimensionalArray;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonArrays.jsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonIntegerArray;
import static com.rb.nonbiz.json.RBJsonArrays.singletonJsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonArrays.singletonJsonIntegerArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.jsonapi.JsonApiPair.jsonApiPair;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

public class MultiDimensionalArrayJsonApiConverterTest
    extends RBCommonsIntegrationTest<MultiDimensionalArrayJsonApiConverter> {

  @Test
  public void testRoundTripConversions() {
    JsonApiTestData<MultiDimensionalArray<Double>> jsonApiTestData = jsonApiTestData(
        f -> multiDimensionalArrayMatcher(f, f2 -> doubleAlmostEqualsMatcher(f2, DEFAULT_EPSILON_1e_8)),

        jsonApiPair(
            singleItemMultiDimensionalArray(12.34),
            jsonObject(
                "dimensions", singletonJsonIntegerArray(1),
                "items", singletonJsonDoubleArray(12.34))),

        jsonApiPair(
            singleDimensionMultiDimensionalArray(70.0, 70.1, 70.2),
            jsonObject(
                "dimensions", singletonJsonIntegerArray(3),
                "items", jsonDoubleArray(
                    70.0, 70.1, 70.2))),

        jsonApiPair(
            twoDimensionalMultiDimensionalArray(new Double[][] {
                { 70.0, 70.1, 70.2 },
                { 71.0, 71.1, 71.2 }
            }),
            jsonObject(
                "dimensions", jsonIntegerArray(2, 3),
                "items", jsonDoubleArray(
                    70.0, 70.1, 70.2,
                    71.0, 71.1, 71.2))),

        jsonApiPair(
            newMultiDimensionalArray(
                MutableMultiDimensionalArray.<Double>mutableMultiDimensionalArray(2, 1, 3)
                    .setAssumingAbsent(1_000.0, coordinates(0, 0, 0))
                    .setAssumingAbsent(1_000.1, coordinates(0, 0, 1))
                    .setAssumingAbsent(1_000.2, coordinates(0, 0, 2))
            
                    .setAssumingAbsent(1_001.0, coordinates(1, 0, 0))
                    .setAssumingAbsent(1_001.1, coordinates(1, 0, 1))
                    .setAssumingAbsent(1_001.2, coordinates(1, 0, 2))),
            jsonObject(
                "dimensions", jsonIntegerArray(2, 1, 3),
                "items", jsonDoubleArray(
                    1_000.0,
                    1_000.1,
                    1_000.2,
        
                    1_001.0,
                    1_001.1,
                    1_001.2))));

    jsonApiTestData.testRoundTripConversions(
        multiDimensionalArray -> makeRealObject().toJsonObject(multiDimensionalArray, v -> jsonDouble(v)),
        jsonObject            -> makeRealObject().fromJsonObject(jsonObject, v -> v.getAsDouble()));
  }

  @Override
  protected Class<MultiDimensionalArrayJsonApiConverter> getClassBeingTested() {
    return MultiDimensionalArrayJsonApiConverter.class;
  }

}
