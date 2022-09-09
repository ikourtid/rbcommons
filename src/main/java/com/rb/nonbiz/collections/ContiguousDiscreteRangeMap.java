package com.rb.nonbiz.collections;

import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBOrderingPreconditions;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.rb.nonbiz.collections.NonContiguousRangeMap.nonContiguousRangeMap;
import static java.util.Collections.singletonList;

/**
 * Represents a set of ranges of discrete values with no gaps or overlaps.  E.g.
 * <pre>
 * [monday, wednesday] {@code ->} 1.1
 * [thursday, friday]  {@code ->} 2.2
 * </pre>
 *
 * <p> This is similar to a Guava RangeMap, except it is guaranteed (via preconditions at assertion time)
 * to be non-disjoint/contiguous, i.e. to have neither holes nor overlaps. </p>
 *
 * <p> It has 'discrete' in the name because it works for ints, LocalDates, etc. where there's a clear notion of a
 * next item. This is not the case with doubles, for instance. </p>
 *
 * @see NonContiguousRangeMap
 */
public class ContiguousDiscreteRangeMap<K extends Comparable<? super K>, V> {

  private final NonContiguousRangeMap<K, V> underlyingMap;

  private ContiguousDiscreteRangeMap(NonContiguousRangeMap<K, V> underlyingMap) {
    this.underlyingMap = underlyingMap;
  }

  public static <K extends Comparable<? super K>, V> ContiguousDiscreteRangeMap<K, V> contiguousDiscreteRangeMap(
      List<Range<K>> ranges, List<V> values, UnaryOperator<K> nextItemGenerator) {
    NonContiguousRangeMap<K, V> nonContiguousMap = nonContiguousRangeMap(ranges, values);
    RBOrderingPreconditions.checkConsecutive(
        nonContiguousMap.getRawRangeMap().asDescendingMapOfRanges().keySet(),
        // Inverted order (range2, range1) is intentional; it effectively reverses the order of asDescendingMapOfRanges.
        (range2, range1) -> (nextItemGenerator.apply(range1.upperEndpoint()).equals(range2.lowerEndpoint())),
        "The %s ranges specified are not contiguous.",
        nonContiguousMap.getRawRangeMap().asDescendingMapOfRanges().size());
    return new ContiguousDiscreteRangeMap<K, V>(nonContiguousMap);
  }

  public static <K extends Comparable<? super K>, V> ContiguousDiscreteRangeMap<K, V> singletonContiguousDiscreteRangeMap(
      Range<K> range, V value) {
    return new ContiguousDiscreteRangeMap<K, V>(nonContiguousRangeMap(
        singletonList(range),
        singletonList(value)));
  }

  public Optional<V> getOptional(K key) {
    return underlyingMap.getOptional(key);
  }

  /**
   * Useful for a case e.g. where the keys are localdates, and we're looking for
   * the latest date that we have a value for
   */
  public Optional<V> getOptionalWithHighestKeyBelow(K key) {
    return underlyingMap.getOptionalWithHighestKeyBelow(key);
  }

  public V getOrThrow(K key) {
    return underlyingMap.getOrThrow(key);
  }

  public NonContiguousRangeMap<K, V> getUnderlyingMap() {
    return underlyingMap;
  }

  @Override
  public String toString() {
    return Strings.format("[CDRM %s CDRM]", underlyingMap);
  }

}
