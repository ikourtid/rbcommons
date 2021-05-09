package com.rb.nonbiz.math.eigen;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.math.eigen.FactorLoadings.factorLoadings;
import static com.rb.nonbiz.math.eigen.InstrumentLoadings.instrumentLoadings;
import static com.rb.nonbiz.math.eigen.InstrumentLoadingsMap.instrumentLoadingsMap;
import static com.rb.nonbiz.math.eigen.InstrumentLoadingsTest.instrumentLoadingsMatcher;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class InstrumentLoadingsMapTest extends RBTestMatcher<InstrumentLoadingsMap> {

  public static InstrumentLoadingsMap singletonInstrumentLoadingsMap(
      InstrumentId instrumentId, FactorLoadings factorLoadings) {
    return instrumentLoadingsMap(singletonIidMap(
        instrumentId, instrumentLoadings(instrumentId, factorLoadings)));
  }

  @Test
  public void mapIsEmpty_throws() {
    assertIllegalArgumentException( () -> instrumentLoadingsMap(emptyIidMap()));
  }

  @Override
  public InstrumentLoadingsMap makeTrivialObject() {
    return singletonInstrumentLoadingsMap(STOCK_A, factorLoadings(0));
  }

  @Override
  public InstrumentLoadingsMap makeNontrivialObject() {
    return instrumentLoadingsMap(iidMapOf(
        STOCK_A1, instrumentLoadings(STOCK_A1, factorLoadings(-1.1, 2.2)),
        STOCK_A2, instrumentLoadings(STOCK_A2, factorLoadings(3.3, -4.4))));
  }

  @Override
  public InstrumentLoadingsMap makeMatchingNontrivialObject() {
    double e = 1e-9;
    return instrumentLoadingsMap(iidMapOf(
        STOCK_A1, instrumentLoadings(STOCK_A1, factorLoadings(-1.1 + e, 2.2 + e)),
        STOCK_A2, instrumentLoadings(STOCK_A2, factorLoadings(3.3 + e, -4.4 + e))));
  }

  @Override
  protected boolean willMatch(InstrumentLoadingsMap expected, InstrumentLoadingsMap actual) {
    return instrumentLoadingsMapMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<InstrumentLoadingsMap> instrumentLoadingsMapMatcher(InstrumentLoadingsMap expected) {
    return makeMatcher(expected,
        matchIidMap(v -> v.getRawMap(), f -> instrumentLoadingsMatcher(f)));
  }

}
