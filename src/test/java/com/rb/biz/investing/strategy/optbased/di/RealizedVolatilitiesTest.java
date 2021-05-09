package com.rb.biz.investing.strategy.optbased.di;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.math.eigen.Investable;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.DoubleFunction;

import static com.rb.biz.investing.strategy.optbased.di.RealizedVolatilities.realizedVolatilities;
import static com.rb.biz.marketdata.FakeInstruments.DUMMY_INSTRUMENT_ID;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.immutableIndexableArray1DMatcher;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.singletonImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.testImmutableIndexableArray1D;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBPublicTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBPublicTestConstants.ZERO_SEED;

// This test class is not generic, but the publicly exposed static matcher is.
public class RealizedVolatilitiesTest extends RBTestMatcher<RealizedVolatilities<InstrumentId>> {

  public static RealizedVolatilities<InstrumentId> testRealizedVolatilitiesWithSeed(
      InstrumentId instrumentId1, InstrumentId instrumentId2, double seed) {
    return realizedVolatilities(testImmutableIndexableArray1D(
        instrumentId1, 0.11 + seed,
        instrumentId2, 0.22 + seed));
  }

  @Test
  public void throwsIfNegativeOrHuge() {
    DoubleFunction<RealizedVolatilities<InstrumentId>> maker = standardDeviation ->
        realizedVolatilities(singletonImmutableIndexableArray1D(DUMMY_INSTRUMENT_ID, standardDeviation));
    assertIllegalArgumentException( () -> maker.apply(-1.23));
    assertIllegalArgumentException( () -> maker.apply(-1e-9));
    RealizedVolatilities<InstrumentId> doesNotThrow;
    doesNotThrow = maker.apply(0);
    doesNotThrow = maker.apply(0.11);
    assertIllegalArgumentException( () -> maker.apply(2)); // 200% daily vol ~= 3,200% annualized vol
  }

  @Override
  public RealizedVolatilities<InstrumentId> makeTrivialObject() {
    return realizedVolatilities(singletonImmutableIndexableArray1D(STOCK_A, 0.123));
  }

  @Override
  public RealizedVolatilities<InstrumentId> makeNontrivialObject() {
    return testRealizedVolatilitiesWithSeed(STOCK_A1, STOCK_A2, ZERO_SEED);
  }

  @Override
  public RealizedVolatilities<InstrumentId> makeMatchingNontrivialObject() {
    return testRealizedVolatilitiesWithSeed(STOCK_A1, STOCK_A2, EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(RealizedVolatilities<InstrumentId> expected, RealizedVolatilities<InstrumentId> actual) {
    return realizedVolatilitiesMatcher(expected).matches(actual);
  }

  public static <K extends Investable> TypeSafeMatcher<RealizedVolatilities<K>> realizedVolatilitiesMatcher(
      RealizedVolatilities<K> expected) {
    return makeMatcher(expected,
        match(v -> v.getDailyizedStandardDeviations(), f -> immutableIndexableArray1DMatcher(f,
            f2 -> typeSafeEqualTo(f2), f3 -> doubleAlmostEqualsMatcher(f3, 1e-8))));
  }

}
