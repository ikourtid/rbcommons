package com.rb.nonbiz.math.beta;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Correlation;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.beta.BetaBackground.betaBackground;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingImpreciseAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Correlation.NO_CORRELATION;
import static com.rb.nonbiz.types.Correlation.correlation;

public class BetaBackgroundTest extends RBTestMatcher<BetaBackground> {

  @Test
  public void numDaysMustBeValid() {
    Correlation dummyCorrelation = correlation(-0.123);
    assertIllegalArgumentException( () -> betaBackground(-999, dummyCorrelation));
    assertIllegalArgumentException( () -> betaBackground(-1, dummyCorrelation));
    assertIllegalArgumentException( () -> betaBackground(0, dummyCorrelation));

    BetaBackground doesNotThrow;
    doesNotThrow = betaBackground(1, dummyCorrelation);
    doesNotThrow = betaBackground(10, dummyCorrelation);
    doesNotThrow = betaBackground(2_500, dummyCorrelation);
    assertIllegalArgumentException( () -> betaBackground(2_501, dummyCorrelation));
  }

  @Override
  public BetaBackground makeTrivialObject() {
    return betaBackground(1, NO_CORRELATION);
  }

  @Override
  public BetaBackground makeNontrivialObject() {
    return betaBackground(123, correlation(-0.456));
  }

  @Override
  public BetaBackground makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return betaBackground(123, correlation(-0.456 + e));
  }

  @Override
  protected boolean willMatch(BetaBackground expected, BetaBackground actual) {
    return betaBackgroundMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<BetaBackground> betaBackgroundMatcher(BetaBackground expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getNumMarketDaysInCalculation()),
        matchUsingImpreciseAlmostEquals(v -> v.getCorrelation(), 1e-8));
  }

}
