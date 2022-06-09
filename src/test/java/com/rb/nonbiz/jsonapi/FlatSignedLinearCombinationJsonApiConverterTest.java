package com.rb.nonbiz.jsonapi;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.collections.FlatSignedLinearCombination;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.SignedFraction;
import com.rb.nonbiz.types.WeightedBySignedFraction;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.FlatSignedLinearCombination.flatSignedLinearCombination;
import static com.rb.nonbiz.collections.FlatSignedLinearCombinationTest.flatSignedLinearCombinationMatcher;
import static com.rb.nonbiz.json.RBGson.jsonBigDecimal;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.singletonJsonArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
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

    assertThat(
        makeTestObject().toJsonArray(
            singleItem,
            string -> jsonString(string)),
        jsonArrayMatcher(
            singletonJsonArray(jsonObject(
                "weight", jsonDouble(0.123),
                "item",   jsonString("abc"))),
            1e-8));

    assertThat(
        makeTestObject().fromJsonArray(
            singletonJsonArray(jsonObject(
                "weight", jsonDouble(0.123),
                "item",   jsonString("abc"))),
            jsonElement -> jsonElement.getAsString()),
        flatSignedLinearCombinationMatcher(
            singleItem, f -> typeSafeEqualTo(f)));
  }

  @Override
  protected FlatSignedLinearCombinationJsonApiConverter makeTestObject() {
    return makeRealObject(FlatSignedLinearCombinationJsonApiConverter.class);
  }

}