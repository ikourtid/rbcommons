package com.rb.nonbiz.json;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.biz.jsonapi.JsonTickerMap;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.*;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.text.HasUniqueId;
import com.rb.nonbiz.text.RBSetOfHasUniqueId;
import com.rb.nonbiz.util.JsonRoundTripStringConvertibleEnum;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.rb.biz.jsonapi.JsonTicker.jsonTicker;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSetWithExpectedSize;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBRanges.constructRange;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR;
import static com.rb.nonbiz.json.RBJsonArrays.emptyJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArrayToList;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonElementOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getOptionalJsonElement;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.text.RBSetOfHasUniqueId.rbSetOfHasUniqueId;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static java.util.Comparator.comparing;
import static java.util.Map.Entry.comparingByKey;

/**
 * Various static utilities related to {@link RBMap} / {@link IidMap} {@code <--> } {@link JsonObject} conversions.
 */
public class RBJsonObjects {

  /**
   * Converts a closed range to a JSON object.
   * Throws if the range has only one bound, or if the range has either an open upper or lower bound.
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
    // that the transformed / serialized keys are unique,
    // because setJsonElement throws if we try to add values for the same key.
    // Note that we don't have to do this with IidMaps (@see #iidMapToJsonObject)
    // because InstrumentId keys are unique, and they get serialized in a standardized way.

    // When we serialize a general RBMap, we often need JSON properties to be ordered for determinism purposes.
    // For instance, when doing git diff, it's easier to compare the before and after outputs of backtests that are
    // run with the backtest JSON API. This method does not guarantee this to its caller, which is why 'ordered'
    // is not part of the name. The only reason this is expected (but not guaranteed) to work is that the implementation
    // of JsonObject seems to retain the order that items were added to it, and its serialization reflects that.
    // It wasn't actually possible to replicate the old behavior with a failing test, but it does seem to fix some
    // nondeterminism in a backtest (a JSON-API-driven one) so it's worth keeping this code.
    RBJsonObjectBuilder builder = rbJsonObjectBuilder();
    map.entrySet()
        .stream()
        // We end up calling keySerializer twice for every key: first, for sorting the map entries, and then for
        // finding out what string property to use in the JSON. This is the least inefficient way to do this probably.
        // We could put the sorted entries into a TreeMap, but then we'd have to deal with the overhead of creating
        // another map.
        .sorted(comparing(entry -> keySerializer.apply(entry.getKey())))
        .forEach(entry -> {
          K key = entry.getKey();
          V value = entry.getValue();
          builder.setJsonElement(keySerializer.apply(key), valueSerializer.apply(value));
        });
    return builder.build();
  }

  /**
   * Although a JsonObject does not ensure a certain order, sometimes it is convenient to have a JSON object
   * with its entries constructed in a certain order, because (depending on the implementation of JsonObject),
   * it's either guaranteed, or at least very likely, to show properties in order in the final JSON.
   * For cases where humans read the JSON, it will make it more legible.
   */
  public static <K, V> JsonObject orderedRBMapToJsonObject(
      RBMap<K, V> map,
      Function<K, String> keySerializer,
      Function<V, JsonElement> valueSerializer,
      Comparator<K> comparator) {
    // Since 'map' is an RBMap, the code below has the advantage that it ensures
    // that the transformed / serialized keys are unique.
    // Note that we don't have to do this with IidMaps (@see #iidMapToJsonObject)
    // because InstrumentId keys are unique, and they get serialized in a standardized way.

    // Note that we can't just return the following:
    // jsonObject(map.orderedTransformKeysAndValuesCopy(keySerializer, valueSerializer, comparator));
    // It's subtle. Here's why. Even if orderedTransformKeysAndValuesCopy constructs the map in a deterministic order,
    // the jsonObject() constructor that takes in an RBMap will call that map's .entrySet() and may not necessarily
    // retrieve the entries in deterministic order.
    RBJsonObjectBuilder rbJsonObjectBuilder = rbJsonObjectBuilder();
    map.entrySet().stream().sorted(comparingByKey(comparator))
        .forEach(entry -> {
          K key = entry.getKey();
          V value = entry.getValue();
          rbJsonObjectBuilder.setJsonElement(keySerializer.apply(key), valueSerializer.apply(value));
        });
    return rbJsonObjectBuilder.build();
  }

