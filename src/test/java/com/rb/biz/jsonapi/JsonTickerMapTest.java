package com.rb.biz.jsonapi;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.biz.jsonapi.JsonTickerMapImplTest.jsonTickerMapImplMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class JsonTickerMapTest {

  public static TypeSafeMatcher<JsonTickerMap> jsonTickerMapMatcher(JsonTickerMap expected) {
    // This is not great, but all tests currently (June 2019) use the JsonTickerMapImpl implementation,
    // so this should be fine.
    return makeMatcher(expected,
        match(v -> (JsonTickerMapImpl) v, f -> jsonTickerMapImplMatcher(f)));
  }

}
