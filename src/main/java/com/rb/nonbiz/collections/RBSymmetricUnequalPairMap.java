package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.RBIterables.forEachUnequalPairInList;
import static com.rb.nonbiz.collections.RBIterables.forEachUniquePair;
import static com.rb.nonbiz.collections.RBOptionals.getIntOrThrow;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;

/**
 * We sometimes want to record information for every pair formed by N items, *when the same item cannot be on the same
 * side* (hence the 'Unequal' in the name), and the order of the items doesn't matter.
 * For example, if we have 4 points A, B, C, D, we may want to record the magnitude of the line segments
 * AB, AC, AD, BC, BD, CD - i.e. every pair of points.
 *
 * This is a space- and time-efficient way of storing this information.
 *
 * There is no explicit constructor. To create this object use either RBSymmetricUnequalPairMapStaticBuilder
 * (if you know ahead of time what the keys will be), or RBSymmetricUnequalPairMapDynamicBuilder (if you don't).
 */
public class RBSymmetricUnequalPairMap<K, V> {

  private final ArrayIndexMapping<K> arrayIndexMapping;
  private final V[] rawFlatArray;

  private RBSymmetricUnequalPairMap(ArrayIndexMapping<K> arrayIndexMapping, V[] rawFlatArray) {
    this.arrayIndexMapping = arrayIndexMapping;
    this.rawFlatArray = rawFlatArray;
  }

  public Optional<V> getOptional(K key1, K key2) {
    OptionalInt flatIndex = getOptionalFlatIndex(key1, key2);
    return flatIndex.isPresent()
        ? Optional.of(rawFlatArray[flatIndex.getAsInt()])
        : Optional.empty();
  }

  public V getOrThrow(K key1, K key2) {
    return RBOptionals.getOrThrow(getOptional(key1, key2), "Cannot find value for [ %s , %s ]", key1, key2);
  }

  public OptionalInt getOptionalFlatIndex(K key1, K key2) {
    return getOptionalFlatIndex(arrayIndexMapping, key1, key2);
  }

  public boolean containsKey(K key) {
    return arrayIndexMapping.getOptionalIndex(key).isPresent();
  }

  public List<V> values() {
    return Arrays.asList(rawFlatArray);
  }

  /**
   * E.g. if key is B, and all keys are A, B, C, D,
   * this returns the values for BA, BC, BD.
   */
  public List<V> valuesForPairsWithKey(K key) {
    int index = getIntOrThrow(arrayIndexMapping.getOptionalIndex(key), "Cannot find mapping for key %s", key);
    return IntStream.range(0, arrayIndexMapping.size())
        .filter(otherKeyIndex -> otherKeyIndex != index)
        .mapToObj(otherKeyIndex -> getOrThrow(key, arrayIndexMapping.getKey(otherKeyIndex)))
        .collect(Collectors.toList());
  }

  /**
   * E.g. if keys are {A, B}, and all keys are A, B, C, D,
   * this returns the values for AB, AC, AD, BC, BD, i.e. all pairs where at least one of the keys appears.
   */
  public List<V> valuesForPairsWithKeys(List<K> keys) {
    List<V> values = newArrayListWithExpectedSize(keys.size() * (keys.size() - 1) / 2);
    forEachUnequalPairInList(keys, (key1, key2) -> values.add(getOrThrow(key1, key2)));
    return values;
  }

  @VisibleForTesting // do not use this; it's here to help the test matcher
  ArrayIndexMapping<K> getArrayIndexMapping() {
    return arrayIndexMapping;
  }

  @VisibleForTesting // do not use this; it's here to help the test matcher
  V[] getRawFlatArray() {
    return rawFlatArray;
  }

