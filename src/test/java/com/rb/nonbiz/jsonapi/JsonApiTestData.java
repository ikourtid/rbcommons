package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;

import java.util.List;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;

/**
 * A collection of pairs of JSON objects and their corresponding Java objects,
 * so we can make sure that back-and-forth conversions are correct.
 *
 * @see JsonApiTestPair
 */
public class JsonApiTestData<T> {

  private final MatcherGenerator<T> matcherGenerator;
  private final List<JsonApiTestPair<T>> testPairs;

  private JsonApiTestData(MatcherGenerator<T> matcherGenerator, List<JsonApiTestPair<T>> testPairs) {
    this.matcherGenerator = matcherGenerator;
    this.testPairs = testPairs;
  }

  @SafeVarargs
  public static <T> JsonApiTestData<T> jsonApiTestData(
      MatcherGenerator<T> matcherGenerator, JsonApiTestPair<T> first, JsonApiTestPair<T> ... rest) {
    return new JsonApiTestData<>(matcherGenerator, concatenateFirstAndRest(first, rest));
  }

  public List<JsonApiTestPair<T>> getTestPairs() {
    return testPairs;
  }

  public MatcherGenerator<T> getMatcherGenerator() {
    return matcherGenerator;
  }

}
