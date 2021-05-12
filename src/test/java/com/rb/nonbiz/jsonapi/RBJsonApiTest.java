package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.rb.biz.jsonapi.JsonRoundTripConverter;
import com.rb.biz.jsonapi.JsonTickerMap;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.util.List;

import static com.rb.biz.jsonapi.JsonTickerMapImplTest.TEST_JSON_TICKER_MAP;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * For JSON API converters, the test classes should inherit from RBJsonApiTest.
 * Just like RBTestMatcher gives you some 'free' unit tests, so will this.
 *
 * This could have implemented RBIntegrationTest instead, because we never use mocks here,
 * but that ends up being a pain to do because of some complication with generics.
 */
public abstract class RBJsonApiTest<T> extends RBTest<JsonRoundTripConverter<T>> {

  public abstract JsonApiTestData<T> getTestData();

  public JsonTickerMap getTickerMap() {
    return TEST_JSON_TICKER_MAP;
  }

  @Test
  public void testRoundTripConversions() {
    JsonApiTestData<T> testData = getTestData();
    MatcherGenerator<T> matcherGenerator = testData.getMatcherGenerator();
    List<JsonApiTestPair<T>> testPairs = testData.getTestPairs();
    JsonTickerMap jsonTickerMap = getTickerMap();

    JsonRoundTripConverter<T> jsonRoundTripConverter = makeTestObject();
    for (int i = 0; i < testPairs.size(); i++) {
      JsonApiTestPair<T> testPair = testPairs.get(i);
      JsonObject expectedJson = testPair.getJsonObject();
      T expectedJavaObject = testPair.getJavaObject();
      JsonObject actualJson = jsonRoundTripConverter.toJsonObject(expectedJavaObject, jsonTickerMap);
      T actualJavaObject = jsonRoundTripConverter.fromJsonObject(expectedJson, jsonTickerMap);

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

}
