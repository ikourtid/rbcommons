package com.rb.biz.marketdata.index;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.math.eigen.FactorLoadings;
import com.rb.nonbiz.math.eigen.Investable;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A7;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A8;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.index.AdditionalCalculatedFactorLoadings.additionalCalculatedInstrumentIdFactorLoadings;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.math.eigen.FactorLoadings.factorLoadings;
import static com.rb.nonbiz.math.eigen.FactorLoadingsTest.factorLoadingsMatcher;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBPublicTestConstants.DUMMY_DOUBLE;
import static junit.framework.TestCase.assertEquals;

public class AdditionalCalculatedFactorLoadingsTest extends RBTestMatcher<AdditionalCalculatedFactorLoadings<InstrumentId>> {

  @Test
  public void factorLoadingsSizeMustBeSame_otherwiseThrows() {
    Function<FactorLoadings, AdditionalCalculatedFactorLoadings<InstrumentId>> maker = factorLoadingsB ->
        additionalCalculatedInstrumentIdFactorLoadings(
            rbMapOf(
                STOCK_A, factorLoadings(DUMMY_DOUBLE, DUMMY_DOUBLE),
                STOCK_B, factorLoadingsB));

    assertIllegalArgumentException( () -> maker.apply(factorLoadings(DUMMY_DOUBLE)));
    AdditionalCalculatedFactorLoadings<InstrumentId> doesNotThrow = maker.apply(factorLoadings(DUMMY_DOUBLE, DUMMY_DOUBLE));
    assertEquals(2, doesNotThrow.getSharedFactorLoadingsSize());
    assertIllegalArgumentException( () -> maker.apply(factorLoadings(DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE)));
  }

  @Override
  public AdditionalCalculatedFactorLoadings<InstrumentId> makeTrivialObject() {
    return additionalCalculatedInstrumentIdFactorLoadings(
        singletonRBMap(
            STOCK_A, factorLoadings(1.0)));
  }

  @Override
  public AdditionalCalculatedFactorLoadings<InstrumentId> makeNontrivialObject() {
    return additionalCalculatedInstrumentIdFactorLoadings(
        rbMapOf(
            STOCK_A7, factorLoadings(7.1, 7.2, 7.3),
            STOCK_A8, factorLoadings(8.1, 8.2, 8.3)));
  }

  @Override
  public AdditionalCalculatedFactorLoadings<InstrumentId> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return additionalCalculatedInstrumentIdFactorLoadings(
        rbMapOf(
            STOCK_A7, factorLoadings(7.1 + e, 7.2 + e, 7.3 + e),
            STOCK_A8, factorLoadings(8.1 + e, 8.2 + e, 8.3 + e)));
  }

  @Override
  protected boolean willMatch(AdditionalCalculatedFactorLoadings<InstrumentId> expected,
                              AdditionalCalculatedFactorLoadings<InstrumentId> actual) {
    return additionalCalculatedFactorLoadingsMatcher(expected).matches(actual);
  }

  public static <K extends Investable> TypeSafeMatcher<AdditionalCalculatedFactorLoadings<K>>
  additionalCalculatedFactorLoadingsMatcher(AdditionalCalculatedFactorLoadings<K> expected) {
    return makeMatcher(expected,
        matchRBMap(v -> v.getRawMap(), f -> factorLoadingsMatcher(f)));
  }

}
