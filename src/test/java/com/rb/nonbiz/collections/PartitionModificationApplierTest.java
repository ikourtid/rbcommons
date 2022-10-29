package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.PartitionModification.PartitionModificationBuilder;
import com.rb.nonbiz.testutils.RBTest;
import org.checkerframework.checker.units.qual.K;
import org.junit.Test;

import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.PartitionTest.epsilonPartitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionModificationApplierTest extends RBTest<PartitionModificationApplier> {

  @Test
  public void generalCase_addsIncreasesRemovesAndSubtracts() {
    assertThat(
        makeTestObject().apply(
            partition(rbMapOf(
                "k1", unitFraction(0.1),
                "k2", unitFraction(0.2),
                "k3", unitFraction(0.3),
                "k4", unitFraction(0.4))),
            PartitionModificationBuilder.<String>partitionModificationBuilder()
                .setKeysToAdd(singletonRBMap(
                    "x", unitFraction(0.04)))
                .setKeysToIncrease(singletonRBMap(
                    "k2", unitFraction(0.07)))
                .setKeysToRemove(singletonRBMap(
                    "k1", unitFraction(0.1))) // 0.1 is the original weight, used for sanity checking purposes
                .setKeysToDecrease(singletonRBMap(
                    "k3", unitFraction(0.01)))
                .build()),
        epsilonPartitionMatcher(
            partition(rbMapOf(
                "x",  unitFraction(0.04),                              // added
                // k1 was removed
                "k2", unitFraction(doubleExplained(0.27, 0.2 + 0.07)), // increased by 0.07
                "k3", unitFraction(doubleExplained(0.29, 0.3 - 0.01)), // decreased by 0.01
                "k4", unitFraction(0.4))),                             // untouched
            1e-8));
  }

  @Override
  protected PartitionModificationApplier makeTestObject() {
    return new PartitionModificationApplier();
  }
  
}
