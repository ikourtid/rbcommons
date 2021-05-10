package com.rb.nonbiz.math.eigen;

import com.google.common.collect.ImmutableList;
import com.rb.biz.investing.strategy.optbased.di.RealizedVolatilities;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ImmutableIndexableArray1D;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Ignore;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.rb.biz.investing.strategy.optbased.di.RealizedVolatilities.realizedVolatilities;
import static com.rb.biz.investing.strategy.optbased.di.RealizedVolatilitiesTest.realizedVolatilitiesMatcher;
import static com.rb.biz.marketdata.FakeInstruments.*;
import static com.rb.biz.marketdata.index.AdditionalCalculatedFactorLoadings.additionalCalculatedFactorLoadings;
import static com.rb.biz.marketdata.index.AdditionalCalculatedFactorLoadings.additionalCalculatedInstrumentIdFactorLoadings;
import static com.rb.biz.marketdata.index.AdditionalCalculatedFactorLoadingsTest.additionalCalculatedFactorLoadingsMatcher;
import static com.rb.biz.types.asset.InstrumentId.EMPTY_INSTRUMENT_ID_LIST;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.immutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.immutableIndexableArray1DMatcher;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.singletonImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.testImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.eigen.EigenExplainabilityFraction.eigenExplainabilityFraction;
import static com.rb.nonbiz.math.eigen.EigenExplainabilityTest.DUMMY_EIGEN_EXPLAINABILITY_FRACTION;
import static com.rb.nonbiz.math.eigen.Eigendecomposition.instrumentIdEigendecomposition;
import static com.rb.nonbiz.math.eigen.Eigenpair.eigenpair;
import static com.rb.nonbiz.math.eigen.EigenpairTest.eigenpairMatcher;
import static com.rb.nonbiz.math.eigen.Eigenvalue.eigenvalue;
import static com.rb.nonbiz.math.eigen.EigenvectorTest.dummyEigenvectorWithSize;
import static com.rb.nonbiz.math.eigen.EigenvectorTest.eigenvector;
import static com.rb.nonbiz.math.eigen.EigenvectorTest.singletonEigenvector;
import static com.rb.nonbiz.math.eigen.FactorLoadings.factorLoadings;
import static com.rb.nonbiz.math.eigen.FactorLoadingsTest.factorLoadingsMatcher;
import static com.rb.nonbiz.math.eigen.MultiItemQualityOfReturns.multiItemQualityOfReturns;
import static com.rb.nonbiz.math.eigen.MultiItemQualityOfReturnsTest.dummyMultiItemQualityOfReturns;
import static com.rb.nonbiz.math.eigen.MultiItemQualityOfReturnsTest.multiItemQualityOfReturnsMatcher;
import static com.rb.nonbiz.math.eigen.SingleItemQualityOfReturnsTest.onlyNumActualReturns;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;

/**
 * The test class is specific to InstrumentId-based decompositions,
 * but eigendecompositionMatcher (which is exposed to other tests) is still generic.
 */
public class EigendecompositionTest extends RBTestMatcher<Eigendecomposition<InstrumentId>> {

  public static RealizedVolatilities<InstrumentId> dummyRealizedVolatilities(InstrumentId...instrumentIds) {
    return realizedVolatilities(dummyStandardDeviations(instrumentIds));
  }

  public static ImmutableIndexableArray1D<InstrumentId, Double> dummyStandardDeviations(InstrumentId...instrumentIds) {
    Double[] values = new Double[instrumentIds.length];
    for (int i = 0; i < values.length; i++) {
      values[i] = 0.10 + i / 1_000.0;
    }
    return immutableIndexableArray1D(simpleArrayIndexMapping(instrumentIds), values);
  }

