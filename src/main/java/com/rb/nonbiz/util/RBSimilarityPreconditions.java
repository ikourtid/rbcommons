package com.rb.nonbiz.util;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.RBStreams;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PreciseValue;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.RBRanges.getMinMaxClosedRange;
import static java.util.function.Function.identity;

public class RBSimilarityPreconditions {

  /**
   * Throws if the items in the collection, after being transformed by a function, are not all the same.
   */
  public static <T, V> V checkAllSame(Collection<T> items, Function<T, V> valueExtractor) {
    return checkAllSame(items, valueExtractor, "");
  }

  /**
   * Throws if the items in the collection, after being transformed by a function, are not all the same.
   * This is useful for cases where there are multiple items, some being in a collection, and some 'loose'.
   */
  @SafeVarargs
  public static <T, V> V checkAllSameX(
      Function<T, V> valueExtractor, Collection<T> itemCollection, T additionalItem1, T ... additionalItems) {
    return checkAllSame(
        Stream.concat(
            itemCollection.stream(),
            RBStreams.concatenateFirstAndRest(additionalItem1, additionalItems))
        .iterator(),
        valueExtractor,
        "");
  }

  /**
   * Throws if the items in the iterator, after being transformed by a function, are not all the same.
   */
  public static <T, V> V checkAllSame(Iterator<T> iterator, Function<T, V> valueExtractor) {
    return checkAllSameUsingPredicate(iterator, valueExtractor, (v1, v2) -> v1.equals(v2), "");
  }

  /**
   * Throws if the items in the collection, after being transformed by a function, are not all the same.
   */
  public static <T, V> V checkAllSame(Collection<T> items, Function<T, V> valueExtractor, String format, Object...args) {
    return checkAllSame(items.iterator(), valueExtractor, format, args);
  }

  /**
   * Throws if the items in the iterable, after being transformed by a function, are not all the same.
   */
  public static <T, V> V checkAllSame(Iterable<T> iterable, Function<T, V> valueExtractor, String format, Object...args) {
    return checkAllSame(iterable.iterator(), valueExtractor, format, args);
  }

  /**
   * Throws if the items in the iterator, after being transformed by a function, are not all the same.
   */
  public static <T, V> V checkAllSame(Iterator<T> iterator, Function<T, V> valueExtractor, String format, Object...args) {
    return checkAllSameUsingPredicate(iterator, valueExtractor, (v1, v2) -> v1.equals(v2), format, args);
  }

  public static <T, V extends PreciseValue<V>> V checkAllAlmostSame(
      Iterable<T> iterable, Function<T, V> valueExtractor, double epsilon, String format, Object...args) {
    return checkAllSameUsingPredicate(iterable.iterator(), valueExtractor, (v1, v2) -> v1.almostEquals(v2, epsilon), format, args);
  }

  /**
   * Throws if the items in the iterator, after being transformed by a function, are not all the same.
   */
  public static <T, V extends PreciseValue<V>> V checkAllAlmostSame(
      Iterator<T> iterator, Function<T, V> valueExtractor, double epsilon, String format, Object...args) {
    return checkAllSameUsingPredicate(iterator, valueExtractor, (v1, v2) -> v1.almostEquals(v2, epsilon), format, args);
  }

  /**
   * This is more general than the other checkAllSame overloads. It works for cases where hashCode/equals
   * are not implemented, i.e. where we have to specify a predicate to determine equality.
   */
  public static <T, V> V checkAllSameUsingPredicate(
      Iterator<T> iterator, Function<T, V> valueExtractor, BiPredicate<V, V> samenessPredicate, String format, Object...args) {
    if (!iterator.hasNext()) {
      throw new IllegalArgumentException(Strings.format(
          "Empty collection in checkAllSame: message would have been: " + format,
          args));
    }
    V sharedValue = valueExtractor.apply(iterator.next());
    while (iterator.hasNext()) {
      V thisValue = valueExtractor.apply(iterator.next());
      if (!samenessPredicate.test(thisValue, sharedValue)) {
        throw new IllegalArgumentException(Strings.format("%s : shared value so far %s ; encountered different value of %s",
            Strings.format(format, args), sharedValue, thisValue));
      }
    }
    return sharedValue;
  }

  /**
   * Throws if the items in the iterator, after being transformed by a function, are not all the same.
   */
  public static <T> T checkAllSame(Iterator<T> iterator, String format, Object...args) {
    return checkAllSame(iterator, identity(), format, args);
  }

  /**
   * Throws if the items in the iterable, after being transformed by a function, are not all the same.
   */
  public static <T> T checkAllSame(Iterable<T> iterable, String format, Object...args) {
    return checkAllSame(iterable.iterator(), identity(), format, args);
  }

  /**
   * Returns the ClosedRange of the values in the iterator passed in.
   * Throws if the iterable is empty, or if the values are too far apart based on the predicate passed in.
   */
  public static <T extends Comparable<? super T>> ClosedRange<T> checkWithinLimitedRange(
      Iterable<T> iterable,
      Predicate<ClosedRange<T>> rangeIsSmallEnough,
      String format,
      Object ... args) {
    return checkWithinLimitedRange(iterable.iterator(), rangeIsSmallEnough, format, args);
  }

