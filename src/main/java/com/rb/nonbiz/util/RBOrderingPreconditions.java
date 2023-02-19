package com.rb.nonbiz.util;

import com.google.inject.Inject;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.PartialComparator;
import com.rb.nonbiz.collections.PartiallyComparable;
import com.rb.nonbiz.text.PrintableMessageFormatterForInstruments;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

import static com.rb.nonbiz.collections.RBIterables.consecutivePairs;
import static com.rb.nonbiz.collections.RBIterables.forEachUnequalPairInList;
import static com.rb.nonbiz.collections.RBIterators.consecutivePairsIterator;

public class RBOrderingPreconditions {

  @Inject static PrintableMessageFormatterForInstruments printableMessageFormatterForInstruments;

  /**
   * For n items, throw if any of the n-1 pairs of consecutive items
   * does not satisfy the supplied predicate.
   */
  public static <T> void checkConsecutive(Iterable<T> iterable, BiPredicate<T, T> sanityChecker, String format, Object...args) {
    checkConsecutive(iterable.iterator(), sanityChecker, format, args);
  }

  /**
   * For n items, throw if any of the n-1 pairs of consecutive items
   * does not satisfy the supplied predicate.
   */
  public static <T> void checkConsecutive(Iterator<T> iterator, BiPredicate<T, T> sanityChecker, String format, Object...args) {
    consecutivePairsIterator(iterator)
        .forEachRemaining(pair -> {
          T item1 = pair.getLeft();
          T item2 = pair.getRight();
          if (!sanityChecker.test(item1, item2)) {
            StringBuilder sb = new StringBuilder();
            sb.append(smartFormat(format, args));
            sb.append(smartFormat(" : %s -> %s", item1, item2));
            throw new IllegalArgumentException(sb.toString());
          }
        });
  }

  /**
   * For n items, throw if any of the n-1 pairs of consecutive items
   * does not satisfy the supplied predicate.
   */
  public static <T> void checkConsecutive(Iterable<T> iterable, BiPredicate<T, T> sanityChecker) {
    checkConsecutive(iterable, sanityChecker, "");
  }

  /**
   * For n items, throw if any of the n-1 pairs of consecutive items
   * does not satisfy the supplied predicate.
   */
  public static <T> void checkConsecutive(Iterator<T> iterator, BiPredicate<T, T> sanityChecker) {
    checkConsecutive(iterator, sanityChecker, "");
  }

  /**
   * For n items, throw if the items (which implement Comparable) aren't strictly increasing.
   */
  public static <T extends Comparable<? super T>> void checkIncreasing(Iterable<T> iterable) {
    checkIncreasing(iterable, "items must be in increasing order");
  }

  /**
   * For n items, throw if the items (which implement Comparable) aren't strictly increasing.
   */
  public static <T extends Comparable<? super T>> void checkIncreasing(Iterator<T> iterator) {
    checkIncreasing(iterator, "items must be in increasing order");
  }

  /**
   * For n items, throw if the items aren't strictly increasing.
   * If T implements Comparator, it's better to use the overload that doesn't take in a comparator.
   */
  public static <T> void checkIncreasing(Iterable<T> iterable, Comparator<T> comparator) {
    checkIncreasing(iterable.iterator(), comparator, "items must be in increasing order");
  }

  /**
   * For n items, throw if the items aren't strictly increasing.
   * If T implements Comparator, it's better to use the overload that doesn't take in a comparator.
   */
  public static <T> void checkIncreasing(Iterator<T> iterator, Comparator<T> comparator) {
    checkIncreasing(iterator, comparator, "items must be in increasing order");
  }

  /**
   * For n items, throw if the items (which implement Comparable) aren't strictly increasing.
   */
  public static <T extends Comparable<? super T>> void checkIncreasing(Iterable<T> iterable, String format, Object...args) {
    checkIncreasing(iterable.iterator(), format, args);
  }

  /**
   * For n items, throw if the items (which implement Comparable) aren't strictly increasing.
   */
  public static <T extends Comparable<? super T>> void checkIncreasing(Iterator<T> iterator, String format, Object...args) {
    checkConsecutive(iterator, (v1, v2) -> v1.compareTo(v2) < 0, format, args);
  }

  /**
   * For n items, throw if the items (which implement Comparable) aren't strictly increasing.
   */
  public static <T> void checkIncreasing(Iterator<T> iterator, Comparator<T> comparator, String format, Object...args) {
    checkConsecutive(iterator, (v1, v2) -> comparator.compare(v1, v2) < 0, format, args);
  }

  /**
   * For n items, throw if the items (which implement Comparable) aren't decreasing (or staying the same).
   */
  public static <T extends Comparable<T>> void checkNotIncreasing(Iterable<T> iterable) {
    checkNotIncreasing(iterable, "items must be in 'not-increasing' order");
  }

