package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.MutableRBMap;
import com.rb.nonbiz.collections.MutableRBSet;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.jsonapi.JsonApiDocumentation.JsonApiDocumentationBuilder;
import com.rb.nonbiz.types.PreciseValue;

import java.math.BigDecimal;
import java.util.function.Function;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSetWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.json.RBGson.jsonBigDecimal;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentation.JsonApiDocumentationBuilder.intermediateJsonApiDocumentationWithFixme;

/**
 * This makes for shorter, easier-to-read JSON for cases where we're dealing with a map of precise values.
 * This allows for precise values to be written as a number, instead of a full object (which we'd see if we
 * were to use a generic Gson conversion).
 *
 * Example: instead of a map (in this case, of unit fractions) looking like this:
 *
 * rawMap
 *    CASH_CLASS
 *       value: 0.002
 *    tips
 *       value: 0.05988
 *
 * ... it will look like this:
 *
 * rawMap
 *    CASH_CLASS: 0.002
 *    tips:       0.05988
 *
 */
public class PreciseValueRBMapJsonConverter implements HasJsonApiDocumentation {

  public <K, V extends PreciseValue<? super V>> JsonObject toJsonObject(
      RBMap<K, V> map,
      Function<K, String> keySerializer) {
    JsonObject jsonObject = new JsonObject();
    MutableRBSet<String> serializedKeysEncountered = newMutableRBSetWithExpectedSize(map.size());
    map.forEachEntry( (key, value) -> {
      String serializedKey = keySerializer.apply(key);
      serializedKeysEncountered.addAssumingAbsent(serializedKey);
      jsonObject.add(serializedKey, jsonBigDecimal(value));
    });
    return jsonObject;
  }

  public <K, V extends PreciseValue<? super V>> RBMap<K, V> fromJsonObject(
      JsonObject jsonObject, Function<String, K> keyConverter, Function<BigDecimal, V> bigDecimalConverter) {
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(jsonObject.entrySet().size());
    jsonObject.entrySet().forEach(entry -> {
      String key = entry.getKey();
      BigDecimal value = entry.getValue().getAsBigDecimal();
      mutableMap.putAssumingAbsent(keyConverter.apply(key), bigDecimalConverter.apply(value));
    });
    return newRBMap(mutableMap);
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return intermediateJsonApiDocumentationWithFixme(RBMap.class);
  }

}
