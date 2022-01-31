package com.rb.nonbiz.json;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.biz.jsonapi.JsonTickerMap;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.MutableIidMap;
import com.rb.nonbiz.collections.MutableRBMap;
import com.rb.nonbiz.collections.MutableRBSet;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.SimpleArrayIndexMapping;
import com.rb.nonbiz.text.HasUniqueId;
import com.rb.nonbiz.text.RBSetOfHasUniqueId;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.rb.biz.jsonapi.JsonTicker.jsonTicker;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSetWithExpectedSize;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBRanges.constructRange;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBJsonArrays.emptyJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArrayToList;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonElementOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getOptionalJsonElement;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.text.RBSetOfHasUniqueId.rbSetOfHasUniqueId;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static java.util.Comparator.comparing;

/**
 * Various utilities related to RBMap / IidMap {@code <--> } JsonObject conversions.
 */
public class RBJsonObjects {

  /**
   * Converts a closed range to a JSON object.
   * Throws if the range has either an open lower bound or an open upper bound.
   */
  public static <C extends Comparable<? super C>> JsonObject closedRangeToJsonObject(
      Range<C> range, Function<C, JsonElement> valueSerializer) {
    RBPreconditions.checkArgument(
        !range.hasLowerBound() || range.lowerBoundType() == BoundType.CLOSED,
        "range lower bound must be absent or closed; was %s",
        range);
    RBPreconditions.checkArgument(
        !range.hasUpperBound() || range.upperBoundType() == BoundType.CLOSED,
        "range upper bound must be absent or closed; was %s",
        range);
    return rbJsonObjectBuilder()
        .setIf("min", range, range.hasLowerBound(), v -> valueSerializer.apply(v.lowerEndpoint()))
        .setIf("max", range, range.hasUpperBound(), v -> valueSerializer.apply(v.upperEndpoint()))
        .build();
  }

  public static <K, V> JsonObject rbMapToJsonObject(
      RBMap<K, V> map,
      Function<K, String> keySerializer,
      Function<V, JsonElement> valueSerializer) {
    // Since 'map' is an RBMap, the code below has the advantage that it ensures
    // that the transformed / serialized keys are unique.
    // Note that we don't have to do this with IidMaps (@see #iidMapToJsonObject)
    // because InstrumentId keys are unique, and they get serialized in a standardized way.
    return jsonObject(
        map.transformKeysAndValuesCopy(keySerializer, valueSerializer));
  }

  /**
   * Converts an RBSet to a JsonObject by using the supplied lambdas to create String keys and JsonElement values.
   */
  public static <T> JsonObject rbSetToJsonObject(
      RBSet<T> set,
      Function<T, String> keySerializer,
      Function<T, JsonElement> valueSerializer) {
    return jsonObject(set.toRBMapWithTransformedKeys(keySerializer, valueSerializer));
  }

  /**
   * Converts an iidMap to a JsonObject using a Function that transforms each JsonElement.
   */
  public static <V> JsonObject iidMapToJsonObject(
      IidMap<V> iidMap,
      JsonTickerMap jsonTickerMap,
      Function<V, JsonElement> valueSerializer) {
    JsonObject jsonObject = new JsonObject();
    // The following code will insert items into the JSON object in ticker alphabetical order.
    // I'm not sure if there's a guarantee that they will also PRINT in that order,
    // but in practice that seems to be the case.
    iidMap.instrumentIdStream()
        .map(longId -> {
          InstrumentId instrumentId = instrumentId(longId.asLong());
          V value = iidMap.getOrThrow(instrumentId);
          return pair(
              jsonTickerMap.getJsonTickerOrThrow(instrumentId).getFreeFormTicker(),
              valueSerializer.apply(value));
        })
        .sorted(comparing(v -> v.getLeft()))
        .forEach(pair -> jsonObject.add(pair.getLeft(), pair.getRight()));
    return jsonObject;
  }

