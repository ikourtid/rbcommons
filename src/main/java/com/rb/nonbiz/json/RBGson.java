package com.rb.nonbiz.json;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.rb.biz.types.OnesBasedReturn;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.UnitFraction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.nonbiz.date.RBDates.checkYearIsReasonable;
import static com.rb.nonbiz.date.RBDates.yyyyMMdd;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * Various wrappers around Gson to increase readability of code.
 */
public class RBGson {

  public static final BigDecimal FRACTION_TO_PERCENTAGE = new BigDecimal("100");
  public static final BigDecimal PERCENTAGE_TO_FRACTION = new BigDecimal("0.01");

  public static final BigDecimal FRACTION_TO_BPS = new BigDecimal("10000");
  public static final BigDecimal BPS_TO_FRACTION = new BigDecimal("0.0001");

  public static JsonPrimitive jsonBoolean(boolean b) {
    return new JsonPrimitive(b);
  }

  public static JsonPrimitive jsonString(String string) {
    return new JsonPrimitive(string);
  }

  // See long comment below on why we round to 8 digits
  public static JsonPrimitive jsonDouble(double d) {
    return new JsonPrimitive(roundTo8digits(d));
  }

  // Sometimes we need less precision to avoid spurious diffs when regenerating Json.
  public static JsonPrimitive jsonDoubleRoundedTo6Digits(double d) {
    return new JsonPrimitive(roundTo6digits(d));
  }

  // round to 2 digits, perhaps for money amounts
  public static JsonPrimitive jsonDoubleRoundedTo2Digits(double d) {
    return new JsonPrimitive(roundTo2digits(d));
  }

  public static JsonPrimitive jsonDoubleUnrounded(double d) {
    return new JsonPrimitive(d);
  }

  public static JsonPrimitive jsonInteger(int d) {
    return new JsonPrimitive(d);
  }

  public static JsonPrimitive jsonYear(int year) {
    checkYearIsReasonable(Year.of(year));
    return jsonInteger(year);
  }

  public static JsonPrimitive jsonLong(long l) {
    return new JsonPrimitive(l);
  }

  /**
   *  In theory, we want to write this out with perfect precision.
   *
   *  return new JsonPrimitive(preciseValue.asBigDecimal());
   *
   *  In practice, we don't care about that much detail, so it doesn't matter. However, more importantly,
   *  many of the PreciseValues used are generated with the output of optimizer, so they may be precise,
   *  but not accurate. If we don't round to 8 digits, then the JSON may record something like 100.000000000001,
   *  which we don't really want, because the JSON comparisons we use aren't 'epsilon comparisons',
   *  so they would cause tests to fail.
   */
  public static <P extends PreciseValue<? super P>> JsonPrimitive jsonBigDecimal(P preciseValue) {
    return new JsonPrimitive(roundTo8digits(preciseValue.doubleValue()));
  }

  public static JsonPrimitive jsonBigDecimal(BigDecimal bigDecimal) {
    return new JsonPrimitive(roundTo8digits(bigDecimal.doubleValue()));
  }

  public static JsonPrimitive jsonBigDecimalUnrounded(PreciseValue preciseValue) {
    return jsonBigDecimalUnrounded(preciseValue.asBigDecimal());
  }

  public static JsonPrimitive jsonBigDecimalUnrounded(BigDecimal bigDecimal) {
    return new JsonPrimitive(bigDecimal);
  }

  public static <P extends PreciseValue<? super P>> JsonPrimitive jsonDouble(P preciseValue) {
    return jsonDouble(preciseValue.doubleValue());
  }

  public static <I extends ImpreciseValue<? super I>> JsonPrimitive jsonDouble(I impreciseValue) {
    return jsonDouble(impreciseValue.doubleValue());
  }

  public static JsonPrimitive jsonDate(LocalDate date) {
    return jsonString(yyyyMMdd(date));
  }

  private static double roundTo8digits(double value) {
    return Math.round(value * 100_000_000d) / 100_000_000d;
  }

  private static double roundTo6digits(double value) {
    return Math.round(value * 1_000_000d) / 1_000_000d;
  }

  private static double roundTo2digits(double value) {
    return Math.round(value * 100d) / 100d;
  }

  public static String toMinimalJson(JsonElement jsonElement) {
    return new GsonBuilder().create().toJson(jsonElement);
  }

  public static String toPrettyJson(JsonElement jsonElement) {
    return new GsonBuilder().setPrettyPrinting().create().toJson(jsonElement);
  }

  public static String toPrettyJson(String jsonString) {
    return toPrettyJson(JsonParser.parseString(jsonString).getAsJsonObject());
  }

  // multiply an input fraction by 100 to get a percentage
  public static JsonPrimitive jsonPercentage(UnitFraction unitFraction) {
    return jsonPercentage(unitFraction.asBigDecimal());
  }

