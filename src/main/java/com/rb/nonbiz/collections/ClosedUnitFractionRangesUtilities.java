package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.UnitFraction;

import static com.rb.nonbiz.collections.ClosedUnitFractionRangeUtilities.tightenClosedUnitFractionRangeAround;
import static com.rb.nonbiz.collections.ClosedUnitFractionRanges.closedUnitFractionRanges;
import static com.rb.nonbiz.collections.RBMapMergers.mergeRBMapsByTransformedValue;

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

}
