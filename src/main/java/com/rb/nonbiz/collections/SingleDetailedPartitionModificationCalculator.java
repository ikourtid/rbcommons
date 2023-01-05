package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.DetailedPartitionModification.DetailedPartitionModificationBuilder;
import com.rb.nonbiz.collections.RBMapVisitors.TwoRBMapsVisitor;
import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;
import com.rb.nonbiz.types.UnitFraction;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBMapVisitors.visitItemsOfTwoRBMaps;
import static com.rb.nonbiz.collections.RBVoid.rbVoid;
import static com.rb.nonbiz.math.RBBigDecimals.epsilonCompareBigDecimals;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * Calculates the difference between two partitions.
 *
 * <p> This is similar to {@link SingleDetailedPartitionModificationApplier}, which calculates
 * partition2 = partition1 + detailedModification. Here, we instead calculate
 * detailedModification = partition2 - partition1.
 * </p>
 *
 * <p> The semantics of {@link DetailedPartitionModification} here are more like 'DetailedPartitionDifference',
 * but we will not create a separate class. Even though it's the same thing, 'modification' sounds like something
 * that is applied to a {@link Partition}, whereas 'difference' sounds like the result of looking at two
 * {@link Partition}s. </p>
 */
public class SingleDetailedPartitionModificationCalculator {

  public <T> DetailedPartitionModification<T> calculate(
      Partition<T> partition1,
      Partition<T> partition2) {
    // We can calculate the final sizes with precision, but that requires running set differences etc.,
    // so let's just estimate high, at the expense of using up a bit more memory.
    int sizeHint = (partition1.size() + partition2.size()) / 2;
    MutableRBMap<T, UnitFraction> keysToAdd      = newMutableRBMapWithExpectedSize(sizeHint);
    MutableRBMap<T, UnitFraction> keysToIncrease = newMutableRBMapWithExpectedSize(sizeHint);
    MutableRBMap<T, UnitFraction> keysToRemove   = newMutableRBMapWithExpectedSize(sizeHint);
    MutableRBMap<T, UnitFraction> keysToDecrease = newMutableRBMapWithExpectedSize(sizeHint);

    visitItemsOfTwoRBMaps(
        partition1.getRawFractionsMap(),
        partition2.getRawFractionsMap(),
        new TwoRBMapsVisitor<T, UnitFraction, UnitFraction>() {
          @Override
          public void visitItemInLeftMapOnly(T keyInLeftMapOnly, UnitFraction valueInLeftMapOnly) {
            // partition2 doesn't even have an entry for this key, so this is like a removal.
            keysToRemove.putAssumingAbsent(keyInLeftMapOnly, valueInLeftMapOnly);
          }

          @Override
          public void visitItemInRightMapOnly(T keyInRightMapOnly, UnitFraction valueInRightMapOnly) {
            // partition1 doesn't have an entry for this key, and partition2 adds this.
            keysToAdd.putAssumingAbsent(keyInRightMapOnly, valueInRightMapOnly);
          }

          @Override
          public void visitItemInBothMaps(T keyInBothMaps, UnitFraction valueInLeftMap, UnitFraction valueInRightMap) {
            epsilonCompareBigDecimals(
                valueInLeftMap.asBigDecimal(),
                valueInRightMap.asBigDecimal(),
                DEFAULT_EPSILON_1e_8,
                new EpsilonComparisonVisitor<RBVoid>() {
                  @Override
                  public RBVoid visitRightIsGreater(double rightMinusLeft) {
                    keysToIncrease.putAssumingAbsent(keyInBothMaps, unitFraction(rightMinusLeft));
                    return rbVoid();
                  }

                  @Override
                  public RBVoid visitAlmostEqual() {
                    // This partition item is unchanged between partition1 and partition2, so it's neither an
                    // increase/addition nor a decrease/removal. Therefore, there's nothing to do here.
                    return rbVoid();
                  }

                  @Override
                  public RBVoid visitLeftIsGreater(double rightMinusLeft) {
                    keysToDecrease.putAssumingAbsent(keyInBothMaps, unitFraction(-1 * rightMinusLeft));
                    return rbVoid();
                  }
                });
          }
        });

    return DetailedPartitionModificationBuilder.<T>detailedPartitionModificationBuilder()
        .setKeysToAdd(newRBMap(keysToAdd))
        .setKeysToIncrease(newRBMap(keysToIncrease))
        .setKeysToRemove(newRBMap(keysToRemove))
        .setKeysToDecrease(newRBMap(keysToDecrease))
        // Although the following two epsilons are not the result of the calculations above, and they are also not
        // being passed in, we need to set some values to avoid a precondition in the builder, so let's use the
        // default. If, in the future, a user of this class wants to control these epsilons in the returned value,
        // we can turn them into arguments into this method.
        .useStandardEpsilonForNetAdditionSanityCheck()
        .useStandardEpsilonForRemovalSanityChecks()
        .build();
  }

}
