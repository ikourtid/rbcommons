package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapMergers.mergeRBMapsByValue;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * Computes the weighted average of 2 partitions.
 * e.g. $3,000 * (40% A, 60% B) + $7,000 * (10% cash, 90% A)
 * will give (7% cash, 75% A, 18% B)
 */
public class PartitionMerger {

  public <T, V extends PreciseValue<V>> Partition<T> mergePartitions(
      V weight1, Partition<T> partition1,
      V weight2, Partition<T> partition2) {
    BigDecimal weight1bd = weight1.asBigDecimal();
    BigDecimal weight2bd = weight2.asBigDecimal();
    RBPreconditions.checkArgument(
        weight1bd.compareTo(BigDecimal.ZERO) >= 0,
        "You have a negative 1st weight; %s * %s + %s * %s",
        weight1, partition1, weight2, partition2);
    RBPreconditions.checkArgument(
        weight2bd.compareTo(BigDecimal.ZERO) >= 0,
        "You have a negative 2nd weight; %s * %s + %s * %s",
        weight1, partition1, weight2, partition2);
    boolean almostZero1 = weight1.isAlmostZero(1e-8);
    boolean almostZero2 = weight2.isAlmostZero(1e-8);
    RBPreconditions.checkArgument(
        !(almostZero1 && almostZero2),
        "Both weights were 0 (or almost 0); %s * %s + %s * %s",
        weight1, partition1, weight2, partition2);
    if (almostZero1) {
      return partition2;
    } else if (almostZero2) {
      return partition1;
    }
    // general case
    BigDecimal denominator = weight1bd.add(weight2bd);
    UnitFraction unitFraction1 = unitFraction(weight1bd.divide(denominator, DEFAULT_MATH_CONTEXT));
    UnitFraction unitFraction2 = unitFraction(weight2bd.divide(denominator, DEFAULT_MATH_CONTEXT));
    return partition(mergeRBMapsByValue(
        (uf1, uf2) -> UnitFraction.sum(ImmutableList.of(
            uf1.multiply(unitFraction1),
            uf2.multiply(unitFraction2))),
        uf1 -> uf1.multiply(unitFraction1),
        uf2 -> uf2.multiply(unitFraction2),
        partition1.getRawFractionsMap(),
        partition2.getRawFractionsMap()));
  }

}
