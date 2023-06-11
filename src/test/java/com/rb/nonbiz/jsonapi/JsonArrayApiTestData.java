package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;

import java.util.List;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;

/**
 * A collection of pairs of JSON arrays and their corresponding Java objects,
 * so we can make sure that back-and-forth conversions are correct.
 *
 * @see JsonArrayApiPair
 */
public class JsonArrayApiTestData<T> {

  private final MatcherGenerator<T> matcherGenerator;
  private final List<JsonArrayApiPair<T>> testPairs;

  private JsonArrayApiTestData(
      MatcherGenerator<T> matcherGenerator,
      List<JsonArrayApiPair<T>> testPairs) {
    this.matcherGenerator = matcherGenerator;
    this.testPairs = testPairs;
  }

  @SafeVarargs
  public static <T> JsonArrayApiTestData<T> jsonArrayApiTestData(
      MatcherGenerator<T> matcherGenerator,
      JsonArrayApiPair<T> first,
      JsonArrayApiPair<T>... rest) {
    return new JsonArrayApiTestData<>(
        matcherGenerator,
        concatenateFirstAndRest(first, rest));
  }

  public List<JsonArrayApiPair<T>> getTestPairs() {
    return testPairs;
  }

  public MatcherGenerator<T> getMatcherGenerator() {
    return matcherGenerator;
  }

}