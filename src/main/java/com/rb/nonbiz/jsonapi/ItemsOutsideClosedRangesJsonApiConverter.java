package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.ItemsOutsideClosedRanges;

import java.util.function.Function;

import static com.rb.nonbiz.collections.ItemsOutsideClosedRanges.itemsOutsideClosedRanges;
import static com.rb.nonbiz.json.RBJsonObjects.jsonObjectToRBMap;
import static com.rb.nonbiz.json.RBJsonObjects.rbMapToJsonObject;

/**
 * Converts a {@link ItemsOutsideClosedRanges} to / from JSON for our JSON APIs.
 */
public class ItemsOutsideClosedRangesJsonApiConverter implements HasJsonApiDocumentation {

  // Can't use JsonValidator, because the keys aren't fixed here
  @Inject ValueOutsideClosedRangeJsonApiConverter valueOutsideClosedRangeJsonApiConverter;

  public <K, V extends Comparable<? super V>> JsonObject toJsonObject(
      ItemsOutsideClosedRanges<K, V> itemsOutsideClosedRanges,
      Function<K, String> keySerializer,
      Function<V, JsonPrimitive> valueSerializer) {
    return rbMapToJsonObject(
        itemsOutsideClosedRanges.getRawMap(),
        keySerializer,
        v -> valueOutsideClosedRangeJsonApiConverter.toJsonObject(v, valueSerializer));
  }

  public <K, V extends Comparable<? super V>> ItemsOutsideClosedRanges<K, V> fromJsonObject(
      JsonObject jsonObject,
      Function<String, K> keyDeserializer,
      Function<JsonPrimitive, V> valueDeserializer) {
    return itemsOutsideClosedRanges(
        jsonObjectToRBMap(
            jsonObject,
            keyDeserializer,
            v -> valueOutsideClosedRangeJsonApiConverter.fromJsonObject(v.getAsJsonObject(), valueDeserializer)));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return null;
  }

}
