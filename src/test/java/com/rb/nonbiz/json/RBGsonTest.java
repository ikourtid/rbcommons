package com.rb.nonbiz.json;

import com.google.gson.JsonPrimitive;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.OnesBasedReturn.FLAT_RETURN;
import static com.rb.biz.types.OnesBasedReturn.onesBasedGain;
import static com.rb.biz.types.OnesBasedReturn.onesBasedLoss;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.nonbiz.json.RBGson.*;
import static com.rb.nonbiz.json.RBJsonArrays.emptyJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.singletonJsonArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.math.stats.ZScore.zScore;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class RBGsonTest {

  @Test
  public void testJsonBoolean() {
    assertEquals(new JsonPrimitive(false), jsonBoolean(false));
    assertEquals(new JsonPrimitive( true), jsonBoolean( true));
  }

  @Test
  public void testJsonString() {
    assertEquals(new JsonPrimitive(""   ), jsonString(""   ));
    assertEquals(new JsonPrimitive("abc"), jsonString("abc"));
  }

  @Test
  public void testJsonInteger() {
    assertEquals(new JsonPrimitive(-123), jsonInteger(-123));
    assertEquals(new JsonPrimitive(   0), jsonInteger(   0));
    assertEquals(new JsonPrimitive( 123), jsonInteger( 123));
  }

  @Test
  public void testJsonYear_mustBeReasonablyCloseToCurrentYear() {
    assertIllegalArgumentException( () -> jsonYear(1_636));
    JsonPrimitive doesNotThrow;
    doesNotThrow = jsonYear(1_900);
    doesNotThrow = jsonYear(2_000);
    doesNotThrow = jsonYear(2_200);
    assertIllegalArgumentException( () -> jsonYear(3_333));
  }

  @Test
  public void testJsonLong() {
    assertEquals(new JsonPrimitive(-123L), jsonLong(-123L));
    assertEquals(new JsonPrimitive(   0L), jsonLong(   0L));
    assertEquals(new JsonPrimitive( 123L), jsonLong( 123L));
  }

  @Test
  public void testJsonDouble() {
    assertEquals(new JsonPrimitive(-1.23), jsonDouble(-1.23));
    assertEquals(new JsonPrimitive( 0.00), jsonDouble( 0.00));
    assertEquals(new JsonPrimitive( 1.23), jsonDouble( 1.23));

    // jsonDouble() rounds to 8 digits
    assertEquals(new JsonPrimitive(-0.123_456_79), jsonDouble(-0.123_456_789));
    assertEquals(new JsonPrimitive(-0.123_456_78), jsonDouble(-0.123_456_781));
    assertEquals(new JsonPrimitive( 0.123_456_78), jsonDouble( 0.123_456_781));
    assertEquals(new JsonPrimitive( 0.123_456_79), jsonDouble( 0.123_456_789));

    assertEquals(new JsonPrimitive(-0.345_679), jsonDoubleRoundedTo6Digits(-0.345_678_9));
    assertEquals(new JsonPrimitive(-0.345_678), jsonDoubleRoundedTo6Digits(-0.345_678_1));
    assertEquals(new JsonPrimitive( 0.345_678), jsonDoubleRoundedTo6Digits( 0.345_678_1));
    assertEquals(new JsonPrimitive( 0.345_679), jsonDoubleRoundedTo6Digits( 0.345_678_9));
  }

  @Test
  public void testJsonDoubleUnrounded() {
    assertEquals(new JsonPrimitive(-1.23), jsonDoubleUnrounded(-1.23));
    assertEquals(new JsonPrimitive( 0.00), jsonDoubleUnrounded( 0.00));
    assertEquals(new JsonPrimitive( 1.23), jsonDoubleUnrounded( 1.23));

    // jsonDoubleUnrounded() does not round to 8 digits
    assertEquals(new JsonPrimitive(-0.123_456_789), jsonDoubleUnrounded(-0.123_456_789));
    assertEquals(new JsonPrimitive(-0.123_456_781), jsonDoubleUnrounded(-0.123_456_781));
    assertEquals(new JsonPrimitive( 0.123_456_781), jsonDoubleUnrounded( 0.123_456_781));
    assertEquals(new JsonPrimitive( 0.123_456_789), jsonDoubleUnrounded( 0.123_456_789));
  }

  @Test
  public void testJsonDoubleFromImpreciseValue() {
    assertEquals(new JsonPrimitive(-1.23), jsonDouble(zScore(-1.23)));
    assertEquals(new JsonPrimitive( 0.00), jsonDouble(zScore( 0.00)));
    assertEquals(new JsonPrimitive( 1.23), jsonDouble(zScore( 1.23)));

    // jsonDouble() rounds to 8 digits
    assertEquals(new JsonPrimitive(-0.123_456_79), jsonDouble(zScore(-0.123_456_789)));
    assertEquals(new JsonPrimitive(-0.123_456_78), jsonDouble(zScore(-0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_78), jsonDouble(zScore( 0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_79), jsonDouble(zScore( 0.123_456_789)));
  }

  @Test
  public void testJsonBigDecimal() {
    assertEquals(new JsonPrimitive(-1.23), jsonBigDecimal(BigDecimal.valueOf(-1.23)));
    assertEquals(new JsonPrimitive( 0.00), jsonBigDecimal(BigDecimal.ZERO));
    assertEquals(new JsonPrimitive( 1.23), jsonBigDecimal(BigDecimal.valueOf( 1.23)));

    // jsonBigDecimal() rounds to 8 digits
    assertEquals(new JsonPrimitive(-0.123_456_79), jsonBigDecimal(BigDecimal.valueOf(-0.123_456_789)));
    assertEquals(new JsonPrimitive(-0.123_456_78), jsonBigDecimal(BigDecimal.valueOf(-0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_78), jsonBigDecimal(BigDecimal.valueOf( 0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_79), jsonBigDecimal(BigDecimal.valueOf( 0.123_456_789)));
  }

  @Test
  public void testJsonBigDecimalUnroundedFromPreciseValue() {
    assertEquals(new JsonPrimitive(-1.23), jsonBigDecimalUnrounded(signedMoney(-1.23)));
    assertEquals(new JsonPrimitive( 0.00), jsonBigDecimalUnrounded(BigDecimal.ZERO));
    assertEquals(new JsonPrimitive( 1.23), jsonBigDecimalUnrounded(signedMoney( 1.23)));

    // jsonBigDecimalUnrounded() does not round to 8 digits
    assertEquals(new JsonPrimitive(-0.123_456_789), jsonBigDecimalUnrounded(signedMoney(-0.123_456_789)));
    assertEquals(new JsonPrimitive(-0.123_456_781), jsonBigDecimalUnrounded(signedMoney(-0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_781), jsonBigDecimalUnrounded(signedMoney( 0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_789), jsonBigDecimalUnrounded(signedMoney( 0.123_456_789)));
  }

  @Test
  public void testJsonBigDecimalUnrounded() {
    assertEquals(new JsonPrimitive(-1.23), jsonBigDecimalUnrounded(signedMoney(-1.23)));
    assertEquals(new JsonPrimitive( 0.00), jsonBigDecimalUnrounded(ZERO_SIGNED_MONEY));
    assertEquals(new JsonPrimitive( 1.23), jsonBigDecimalUnrounded(BigDecimal.valueOf( 1.23)));

    // jsonBigDecimalUnrounded() does not round to 8 digits
    assertEquals(new JsonPrimitive(-0.123_456_789), jsonBigDecimalUnrounded(BigDecimal.valueOf(-0.123_456_789)));
    assertEquals(new JsonPrimitive(-0.123_456_781), jsonBigDecimalUnrounded(BigDecimal.valueOf(-0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_781), jsonBigDecimalUnrounded(BigDecimal.valueOf( 0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_789), jsonBigDecimalUnrounded(BigDecimal.valueOf( 0.123_456_789)));
  }

  @Test
  public void testJsonBigDecimalFromPreciseValue() {
    assertEquals(new JsonPrimitive(-1.23), jsonBigDecimal(signedMoney(-1.23)));
    assertEquals(new JsonPrimitive( 0.00), jsonBigDecimal(ZERO_SIGNED_MONEY));
    assertEquals(new JsonPrimitive( 1.23), jsonBigDecimal(signedMoney( 1.23)));

    // jsonBigDecimal() rounds to 8 digits
    assertEquals(new JsonPrimitive(-0.123_456_79), jsonBigDecimal(signedMoney(-0.123_456_789)));
    assertEquals(new JsonPrimitive(-0.123_456_78), jsonBigDecimal(signedMoney(-0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_78), jsonBigDecimal(signedMoney( 0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_79), jsonBigDecimal(signedMoney( 0.123_456_789)));
  }

  @Test
  public void testJsonDoubleFromPreciseValue() {
    assertEquals(new JsonPrimitive(-1.23), jsonDouble(signedMoney(-1.23)));
    assertEquals(new JsonPrimitive( 0.00), jsonDouble(ZERO_SIGNED_MONEY));
    assertEquals(new JsonPrimitive( 1.23), jsonDouble(signedMoney( 1.23)));

    // jsonDouble() rounds to 8 digits
    assertEquals(new JsonPrimitive(-0.123_456_79), jsonDouble(signedMoney(-0.123_456_789)));
    assertEquals(new JsonPrimitive(-0.123_456_78), jsonDouble(signedMoney(-0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_78), jsonDouble(signedMoney( 0.123_456_781)));
    assertEquals(new JsonPrimitive( 0.123_456_79), jsonDouble(signedMoney( 0.123_456_789)));
  }

  @Test
  public void testJsonDate() {
    assertEquals(new JsonPrimitive("1970-01-01"), jsonDate(LocalDate.of(1970, 1, 1)));
    assertEquals(new JsonPrimitive("2014-04-04"), jsonDate(LocalDate.of(2014, 4, 4)));
  }

  @Test
  public void testJsonPercentageFromBigDecimal() {
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(12.3456)),
        jsonPercentage(unitFraction(0.123456)));
    assertEquals(
        new JsonPrimitive(new BigDecimal(100)),
        jsonPercentage(UNIT_FRACTION_1));
    assertEquals(
        new JsonPrimitive(BigDecimal.ZERO),
        jsonPercentage(UNIT_FRACTION_0));

    // BigDecimal inputs are rounded to 8 digits AFTER converting to percentages.
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(12.345_678_90)),
        jsonPercentage(unitFraction(0.123_456_789_012_345)));
  }

  @Test
  public void testJsonPercentageFromPreciseValue() {
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(12.3456)),
        jsonPercentage(signedFraction(0.123456)));
    assertEquals(
        new JsonPrimitive(new BigDecimal(100)),
        jsonPercentage(signedFraction(1.0)));
    assertEquals(
        new JsonPrimitive(BigDecimal.ZERO),
        jsonPercentage(signedFraction(0.0)));

    // PreciseValues inputs are rounded to 8 digits AFTER converting to percentages.
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(12.345_678_90)),
        jsonPercentage(money(0.123_456_789_012_345)));
  }

  @Test
  public void testJsonPercentageFromImpreciseValue() {
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(12.3456)),
        jsonPercentage(zScore(0.123456)));
    assertEquals(
        new JsonPrimitive(new BigDecimal(100)),
        jsonPercentage(zScore(1.0)));
    assertEquals(
        new JsonPrimitive(BigDecimal.ZERO),
        jsonPercentage(zScore(0.0)));

    // ImpreciseValues are stored as doubles, so they are rounded to 8 digits BEFORE
    // converting to percentages. So the final JSON value is rounded to 6 digits.
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(12.345_679)),
        jsonPercentage(zScore(0.123_456_789_012_345)));
  }

  @Test
  public void testJsonPercentageFromDouble() {
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(12.3456)),
        jsonPercentage(0.123456));
    assertEquals(
        new JsonPrimitive(new BigDecimal(100)),
        jsonPercentage(1.0));
    assertEquals(
        new JsonPrimitive(BigDecimal.ZERO),
        jsonPercentage(0.0));

    // The doubleValue is rounded to 8 digits BEFORE
    // converting to percentages. So the final JSON value is rounded to 6 digits.
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(12.345_679)),
        jsonPercentage(0.123_456_789_012_345));
  }

  @Test
  public void testUnitFractionFromJsonPercentage() {
    assertEquals(
        unitFraction(0.123456),
        unitFractionFromJsonPercentage(new JsonPrimitive(BigDecimal.valueOf(12.3456))));
    assertEquals(
        UNIT_FRACTION_1,
        unitFractionFromJsonPercentage(new JsonPrimitive(new BigDecimal(100))));
    assertEquals(
        UNIT_FRACTION_0,
        unitFractionFromJsonPercentage(new JsonPrimitive(BigDecimal.ZERO)));
  }

  @Test
  public void testBigDecimalFromJsonPercentage() {
    assertThat(
        bigDecimalFromJsonPercentage(new JsonPrimitive(BigDecimal.valueOf(12.3456))),
        bigDecimalMatcher(BigDecimal.valueOf(0.123456), 1e-8));
    assertThat(
        bigDecimalFromJsonPercentage(new JsonPrimitive(new BigDecimal(100))),
        bigDecimalMatcher(BigDecimal.ONE, 1e-8));
    assertThat(
        bigDecimalFromJsonPercentage(new JsonPrimitive(BigDecimal.ZERO)),
        bigDecimalMatcher(BigDecimal.ZERO, 1e-8));

    // does NOT round to 8 digits converting from JSON to double; only rounds when going TO JSON.
    assertEquals(
        0.123_456_789_012_34,
        doubleFromJsonPercentage(new JsonPrimitive(BigDecimal.valueOf(12.345_678_901_234))), 1e-14);
  }

  @Test
  public void testDoubleFromJsonPercentage() {
    assertEquals(-1.23456, doubleFromJsonPercentage(new JsonPrimitive(BigDecimal.valueOf(-123.456))), 1e-8);
    assertEquals( 0.0,     doubleFromJsonPercentage(new JsonPrimitive(BigDecimal.valueOf(   0.0  ))), 1e-8);
    assertEquals( 1.0,     doubleFromJsonPercentage(new JsonPrimitive(BigDecimal.valueOf( 100.0  ))), 1e-8);
    assertEquals( 1.23456, doubleFromJsonPercentage(new JsonPrimitive(BigDecimal.valueOf( 123.456))), 1e-8);

    // does NOT round to 8 digits converting from JSON to double; only rounds when going TO JSON.
    assertEquals(
        0.123_456_789_012_34,
        doubleFromJsonPercentage(new JsonPrimitive(BigDecimal.valueOf(12.345_678_901_234))), 1e-14);
  }

  @Test
  public void testJsonBps() {
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(1_234.56)),
        jsonBps(unitFraction(0.123456)));
    assertEquals(
        new JsonPrimitive(new BigDecimal(10_000)),
        jsonBps(UNIT_FRACTION_1));
    assertEquals(
        new JsonPrimitive(BigDecimal.ZERO),
        jsonBps(UNIT_FRACTION_0));
  }

  @Test
  public void testJsonBpsFromPreciseValue() {
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(1_234.56)),
        jsonBps(signedFraction(BigDecimal.valueOf(0.123456))));
    assertEquals(
        new JsonPrimitive(new BigDecimal(10_000)),
        jsonBps(signedFraction(BigDecimal.ONE)));
    assertEquals(
        new JsonPrimitive(BigDecimal.ZERO),
        jsonBps(signedFraction(BigDecimal.ZERO)));

    // PreciseValue input; rounds to 8 digits AFTER converting to BPS.
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(1_234.567_890_12)),
        jsonBps(signedFraction(BigDecimal.valueOf(0.123_456_789_012_345))));
  }

  @Test
  public void testJsonBpsFromImpreciseValue() {
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(1_234.56)),
        jsonBps(zScore(0.123456)));
    assertEquals(
        new JsonPrimitive(new BigDecimal(10_000)),
        jsonBps(zScore(1.0)));
    assertEquals(
        new JsonPrimitive(BigDecimal.ZERO),
        jsonBps(zScore(0.0)));

    // ImpreciseValues are stored as doubles, so they are rounded to 8 digits BEFORE
    // converting to BPS. So the final JSON value is rounded to 4 digits.
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(1_234.567_9)),
        jsonBps(zScore(0.123_456_789_012_345)));
  }

  @Test
  public void testJsonBpsFromDouble() {
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(1_234.56)),
        jsonBps(0.123456));
    assertEquals(
        new JsonPrimitive(new BigDecimal(10_000)),
        jsonBps(1.0));
    assertEquals(
        new JsonPrimitive(BigDecimal.ZERO),
        jsonBps(0.0));

    // jsonBps(double) rounds the input double to 8 digits BEFORE converting to BPS,
    // so the final value has 4 decimals precision.
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(1_234.567_9)),
        jsonBps(0.123_456_789_012_345));
  }

  @Test
  public void testJsonBpsFromBigDecimal() {
    assertEquals(
        new JsonPrimitive(BigDecimal.valueOf(1_234.56)),
        jsonBps(BigDecimal.valueOf(0.123456)));
    assertEquals(
        new JsonPrimitive(new BigDecimal(10_000)),
        jsonBps(BigDecimal.ONE));
    assertEquals(
        new JsonPrimitive(BigDecimal.ZERO),
        jsonBps(BigDecimal.ZERO));
  }

  @Test
  public void testDoubleFromJsonBps() {
    assertEquals(-0.0123456, doubleFromJsonBps(new JsonPrimitive(BigDecimal.valueOf(-123.456))), 1e-8);
    assertEquals( 0.0,       doubleFromJsonBps(new JsonPrimitive(BigDecimal.valueOf(   0.0  ))), 1e-8);
    assertEquals( 0.01,      doubleFromJsonBps(new JsonPrimitive(BigDecimal.valueOf( 100.0  ))), 1e-8);
    assertEquals( 0.0123456, doubleFromJsonBps(new JsonPrimitive(BigDecimal.valueOf( 123.456))), 1e-8);

    // does NOT round to 8 digits converting from JSON to double; only rounds when going TO JSON.
    assertEquals(0.001_234_567_890_123_4, doubleFromJsonBps(new JsonPrimitive(BigDecimal.valueOf(12.345_678_901_234))), 1e-14);
  }

  @Test
  public void testBigDecimalFromJsonBps() {
    assertThat(
        BigDecimal.valueOf(-0.0123456),
        bigDecimalMatcher(bigDecimalFromJsonBps(new JsonPrimitive(BigDecimal.valueOf(-123.456))), 1e-12));
    assertThat(
        BigDecimal.valueOf( 0.0),
        bigDecimalMatcher(bigDecimalFromJsonBps(new JsonPrimitive(BigDecimal.valueOf(   0.0  ))), 1e-12));
    assertThat(
        BigDecimal.valueOf( 0.01),
        bigDecimalMatcher(bigDecimalFromJsonBps(new JsonPrimitive(BigDecimal.valueOf( 100.0  ))), 1e-12));
    assertThat(
        BigDecimal.valueOf(0.0123456),
        bigDecimalMatcher(bigDecimalFromJsonBps(new JsonPrimitive(BigDecimal.valueOf( 123.456))), 1e-12));

    // does NOT round to 8 digits converting from JSON to double; only rounds when going TO JSON.
    assertThat(
        BigDecimal.valueOf(0.001_234_567_890_123_4),
        bigDecimalMatcher(bigDecimalFromJsonBps(new JsonPrimitive(BigDecimal.valueOf(12.345_678_901_234))), 1e-14));
  }

  @Test
  public void testJsonZeroBasedReturn() {
    assertEquals(
        jsonDouble(-7),
        jsonZeroBasedReturn(onesBasedLoss(0.93)));
    assertEquals(
        jsonDouble(0),
        jsonZeroBasedReturn(FLAT_RETURN));
    assertEquals(
        jsonDouble(7),
        jsonZeroBasedReturn(onesBasedGain(1.07)));
  }

  @Test
  public void testOnesBasedReturnFromJsonZeroBasedReturnPercentage() {
    assertEquals(
        onesBasedLoss(0.93),
        onesBasedReturnFromJsonZeroBasedReturnPercentage(jsonDouble(-7)));
    assertEquals(
        FLAT_RETURN,
        onesBasedReturnFromJsonZeroBasedReturnPercentage(jsonDouble(0)));
    assertEquals(
        onesBasedGain(1.07),
        onesBasedReturnFromJsonZeroBasedReturnPercentage(jsonDouble(7)));
  }

  @Test
  public void testIsEmpty_JsonObject() {
    assertTrue(isEmpty(emptyJsonObject()));
    assertFalse(isEmpty(singletonJsonObject(DUMMY_STRING, jsonDouble(DUMMY_DOUBLE))));
    assertFalse(isEmpty(singletonJsonObject(DUMMY_STRING, emptyJsonObject())));
    assertFalse(isEmpty(jsonObject(
        "dummy1", jsonDouble(DUMMY_DOUBLE),
        "dummy2", jsonString(DUMMY_STRING))));
  }

  @Test
  public void testIsEmpty_JsonArray() {
    assertTrue(isEmpty(emptyJsonArray()));
    assertFalse(isEmpty(singletonJsonArray(jsonString(DUMMY_STRING))));
    assertFalse(isEmpty(singletonJsonArray(emptyJsonArray())));
    assertFalse(isEmpty(singletonJsonArray(emptyJsonObject())));
  }

  @Test
  public void testIsSingleton_JsonArray() {
    assertFalse(isSingleton(emptyJsonArray()));
    assertTrue(isSingleton(singletonJsonArray(jsonString(DUMMY_STRING))));
    assertTrue(isSingleton(singletonJsonArray(emptyJsonArray())));
    assertTrue(isSingleton(singletonJsonArray(emptyJsonObject())));
    assertFalse(isSingleton(jsonArray(jsonString("abc"), jsonString("def"))));
    assertFalse(isSingleton(jsonArray(jsonString("abc"), jsonString("def"), jsonString("ghi"))));
  }

}
