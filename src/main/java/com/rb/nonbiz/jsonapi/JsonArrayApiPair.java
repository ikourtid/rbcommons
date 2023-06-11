package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonArray;

/**
 * Represents a Java object and its corresponding JSON array representation, for those Java objects whose JSON
 * representation is a {@link JsonArray}.
 *
 * <p> The unit tests will check that conversions in both directions produce the correct results. </p>
 *
 * @see JsonApiPair
 */
public class JsonArrayApiPair<T> {

  private final T javaObject;
  private final JsonArray jsonArray;

  private JsonArrayApiPair(T javaObject, JsonArray jsonArray) {
    this.javaObject = javaObject;
    this.jsonArray = jsonArray;
  }

  public static <T> JsonArrayApiPair<T> jsonArrayApiPair(
      T javaObject,
      JsonArray jsonArray) {
    return new JsonArrayApiPair<>(javaObject, jsonArray);
  }

  public T getJavaObject() {
    return javaObject;
  }

  public JsonArray getJsonArray() {
    return jsonArray;
  }

}
