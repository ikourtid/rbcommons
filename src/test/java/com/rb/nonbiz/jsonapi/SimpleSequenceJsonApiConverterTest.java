package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.math.sequence.ConstantSequence;
import com.rb.nonbiz.math.sequence.SimpleSequence;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTestHelper.hasJsonApiDocumentationTestHelper;
import static com.rb.nonbiz.jsonapi.JsonApiPair.jsonApiPair;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.math.sequence.ArithmeticProgression.ArithmeticProgressionBuilder.arithmeticProgressionBuilder;
import static com.rb.nonbiz.math.sequence.ConstantSequence.constantSequence;
import static com.rb.nonbiz.math.sequence.ConstantSequenceTest.constantSequenceMatcher;
import static com.rb.nonbiz.math.sequence.SimpleSequenceTest.doubleSequenceMatcher;
import static com.rb.nonbiz.math.sequence.GeometricProgression.GeometricProgressionBuilder.geometricProgressionBuilder;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class SimpleSequenceJsonApiConverterTest
    extends RBCommonsIntegrationTest<SimpleSequenceJsonApiConverter> {

  @Test
  public void handlesAllSubclasses() {
    // Unfortunately, we must separately test the DoubleSequence subclasses...
    jsonApiTestData(
        f -> doubleSequenceMatcher(f),

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
            geometricProgressionBuilder()
                .setInitialValue(111.22)
                .setCommonRatio(1.08)
                .build(),
            jsonObject(
                "type",         jsonString("geometricProgression"),
                "initialValue", jsonDouble(111.22),
                "commonRatio",  jsonDouble(1.08))))
        .testRoundTripConversions(
            v -> makeRealObject().toJsonObject(v),
            v -> (SimpleSequence) makeRealObject().fromJsonObject(v));

    // ... vs. the ConstantSequence class.
    // These classes do not form a single inheritance hierarchy because ConstantSequence is generic; it does not just
    // apply to doubles.
    jsonApiTestData(
        f -> constantSequenceMatcher(f, f2 -> doubleAlmostEqualsMatcher(f2, DEFAULT_EPSILON_1e_8)),

        jsonApiPair(
            constantSequence(12.34),
            jsonObject(
                "type", jsonString("constantSequence"),
                "constantValue", jsonDouble(12.34))))
        .testRoundTripConversions(
            v -> makeRealObject().toJsonObject(v),
            v -> (ConstantSequence<Double>) makeRealObject().fromJsonObject(v));
  }

  @Test
  public void hasJsonApiDocumentation_runAllTests() {
    SimpleSequenceJsonApiConverter simpleSequenceJsonApiConverter = makeRealObject();
    hasJsonApiDocumentationTestHelper().runAllTests(
        simpleSequenceJsonApiConverter,
        v -> simpleSequenceJsonApiConverter.fromJsonObject(v));
  }

  @Override
  protected Class<SimpleSequenceJsonApiConverter> getClassBeingTested() {
    return SimpleSequenceJsonApiConverter.class;
  }

}
