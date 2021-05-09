package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.PartitionTest.partitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionExtenderTest extends RBTest<PartitionExtender> {

  @Test
  public void happyPath_newItemReducesPreviousItemsProportionately() {
    assertThat(
        makeTestObject().extend(
            partition(rbMapOf(
                "a", unitFraction(0.25), // e.g. $250
                "b", unitFraction(0.75))), // e.g. $750
            "c",
            unitFraction(0.2)), // add another $250, which will be 0.2 = 20% of the new total of $250 + $750 + $250 = $1250
        partitionMatcher(partition(rbMapOf(
            "a", unitFraction(0.2), // $250 / $1250
            "b", unitFraction(0.6), // $750 / $1250
            "c", unitFraction(0.2))))); // $250 / $1250
    assertThat(
        makeTestObject().extend(
            partition(rbMapOf(
                "a", unitFraction(0.2),
                "b", unitFraction(0.8))),
            "c",
            unitFraction(0.5)),
        partitionMatcher(
            partition(rbMapOf(
                "a", unitFraction(0.1),
                "b", unitFraction(0.4),
                "c", unitFraction(0.5)))));
    assertThat(
        makeTestObject().extend(singletonPartition("a"), "b", unitFraction(0.99)),
        partitionMatcher(partition(rbMapOf(
            "a", unitFraction(0.01),
            "b", unitFraction(0.99)))));
  }

  @Test
  public void newItemAlreadyExists_newFractionAddedToExtended() {
    assertThat(
        makeTestObject().extend(
            partition(rbMapOf(
                "a", unitFraction(0.25),
                "b", unitFraction(0.75))),
            "a",
            unitFraction(0.2)),
        partitionMatcher(partition(rbMapOf(
            "a", unitFraction(doubleExplained(0.4, 0.2 + 0.25 * (1 - 0.2))),
            "b", unitFraction(doubleExplained(0.6, 0.75 * (1 - 0.2)))))));
  }

  @Test
  public void newFractionIs0_throws() {
    assertIllegalArgumentException( () -> makeTestObject().extend(
        partition(rbMapOf(
            "a", unitFraction(0.25),
            "b", unitFraction(0.75))),
        "c",
        UNIT_FRACTION_0));
  }

  @Test
  public void newFractionIs1_throws() {
    assertIllegalArgumentException( () -> makeTestObject().extend(
        partition(rbMapOf(
            "a", unitFraction(0.25),
            "b", unitFraction(0.75))),
        "c",
        UNIT_FRACTION_1));
  }

  @Override
  protected PartitionExtender makeTestObject() {
    return new PartitionExtender();
  }

}