  /**
   * Returns the ClosedRange of the values in the iterator passed in.
   * Throws if the iterator is empty, or if the values are too far apart based on the predicate passed in.
   */
  public static <T extends Comparable<? super T>> ClosedRange<T> checkWithinLimitedRange(
      Iterator<T> iterator,
      Predicate<ClosedRange<T>> rangeIsSmallEnough,
      String format,
      Object ... args) {
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "checkWithinLimitedRange must be called for at least one item");
    ClosedRange<T> minMaxClosedRange = getMinMaxClosedRange(iterator);
    RBPreconditions.checkArgument(
        rangeIsSmallEnough.test(minMaxClosedRange),
        format,
        args);
    return minMaxClosedRange;
  }

  /**
   * Returns the ClosedRange of the values in the iterator passed in.
   * Throws if the iterable is empty, or if the values are too far apart based on the predicate passed in.
   */
  public static <T extends Comparable<? super T>> ClosedRange<T> checkWithinLimitedRange(
      Iterable<T> iterable,
      Predicate<ClosedRange<T>> rangeIsSmallEnough) {
    return checkWithinLimitedRange(iterable.iterator(), rangeIsSmallEnough);
  }

  /**
   * Returns the ClosedRange of the values in the iterator passed in.
   * Throws if the iterator is empty, or if the values are too far apart based on the predicate passed in.
   */
  public static <T extends Comparable<? super T>> ClosedRange<T> checkWithinLimitedRange(
      Iterator<T> iterator,
      Predicate<ClosedRange<T>> rangeIsSmallEnough) {
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "checkWithinLimitedRange must be called for at least one item");
    ClosedRange<T> minMaxClosedRange = getMinMaxClosedRange(iterator);
    RBPreconditions.checkArgument(
        rangeIsSmallEnough.test(minMaxClosedRange),
        "Range of values was %s which according to the predicate passed in is too wide a range",
        minMaxClosedRange);
    return minMaxClosedRange;
  }

  /**
   * Returns the ClosedRange of the values in the iterator passed in.
   * Throws if the iterator is empty, or if the values are too far apart based on the predicate passed in.
   */
  public static ClosedRange<LocalDateTime> checkWithinSeconds(
      Iterable<LocalDateTime> iterator,
      long maxDifferenceInSeconds) {
    return checkWithinSeconds(iterator.iterator(), maxDifferenceInSeconds);
  }

  /**
   * Returns the ClosedRange of the values in the iterator passed in.
   * Throws if the iterator is empty, or if the values are too far apart based on the predicate passed in.
   */
  public static ClosedRange<LocalDateTime> checkWithinSeconds(
      Iterator<LocalDateTime> iterator,
      long maxDifferenceInSeconds) {
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "checkWithinSeconds must be called for at least one item");
    RBPreconditions.checkArgument(
        maxDifferenceInSeconds >= 0,
        "Internal error: maxDifferenceInSeconds must be non-negative when you call #checkWithinSeconds; was %s",
        maxDifferenceInSeconds);
    ClosedRange<LocalDateTime> range = getMinMaxClosedRange(iterator);
    long differenceInSeconds = ChronoUnit.SECONDS.between(range.lowerEndpoint(), range.upperEndpoint());
    RBPreconditions.checkArgument(
        differenceInSeconds <= maxDifferenceInSeconds,
        "Range of times was %s and difference between earliest and latest was %s which is above max of %s",
        range, differenceInSeconds, maxDifferenceInSeconds);
    return range;
  }

  public static <T> T checkBothSame(T item1, T item2, String format, Object...args) {
    return checkAllSame(ImmutableList.of(item1, item2), identity(), format, args);
  }

  public static <T> T checkAllThreeSame(T item1, T item2, T item3, String format, Object...args) {
    return checkAllSame(ImmutableList.of(item1, item2, item3), identity(), format, args);
  }

  /**
   * checkBothSame is useful because it also returns the item that you asserted is the same.
   * This is useful in cases where you want to create a new variable whose name denotes 'shared value'. Example:
   *
   * int sharedLength = RBPreconditions.checkBothSame(arr1.length, arr2.length);
   * String[] newArray = new String[sharedLength];
   *
   * vs
   *
   * RBPreconditions.checkArgument(arr1.length == arr2.length);
   * // lots of intervening code
   * String[] newArray = new String[arr1.length];
   *
   * in the latter case, we can of course pick arr1.length or arr2.length arbitrarily, but if enough code intervenes
   * after the precondition, it may look like in the code
   * that are specifically choosing the length of array 1 instead of the length of array 2, but it may not be
   * clear by that point that they are the same.
   */
  public static <T> T checkBothSame(T item1, T item2, BiPredicate<T, T> samenessPredicate, String format, Object...args) {
    return checkAllSameUsingPredicate(ImmutableList.of(item1, item2).iterator(), identity(), samenessPredicate, format, args);
  }

}
