package com.rb.nonbiz.collections;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBOrderingPreconditions;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import static com.rb.nonbiz.collections.RBRanges.rangeIsAtLeast;
import static com.rb.nonbiz.collections.RBRanges.rangeIsClosed;
import static java.util.Collections.singletonList;

/**
 * Strictly speaking this is 'not necessarily contiguous'. It could still be contiguous.
 *
 * I'm using {@code <? super K>} instead of {@code <K>} because there I may eventually want to have K be LocalDate,
 * but LocalDate implements ChronoLocalDate which in turn implements {@code Comparable<ChronoLocalDate>},
 * so there's no other way to get this to work.
 *
 * Note that we model all ranges (except maybe the last one) as closed, which is intentional for 'non-contiguous'
 * (discrete) values.
 * For e.g. doubles, this wouldn't work; we'd be representing this as e.g. [2, 5.5] and [5.5, 7]
 * instead of [2, 5.5) and [5.5, 7], so there would be overlap.
 * For e.g. integers, this does work; we'd be using e.g. [2, 4] and [5, 7], so there would be no gaps,
 * but no range would be open on the right side.
 */
public class NonContiguousRangeCollection<K extends Comparable<? super K>> {

  private final List<Range<K>> rawRangeCollection;

  private NonContiguousRangeCollection(List<Range<K>> rawRangeCollection) {
    this.rawRangeCollection = rawRangeCollection;
  }

  public static <K extends Comparable<? super K>> NonContiguousRangeCollection<K> nonContiguousRangeCollectionWithEnd(
      List<Range<K>> closedRanges) {
    RBPreconditions.checkArgument(
        !closedRanges.isEmpty() && rangeIsClosed(Iterables.getLast(closedRanges)),
        "If you know this non-contiguous range has an end, the last range should be closed");
    return nonContiguousRangeCollection(closedRanges);
  }

  public static <K extends Comparable<? super K>> NonContiguousRangeCollection<K> nonContiguousRangeCollectionWithNoEnd(
      List<Range<K>> closedRanges) {
    RBPreconditions.checkArgument(
        !closedRanges.isEmpty() && rangeIsAtLeast(Iterables.getLast(closedRanges)),
        "If you know this non-contiguous range has an end, the last range should be 'at least'");
    return nonContiguousRangeCollection(closedRanges);
  }

  public static <K extends Comparable<? super K>> NonContiguousRangeCollection<K> nonContiguousRangeCollection(
      List<Range<K>> ranges) {
    RBPreconditions.checkArgument(
        !ranges.isEmpty(),
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
    return new NonContiguousRangeCollection<K>(ranges);
  }

  public static <K extends Comparable<? super K>> NonContiguousRangeCollection<K> singletonNonContiguousRangeCollectionWithEnd(
      Range<K> closedRange) {
    return nonContiguousRangeCollectionWithEnd(singletonList(closedRange));
  }

  public static <K extends Comparable<? super K>> NonContiguousRangeCollection<K> singletonNonContiguousRangeCollectionWithNoEnd(
      K startOfOpenInterval) {
    return nonContiguousRangeCollectionWithEnd(singletonList(Range.atLeast(startOfOpenInterval)));
  }

  private static <K extends Comparable<? super K>> void sanityCheckCommon(List<Range<K>> closedRanges) {
    RBPreconditions.checkArgument(
        closedRanges.stream().allMatch(closedRange -> rangeIsClosed(closedRange)),
        "All %s ranges must all be closed",
        closedRanges.size());
    RBOrderingPreconditions.checkConsecutive(
        closedRanges,
        (range1, range2) -> range1.upperEndpoint().compareTo(range2.lowerEndpoint()) < 0,
        "Ranges must be strictly increasing");
  }

  public List<Range<K>> getRawRangeCollection() {
    return rawRangeCollection;
  }

  @Override
  public String toString() {
    return Strings.format("[NCRC %s NCRC]", rawRangeCollection);
  }

}
