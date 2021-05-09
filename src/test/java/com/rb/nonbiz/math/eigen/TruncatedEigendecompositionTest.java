package com.rb.nonbiz.math.eigen;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.IntFunction;

import static com.rb.biz.investing.strategy.optbased.di.RealizedVolatilities.realizedVolatilities;
import static com.rb.biz.marketdata.FakeInstruments.DUMMY_INSTRUMENT_ID;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A7;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A8;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.index.AdditionalCalculatedFactorLoadings.additionalCalculatedInstrumentIdFactorLoadings;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.testImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.math.eigen.EigenDimensionIndex.eigenDimensionIndex;
import static com.rb.nonbiz.math.eigen.EigenDimensionIndexTest.eigenDimensionIndexMatcher;
import static com.rb.nonbiz.math.eigen.EigenExplainabilityFraction.eigenExplainabilityFraction;
import static com.rb.nonbiz.math.eigen.Eigendecomposition.instrumentIdEigendecomposition;
import static com.rb.nonbiz.math.eigen.EigendecompositionTest.eigendecompositionMatcher;
import static com.rb.nonbiz.math.eigen.Eigenpair.eigenpair;
import static com.rb.nonbiz.math.eigen.Eigenvalue.eigenvalue;
import static com.rb.nonbiz.math.eigen.EigenvectorTest.eigenvector;
import static com.rb.nonbiz.math.eigen.FactorLoadings.factorLoadings;
import static com.rb.nonbiz.math.eigen.FactorLoadingsTest.factorLoadingsMatcher;
import static com.rb.nonbiz.math.eigen.MultiItemQualityOfReturns.multiItemQualityOfReturns;
import static com.rb.nonbiz.math.eigen.SingleItemQualityOfReturnsTest.onlyNumActualReturns;
import static com.rb.nonbiz.math.eigen.TruncatedEigendecomposition.truncatedEigendecomposition;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

// This class is not generic, but one of the publicly exposed static matchers is
public class TruncatedEigendecompositionTest extends RBTestMatcher<TruncatedEigendecomposition<InstrumentId>> {

  public static <K extends Investable> TruncatedEigendecomposition<K> eigendecompositionWithoutTruncation(
      Eigendecomposition<K> eigendecomposition) {
    return truncatedEigendecomposition(
        eigendecomposition,
        eigenDimensionIndex(eigendecomposition.getNumRetainedEigenpairs() - 1)); // -1 due to 0-based counting in arrays
  }

  @Test
  public void testMaxCannotExceedActual() {
    Eigendecomposition<InstrumentId> eigendecomposition = new EigendecompositionTest().makeNontrivialObject();
    assertEquals(2, eigendecomposition.getNumRetainedEigenpairs());
    IntFunction<TruncatedEigendecomposition<InstrumentId>> maker = maxIndex -> truncatedEigendecomposition(
        eigendecomposition,
        eigenDimensionIndex(maxIndex));
    TruncatedEigendecomposition<InstrumentId> doesNotThrow = maker.apply(1);
    assertIllegalArgumentException( () -> maker.apply(2));
  }

