package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.RBCategoryMap;

import java.util.function.Function;

/**
 * Converts an {@link RBCategoryMap} back and forth to JSON for our public API.
 */
public class RBCategoryMapJsonApiConverter implements HasJsonApiDocumentation {

  public <K, V> JsonObject toJsonObject(
      RBCategoryMap<K, V> rbCategoryMap,
      Function<K, String> keySerializer,
      Function<V, JsonElement> valueSerializer) {
    return null;
  }

  public <K, V> RBCategoryMap<K, V> fromJsonObject(
    JsonObject jsonObject,
    Function<String, K> keyDeserializer,
    Function<JsonElement, V> valueDeserializer) {

    return null;
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return null;
  }

}
