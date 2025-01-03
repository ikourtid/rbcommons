package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.ItemsOutsideClosedRanges;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.ItemsOutsideClosedRanges.emptyItemsOutsideClosedRanges;
import static com.rb.nonbiz.collections.ItemsOutsideClosedRanges.itemsOutsideClosedRanges;
import static com.rb.nonbiz.collections.ItemsOutsideClosedRangesTest.itemsOutsideClosedRangesMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.collections.ValueOutsideClosedRange.valueOutsideClosedRange;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonPercentage;
import static com.rb.nonbiz.json.RBGson.unitFractionFromJsonPercentage;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class ItemsOutsideClosedRangesJsonApiConverterTest
    extends RBCommonsIntegrationTest<ItemsOutsideClosedRangesJsonApiConverter> {

  @Test
  public void generalCase_hasTwoItems() {
    testRoundTrip(
        itemsOutsideClosedRanges(
            rbMapOf(
                "_A", valueOutsideClosedRange(unitFraction(0.17), closedRange(unitFraction(0.27), unitFraction(0.37))),
                "_B", valueOutsideClosedRange(unitFraction(0.18), closedRange(unitFraction(0.28), unitFraction(0.38))))),
        jsonObject(
            "A", jsonObject(
                "value", jsonDouble(17),
                "range", jsonObject(
                    "min", jsonDouble(27),
                    "max", jsonDouble(37))),
            "B", jsonObject(
                "value", jsonDouble(18),
                "range", jsonObject(
                    "min", jsonDouble(28),
                    "max", jsonDouble(38)))));
  }

  @Test
  public void specialCase_empty() {
    testRoundTrip(
        emptyItemsOutsideClosedRanges(),
        emptyJsonObject());
  }

  private void testRoundTrip(
      ItemsOutsideClosedRanges<String, UnitFraction> itemsOutsideClosedRanges,
      JsonObject jsonObject) {
    assertThat(
        makeRealObject().toJsonObject(
            itemsOutsideClosedRanges,
            v -> v.substring(1), // drop the 1st underscore
            v -> jsonPercentage(v)),
        jsonObjectEpsilonMatcher(
            jsonObject));

    assertThat(
        makeRealObject().fromJsonObject(
            jsonObject,
            v -> "_" + v, // add an underscore to the key, to make (de)serialization general in this test.
            v -> unitFractionFromJsonPercentage(v)),
        itemsOutsideClosedRangesMatcher(
            itemsOutsideClosedRanges, f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

  @Test
  public void testValidSampleJson() {
    ItemsOutsideClosedRangesJsonApiConverter realObject = makeRealObject();

    JsonElement sampleJson = getOrThrow(
        // have to cast because not all JsonApiDocumentation classes have NontrivialSampleJson
        ((JsonApiClassDocumentation) realObject.getJsonApiDocumentation()).getNontrivialSampleJson(),
        "Internal error - should have a sample JSON");

    // Check that the sample JSON can be successfully processed by fromJsonObject().
    ItemsOutsideClosedRanges<String, UnitFraction> doesNotThrow = realObject.fromJsonObject(
        sampleJson.getAsJsonObject(),
        k -> k,
        v -> unitFractionFromJsonPercentage(v));
  }

  @Override
  protected Class<ItemsOutsideClosedRangesJsonApiConverter> getClassBeingTested() {
    return ItemsOutsideClosedRangesJsonApiConverter.class;
  }

}
