package com.rb.biz.jsonapi;

import com.google.gson.JsonObject;

/**
 * In most cases where we convert Java objects to JSON (and vice versa),
 * the conversions will not take any extra parameters. This is a nice API that lets us make most such
 * converter verb classes uniform. It also helps simplify the specification of unit tests when applicable,
 * kind of like the way RBTestMatcher lets a test class inherit a set of pre-created unit tests.
 */
public interface JsonRoundTripConverter<T> {

  JsonObject toJsonObject(T javaObject, JsonTickerMap jsonTickerMap);

  T fromJsonObject(JsonObject jsonObject, JsonTickerMap jsonTickerMap);

}
