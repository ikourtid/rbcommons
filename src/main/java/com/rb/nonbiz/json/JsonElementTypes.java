package com.rb.nonbiz.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.json.JsonElementType.JSON_ARRAY;
import static com.rb.nonbiz.json.JsonElementType.JSON_BOOLEAN;
import static com.rb.nonbiz.json.JsonElementType.JSON_NULL;
import static com.rb.nonbiz.json.JsonElementType.JSON_NUMBER;
import static com.rb.nonbiz.json.JsonElementType.JSON_OBJECT;
import static com.rb.nonbiz.json.JsonElementType.JSON_STRING;

/**
 * Methods related to getting the type of a {@link JsonElement}.
 */
public class JsonElementTypes {

  public static JsonElementType getJsonElementType(JsonElement jsonElement) {
    if (jsonElement.isJsonNull()) {
      return JSON_NULL;
    }
    if (jsonElement.isJsonPrimitive()) {
      JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
      if (jsonPrimitive.isBoolean()) {
        return JSON_BOOLEAN;
      }
      if (jsonPrimitive.isNumber()) {
        return JSON_NUMBER;
      }
      if (jsonPrimitive.isString()) {
        return JSON_STRING;
      }
      throw new IllegalArgumentException(Strings.format("Unknown JsonElement primitive type: %s", jsonElement));
    }
    if (jsonElement.isJsonObject()) {
      return JSON_OBJECT;
    }
    if (jsonElement.isJsonArray()) {
      return JSON_ARRAY;
    }
    throw new IllegalArgumentException(Strings.format("Unknown JsonElement non-primitive type: %s", jsonElement));
  }

}
