package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.ClosedUnitFractionRange;
import com.rb.nonbiz.types.UnitFraction;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.RBStreams.sumAsBigDecimals;

/**
 * If the sum of the lower bounds is {@code <=} 1 or upper bounds {@code >=} 1, there's no valid solution to the LP,
 * so it's good to be able to catch this earlier.
 */
public class PartitionRangesSanityChecker {

  private static final BigDecimal MAX_SUM_OF_LOWER_BOUNDS = BigDecimal.valueOf(1 + 1e-8);
  private static final BigDecimal MIN_SUM_OF_UPPER_BOUNDS = BigDecimal.valueOf(1 - 1e-8);

  public boolean rangesAreValid(Collection<ClosedUnitFractionRange> ranges) {
    return fractionRangesAreValid(ranges);
  }

  public static boolean fractionRangesAreValid(Collection<ClosedUnitFractionRange> ranges) {
    Stream<UnitFraction> lowerBounds = ranges.stream().map(range -> range.lowerEndpoint());
    Stream<UnitFraction> upperBounds = ranges.stream().map(range -> range.upperEndpoint());
    BigDecimal sumOfLowerBounds = sumAsBigDecimals(lowerBounds);
    BigDecimal sumOfUpperBounds = sumAsBigDecimals(upperBounds);
    if (sumOfLowerBounds.compareTo(MAX_SUM_OF_LOWER_BOUNDS) > 0) {
      return false;
    }
    if (sumOfUpperBounds.compareTo(MIN_SUM_OF_UPPER_BOUNDS) < 0) {
      return false;
    }
    return true;
  }

}