  private static <K> OptionalInt getOptionalFlatIndex(ArrayIndexMapping<K> arrayIndexMapping, K key1, K key2) {
    RBPreconditions.checkArgument(
        !key1.equals(key2),
        "We explicitly disallow the same item on both sides of a pair: %s",
        key1);

    // I.e. if we had been storing things in a square matrix (instead of a triangle), what would the matrix indices be?
    OptionalInt index1 = arrayIndexMapping.getOptionalIndex(key1);
    if (!index1.isPresent()) {
      return OptionalInt.empty();
    }
    OptionalInt index2 = arrayIndexMapping.getOptionalIndex(key2);
    if (!index2.isPresent()) {
      return OptionalInt.empty();
    }

    // We store the data as a lower triangular matrix, with the diagonal missing,
    // flattened to a single 1-d array.
    // Below, the number corresponds to the index of each matrix element in a flat array.
    //    A   B   C   D   E   F   G
    // A
    // B  0
    // C  1   2
    // D  3   4   5
    // E  6   7   8   9
    // F 10  11  12  13  14
    // G 15  16  17  18  19  20
    //
    // Let's take the {B, F} pair.
    // The array index mapping maps { A, B, C, D, E, F, G } to { 0, 1, 2, 3, 4, 5, 6 }.
    //
    // rowIndexInSquare = 5 (for F)
    // columnIndexInSquare = 1 (for B)
    // rowIndex = 4
    // (rowIndex * (rowIndex + 1)) / 2 = 10. This takes us to the beginning of the row of the triangular matrix
    // where the pair {B, F} will be found.
    // Then we add 1 (columnIndexInSquare) to get to 11, which is the index in the flat where the pair {B, F} is stored.
    int rowIndexInSquare    = Math.max(index1.getAsInt(), index2.getAsInt());
    int columnIndexInSquare = Math.min(index1.getAsInt(), index2.getAsInt());

    int rowIndex = rowIndexInSquare - 1;
    int columnIndex = columnIndexInSquare;

    int flatIndex = (rowIndex * (rowIndex + 1)) / 2 + columnIndex;
    return OptionalInt.of(flatIndex);
  }


  /**
   * Use this builder when you don't know ahead of time what the keys will be.
   * Hence the word 'dynamic' in the name.
   * If you do know what the keys will be, use RBSymmetricUnequalPairMapStaticBuilder.
   */
  public static class RBSymmetricUnequalPairMapDynamicBuilder<K, V> implements RBBuilder<RBSymmetricUnequalPairMap<K, V>> {

    private final MutableRBMap<K, MutableRBMap<K, V>> rawMapOfMaps;
    private final IntFunction<V[]> rawArrayInstantiator;

    private RBSymmetricUnequalPairMapDynamicBuilder(IntFunction<V[]> rawArrayInstantiator) {
      this.rawMapOfMaps = newMutableRBMap();
      this.rawArrayInstantiator = rawArrayInstantiator;
    }

    public static <K, V> RBSymmetricUnequalPairMapDynamicBuilder<K, V> rbSymmetricUnequalPairMapDynamicBuilder(
        IntFunction<V[]> rawArrayInstantiator) {
      return new RBSymmetricUnequalPairMapDynamicBuilder<>(rawArrayInstantiator);
    }

    public RBSymmetricUnequalPairMapDynamicBuilder<K, V> putAssumingAbsent(K key1, K key2, V value) {
      rawMapOfMaps.putIfAbsent(key1, () -> newMutableRBMap());
      rawMapOfMaps.putIfAbsent(key2, () -> newMutableRBMap());

      // Because we want to keep the code general and not have the key type K extend Comparable,
      // it is simpler to insert both combinations until we build. If K extended Comparable, we would be able to
      // determine which key is smaller in the comparison, and only pick one of the two combinations
      // deterministically.
      // This just means that the construction (using this builder) will be slightly less efficient.
      // However, the resulting RBSymmetricUnequalPairMap that will get built by build() will not waste space.
      rawMapOfMaps.getOrThrow(key1).putAssumingAbsent(key2, value);
      rawMapOfMaps.getOrThrow(key2).putAssumingAbsent(key1, value);

      return this;
    }

