package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.SignedFraction;
import com.rb.nonbiz.types.UnitFraction;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.Deviations.deviations;
import static com.rb.nonbiz.collections.NonZeroDeviations.nonZeroDeviations;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapMergers.mergeRBMapsByValue;
import static com.rb.nonbiz.collections.RBStreams.sumAsBigDecimals;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

public class Partitions {

  /**
   * This is useful for generating a partition when we have a fractions that sum to APPROXIMATELY 1
   * (subject to a small but not tiny epsilon), but not necessariliy exactly (i.e. tiny epsilon).
   */
  public static <K> Partition<K> partitionFromApproximateFractions(RBMap<K, UnitFraction> approximatePartition, Epsilon epsilon) {
    BigDecimal sumOfFractions = sumAsBigDecimals(approximatePartition.values());
    if (!epsilon.valuesAreWithin(sumOfFractions.doubleValue(), 1)) {
      throw new IllegalArgumentException(Strings.format(
          "Fractions add up to %s which is too far from 1 within an epsilon of %s : %s",
          sumOfFractions, epsilon, approximatePartition));
    }
    return partition(approximatePartition.transformValuesCopy(weight -> weight.divide(sumOfFractions)));
  }

  public static <K> Deviations<K> calculatePartitionDeviations(Partition<K> partitionA, Partition<K> partitionB) {
    return deviations(calculateFractionsForDifference(partitionA.toSignedPartition(), partitionB.toSignedPartition()));
  }

  public static <K> Deviations<K> calculatePartitionDeviations(Partition<K> partitionA, SignedPartition<K> partitionB) {
    return deviations(calculateFractionsForDifference(partitionA.toSignedPartition(), partitionB));
  }

  public static <K> Deviations<K> calculatePartitionDeviations(SignedPartition<K> partitionA, Partition<K> partitionB) {
    return deviations(calculateFractionsForDifference(partitionA, partitionB.toSignedPartition()));
  }

  public static <K> Deviations<K> calculatePartitionDeviations(SignedPartition<K> partitionA, SignedPartition<K> partitionB) {
    return deviations(calculateFractionsForDifference(partitionA, partitionB));
  }

  public static <K> NonZeroDeviations<K> calculatePartitionNonZeroDeviations(Partition<K> partitionA, Partition<K> partitionB) {
    return nonZeroDeviations(calculateFractionsForDifference(partitionA.toSignedPartition(), partitionB.toSignedPartition())
        .filterValues(signedFraction -> !signedFraction.isAlmostZero(DEFAULT_EPSILON_1e_8)));
  }

  public static <K> NonZeroDeviations<K> calculatePartitionNonZeroDeviations(SignedPartition<K> partitionA, Partition<K> partitionB) {
    return nonZeroDeviations(calculateFractionsForDifference(partitionA, partitionB.toSignedPartition())
        .filterValues(signedFraction -> !signedFraction.isAlmostZero(DEFAULT_EPSILON_1e_8)));
  }

  public static <K> NonZeroDeviations<K> calculatePartitionNonZeroDeviations(Partition<K> partitionA, SignedPartition<K> partitionB) {
    return nonZeroDeviations(calculateFractionsForDifference(partitionA.toSignedPartition(), partitionB)
        .filterValues(signedFraction -> !signedFraction.isAlmostZero(DEFAULT_EPSILON_1e_8)));
  }

  public static <K> NonZeroDeviations<K> calculatePartitionNonZeroDeviations(SignedPartition<K> partitionA, SignedPartition<K> partitionB) {
    return nonZeroDeviations(calculateFractionsForDifference(partitionA, partitionB)
        .filterValues(signedFraction -> !signedFraction.isAlmostZero(DEFAULT_EPSILON_1e_8)));
  }

  private static <K> RBMap<K, SignedFraction> calculateFractionsForDifference(
      SignedPartition<K> partitionA, SignedPartition<K> partitionB) {
    return mergeRBMapsByValue(
        (valueInA, valueInB) -> valueInA.subtract(valueInB),
        valueInA -> valueInA,
        valueInB -> valueInB.negate(),

        partitionA.getRawFractionsMap(),
        partitionB.getRawFractionsMap());
  }

}
