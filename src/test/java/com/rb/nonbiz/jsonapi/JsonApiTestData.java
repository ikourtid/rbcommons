package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;

import java.util.List;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

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

  public void testRoundTripConversions(
      Function<T, JsonObject> toJsonObject,
      Function<JsonObject, T> fromJsonObject) {
    for (int i = 0; i < testPairs.size(); i++) {
      JsonApiTestPair<T> testPair = testPairs.get(i);
      JsonObject expectedJson = testPair.getJsonObject();
      T    expectedJavaObject = testPair.getJavaObject();
      JsonObject actualJson = toJsonObject.apply(expectedJavaObject);
      T    actualJavaObject = fromJsonObject.apply(expectedJson);

      assertThat(
          Strings.format("Test %s of %s : JSON (generated from Java object) does not match expected", i + 1, testPairs.size()),
          actualJson,
          jsonObjectEpsilonMatcher(expectedJson));
      assertThat(
          Strings.format("Test %s of %s : Java object (generated from JSON) does not match expected", i + 1, testPairs.size()),
          actualJavaObject,
          matcherGenerator.apply(expectedJavaObject));
    }
  }

  // Only tests a conversion from JSON to Java, not vice versa.
  // Useful for cases where the opposite conversion does not exist, or is done by a different class whose code
  // is meant to be tested elsewhere.
  public void testFromJsonObject(
      Function<JsonObject, T> fromJsonObject) {
    for (int i = 0; i < testPairs.size(); i++) {
      JsonApiTestPair<T> testPair = testPairs.get(i);
      JsonObject expectedJson = testPair.getJsonObject();
      T    expectedJavaObject = testPair.getJavaObject();
      T    actualJavaObject = fromJsonObject.apply(expectedJson);

      assertThat(
          Strings.format("Test %s of %s : Java object (generated from JSON) does not match expected", i + 1, testPairs.size()),
          actualJavaObject,
          matcherGenerator.apply(expectedJavaObject));
    }
  }

  // Only tests a conversion to JSON from Java, not vice versa.
  // Useful for cases where the opposite conversion does not exist, or is done by a different class whose code
  // is meant to be tested elsewhere.
  public void testToJsonObject(
      Function<T, JsonObject> toJsonObject) {
    for (int i = 0; i < testPairs.size(); i++) {
      JsonApiTestPair<T> testPair = testPairs.get(i);
      JsonObject expectedJson = testPair.getJsonObject();
      T    expectedJavaObject = testPair.getJavaObject();
      JsonObject actualJson = toJsonObject.apply(expectedJavaObject);

      assertThat(
          Strings.format("Test %s of %s : JSON (generated from Java object) does not match expected", i + 1, testPairs.size()),
          actualJson,
          jsonObjectEpsilonMatcher(expectedJson));
    }
  }

}
