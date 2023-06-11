package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiPair.jsonApiPair;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

// This test class is not generic, but the prod class and static matcher are.
public class JsonApiPairTest extends RBTestMatcher<JsonApiPair<Double>> {

  @Override
  public JsonApiPair<Double> makeTrivialObject() {
    return jsonApiPair(0.0, singletonJsonObject("v", jsonDouble(0)));
  }

  @Override
  public JsonApiPair<Double> makeNontrivialObject() {
    return jsonApiPair(1.23, singletonJsonObject("value", jsonDouble(1.23)));
  }

  @Override
  public JsonApiPair<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return jsonApiPair(1.23 + e, singletonJsonObject("value", jsonDouble(1.23 + e)));
  }

  @Override
  protected boolean willMatch(JsonApiPair<Double> expected, JsonApiPair<Double> actual) {
    return jsonApiPairMatcher(expected, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<JsonApiPair<T>> jsonApiPairMatcher(
      JsonApiPair<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getJavaObject(), matcherGenerator),
        match(v -> v.getJsonObject(), f -> jsonObjectEpsilonMatcher(f)));
  }

}
