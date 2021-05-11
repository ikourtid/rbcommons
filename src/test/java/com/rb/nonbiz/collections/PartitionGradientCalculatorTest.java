package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBCommonsTestPlusIntegration;
import com.rb.nonbiz.types.UnitFraction;
import org.jmock.Expectations;
import org.junit.Test;

import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.PartitionGradient.partitionGradient;
import static com.rb.nonbiz.collections.PartitionGradientTest.partitionGradientMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionGradientCalculatorTest extends RBCommonsTestPlusIntegration<PartitionGradientCalculator> {

  PartitionExtender partitionExtender =
      mockery.mock(PartitionExtender.class);
  PartitionUnextender partitionUnextender =
      mockery.mock(PartitionUnextender.class);

  private final Partition<String> UNBUMPED = partition(rbMapOf(
      "a", unitFraction(0.2),
      "b", unitFraction(0.3),
      "c", unitFraction(0.5)));

  @Test
  public void allItemsCanBeBumpedDown() {
    doubleExplained(0.99, 1 - 0.01);
    Partition<String> bumpedUpA = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.208, 0.2 * 0.99 + 0.01)),
        "b", unitFraction(doubleExplained(0.297, 0.3 / (0.3 + 0.5) * (1 - 0.208))),
        "c", unitFraction(doubleExplained(0.495, 0.5 / (0.3 + 0.5) * (1 - 0.208)))));
    Partition<String> bumpedUpB = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.198, 0.2 / (0.2 + 0.5) * (1 - 0.307))),
        "b", unitFraction(doubleExplained(0.307, 0.3 * 0.99 + 0.01)),
        "c", unitFraction(doubleExplained(0.495, 0.5 / (0.2 + 0.5) * (1 - 0.307)))));
    Partition<String> bumpedUpC = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.198, 0.2 / (0.2 + 0.3) * (1 - 0.505))),
        "b", unitFraction(doubleExplained(0.297, 0.3 / (0.2 + 0.3) * (1 - 0.505))),
        "c", unitFraction(doubleExplained(0.505, 0.5 * 0.99 + 0.01))));
    Partition<String> bumpedDownA = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.191919191, (0.2 - 0.01) / 0.99)),
        "b", unitFraction(doubleExplained(0.303030303, 0.3 / (0.3 + 0.5) * (1 - 0.1919191919))),
        "c", unitFraction(doubleExplained(0.505050505, 0.5 / (0.3 + 0.5) * (1 - 0.1919191919)))));
    Partition<String> bumpedDownB = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.202020202, 0.2 / (0.2 + 0.5) * (1 - 0.292929293))),
        "b", unitFraction(doubleExplained(0.292929293, (0.3 - 0.01) / 0.99)),
        "c", unitFraction(doubleExplained(0.505050505, 0.5 / (0.2 + 0.5) * (1 - 0.292929293)))));
    Partition<String> bumpedDownC = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.202020202, 0.2 / (0.2 + 0.3) * (1 - 0.494949495))),
        "b", unitFraction(doubleExplained(0.303030303, 0.3 / (0.2 + 0.3) * (1 - 0.494949495))),
        "c", unitFraction(doubleExplained(0.494949495, (0.5 - 0.01) / 0.99))));

    expectBumpingUp("a", unitFraction(0.01), bumpedUpA);
    expectBumpingUp("b", unitFraction(0.01), bumpedUpB);
    expectBumpingUp("c", unitFraction(0.01), bumpedUpC);

    expectBumpingDown("a", unitFraction(0.01), bumpedDownA);
    expectBumpingDown("b", unitFraction(0.01), bumpedDownB);
    expectBumpingDown("c", unitFraction(0.01), bumpedDownC);

    for (PartitionGradientCalculator calculator : rbSetOf(makeRealObject(), makeTestObject())) {
      assertThat(
          calculator.calculatePartitionGradient(UNBUMPED, unitFraction(0.01)),
          partitionGradientMatcher(
              partitionGradient(
                  UNBUMPED,
                  unitFraction(0.01),
                  rbMapOf(
                      "a", bumpedUpA,
                      "b", bumpedUpB,
                      "c", bumpedUpC),
                  rbMapOf(
                      "a", bumpedDownA,
                      "b", bumpedDownB,
                      "c", bumpedDownC))));
    }
  }

  @Test
  public void someItemsCannotBeBumpedDown() {
    doubleExplained(0.79, 1 - 0.21);
    Partition<String> bumpedUpA = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.368, 0.2 * 0.79 + 0.21)),
        "b", unitFraction(doubleExplained(0.237, 0.3 / (0.3 + 0.5) * (1 - 0.368))),
        "c", unitFraction(doubleExplained(0.395, 0.5 / (0.3 + 0.5) * (1 - 0.368)))));
    Partition<String> bumpedUpB = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.158, 0.2 / (0.2 + 0.5) * (1 - 0.447))),
        "b", unitFraction(doubleExplained(0.447, 0.3 * 0.79 + 0.21)),
        "c", unitFraction(doubleExplained(0.395, 0.5 / (0.2 + 0.5) * (1 - 0.447)))));
    Partition<String> bumpedUpC = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.158, 0.2 / (0.2 + 0.3) * (1 - 0.605))),
        "b", unitFraction(doubleExplained(0.237, 0.3 / (0.2 + 0.3) * (1 - 0.605))),
        "c", unitFraction(doubleExplained(0.605, 0.5 * 0.79 + 0.21))));
    Partition<String> bumpedDownB = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.253164557, 0.2 / (0.2 + 0.5) * (1 - 0.113924051))),
        "b", unitFraction(doubleExplained(0.113924051, (0.3 - 0.21) / 0.79)),
        "c", unitFraction(doubleExplained(0.632911392, 0.5 / (0.2 + 0.5) * (1 - 0.113924051)))));
    Partition<String> bumpedDownC = partition(rbMapOf(
        "a", unitFraction(doubleExplained(0.253164557, 0.2 / (0.2 + 0.3) * (1 - 0.367088608))),
        "b", unitFraction(doubleExplained(0.379746835, 0.3 / (0.2 + 0.3) * (1 - 0.367088608))),
        "c", unitFraction(doubleExplained(0.367088608, (0.5 - 0.21) / 0.79))));

    expectBumpingUp("a", unitFraction(0.21), bumpedUpA);
    expectBumpingUp("b", unitFraction(0.21), bumpedUpB);
    expectBumpingUp("c", unitFraction(0.21), bumpedUpC);

    expectBumpingDown("b", unitFraction(0.21), bumpedDownB);
    expectBumpingDown("c", unitFraction(0.21), bumpedDownC);

    for (PartitionGradientCalculator calculator : rbSetOf(makeRealObject(), makeTestObject())) {
      assertThat(
          calculator.calculatePartitionGradient(UNBUMPED, unitFraction(0.21)),
          partitionGradientMatcher(
              partitionGradient(
                  UNBUMPED,
                  unitFraction(0.21),
                  rbMapOf(
                      "a", bumpedUpA,
                      "b", bumpedUpB,
                      "c", bumpedUpC),
                  rbMapOf(
                      "b", bumpedDownB,
                      "c", bumpedDownC))));
    }
  }

  private void expectBumpingUp(String keyToBumpUp, UnitFraction bumpAmount, Partition<String> expectedResult) {
    mockery.checking(new Expectations() {{
      oneOf(partitionExtender).extend(
          with(same(UNBUMPED)),
          with(equal(keyToBumpUp)),
          with(preciseValueMatcher(bumpAmount, 1e-8)));
      will(returnValue(expectedResult));
    }});
  }

  private void expectBumpingDown(String keyToBumpDown, UnitFraction bumpAmount, Partition<String> expectedResult) {
    mockery.checking(new Expectations() {{
      oneOf(partitionUnextender).unextend(
          with(same(UNBUMPED)),
          with(equal(keyToBumpDown)),
          with(preciseValueMatcher(bumpAmount, 1e-8)));
      will(returnValue(expectedResult));
    }});
  }

  @Override
  protected Class<PartitionGradientCalculator> getClassBeingTested() {
    return PartitionGradientCalculator.class;
  }

  @Override
  protected PartitionGradientCalculator makeTestObject() {
    PartitionGradientCalculator testObject = new PartitionGradientCalculator();
    testObject.partitionExtender = partitionExtender;
    testObject.partitionUnextender = partitionUnextender;
    return testObject;
  }

}