  @Test
  public void testGetFactorLoading() {
    Eigendecomposition<InstrumentId> eigendecomposition = instrumentIdEigendecomposition(
        "test",
        eigenExplainabilityFraction(0.8),
        doubleExplained(90.01, 50 + 40 + 0.01),
        ImmutableList.of(
            eigenpair(eigenvalue(50.0), eigenvector(5.00, 5.01, 5.02)),
            eigenpair(eigenvalue(40.0), eigenvector(4.00, 4.01, 4.02))),
        testImmutableIndexableArray1D(
            STOCK_A, factorLoadings(50.00, 40.01),
            STOCK_B, factorLoadings(50.10, 40.11),
            STOCK_C, factorLoadings(50.20, 40.21)),
        multiItemQualityOfReturns(rbMapOf(
            STOCK_A, onlyNumActualReturns(STOCK_A, 2),
            STOCK_B, onlyNumActualReturns(STOCK_B, 2),
            STOCK_C, onlyNumActualReturns(STOCK_C, 2))),
        realizedVolatilities(testImmutableIndexableArray1D(
            STOCK_A, 0.11,
            STOCK_B, 0.22,
            STOCK_C, 0.33)));
    eigendecomposition.setMutableAdditionalCalculatedFactorLoadings(
        additionalCalculatedInstrumentIdFactorLoadings(
            rbMapOf(
                STOCK_A7, factorLoadings(7.1, 7.2),
                STOCK_A8, factorLoadings(8.1, 8.2))));

    TruncatedEigendecomposition<InstrumentId> noTruncation = eigendecompositionWithoutTruncation(eigendecomposition);
    assertEquals(50.00, noTruncation.getFactorLoading(STOCK_A,  eigenDimensionIndex(0)), 1e-8);
    assertEquals(50.10, noTruncation.getFactorLoading(STOCK_B,  eigenDimensionIndex(0)), 1e-8);
    assertEquals(50.20, noTruncation.getFactorLoading(STOCK_C,  eigenDimensionIndex(0)), 1e-8);
    assertEquals(7.1,   noTruncation.getFactorLoading(STOCK_A7, eigenDimensionIndex(0)), 1e-8);
    assertEquals(8.1,   noTruncation.getFactorLoading(STOCK_A8, eigenDimensionIndex(0)), 1e-8);

    assertEquals(40.01, noTruncation.getFactorLoading(STOCK_A,  eigenDimensionIndex(1)), 1e-8);
    assertEquals(40.11, noTruncation.getFactorLoading(STOCK_B,  eigenDimensionIndex(1)), 1e-8);
    assertEquals(40.21, noTruncation.getFactorLoading(STOCK_C,  eigenDimensionIndex(1)), 1e-8);
    assertEquals(7.2,   noTruncation.getFactorLoading(STOCK_A7, eigenDimensionIndex(1)), 1e-8);
    assertEquals(8.2,   noTruncation.getFactorLoading(STOCK_A8, eigenDimensionIndex(1)), 1e-8);

    assertIllegalArgumentException( () -> noTruncation.getFactorLoading(STOCK_A,  eigenDimensionIndex(2)));
    assertIllegalArgumentException( () -> noTruncation.getFactorLoading(STOCK_B,  eigenDimensionIndex(2)));
    assertIllegalArgumentException( () -> noTruncation.getFactorLoading(STOCK_C,  eigenDimensionIndex(2)));
    assertIllegalArgumentException( () -> noTruncation.getFactorLoading(STOCK_A7, eigenDimensionIndex(2)));
    assertIllegalArgumentException( () -> noTruncation.getFactorLoading(STOCK_A8, eigenDimensionIndex(2)));

    assertIllegalArgumentException( () -> noTruncation.getFactorLoading(DUMMY_INSTRUMENT_ID, eigenDimensionIndex(0)));
    assertIllegalArgumentException( () -> noTruncation.getFactorLoading(DUMMY_INSTRUMENT_ID, eigenDimensionIndex(1)));
    assertIllegalArgumentException( () -> noTruncation.getFactorLoading(DUMMY_INSTRUMENT_ID, eigenDimensionIndex(2)));

    // max index will be 0 => only store a single (and biggest) principal component.
    TruncatedEigendecomposition<InstrumentId> withTruncation = truncatedEigendecomposition(eigendecomposition, eigenDimensionIndex(0));
    assertEquals(50.00, withTruncation.getFactorLoading(STOCK_A,  eigenDimensionIndex(0)), 1e-8);
    assertEquals(50.10, withTruncation.getFactorLoading(STOCK_B,  eigenDimensionIndex(0)), 1e-8);
    assertEquals(50.20, withTruncation.getFactorLoading(STOCK_C,  eigenDimensionIndex(0)), 1e-8);
    assertEquals(7.1,   withTruncation.getFactorLoading(STOCK_A7, eigenDimensionIndex(0)), 1e-8);
    assertEquals(8.1,   withTruncation.getFactorLoading(STOCK_A8, eigenDimensionIndex(0)), 1e-8);

    assertIllegalArgumentException( () -> withTruncation.getFactorLoading(STOCK_A,  eigenDimensionIndex(1)));
    assertIllegalArgumentException( () -> withTruncation.getFactorLoading(STOCK_B,  eigenDimensionIndex(1)));
    assertIllegalArgumentException( () -> withTruncation.getFactorLoading(STOCK_C,  eigenDimensionIndex(1)));
    assertIllegalArgumentException( () -> withTruncation.getFactorLoading(STOCK_A7, eigenDimensionIndex(1)));
    assertIllegalArgumentException( () -> withTruncation.getFactorLoading(STOCK_A8, eigenDimensionIndex(1)));

    assertIllegalArgumentException( () -> withTruncation.getFactorLoading(DUMMY_INSTRUMENT_ID, eigenDimensionIndex(0)));
    assertIllegalArgumentException( () -> withTruncation.getFactorLoading(DUMMY_INSTRUMENT_ID, eigenDimensionIndex(1)));

    // Testing getTruncatedFactorLoadings could happen in a separate unit test, but we already have a lot of the setup
    // here, so let's just do it here.
    assertThat(
        noTruncation.getTruncatedFactorLoadings(STOCK_A),
        factorLoadingsMatcher(
            factorLoadings(50.0, 40.01)));
    assertThat(
        withTruncation.getTruncatedFactorLoadings(STOCK_A),
        factorLoadingsMatcher(
            factorLoadings(50.0)));
  }

  @Override
  public TruncatedEigendecomposition<InstrumentId> makeTrivialObject() {
    return eigendecompositionWithoutTruncation(new EigendecompositionTest().makeTrivialObject());
  }

  @Override
  public TruncatedEigendecomposition<InstrumentId> makeNontrivialObject() {
    return truncatedEigendecomposition(
        new EigendecompositionTest().makeNontrivialObject(),
        eigenDimensionIndex(0));
  }

  @Override
  public TruncatedEigendecomposition<InstrumentId> makeMatchingNontrivialObject() {
    return truncatedEigendecomposition(
        new EigendecompositionTest().makeMatchingNontrivialObject(),
        eigenDimensionIndex(0));
  }

  @Override
  protected boolean willMatch(TruncatedEigendecomposition<InstrumentId> expected,
                              TruncatedEigendecomposition<InstrumentId> actual) {
    return instrumentIdTruncatedEigendecompositionMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<TruncatedEigendecomposition<InstrumentId>> instrumentIdTruncatedEigendecompositionMatcher(
      TruncatedEigendecomposition<InstrumentId> expected) {
    return truncatedEigendecompositionMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <K extends Investable> TypeSafeMatcher<TruncatedEigendecomposition<K>> truncatedEigendecompositionMatcher(
      TruncatedEigendecomposition<K> expected,
      MatcherGenerator<K> keyMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getEigendecomposition(), f -> eigendecompositionMatcher(f, keyMatcherGenerator)),
        match(v -> v.getMaxValidEigenDimensionIndex(), f -> eigenDimensionIndexMatcher(f)),
        matchUsingEquals(v -> v.getNumEigenDimensionsAfterTruncation()));
  }

}
