package com.rb.nonbiz.collections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;

import static com.rb.nonbiz.collections.PartialComparisonResult.definedPartialComparison;
import static com.rb.nonbiz.collections.PartialComparisonResult.noOrderingDefined;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformPairOfOptionalDoubles;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformPairOfOptionalInts;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformPairOfOptionals;

public class RBComparators {

  @SafeVarargs
  public static <T> Comparator<T> composeComparators(Comparator<T> first, Comparator<T>...rest) {
    return composeComparators(first, Arrays.asList(rest));
  }

  public static <T> Comparator<T> composeComparators(Comparator<T> first, List<Comparator<T>> rest) {
    Comparator<T> result = first;
    for (Comparator<T> next : rest) {
      result = result.thenComparing(next);
    }
    return result;
  }

  public static <T> T maxFromComparator(Comparator<T> comparator, T value1, T value2) {
    return comparator.compare(value1, value2) < 0 ? value2 : value1;
  }

  public static <T> T minFromComparator(Comparator<T> comparator, T value1, T value2) {
    return comparator.compare(value1, value2) > 0 ? value2 : value1;
  }

  /**
   * If both optionals are present, returns the comparison result based on the ordering passed in.
   * Otherwise, returns PartialComparisonResult#noOrderingDefined.
   *
   * If exactly one is empty, there is no comparison to make, so we will assume the two are not comparable.
   */
  public static <T> PartialComparisonResult compareOptionals(
      Comparator<T> comparator, Optional<T> value1, Optional<T> value2) {
    return transformPairOfOptionals(
        value1,
        value2,

        // if neither is empty, compare them
        (v1, v2) -> definedPartialComparison(comparator.compare(v1, v2)),

        // if exactly 1 of the 2 is empty, then there's no ordering defined
        v1 -> noOrderingDefined(),
        v2 -> noOrderingDefined(),

        // if both are empty, then they count as equal
        () -> PartialComparisonResult.equal());
  }

  /**
   * If both optionals are present, returns the PARTIAL comparison result based on the ordering passed in.
   * Otherwise, returns PartialComparisonResult#noOrderingDefined.
   *
   * If exactly one is empty, there is no comparison to make, so we will assume the two are not comparable.
   */
  public static <T> PartialComparisonResult partiallyCompareOptionals(
      PartialComparator<T> partialComparator, Optional<T> value1, Optional<T> value2) {
    return transformPairOfOptionals(
        value1,
        value2,

        // if neither is empty, compare them
        (v1, v2) -> partialComparator.partiallyCompare(v1, v2),

        // if exactly 1 of the 2 is empty, then there's no ordering defined
        v1 -> noOrderingDefined(),
        v2 -> noOrderingDefined(),

        // if both are empty, then they count as equal
        () -> PartialComparisonResult.equal());
  }

  /**
   * If both optionals are present, returns the comparison result.
   * Otherwise, returns PartialComparisonResult#noOrderingDefined.
   *
   * If exactly one is empty, there is no comparison to make, so we will assume the two are not comparable.
   */
  public static PartialComparisonResult compareOptionalDoubles(
      OptionalDouble value1, OptionalDouble value2) {
    return transformPairOfOptionalDoubles(
        value1,
        value2,

        // if neither is empty, compare them
        (v1, v2) -> definedPartialComparison(Double.compare(v1, v2)),

        // if exactly 1 of the 2 is empty, then there's no ordering defined
        v1 -> noOrderingDefined(),
        v2 -> noOrderingDefined(),

        // if both are empty, then they count as equal
        () -> PartialComparisonResult.equal());
  }

  /**
   * If both optionals are present, returns the comparison result.
   * Otherwise, returns PartialComparisonResult#noOrderingDefined.
   *
   * If exactly one is empty, there is no comparison to make, so we will assume the two are not comparable.
   */
  public static PartialComparisonResult compareOptionalInts(
      OptionalInt value1, OptionalInt value2) {
    return transformPairOfOptionalInts(
        value1,
        value2,

        // if neither is empty, compare them
        (v1, v2) -> definedPartialComparison(Integer.compare(v1, v2)),

        // if exactly 1 of the 2 is empty, then there's no ordering defined
        v1 -> noOrderingDefined(),
        v2 -> noOrderingDefined(),

        // if both are empty, then they count as equal
        () -> PartialComparisonResult.equal());
  }

  public static <T> PartialComparisonResult compareOptionalInts(
      T value1, T value2, Function<T, OptionalInt> extractor) {
    return compareOptionalInts(extractor.apply(value1), extractor.apply(value2));
  }

  public static <T> boolean nonDecreasingPerComparator(Comparator<T> comparator, T v1, T v2, T v3) {
    return comparator.compare(v1, v2) <= 0 && comparator.compare(v2, v3) <= 0;
  }

  public static <T> boolean increasingPerComparator(Comparator<T> comparator, T v1, T v2, T v3) {
    return comparator.compare(v1, v2) < 0 && comparator.compare(v2, v3) < 0;
  }

}
