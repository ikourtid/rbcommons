package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.ClosedUnitFractionRange;
import com.rb.nonbiz.types.UnitFraction;

import static com.rb.nonbiz.collections.ClosedUnitFractionRangeUtilities.optionalClosedUnitFractionRangeIntersection;
import static com.rb.nonbiz.collections.ClosedUnitFractionRangeUtilities.tightenClosedUnitFractionRangeAround;
import static com.rb.nonbiz.collections.ClosedUnitFractionRanges.closedUnitFractionRanges;
import static com.rb.nonbiz.collections.RBMapMergers.mergeRBMapsByTransformedValue;
import static com.rb.nonbiz.collections.RBMapMergers.mergeRBMapsByValue;
import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;

/**
 * Various static functions pertaining to {@link ClosedUnitFractionRanges} objects.
 *
 * <p> We would normally have named it as the plural of ClosedUnitFractionRanges, to follow the usual convention,
 * but the plural of ClosedUnitFractionRanges is awkward. </p>
 *
 * @see ClosedUnitFractionRanges
 */
public class ClosedUnitFractionRangesUtilities {

  public static <K> ClosedUnitFractionRanges<K> tightenClosedUnitFractionRangesAround(
      ClosedUnitFractionRanges<K> initialRanges,
      RBMap<K, UnitFraction> centersOfRanges,
      UnitFraction fractionToTightenOnEachSide) {
    return closedUnitFractionRanges(
        mergeRBMapsByTransformedValue(
            (initialRange, centerOfRange) ->
                tightenClosedUnitFractionRangeAround(initialRange, centerOfRange, fractionToTightenOnEachSide),

            onlyInitialRange -> {
              throw new IllegalArgumentException("You specified an initial range, but no center");
            },

            onlyCenterOfRange -> {
              throw new IllegalArgumentException("You specified the center for a range, but no initial range");
            },

            initialRanges.getRawMap(),
            centersOfRanges));
  }

  /**
   * Merges the two {@link ClosedUnitFractionRanges} by using the set intersection for any two keys that have
   * a {@link ClosedUnitFractionRange} in both arguments (or throwing an exception if no valid intersection exists).
   * Keys that appear in only one of the two input arguments will just get copied over into the returned value.
   */
  public static <K> ClosedUnitFractionRanges<K> closedUnitFractionRangesIntersectionOrThrow(
      ClosedUnitFractionRanges<K> ranges1,
      ClosedUnitFractionRanges<K> ranges2) {
    return closedUnitFractionRanges(mergeRBMapsByValue(
        (v1, v2) -> getOrThrow(
            optionalClosedUnitFractionRangeIntersection(v1, v2),
            "ClosedUnitFractionRange objects %s and %s do not have a valid intersection.",
            v1, v2),
        ranges1.getRawMap(),
        ranges2.getRawMap()));
  }

}
