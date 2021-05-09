package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.nonbiz.math.eigen.FactorLoadingsTest.factorLoadingsMatcher;
import static com.rb.nonbiz.math.eigen.InstrumentLoadings.instrumentLoadings;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class InstrumentLoadingsTest extends RBTestMatcher<InstrumentLoadings> {

  @Override
  public InstrumentLoadings makeTrivialObject() {
    return instrumentLoadings(STOCK_A, new FactorLoadingsTest().makeTrivialObject());
  }

  @Override
  public InstrumentLoadings makeNontrivialObject() {
    return instrumentLoadings(STOCK_B, new FactorLoadingsTest().makeNontrivialObject());
  }

  @Override
  public InstrumentLoadings makeMatchingNontrivialObject() {
    return instrumentLoadings(STOCK_B, new FactorLoadingsTest().makeMatchingNontrivialObject());
  }

  @Override
  protected boolean willMatch(InstrumentLoadings expected, InstrumentLoadings actual) {
    return instrumentLoadingsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<InstrumentLoadings> instrumentLoadingsMatcher(InstrumentLoadings expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v. getInstrumentId()),
        match(           v -> v.getLoadings(), f -> factorLoadingsMatcher(f)));
  }

}