  public static <E extends Enum<E> & JsonRoundTripStringConvertibleEnum<E>, V> JsonObject rbEnumMapToJsonObject(
      RBEnumMap<E, V> rbEnumMap,
      Function<V, JsonElement> valueSerializer) {
    JsonObject jsonObject = new JsonObject();
    rbEnumMap.forEachEntryInKeyOrder( (enumConstantKey, jsonElement) -> jsonObject.add(
        enumConstantKey.toUniqueStableString(), valueSerializer.apply(jsonElement)));
    return jsonObject;
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
    RBJsonObjectBuilder builder = rbJsonObjectBuilder();
    // The following code will insert items into the JSON object in ticker alphabetical order.
    // I'm not sure if there's a guarantee that they will also PRINT in that order,
    // but in practice that seems to be the case.
    iidMap.instrumentIdStream()
        .map(longId -> {
          InstrumentId instrumentId = instrumentId(longId.asLong());
          V value = iidMap.getOrThrow(instrumentId);
          return pair(
              jsonTickerMap.getJsonTickerOrThrow(instrumentId).getFreeFormString(),
              valueSerializer.apply(value));
        })
        .sorted(comparing(v -> v.getLeft()))
        .forEach(pair -> builder.setJsonElement(pair.getLeft(), pair.getRight()));
    return builder.build();
  }

  /**
   * Converts an iidMap to a JsonObject using a BiFunction that transforms each (InstrumentId, JsonElement) entry.
   */
  public static <V> JsonObject iidMapToJsonObject(
      IidMap<V> iidMap,
      JsonTickerMap jsonTickerMap,
      BiFunction<InstrumentId, V, JsonElement> valueSerializer) {
    // pass in a predicate that is always true; no filtering
    return iidMapToFilteredJsonObject(iidMap, jsonTickerMap, valueSerializer, v -> true);
  }

  /**
   * Converts an iidMap to a JsonObject using a BiFunction that transforms each (InstrumentId, JsonElement) entry.
   * Keeps only those elements that pass a supplied predicate.
   */
  public static <V> JsonObject iidMapToFilteredJsonObject(
      IidMap<V> iidMap,
      JsonTickerMap jsonTickerMap,
      BiFunction<InstrumentId, V, JsonElement> valueSerializer,
      Predicate<JsonElement> mustIncludeIf) {
    RBJsonObjectBuilder builder = rbJsonObjectBuilder();
    // The following code will insert items into the JSON object in ticker alphabetical order.
    // I'm not sure if there's a guarantee that they will also PRINT in that order,
    // but in practice that seems to be the case.
    iidMap.instrumentIdStream()
        .map(longId -> {
          InstrumentId instrumentId = instrumentId(longId.asLong());
          V value = iidMap.getOrThrow(instrumentId);
          return pair(
              jsonTickerMap.getJsonTickerOrThrow(instrumentId).getFreeFormString(),
              valueSerializer.apply(instrumentId, value));
        })
        .sorted(comparing(v -> v.getLeft()))
        .filter(pair -> mustIncludeIf.test(pair.getRight()))
        .forEach(pair -> builder.setJsonElement(pair.getLeft(), pair.getRight()));
    return builder.build();
  }

  public static <V extends HasUniqueId<V>> JsonObject rbSetOfHasUniqueIdToJsonObject(
      RBSetOfHasUniqueId<V> rbSetOfHasUniqueId,
      Function<V, JsonElement> valueSerializer) {
    return rbMapToJsonObject(
        rbSetOfHasUniqueId.getRawMap(),
        uniqueId -> uniqueId.getStringId(),
        valueSerializer);
  }

  public static <V> JsonObject streamToJsonObject(
      Stream<V> stream,
      Function<V, String> keySerializer,
      Function<V, JsonElement> itemSerializer) {
    RBJsonObjectBuilder builder = rbJsonObjectBuilder();
    stream.forEachOrdered(
        v -> builder.setJsonElement(
            keySerializer.apply(v),
            itemSerializer.apply(v)));
    return builder.build();
  }

