package com.rb.nonbiz.collections;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBOrderingPreconditions;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBRanges.rangeIsAtLeast;
import static com.rb.nonbiz.collections.RBRanges.rangeIsClosed;
import static java.util.Collections.singletonList;

/**
 * Strictly speaking this is 'not necessarily contiguous'. It could still be contiguous.
 *
 * I'm using {@code <? super K>} instead of {@code <K>} because there I mostly want to have K be LocalDate,
 * but LocalDate implements ChronoLocalDate which in turn implements {@code Comparable<ChronoLocalDate>},
 * so there's no other way to get this to work.
 */
public class NonContiguousRangeMap<K extends Comparable<? super K>, V> {

  private final RangeMap<K, V> rawRangeMap;

  private NonContiguousRangeMap(RangeMap<K, V> rawRangeMap) {
    this.rawRangeMap = rawRangeMap;
  }

  public static <K extends Comparable<? super K>, V> NonContiguousRangeMap<K, V> nonContiguousRangeMapWithEnd(
      List<Range<K>> closedRanges, List<V> values) {
    RBPreconditions.checkArgument(
        !closedRanges.isEmpty() && rangeIsClosed(Iterables.getLast(closedRanges)),
        "If you know this non-contiguous range has an end, the last range should be closed");
    return nonContiguousRangeMap(closedRanges, values);
  }

  public static <K extends Comparable<? super K>, V> NonContiguousRangeMap<K, V> nonContiguousRangeMapWithNoEnd(
      List<Range<K>> closedRanges, List<V> values) {
    RBPreconditions.checkArgument(
        !closedRanges.isEmpty() && rangeIsAtLeast(Iterables.getLast(closedRanges)),
        "If you know this non-contiguous range has an end, the last range should be 'at least'");
    return nonContiguousRangeMap(closedRanges, values);
  }

  public static <K extends Comparable<? super K>, V> NonContiguousRangeMap<K, V> nonContiguousRangeMap(
      List<Range<K>> ranges, List<V> values) {
    RBPreconditions.checkArgument(
        ranges.size() == values.size(),
        "You have %s ranges but %s items",
        ranges.size(), values.size());
    RBPreconditions.checkArgument(
        !ranges.isEmpty() && !values.isEmpty(),
        "You must have > 0 ranges and values");
    RBPreconditions.checkArgument(
        ranges.stream().limit(ranges.size() - 1).allMatch(closedRange -> rangeIsClosed(closedRange)),
        "All ranges (except possibly the last one) must be closed",
        ranges.size());
    Range<K> lastRange = Iterables.getLast(ranges);
    RBPreconditions.checkArgument(
        rangeIsClosed(lastRange) || rangeIsAtLeast(lastRange),
        "The final range %s must be either closed on both ends or 'at least X' (closed on bottom only)",
        lastRange);
    RBOrderingPreconditions.checkConsecutive(
        ranges,
        (range1, range2) -> range1.upperEndpoint().compareTo(range2.lowerEndpoint()) < 0,
        "Ranges must be strictly increasing");
    RangeMap<K, V> rawRangeMap = TreeRangeMap.create();
    RBIterables.forEachPair(ranges, values, (closedRange, value) -> rawRangeMap.put(closedRange, value));
    return new NonContiguousRangeMap<K, V>(rawRangeMap);
  }

  public static <K extends Comparable<? super K>, V> NonContiguousRangeMap<K, V> singletonNonContiguousRangeMapWithEnd(
      Range<K> closedRange, V value) {
    return nonContiguousRangeMapWithEnd(
        singletonList(closedRange),
        singletonList(value));
  }

  public static <K extends Comparable<? super K>, V> NonContiguousRangeMap<K, V> singletonNonContiguousRangeMapWithNoEnd(
      K startOfOpenInterval, V value) {
    RangeMap<K, V> rawRangeMap = TreeRangeMap.create();
    rawRangeMap.put(Range.atLeast(startOfOpenInterval), value);
    return new NonContiguousRangeMap<K, V>(rawRangeMap);
  }

  private static <K extends Comparable<? super K>, V> void sanityCheckCommon(List<Range<K>> closedRanges,
                                                                             List<V> values) {
    RBPreconditions.checkArgument(
        closedRanges.size() == values.size(),
        "You have %s ranges but %s items",
        closedRanges.size(), values.size());
    RBPreconditions.checkArgument(
        closedRanges.stream().allMatch(closedRange -> rangeIsClosed(closedRange)),
        "All %s ranges must all be closed",
        closedRanges.size());
    RBOrderingPreconditions.checkConsecutive(
        closedRanges,
        (range1, range2) -> range1.upperEndpoint().compareTo(range2.lowerEndpoint()) < 0,
        "Ranges must be strictly increasing");
  }

  public Optional<V> getOptional(K key) {
    return Optional.ofNullable(rawRangeMap.get(key));
  }

  public Optional<V> getOptionalWithHighestKeyBelow(K key) {
    for (Map.Entry<Range<K>, V> entry : rawRangeMap.asDescendingMapOfRanges().entrySet()) {
      Range<K> range = entry.getKey();
      V value = entry.getValue();
      if (range.contains(key)) {
        return Optional.of(value);
      } else if (range.hasUpperBound() && range.upperEndpoint().compareTo(key) < 0) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }

  public V getOrThrow(K key) {
    Optional<V> value = getOptional(key);
    if (!value.isPresent()) {
      throw new IllegalArgumentException(Strings.format(
          "Cannot find a value for key %s ; it is outside the range"));    }
    return value.get();
  }

  public RangeMap<K, V> getRawRangeMap() {
    return rawRangeMap;
  }

  @Override
  public String toString() {
    return Strings.format("[NCRM %s NCRM]", rawRangeMap);
  }

}
