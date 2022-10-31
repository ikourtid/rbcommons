package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.DetailedPartitionModification.DetailedPartitionModificationBuilder;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.PartitionTest.epsilonPartitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class SingleDetailedPartitionModificationApplierTest extends RBTest<SingleDetailedPartitionModificationApplier> {

  @Test
  public void generalCase_addsIncreasesRemovesAndSubtracts() {
    assertThat(
        makeTestObject().apply(
            partition(rbMapOf(
                "k1", unitFraction(0.1),
                "k2", unitFraction(0.2),
                "k3", unitFraction(0.3),
                "k4", unitFraction(0.4))),
            DetailedPartitionModificationBuilder.<String>detailedPartitionModificationBuilder()
                .setKeysToAdd(singletonRBMap(
                    "x", unitFraction(0.04)))
                .setKeysToIncrease(singletonRBMap(
                    "k2", unitFraction(0.07)))
                .setKeysToRemove(singletonRBMap(
                    "k1", unitFraction(0.1))) // 0.1 is the original weight, used for sanity checking purposes
                .setKeysToDecrease(singletonRBMap(
                    "k3", unitFraction(0.01)))
                .useStandardEpsilonForRemovalSanityChecks()
                .useStandardEpsilonForNetAdditionSanityCheck()
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

  @Test
  public void usesEpsilonForRemovals() {
    Function<UnitFraction, Partition<String>> maker = epsilonForRemovals ->
        makeTestObject().apply(
            partition(rbMapOf(
                "k1", unitFraction(0.1),
                "k4", unitFraction(0.4),
                "k5", unitFraction(0.5))),
            DetailedPartitionModificationBuilder.<String>detailedPartitionModificationBuilder()
                .noKeysToAdd()
                .setKeysToIncrease(singletonRBMap(
                    "k5", unitFraction(0.1)))
                // 0.1 is the original weight, used for sanity checking purposes/ using 0.105
                // Since k1 is being removed, the value below is only relevant for sanity checking purposes;
                // it will not affect the final partition.
                .setKeysToRemove(singletonRBMap(
                    "k1", unitFraction(0.10777)))
                .noKeysToDecrease()
                .setEpsilonForRemovalSanityChecks(epsilonForRemovals)
                // Using a large (different) epsilon below, so that the exceptions in this test will not be
                // due to the fact that the total addition and removal amounts aren't the same.
                .setEpsilonForNetAdditionSanityCheck(unitFraction(0.123))
                .build());

    // An epsilon of 1e-8 is too tight, and will cause an exception.
    assertIllegalArgumentException( () -> maker.apply(unitFraction(1e-8)));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(1e-3)));
    Partition<String> doesNotThrow;
    doesNotThrow = maker.apply(unitFraction(1e-2));
    doesNotThrow = maker.apply(unitFraction(1e-1));
  }

  @Test
  public void normalizesPartitionWeightsIfWeightsAreOffFrom100pctAndEpsilonForNetAdditionSanityCheckExists() {
    UnitFraction hugeEpsilon = unitFraction(0.888);
    assertThat(
        makeTestObject().apply(
            partition(rbMapOf(
                "k1", unitFraction(0.1),
                "k4", unitFraction(0.4),
                "k5", unitFraction(0.5))),
            DetailedPartitionModificationBuilder.<String>detailedPartitionModificationBuilder()
                .noKeysToAdd()
                // As it stands, we'd have 40% k4, 80% (0.5 + 0.3) k5 - obviously an extreme scenario, because typically
                // the differences between additions/increases and removals/decreases should be small,
                // off due to rounding usually.
                .setKeysToIncrease(singletonRBMap(
                    "k5", unitFraction(0.3)))
                .setKeysToRemove(singletonRBMap(
                    "k1", unitFraction(0.1)))
                .noKeysToDecrease()
                .setEpsilonForRemovalSanityChecks(hugeEpsilon)
                .setEpsilonForNetAdditionSanityCheck(hugeEpsilon)
                .build()),
        epsilonPartitionMatcher(
            partition(rbMapOf(
                "k4", unitFraction(doubleExplained(0.333333333, 0.4 / (0.4 + (0.3 + 0.5)))),
                "k5", unitFraction(doubleExplained(0.666666666, 0.8 / (0.4 + (0.3 + 0.5)))))),
            1e-8));
  }

  @Override
  protected SingleDetailedPartitionModificationApplier makeTestObject() {
    return new SingleDetailedPartitionModificationApplier();
  }

}
