package com.rb.nonbiz.math.stats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rb.biz.types.OnesBasedReturn;
import com.rb.biz.types.SignedMoney;
import com.rb.nonbiz.collections.PreciseValueWeighter;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.rb.biz.types.OnesBasedReturn.onesBasedGain;
import static com.rb.biz.types.OnesBasedReturn.onesBasedLoss;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertEquals;

public class StdDevCalculatorTest extends RBTest<StdDevCalculator> {

  @Test
  public void valuesAreDifferent_weightsAreSame_returnsPlainUnweightedStandardDeviationForPopulation() {
    UnitFraction sameWeight = unitFraction(0.3);
    assertEquals(
        2.5,
        makeTestObject().calculateWeightedStandardDeviationForPopulation(
            ImmutableList.of(signedMoney(4), ZERO_SIGNED_MONEY, signedMoney(-3), signedMoney(1)),
            ImmutableList.of(sameWeight, sameWeight, sameWeight, sameWeight)),
        1e-8);
  }

  @Test
  public void valuesAreSame_weightsAreSame_standardDeviationIsZero() {
    UnitFraction sameWeight = unitFraction(0.3);
    assertEquals(
        0,
        makeTestObject().calculateWeightedStandardDeviationForPopulation(
            ImmutableList.of(signedMoney(4), signedMoney(4), signedMoney(4)),
            ImmutableList.of(sameWeight, sameWeight, sameWeight)),
        1e-8);
  }

  @Test
  public void valuesAreSame_weightsAreDifferent_standardDeviationIsZero() {
    assertEquals(
        0,
        makeTestObject().calculateWeightedStandardDeviationForPopulation(
            ImmutableList.of(signedMoney(4), signedMoney(4), signedMoney(4)),
            ImmutableList.of(UNIT_FRACTION_0, unitFraction(0.1111), unitFraction(0.7777))),
        1e-8);
  }

  @Test
  public void valuesAreDifferent_weightsAreDifferent() {
    List<SignedMoney> values = ImmutableList.of(
        signedMoney(4), ZERO_SIGNED_MONEY, signedMoney(-3), signedMoney(1));
    List<UnitFraction> weights = ImmutableList.of(
        unitFraction(0.5), unitFraction(0.7), unitFraction(0.2), unitFraction(0.6));
    double sumOfWeights = doubleExplained(2, (0.5 + 0.7 + 0.2 + 0.6));
    assertAlmostEquals(
        "unweighted average of 0.5 is irrelevant in this calculation. The relevant weighted average is 1",
        signedMoney(doubleExplained(1, (0.5 * 4 + 0.7 * 0 + 0.2 * (-3) + 0.6 * 1) / sumOfWeights)),
        signedMoney(new PreciseValueWeighter().makeWeightedAverage(values, weights)),
        DEFAULT_EPSILON_1e_8);
    double varianceNumerator = doubleExplained(8.4,
        0.5 * ( 4 - 1) * ( 4 - 1)
      + 0.7 * ( 0 - 1) * ( 0 - 1)
      + 0.2 * (-3 - 1) * (-3 - 1)
      + 0.6 * ( 1 - 1) * ( 1 - 1));
    assertEquals(
        doubleExplained(2.04939015, Math.sqrt(varianceNumerator / sumOfWeights)),
        makeTestObject().calculateWeightedStandardDeviationForPopulation(values, weights),
        1e-8);
  }

  @Test
  public void testPlainUnweightedStandardDeviation() {
    assertEquals(
        2.5,
        makeTestObject().calculateStandardDeviationForPopulationWithOnePass(ImmutableList.of(
            signedMoney(4), ZERO_SIGNED_MONEY, signedMoney(-3), signedMoney(1))),
        1e-8);
  }

  // The "sum of squares" implementation of stdev() has numerical stability problems
  // when the variance is much smaller than the mean.
  @Test
  public void numericalStressTest_largeAverage_smallDeviation_unWeighted() {
    int nItems = 10_000;
    SignedMoney smallOffset = signedMoney(1.234567890123456);   // 16 digits precision
    SignedMoney largeAverage = signedMoney(1e10);
    List<SignedMoney> v = alternatingSample_helper(nItems, smallOffset, largeAverage);

    assertEquals(
        1.234567890123456,
        makeTestObject().calculateStandardDeviationForPopulationWithOnePass(v),
        1e-8);
  }

