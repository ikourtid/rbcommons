package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.nonbiz.json.JsonSparseTimeSeries.JsonSparseTimeSeriesBuilder.jsonSparseTimeSeriesBuilder;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.jsonElementArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonStringArray;
import static com.rb.nonbiz.json.RBJsonArraysTest.jsonArrayExactMatcher;
import static com.rb.nonbiz.json.RBJsonLocalDateArrayTest.rbJsonLocalDateArrayMatcher;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonStringArrayTest.rbJsonStringArrayMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonSparseTimeSeriesTest extends RBTestMatcher<JsonSparseTimeSeries> {

  public static JsonSparseTimeSeries emptyJsonSparseTimeSeries() {
    return jsonSparseTimeSeriesBuilder().build();
  }

  @Test
  public void testAsJsonObject_noText() {
    assertThat(
        jsonSparseTimeSeriesBuilder()
            .addPoint(LocalDate.of(2014, 4, 4), jsonObject("a", jsonDouble(123), "b", jsonString("xxx")))
            .addPoint(LocalDate.of(2015, 5, 5), jsonObject("a", jsonDouble(456), "b", jsonString("yyy")))
            .build()
            .asJsonObject(),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "x", jsonStringArray("2014-04-04", "2015-05-05"),
                "y", jsonElementArray(
                    jsonObject("a", jsonDouble(123), "b", jsonString("xxx")),
                    jsonObject("a", jsonDouble(456), "b", jsonString("yyy"))))));
  }

  @Test
  public void testAsJsonObject_hasText() {
    assertThat(
        jsonSparseTimeSeriesBuilder()
            .addPoint(LocalDate.of(2014, 4, 4), jsonObject("a", jsonDouble(123), "b", jsonString("xxx")), "l1")
            .addPoint(LocalDate.of(2015, 5, 5), jsonObject("a", jsonDouble(456), "b", jsonString("yyy")), "l2")
            .build()
            .asJsonObject(),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "x", jsonStringArray("2014-04-04", "2015-05-05"),
                "y", jsonElementArray(
                    jsonObject("a", jsonDouble(123), "b", jsonString("xxx")),
                    jsonObject("a", jsonDouble(456), "b", jsonString("yyy"))),
                "text", jsonStringArray("l1", "l2"))));
  }

  @Override
  public JsonSparseTimeSeries makeTrivialObject() {
    return emptyJsonSparseTimeSeries();
  }

  @Override
  public JsonSparseTimeSeries makeNontrivialObject() {
    return jsonSparseTimeSeriesBuilder()
        .addPoint(LocalDate.of(2014, 4, 4), jsonObject("a", jsonDouble(123), "b", jsonString("xxx")))
        .addPoint(LocalDate.of(2015, 5, 5), jsonObject("a", jsonDouble(456), "b", jsonString("yyy")), "l2")
        .build();
  }

  @Override
  public JsonSparseTimeSeries makeMatchingNontrivialObject() {
    return jsonSparseTimeSeriesBuilder()
        .addPoint(LocalDate.of(2014, 4, 4), jsonObject("a", jsonDouble(123), "b", jsonString("xxx")))
        .addPoint(LocalDate.of(2015, 5, 5), jsonObject("a", jsonDouble(456), "b", jsonString("yyy")), "l2")
        .build();
  }

  @Override
  protected boolean willMatch(JsonSparseTimeSeries expected, JsonSparseTimeSeries actual) {
    return jsonSparseTimeSeriesMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonSparseTimeSeries> jsonSparseTimeSeriesMatcher(JsonSparseTimeSeries expected) {
    return makeMatcher(expected,
        match(        v -> v.getXCoordinates(), f -> rbJsonLocalDateArrayMatcher(f)),
        match(        v -> v.getYCoordinates(), f -> jsonArrayExactMatcher(f)),
        matchOptional(v -> v.getTextLabels(),   f -> rbJsonStringArrayMatcher(f)));
  }

}
