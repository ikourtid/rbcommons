package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.MultiDimensionalArray;
import com.rb.nonbiz.collections.MultiDimensionalArrayTest;
import com.rb.nonbiz.collections.MutableMultiDimensionalArray;
import com.rb.nonbiz.json.RBJsonArrays;
import com.rb.nonbiz.testmatchers.RBValueMatchers;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.collections.MultiDimensionalArray.newMultiDimensionalArray;
import static com.rb.nonbiz.collections.MultiDimensionalArrayTest.multiDimensionalArrayMatcher;
import static com.rb.nonbiz.json.RBJsonArrays.jsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonIntegerArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.jsonapi.JsonApiTestPair.jsonApiTestPair;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class MultiDimensionalArrayJsonApiConverterTest
    extends RBCommonsIntegrationTest<MultiDimensionalArrayJsonApiConverter> {

  @Test
  public void testBackAndForth() {
    JsonApiTestData<MultiDimensionalArray<Double>> jsonApiTestData = jsonApiTestData(
        f -> multiDimensionalArrayMatcher(f, f2 -> doubleAlmostEqualsMatcher(f2, DEFAULT_EPSILON_1e_8)),

        jsonApiTestPair(
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
    // FIXME IAK
  }

  @Override
  protected Class<MultiDimensionalArrayJsonApiConverter> getClassBeingTested() {
    return MultiDimensionalArrayJsonApiConverter.class;
  }

}
