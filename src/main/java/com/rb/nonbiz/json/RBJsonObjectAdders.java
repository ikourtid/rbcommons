package com.rb.nonbiz.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;

public class RBJsonObjectAdders {

  // This is very simple but it allows us to add stuff fluently, which makes the code shorter.
  public static JsonObject addToJsonObject(JsonObject inputObject, String property, JsonElement jsonElement) {
    RBPreconditions.checkArgument(
        !inputObject.has(property),
        "Json already has property '%s' so we cannot add value '%s': json was %s",
        property, jsonElement, inputObject);
    inputObject.add(property, jsonElement);
    return inputObject;
  }

  // This is very simple but it allows us to add stuff fluently, which makes the code shorter.
  public static JsonObject addStringToJsonObject(JsonObject inputObject, String property, String value) {
    RBPreconditions.checkArgument(
        !inputObject.has(property),
        "Json already has property '%s' so we cannot add value '%s': json was %s",
        property, value, inputObject);
    inputObject.add(property, jsonString(value));
    return inputObject;
  }

  // This is very simple but it allows us to add stuff fluently, which makes the code shorter.
  public static JsonObject addAllAssumingNoOverlap(JsonObject target, JsonObject source) {
    source.entrySet().forEach(entry -> {
      String property = entry.getKey();
      JsonElement value = entry.getValue();
      RBPreconditions.checkArgument(
          !target.has(property),
          "Json already has property '%s' so we cannot add value '%s': json was %s",
          property, value, target);
      target.add(property, value);
    });
    return target;
  }

  /**
   * Adds { property : jsonSubObject } to inputObject if jsonSubObject is non-empty.
   * Throws if property already exists in inputObject.
   *
   * This is very simple but it allows us to add stuff fluently, which makes the code shorter.
   */
  public static JsonObject addToJsonObjectIfNonEmpty(JsonObject inputObject, String property, JsonObject jsonSubObject) {
    RBPreconditions.checkArgument(
        !inputObject.has(property),
        "Json already has property '%s' so we cannot add jsonSubObject '%s': json was %s",
        property, jsonSubObject, inputObject);
    if (!jsonSubObject.entrySet().isEmpty()) {
      inputObject.add(property, jsonSubObject);
    }
    return inputObject;
  }

  /**
   * Adds { property : jsonSubArray } to inputObject if jsonSubArray is non-empty.
   * Throws if property already exists in inputObject.
   *
   * This is very simple but it allows us to add stuff fluently, which makes the code shorter.
   */
  public static JsonObject addToJsonObjectIfNonEmpty(JsonObject inputObject, String property, JsonArray jsonSubArray) {
    RBPreconditions.checkArgument(
        !inputObject.has(property),
        "Json already has property '%s' so we cannot add jsonArray '%s': json was %s",
        property, jsonSubArray, inputObject);
    if (jsonSubArray.size() > 0) {
      inputObject.add(property, jsonSubArray);
    }
    return inputObject;
  }

  /**
   * Adds { property : jsonElement } to inputObject if {@code Optional<T>} is present.
   * Throws if 'property' already exists in inputObject.
   */
  public static <T> JsonObject addToJsonObjectIfOptionalPresent(
      JsonObject inputObject, String property, Optional<T> maybeValue, Function<T, JsonElement> valueSerializer) {
    RBPreconditions.checkArgument(
        !inputObject.has(property),
        "Json already has property '%s' so we cannot add Optional<T> '%s': json was %s",
        property, maybeValue, inputObject);
    if (maybeValue.isPresent()) {
      inputObject.add(property, valueSerializer.apply(maybeValue.get()));
    }
    return inputObject;
  }

  /**
   * Adds { property : jsonElement } to inputObject if T passes the predicate.
   * Throws if 'property' already exists in inputObject.
   */
  public static <T> JsonObject addToJsonObjectIf(
      JsonObject inputObject, String property, T value, Predicate<T> onlyIncludeIf, Function<T, JsonElement> valueSerializer) {
    RBPreconditions.checkArgument(
        !inputObject.has(property),
        "Json object already has property '%s' so we cannot add '%s': json object was %s",
        property, value, inputObject);
    if (onlyIncludeIf.test(value)) {
      inputObject.add(property, valueSerializer.apply(value));
    }
    return inputObject;
  }

  /**
   * Adds { property : jsonDouble } to inputObject if this PreciseValue is non-zero (subject to an epsilon).
   * Throws if 'property' already exists in inputObject.
   */
  public static <P extends PreciseValue<? super P>> JsonObject addPreciseValueToJsonObjectIfNonZero(
      JsonObject inputObject, String property, P value, double epsilon) {
    return addToJsonObjectIf(inputObject, property, value, v -> !v.isAlmostZero(epsilon), v -> jsonDouble(v));
  }

}