  /**
   * Use this in test when you want an eigendecomposition to contain loadings for a certain set of instruments,
   * but you don't care about any of the values in it.
   */
  public static Eigendecomposition<InstrumentId> dummyEigendecomposition(InstrumentId...instrumentIds) {
    int size = instrumentIds.length;
    return instrumentIdEigendecomposition(
        "",
        DUMMY_EIGEN_EXPLAINABILITY_FRACTION,
        1_000_000_000, // sufficiently huge to keep code from throwing
        // We need to make eigenpairs with descending positive eigenvalues so this won't throw
        IntStream
            .range(0, size)
            .mapToObj(i -> eigenpair(eigenvalue(size + 1 - i), dummyEigenvectorWithSize(size)))
            .collect(Collectors.toList()),
        immutableIndexableArray1D(
            simpleArrayIndexMapping(instrumentIds),
            IntStream
                .range(0, size)
                .mapToObj(i -> factorLoadings(new double[size]))
                .toArray(FactorLoadings[]::new)),
        multiItemQualityOfReturns(newRBSet(instrumentIds)
            .toRBMap(instrumentId -> onlyNumActualReturns(instrumentId, 1))),
        dummyRealizedVolatilities(instrumentIds));
  }

  @Test
  public void metaTest_checkDummyEigendecompositionConstructionWorks() {
    Eigendecomposition<InstrumentId> doesNotThrow = dummyEigendecomposition(STOCK_A, STOCK_B, STOCK_C);
  }

