package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;

/**
 * Represents a Java object and its corresponding JSON object representation.
 *
 * <p> The unit tests will check that conversions in both directions produce the right results. </p>
 */
public class JsonApiTestPair<T> {

  private final T javaObject;
  private final JsonObject jsonObject;

  private JsonApiTestPair(T javaObject, JsonObject jsonObject) {
    this.javaObject = javaObject;
    this.jsonObject = jsonObject;
  }

  public static <T> JsonApiTestPair<T> jsonApiTestPair(T javaObject, JsonObject jsonObject) {
    return new JsonApiTestPair<>(javaObject, jsonObject);
  }

  public T getJavaObject() {
    return javaObject;
  }

  public JsonObject getJsonObject() {
    return jsonObject;
  }

}