  /**
   * Creates a copy of a JsonObject with some entries possibly removed.
   */
  public static JsonObject filterEntries(
      JsonObject jsonObject,
      BiPredicate<String, JsonElement> mustKeepEntryPredicate) {
    RBJsonObjectBuilder builder = rbJsonObjectBuilder();
    jsonObject.entrySet().forEach(entry -> {
          String key = entry.getKey();
          JsonElement value = entry.getValue();
          if (mustKeepEntryPredicate.test(key, value)) {
            builder.setJsonElement(key, value);
          }
        });
    return builder.build();
  }

  /**
   * Creates a copy of a JsonObject with some entries possibly removed, based only on the JSON property (not the value).
   */
  public static JsonObject filterKeys(
      JsonObject jsonObject,
      Predicate<String> mustKeepKeyPredicate) {
    return filterEntries(jsonObject, (key, ignoredValue) -> mustKeepKeyPredicate.test(key));
  }

  public static <V extends HasUniqueId<V>> JsonObject streamOfHasUniqueIdToJsonObject(
      Stream<V> stream,
      Function<V, JsonElement> itemSerializer) {
    RBJsonObjectBuilder builder = rbJsonObjectBuilder();
    stream.forEachOrdered(
        v -> builder.setJsonElement(
            v.getUniqueId().getStringId(),
            itemSerializer.apply(v)));
    return builder.build();
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

  public static <E extends Enum<E>, V> RBEnumMap<E, V> jsonObjectToRBEnumMap(
      JsonObject jsonObject,
      Class<E> enumClass,
      Function<String, E> keyDeserializer,
      Function<JsonElement, V> valueDeserializer) {
    return newRBEnumMap(
        enumClass,
        jsonObjectToRBMap(jsonObject, keyDeserializer, valueDeserializer));
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

  /**
   * Converts a {@link JsonObject} to an {@link RBMap} in the general case where the (transformed) map value
   * also relies on the original key. In theory, the transformed key could also rely on the original map's value.
   *
   * <p> This is for the most general case of conversion. </p>
   */
  public static <K, V> RBMap<K, V> jsonObjectToRBMap(
      JsonObject jsonObject,
      BiFunction<String, JsonElement, Pair<K, V>> entryDeserializer) {
    Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(entrySet.size());
    entrySet.forEach(entry -> {
      String stringKey = entry.getKey();
      JsonElement jsonElement = entry.getValue();
      Pair<K, V> rbMapEntryPair = entryDeserializer.apply(stringKey, jsonElement);
      K mapKey = rbMapEntryPair.getLeft();
      V value = rbMapEntryPair.getRight();
      mutableMap.putAssumingAbsent(mapKey, value);
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

  public static void forEachJsonObjectEntry(JsonObject jsonObject, BiConsumer<String, JsonElement> entryConsumer) {
    jsonObject.entrySet().forEach(entry -> {
      String property = entry.getKey();
      JsonElement value = entry.getValue();
      entryConsumer.accept(property, value);
    });
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
    JsonValidator.staticValidate(
        jsonObject,
        jsonValidationInstructionsForRange(
            UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
            UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR));
    return constructRange(
        transformOptional(getOptionalJsonElement(jsonObject, "min"), valueDeserializer), BoundType.CLOSED,
        transformOptional(getOptionalJsonElement(jsonObject, "max"), valueDeserializer), BoundType.CLOSED);
  }

  /**
   * Typically, {@link JsonValidationInstructions} will be associated with a JSON API converter verb class.
   * However, the {@link Range} gets converted using static methods and doesn't follow our usual paradigm.
   * Therefore, we'll expose these for any other JSON API converter that converts an object that's a simple
   * wrapper around a Range.
   */
  public static JsonValidationInstructions jsonValidationInstructionsForRange(
      JsonApiPropertyDescriptor jsonApiPropertyDescriptorForMin,
      JsonApiPropertyDescriptor jsonApiPropertyDescriptorForMax) {
    return jsonValidationInstructionsBuilder()
        .hasNoRequiredProperties()
        .setOptionalProperties(rbMapOf(
            "min", jsonApiPropertyDescriptorForMin,
            "max", jsonApiPropertyDescriptorForMax))
        .build();
  }

  public static <C extends Comparable<? super C>> ClosedRange<C> jsonObjectToClosedRange(
      JsonObject jsonObject,
      Function<JsonElement, C> valueDeserializer) {
    JsonValidator.staticValidate(jsonObject, jsonValidationInstructionsBuilder()
        .setRequiredProperties(rbMapOf(
            "min", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
            "max", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR))
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
