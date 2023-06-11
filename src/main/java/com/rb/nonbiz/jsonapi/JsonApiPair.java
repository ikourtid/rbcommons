package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;

/**
 * Represents a Java object and its corresponding JSON object representation.
 *
 * <p> The unit tests will check that conversions in both directions produce the correct results. </p>
 */
public class JsonApiPair<T> {

  private final T javaObject;
  private final JsonObject jsonObject;

  private JsonApiPair(T javaObject, JsonObject jsonObject) {
    this.javaObject = javaObject;
    this.jsonObject = jsonObject;
  }

  public static <T> JsonApiPair<T> jsonApiPair(T javaObject, JsonObject jsonObject) {
    return new JsonApiPair<>(javaObject, jsonObject);
  }

  public T getJavaObject() {
    return javaObject;
  }

  public JsonObject getJsonObject() {
    return jsonObject;
  }

}
