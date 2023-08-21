package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.math.sequence.ArithmeticProgression;
import com.rb.nonbiz.math.sequence.ArithmeticProgression.ArithmeticProgressionBuilder;
import com.rb.nonbiz.math.sequence.ArithmeticProgressionTest;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTestHelper.hasJsonApiDocumentationTestHelper;
import static com.rb.nonbiz.jsonapi.JsonApiPair.jsonApiPair;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.math.sequence.ArithmeticProgression.ArithmeticProgressionBuilder.arithmeticProgressionBuilder;
import static com.rb.nonbiz.math.sequence.ArithmeticProgressionTest.arithmeticProgressionMatcher;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class ArithmeticProgressionJsonApiConverterTest
    extends RBCommonsIntegrationTest<ArithmeticProgressionJsonApiConverter> {

  @Test
  public void hasJsonApiDocumentation_runAllTests() {
    ArithmeticProgressionJsonApiConverter arithmeticProgressionJsonApiConverter = makeRealObject();
    hasJsonApiDocumentationTestHelper().runAllTests(
        arithmeticProgressionJsonApiConverter,
        jsonObject -> arithmeticProgressionJsonApiConverter.fromJsonObject(jsonObject));
  }

  @Test
  public void generalCase() {
    JsonApiTestData<ArithmeticProgression> jsonApiTestData = jsonApiTestData(
        f -> arithmeticProgressionMatcher(f),

        jsonApiPair(
            arithmeticProgressionBuilder()
                .setInitialValue(111.22)
                .setCommonDifference(0.33)
                .build(),
            jsonObject(
                "type",             jsonString("arithmeticProgression"),
                "initialValue",     jsonDouble(111.22),
                "commonDifference", jsonDouble(0.33))),

        jsonApiPair(
            arithmeticProgressionBuilder()
                .setInitialValue(111.22)
                .setCommonDifference(0)
                .build(),
            jsonObject(
                "type",             jsonString("arithmeticProgression"),
                "initialValue",     jsonDouble(111.22),
                "commonDifference", jsonDouble(0))),

        jsonApiPair(
            arithmeticProgressionBuilder()
                .setInitialValue(-111.22)
                .setCommonDifference(-0.33)
                .build(),
            jsonObject(
                "type",             jsonString("arithmeticProgression"),
                "initialValue",     jsonDouble(-111.22),
                "commonDifference", jsonDouble(-0.33))));

    jsonApiTestData.testRoundTripConversions(
        v -> makeRealObject().toJsonObject(v),
        v -> makeRealObject().fromJsonObject(v));
  }

  @Override
  protected Class<ArithmeticProgressionJsonApiConverter> getClassBeingTested() {
    return ArithmeticProgressionJsonApiConverter.class;
  }

}
