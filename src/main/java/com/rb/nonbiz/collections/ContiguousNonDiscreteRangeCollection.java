package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBOrderingPreconditions;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;

/**
 * Represents a list of contiguous ranges of a continuous value such as double, with the uppermost range
 * possibly being unbounded on the upper endpoint. Examples:
 * using static constructor contiguousNonDiscreteRangeCollectionWithEnd:   [1.1, 3.3), [3.3, 7.7)
 * using static constructor contiguousNonDiscreteRangeCollectionWithNoEnd: [1.1, 3.3), [3.3, +inf)
 *
 * This is similar to a {@code List<Range>}, except it is guaranteed (via preconditions at assertion time)
 * to be non-disjoint/contiguous, i.e. have neither overlap or holes.
 *
 * It has 'non-discrete' in its name to clarify that it should be used for cases where there's no clearly
 * defined notion of a next item. So it essentially represents a bunch of ranges like
 * [k1, k2) , [k2, k3), [k3, k4), ...
 * E.g. doubles is a good example.
 *
 * A better word for this is 'continuous', but ContiguousContinuousRangeCollection sounds very confusing.
 */
public class ContiguousNonDiscreteRangeCollection<K extends Comparable> {

  public enum LastPointInRangeTreatment {
    INCLUDE(true),
    EXCLUDE(false);

    private final boolean include;

    LastPointInRangeTreatment(boolean include) {
      this.include = include;
    }

    public boolean include() {
      return include;
    }

  }

  private final List<Range<K>> rawRangeCollection;

  private ContiguousNonDiscreteRangeCollection(List<Range<K>> rawRangeCollection) {
    this.rawRangeCollection = rawRangeCollection;
  }

  /**
   * E.g. [1.1, 3.3), [3.3, +inf)
   */
  public static <K extends Comparable> ContiguousNonDiscreteRangeCollection<K> contiguousNonDiscreteRangeCollectionWithNoEnd(
      List<K> rangeStartingPoints) {
    sanityCheckCommon(rangeStartingPoints);
    List<Range<K>> rawRangeCollection = newArrayList();
    for (int i = 0; i < rangeStartingPoints.size(); i++) {
      boolean isLast = (i == rangeStartingPoints.size() - 1);
      Range<K> range = isLast
          ? Range.<K>atLeast(rangeStartingPoints.get(i))
          : Range.closedOpen(rangeStartingPoints.get(i), rangeStartingPoints.get(i + 1));
      rawRangeCollection.add(range);
    }
    return new ContiguousNonDiscreteRangeCollection<K>(rawRangeCollection);
  }

  public static <K extends Comparable> ContiguousNonDiscreteRangeCollection<K> singletonNonDiscreteContiguousRangeCollectionWithNoEnd(
      K rangeStartingPoint) {
    return contiguousNonDiscreteRangeCollectionWithNoEnd(singletonList(rangeStartingPoint));
  }

  /**
   * E.g. [1.1, 3.3), [3.3, 7.7)
   */
  public static <K extends Comparable> ContiguousNonDiscreteRangeCollection<K> contiguousNonDiscreteRangeCollectionWithEnd(
      List<K> rangePoints,
      LastPointInRangeTreatment lastPointInRangeTreatment) {
    sanityCheckCommon(rangePoints);
    int size = rangePoints.size();
    RBPreconditions.checkArgument(
        size >= 2,
        "We must have at least a starting and an ending point, vs %s",
        rangePoints);
    List<Range<K>> rawRangeCollection = newArrayList();
    for (int i = 0; i < size - 2; i++) {
      rawRangeCollection.add(Range.closedOpen(rangePoints.get(i), rangePoints.get(i + 1)));
    }
    K lastLowerEndpoint = rangePoints.get(size - 2);
    K lastUpperEndpoint = rangePoints.get(size - 1);
    rawRangeCollection.add(lastPointInRangeTreatment.include()
        ? Range.closed(lastLowerEndpoint, lastUpperEndpoint)
        : Range.closedOpen(lastLowerEndpoint, lastUpperEndpoint));
    return new ContiguousNonDiscreteRangeCollection<K>(rawRangeCollection);
  }

  public static <K extends Comparable> ContiguousNonDiscreteRangeCollection<K> singletonNonDiscreteContiguousRangeCollectionWithEnd(
      K rangeStartingPoint,
      K rangeEndingPoint,
      LastPointInRangeTreatment lastPointInRangeTreatment) {
    return contiguousNonDiscreteRangeCollectionWithEnd(
        ImmutableList.of(rangeStartingPoint, rangeEndingPoint),
        lastPointInRangeTreatment);
  }

  private static <K extends Comparable> void sanityCheckCommon(List<K> rangeStartingPoints) {
    RBPreconditions.checkArgument(
        !rangeStartingPoints.isEmpty(),
        "You must have > 0 ranges");
    RBOrderingPreconditions.checkIncreasing(
        rangeStartingPoints,
        "Keys in rangeCollection (the starts of ranges) must be strictly increasing");
  }

  public List<Range<K>> getRawRangeCollection() {
    return rawRangeCollection;
  }

  @Override
  public String toString() {
    return Strings.format("[CNDRC %s CNDRC]", rawRangeCollection);
  }

}
