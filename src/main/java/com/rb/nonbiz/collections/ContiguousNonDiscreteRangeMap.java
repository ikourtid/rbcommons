package com.rb.nonbiz.collections;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBOrderingPreconditions;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.text.SmartFormatter.smartFormat;
import static java.util.Collections.singletonList;

/**
 * This is similar to a {@link RangeMap}, except it is guaranteed (via preconditions at assertion time)
 * to be non-disjoint/contiguous.
 *
 * <p> It has 'non-discrete' in its name to clarify that it should be used for cases where there's no clearly
 * defined notion of a next item. So it essentially represents a bunch of ranges like
 * [k1, k2) , [k2, k3), [k3, k4), ... </p>
 *
 * <p> E.g. doubles is a good example.
 * Integers, LocalDates etc are NOT. </p>
 *
 * <p> A better word for this is 'continuous', but ContiguousContinuousRangeMap sounds very confusing. </p>
 */
// RangeMap is officially marked @Beta / unstable, but it sounds like this is an oversight. So it's safe to use.
// https://github.com/google/guava/issues/3376
// However, we'll add this to prevent warnings / 'yellowness'.
@SuppressWarnings("UnstableApiUsage")
public class ContiguousNonDiscreteRangeMap<K extends Comparable<? super K>, V> {

  private final RangeMap<K, V> rawRangeMap;

  private ContiguousNonDiscreteRangeMap(RangeMap<K, V> rawRangeMap) {
    this.rawRangeMap = rawRangeMap;
  }

  /**
   * Represents e.g.
   * [1.1, 3.3) {@code ->} "a"
   * [3.3, +inf) {@code ->} "b"
   */
  public static <K extends Comparable<? super K>, V> ContiguousNonDiscreteRangeMap<K, V> contiguousNonDiscreteRangeMapWithNoEnd(
      List<K> rangeStartingPoints,
      List<V> values) {
    sanityCheckCommon(rangeStartingPoints, values);
    RangeMap<K, V> rawRangeMap = TreeRangeMap.create();
    for (int i = 0; i < values.size(); i++) {
      boolean isLast = (i == values.size() - 1);
      Range<K> range = isLast
          ? Range.<K>atLeast(rangeStartingPoints.get(i))
          : Range.closedOpen(rangeStartingPoints.get(i), rangeStartingPoints.get(i + 1));
      rawRangeMap.put(range, values.get(i));
    }
    return new ContiguousNonDiscreteRangeMap<K, V>(rawRangeMap);
  }

  /**
   * Represents e.g.
   * [1.1, +inf) {@code ->} "a"
   */
  public static <K extends Comparable<? super K>, V> ContiguousNonDiscreteRangeMap<K, V> singletonContiguousNonDiscreteRangeMapWithNoEnd(
      K rangeStartingPoint, V value) {
    return contiguousNonDiscreteRangeMapWithNoEnd(
        singletonList(rangeStartingPoint),
        singletonList(value));
  }

  /**
   * Represents e.g.
   * [1.1, 3.3) {@code ->} "a"
   * [3.3, 7.7) {@code ->} "b"
   */
  public static <K extends Comparable<? super K>, V> ContiguousNonDiscreteRangeMap<K, V> contiguousNonDiscreteRangeMapWithEnd(
      List<K> rangeStartingPoints,
      List<V> values,
      K firstInvalidPointAfterRanges) {
    sanityCheckCommon(rangeStartingPoints, values);
    RBPreconditions.checkArgument(
        rangeStartingPoints.get(rangeStartingPoints.size() - 1).compareTo(firstInvalidPointAfterRanges) < 0,
        "First invalid point after ranges is %s but it is not strictly larger than the last starting point %s",
        firstInvalidPointAfterRanges, rangeStartingPoints.get(rangeStartingPoints.size() - 1));
    RangeMap<K, V> rawRangeMap = TreeRangeMap.create();
    for (int i = 0; i < values.size(); i++) {
      boolean isLast = (i == values.size() - 1);
      Range<K> range = Range.closedOpen(
          rangeStartingPoints.get(i),
          isLast ? firstInvalidPointAfterRanges : rangeStartingPoints.get(i + 1));
      rawRangeMap.put(range, values.get(i));
    }
    return new ContiguousNonDiscreteRangeMap<K, V>(rawRangeMap);
  }

  /**
   * Represents e.g.
   * [1.1, 3.3) {@code ->} "a"
   */
  public static <K extends Comparable<? super K>, V> ContiguousNonDiscreteRangeMap<K, V> singletonContiguousNonDiscreteRangeMapWithEnd(
      K rangeStartingPoint,
      V value,
      K firstInvalidPointAfterRange) {
    return contiguousNonDiscreteRangeMapWithEnd(
        singletonList(rangeStartingPoint),
        singletonList(value),
        firstInvalidPointAfterRange);
  }

  private static <K extends Comparable<? super K>, V> void sanityCheckCommon(List<K> rangeStartingPoints, List<V> values) {
    RBPreconditions.checkArgument(
        rangeStartingPoints.size() == values.size(),
        "You have %s ranges but %s items",
        rangeStartingPoints.size(), values.size());
    RBPreconditions.checkArgument(
        !values.isEmpty(),
        "You must have > 0 ranges and values");
    RBOrderingPreconditions.checkConsecutive(
        rangeStartingPoints,
        (item1, item2) -> item1.compareTo(item2) < 0,
        "Keys in rangemap (the starts of ranges) must be strictly increasing");
  }

  public Optional<V> getOptional(K key) {
    return Optional.ofNullable(rawRangeMap.get(key));
  }

  public V getOrThrow(K key) {
    Optional<V> value = getOptional(key);
    if (!value.isPresent()) {
      throw new IllegalArgumentException(smartFormat(
          "Cannot find a value for key %s ; it is outside the range", key));
    }
    return value.get();
  }

  public RangeMap<K, V> getRawRangeMap() {
    return rawRangeMap;
  }

  public boolean hasEnd() {
    return Iterables.getLast(rawRangeMap.asMapOfRanges().keySet()).hasUpperBound();
  }

  public Optional<K> getFirstInvalidPointAfterRange() {
    Range<K> lastRange = Iterables.getLast(rawRangeMap.asMapOfRanges().keySet());
    return lastRange.hasUpperBound()
        ? Optional.of(lastRange.upperEndpoint())
        : Optional.empty();
  }

  @Override
  public String toString() {
    return Strings.format("[CNDRM %s CNDRM]", rawRangeMap);
  }

}
