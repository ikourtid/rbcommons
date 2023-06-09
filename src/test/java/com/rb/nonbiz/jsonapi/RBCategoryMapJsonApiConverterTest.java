package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.collections.RBCategoryMap;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBCategoryMap.rbCategoryMap;
import static com.rb.nonbiz.collections.RBCategoryMapTest.rbCategoryMapMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.jsonapi.JsonApiTestPair.jsonApiTestPair;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class RBCategoryMapJsonApiConverterTest extends RBCommonsIntegrationTest<RBCategoryMapJsonApiConverter> {

  @Test
  public void testRoundTripConversions() {
    JsonApiTestData<RBCategoryMap<String, Double>> jsonApiTestData =
        jsonApiTestData(
            f -> rbCategoryMapMatcher(f, f2 -> doubleAlmostEqualsMatcher(f2, DEFAULT_EPSILON_1e_8)),

            // an empty category map
            jsonApiTestPair(
                rbCategoryMap(
                    123.45,
                    emptyRBMap()),
                jsonObject(
                    "valueRegardlessOfCategory", jsonDouble(123.45),
                    "categoryMap", emptyJsonObject())),

            // non-empty category map
            jsonApiTestPair(
                rbCategoryMap(
                    1_100.0,
                    rbMapOf(
                        "categoryA", 100.0,
                        "categoryB", 300.0,
                        "categoryC", 700.0)),
                jsonObject(
                    "valueRegardlessOfCategory", jsonDouble(1_100.0),
                    "categoryMap", jsonObject(
                        "categoryA", jsonDouble(100.0),
                        "categoryB", jsonDouble(300.0),
                        "categoryC", jsonDouble(700.0)))));

    jsonApiTestData.testRoundTripConversions(
        rbCategoryMap -> makeRealObject().toJsonObject(
            rbCategoryMap,
            key -> key,
            d -> jsonDouble(d)),
        jsonObject -> makeRealObject().fromJsonObject(
            jsonObject,
            key -> key,
            jsonElement -> jsonElement.getAsDouble()));
  }

  @Override
  protected Class<RBCategoryMapJsonApiConverter> getClassBeingTested() {
    return RBCategoryMapJsonApiConverter.class;
  }

}