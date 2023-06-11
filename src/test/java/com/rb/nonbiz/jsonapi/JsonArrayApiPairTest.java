package com.rb.nonbiz.jsonapi;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testmatchers.RBCollectionMatchers;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import static com.rb.nonbiz.json.RBJsonArrays.emptyJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonDoubleArray;
import static com.rb.nonbiz.jsonapi.JsonArrayApiPair.jsonArrayApiPair;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonArrayEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.emptyList;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import java.util.Collections;
import java.util.List;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

// This test class is not generic, but the prod class and static matcher are.
public class JsonArrayApiPairTest extends RBTestMatcher<JsonArrayApiPair<List<Double>>> {

  @Override
  public JsonArrayApiPair<List<Double>> makeTrivialObject() {
    return jsonArrayApiPair(emptyList(), emptyJsonArray());
  }

  @Override
  public JsonArrayApiPair<List<Double>> makeNontrivialObject() {
    return jsonArrayApiPair(
        ImmutableList.of(-1.1, 3.3),
        jsonDoubleArray( -1.1, 3,3));
  }

  @Override
  public JsonArrayApiPair<List<Double>> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return jsonArrayApiPair(
        ImmutableList.of(-1.1 + e, 3.3 + e),
        jsonDoubleArray( -1.1 + e, 3,3 + e));
  }

  @Override
  protected boolean willMatch(JsonArrayApiPair<List<Double>> expected, JsonArrayApiPair<List<Double>> actual) {
    return jsonArrayApiPairMatcher(expected, f -> doubleListMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<JsonArrayApiPair<T>> jsonArrayApiPairMatcher(
      JsonArrayApiPair<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getJavaObject(), matcherGenerator),
        match(v -> v.getJsonArray(), f -> jsonArrayEpsilonMatcher(f)));
  }

}
