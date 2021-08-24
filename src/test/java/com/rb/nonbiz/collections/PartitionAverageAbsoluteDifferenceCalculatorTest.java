package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionAverageAbsoluteDifferenceCalculatorTest
    extends RBTest<PartitionAverageAbsoluteDifferenceCalculator> {

  @Test
  public void generalCase_partitionsHaveSomeOverlap_returnsAverage() {
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
        doubleExplained(0.184,
            1 / 5.0 * (0.10 + (0.25 - 0.21) + (0.66 - 0.33) + 0.36 + 0.09)));
  }

  @Test
  public void partitionsAreDisjoint_returnsAverage() {
    assertResult(
        partition(rbMapOf(
            "a1", unitFraction(0.40),
            "b1", unitFraction(0.60))),
        partition(rbMapOf(
            "a2", unitFraction(0.09),
            "b2", unitFraction(0.25),
            "c2", unitFraction(0.66))),
        doubleExplained(0.4,
            1 / 5.0 * (0.40 + 0.60 + 0.09 + 0.25 + 0.66)));
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
        doubleExplained(0.02,
            1 / 4.0 * ( (0.10 - 0.1) + (0.21 - 0.2) + (0.33 - 0.3) + (0.4 - 0.36))));
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

  @Override
  protected PartitionAverageAbsoluteDifferenceCalculator makeTestObject() {
    return new PartitionAverageAbsoluteDifferenceCalculator();
  }

}
