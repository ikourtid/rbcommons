package com.rb.nonbiz.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.MutableRBSet;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.RBStreams;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromStream;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSetWithExpectedSize;
import static com.rb.nonbiz.collections.RBOptionals.filterPresentOptionals;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonDoubleRoundedTo6Digits;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBGson.jsonLong;
import static com.rb.nonbiz.json.RBGson.jsonString;

/**
 * Utility methods for {@link JsonArray}s.
 */
public class RBJsonArrays {

  /**
   * Create an empty {@link JsonArray}.
   */
  public static JsonArray emptyJsonArray() {
    return new JsonArray();
  }

  public static JsonArray newJsonArrayWithExpectedSize(int size) {
    return new JsonArray(size);
  }

  public static JsonArray jsonArray(int size, Stream<? extends JsonElement> items) {
    JsonArray jsonArray = new JsonArray(size);
    items.forEach(item -> jsonArray.add(item));
    return jsonArray;
  }

  public static JsonArray jsonArray(Collection<? extends JsonElement> items) {
    JsonArray jsonArray = new JsonArray(items.size());
    items.forEach(item -> jsonArray.add(item));
    return jsonArray;
  }

  /**
   * Create a single-item array.
   */
  public static JsonArray singletonJsonArray(JsonElement item) {
    JsonArray jsonArray = new JsonArray(1);
    jsonArray.add(item);
    return jsonArray;
  }

  public static JsonArray jsonArray(JsonElement first, JsonElement second, JsonElement...rest) {
    JsonArray jsonArray = new JsonArray(rest.length + 2);
    jsonArray.add(first);
    jsonArray.add(second);
    for (JsonElement element : rest) {
      jsonArray.add(element);
    }
    return jsonArray;
  }

  public static JsonArray singletonJsonStringArray(String item) {
    JsonArray jsonArray = new JsonArray(1);
    jsonArray.add(item);
    return jsonArray;
  }

  public static JsonArray jsonStringArray(String first, String second, String...rest) {
    return jsonStringArray(2 + rest.length, RBStreams.concatenateFirstSecondAndRest(first, second, rest));
  }

  public static JsonArray jsonStringArray(int size, Stream<String> items) {
    JsonArray jsonArray = new JsonArray(size);
    items.forEach(item -> jsonArray.add(item));
    return jsonArray;
  }

  public static JsonArray singletonJsonBooleanArray(boolean item) {
    JsonArray jsonArray = new JsonArray(1);
    jsonArray.add(item);
    return jsonArray;
  }

  public static JsonArray jsonBooleanArray(boolean first, boolean second, boolean...rest) {
    JsonArray jsonArray = new JsonArray(2 + rest.length);
    jsonArray.add(first);
    jsonArray.add(second);
    for (Boolean item : rest) {
      jsonArray.add(item);
    }
    return jsonArray;
  }

  public static JsonArray singletonJsonIntegerArray(int item) {
    JsonArray jsonArray = new JsonArray(1);
    jsonArray.add(jsonInteger(item));
    return jsonArray;
  }

  public static JsonArray jsonIntegerArray(int first, int second, int... rest) {
    JsonArray jsonArray = new JsonArray(2 + rest.length);
    jsonArray.add(jsonInteger(first));
    jsonArray.add(jsonInteger(second));
    for (int value : rest) {
      jsonArray.add(jsonInteger(value));
    }
    return jsonArray;
  }

  public static JsonArray singletonJsonLongArray(long item) {
    JsonArray jsonArray = new JsonArray(1);
    jsonArray.add(jsonLong(item));
    return jsonArray;
  }

  public static JsonArray jsonLongArray(long first, long second, long... rest) {
    JsonArray jsonArray = new JsonArray(2 + rest.length);
    jsonArray.add(jsonLong(first));
    jsonArray.add(jsonLong(second));
    for (long value : rest) {
      jsonArray.add(jsonLong(value));
    }
    return jsonArray;
  }

  public static JsonArray singletonJsonElementArray(JsonElement item) {
    JsonArray jsonArray = new JsonArray(1);
    jsonArray.add(item);
    return jsonArray;
  }

  public static JsonArray jsonElementArray(JsonElement first, JsonElement second, JsonElement...rest) {
    JsonArray jsonArray = new JsonArray(2 + rest.length);
    jsonArray.add(first);
    jsonArray.add(second);
    for (JsonElement item : rest) {
      jsonArray.add(item);
    }
    return jsonArray;
  }

  public static JsonArray singletonJsonDoubleArray(double item) {
    JsonArray jsonArray = new JsonArray(1);
    jsonArray.add(item);
    return jsonArray;
  }

  public static JsonArray jsonDoubleArray(double first, double second, double ... rest) {
    JsonArray jsonArray = new JsonArray(2 + rest.length);
    jsonArray.add(jsonDouble(first));
    jsonArray.add(jsonDouble(second));
    for (double value : rest) {
      jsonArray.add(jsonDouble(value));
    }
    return jsonArray;
  }

  public static <T> JsonArray listToJsonArray(List<T> list, Function<T, JsonElement> itemSerializer) {
    return iteratorToJsonArray(list.size(), list.iterator(), itemSerializer);
  }

  /**
   * Convert a List of Optionals into a JsonArray consisting only of the list elements that are not Optional.empty().
   */
  public static <T> JsonArray listOfOptionalsToJsonArray(
      List<Optional<T>> list,
      Function<T, JsonElement> itemSerializer) {
    return listToJsonArray(filterPresentOptionals(list), itemSerializer);
  }