  // We often take the stddev() of many one-based returns, all of which are close to 1.0
  // This is essentially a rescaling of the above, specialized to our most frequent use case
  @Test
  public void numericalStressTest_smallDeviationsFromFlatReturn_unWeighted() {
    int nItems = 365 * 10;
    double smallMove = 0.00000123456789;
    // Test alternative small positive and negative returns.
    // Since the onesBasedReturns are multiplicative, this will not yield a flat return but a small negative return.
    // However, we want the average onesBasedReturn to be clearly flat, so the stdDev will be clearly 'smallMove'
    OnesBasedReturn smallUp   = onesBasedGain(1.0 + smallMove);
    OnesBasedReturn smallDown = onesBasedLoss(1.0 - smallMove);
    List<OnesBasedReturn> returns = Lists.newArrayListWithExpectedSize(nItems);
    for (int i = 0; i < nItems / 2; ++i) {
      returns.add(smallUp);
      returns.add(smallDown);
    }
    assertEquals(
        0.00000123456789,
        makeTestObject().calculateStandardDeviationForPopulationWithOnePass(returns),
        1e-12);
  }

  @Test
  public void numericalStressTest_largeAverage_smallDeviation_sameWeights() {
    int nItems = 10_000;
    SignedMoney smallOffset = signedMoney(1.234567890123456);
    SignedMoney largeAverage = signedMoney(1e10);
    List<SignedMoney> values = alternatingSample_helper(nItems, smallOffset, largeAverage);
    for (UnitFraction sameWeight : ImmutableList.of(
        unitFraction(1e-8),
        unitFraction(1e-4),
        unitFraction(0.01),
        unitFraction(0.3),
        UNIT_FRACTION_1)) {
      List<UnitFraction> weights = Collections.nCopies(nItems, sameWeight);

      assertEquals(
          1.234567890123456,
          makeTestObject().calculateWeightedStandardDeviationForPopulation(values, weights),
          1e-8);
    }
  }

  // Weights of very varying sizes can also introduce numerical problems
  @Test
  public void numericalStressTest_largeAverage_smallDeviation_decreasingWeights() {
    int nItems = 1_000;
    SignedMoney smallOffset = signedMoney(1.234567890123456);
    SignedMoney largeAverage = signedMoney(1e10);
    List<SignedMoney> values = alternatingSample_helper(nItems, smallOffset, largeAverage);
    double weight = 1.0;
    double weightRatio = 0.8;
    List<UnitFraction> weights = Lists.newArrayListWithExpectedSize(nItems);
    // Set up decreasing pairs of weights [1.0, 1.0, 0.8, 0.8, 0.64, 0.64, ...]
    // The value offsets alternate in sign, so the pairs of weight * value will cancel when calculating the average
    for (int i = 0; i < nItems; i++) {
      weights.add(unitFraction(weight));
      if (i % 2 == 0) {
        continue;
      }
      weight *= weightRatio;
    }

    assertEquals(
        1.234567890123456,
        makeTestObject().calculateWeightedStandardDeviationForPopulation(values, weights),
        1e-8);

  }

  private List<SignedMoney> alternatingSample_helper(int nItems, SignedMoney smallOffset, SignedMoney largeAverage) {
    List<SignedMoney> v = Lists.newArrayListWithExpectedSize(nItems);
    for (int i = 0; i < nItems; i++) {
      if (i % 2 == 0) {
        v.add(largeAverage.add(smallOffset));
      } else {
        v.add(largeAverage.subtract(smallOffset));
      }
    }
    return v;
  }

  @Override
  protected StdDevCalculator makeTestObject() {
    StdDevCalculator testObject = new StdDevCalculator();
    testObject.preciseValueWeighter = new PreciseValueWeighter();
    return testObject;
  }

}
