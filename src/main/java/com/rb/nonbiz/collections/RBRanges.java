package com.rb.nonbiz.collections;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ClosedUnitFractionRange;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static com.google.common.collect.BoundType.CLOSED;
import static com.google.common.collect.BoundType.OPEN;
import static com.google.common.collect.Range.range;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

/**
 * Various static methods pertaining to Range objects.
 */
public class RBRanges {

  public static boolean closedDoubleRangeEpsilonContains(ClosedRange<Double> range, double value) {
    return closedDoubleRangeEpsilonContains(range, value, 1e-8);
  }

  public static boolean closedDoubleRangeEpsilonContains(ClosedRange<Double> range, double value, double epsilon) {
    return range.lowerEndpoint() - epsilon <= value && value <= range.upperEndpoint() + epsilon;
  }

  public static boolean closedDoubleRangeContainsWellWithinBounds(ClosedRange<Double> range, double value) {
    return closedDoubleRangeContainsWellWithinBounds(range, value, 1e-8);
  }

  public static boolean closedDoubleRangeContainsWellWithinBounds(ClosedRange<Double> range, double value, double epsilon) {
    return range.lowerEndpoint() + epsilon <= value && value <= range.upperEndpoint() - epsilon;
  }

  public static <P extends PreciseValue<? super P>> boolean preciseValueRangeEpsilonContains(Range<P> range, P value) {
    return preciseValueRangeEpsilonContains(range, value, 1e-8);
  }

  public static <P extends PreciseValue<? super P>> boolean preciseValueRangeEpsilonContains(Range<P> range, P value, double epsilon) {
    if (range.hasLowerBound()) {
      P lowerBound = range.lowerEndpoint();
      // is 'value' more than epsilon BELOW the lower bound?
      // Treat OPEN and CLOSED separately to support epsilon = 0
      int valueCompareToLower = Double.compare(value.doubleValue(), lowerBound.doubleValue() - epsilon);
      if (range.lowerBoundType() == CLOSED && valueCompareToLower <  0 ||
          range.lowerBoundType() == OPEN   && valueCompareToLower <= 0) {
        return false;
      }
    }

    if (range.hasUpperBound()) {
      P upperBound = range.upperEndpoint();
      // is 'value' more than epsilon ABOVE the upper bound?
      int valueCompareToUpper = Double.compare(value.doubleValue(), upperBound.doubleValue() + epsilon);
      if (range.upperBoundType() == CLOSED && valueCompareToUpper >  0 ||
          range.upperBoundType() == OPEN   && valueCompareToUpper >= 0) {
        return false;
      }
    }

    return true;
  }

  public static boolean doubleRangeIsAlmostSinglePoint(Range<Double> range) {
    return doubleRangeIsAlmostSinglePoint(range, 1e-8);
  }

  public static boolean doubleRangeIsAlmostSinglePoint(Range<Double> range, double epsilon) {
    RBPreconditions.checkArgument(epsilon >= 0);
    return rangeIsClosed(range) && (range.upperEndpoint() - range.lowerEndpoint() <= epsilon);
  }

  public static boolean doubleRangeIsAlmostThisSinglePoint(Range<Double> range, double value) {
    return doubleRangeIsAlmostThisSinglePoint(range, value, 1e-8);
  }

  /**
   * E.g. returns true if you pass it [1.1, 1.1] and 1.1
   */
  public static boolean doubleRangeIsAlmostThisSinglePoint(Range<Double> range, double value, double epsilon) {
    RBPreconditions.checkArgument(epsilon >= 0);
    return rangeIsClosed(range)
        && Math.abs(range.upperEndpoint() - value) <= epsilon
        && Math.abs(range.lowerEndpoint() - value) <= epsilon;
  }

  // Since we use an epsilon, the extra precision of BigDecimal is irrelevant here, so this has no PreciseValue equivalent.
  public static <T extends RBNumeric<? super T>> boolean rbNumericRangeIsAlmostSinglePoint(Range<T> range) {
    return rbNumericRangeIsAlmostSinglePoint(range, 1e-8);
  }

  // Since we use an epsilon, the extra precision of BigDecimal is irrelevant here, so this has no PreciseValue equivalent.
  public static <T extends RBNumeric<? super T>> boolean rbNumericRangeIsAlmostSinglePoint(Range<T> range, double epsilon) {
    RBPreconditions.checkArgument(epsilon >= 0);
    return rangeIsClosed(range) && (range.upperEndpoint().doubleValue() - range.lowerEndpoint().doubleValue() <= epsilon);
  }

  // Since we use an epsilon, the extra precision of BigDecimal is irrelevant here, so this has no PreciseValue equivalent.
  public static <T extends RBNumeric<? super T>> boolean rbNumericRangeIsAlmostThisSinglePoint(Range<T> range, T value) {
    return rbNumericRangeIsAlmostThisSinglePoint(range, value, 1e-8);
  }

  /**
   * E.g. returns true if you pass it [1.1, 1.1] and 1.1
   *
   * Since we use an epsilon, the extra precision of BigDecimal is irrelevant here, so this has no PreciseValue equivalent.
   */
  public static <T extends RBNumeric<? super T>> boolean rbNumericRangeIsAlmostThisSinglePoint(Range<T> range, T value, double epsilon) {
    RBPreconditions.checkArgument(epsilon >= 0);
    return rangeIsClosed(range)
        && Math.abs(range.upperEndpoint().doubleValue() - value.doubleValue()) <= epsilon
        && Math.abs(range.lowerEndpoint().doubleValue() - value.doubleValue()) <= epsilon;
  }

