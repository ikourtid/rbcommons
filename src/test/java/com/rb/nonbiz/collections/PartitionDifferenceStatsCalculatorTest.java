package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.biz.marketdata.FakeInstruments.*;
import static com.rb.nonbiz.collections.HasInstrumentIdMaps.hasInstrumentIdMapOf;
import static com.rb.nonbiz.collections.HasInstrumentIdPartition.hasInstrumentIdPartition;
import static com.rb.nonbiz.collections.HasInstrumentIdPartition.singletonHasInstrumentIdPartition;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentId;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionDifferenceStatsCalculatorTest
    extends RBTest<PartitionDifferenceStatsCalculator> {

  @Test
  public void generalCase_partitionsHaveSomeOverlap_returnsSum() {
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
        doubleExplained(0.92, 0.10 + (0.25 - 0.21) + (0.66 - 0.33) + 0.36 + 0.09));
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
        0.92);
  }

  @Test
  public void partitionsAreDisjoint_returns2() {
    assertResult(
        partition(rbMapOf(
            "a1", unitFraction(0.40),
            "b1", unitFraction(0.60))),
        partition(rbMapOf(
            "a2", unitFraction(0.09),
            "b2", unitFraction(0.25),
            "c2", unitFraction(0.66))),
        doubleExplained(2, 0.40 + 0.60 + 0.09 + 0.25 + 0.66));
    assertResult(
        hasInstrumentIdPartition(hasInstrumentIdMapOf(
            testHasInstrumentId(STOCK_A1, DUMMY_DOUBLE), unitFraction(0.40),
            testHasInstrumentId(STOCK_B1, DUMMY_DOUBLE), unitFraction(0.60))),
        hasInstrumentIdPartition(hasInstrumentIdMapOf(
            testHasInstrumentId(STOCK_A2, DUMMY_DOUBLE), unitFraction(0.09),
            testHasInstrumentId(STOCK_B2, DUMMY_DOUBLE), unitFraction(0.25),
            testHasInstrumentId(STOCK_C2, DUMMY_DOUBLE), unitFraction(0.66))),
        2.0);
  }

  @Test
  public void sameKeys() {
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
        doubleExplained(0.08, (0.10 - 0.1) + (0.21 - 0.2) + (0.33 - 0.3) + (0.4 - 0.36)));
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
        0.08);
  }

  @Test
  public void sameKeys_returns0() {
    assertResult(
        singletonPartition("x"),
        singletonPartition("x"),
        0);
    assertResult(
        singletonHasInstrumentIdPartition(testHasInstrumentId(STOCK_A, DUMMY_DOUBLE)),
        singletonHasInstrumentIdPartition(testHasInstrumentId(STOCK_A, DUMMY_DOUBLE)),
        0);
  }

  @Test
  public void standardPartition_singletonPartition_differentKeys_returns2() {
    assertResult(
        singletonPartition("x"),
        singletonPartition("y"),
        doubleExplained(2, (1.0 - 0.0) + (1.0 - 0.0)));
    assertResult(
        singletonHasInstrumentIdPartition(testHasInstrumentId(STOCK_A, DUMMY_DOUBLE)),
        singletonHasInstrumentIdPartition(testHasInstrumentId(STOCK_B, DUMMY_DOUBLE)),
        2);
  }

  private void assertResult(Partition<String> partition1, Partition<String> partition2, double expectedResult) {
    assertThat(
        makeTestObject().calculate(partition1, partition2),
        bigDecimalMatcher(
            BigDecimal.valueOf(expectedResult),
            1e-8));
    assertThat(
        makeTestObject().calculate(partition2, partition1),
        bigDecimalMatcher(
            BigDecimal.valueOf(expectedResult),
            1e-8));

    // Although this isn't the job of this method, let's also assert that the difference of any partition and itself
    // also produces zero.
    assertThat(
        makeTestObject().calculate(partition1, partition1),
        bigDecimalMatcher(BigDecimal.ZERO, 1e-8));
    assertThat(
        makeTestObject().calculate(partition2, partition2),
        bigDecimalMatcher(BigDecimal.ZERO, 1e-8));
  }

  private void assertResult(
      HasInstrumentIdPartition<TestHasInstrumentId> partition1,
      HasInstrumentIdPartition<TestHasInstrumentId> partition2,
      double expectedResult) {
    assertThat(
        makeTestObject().calculate(partition1, partition2),
        bigDecimalMatcher(
            BigDecimal.valueOf(expectedResult),
            1e-8));
    assertThat(
        makeTestObject().calculate(partition2, partition1),
        bigDecimalMatcher(
            BigDecimal.valueOf(expectedResult),
            1e-8));

    // Although this isn't the job of this method, let's also assert that the difference of any partition and itself
    // also produces zero.
    assertThat(
        makeTestObject().calculate(partition1, partition1),
        bigDecimalMatcher(BigDecimal.ZERO, 1e-8));
    assertThat(
        makeTestObject().calculate(partition2, partition2),
        bigDecimalMatcher(BigDecimal.ZERO, 1e-8));
  }

  @Override
  protected PartitionDifferenceStatsCalculator makeTestObject() {
    return new PartitionDifferenceStatsCalculator();
  }

}
