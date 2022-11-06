package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.DetailedPartitionModification.DetailedPartitionModificationBuilder.detailedPartitionModificationBuilder;
import static com.rb.nonbiz.collections.DetailedPartitionModification.emptyDetailedPartitionModification;
import static com.rb.nonbiz.collections.DetailedPartitionModificationTest.detailedPartitionModificationMatcher;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class SingleDetailedPartitionModificationCalculatorTest
    extends RBTest<SingleDetailedPartitionModificationCalculator> {

  @Test
  public void generalCase_hasAllTypes_AdditionsIncreasesSubtractionsRemovals_plusUntouched() {
    assertThat(
        makeTestObject().calculate(
            partition(rbMapOf(
                "a", unitFraction(0.1),
                "b", unitFraction(0.2),
                "c", unitFraction(0.3),
                "d", unitFraction(0.4))),
            partition(rbMapOf(
                "x", unitFraction(0.1),  // added, and 'a' was removed
                "b", unitFraction(doubleExplained(0.22, 0.2 + 0.02)),  // increased by 0.02 from before
                "c", unitFraction(doubleExplained(0.28, 0.3 - 0.02)),  // decreased by 0.02 from before
                "d", unitFraction(0.4)))), // unchanged
        detailedPartitionModificationMatcher(
            detailedPartitionModificationBuilder()
                .setKeysToAdd(singletonRBMap(
                    "x", unitFraction(0.1)))
                .setKeysToIncrease(singletonRBMap(
                    "b", unitFraction(0.02)))
                .setKeysToRemove(singletonRBMap(
                    "a", unitFraction(0.1)))
                .setKeysToDecrease(singletonRBMap(
                    "c", unitFraction(0.02)))
                .useStandardEpsilonForNetAdditionSanityCheck()
                .useStandardEpsilonForRemovalSanityChecks()
                .build()));
  }

  @Test
  public void trivialCase_partitionsSame_resultShowsNoDifferences() {
    Partition<String> partition = partition(rbMapOf(
        "a", unitFraction(0.1),
        "b", unitFraction(0.2),
        "c", unitFraction(0.3),
        "d", unitFraction(0.4)));

    assertThat(
        "A partition has no difference vs. itself",
        makeTestObject().calculate(partition, partition),
        detailedPartitionModificationMatcher(
            emptyDetailedPartitionModification()));
  }

  @Override
  protected SingleDetailedPartitionModificationCalculator makeTestObject() {
    return new SingleDetailedPartitionModificationCalculator();
  }

}