  /**
   * For n items, throw if the items (which implement Comparable) aren't decreasing (or staying the same).
   */
  public static <T extends Comparable<T>> void checkNotIncreasing(Iterator<T> iterator) {
    checkNotIncreasing(iterator, "items must be in 'not-increasing' order");
  }

  /**
   * For n items, throw if the items (which implement Comparable) aren't decreasing (or staying the same).
   */
  public static <T extends Comparable<T>> void checkNotIncreasing(Iterable<T> iterable, String format, Object...args) {
    checkConsecutive(iterable, (v1, v2) -> v1.compareTo(v2) >= 0, format, args);
  }

  /**
   * For n items, throw if the items (which implement Comparable) aren't decreasing (OK to stay the same).
   */
  public static <T extends Comparable<T>> void checkNotIncreasing(Iterator<T> iterator, String format, Object...args) {
    checkConsecutive(iterator, (v1, v2) -> v1.compareTo(v2) >= 0, format, args);
  }

  /**
   * For n items, throw if the items (which implement Comparable) aren't increasing (OK to stay the same).
   */
  public static <T extends Comparable<T>> void checkNotDecreasing(Iterator<T> iterator, String format, Object...args) {
    checkConsecutive(iterator, (v1, v2) -> v1.compareTo(v2) <= 0, format, args);
  }

  /**
   * For n items, throw if the items aren't increasing (OK to stay the same).
   */
  public static <T> void checkNotDecreasing(Iterator<T> iterator, Comparator<T> comparator, String format, Object...args) {
    checkConsecutive(iterator, (v1, v2) -> comparator.compare(v1, v2) <= 0, format, args);
  }

  /**
   * For n items, throw if the items are not decreasing, in the 'partial comparison' sense.
   * Since we are dealing with a partial ordering, it is possible that e.g. {@code a < b}, {@code c < d}, but there are no other
   * partial orderings. Therefore, we must check all pairs, not just consecutive ones, as we would with
   * 'non-partial' comparisons. For example, dacb is invalid because {@code a > b} and {@code d > c,} but bc and ca aren't invalid,
   * as no partial comparison exists for those.
   */
  public static <T> void checkDecreasingPerPartialComparison(
      PartialComparator<T> partialComparator, List<T> list, String format, Object...args) {
    forEachUnequalPairInList(list, (item1, item2) ->
        partialComparator.partiallyCompare(item1, item2)
            .getRawResult()
            .ifPresent(comparisonResult -> RBPreconditions.checkArgument(
                comparisonResult > 0,
                format,
                args)));
  }

  /**
   * For n items, throw if the items are not decreasing, in the 'partial comparison' sense.
   * Since we are dealing with a partial ordering, it is possible that e.g. {@code a < b}, {@code c < d}, but there are no other
   * partial orderings. Therefore, we must check all pairs, not just consecutive ones, as we would with
   * 'non-partial' comparisons. For example, dacb is invalid because {@code a > b} and {@code d > c}, but bc and ca aren't invalid,
   * as no partial comparison exists for those.
   */
  public static <T extends PartiallyComparable<T>> void checkDecreasingPerPartialComparison(
      List<T> list, String format, Object...args) {
    forEachUnequalPairInList(list, (item1, item2) ->
        item1.partiallyCompareTo(item2)
            .getRawResult()
            .ifPresent(comparisonResult -> RBPreconditions.checkArgument(
                comparisonResult > 0,
                format,
                args)));
  }

  /**
   * This checks that the right endpoint of a subrange is the left endpoint of the next subrange,
   * e.g. {[x0, x1], [x1, x2], [x2, x3]}
   *
   * We often model a closed range as a partition of connected subranges that make up the entire range.
   *
   * We actually model this with closed ranges. It would have been more correct to use open ranges
   * on one side of each 'connection point', such as { [x0, x1), [x1, x2), [x2, x3]}, but this is a bit less relevant
   * for doubles, since we never care about precise equality anyway. Used closed ranges everywhere is more expedient.
   */
  public static <T> void checkConsecutiveClosedDoubleRanges(
      Iterator<ClosedRange<Double>> rangesIterator, Epsilon epsilon) {
    consecutivePairs( () -> rangesIterator)
        .stream()
        .forEach(pair -> {
          ClosedRange<Double> range1 = pair.getLeft();
          ClosedRange<Double> range2 = pair.getRight();
          RBPreconditions.checkArgument(
              Math.abs(range1.upperEndpoint() - range2.lowerEndpoint()) < 1e-8,
              "Ranges must be consecutive using epsilon %s but that was not the case for %s -> %s",
              epsilon, range1, range2);
        });
  }

  private static String smartFormat(String template, Object... args) {
    return printableMessageFormatterForInstruments == null
        ? Strings.format(template, args)
        : printableMessageFormatterForInstruments.formatWithTimePrepended(template, args);
  }

}
