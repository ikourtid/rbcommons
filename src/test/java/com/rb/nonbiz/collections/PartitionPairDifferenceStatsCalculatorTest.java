package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.*;
import static com.rb.nonbiz.collections.HasInstrumentIdMaps.hasInstrumentIdMapOf;
import static com.rb.nonbiz.collections.HasInstrumentIdPartition.hasInstrumentIdPartition;
import static com.rb.nonbiz.collections.HasInstrumentIdPartition.singletonHasInstrumentIdPartition;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.PartitionPairDifferenceStatsTest.partitionPairDifferenceStatsMatcher;
import static com.rb.nonbiz.collections.PartitionPairDifferenceStatsTest.partitionPairDifferenceStatsWhenNoDifferences;
import static com.rb.nonbiz.collections.PartitionPairDifferenceStatsTest.singletonPartitionPairDifferenceStats;
import static com.rb.nonbiz.collections.PartitionPairDifferenceStatsTest.testPartitionPairDifferenceStats;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentId;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionPairDifferenceStatsCalculatorTest extends RBTest<PartitionPairDifferenceStatsCalculator> {

  @Test
  public void generalCase_partitionsHaveSomeOverlap_returnsSum() {
    PartitionPairDifferenceStats expectedResult = testPartitionPairDifferenceStats(
        // a1   b           c             d1    a2; also
        // A1   STOCK_B     STOCK_C       D1    A2
        -0.10, 0.25 - 0.21, 0.66 - 0.33, -0.36, 0.09);
    assertResult(
        partition(rbMapOf(
            "a1", unitFraction(0.10),
            "b", unitFraction(0.21),
            "c", unitFraction(0.33),
            "d1", unitFraction(0.36))),
        partition(rbMapOf(
            "a2", unitFraction(0.09),
            "b", unitFraction(0.25),
            "c", unitFraction(0.66))),
        expectedResult); // a1, b, c, d1, a2, respectively
    assertResult(
        hasInstrumentIdPartition(hasInstrumentIdMapOf(
            testHasInstrumentId(STOCK_A1, DUMMY_DOUBLE), unitFraction(0.10),
            testHasInstrumentId(STOCK_B,  DUMMY_DOUBLE), unitFraction(0.21),
            testHasInstrumentId(STOCK_C,  DUMMY_DOUBLE), unitFraction(0.33),
            testHasInstrumentId(STOCK_D1, DUMMY_DOUBLE), unitFraction(0.36))),
        hasInstrumentIdPartition(hasInstrumentIdMapOf(
            testHasInstrumentId(STOCK_A2, DUMMY_DOUBLE), unitFraction(0.09),
            testHasInstrumentId(STOCK_B,  DUMMY_DOUBLE), unitFraction(0.25),
            testHasInstrumentId(STOCK_C,  DUMMY_DOUBLE), unitFraction(0.66))),
        expectedResult);
  }

  @Test
  public void partitionsAreDisjoint_returns2() {
    PartitionPairDifferenceStats expectedResult = testPartitionPairDifferenceStats(
        // a1     b1    a2    b2    c2; likewise for the 2nd partition
        -0.40, -0.60, 0.09, 0.25, 0.66);
    assertResult(
        partition(rbMapOf(
            "a1", unitFraction(0.40),
            "b1", unitFraction(0.60))),
        partition(rbMapOf(
            "a2", unitFraction(0.09),
            "b2", unitFraction(0.25),
            "c2", unitFraction(0.66))),
        expectedResult);
    assertResult(
        hasInstrumentIdPartition(hasInstrumentIdMapOf(
            testHasInstrumentId(STOCK_A1, DUMMY_DOUBLE), unitFraction(0.40),
            testHasInstrumentId(STOCK_B1, DUMMY_DOUBLE), unitFraction(0.60))),
        hasInstrumentIdPartition(hasInstrumentIdMapOf(
            testHasInstrumentId(STOCK_A2, DUMMY_DOUBLE), unitFraction(0.09),
            testHasInstrumentId(STOCK_B2, DUMMY_DOUBLE), unitFraction(0.25),
            testHasInstrumentId(STOCK_C2, DUMMY_DOUBLE), unitFraction(0.66))),
        expectedResult);
  }

  @Test
  public void sameKeys() {
    PartitionPairDifferenceStats expectedResult = testPartitionPairDifferenceStats(
        // a1      a2          a3          a4          ; likewise for the 2nd partition
        0.1 - 0.1, 0.2 - 0.21, 0.3 - 0.33, 0.4 - 0.36);
    assertResult(
        partition(rbMapOf(
            "a", unitFraction(0.10),
            "b", unitFraction(0.21),
            "c", unitFraction(0.33),
            "d", unitFraction(0.36))),
        partition(rbMapOf(
            "a", unitFraction(0.1),
            "b", unitFraction(0.2),
            "c", unitFraction(0.3),
            "d", unitFraction(0.4))),
        expectedResult);
    assertResult(
        hasInstrumentIdPartition(hasInstrumentIdMapOf(
            testHasInstrumentId(STOCK_A, DUMMY_DOUBLE), unitFraction(0.10),
            testHasInstrumentId(STOCK_B, DUMMY_DOUBLE), unitFraction(0.21),
            testHasInstrumentId(STOCK_C, DUMMY_DOUBLE), unitFraction(0.33),
            testHasInstrumentId(STOCK_D, DUMMY_DOUBLE), unitFraction(0.36))),
        hasInstrumentIdPartition(hasInstrumentIdMapOf(
            testHasInstrumentId(STOCK_A, DUMMY_DOUBLE), unitFraction(0.1),
            testHasInstrumentId(STOCK_B, DUMMY_DOUBLE), unitFraction(0.2),
            testHasInstrumentId(STOCK_C, DUMMY_DOUBLE), unitFraction(0.3),
            testHasInstrumentId(STOCK_D, DUMMY_DOUBLE), unitFraction(0.4))),
        expectedResult);
  }

  @Test
  public void sameKeys_returns0() {
    assertResult(
        singletonPartition("x"),
        singletonPartition("x"),
        singletonPartitionPairDifferenceStats(0.0));
    assertResult(
        singletonHasInstrumentIdPartition(testHasInstrumentId(STOCK_A, DUMMY_DOUBLE)),
        singletonHasInstrumentIdPartition(testHasInstrumentId(STOCK_A, DUMMY_DOUBLE)),
        singletonPartitionPairDifferenceStats(0.0));
  }

  @Test
  public void standardPartition_singletonPartition_differentKeys_returns2() {
    PartitionPairDifferenceStats expectedRest = testPartitionPairDifferenceStats(-1.0, 1.0);
    assertResult(
        singletonPartition("x"),
        singletonPartition("y"),
        expectedRest);
    assertResult(
        singletonHasInstrumentIdPartition(testHasInstrumentId(STOCK_A, DUMMY_DOUBLE)),
        singletonHasInstrumentIdPartition(testHasInstrumentId(STOCK_B, DUMMY_DOUBLE)),
        expectedRest);
  }

  private void assertResult(
      Partition<String> partition1,
      Partition<String> partition2,
      PartitionPairDifferenceStats expectedResult) {
    assertThat(
        makeTestObject().calculate(partition1, partition2),
        partitionPairDifferenceStatsMatcher(expectedResult));
    // The following does not hold because the overweight & underweight items would be flipped.
    //    assertThat(
    //        makeTestObject().calculate(partition2, partition1),
    //        partitionPairDifferenceStatsMatcher(expectedResult));

    // Although this isn't the job of this method, let's also assert that the difference of any partition and itself
    // also produces zero.
    assertThat(
        makeTestObject().calculate(partition1, partition1),
        partitionPairDifferenceStatsMatcher(partitionPairDifferenceStatsWhenNoDifferences(partition1.size())));
    assertThat(
        makeTestObject().calculate(partition2, partition2),
        partitionPairDifferenceStatsMatcher(partitionPairDifferenceStatsWhenNoDifferences(partition2.size())));
  }

  private void assertResult(
      HasInstrumentIdPartition<TestHasInstrumentId> partition1,
      HasInstrumentIdPartition<TestHasInstrumentId> partition2,
      PartitionPairDifferenceStats expectedResult) {
    assertThat(
        makeTestObject().calculate(partition1, partition2),
        partitionPairDifferenceStatsMatcher(expectedResult));
    // The following does not hold because the overweight & underweight items would be flipped.
    //    assertThat(
    //        makeTestObject().calculate(partition2, partition1),
    //        partitionPairDifferenceStatsMatcher(expectedResult));

    // Although this isn't the job of this method, let's also assert that the difference of any partition and itself
    // also produces zero.
    assertThat(
        makeTestObject().calculate(partition1, partition1),
        partitionPairDifferenceStatsMatcher(partitionPairDifferenceStatsWhenNoDifferences(partition1.size())));
    assertThat(
        makeTestObject().calculate(partition2, partition2),
        partitionPairDifferenceStatsMatcher(partitionPairDifferenceStatsWhenNoDifferences(partition2.size())));
  }

  @Override
  protected PartitionPairDifferenceStatsCalculator makeTestObject() {
    return new PartitionPairDifferenceStatsCalculator();
  }

}