  @Test
  public void standardDeviations_mustBeValid() {
    DoubleFunction<Eigendecomposition<InstrumentId>> instantiator =
        dailyizedStandardDeviation -> instrumentIdEigendecomposition(
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
                STOCK_A, dailyizedStandardDeviation,
                STOCK_B, dailyizedStandardDeviation,
                STOCK_C, dailyizedStandardDeviation)));
    Eigendecomposition<InstrumentId> doesNotThrow;
    assertIllegalArgumentException( () -> instantiator.apply(-999));
    assertIllegalArgumentException( () -> instantiator.apply(-1e-7));
    assertIllegalArgumentException( () -> instantiator.apply(-1e-9));
    doesNotThrow = instantiator.apply(0.0);
    doesNotThrow = instantiator.apply(1e-9);
    doesNotThrow = instantiator.apply(1e-7);
    doesNotThrow = instantiator.apply(0.1234);
    doesNotThrow = instantiator.apply(0.99);
    // A daily stddev a 1 = 100% corresponds roughly to an annualized vol of 1,600 %,
    // which RealizedVolatilities flags as being too high.
    assertIllegalArgumentException( () -> instantiator.apply(1.00));
    assertIllegalArgumentException( () -> instantiator.apply(1.01));
  }

  @Test
  public void keysMustMatchFor_FactorLoadings_QualityOfReturns_StandardDeviations() {
    BiFunction<
        MultiItemQualityOfReturns<InstrumentId>,
        ImmutableIndexableArray1D<InstrumentId, Double>,
        Eigendecomposition<InstrumentId>> instantiator =
        (multiItemQualityOfReturns, standardDeviations) -> instrumentIdEigendecomposition(
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
            multiItemQualityOfReturns,
            realizedVolatilities(standardDeviations));
    Eigendecomposition<InstrumentId> doesNotThrow = instantiator.apply(
        multiItemQualityOfReturns(rbMapOf(
            STOCK_A, onlyNumActualReturns(STOCK_A, 2),
            STOCK_B, onlyNumActualReturns(STOCK_B, 2),
            STOCK_C, onlyNumActualReturns(STOCK_C, 2))),
        dummyStandardDeviations(STOCK_A, STOCK_B, STOCK_C));
    // too few in quality-of-returns
    assertIllegalArgumentException( () -> instantiator.apply(
        multiItemQualityOfReturns(rbMapOf(
            STOCK_A, onlyNumActualReturns(STOCK_A, 2),
            STOCK_B, onlyNumActualReturns(STOCK_B, 2))),
        dummyStandardDeviations(STOCK_A, STOCK_B, STOCK_C)));
    // too many in quality-of-returns
    assertIllegalArgumentException( () -> instantiator.apply(
        multiItemQualityOfReturns(rbMapOf(
            STOCK_A, onlyNumActualReturns(STOCK_A, 2),
            STOCK_B, onlyNumActualReturns(STOCK_B, 2),
            STOCK_C, onlyNumActualReturns(STOCK_C, 2),
            STOCK_D, onlyNumActualReturns(STOCK_D, 2))),
        dummyStandardDeviations(STOCK_A, STOCK_B, STOCK_C)));
    // too few in standard deviations
    assertIllegalArgumentException( () -> instantiator.apply(
        multiItemQualityOfReturns(rbMapOf(
            STOCK_A, onlyNumActualReturns(STOCK_A, 2),
            STOCK_B, onlyNumActualReturns(STOCK_B, 2),
            STOCK_C, onlyNumActualReturns(STOCK_C, 2))),
        dummyStandardDeviations(STOCK_A, STOCK_B)));
    // too many in standard deviations
    assertIllegalArgumentException( () -> instantiator.apply(
        multiItemQualityOfReturns(rbMapOf(
            STOCK_A, onlyNumActualReturns(STOCK_A, 2),
            STOCK_B, onlyNumActualReturns(STOCK_B, 2),
            STOCK_C, onlyNumActualReturns(STOCK_C, 2))),
        dummyStandardDeviations(STOCK_A, STOCK_B, STOCK_C, STOCK_D)));
  }

  @Test
  public void sumOfAllEigenvaluesField_mustExceedSumOfRetainedEigenvalues_otherwiseThrows() {
    for (double sumField : rbSetOf(0.99, 1.0)) {
      assertIllegalArgumentException( () -> instrumentIdEigendecomposition(
          "",
          DUMMY_EIGEN_EXPLAINABILITY_FRACTION,
          sumField,
          singletonList(
              eigenpair(eigenvalue(1.0), singletonEigenvector(1.0))),
          singletonImmutableIndexableArray1D(STOCK_A, factorLoadings(50.00)),
          dummyMultiItemQualityOfReturns(STOCK_A),
          realizedVolatilities(singletonImmutableIndexableArray1D(STOCK_A, 0.11))));
    }
  }

  @Test
  public void happyPath() {
    Eigendecomposition<InstrumentId> ignored = instrumentIdEigendecomposition(
        "test",
        DUMMY_EIGEN_EXPLAINABILITY_FRACTION,
        doubleExplained(90.01, 50 + 40 + 0.01),
        ImmutableList.of(
            eigenpair(eigenvalue(50.0), eigenvector(5.00, 5.01, 5.02)),
            eigenpair(eigenvalue(40.0), eigenvector(4.00, 4.01, 4.02))),
        testImmutableIndexableArray1D(
            STOCK_A, factorLoadings(50.00, 40.01),
            STOCK_B, factorLoadings(50.10, 40.11),
            STOCK_C, factorLoadings(50.20, 40.21)),
        dummyMultiItemQualityOfReturns(STOCK_A, STOCK_B, STOCK_C),
        realizedVolatilities(testImmutableIndexableArray1D(
            STOCK_A, 0.11,
            STOCK_B, 0.22,
            STOCK_C, 0.33)));
  }

  @Test
  public void eigenvaluesNotDescending_throws() {
    assertIllegalArgumentException( () -> instrumentIdEigendecomposition(
        "test",
        DUMMY_EIGEN_EXPLAINABILITY_FRACTION,
        doubleExplained(100.01, 10 + 90 + 0.01),
        ImmutableList.of(
            eigenpair(eigenvalue(10.0), eigenvector(5.00, 5.01, 5.02)),
            eigenpair(eigenvalue(90.0), eigenvector(4.00, 4.01, 4.02))),
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
            STOCK_C, 0.33))));
  }

  @Test
  public void noEigenpairs_throws() {
    assertIllegalArgumentException( () -> instrumentIdEigendecomposition(
        "test",
        DUMMY_EIGEN_EXPLAINABILITY_FRACTION,
        1.0,
        emptyList(),
        immutableIndexableArray1D(
            simpleArrayIndexMapping(EMPTY_INSTRUMENT_ID_LIST),

            new FactorLoadings[] { }),
        // I can't create an empty multiItemQualityOfReturns, so this may throw for the 'instrument ids don't match'
        // reason, vs the 'no eigenpairs'.
        multiItemQualityOfReturns(singletonRBMap(
            STOCK_A, onlyNumActualReturns(STOCK_A, 1))),
        realizedVolatilities(singletonImmutableIndexableArray1D(STOCK_A, 0.11))));
  }

  @Test
  public void test_getFactorLoadings_and_setMutableAdditionalCalculatedFactorLoadings() {
    Eigendecomposition<InstrumentId> eigendecomposition = instrumentIdEigendecomposition(
        DUMMY_STRING,
        DUMMY_EIGEN_EXPLAINABILITY_FRACTION,
        doubleExplained(90.01, 50 + 40 + 0.01),
        ImmutableList.of(
            eigenpair(eigenvalue(50.0), eigenvector(5.00, 5.01, 5.02)),
            eigenpair(eigenvalue(40.0), eigenvector(4.00, 4.01, 4.02))),
        testImmutableIndexableArray1D(
            // Only the following 5 lines are relevant in this test, but we can't just use DUMMY_DOUBLE
            // everywhere else, because there are some preconditions inside instrumentIdEigendecomposition.
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

    TriConsumer<InstrumentId, Double, Double> assertLoadings = (instrumentId, loading0, loading1) ->
        assertThat(
            eigendecomposition.getFactorLoadings(instrumentId),
            factorLoadingsMatcher(
                factorLoadings(loading0, loading1)));

    assertLoadings.accept(STOCK_A, 50.00, 40.01);
    assertLoadings.accept(STOCK_B, 50.10, 40.11);
    assertLoadings.accept(STOCK_C, 50.20, 40.21);

    // There's nothing special about the instrument IDs with names ETF_*, except that using ETFs
    // illustrates the only current (Oct 2018) use case of additionalCalculatedFactorLoadings.
    assertIllegalArgumentException( () -> eigendecomposition.getFactorLoadings(ETF_1));
    assertIllegalArgumentException( () -> eigendecomposition.setMutableAdditionalCalculatedFactorLoadings(
        additionalCalculatedFactorLoadings(singletonRBMap(
            ETF_1, factorLoadings(DUMMY_DOUBLE))))); // size 1 < 2
    assertIllegalArgumentException( () -> eigendecomposition.setMutableAdditionalCalculatedFactorLoadings(
        additionalCalculatedFactorLoadings(singletonRBMap(
            ETF_1, factorLoadings(DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE))))); // size 3 > 2
    eigendecomposition.setMutableAdditionalCalculatedFactorLoadings(
        additionalCalculatedFactorLoadings(rbMapOf(
            ETF_1, factorLoadings(7.1, 8.1),
            ETF_2, factorLoadings(7.2, 8.2)))); // factor loadings length = 2; same as existing loadings, so this works.

    assertLoadings.accept(STOCK_A, 50.00, 40.01);
    assertLoadings.accept(STOCK_B, 50.10, 40.11);
    assertLoadings.accept(STOCK_C, 50.20, 40.21);

    assertLoadings.accept(ETF_1, 7.1, 8.1);
    assertLoadings.accept(ETF_2, 7.2, 8.2);

    assertIllegalArgumentException( () -> eigendecomposition.getFactorLoadings(ETF_3));
  }

  @Ignore("As of Sep 27 2016, this is actually valid behavior")
  @Test
  public void notAllEigenvectorsHaveSameLength_throws() {
    assertIllegalArgumentException( () -> instrumentIdEigendecomposition(
        "test",
        DUMMY_EIGEN_EXPLAINABILITY_FRACTION,
        doubleExplained(90.01, 50 + 40 + 0.01),
        ImmutableList.of(
            eigenpair(eigenvalue(50.0), eigenvector(5.00, 5.01, 5.02)),
            eigenpair(eigenvalue(40.0), eigenvector(4.00, 4.01, 4.02, 4.03))),
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
            STOCK_C, 0.33))));
  }

  @Test
  public void stocksHaveFewerLoadingsThanRetainedFactors_throws() {
    assertIllegalArgumentException( () -> instrumentIdEigendecomposition(
        "test",
        DUMMY_EIGEN_EXPLAINABILITY_FRACTION,
        doubleExplained(90.01, 50 + 40 + 0.01),
        ImmutableList.of(
            eigenpair(eigenvalue(50.0), eigenvector(5.00, 5.01, 5.02)),
            eigenpair(eigenvalue(40.0), eigenvector(4.00, 4.01, 4.02))),
        testImmutableIndexableArray1D(
            STOCK_A, factorLoadings(50.00),
            STOCK_B, factorLoadings(50.10),
            STOCK_C, factorLoadings(50.20)),
        multiItemQualityOfReturns(rbMapOf(
            STOCK_A, onlyNumActualReturns(STOCK_A, 2),
            STOCK_B, onlyNumActualReturns(STOCK_B, 2),
            STOCK_C, onlyNumActualReturns(STOCK_C, 2))),
        realizedVolatilities(testImmutableIndexableArray1D(
            STOCK_A, 0.11,
            STOCK_B, 0.22,
            STOCK_C, 0.33))));
  }

  @Test
  public void stocksHaveMoreLoadingsThanRetainedFactors_throws() {
    assertIllegalArgumentException( () -> instrumentIdEigendecomposition(
        "test",
        DUMMY_EIGEN_EXPLAINABILITY_FRACTION,
        doubleExplained(90.01, 50 + 40 + 0.01),
        ImmutableList.of(
            eigenpair(eigenvalue(50.0), eigenvector(5.00, 5.01, 5.02)),
            eigenpair(eigenvalue(40.0), eigenvector(4.00, 4.01, 4.02))),
        testImmutableIndexableArray1D(
            STOCK_A, factorLoadings(50.00, 40.01, 30.02),
            STOCK_B, factorLoadings(50.10, 40.11, 30.12),
            STOCK_C, factorLoadings(50.20, 40.21, 30.22)),
        multiItemQualityOfReturns(rbMapOf(
            STOCK_A, onlyNumActualReturns(STOCK_A, 2),
            STOCK_B, onlyNumActualReturns(STOCK_B, 2),
            STOCK_C, onlyNumActualReturns(STOCK_C, 2))),
        realizedVolatilities(testImmutableIndexableArray1D(
            STOCK_A, 0.11,
            STOCK_B, 0.22,
            STOCK_C, 0.33))));
  }

  @Test
  public void containsKey_alsoConsidersAdditionalFactorLoadings() {
    Eigendecomposition<InstrumentId> eigendecomposition = makeNontrivialObject();
    assertOptionalNonEmpty(
        eigendecomposition.getMutableAdditionalCalculatedFactorLoadings(),
        additionalCalculatedFactorLoadingsMatcher(
            additionalCalculatedInstrumentIdFactorLoadings(
                rbMapOf(
                    STOCK_A7, factorLoadings(7.1, 7.2),
                    STOCK_A8, factorLoadings(8.1, 8.2)))));
    assertTrue(eigendecomposition.containsKey(STOCK_A7));
    assertTrue(eigendecomposition.containsKey(STOCK_A8));
    assertFalse(eigendecomposition.containsKey(STOCK_A6));
  }

  @Override
  public Eigendecomposition<InstrumentId> makeTrivialObject() {
    return instrumentIdEigendecomposition(
        "",
        eigenExplainabilityFraction(0.8),
        doubleExplained(1.01, 1.0 + 0.01),
        singletonList(
            eigenpair(eigenvalue(1.0), singletonEigenvector(1.0))),
        singletonImmutableIndexableArray1D(STOCK_A, factorLoadings(50.00)),
        multiItemQualityOfReturns(singletonRBMap(
            STOCK_A, onlyNumActualReturns(STOCK_A, 2))),
        realizedVolatilities(singletonImmutableIndexableArray1D(STOCK_A, 0.11)));
  }

  @Override
  public Eigendecomposition<InstrumentId> makeNontrivialObject() {
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
    return eigendecomposition;
  }

  @Override
  public Eigendecomposition<InstrumentId> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    Eigendecomposition<InstrumentId> eigendecomposition = instrumentIdEigendecomposition(
        "test",
        eigenExplainabilityFraction(0.8 + e),
        90.01 + e,
        ImmutableList.of(
            eigenpair(eigenvalue(50.0 + e), eigenvector(5.00 + e, 5.01 + e, 5.02 + e)),
            eigenpair(eigenvalue(40.0 + e), eigenvector(4.00 + e, 4.01 + e, 4.02 + e))),
        testImmutableIndexableArray1D(
            STOCK_A, factorLoadings(50.00 + e, 40.01 + e),
            STOCK_B, factorLoadings(50.10 + e, 40.11 + e),
            STOCK_C, factorLoadings(50.20 + e, 40.21 + e)),
        multiItemQualityOfReturns(rbMapOf(
            STOCK_A, onlyNumActualReturns(STOCK_A, 2),
            STOCK_B, onlyNumActualReturns(STOCK_B, 2),
            STOCK_C, onlyNumActualReturns(STOCK_C, 2))),
        realizedVolatilities(testImmutableIndexableArray1D(
            STOCK_A, 0.11 + e,
            STOCK_B, 0.22 + e,
            STOCK_C, 0.33 + e)));
    eigendecomposition.setMutableAdditionalCalculatedFactorLoadings(
        additionalCalculatedInstrumentIdFactorLoadings(
            rbMapOf(
                STOCK_A7, factorLoadings(7.1 + e, 7.2 + e),
                STOCK_A8, factorLoadings(8.1 + e, 8.2 + e))));
    return eigendecomposition;
  }

  @Override
  protected boolean willMatch(Eigendecomposition<InstrumentId> expected, Eigendecomposition<InstrumentId> actual) {
    return eigendecompositionMatcher(expected, iid -> typeSafeEqualTo(iid)).matches(actual);
  }

  public static TypeSafeMatcher<Eigendecomposition<InstrumentId>> instrumentIdEigendecompositionMatcher(
      Eigendecomposition<InstrumentId> expected) {
    return eigendecompositionMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <K extends Investable> TypeSafeMatcher<Eigendecomposition<K>> eigendecompositionMatcher(
      Eigendecomposition<K> expected,
      MatcherGenerator<K> keyMatcherGenerator) {
    return makeMatcher(expected,
        matchUsingEquals(            v -> v.getHumanDescription()),
        matchUsingAlmostEquals(      v -> v.getEigenExplainabilityFraction(), 1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getSumOfAllEigenvaluesIncludingSkipped(), 1e-8),
        matchList(                   v -> v.getEigenpairsInDescendingEigenvalues(), f -> eigenpairMatcher(f)),
        match(                       v -> v.getRawFactorLoadingsByKey(),            f -> immutableIndexableArray1DMatcher(f,
            keyMatcherGenerator, f2 -> factorLoadingsMatcher(f2))),
        match(                       v -> v.getMultiItemQualityOfReturns(),         f -> multiItemQualityOfReturnsMatcher(f)),
        match(                       v -> v.getRealizedVolatilities(),              f -> realizedVolatilitiesMatcher(f)),
        matchOptional(v -> v.getMutableAdditionalCalculatedFactorLoadings(),        f -> additionalCalculatedFactorLoadingsMatcher(f)));
  }

}
