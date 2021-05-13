package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.nonbiz.json.JsonPointList.JsonPointListBuilder.jsonPointListBuilder;
import static com.rb.nonbiz.json.RBJsonArrays.jsonStringArray;
import static com.rb.nonbiz.json.RBJsonDoubleArray.rbJsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonDoubleArrayTest.rbJsonDoubleArrayMatcher;
import static com.rb.nonbiz.json.RBJsonLocalDateArrayTest.rbJsonLocalDateArrayMatcher;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonStringArrayTest.rbJsonStringArrayMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonPointListTest extends RBTestMatcher<JsonPointList> {

  public static JsonPointList emptyJsonPointList() {
    return jsonPointListBuilder().build();
  }

  public static JsonPointList singletonJsonPointList(LocalDate date, double value) {
    return jsonPointListBuilder()
        .addPoint(date, value)
        .build();
  }

  public static JsonPointList jsonPointList(LocalDate date1, double value1, LocalDate date2, double value2) {
    return jsonPointListBuilder()
        .addPoint(date1, value1)
        .addPoint(date2, value2)
        .build();
  }

  @Test
  public void testAsJsonObject_noText() {
    assertThat(
        jsonPointListBuilder()
            .addPoint(LocalDate.of(2014, 4, 4), 123)
            .addPoint(LocalDate.of(2015, 5, 5), 456)
            .build()
            .asJsonObject(),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "x", jsonStringArray("2014-04-04", "2015-05-05"),
                "y", rbJsonDoubleArray(123, 456).asJsonArray())));
  }

  @Test
  public void testAsJsonObject_hasText() {
    assertThat(
        jsonPointListBuilder()
            .addPoint(LocalDate.of(2014, 4, 4), 123, "a")
            .addPoint(LocalDate.of(2015, 5, 5), 456, "b")
            .build()
            .asJsonObject(),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "x", jsonStringArray("2014-04-04", "2015-05-05"),
                "y", rbJsonDoubleArray(123, 456).asJsonArray(),
                "text", jsonStringArray("a", "b"))));
  }

  @Override
  public JsonPointList makeTrivialObject() {
    return emptyJsonPointList();
  }

  @Override
  public JsonPointList makeNontrivialObject() {
    return jsonPointListBuilder()
        .addPoint(LocalDate.of(2014, 4, 4), 123)
        .addPoint(LocalDate.of(2015, 5, 5), 456, "xyz")
        .build();
  }

  @Override
  public JsonPointList makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return jsonPointListBuilder()
        .addPoint(LocalDate.of(2014, 4, 4), 123 + e)
        .addPoint(LocalDate.of(2015, 5, 5), 456 + e, "xyz")
        .build();
  }

  @Override
  protected boolean willMatch(JsonPointList expected, JsonPointList actual) {
    return jsonPointListMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonPointList> jsonPointListMatcher(JsonPointList expected) {
    return makeMatcher(expected,
        match(        v -> v.getXCoordinates(), f -> rbJsonLocalDateArrayMatcher(f)),
        match(        v -> v.getYCoordinates(), f -> rbJsonDoubleArrayMatcher(f, 1e-8)),
        matchOptional(v -> v.getTextLabels(),   f -> rbJsonStringArrayMatcher(f)));
  }

}
