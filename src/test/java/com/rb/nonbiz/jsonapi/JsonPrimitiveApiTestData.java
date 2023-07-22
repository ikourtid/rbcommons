package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;

import java.util.List;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;

/**
 * A collection of pairs of JSON primitives and their corresponding Java objects,
 * so we can make sure that back-and-forth conversions are correct.
 *
 * @see JsonArrayApiPair
 * @see JsonApiPair
 */
public class JsonPrimitiveApiTestData<T>  {

  private final MatcherGenerator<T> matcherGenerator;
  private final List<JsonPrimitiveApiPair<T>> testPairs;

  private JsonPrimitiveApiTestData(
      MatcherGenerator<T> matcherGenerator,
      List<JsonPrimitiveApiPair<T>> testPairs) {
    this.matcherGenerator = matcherGenerator;
    this.testPairs = testPairs;
  }

  @SafeVarargs
  public static <T> JsonPrimitiveApiTestData<T> jsonPrimitiveApiTestData(
      MatcherGenerator<T> matcherGenerator,
      JsonPrimitiveApiPair<T> first,
      JsonPrimitiveApiPair<T>... rest) {
    return new JsonPrimitiveApiTestData<>(
        matcherGenerator,
        concatenateFirstAndRest(first, rest));
  }

  public List<JsonPrimitiveApiPair<T>> getTestPairs() {
    return testPairs;
  }

  public MatcherGenerator<T> getMatcherGenerator() {
    return matcherGenerator;
  }

}
