package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.jsonapi.JsonPrimitiveApiPair.jsonPrimitiveApiPair;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonPrimitiveMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

public class JsonPrimitiveApiPairTest extends RBTestMatcher<JsonPrimitiveApiPair<Double>> {

  @Override
  public JsonPrimitiveApiPair<Double> makeTrivialObject() {
    return jsonPrimitiveApiPair(0.0, jsonDouble(0.0));
  }

  @Override
  public JsonPrimitiveApiPair<Double> makeNontrivialObject() {
    return jsonPrimitiveApiPair(123.45, jsonDouble(123.45));
  }

  @Override
  public JsonPrimitiveApiPair<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return jsonPrimitiveApiPair(123.45 + e, jsonDouble(123.45 + e));
  }

  @Override
  protected boolean willMatch(JsonPrimitiveApiPair<Double> expected, JsonPrimitiveApiPair<Double> actual) {
    return jsonPrimitiveApiPairMatcher(expected, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<JsonPrimitiveApiPair<T>> jsonPrimitiveApiPairMatcher(
      JsonPrimitiveApiPair<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getJavaObject(), matcherGenerator),
        match(v -> v.getJsonPrimitive(), f -> jsonPrimitiveMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

}
