package com.rb.nonbiz.math.eigen;

import com.google.common.collect.ImmutableList;
import com.rb.biz.investing.strategy.optbased.rebal.lp.EigenSubObjectiveInstructionsForCoefficients;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.rb.biz.investing.strategy.optbased.rebal.lp.EigenSubObjectiveInstructionsForCoefficients.EigenSubObjectiveMinimizesStandardDeviation.eigenSubObjectiveMinimizesStandardDeviation;
import static com.rb.biz.investing.strategy.optbased.rebal.lp.EigenSubObjectiveInstructionsForCoefficients.EigenSubObjectiveMinimizesVariance.eigenSubObjectiveMinimizesVariance;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.math.EigenDistance.eigenDistanceInVarianceSpace;
import static com.rb.nonbiz.math.EigenDistanceTest.eigenDistanceMatcher;
import static com.rb.nonbiz.math.EigenDistanceTest.zeroEigenDistance;
import static com.rb.nonbiz.math.eigen.DecreasingPositiveDoublesTest.decreasingPositiveDoubles;
import static com.rb.nonbiz.math.eigen.EigenDistanceCalculationInstructions.eigenDistanceCalculationInstructions;
import static com.rb.nonbiz.math.eigen.FactorLoadings.factorLoadings;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.hamcrest.MatcherAssert.assertThat;

public class DistanceInEigenspaceCalculatorTest extends RBTest<DistanceInEigenspaceCalculator> {

  private final FactorLoadings FACTOR_LOADINGS_A = factorLoadings(50, 60);
  private final FactorLoadings FACTOR_LOADINGS_B = factorLoadings(53, 64);
  private final FactorLoadings FACTOR_LOADINGS_C = factorLoadings(-50, -60);
  private final FactorLoadings FACTOR_LOADINGS_D = factorLoadings(-53, -64);

  private final IidMap<FactorLoadings> FACTOR_LOADINGS_MAP = iidMapOf(
      STOCK_A, FACTOR_LOADINGS_A,
      STOCK_B, FACTOR_LOADINGS_B,
      STOCK_C, FACTOR_LOADINGS_C,
      STOCK_D, FACTOR_LOADINGS_D);

  @Test
  public void factorLengthsUnequal_throws() {
    for (EigenSubObjectiveInstructionsForCoefficients eigenSubObjectiveInstructionsForCoefficients : rbSetOf(
        eigenSubObjectiveMinimizesVariance(),
        eigenSubObjectiveMinimizesStandardDeviation())) {
      assertIllegalArgumentException( () -> makeTestObject().calculateDistanceInSameEigenspace(
          factorLoadings(1.234),
          factorLoadings(2.345, 2.789),
          eigenDistanceCalculationInstructions(
              eigenSubObjectiveInstructionsForCoefficients,
              decreasingPositiveDoubles(DUMMY_DOUBLE, DUMMY_DOUBLE))));
      EigenDistance doesNotThrow = makeTestObject().calculateDistanceInSameEigenspace(
          factorLoadings(1.234, 1.789),
          factorLoadings(2.345, 2.789),
          eigenDistanceCalculationInstructions(
              eigenSubObjectiveInstructionsForCoefficients,
              decreasingPositiveDoubles(DUMMY_DOUBLE, DUMMY_DOUBLE)));
    }
  }

  @Test
  public void tooFewMultipliers_throws() {
    for (EigenSubObjectiveInstructionsForCoefficients eigenSubObjectiveInstructionsForCoefficients : rbSetOf(
        eigenSubObjectiveMinimizesVariance(),
        eigenSubObjectiveMinimizesStandardDeviation())) {
      assertIllegalArgumentException( () -> makeTestObject().calculateDistanceInSameEigenspace(
          FACTOR_LOADINGS_A,
          FACTOR_LOADINGS_B,
          eigenDistanceCalculationInstructions(
              eigenSubObjectiveInstructionsForCoefficients,
              decreasingPositiveDoubles(DUMMY_DOUBLE))));
      // too many multipliers are OK; we may be using only the top n eigenvectors
      EigenDistance doesNotThrow = makeTestObject().calculateDistanceInSameEigenspace(
          FACTOR_LOADINGS_A,
          FACTOR_LOADINGS_B,
          eigenDistanceCalculationInstructions(
              eigenSubObjectiveInstructionsForCoefficients,
              decreasingPositiveDoubles(DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE)));
    }
  }

  @Test
  public void multipliersIncreasing_throws() {
    assertIllegalArgumentException( () -> makeTestObject().calculateDistanceInSameEigenspace(
        FACTOR_LOADINGS_A,
        FACTOR_LOADINGS_B,
        makeInstructions(0.9, 1.0)));
    EigenDistance doesNotThrow = makeTestObject().calculateDistanceInSameEigenspace(
        FACTOR_LOADINGS_A,
        FACTOR_LOADINGS_B,
        makeInstructions(0.9, 0.9));
  }