  public static <C extends Comparable<? super C>> boolean rangeIsUnrestricted(Range<C> range) {
    return !range.hasUpperBound() && !range.hasLowerBound();
  }

  public static <C extends Comparable<? super C>> boolean rangeIsClosed(Range<C> range) {
    return range.hasLowerBound() && range.lowerBoundType() == CLOSED
        && range.hasUpperBound() && range.upperBoundType() == CLOSED;
  }

  public static <C extends Comparable<? super C>> boolean rangeIsBounded(Range<C> range) {
    return range.hasLowerBound() && range.hasUpperBound();
  }

  public static <C extends Comparable<? super C>> boolean rangeIsAtLeast(Range<C> range) {
    return range.hasLowerBound() && range.lowerBoundType() == CLOSED
        && !range.hasUpperBound();
  }

  public static <C extends Comparable<? super C>> boolean hasOpenLowerBound(Range<C> range) {
    return range.hasLowerBound() && range.lowerBoundType() == OPEN;
  }

  public static <C extends Comparable<? super C>> boolean hasOpenUpperBound(Range<C> range) {
    return range.hasUpperBound() && range.upperBoundType() == OPEN;
  }

  public static <C extends Comparable<? super C>> boolean hasEitherBoundOpen(Range<C> range) {
    return hasOpenLowerBound(range) || hasOpenUpperBound(range);
  }

