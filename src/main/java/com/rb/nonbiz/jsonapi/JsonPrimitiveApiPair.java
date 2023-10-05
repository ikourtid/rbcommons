package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonPrimitive;
import com.rb.nonbiz.text.Strings;

/**
 * Represents a Java object and its corresponding JSON primitive representation, for those Java objects whose JSON
 * representation is a {@link JsonPrimitive}.
 *
 * <p> The unit tests will check that conversions in both directions produce the correct results. </p>
 *
 * @see JsonApiPair
 */
public class JsonPrimitiveApiPair<T> {

  private final T javaObject;
  private final JsonPrimitive jsonPrimitive;

  private JsonPrimitiveApiPair(T javaObject, JsonPrimitive jsonPrimitive) {
    this.javaObject = javaObject;
    this.jsonPrimitive = jsonPrimitive;
  }

  public static <T> JsonPrimitiveApiPair<T> jsonPrimitiveApiPair(
      T javaObject,
      JsonPrimitive jsonPrimitive) {
    return new JsonPrimitiveApiPair<>(javaObject, jsonPrimitive);
  }

  public T getJavaObject() {
    return javaObject;
  }

  public JsonPrimitive getJsonPrimitive() {
    return jsonPrimitive;
  }

  @Override
  public String toString() {
    return Strings.format("[JPAP %s %s JPAP]",
        javaObject, jsonPrimitive);
  }

}
