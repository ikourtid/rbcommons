package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonArray;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;

import java.util.List;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonArrayEpsilonMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

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

  public void testRoundTripConversions(
      Function<T, JsonArray> toJsonArray,
      Function<JsonArray, T> fromJsonArray) {
    for (int i = 0; i < testPairs.size(); i++) {
      JsonArrayApiPair<T> testPair = testPairs.get(i);
      JsonArray expectedJsonArray  = testPair.getJsonArray();
      T         expectedJavaObject = testPair.getJavaObject();
      JsonArray actualJsonArray    = toJsonArray.apply(expectedJavaObject);
      T         actualJavaObject   = fromJsonArray.apply(expectedJsonArray);

      assertThat(
          Strings.format("Test %s of %s : JSON array (generated from Java object) does not match expected",
              i + 1, testPairs.size()),
          actualJsonArray,
          jsonArrayEpsilonMatcher(expectedJsonArray));
      assertThat(
          Strings.format("Test %s of %s : Java object (generated from JSON array) does not match expected",
              i + 1, testPairs.size()),
          actualJavaObject,
          matcherGenerator.apply(expectedJavaObject));
    }
  }

}