  @Test
  public void multiplierNegativeOrZero_throws() {
    for (double secondMultiplier : ImmutableList.of(-1e-9, 0.0)) {
      assertIllegalArgumentException( () -> makeTestObject().calculateDistanceInSameEigenspace(
          FACTOR_LOADINGS_A,
          FACTOR_LOADINGS_B,
          makeInstructions(1.0, secondMultiplier)));
    }
    EigenDistance doesNotThrow = makeTestObject().calculateDistanceInSameEigenspace(
        FACTOR_LOADINGS_A,
        FACTOR_LOADINGS_B,
        makeInstructions(1.0, 1e-9));
  }

  @Test
  public void eigenDistancesAreCorrect_noMultipliers() {
    assertThat(
        calculateDistance(STOCK_A, STOCK_B, ImmutableList.of(1.0, 1.0)),
        eigenDistanceMatcher(eigenDistanceInVarianceSpace(
            doubleExplained(7, (53 - 50) + (64 - 60)), doubleExplained(5, Math.sqrt((53 - 50) * (53 - 50) + (64 - 60) * (64 - 60)))
        )));
    assertThat(
        calculateDistance(STOCK_C, STOCK_D, ImmutableList.of(1.0, 1.0)),
        eigenDistanceMatcher(eigenDistanceInVarianceSpace(
            doubleExplained(7, (53 - 50) + (64 - 60)), doubleExplained(5, Math.sqrt((53 - 50) * (53 - 50) + (64 - 60) * (64 - 60)))
        )));
  }

  @Test
  public void distancesToOneselfAreZero() {
    for (List<Double> multipliers : rbSetOf(
        ImmutableList.of(1.1, 1.1),
        ImmutableList.of(1.0, 1.0),
        ImmutableList.of(0.9, 0.9),
        ImmutableList.of(1.1, 0.9))) {
      for (FactorLoadings factorLoadings : rbSetOf(FACTOR_LOADINGS_A, FACTOR_LOADINGS_B, FACTOR_LOADINGS_C, FACTOR_LOADINGS_D)) {
        assertThat(
            "Every instrument has a distance of 0 to itself",
            makeTestObject().calculateDistanceInSameEigenspace(factorLoadings, factorLoadings, makeInstructions(multipliers)),
            eigenDistanceMatcher(zeroEigenDistance()));
      }
    }
  }

  @Test
  public void distanceCalculationIsCommutative() {
    for (List<Double> multipliers : rbSetOf(
        ImmutableList.of(1.1, 1.1),
        ImmutableList.of(0.9, 0.9),
        ImmutableList.of(1.1, 0.9))) {
      for (FactorLoadings factorLoadings1 : rbSetOf(FACTOR_LOADINGS_A, FACTOR_LOADINGS_B, FACTOR_LOADINGS_C, FACTOR_LOADINGS_D)) {
        for (FactorLoadings factorLoadings2 : rbSetOf(FACTOR_LOADINGS_A, FACTOR_LOADINGS_B, FACTOR_LOADINGS_C, FACTOR_LOADINGS_D)) {
          assertThat(
              "The distance calculation is commutative in the presence of equal multipliers (1 / eigenvalue)," +
                  " i.e. the distance between A and B is the same as the distance between B and A",
              makeTestObject().calculateDistanceInSameEigenspace(factorLoadings1, factorLoadings2, makeInstructions(multipliers)),
              eigenDistanceMatcher(
                  makeTestObject().calculateDistanceInSameEigenspace(factorLoadings2, factorLoadings1, makeInstructions(multipliers))));
        }
      }
    }
  }

  private EigenDistance calculateDistance(InstrumentId instrumentId1, InstrumentId instrumentId2,
                                          List<Double> multipliers) {
    return makeTestObject().calculateDistanceInSameEigenspace(
        FACTOR_LOADINGS_MAP.getOrThrow(instrumentId1),
        FACTOR_LOADINGS_MAP.getOrThrow(instrumentId2),
        makeInstructions(multipliers));
  }

  private final EigenDistanceCalculationInstructions makeInstructions(Double...multipliers) {
    return makeInstructions(Arrays.asList(multipliers));
  }

  private final EigenDistanceCalculationInstructions makeInstructions(List<Double> multipliers) {
    return eigenDistanceCalculationInstructions(
        eigenSubObjectiveMinimizesVariance(),
        decreasingPositiveDoubles(multipliers));
  }

  @Override
  protected DistanceInEigenspaceCalculator makeTestObject() {
    DistanceInEigenspaceCalculator testObject = new DistanceInEigenspaceCalculator();
    testObject.rawEigenDistanceCalculator = new RawEigenDistanceCalculator();
    return testObject;
  }

}
