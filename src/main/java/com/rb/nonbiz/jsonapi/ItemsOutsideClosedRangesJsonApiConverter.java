package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.rb.biz.jsonapi.JsonTickerMap;
import com.rb.nonbiz.collections.ItemsOutsideClosedRanges;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.json.RBGson;
import com.rb.nonbiz.json.RBJsonObjectBuilder;
import com.rb.nonbiz.json.RBJsonObjects;

import java.util.function.Function;

import static com.rb.nonbiz.collections.ItemsOutsideClosedRanges.itemsOutsideClosedRanges;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjects.jsonObjectToRBMap;

/**
 * Converts a {@link ItemsOutsideClosedRanges} to / from JSON for our JSON APIs.
 */
public class ItemsOutsideClosedRangesJsonApiConverter implements HasJsonApiDocumentation {

  // Can't use JsonValidator, because the keys aren't fixed here

  public <K, V extends Comparable<? super V>> JsonObject toJsonObject(
      ItemsOutsideClosedRanges<K, V> itemsOutsideClosedRanges,
      Function<K, String> keySerializer) {
    return rbJsonObjectBuilder()
        .build();
  }

  public <K, V extends Comparable<? super V>> ItemsOutsideClosedRanges<K, V> fromJsonObject(
      JsonObject jsonObject,
      Function<String, K> keyDeserializer) {
    return itemsOutsideClosedRanges(
        jsonObjectToRBMap(
            jsonObject,
            keyDeserializer,

        )
    )
    return null;
  }


  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return null;
  }

}
