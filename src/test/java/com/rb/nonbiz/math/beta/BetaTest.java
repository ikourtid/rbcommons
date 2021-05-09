package com.rb.nonbiz.math.beta;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.beta.Beta.BETA_OF_1;
import static com.rb.nonbiz.math.beta.Beta.beta;
import static com.rb.nonbiz.math.beta.Beta.betaWithBackground;
import static com.rb.nonbiz.math.beta.BetaBackgroundTest.betaBackgroundMatcher;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class BetaTest extends RBTestMatcher<Beta>  {

  @Test
  public void hasBackground_betaMustBeReasonablea_otherwiseThrows() {
    assertIllegalArgumentException( () -> beta(-31));
    Beta doesNotThrow;
    doesNotThrow = beta(-29);
    doesNotThrow = beta(-1);
    doesNotThrow = beta(0);
    doesNotThrow = beta(1);
    doesNotThrow = beta(29);
    assertIllegalArgumentException( () -> beta(31));
  }

  @Test
  public void hasBackground_betaMustBeReasonable_otherwiseThrows() {
    BetaBackground dummyBetaBackground = new BetaBackgroundTest().makeTrivialObject();
    assertIllegalArgumentException( () -> betaWithBackground(-31, dummyBetaBackground));
    Beta doesNotThrow;
    doesNotThrow = betaWithBackground(-29, dummyBetaBackground);
    doesNotThrow = betaWithBackground(-1, dummyBetaBackground);
    doesNotThrow = betaWithBackground(0, dummyBetaBackground);
    doesNotThrow = betaWithBackground(1, dummyBetaBackground);
    doesNotThrow = betaWithBackground(29, dummyBetaBackground);
    assertIllegalArgumentException( () -> betaWithBackground(31, dummyBetaBackground));
  }

  @Override
  public Beta makeTrivialObject() {
    return BETA_OF_1;
  }

  @Override
  public Beta makeNontrivialObject() {
    return betaWithBackground(-1.1, new BetaBackgroundTest().makeNontrivialObject());
  }

  @Override
  public Beta makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return betaWithBackground(-1.1 + e, new BetaBackgroundTest().makeMatchingNontrivialObject());
  }

  @Override
  protected boolean willMatch(Beta expected, Beta actual) {
    return betaMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<Beta> betaMatcher(Beta expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getValue(), 1e-8),
        matchOptional(v -> v.getBetaBackground(), f -> betaBackgroundMatcher(f)));
  }

}