  public static <T> JsonArray streamToJsonArray(int size, Stream<T> stream, Function<T, JsonElement> itemSerializer) {
    return iteratorToJsonArray(size, stream.iterator(), itemSerializer);
  }

  public static <T> JsonArray iteratorToJsonArray(int size, Iterator<T> iter, Function<T, JsonElement> itemSerializer) {
    JsonArray jsonArray = new JsonArray(size);
    iter.forEachRemaining(item -> jsonArray.add(itemSerializer.apply(item)));
    return jsonArray;
  }

  public static <T> Stream<T> jsonArrayToStream(JsonArray jsonArray, Function<JsonElement, T> itemDeserializer) {
    return IntStream.range(0, jsonArray.size())
        .mapToObj(i -> itemDeserializer.apply(jsonArray.get(i)));
  }

  public static <T> IidMap<T> jsonArrayOfObjectsToIidMap(
      JsonArray jsonArray,
      Function<JsonObject, InstrumentId> instrumentIdExtractor,
      Function<JsonObject, T> itemDeserializer) {
    return iidMapFromStream(
        IntStream.range(0, jsonArray.size())
            .mapToObj(i -> jsonArray.get(i).getAsJsonObject()),
        instrumentIdExtractor,
        itemDeserializer);
  }

  public static <T> List<T> jsonArrayToList(JsonArray jsonArray, Function<JsonElement, T> itemDeserializer) {
    List<T> list = newArrayListWithExpectedSize(jsonArray.size());
    jsonArray.forEach(jsonElement -> list.add(itemDeserializer.apply(jsonElement)));
    return list;
  }

  /**
   * Converts a JsonArray to a raw array of type double[].
   */
  public static double[] jsonArrayToDoubleArray(JsonArray jsonArray, Function<JsonElement, Double> itemDeserializer) {
    return jsonArrayToList(jsonArray, itemDeserializer)
        .stream()
        .mapToDouble(d -> d) // a DoubleStream, e.g. a stream of primitive doubles
        .toArray();          // a raw array of type double[]
  }

  public static <T> RBSet<T> jsonArrayToRBSet(JsonArray jsonArray, Function<JsonElement, T> itemDeserializer) {
    MutableRBSet<T> mutableSet = newMutableRBSetWithExpectedSize(jsonArray.size());
    jsonArray.forEach(jsonElement -> mutableSet.addAssumingAbsent(itemDeserializer.apply(jsonElement)));
    return newRBSet(mutableSet);
  }

  public static <V> JsonArray rbSetToJsonArray(RBSet<V> rbSet, Function<V, JsonElement> serializer) {
    return jsonArray(
        rbSet.size(),
        rbSet.stream().map(serializer));
  }

  public static <V> JsonArray rbSetToJsonArray(
      RBSet<V> rbSet,
      Comparator<V> comparator,
      Function<V, JsonElement> serializer) {
    return streamToJsonArray(
        rbSet.size(),
        rbSet.stream().sorted(comparator),
        serializer);
  }

  public static <K, V> JsonArray rbMapToJsonArray(
      RBMap<K, V> rbMap,
      Comparator<Entry<K, V>> comparator,
      BiFunction<K, V, JsonElement> serializer) {
    return streamToJsonArray(
        rbMap.size(),
        rbMap.entrySet()
            .stream()
            .sorted(comparator),
        entry -> serializer.apply(entry.getKey(), entry.getValue()));
  }

  public static void ifHasJsonArrayProperty(
      JsonObject jsonObject,
      String property,
      Consumer<JsonArray> consumer) {
    if (jsonObject.has(property)) {
      consumer.accept(jsonObject.get(property).getAsJsonArray());
    }
  }

  /**
   * If the items in the supplied list are all numbers (double, BigDecimal, or even PreciseValue / ImpreciseValue),
   * returns empty optional.
   * Otherwise, returns the items as a JsonArray. If the items are numbers, these get converted to jsonDouble
   * with 6 digits of accuracy.
   */
  public static <T> Optional<JsonArray> toJsonArrayIfNotAllZeros(List<T> list) {
    Class<?> sharedClass = RBSimilarityPreconditions.checkAllSame(list, v -> v.getClass());
    // We probably should avoid converting strings for this particular scenario, b/c the 3d grid will show numeric
    // values at each point, and strings don't make sense.
    return
        Number        .class.isAssignableFrom(sharedClass) ? toJsonArrayIfNotAllZeros(list, v -> ((Number)         v).doubleValue()) :
        PreciseValue  .class.isAssignableFrom(sharedClass) ? toJsonArrayIfNotAllZeros(list, v -> ((PreciseValue)   v).doubleValue()) :
        ImpreciseValue.class.isAssignableFrom(sharedClass) ? toJsonArrayIfNotAllZeros(list, v -> ((ImpreciseValue) v).doubleValue()) :
        Optional.of(jsonArray(list.size(), list.stream().map(v -> jsonString(v.toString()))));
  }

  private static <T> Optional<JsonArray> toJsonArrayIfNotAllZeros(List<T> list, ToDoubleFunction<T> converter) {
    List<Double> numericValues = list.stream()
        .map(v -> converter.applyAsDouble(v))
        .collect(Collectors.toList());
    return numericValues.stream().allMatch(v -> Math.abs(v) < 1e-8)
           ? Optional.empty()
           : Optional.of(jsonArray(
               numericValues.size(),
               numericValues.stream().map(v -> jsonDoubleRoundedTo6Digits(v))));
  }

}
