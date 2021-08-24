package com.rb.nonbiz.collections;

import com.google.common.collect.Sets;
import com.rb.nonbiz.types.UnitFraction;

import java.math.BigDecimal;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.RBSet.rbSet;
import static com.rb.nonbiz.collections.RBStreams.sumBigDecimals;

/**
 * Returns the average difference in partition fraction over each key in the two partitions.
 *
 * If a key appears in one partition but not another, we will treat that other partition like it has a 0% holding
 * in that key. This is not some limiting assumption; partitions never include entries for keys that have a 0%
 * in the partition, so no key means 0%.
 */
public class PartitionAverageAbsoluteDifferenceCalculator {

  public <T> BigDecimal calculate(Partition<T> partition1, Partition<T> partition2) {
    RBSet<T> allKeys = rbSet(Sets.union(
        partition1.keySet(),
        partition2.keySet()));
    BigDecimal sumOfAbsDiffs = sumBigDecimals(allKeys.asSet()
        .stream()
        .map( key -> {
          UnitFraction unitFraction1 = partition1.getOrZero(key);
          UnitFraction unitFraction2 = partition2.getOrZero(key);
          return unitFraction1.asBigDecimal().subtract(unitFraction2.asBigDecimal()).abs();
        }));
    return sumOfAbsDiffs
        .divide(BigDecimal.valueOf(allKeys.size()), DEFAULT_MATH_CONTEXT);
  }

}