  /**
   * Converts an iidMap to a JsonObject using a BiFunction that transforms each (InstrumentId, JsonElement) entry.
   */
  public static <V> JsonObject iidMapToJsonObject(
      IidMap<V> iidMap,
      JsonTickerMap jsonTickerMap,
      BiFunction<InstrumentId, V, JsonElement> valueSerializer) {
    JsonObject jsonObject = new JsonObject();
    // The following code will insert items into the JSON object in ticker alphabetical order.
    // I'm not sure if there's a guarantee that they will also PRINT in that order,
    // but in practice that seems to be the case.
    iidMap.instrumentIdStream()
        .map(longId -> {
          InstrumentId instrumentId = instrumentId(longId.asLong());
          V value = iidMap.getOrThrow(instrumentId);
          return pair(
              jsonTickerMap.getJsonTickerOrThrow(instrumentId).getFreeFormTicker(),
              valueSerializer.apply(instrumentId, value));
        })
        .sorted(comparing(v -> v.getLeft()))
        .forEach(pair -> jsonObject.add(pair.getLeft(), pair.getRight()));
    return jsonObject;
  }

  public static <V extends HasUniqueId<V>> JsonObject rbSetOfHasUniqueIdToJsonObject(
      RBSetOfHasUniqueId<V> rbSetOfHasUniqueId,
      Function<V, JsonElement> valueSerializer) {
    return rbMapToJsonObject(
        rbSetOfHasUniqueId.getRawMap(),
        uniqueId -> uniqueId.getStringId(),
        valueSerializer);
  }

  public static <V> JsonArray arrayIndexMappingToJsonArray(
      ArrayIndexMapping<V> arrayIndexMapping,
      Function<V, JsonElement> valueSerializer) {
    JsonArray jsonArray = emptyJsonArray();
    for (int i = 0; i < arrayIndexMapping.size(); i++) {
      jsonArray.add(valueSerializer.apply(arrayIndexMapping.getKey(i)));
    }
    return jsonArray;
  }

  public static <V> RBSet<V> jsonObjectToRBSet(
      JsonObject jsonObject,
      BiFunction<String, JsonElement, V> deserializer) {
    Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
    MutableRBSet<V> mutableSet = newMutableRBSetWithExpectedSize(entrySet.size());
    entrySet.forEach(entry -> mutableSet.addAssumingAbsent(deserializer.apply(entry.getKey(), entry.getValue())));
    return newRBSet(mutableSet);
  }

  public static <V extends HasUniqueId<V>> RBSetOfHasUniqueId<V> jsonObjectToRBSetOfHasUniqueId(
      JsonObject jsonObject,
      Function<JsonElement, V> valueDeserializer) {
    return rbSetOfHasUniqueId(
        jsonObjectToRBMap(
            jsonObject,
            uniqueIdString -> uniqueId(uniqueIdString),
            valueDeserializer));
  }

  public static <V> Stream<V> jsonObjectToStream(
      JsonObject jsonObject,
      BiFunction<String, JsonElement, V> deserializer) {
    return jsonObject.entrySet()
        .stream()
        .map(entry -> deserializer.apply(entry.getKey(), entry.getValue()));
  }

  public static <K, V> RBMap<K, V> jsonObjectToRBMap(
      JsonObject jsonObject,
      Function<String, K> keyDeserializer,
      Function<JsonElement, V> valueDeserializer) {
    return jsonObjectToRBMap(
        jsonObject,
        keyDeserializer,
        (ignoredKey, jsonElement) -> valueDeserializer.apply(jsonElement));
  }

  public static <K, V> RBMap<K, V> jsonObjectToRBMap(
      JsonObject jsonObject,
      Function<String, K> keyDeserializer,
      BiFunction<K, JsonElement, V> valueDeserializer) {
    Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(entrySet.size());
    entrySet.forEach(entry -> {
      K key = keyDeserializer.apply(entry.getKey());
      V value = valueDeserializer.apply(key, entry.getValue());
      mutableMap.putAssumingAbsent(key, value);
    });
    return newRBMap(mutableMap);
  }

