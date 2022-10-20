package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;

import java.util.List;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;

/**
 * A collection of pairs of JSON arrays and the corresponding Java objects,
 * so we can make sure that back-and-forth conversions are correct.
 */
public class JsonArrayApiTestData<T> {

  private final MatcherGenerator<T> matcherGenerator;
  private final List<JsonArrayApiTestPair<T>> testPairs;

  private JsonArrayApiTestData
      (MatcherGenerator<T> matcherGenerator,
       List<JsonArrayApiTestPair<T>> testPairs) {
    this.matcherGenerator = matcherGenerator;
    this.testPairs = testPairs;
  }

  @SafeVarargs
  public static <T> JsonArrayApiTestData<T> jsonArrayApiTestData(
      MatcherGenerator<T> matcherGenerator,
      JsonArrayApiTestPair<T> first,
      JsonArrayApiTestPair<T> ... rest) {
    return new JsonArrayApiTestData<>(
        matcherGenerator,
        concatenateFirstAndRest(first, rest));
  }

  public List<JsonArrayApiTestPair<T>> getTestPairs() {
    return testPairs;
  }

  public MatcherGenerator<T> getMatcherGenerator() {
    return matcherGenerator;
  }

}