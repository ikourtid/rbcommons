package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.SimplePartitionModification.SimplePartitionModificationBuilder;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.PartitionTest.epsilonPartitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class SingleSimplePartitionModificationApplierTest extends RBTest<SingleSimplePartitionModificationApplier> {

  @Test
  public void generalCase_addsIncreasesRemovesAndSubtracts() {
    assertThat(
        makeTestObject().apply(
            partition(rbMapOf(
                "k1", unitFraction(0.1),
                "k2", unitFraction(0.2),
                "k3", unitFraction(0.3),
                "k4", unitFraction(0.4))),
            SimplePartitionModificationBuilder.<String>simplePartitionModificationBuilder()
                .setKeysToAddOrIncrease(rbMapOf(
                    "k2", unitFraction(0.06),
                    "k3", unitFraction(0.08)))
                .setKeysToRemoveOrDecrease(rbMapOf(
                    "k1", unitFraction(0.1), // we must remove k1 altogether, since original weight is also 0.1
                    "k4", unitFraction(0.04)))
                .build()),
        epsilonPartitionMatcher(
            partition(rbMapOf(
                // "k1" - removed
                "k2", unitFraction(doubleExplained(0.26, 0.2 + 0.06)),
                "k3", unitFraction(doubleExplained(0.38, 0.3 + 0.08)),
                "k4", unitFraction(doubleExplained(0.36, 0.4 - 0.04)))), // removed
            DEFAULT_EPSILON_1e_8));
  }

  @Override
  protected SingleSimplePartitionModificationApplier makeTestObject() {
    return new SingleSimplePartitionModificationApplier();
  }

}
