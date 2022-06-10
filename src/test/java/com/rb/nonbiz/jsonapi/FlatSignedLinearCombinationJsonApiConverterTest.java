package com.rb.nonbiz.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.rb.nonbiz.collections.FlatSignedLinearCombination;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.FlatSignedLinearCombination.flatSignedLinearCombination;
import static com.rb.nonbiz.collections.FlatSignedLinearCombinationTest.flatSignedLinearCombinationMatcher;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.singletonJsonArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.WeightedBySignedFraction.weightedBySignedFraction;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class FlatSignedLinearCombinationJsonApiConverterTest
    extends RBTest<FlatSignedLinearCombinationJsonApiConverter> {

  @Test
  public void testSingleItemCombination() {
    FlatSignedLinearCombination<String> singleItem =
        flatSignedLinearCombination(singletonList(
            weightedBySignedFraction("abc", signedFraction(0.123))));

    assertEquals(1, singleItem.size());

    testRoundTripConversionHelper(
        singleItem,
        singletonJsonArray(jsonObject(
            "weight", jsonDouble(0.123),
            "item",   jsonString("abc"))));
  }

  @Test
  public void testMultipleItemCombination() {
    FlatSignedLinearCombination<String> multipleItems =
        flatSignedLinearCombination(ImmutableList.of(
            weightedBySignedFraction("aaa", signedFraction(-0.111)),    // weights can be negative
            weightedBySignedFraction("bbb", signedFraction( 0.222)),
            weightedBySignedFraction("ccc", signedFraction( 333.0))));  // weights can be > 1

    assertEquals(3, multipleItems.size());

    testRoundTripConversionHelper(
        multipleItems,
        jsonArray(
            jsonObject(
                "weight", jsonDouble(-0.111),
                "item",   jsonString("aaa")),
            jsonObject(
                "weight", jsonDouble( 0.222),
                "item",   jsonString("bbb")),
            jsonObject(
                "weight", jsonDouble( 333.0),
                "item",   jsonString("ccc"))));
  }

  private void testRoundTripConversionHelper(
      FlatSignedLinearCombination<String> flatSignedLinearCombination,
      JsonArray jsonArray) {

    assertThat(
        makeTestObject().toJsonArray(
            flatSignedLinearCombination,
            string -> jsonString(string)),
        jsonArrayMatcher(jsonArray, 1e-8));

    assertThat(
        makeTestObject().fromJsonArray(
            jsonArray,
            jsonElement -> jsonElement.getAsString()),
        flatSignedLinearCombinationMatcher(
            flatSignedLinearCombination, f -> typeSafeEqualTo(f)));
  }

  @Override
  protected FlatSignedLinearCombinationJsonApiConverter makeTestObject() {
    return makeRealObject(FlatSignedLinearCombinationJsonApiConverter.class);
  }

}
