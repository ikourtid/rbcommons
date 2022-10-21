package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonArray;

/**
 * Represents a Java object and its corresponding JSON array representation.
 *
 * <p> The unit tests will check that conversions in both directions produce the correct results. </p>
 */
public class JsonArrayApiTestPair<T> {

  private final T javaObject;
  private final JsonArray jsonArray;

  private JsonArrayApiTestPair(T javaObject, JsonArray jsonArray) {
    this.javaObject = javaObject;
    this.jsonArray = jsonArray;
  }

  public static <T> JsonArrayApiTestPair<T> jsonArrayApiTestPair(
      T javaObject,
      JsonArray jsonArray) {
    return new JsonArrayApiTestPair<>(javaObject, jsonArray);
  }

  public T getJavaObject() {
    return javaObject;
  }

  public JsonArray getJsonArray() {
    return jsonArray;
  }

}