  /**
   * Check if the first Range is a "proper" subset of the second.
   * Note that we're interpreting "proper" to mean that there is at least one point on BOTH
   * the upper and lower bounds of the superset that is NOT in the subset.
   *
   * <p> Open Ranges (a, b), closed ranged [a, b] and mixed open closed [a, b), (a, b] are supported.
   * (a, b) would be considered a proper subset of [a, b], but (a, b] would not. </p>
   *
   * <p> 'Unrestricted' ranges, where one bound is +/- infinity, e.g. [a, inf), (-inf, b] are also supported.
   * If both Ranges are unrestricted on the same end, we check the other to see if it qualified as "proper".
   * E.g. we consider [2, inf) to be a proper subset of [1, inf). </p>
   **/
  public static <C extends Comparable<? super C>> boolean rangeIsProperSubsetOnBothEnds(Range<C> subset, Range<C> superSet) {
    // check if both ranges are fully unrestricted
    if (rangeIsUnrestricted(superSet) && rangeIsUnrestricted(subset)) {
      return false;
    }

    // If the superset is unbounded on the lower side, it will always contain the subset's lower endpoint (if it exists).
    // So we won't go inside the 'if' statement, because there's no potential for the lower bound causing a 'false' return.
    if (superSet.hasLowerBound()) {
      if (!subset.hasLowerBound()) {
        // superset is bounded on the lower side, but subset isn't, so it can't be a subset.
        return false;
      }
      int compareSubsetLowerBound = superSet.lowerEndpoint().compareTo(subset.lowerEndpoint());
      if (compareSubsetLowerBound > 0) {
        // both subset and superset are bounded on the lower side, but the lower bound is not inside the superset,
        // so this is not a subset/superset relationship.
        return false;
      }
      // equal lower bounds?
      if (compareSubsetLowerBound == 0) {
        if (superSet.lowerBoundType() == CLOSED && subset.lowerBoundType() == CLOSED) {
          // lower bounds are equal and both are of type 'CLOSED'; not a proper subset
          return false;
        }
        if (superSet.lowerBoundType() == OPEN) {
          // lower bounds are equal but 'superSet' lower bound is OPEN; not a proper subset
          return false;
        }
      }
    }

    // The comments here are symmetric to the case above, so we won't repeat them.
    if (superSet.hasUpperBound()) {
      if (!subset.hasUpperBound()) {
        return false;
      }
      int compareSubsetUpperBound = superSet.upperEndpoint().compareTo(subset.upperEndpoint());
      if (compareSubsetUpperBound < 0) {
        return false;
      }
      if (compareSubsetUpperBound == 0) {
        if (superSet.upperBoundType() == CLOSED && subset.upperBoundType() == CLOSED) {
          return false;
        }
        if (superSet.upperBoundType() == OPEN) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Tells us if a range is a subset of another one, but 'well within' inside it, i.e. subject to an epsilon.
   *
   * Note that this cannot just operate on Comparable; it has to deal with things that can be converted to a number,
   * because otherwise it doesn't make sense to add/subtract an epsilon. So this only works with RBNumeric.
   */
  public static <T extends RBNumeric<? super T>> boolean rangeIsSafelyProperSubsetOf(
      Range<T> subset, Range<T> superSet, double epsilon) {
    RBPreconditions.checkArgument(
        epsilon >= 0,
        "Epsilon must be >= 0; found %s",
        epsilon);
    return rangeIsProperSubsetOnBothEnds(
        extendDoubleRangeBiDirectionally(toDoubleRange(subset), epsilon),
        toDoubleRange(superSet));
  }

  /**
   * Returns a closed range for this collection, i.e. [smallest, largest].
   * Throws if the collection is empty.
   */
  public static <C extends Comparable<? super C>> ClosedRange<C> getMinMaxClosedRange(Stream<C> stream) {
    return getMinMaxClosedRange(stream.iterator());
  }

  /**
   * Returns a closed range for this collection of ranges where the lower endpoint is the min of all endpoints,
   * end the upper endpoint is the max of all endpoints.
   * It's not called something like 'rangeSetUnion', because there could be gaps; e.g. [1, 3] and [10, 13] will
   * return [1, 13] here, even though there's a gap between 3 and 10.
   *
   * All ranges passed in must be closed ranges.
   *
   * Throws if the iterator is empty.
   */
  public static <C extends Comparable<? super C>> ClosedRange<C> getMinMaxOfClosedRanges(Iterator<ClosedRange<C>> iterator) {
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "Cannot calculate min and max of 0 ranges");
    ClosedRange<C> firstRange = iterator.next();
    C min = firstRange.lowerEndpoint();
    C max = firstRange.upperEndpoint();
    while (iterator.hasNext()) {
      ClosedRange<C> closedRange = iterator.next();
      if (closedRange.lowerEndpoint().compareTo(min) < 0) {
        min = closedRange.lowerEndpoint();
      }
      if (closedRange.upperEndpoint().compareTo(max) > 0) {
        max = closedRange.upperEndpoint();
      }
    }
    return closedRange(min, max);
  }

  /**
   * Returns a closed range for this collection of ranges where the lower endpoint is the min of all endpoints,
   * end the upper endpoint is the max of all endpoints.
   * It's not called something like 'rangeSetUnion', because there could be gaps; e.g. [1, 3] and [10, 13] will
   * return [1, 13] here, even though there's a gap between 3 and 10.
   *
   * All ranges passed in must be closed ranges.
   *
   * Throws if the iterator is empty.
   */
  @SafeVarargs
  public static <C extends Comparable<? super C>> ClosedRange<C> getMinMaxOfClosedRanges(
      ClosedRange<C> first, ClosedRange<C>...rest) {
    return getMinMaxOfClosedRanges(concatenateFirstAndRest(first, rest).iterator());
  }

  /**
   * Returns a closed range for this iterable, i.e. [smallest, largest].
   * Throws if the collection is empty.
   */
  public static <C extends Comparable<? super C>> ClosedRange<C> getMinMaxClosedRange(Iterable<C> iterable) {
    return getMinMaxClosedRange(iterable.iterator());
  }

  /**
   * Returns a closed range for this collection, i.e. [smallest, largest].
   * Throws if the collection is empty.
   */
  public static <C extends Comparable<? super C>> ClosedRange<C> getMinMaxClosedRange(Iterator<C> iterator) {
    C min = null;
    C max = null;
    if (!iterator.hasNext()) {
      throw new IllegalArgumentException("Cannot get min & max for empty collection");
    }
    while (iterator.hasNext()) {
      C item = iterator.next();
      if (min == null) {
        min = item;
        max = item;
        continue;
      }
      if (item.compareTo(min) < 0) {
        min = item;
      } else if (item.compareTo(max) > 0) {
        max = item;
      }
    }
    return closedRange(min, max);
  }

  /**
   * Returns a closed range where the lower endpoint is a function of the 1st item
   * and the upper endpoint is a function of the last time.
   *
   * Similar to getMinMaxClosedRange, but we assume that the items are already sorted by C,
   * so there is no need to look at items other than the first and last.
   */
  public static <T, C extends Comparable<? super C>> ClosedRange<C> getClosedRangeFromSorted(Iterable<T> iterable, Function<T, C> extractor) {
    return getClosedRangeFromSorted(iterable.iterator(), extractor);
  }

  public static <T, C extends Comparable<? super C>> ClosedRange<C> getClosedRangeFromSorted(Iterator<T> iterator, Function<T, C> extractor) {
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "Cannot make [first, last] range using empty iterator");
    C first = extractor.apply(iterator.next());
    C previous = first;
    while (iterator.hasNext()) {
      C thisItem = extractor.apply(iterator.next());
      RBPreconditions.checkArgument(
          previous.compareTo(thisItem) <= 0,
          "Items are not sorted in increasing value; previous= %s ; this= %s",
          previous, thisItem);
      previous = thisItem;
    }
    return closedRange(first, previous);
  }

  /**
   * E.g. [-3, 5] becomes [-5, 3]
   */
  public static <C extends Comparable<? super C>> Range<C> flipRange(Range<C> original, UnaryOperator<C> flipper) {
    if (original.hasLowerBound()) {
      return !original.hasUpperBound()
          ? Range.upTo(flipper.apply(original.lowerEndpoint()), original.lowerBoundType())
          : range(
          flipper.apply(original.upperEndpoint()), original.upperBoundType(),
          flipper.apply(original.lowerEndpoint()), original.lowerBoundType());
    } else {
      return !original.hasUpperBound()
          ? Range.all()
          : Range.downTo(flipper.apply(original.upperEndpoint()), original.upperBoundType());
    }
  }

  public static Range<Double> flipRange(Range<Double> range) {
    return flipRange(range, d -> -d);
  }

  /**
   * Returns Optional.empty() if the two ranges have nothing in common; otherwise, returns the intersection
   */
  public static <C extends Comparable<? super C>> Optional<Range<C>> optionalIntersection(Range<C> range1, Range<C> range2) {
    if (!range1.isConnected(range2)) {
      return Optional.empty();
    }
    Range<C> intersection = range1.intersection(range2);
    // I have no idea if that's a bug, but the Guava intersection of e.g.
    // (-inf, 2.2) and [2.2, inf) actually shows as (2.2, 2.2], which doesn't seem like a valid range.
    // So I'll handle this separately.
    if (intersection.hasLowerBound() && intersection.hasUpperBound()
        && intersection.lowerEndpoint().compareTo(intersection.upperEndpoint()) == 0
        && (intersection.upperBoundType() == OPEN ||
        intersection.lowerBoundType() == OPEN)) {
      return Optional.empty();
    }
    return Optional.of(intersection);
  }

  /**
   * E.g. [-7, 9] shifted right by 3 becomes [-4, 12]
   *
   * Similar to {@link #extendDoubleRangeBiDirectionally}, except that both endpoints move in the same direction.
   */
  public static Range<Double> shiftDoubleRange(Range<Double> originalRange, double offset) {
    boolean hasLower = originalRange.hasLowerBound();
    boolean hasUpper = originalRange.hasUpperBound();
    return hasLower && hasUpper ? range(
        originalRange.lowerEndpoint() + offset, originalRange.lowerBoundType(),
        originalRange.upperEndpoint() + offset, originalRange.upperBoundType())
        : hasLower ? Range.downTo(originalRange.lowerEndpoint() + offset, originalRange.lowerBoundType())
        : hasUpper ? Range.upTo(originalRange.upperEndpoint() + offset, originalRange.upperBoundType())
        : Range.all();
  }

  /**
   *  E.g. [-7, 9] extended by 3 becomes [-10, 12]
   *
   * Similar to {@link #shiftDoubleRange}, except that the endpoints move in opposite directions.
   */
  public static Range<Double> extendDoubleRangeBiDirectionally(Range<Double> originalRange, double offset) {
    RBPreconditions.checkArgument(
        offset >= 0,
        "Can't 'extend' a range by a negative or zero amount %s", offset);
    boolean hasLower = originalRange.hasLowerBound();
    boolean hasUpper = originalRange.hasUpperBound();
    return hasLower && hasUpper ? range(
        originalRange.lowerEndpoint() - offset, originalRange.lowerBoundType(),
        originalRange.upperEndpoint() + offset, originalRange.upperBoundType())
        : hasLower ? Range.downTo(originalRange.lowerEndpoint() - offset, originalRange.lowerBoundType())
        : hasUpper ? Range.upTo(  originalRange.upperEndpoint() + offset, originalRange.upperBoundType())
        : Range.all();
  }

  /** E.g. the range for the absolute value of a variable in the range of [-7, 9] is [0, 9],
   * and for [-8, 6] it is [0, 8]
   */
  public static Range<Double> rangeForAbsValue(Range<Double> originalRange) {
    boolean hasLower = originalRange.hasLowerBound();
    boolean hasUpper = originalRange.hasUpperBound();

    Double finalUpperBound;
    BoundType finalUpperBoundType;
    if (hasLower && hasUpper) {
      double absOfLower = Math.abs(originalRange.lowerEndpoint());
      double absOfUpper = Math.abs(originalRange.upperEndpoint());
      if (absOfUpper > absOfLower) {
        finalUpperBound = absOfUpper;
        finalUpperBoundType = originalRange.upperBoundType();
      } else if (absOfLower > absOfUpper) {
        finalUpperBound = absOfLower;
        finalUpperBoundType = originalRange.lowerBoundType();
      } else { // absOfLower == absOfUpper
        // bound is the most expansive of the two
        finalUpperBound = absOfLower;
        finalUpperBoundType = getMostInclusiveBoundType(originalRange.lowerBoundType(), originalRange.upperBoundType());
      }
    } else {
      finalUpperBound = null;
      finalUpperBoundType = null;
    }

    double finalLowerBound;
    BoundType finalLowerBoundType;
    boolean useLower = hasLower && originalRange.lowerEndpoint() >= 0;
    boolean useUpper = hasUpper && originalRange.upperEndpoint() <= 0;
    if (useLower && useUpper) {
      double lower = originalRange.lowerEndpoint();
      double upper = originalRange.upperEndpoint();
      RBPreconditions.checkArgument(
          lower == 0 && upper == 0,
          "This isn't possible when bounds aren't 0; it implies lower bound of %s > upper bound of %s",
          lower, upper);
      finalLowerBound = lower; // same as upper
      finalLowerBoundType = CLOSED;
    } else if (useUpper) {
      // At this point, the upper endpoint is guaranteed to be negative
      finalLowerBound = -1 * originalRange.upperEndpoint();
      finalLowerBoundType = originalRange.upperBoundType();
    } else if (useLower) {
      // At this point, the lower endpoint is guaranteed to be positive
      finalLowerBound = originalRange.lowerEndpoint();
      finalLowerBoundType = originalRange.lowerBoundType();
    } else {
      finalLowerBound = 0.0;
      finalLowerBoundType = CLOSED;
    }

    return finalUpperBoundType != null
        ? range(finalLowerBound, finalLowerBoundType, finalUpperBound, finalUpperBoundType)
        : Range.downTo(finalLowerBound, finalLowerBoundType);
  }

  private static BoundType getMostInclusiveBoundType(BoundType boundType1, BoundType boundType2) {
    return boundType1 == CLOSED || boundType2 == CLOSED ? CLOSED : OPEN;
  }

  /**
   * Transforms a {@code ClosedRange<A>} into a {@code ClosedRange<B>}.
   */
  public static <A extends Comparable<? super A>, B extends Comparable<? super B>> ClosedRange<B> transformClosedRange(
      ClosedRange<A> range,
      Function<A, B> transformer) {
    return closedRange(
        transformer.apply(range.lowerEndpoint()),
        transformer.apply(range.upperEndpoint()));
  }

  public static <P extends PreciseValue<? super P>> Range<BigDecimal> transformToBigDecimalRange(Range<P> range) {
    return transformRange(range, v -> v.asBigDecimal());
  }

  public static <P extends PreciseValue<P>> ClosedRange<BigDecimal> transformToBigDecimalClosedRange(
      ClosedRange<P> range) {
    return transformClosedRange(range, v -> v.asBigDecimal());
  }

  /**
   * Transforms a {@code Range<A>} into a {@code Range<B>}.
   *
   * Throws if the transformation reverses the ordering (e.g. {@code v → -v}) AND both lower and upper bounds exist,
   * because a Range must always be in (lower, upper) order.
   */
  public static <A extends Comparable<? super A>, B extends Comparable<? super B>> Range<B> transformRange(
      Range<A> range,
      Function<A, B> transformer) {
    boolean hasLower = range.hasLowerBound();
    boolean hasUpper = range.hasUpperBound();

    if (hasLower) {
      return hasUpper
          ? Range.range(
          transformer.apply(range.lowerEndpoint()), range.lowerBoundType(),
          transformer.apply(range.upperEndpoint()), range.upperBoundType())
          : Range.downTo(transformer.apply(range.lowerEndpoint()), range.lowerBoundType());
    } else {
      return hasUpper
          ? Range.upTo(transformer.apply(range.upperEndpoint()), range.upperBoundType())
          : Range.all();
    }
  }

  /**
   * Returns a copy of the input range, with the lower bound possibly replaced.
   * If the input range has no lower bound, the returned range will have the supplied lower bound (as a closed bound).
   * If a lower bound is present, the greater of the existing and the supplied lower bound will be used.
   *
   * This functionality could be (almost) implemented with withNewLowerEndpoint(). The only difference here is the treatment of
   * the lower bound type; this forces it to be 'closed' if it is overriden by the supplied value.
   * This is a bit inelegant and may be confusing, but we actually want it to be that way.
   *
   * @see #withNewLowerEndpoint
   */
  public static <C extends Comparable<? super C>> Range<C> withNewLowerEndpointFloor(
      Range<C> range,
      C newLowerBoundFloor) {
    boolean hasLower = range.hasLowerBound();
    boolean hasUpper = range.hasUpperBound();

    // if the range has an upper bound, it should be at least as large as the newLowerBoundFloor
    if (hasUpper) {
      RBPreconditions.checkArgument(
          newLowerBoundFloor.compareTo(range.upperEndpoint()) <= 0,
          "newLowerBoundFloor %s > upperEndpoint %s, but that would give an inverted range",
          newLowerBoundFloor, range.upperEndpoint());
    }

    if (hasLower) {
      // replace the lower bound if the new bound is GREATER THAN OR EQUAL TO the lower
      // replacing if equal guarantees that the returned lower bound will be CLOSED if its value equals newLowerBoundFloor;
      boolean replaceLower = range.lowerEndpoint().compareTo(newLowerBoundFloor) <= 0;
      C maxLower = replaceLower ? newLowerBoundFloor : range.lowerEndpoint();
      BoundType lowerBoundType = replaceLower ? CLOSED : range.lowerBoundType();
      return hasUpper
          ? Range.range( maxLower, lowerBoundType, range.upperEndpoint(), range.upperBoundType())
          : Range.downTo(maxLower, lowerBoundType);
    } else {
      return hasUpper
          ? range(newLowerBoundFloor, CLOSED, range.upperEndpoint(), range.upperBoundType())
          : Range.atLeast(newLowerBoundFloor);
    }
  }

  /**
   * Returns a copy of the input range, with the upper bound possibly replaced.
   * If the input range has no upper bound, the returned range will have the supplied upper bound (as a closed bound).
   * If an upper bound is present, the lesser (i.e. tighter) of the existing and the supplied upper bound will be used.
   *
   * @see #withNewLowerEndpointFloor
   */
  public static <C extends Comparable<? super C>> Range<C> withNewUpperEndpointCeiling(
      Range<C> range,
      C newUpperBoundCeiling) {
    boolean hasLower = range.hasLowerBound();
    boolean hasUpper = range.hasUpperBound();

    // if the range has a lower bound, it should be at least as large as the newUpperBoundCeiling
    if (hasLower) {
      RBPreconditions.checkArgument(
          newUpperBoundCeiling.compareTo(range.lowerEndpoint()) >= 0,
          "newUpperBoundCeiling %s < lowerEndpoint %s, but that would give an inverted range",
          newUpperBoundCeiling, range.lowerEndpoint());
    }

    if (hasUpper) {
      // replace the upper bound if the new bound is LESS THAN OR EQUAL TO the upper (i.e. tighter)
      // replacing if equal guarantees that the returned lower bound will be CLOSED if its value equals newLowerBounFloor
      boolean replaceUpper = newUpperBoundCeiling.compareTo(range.upperEndpoint()) <= 0;
      C minUpper = replaceUpper ? newUpperBoundCeiling : range.upperEndpoint();
      BoundType upperBoundType = replaceUpper ? CLOSED : range.upperBoundType();
      return hasLower
          ? Range.range(range.lowerEndpoint(), range.lowerBoundType(), minUpper, upperBoundType)
          : Range.upTo(minUpper, upperBoundType);
    } else {
      return hasLower
          ? range(range.lowerEndpoint(), range.lowerBoundType(), newUpperBoundCeiling, CLOSED)
          : Range.atMost(newUpperBoundCeiling);
    }
  }

  /**
   * Transforms a ClosedRange of PreciseValue into a {@code ClosedRange<Double>}.
   */
  public static <P extends PreciseValue<P>> ClosedRange<Double> toClosedDoubleRange(ClosedRange<P> range) {
    return transformClosedRange(range, v -> v.doubleValue());
  }

  /**
   * Transforms a ClosedRange of ImpreciseValue into a {@code ClosedRange<Double>}.
   */
  public static <P extends ImpreciseValue<P>> ClosedRange<Double> toClosedDoubleRangeFromImpreciseValue(ClosedRange<P> range) {
    return transformClosedRange(range, v -> v.doubleValue());
  }

  /**
   * Transforms a Range of ImpreciseValue or PreciseValue into a {@code Range<Double>}.
   */
  public static <T extends RBNumeric<? super T>> Range<Double> toDoubleRange(Range<T> range) {
    return transformRange(range, v -> v.doubleValue());
  }

  /**
   * Transforms a {@code Range<BigDecimal>} into a {@code Range<Double>}.
   */
  public static Range<Double> bigDecimalToDoubleRange(Range<BigDecimal> range) {
    return transformRange(range, v -> v.doubleValue());
  }

  /**
   * Creates copy of a range with a new lower endpoint,
   * which is either a new one, or a function of the existing one.
   * The upper endpoint (if available) remains the same.
   *
   * @see #withNewUpperEndpoint
   * @see #withPossiblyNewLowerEndpoint
   * @see #withPossiblyNewUpperEndpoint
   */
  public static <P extends Comparable<? super P>> Range<P> withNewLowerEndpoint(
      Range<P> startingRange, Function<Optional<P>, P> existingToNewLowerEndpointCalculator) {
    BoundType newLowerBoundType = startingRange.hasLowerBound() ? startingRange.lowerBoundType() : BoundType.CLOSED;
    P newLowerEndpoint = !startingRange.hasLowerBound()
        ? existingToNewLowerEndpointCalculator.apply(Optional.empty())
        : existingToNewLowerEndpointCalculator.apply(Optional.of(startingRange.lowerEndpoint()));
    return startingRange.hasUpperBound()
        ? Range.range(newLowerEndpoint, newLowerBoundType, startingRange.upperEndpoint(), startingRange.upperBoundType())
        : Range.downTo(newLowerEndpoint, newLowerBoundType);
  }

  /**
   * Creates a copy of a range with a new lower endpoint that must be lower than the existing one.
   * The new lower bound type will be CLOSED, regardless of the bound type of the original.
   *
   * <p> If the initial range has an open lower bound and is extended down to that same bound, this will
   * also count as "decreased". E.g. extending (3, 5] to [3, 5] is supported. </p>
   *
   * @see #withNewLowerEndpoint
   */
  public static <P extends Comparable<? super P>> Range<P> withNewDecreasedClosedLowerEndpoint(
      Range<P> startingRange, UnaryOperator<P> existingToNewLowerEndpointCalculator) {
    RBPreconditions.checkArgument(
        startingRange.hasLowerBound(),
        "starting range must have a lower bound: %s",
        startingRange);
    P newLowerEndpoint = existingToNewLowerEndpointCalculator.apply(startingRange.lowerEndpoint());
    // the new lower endpoint must be below the starting range
    int lowerComparedToNewLower = startingRange.lowerEndpoint().compareTo(newLowerEndpoint);
    RBPreconditions.checkArgument(
        lowerComparedToNewLower > 0 ||
            startingRange.lowerBoundType() == OPEN && lowerComparedToNewLower == 0,
        "new lower bound %s must be below the previous lower bound of %s",
        newLowerEndpoint, startingRange);
    return startingRange.hasUpperBound()
        ? Range.range( newLowerEndpoint, BoundType.CLOSED, startingRange.upperEndpoint(), startingRange.upperBoundType())
        : Range.downTo(newLowerEndpoint, BoundType.CLOSED);
  }

  /**
   * Creates a copy of a range with a new upper endpoint that must be higher than the existing one.
   * The new upper bound type will be CLOSED, regardless of the bound type of the original.
   *
   * <p> If the initial range has an open end upper bound and is extended to that same bound, this will
   * also count as "increased". E.g. [3, 5) increasing to [3, 5] is supported. </p>
   *
   * @see #withNewUpperEndpoint
   */
  public static <P extends Comparable<? super P>> Range<P> withNewIncreasedClosedUpperEndpoint(
      Range<P> startingRange, UnaryOperator<P> existingToNewUpperEndpointCalculator) {
    RBPreconditions.checkArgument(
        startingRange.hasUpperBound(),
        "starting range must have an upper bound: %s",
        startingRange);
    P newUpperEndpoint = existingToNewUpperEndpointCalculator.apply(startingRange.upperEndpoint());
    // the new upper endpoint must be above the starting range
    int upperComparedToNewUpper = startingRange.upperEndpoint().compareTo(newUpperEndpoint);
    RBPreconditions.checkArgument(
        upperComparedToNewUpper < 0 || (startingRange.upperBoundType() == OPEN && upperComparedToNewUpper == 0),
        "new upper bound %s must be above the starting range %s",
        newUpperEndpoint, startingRange);
    return startingRange.hasLowerBound()
        ? Range.range(startingRange.lowerEndpoint(), startingRange.lowerBoundType(), newUpperEndpoint, BoundType.CLOSED)
        : Range.upTo(newUpperEndpoint, BoundType.CLOSED);
  }

  /**
   * Creates copy of a range with a new lower endpoint,
   * which is either a new one, or a function of the existing one,
   * except that if the result of the function of the existing one is empty, we just return the same range.
   * The upper endpoint (if available) remains the same.
   *
   * @see #withNewLowerEndpoint
   * @see #withNewUpperEndpoint
   * @see #withPossiblyNewUpperEndpoint
   */
  public static <P extends Comparable<? super P>> Range<P> withPossiblyNewLowerEndpoint(
      Range<P> startingRange, Function<Optional<P>, Optional<P>> existingToPossiblyNewLowerEndpointCalculator) {
    Optional<P> newLowerEndpoint = !startingRange.hasLowerBound()
        ? existingToPossiblyNewLowerEndpointCalculator.apply(Optional.empty())
        : existingToPossiblyNewLowerEndpointCalculator.apply(Optional.of(startingRange.lowerEndpoint()));
    if (!newLowerEndpoint.isPresent()) {
      return startingRange;
    }
    BoundType newLowerBoundType = startingRange.hasLowerBound() ? startingRange.lowerBoundType() : BoundType.CLOSED;
    return startingRange.hasUpperBound()
        ? Range.range(newLowerEndpoint.get(), newLowerBoundType, startingRange.upperEndpoint(), startingRange.upperBoundType())
        : Range.downTo(newLowerEndpoint.get(), newLowerBoundType);
  }

  /**
   * Creates copy of a range with a new upper endpoint,
   * which is either a new one, or a function of the existing one.
   * The lower endpoint (if available) remains the same.
   *
   * @see #withNewLowerEndpoint
   * @see #withPossiblyNewLowerEndpoint
   * @see #withPossiblyNewUpperEndpoint
   */
  public static <P extends Comparable<? super P>> Range<P> withNewUpperEndpoint(
      Range<P> startingRange, Function<Optional<P>, P> existingToNewUpperEndpointCalculator) {
    BoundType newUpperBoundType = startingRange.hasUpperBound() ? startingRange.upperBoundType() : BoundType.CLOSED;
    P newUpperEndpoint = !startingRange.hasUpperBound()
        ? existingToNewUpperEndpointCalculator.apply(Optional.empty())
        : existingToNewUpperEndpointCalculator.apply(Optional.of(startingRange.upperEndpoint()));
    return startingRange.hasLowerBound()
        ? Range.range(startingRange.lowerEndpoint(), startingRange.lowerBoundType(), newUpperEndpoint, newUpperBoundType)
        : Range.upTo(newUpperEndpoint, newUpperBoundType);
  }

  /**
   * Creates copy of a range with a new upper endpoint,
   * which is either a new one, or a function of the existing one,
   * except that if the result of the function of the existing one is empty, we just return the same range.
   * The lower endpoint (if available) remains the same.
   *
   * @see #withNewLowerEndpoint
   * @see #withNewUpperEndpoint
   * @see #withPossiblyNewLowerEndpoint
   */
  public static <P extends Comparable<? super P>> Range<P> withPossiblyNewUpperEndpoint(
      Range<P> startingRange, Function<Optional<P>, Optional<P>> existingToNewUpperEndpointCalculator) {
    Optional<P> newUpperEndpoint = !startingRange.hasUpperBound()
        ? existingToNewUpperEndpointCalculator.apply(Optional.empty())
        : existingToNewUpperEndpointCalculator.apply(Optional.of(startingRange.upperEndpoint()));
    if (!newUpperEndpoint.isPresent()) {
      return startingRange;
    }
    BoundType newUpperBoundType = startingRange.hasUpperBound() ? startingRange.upperBoundType() : BoundType.CLOSED;
    return startingRange.hasLowerBound()
        ? Range.range(startingRange.lowerEndpoint(), startingRange.lowerBoundType(), newUpperEndpoint.get(), newUpperBoundType)
        : Range.upTo(newUpperEndpoint.get(), newUpperBoundType);
  }

  /**
   * Loosen a Range just enough to intersect another range by a single point.
   *
   * <p> For example, [3.0, 5.0] would be loosened to [3.0, 7.0] to mininally overlap with [7.0, 9.0]. </p>
   *
   * <p> Note that if the initial range is extended, the extended bound type will be CLOSED.
   * E.g. [3.0, 5.0) would be loosened to [3.0, 7.0] to minimall overlap with [7.0, 10.0]. </p>
   *
   * <p> Note that Guava defines (3.0, 5.0) to intersect with (5.0, 9.0), even though the intersection
   * range (5.0, 5.0) is empty. </p>
   *
   * <p> This means that [3.0, 5.0) will be extended to [3.0, 7.0] to overlap with (7.0, 9.0),
   * even though the extended range doesn't "overlap" the other range in the usual sense. However,
   * they would "connect" according to Guava's definition. </p>
   */
  public static <P extends Comparable<? super P>> Range<P> minimallyLoosenToOverlapRange(
      Range<P> initialRange,
      Range<P> otherRange) {
    // If the ranges intersect, return the original range; no loosening is needed.
    // Note that [1, 5) IS considered "connected" to [5, 10], but we would want to extend [1, 5) to [1, 5].
    if (initialRange.isConnected(otherRange) && !initialRange.intersection(otherRange).isEmpty()) {
      return initialRange;
    }

    // initialRange and otherRange do NOT intersect; one must be entirely higher than the other
    boolean initialBelowOther = initialRange.hasUpperBound() && otherRange.hasLowerBound() &&
        initialRange.upperEndpoint().compareTo(otherRange.lowerEndpoint()) <= 0;
    boolean initialAboveOther = initialRange.hasLowerBound() && otherRange.hasUpperBound() &&
        initialRange.lowerEndpoint().compareTo(otherRange.upperEndpoint()) >= 0;
    RBPreconditions.checkArgument(
        // XOR check that exactly one of initialAboveOther and initialBelowOther is true
        initialAboveOther ^ initialBelowOther,
        "Internal error: initial range %s must either be entirely above (%s) or below other (%s) range %s",
        initialRange, initialAboveOther, initialBelowOther, otherRange);
    return initialBelowOther
        ? withNewIncreasedClosedUpperEndpoint(initialRange, v -> otherRange.lowerEndpoint())
        : withNewDecreasedClosedLowerEndpoint(initialRange, v -> otherRange.upperEndpoint());
  }

  /**
   * Given a range, finds the 'nearest' valid value close to a starting value. The tests explain this better.
   */
  public static <P extends Comparable<? super P>> P getNearestValueInRange(Range<P> range, P startingValue) {
    if (range.contains(startingValue)) {
      return startingValue;
    }
    if (range.hasLowerBound() && startingValue.compareTo(range.lowerEndpoint()) <= 0) {
      // starting value is lower than the lower endpoint of the range
      return range.lowerEndpoint();
    }
    if (range.hasUpperBound() && range.upperEndpoint().compareTo(startingValue) <= 0) {
      // starting value is higher than the upper endpoint of range
      return range.upperEndpoint();
    }
    throw new IllegalArgumentException(Strings.format(
        "We should never be here in getNearestValueInRange: range= %s ; startingValue= %s",
        range, startingValue));
  }

  /**
   * One annoying thing about Guava Range is that you cannot construct it without knowing which exact
   * sub-type of range you want. This abstracts away some of that.
   */
  public static <P extends Comparable<? super P>> Range<P> constructRange(
      Optional<P> lowerEndpoint,
      BoundType lowerBoundType,
      Optional<P> upperEndpoint,
      BoundType upperBoundType) {
    boolean hasLower = lowerEndpoint.isPresent();
    boolean hasUpper = upperEndpoint.isPresent();
    return hasLower && hasUpper ?
        range(lowerEndpoint.get(), lowerBoundType, upperEndpoint.get(), upperBoundType)
        : hasLower ? Range.downTo(lowerEndpoint.get(), lowerBoundType)
        : hasUpper ? Range.upTo(upperEndpoint.get(), upperBoundType)
        : Range.all();
  }

  /**
   * The constructors of Guava Range do not guard against some weird cases for Double ranges, so let's check here
   * just to be safe.
   */
  public static void validateAgainstExtremes(Range<Double> range) {
    if (range.hasLowerBound()) {
      double lower = range.lowerEndpoint();
      RBPreconditions.checkArgument(
          !Double.isNaN(lower),
          "Although the Range constructor allows it, you should not have a lower bound of NaN in %s",
          range);
      RBPreconditions.checkArgument(
          lower != NEGATIVE_INFINITY,
          "Although an explicit lower bound of -inf is reasonable, you should use specialized constructors such as Range.atMost %s",
          range);
      RBPreconditions.checkArgument(
          lower != POSITIVE_INFINITY,
          "A lower bound of positive infinity implies a range that matches nothing: %s",
          range);
    }
    if (range.hasUpperBound()) {
      double upper = range.upperEndpoint();
      RBPreconditions.checkArgument(
          !Double.isNaN(upper),
          "Although the Range constructor allows it, you should not have an upper bound of NaN in %s",
          range);
      RBPreconditions.checkArgument(
          upper != NEGATIVE_INFINITY,
          "An upper bound of negative infinity implies a range that matches nothing: %s",
          range);
      RBPreconditions.checkArgument(
          upper != POSITIVE_INFINITY,
          "Although an explicit upper bound of infinity is reasonable, you should use specialized constructors such as Range.atLeast %s",
          range);
    }
  }

  /**
   * Converts a ClosedRange into a range that's possibly open on one or both sides,
   * if the semantics of that
   *
   * This is useful e.g. with {@link ClosedUnitFractionRange}, where a lower bound of UNIT_FRACTION_0 is equivalent
   * to having no lower bound, and an upper bound of UNIT_FRACTION_1 is equivalent to having no upper bound.
   * This method lets us convert e.g. a ClosedRange of [ unitFraction(0.3), UNIT_FRACTION_1 ] to a
   * Range of [ unitFraction(0.3), +inf)
   */
  public static <P extends Comparable<? super P>> Range<P> toRangeWithoutTrivialEndpoints(
      ClosedRange<P> inputRange,
      ClosedRange<P> widestPossibleRange) {
    P trivialLowerEndpoint = widestPossibleRange.lowerEndpoint();
    P trivialUpperEndpoint = widestPossibleRange.upperEndpoint();

    P lower = inputRange.lowerEndpoint();
    P upper = inputRange.upperEndpoint();

    int lowerComparisonResult = lower.compareTo(trivialLowerEndpoint);
    int upperComparisonResult = upper.compareTo(trivialUpperEndpoint);

    RBPreconditions.checkArgument(
        lowerComparisonResult >= 0,
        "The lower bound in the supplied range %s is less than the lower bound of the widest possible range %",
        inputRange, widestPossibleRange);
    RBPreconditions.checkArgument(
        upperComparisonResult <= 0,
        "The upper bound in the supplied range %s is more than the upper bound of the widest possible range %s",
        inputRange, widestPossibleRange);

    // First, handle singleton range case
    if (lower.compareTo(upper) == 0) {
      return Range.singleton(lower);
    }

    return constructRange(
        lower.compareTo(trivialLowerEndpoint) == 0 ? Optional.empty() : Optional.of(lower), CLOSED,
        upper.compareTo(trivialUpperEndpoint) == 0 ? Optional.empty() : Optional.of(upper), CLOSED);
  }

}