  // multiply an input fraction by 100 to get a percentage
  public static <P extends PreciseValue<? super P>> JsonPrimitive jsonPercentage(P preciseValue) {
    return jsonPercentage(preciseValue.asBigDecimal());
  }

  // multiply an input fraction by 100 to get a percentage
  public static <I extends ImpreciseValue<? super I>> JsonPrimitive jsonPercentage(I impreciseValue) {
    return jsonPercentage(impreciseValue.doubleValue());
  }

  // multiply an input fraction by 100 to get a percentage
  public static JsonPrimitive jsonPercentage(double doubleValue) {
    return jsonPercentage(BigDecimal.valueOf(roundTo8digits(doubleValue)));
  }

  // multiply an input fraction by 100 to get a percentage
  public static JsonPrimitive jsonPercentage(BigDecimal bigDecimal) {
    return jsonBigDecimal(bigDecimal.multiply(FRACTION_TO_PERCENTAGE, DEFAULT_MATH_CONTEXT));
  }

  // multiply an input percentage by 0.01 to get a fraction
  public static UnitFraction unitFractionFromJsonPercentage(JsonElement jsonElementPercentage) {
    return unitFraction(bigDecimalFromJsonPercentage(jsonElementPercentage));
  }

  // multiply an input percentage by 0.01 to get a fraction
  public static BigDecimal bigDecimalFromJsonPercentage(JsonElement jsonElementPercentage) {
    return jsonElementPercentage.getAsBigDecimal().multiply(PERCENTAGE_TO_FRACTION, DEFAULT_MATH_CONTEXT);
  }

  // multiply an input percentage by 0.01 to get a fraction
  public static double doubleFromJsonPercentage(JsonElement jsonElementPercentage) {
    return bigDecimalFromJsonPercentage(jsonElementPercentage).doubleValue();
  }

  // multiply an input fraction by 10_000 to get BPS
  public static <P extends PreciseValue<? super P>> JsonPrimitive jsonBps(P preciseValue) {
    return jsonBps(preciseValue.asBigDecimal());
  }

  // multiply an input fraction by 10_000 to get BPS
  public static <I extends ImpreciseValue<? super I>> JsonPrimitive jsonBps(I impreciseValue) {
    return jsonBps(impreciseValue.doubleValue());
  }

  // multiply an input fraction by 10_000 to get BPS
  public static JsonPrimitive jsonBps(double doubleValue) {
    return jsonBps(BigDecimal.valueOf(roundTo8digits(doubleValue)));
  }

  // multiply an input fraction by 10_000 to get BPS
  public static JsonPrimitive jsonBps(BigDecimal bigDecimal) {
    return jsonBigDecimal(bigDecimal.multiply(FRACTION_TO_BPS, DEFAULT_MATH_CONTEXT));
  }

  // multiply an input BPS amount by 0.0001 to get a fraction
  public static double doubleFromJsonBps(JsonElement jsonElementBps) {
    return bigDecimalFromJsonBps(jsonElementBps).doubleValue();
  }

  // multiply an input BPS amount by 0.0001 to get a fraction
  public static BigDecimal bigDecimalFromJsonBps(JsonElement jsonElementBps) {
    return jsonElementBps.getAsBigDecimal().multiply(BPS_TO_FRACTION, DEFAULT_MATH_CONTEXT);
  }

  public static JsonPrimitive jsonZeroBasedReturn(OnesBasedReturn onesBasedReturn) {
    return jsonBigDecimal(onesBasedReturn.asBigDecimal().subtract(BigDecimal.ONE, DEFAULT_MATH_CONTEXT)
        .multiply(FRACTION_TO_PERCENTAGE, DEFAULT_MATH_CONTEXT));
  }

  public static OnesBasedReturn onesBasedReturnFromJsonZeroBasedReturnPercentage(JsonElement jsonElement) {
    return onesBasedReturnFromJsonZeroBasedReturnPercentage(jsonElement.getAsBigDecimal());
  }

  public static OnesBasedReturn onesBasedReturnFromJsonZeroBasedReturnPercentage(BigDecimal zeroBasedReturn) {
    return onesBasedReturn(zeroBasedReturn
        .multiply(PERCENTAGE_TO_FRACTION, DEFAULT_MATH_CONTEXT)
        .add(BigDecimal.ONE, DEFAULT_MATH_CONTEXT));
  }

  // We could call this jsonObjectIsEmpty (longer), but usually it will be obvious from the variable name.
  public static boolean isEmpty(JsonObject jsonObject) {
    return jsonObject.size() == 0;
  }

  // We could call this jsonArrayIsEmpty (longer), but usually it will be obvious from the variable name.
  public static boolean isEmpty(JsonArray jsonArray) {
    return jsonArray.size() == 0;
  }

  // We could call this jsonArrayIsSingleton (longer), but usually it will be obvious from the variable name.
  public static boolean isSingleton(JsonArray jsonArray) {
    return jsonArray.size() == 1;
  }

}
