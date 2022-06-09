package com.rb.nonbiz.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.RBMap;

import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;

public class RBJsonObjectSimpleConstructors {

  public static JsonObject emptyJsonObject() {
    return new JsonObject();
  }

  public static JsonObject jsonObject(RBMap<String, JsonElement> map) {
    JsonObject jsonObject = new JsonObject();
    map.forEachEntry( (key, jsonElement) -> jsonObject.add(key, jsonElement));
    return jsonObject;
  }

  public static JsonObject singletonJsonObject(String property, JsonElement element) {
    return rbJsonObjectBuilder()
        .setJsonElement(property, element)
        .build();
  }

  public static JsonObject singletonJsonObject(String property, String stringElement) {
    return rbJsonObjectBuilder()
        .setString(property, stringElement)
        .build();
  }

  public static JsonObject jsonObject(
      String property1, JsonElement element1,
      String property2, JsonElement element2) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .build();
  }

  public static JsonObject jsonObject(
      String property1, JsonElement element1,
      String property2, JsonElement element2,
      String property3, JsonElement element3) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .build();
  }

  public static JsonObject jsonObject(
      String property1, JsonElement element1,
      String property2, JsonElement element2,
      String property3, JsonElement element3,
      String property4, JsonElement element4) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .build();
  }

  public static JsonObject jsonObject(
      String property1, JsonElement element1,
      String property2, JsonElement element2,
      String property3, JsonElement element3,
      String property4, JsonElement element4,
      String property5, JsonElement element5) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .build();
  }

  public static JsonObject jsonObject(
      String property1, JsonElement element1,
      String property2, JsonElement element2,
      String property3, JsonElement element3,
      String property4, JsonElement element4,
      String property5, JsonElement element5,
      String property6, JsonElement element6) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .build();
  }

  public static JsonObject jsonObject(
      String property1, JsonElement element1,
      String property2, JsonElement element2,
      String property3, JsonElement element3,
      String property4, JsonElement element4,
      String property5, JsonElement element5,
      String property6, JsonElement element6,
      String property7, JsonElement element7) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .build();
  }

  public static JsonObject jsonObject(
      String property1, JsonElement element1,
      String property2, JsonElement element2,
      String property3, JsonElement element3,
      String property4, JsonElement element4,
      String property5, JsonElement element5,
      String property6, JsonElement element6,
      String property7, JsonElement element7,
      String property8, JsonElement element8) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .build();
  }

}
