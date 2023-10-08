package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.types.LongCounter;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.emptyImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.immutableIndexableArray1D;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.types.LongCounter.longCounter;

public class ImmutableIndexableArrays1D {

  /**
   * Creates an ImmutableIndexableArray1D by merging several individual such arrays.
   * When a key only appears once in the entire set of arrays passed (first, second, rest), then we just copy the value.
   * When it appears N times (N &ge; 2), we apply 'mergeFunction' (ideally a commutative one, otherwise the behavior
   * would be weird and would depend on ordering) N - 1 times, and put the result as the value of that key.
   *
   * The ordering of the keys in the final array is this:
   * - all keys from the first, in order
   * - all keys from the 2nd (that aren't already in the first), in order
   * - ... etc.
   *
   * @see RBMapMergers#mergeRBMapsByValue
   */
  @SafeVarargs
  public static <K, V> ImmutableIndexableArray1D<K, V> mergeImmutableIndexableArrays1DByValue(
      IntFunction<V[]> arrayInstantiator, // we need this unfortunately, because arrays don't play well with generics
      BinaryOperator<V> mergeFunction,
      ImmutableIndexableArray1D<K, V> first,
      ImmutableIndexableArray1D<K, V> second,
      ImmutableIndexableArray1D<K, V> ... rest) {
    return mergeImmutableIndexableArrays1DByValue(
        arrayInstantiator,
        mergeFunction,
        RBLists.concatenateFirstSecondAndRest(first, second, rest));
  }

  public static <K, V> ImmutableIndexableArray1D<K, V> mergeImmutableIndexableArrays1DByValue(
      IntFunction<V[]> arrayInstantiator, // we need this unfortunately, because arrays don't play well with generics
      BinaryOperator<V> mergeFunction,
      List<ImmutableIndexableArray1D<K, V>> arraysToMerge) {
    int numArraysToMerge = arraysToMerge.size();
    RBPreconditions.checkArgument(
        numArraysToMerge > 0,
        "We can't merge 0 arrays");
    // Special case, for better performance
    if (numArraysToMerge == 1) {
      return getOnlyElement(arraysToMerge);
    }

    int sizeHint = arraysToMerge.stream().mapToInt(v -> v.size()).sum();
    MutableRBMap<K, Integer> keysEncountered = newMutableRBMapWithExpectedSize(sizeHint);
    LongCounter currentPosition = longCounter();
    List<K> keysInOrder = newArrayListWithExpectedSize(sizeHint);
    List<V> valuesInOrder = newArrayListWithExpectedSize(sizeHint);
    arraysToMerge
        .forEach(array -> array.forEachEntry( (key, value) -> {
          Optional<Integer> keyPosition = keysEncountered.getOptional(key);
          if (!keyPosition.isPresent()) {
            keysEncountered.putAssumingAbsent(key, currentPosition.getAsIntOrThrow());
            keysInOrder.add(key);
            valuesInOrder.add(value);
            currentPosition.increment();
          } else {
            int position = keyPosition.get();
            keysInOrder.set(position, key);
            valuesInOrder.set(position, mergeFunction.apply(valuesInOrder.get(position), value));
          }
        }));

    V[] rawArray = arrayInstantiator.apply(keysInOrder.size());
    valuesInOrder.toArray(rawArray);
    return keysInOrder.isEmpty()
        ? emptyImmutableIndexableArray1D(rawArray)
        : immutableIndexableArray1D(simpleArrayIndexMapping(keysInOrder), rawArray);
  }

