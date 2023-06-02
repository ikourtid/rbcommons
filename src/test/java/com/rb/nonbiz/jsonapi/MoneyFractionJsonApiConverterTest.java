package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.types.MoneyFraction;
import org.junit.Test;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.types.MoneyFraction.moneyFraction;
import static com.rb.nonbiz.types.MoneyFractionTest.moneyFractionMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class MoneyFractionJsonApiConverterTest extends RBCommonsIntegrationTest<MoneyFractionJsonApiConverter> {

  @Test
  public void testZeroFraction() {
    testRoundTripConverterHelper(
        moneyFraction(
            ZERO_MONEY,
            money(789)),
        jsonObject(
            "numerator",   jsonDouble(0),
            "denominator", jsonDouble(789)));
  }

  // the "fraction" $0 / $0 is allowed
  @Test
  public void testZeroOverZero_allowed() {
    testRoundTripConverterHelper(
        moneyFraction(
            ZERO_MONEY,
            ZERO_MONEY),
        jsonObject(
            "numerator",   jsonDouble(0),
            "denominator", jsonDouble(0)));
  }

  // a MoneyFraction does not need to be less than 1.0
  @Test
  public void testFractionGreaterThanOne_allowed() {
    testRoundTripConverterHelper(
        moneyFraction(
            money(789),
            money(123)),
        jsonObject(
            "numerator",   jsonDouble(789),
            "denominator", jsonDouble(123)));
  }

  @Test
  public void testGeneralCase() {
    testRoundTripConverterHelper(
        moneyFraction(
            money(123.45),
            money(456.78)),
        jsonObject(
            "numerator",   jsonDouble(123.45),
            "denominator", jsonDouble(456.78)));
  }

  private void testRoundTripConverterHelper(
      MoneyFraction moneyFraction,
      JsonObject jsonObject) {
    assertThat(
        makeRealObject().toJsonObject(moneyFraction),
        jsonObjectEpsilonMatcher(jsonObject));
    assertThat(
        makeRealObject().fromJsonObject(jsonObject),
        moneyFractionMatcher(moneyFraction));
  }

  @Override
  protected Class<MoneyFractionJsonApiConverter> getClassBeingTested() {
    return MoneyFractionJsonApiConverter.class;
  }

}