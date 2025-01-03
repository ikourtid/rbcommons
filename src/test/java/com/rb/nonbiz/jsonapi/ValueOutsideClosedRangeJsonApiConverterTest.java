package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.ValueOutsideClosedRange;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.collections.ValueOutsideClosedRange.valueOutsideClosedRange;
import static com.rb.nonbiz.collections.ValueOutsideClosedRangeTest.valueOutsideClosedRangeMatcher;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonPercentage;
import static com.rb.nonbiz.json.RBGson.unitFractionFromJsonPercentage;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class ValueOutsideClosedRangeJsonApiConverterTest
    extends RBCommonsIntegrationTest<ValueOutsideClosedRangeJsonApiConverter> {

  @Test
  public void generalCase() {
    ValueOutsideClosedRange<UnitFraction> valueOutsideClosedRange =
        valueOutsideClosedRange(unitFraction(0.11), closedRange(unitFraction(0.21), unitFraction(0.31)));
    JsonObject jsonObject = jsonObject(
        "value", jsonDouble(11),
        "range", jsonObject(
            "min", jsonDouble(21),
            "max", jsonDouble(31)));

    assertThat(
        makeRealObject().toJsonObject(valueOutsideClosedRange, v -> jsonPercentage(v)),
        jsonObjectEpsilonMatcher(jsonObject));

    assertThat(
        makeRealObject().fromJsonObject(jsonObject, v -> unitFractionFromJsonPercentage(v)),
        valueOutsideClosedRangeMatcher(valueOutsideClosedRange, f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

  @Test
  public void testValidSampleJson() {
    ValueOutsideClosedRangeJsonApiConverter realObject = makeRealObject();

    JsonElement sampleJson = getOrThrow(
        // have to cast because not all JsonApiDocumentation classes have NontrivialSampleJson
        ((JsonApiClassDocumentation) realObject.getJsonApiDocumentation()).getNontrivialSampleJson(),
        "Internal error - should have a sample JSON");

    // Check that the sample JSON can be successfully processed by fromJsonObject().
    ValueOutsideClosedRange<UnitFraction> doesNotThrow = realObject.fromJsonObject(
        sampleJson.getAsJsonObject(),
        v -> unitFractionFromJsonPercentage(v));
  }

  @Override
  protected Class<ValueOutsideClosedRangeJsonApiConverter> getClassBeingTested() {
    return ValueOutsideClosedRangeJsonApiConverter.class;
  }

}