  /**
   * This will also throw if there is more than 1 item with the same key.
   *
   * @see RBMapConstructors#rbMapFromStream
   */
  public static <K, V> ImmutableIndexableArray1D<K, V> immutableIndexableArray1DFromStream(
      IntFunction<V[]> arrayInstantiator, // we need this unfortunately, because arrays don't play well with generics
      Stream<V> stream,
      Function<V, K> keyExtractor) {
    List<K> keysInOrder = newArrayListWithExpectedSize(100);
    List<V> valuesInOrder = newArrayListWithExpectedSize(100);
    stream.forEach(value -> {
      keysInOrder.add(keyExtractor.apply(value));
      valuesInOrder.add(value);
    });
    V[] rawArray = arrayInstantiator.apply(keysInOrder.size());
    valuesInOrder.toArray(rawArray);
    return keysInOrder.isEmpty()
        ? emptyImmutableIndexableArray1D(rawArray)
        : immutableIndexableArray1D(simpleArrayIndexMapping(keysInOrder), rawArray);
  }

  public static <K, V> RBMap<K, V> immutableIndexableArray1DToRBMap(
      ImmutableIndexableArray1D<K, V> immutableIndexableArray1D) {
    MutableRBMap<K, V> mutableMap = newMutableRBMapWithExpectedSize(immutableIndexableArray1D.size());
    immutableIndexableArray1D.forEachEntry( (key, value) ->
        mutableMap.putAssumingAbsent(key, value));
    return newRBMap(mutableMap);
  }

  /**
   * Converts an {@link RBMap} into an {@link ImmutableIndexableArray1D}.
   *
   * <p> Unlike an {@link RBMap}, ordering matters in an indexable array. Therefore,
   * a comparator is passed to help us decide in which order the entries will appear. </p>
   */
  public static <K, V> ImmutableIndexableArray1D<K, V> rbMapToImmutableIndexableArray1D(
      RBMap<K, V> rbMap,
      Comparator<Map.Entry<K, V>> entryComparator) {
    K[] keys   = (K[]) new Object[rbMap.size()];
    V[] values = (V[]) new Object[rbMap.size()];

    int i = 0;
    Iterator<Map.Entry<K, V>> iter = rbMap.entrySet()
        .stream()
        .sorted(entryComparator)
        .iterator();

    while (iter.hasNext()) {
      Map.Entry<K, V> entry = iter.next();
      keys[i] = entry.getKey();
      values[i] = entry.getValue();
      i++;
    }

    return immutableIndexableArray1D(
        simpleArrayIndexMapping(keys),
        values);
  }

  public static <V> IidMap<V> immutableIndexableArray1DToIidMap(
      ImmutableIndexableArray1D<InstrumentId, V> immutableIndexableArray1D) {
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(immutableIndexableArray1D.size());
    immutableIndexableArray1D.forEachEntry( (instrumentId, value) ->
        mutableMap.putAssumingAbsent(instrumentId, value));
    return newIidMap(mutableMap);
  }

  /**
   * Converts an {@link IidMap} into an {@link ImmutableIndexableArray1D}.
   *
   * <p> Unlike an {@link IidMap}, ordering matters in an indexable array. Therefore,
   * a comparator is passed to help us decide in which order the entries will appear. </p>
   */
  public static <V> ImmutableIndexableArray1D<InstrumentId, V> iidMapToImmutableIndexableArray1D(
      IidMap<V> iidMap,
      Comparator<Pair<InstrumentId, V>> entryComparator) {
    InstrumentId[] instrumentIds = new InstrumentId[iidMap.size()];
    V[] values = (V[]) new Object[iidMap.size()];

    int i = 0;
    Iterator<Pair<InstrumentId, V>> iter = iidMap.instrumentIdStream()
        .map(v -> pair(v, iidMap.getOrThrow(v)))
        // I can't find a succinct way to do this using comparing(), but this slightly more verbose version will do.
        .sorted(entryComparator)
        .iterator();

    while (iter.hasNext()) {
      Pair<InstrumentId, V> pair = iter.next();
      instrumentIds[i] = pair.getLeft();
      values[i] = pair.getRight();
      i++;
    }

    return immutableIndexableArray1D(
        simpleArrayIndexMapping(instrumentIds),
        values);
  }

}