  public static <V> IidMap<V> jsonObjectToIidMap(
      JsonObject jsonObject,
      JsonTickerMap jsonTickerMap,
      Function<JsonElement, V> valueDeserializer) {
    Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(entrySet.size());
    entrySet.forEach(entry -> {
      String key = entry.getKey();
      V value = valueDeserializer.apply(entry.getValue());
      mutableMap.putAssumingAbsent(jsonTickerMap.getInstrumentIdOrThrow(jsonTicker(key)), value);
    });
    return newIidMap(mutableMap);
  }

  public static <V> IidMap<V> jsonObjectToIidMap(
      JsonObject jsonObject,
      JsonTickerMap jsonTickerMap,
      BiFunction<InstrumentId, JsonElement, V> valueDeserializer) {
    Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(entrySet.size());
    entrySet.forEach(entry -> {
      String key = entry.getKey();
      InstrumentId instrumentId = jsonTickerMap.getInstrumentIdOrThrow(jsonTicker(key));
      V value = valueDeserializer.apply(instrumentId, entry.getValue());
      mutableMap.putAssumingAbsent(instrumentId, value);
    });
    return newIidMap(mutableMap);
  }

  public static <V> SimpleArrayIndexMapping<V> jsonArrayToSimpleArrayIndexMapping(
      JsonArray jsonArray,
      Function<JsonElement, V> valueDeserializer) {
    return simpleArrayIndexMapping(
        jsonArrayToList(jsonArray, valueDeserializer));
  }

  public static <C extends Comparable<? super C>> Range<C> jsonObjectToRange(
      JsonObject jsonObject,
      Function<JsonElement, C> valueDeserializer) {
    JsonValidator.staticValidate(jsonObject, jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .setOptionalProperties("min", "max")
        .build());
    return constructRange(
        transformOptional(getOptionalJsonElement(jsonObject, "min"), valueDeserializer), BoundType.CLOSED,
        transformOptional(getOptionalJsonElement(jsonObject, "max"), valueDeserializer), BoundType.CLOSED);
  }

  public static <C extends Comparable<? super C>> ClosedRange<C> jsonObjectToClosedRange(
      JsonObject jsonObject,
      Function<JsonElement, C> valueDeserializer) {
    JsonValidator.staticValidate(jsonObject, jsonValidationInstructionsBuilder()
        .setRequiredProperties("min", "max")
        .hasNoOptionalProperties()
        .build());
    return closedRange(
        valueDeserializer.apply(getJsonElementOrThrow(jsonObject, "min")),
        valueDeserializer.apply(getJsonElementOrThrow(jsonObject, "max")));
  }

  public static void ifHasJsonObjectProperty(
      JsonObject jsonObject,
      String property,
      Consumer<JsonObject> consumer) {
    if (jsonObject.has(property)) {
      consumer.accept(jsonObject.get(property).getAsJsonObject());
    }
  }

  public static void ifHasJsonObjectPropertyElse(
      JsonObject jsonObject,
      String property,
      Consumer<JsonObject> consumer,
      Runnable ifNotIncluded) {
    if (jsonObject.has(property)) {
      consumer.accept(jsonObject.get(property).getAsJsonObject());
    } else {
      ifNotIncluded.run();
    }
  }

  public static void ifHasDoublePropertyElse(
      JsonObject jsonObject,
      String property,
      DoubleConsumer ifIncluded,
      Runnable ifNotIncluded) {
    RBPreconditions.checkArgument(
        !jsonObject.has(property) ||
            (jsonObject.get(property).isJsonPrimitive() && jsonObject.getAsJsonPrimitive(property).isNumber()),
        "json property '%s' is not a number: %s",
        property, jsonObject.get(property));
    if (jsonObject.has(property)) {
      ifIncluded.accept(jsonObject.get(property).getAsDouble());
    } else {
      ifNotIncluded.run();
    }
  }

}
