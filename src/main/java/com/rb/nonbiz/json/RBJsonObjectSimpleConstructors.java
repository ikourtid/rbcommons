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

  public static JsonObject jsonObject(
      String property1, JsonElement element1,
      String property2, JsonElement element2,
      String property3, JsonElement element3,
      String property4, JsonElement element4,
      String property5, JsonElement element5,
      String property6, JsonElement element6,
      String property7, JsonElement element7,
      String property8, JsonElement element8,
      String property9, JsonElement element9) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10,
      String property11, JsonElement element11) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
        .setJsonElement(property11, element11)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10,
      String property11, JsonElement element11,
      String property12, JsonElement element12) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
        .setJsonElement(property11, element11)
        .setJsonElement(property12, element12)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10,
      String property11, JsonElement element11,
      String property12, JsonElement element12,
      String property13, JsonElement element13) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
        .setJsonElement(property11, element11)
        .setJsonElement(property12, element12)
        .setJsonElement(property13, element13)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10,
      String property11, JsonElement element11,
      String property12, JsonElement element12,
      String property13, JsonElement element13,
      String property14, JsonElement element14) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
        .setJsonElement(property11, element11)
        .setJsonElement(property12, element12)
        .setJsonElement(property13, element13)
        .setJsonElement(property14, element14)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10,
      String property11, JsonElement element11,
      String property12, JsonElement element12,
      String property13, JsonElement element13,
      String property14, JsonElement element14,
      String property15, JsonElement element15) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
        .setJsonElement(property11, element11)
        .setJsonElement(property12, element12)
        .setJsonElement(property13, element13)
        .setJsonElement(property14, element14)
        .setJsonElement(property15, element15)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10,
      String property11, JsonElement element11,
      String property12, JsonElement element12,
      String property13, JsonElement element13,
      String property14, JsonElement element14,
      String property15, JsonElement element15,
      String property16, JsonElement element16) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
        .setJsonElement(property11, element11)
        .setJsonElement(property12, element12)
        .setJsonElement(property13, element13)
        .setJsonElement(property14, element14)
        .setJsonElement(property15, element15)
        .setJsonElement(property16, element16)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10,
      String property11, JsonElement element11,
      String property12, JsonElement element12,
      String property13, JsonElement element13,
      String property14, JsonElement element14,
      String property15, JsonElement element15,
      String property16, JsonElement element16,
      String property17, JsonElement element17) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
        .setJsonElement(property11, element11)
        .setJsonElement(property12, element12)
        .setJsonElement(property13, element13)
        .setJsonElement(property14, element14)
        .setJsonElement(property15, element15)
        .setJsonElement(property16, element16)
        .setJsonElement(property17, element17)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10,
      String property11, JsonElement element11,
      String property12, JsonElement element12,
      String property13, JsonElement element13,
      String property14, JsonElement element14,
      String property15, JsonElement element15,
      String property16, JsonElement element16,
      String property17, JsonElement element17,
      String property18, JsonElement element18) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
        .setJsonElement(property11, element11)
        .setJsonElement(property12, element12)
        .setJsonElement(property13, element13)
        .setJsonElement(property14, element14)
        .setJsonElement(property15, element15)
        .setJsonElement(property16, element16)
        .setJsonElement(property17, element17)
        .setJsonElement(property18, element18)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10,
      String property11, JsonElement element11,
      String property12, JsonElement element12,
      String property13, JsonElement element13,
      String property14, JsonElement element14,
      String property15, JsonElement element15,
      String property16, JsonElement element16,
      String property17, JsonElement element17,
      String property18, JsonElement element18,
      String property19, JsonElement element19) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
        .setJsonElement(property11, element11)
        .setJsonElement(property12, element12)
        .setJsonElement(property13, element13)
        .setJsonElement(property14, element14)
        .setJsonElement(property15, element15)
        .setJsonElement(property16, element16)
        .setJsonElement(property17, element17)
        .setJsonElement(property18, element18)
        .setJsonElement(property19, element19)
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
      String property8, JsonElement element8,
      String property9, JsonElement element9,
      String property10, JsonElement element10,
      String property11, JsonElement element11,
      String property12, JsonElement element12,
      String property13, JsonElement element13,
      String property14, JsonElement element14,
      String property15, JsonElement element15,
      String property16, JsonElement element16,
      String property17, JsonElement element17,
      String property18, JsonElement element18,
      String property19, JsonElement element19,
      String property20, JsonElement element20) {
    return rbJsonObjectBuilder()
        .setJsonElement(property1, element1)
        .setJsonElement(property2, element2)
        .setJsonElement(property3, element3)
        .setJsonElement(property4, element4)
        .setJsonElement(property5, element5)
        .setJsonElement(property6, element6)
        .setJsonElement(property7, element7)
        .setJsonElement(property8, element8)
        .setJsonElement(property9, element9)
        .setJsonElement(property10, element10)
        .setJsonElement(property11, element11)
        .setJsonElement(property12, element12)
        .setJsonElement(property13, element13)
        .setJsonElement(property14, element14)
        .setJsonElement(property15, element15)
        .setJsonElement(property16, element16)
        .setJsonElement(property17, element17)
        .setJsonElement(property18, element18)
        .setJsonElement(property19, element19)
        .setJsonElement(property20, element20)
        .build();
  }

}