    @Override
    public void sanityCheckContents() {
      // all pairs must exist
      RBSet<K> topLevelKeys = newRBSet(rawMapOfMaps.keySet());
      topLevelKeys.forEach(topLevelKey ->
          RBSimilarityPreconditions.checkBothSame(
              newRBSet(rawMapOfMaps.getOrThrow(topLevelKey).keySet()),
              RBSets.difference(topLevelKeys, singletonRBSet(topLevelKey)),
              "Each sub-map must have items for all keys except the top-level key that points to it"));
    }

    @Override
    public RBSymmetricUnequalPairMap<K, V> buildWithoutPreconditions() {
      ArrayIndexMapping<K> arrayIndexMapping = simpleArrayIndexMapping(newRBSet(rawMapOfMaps.keySet()).toSortedList());
      int numKeys = arrayIndexMapping.size();
      int size = numKeys * (numKeys - 1) / 2;
      V[] rawFlatArray = rawArrayInstantiator.apply(size);
      forEachUniquePair(rawMapOfMaps.keySet(), (key1, key2) -> {
        int rawIndex = getOptionalFlatIndex(arrayIndexMapping, key1, key2).getAsInt();
        rawFlatArray[rawIndex] = rawMapOfMaps.getOrThrow(key1).getOrThrow(key2);
      });
      return new RBSymmetricUnequalPairMap<>(arrayIndexMapping, rawFlatArray);
    }

  }

  /**
   * Use this builder when you know ahead of time what the keys will be.
   * Hence the word 'static' in the name.
   * If you don't know what the keys will be, use RBSymmetricUnequalPairMapDynamicBuilder.
   */
  public static class RBSymmetricUnequalPairMapStaticBuilder<K, V> implements RBBuilder<RBSymmetricUnequalPairMap<K, V>> {

    private final ArrayIndexMapping<K> arrayIndexMapping;
    private final V[] rawFlatArray;

    private RBSymmetricUnequalPairMapStaticBuilder(ArrayIndexMapping<K> arrayIndexMapping, V[] rawFlatArray) {
      this.arrayIndexMapping = arrayIndexMapping;
      this.rawFlatArray = rawFlatArray;
    }

    public static <K, V> RBSymmetricUnequalPairMapStaticBuilder<K, V> rbSymmetricUnequalPairMapStaticBuilder(
        List<K> keys, IntFunction<V[]> rawArrayInstantiator) {
      RBPreconditions.checkArgument(
          keys.size() > 1,
          "We must have 2 or more keys in order to store pairs of those keys; we had %s",
          keys);
      int rawFlatArraySize = keys.size() * (keys.size() - 1) / 2;
      return new RBSymmetricUnequalPairMapStaticBuilder<>(
          simpleArrayIndexMapping(keys), rawArrayInstantiator.apply(rawFlatArraySize));
    }

    public RBSymmetricUnequalPairMapStaticBuilder<K, V> putAssumingAbsent(K key1, K key2, V value) {
      OptionalInt flatIndex = RBSymmetricUnequalPairMap.getOptionalFlatIndex(arrayIndexMapping, key1, key2);
      RBPreconditions.checkArgument(
          flatIndex.isPresent(),
          "Cannot add entry for invalid key combination ( %s , %s ); value was %s",
          key1, key2, value);
      V existingValue = rawFlatArray[flatIndex.getAsInt()];
      RBPreconditions.checkArgument(
          existingValue == null,
          "Trying to set ( %s , %s ) = %s but there is an existing value of %s",
          key1, key2, value, existingValue);
      rawFlatArray[flatIndex.getAsInt()] = value;
      return this;
    }

    @Override
    public void sanityCheckContents() {
      Arrays.stream(rawFlatArray).forEach(item -> RBPreconditions.checkNotNull(item));
    }

    @Override
    public RBSymmetricUnequalPairMap<K, V> buildWithoutPreconditions() {
      return new RBSymmetricUnequalPairMap<>(arrayIndexMapping, rawFlatArray);
    }

  }


}
