package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.SimplePartitionModification.SimplePartitionModificationBuilder.simplePartitionModificationBuilder;
import static com.rb.nonbiz.collections.SimplePartitionModification.emptySimplePartitionModification;
import static com.rb.nonbiz.collections.SimplePartitionModificationTest.simplePartitionModificationMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class SingleSimplePartitionModificationCalculatorTest
    extends RBTest<SingleSimplePartitionModificationCalculator> {

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
            simplePartitionModificationMatcher(
                simplePartitionModificationBuilder()
                    .setKeysToAddOrIncrease(rbMapOf(
                        "x", unitFraction(0.1),
                        "b", unitFraction(0.02)))
                    .setKeysToRemoveOrDecrease(rbMapOf(
                        "a", unitFraction(0.1),
                        "c", unitFraction(0.02)))
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
        simplePartitionModificationMatcher(
            emptySimplePartitionModification()));
  }

  @Override
  protected SingleSimplePartitionModificationCalculator makeTestObject() {
    return new SingleSimplePartitionModificationCalculator();
  }

}
