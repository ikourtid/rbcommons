package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.RBMapVisitors.TwoRBMapsVisitor;
import com.rb.nonbiz.collections.SimplePartitionModification.SimplePartitionModificationBuilder;
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
 * <p> This is similar to {@link SingleSimplePartitionModificationApplier}, which calculates
 * partition2 = partition1 + simpleModification. Here, we instead calculate
 * simpleModification = partition2 - partition1.
 * </p>
 *
 * <p> The semantics of {@link SimplePartitionModification} here are more like 'SimplePartitionDifference',
 * but we will not create a separate class. Even though it's the same thing, 'modification' sounds like something
 * that is applied to a {@link Partition}, whereas 'difference' sounds like the result of looking at two
 * {@link Partition}s. </p>
 */
public class SingleSimplePartitionModificationCalculator {

  public <T> SimplePartitionModification<T> calculate(
      Partition<T> partition1,
      Partition<T> partition2) {
    // We can calculate the final sizes with precision, but that requires running set differences etc.,
    // so let's just estimate high, at the expense of using up a bit more memory.
    int sizeHint = partition1.size() + partition2.size();
    MutableRBMap<T, UnitFraction> keysToAddOrIncrease    = newMutableRBMapWithExpectedSize(sizeHint);
    MutableRBMap<T, UnitFraction> keysToRemoveOrDecrease = newMutableRBMapWithExpectedSize(sizeHint);

    visitItemsOfTwoRBMaps(
        partition1.getRawFractionsMap(),
        partition2.getRawFractionsMap(),
        new TwoRBMapsVisitor<T, UnitFraction, UnitFraction>() {
          @Override
          public void visitItemInLeftMapOnly(T keyInLeftMapOnly, UnitFraction valueInLeftMapOnly) {
            // partition2 doesn't even have an entry for this key, so this is like a removal.
            keysToRemoveOrDecrease.putAssumingAbsent(keyInLeftMapOnly, valueInLeftMapOnly);
          }

          @Override
          public void visitItemInRightMapOnly(T keyInRightMapOnly, UnitFraction valueInRightMapOnly) {
            // partition1 doesn't have an entry for this key, and partition2 adds this.
            keysToAddOrIncrease.putAssumingAbsent(keyInRightMapOnly, valueInRightMapOnly);
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
                    keysToAddOrIncrease.putAssumingAbsent(keyInBothMaps, unitFraction(rightMinusLeft));
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
                    keysToRemoveOrDecrease.putAssumingAbsent(keyInBothMaps, unitFraction(-1 * rightMinusLeft));
                    return rbVoid();
                  }
                });
          }
        });

    return SimplePartitionModificationBuilder.<T>simplePartitionModificationBuilder()
        .setKeysToAddOrIncrease(newRBMap(keysToAddOrIncrease))
        .setKeysToRemoveOrDecrease(newRBMap(keysToRemoveOrDecrease))
        .build();
  }

}
