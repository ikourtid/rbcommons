package com.rb.nonbiz.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import static com.rb.biz.types.StringFunctions.isAllWhiteSpace;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.text.Strings.formatCollectionInOrder;
import static java.util.Collections.singletonList;

/**
 * Represents a way to drill down a {@link JsonObject} (possibly down multiple levels, including other JSON
 * subobjects) to get to a {@link JsonElement} inside it.
 */
public class JsonObjectPath {

  private final List<String> jsonProperties;

  private JsonObjectPath(List<String> jsonProperties) {
    this.jsonProperties = jsonProperties;
  }

  public static JsonObjectPath jsonObjectPath(List<String> jsonProperties) {
    RBPreconditions.checkArgument(
        !jsonProperties.isEmpty(),
        "We must have at least one property in the JsonObjectPath");
    // Ideally we would have a typesafe JsonProperty type here that disallows such bad values,
    // but it's too late by now (May 2023) to make such a change, as most code uses a plain String
    // to denote a JSON property.
    RBPreconditions.checkArgument(
        jsonProperties.stream().noneMatch(v -> v.isEmpty() || isAllWhiteSpace(v)),
        "We cannot have empty or all-whitespace properties in the JSON path: %s",
        jsonProperties);
    return new JsonObjectPath(jsonProperties);
  }

  public static JsonObjectPath jsonObjectPath(String first, String ... rest) {
    return jsonObjectPath(concatenateFirstAndRest(first, rest));
  }

  public static JsonObjectPath singletonJsonObjectPath(String onlyProperty) {
    return jsonObjectPath(singletonList(onlyProperty));
  }

  public List<String> getJsonProperties() {
    return jsonProperties;
  }

  @Override
  public String toString() {
    return Strings.format("[JOP %s JOP]", formatCollectionInOrder(jsonProperties));
  }

}
