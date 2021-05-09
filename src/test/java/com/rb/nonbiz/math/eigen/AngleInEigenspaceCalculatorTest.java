package com.rb.nonbiz.math.eigen;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.math.Angle;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.immutableIndexableArray1D;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.Angle.angleInDegrees;
import static com.rb.nonbiz.math.AngleTest.angleMatcher;
import static com.rb.nonbiz.math.eigen.EigenExplainabilityFraction.eigenExplainabilityFraction;
import static com.rb.nonbiz.math.eigen.Eigendecomposition.instrumentIdEigendecompositionWithPossibleFilter;
import static com.rb.nonbiz.math.eigen.EigendecompositionTest.dummyRealizedVolatilities;
import static com.rb.nonbiz.math.eigen.Eigenpair.eigenpair;
import static com.rb.nonbiz.math.eigen.Eigenvalue.eigenvalue;
import static com.rb.nonbiz.math.eigen.EigenvectorTest.eigenvector;
import static com.rb.nonbiz.math.eigen.FactorLoadings.factorLoadings;
import static com.rb.nonbiz.math.eigen.MultiItemQualityOfReturnsTest.dummyMultiItemQualityOfReturns;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.hamcrest.MatcherAssert.assertThat;

public class AngleInEigenspaceCalculatorTest extends RBTest<AngleInEigenspaceCalculator> {

  private final double SQRT_2 = Math.sqrt(2);
  private final FactorLoadings FACTOR_LOADINGS_A = factorLoadings(0, 1);
  private final FactorLoadings FACTOR_LOADINGS_B = factorLoadings(SQRT_2 / 2, SQRT_2 / 2);
  private final FactorLoadings FACTOR_LOADINGS_C = factorLoadings(1, 0);
  private final FactorLoadings FACTOR_LOADINGS_D = factorLoadings(-SQRT_2 / 2, -SQRT_2 / 2);
  private final Eigendecomposition<InstrumentId> EIGENDECOMPOSITION = instrumentIdEigendecompositionWithPossibleFilter(
      "test",
      eigenExplainabilityFraction(0.8),
      doubleExplained(90.01, 50 + 40 + 0.01),
      ImmutableList.of(
          eigenpair(eigenvalue(2.0), eigenvector( SQRT_2 / 2, SQRT_2 / 2)),
          eigenpair(eigenvalue(1.0), eigenvector(-SQRT_2 / 2, SQRT_2 / 2))),
      immutableIndexableArray1D(
          simpleArrayIndexMapping(STOCK_A, STOCK_B, STOCK_C, STOCK_D),
          new FactorLoadings[] {
              FACTOR_LOADINGS_A,
              FACTOR_LOADINGS_B,
              FACTOR_LOADINGS_C,
              FACTOR_LOADINGS_D
          }),
      dummyMultiItemQualityOfReturns(STOCK_A, STOCK_B, STOCK_C, STOCK_D),
      dummyRealizedVolatilities(STOCK_A, STOCK_B, STOCK_C, STOCK_D));

  @Test
  public void anglesOfSpecificPairsAreCorrectInUntransformedSpace() {
    assertThat(calculateAngle(STOCK_A, STOCK_B), angleMatcher(angleInDegrees(45)));
    assertThat(calculateAngle(STOCK_A, STOCK_C), angleMatcher(angleInDegrees(90)));
    assertThat(calculateAngle(STOCK_A, STOCK_D), angleMatcher(angleInDegrees(135)));
    assertThat(calculateAngle(STOCK_B, STOCK_C), angleMatcher(angleInDegrees(45)));
    assertThat(calculateAngle(STOCK_B, STOCK_D), angleMatcher(angleInDegrees(180)));
    assertThat(calculateAngle(STOCK_C, STOCK_D), angleMatcher(angleInDegrees(135)));
  }

  @Test
  public void anglesToOneselfAreZero() {
    for (FactorLoadings factorLoadings : rbSetOf(FACTOR_LOADINGS_A, FACTOR_LOADINGS_B, FACTOR_LOADINGS_C, FACTOR_LOADINGS_D)) {
      assertThat(
          "Every instrument has an angle of 0 to itself",
          makeTestObject().calculateAngleInSameEigenspace(factorLoadings, factorLoadings),
          angleMatcher(angleInDegrees(0)));
    }
  }

  @Test
  public void angleCalculationIsCommutative() {
    for (FactorLoadings factorLoadings1 : rbSetOf(FACTOR_LOADINGS_A, FACTOR_LOADINGS_B, FACTOR_LOADINGS_C, FACTOR_LOADINGS_D)) {
      for (FactorLoadings factorLoadings2 : rbSetOf(FACTOR_LOADINGS_A, FACTOR_LOADINGS_B, FACTOR_LOADINGS_C, FACTOR_LOADINGS_D)) {
        assertThat(
            "The angle calculation is commutative, i.e. the angle between A and B is the same as the angle between B and A",
            makeTestObject().calculateAngleInSameEigenspace(factorLoadings1, factorLoadings2),
            angleMatcher(
                makeTestObject().calculateAngleInSameEigenspace(factorLoadings2, factorLoadings1)));
      }
    }
  }

  private Angle calculateAngle(InstrumentId instrumentId1, InstrumentId instrumentId2) {
    return makeTestObject().calculateAngleInSameEigenspace(
        EIGENDECOMPOSITION.getFactorLoadings(instrumentId1),
        EIGENDECOMPOSITION.getFactorLoadings(instrumentId2));
  }

  @Override
  protected AngleInEigenspaceCalculator makeTestObject() {
    AngleInEigenspaceCalculator testObject = new AngleInEigenspaceCalculator();
    testObject.rawAngleCalculator = new RawAngleCalculator();
    return testObject;
  }

}
