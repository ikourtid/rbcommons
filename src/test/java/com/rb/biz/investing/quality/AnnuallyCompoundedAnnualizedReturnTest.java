package com.rb.biz.investing.quality;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.investing.quality.AnnuallyCompoundedAnnualizedReturn.annuallyCompoundedAnnualizedReturn;
import static com.rb.biz.investing.quality.ContinuouslyCompoundedAnnualizedReturn.continuouslyCompoundedAnnualizedReturn;
import static com.rb.biz.investing.quality.ContinuouslyCompoundedAnnualizedReturnTest.continuouslyCompoundedAnnualizedReturnMatcher;
import static com.rb.biz.types.OnesBasedReturn.FLAT_RETURN;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnnuallyCompoundedAnnualizedReturnTest extends RBTestMatcher<AnnuallyCompoundedAnnualizedReturn> {

  @Test
  public void testSimpleConversions() {
    assertThat(
        annuallyCompoundedAnnualizedReturn(FLAT_RETURN)
            .toContinuouslyCompoundedAnnualizedReturn(),
        continuouslyCompoundedAnnualizedReturnMatcher(
            continuouslyCompoundedAnnualizedReturn(FLAT_RETURN)));
  }

  @Test
  public void testSpecificConversions() {
    assertThat(
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toContinuouslyCompoundedAnnualizedReturn(),
        continuouslyCompoundedAnnualizedReturnMatcher(
            continuouslyCompoundedAnnualizedReturn(onesBasedReturn(1.048790164))));
    assertThat(
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(0.95))
            .toContinuouslyCompoundedAnnualizedReturn(),
        continuouslyCompoundedAnnualizedReturnMatcher(
            continuouslyCompoundedAnnualizedReturn(onesBasedReturn(0.9487067056))));
  }

  @Test
  public void testConversionToNonAnnualized() {
    assertAlmostEquals(
        onesBasedReturn(1.05),
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toNonAnnualized(1.0),
        1e-8);
    assertAlmostEquals(
        onesBasedReturn(1.05 * 1.05),
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toNonAnnualized(2.0),
        1e-8);
    assertAlmostEquals(
        onesBasedReturn(1.05 * 1.05 * 1.05),
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toNonAnnualized(3.0),
        1e-8);
    assertAlmostEquals(
        onesBasedReturn(doubleExplained(1.02469508, Math.sqrt(1.05))),
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toNonAnnualized(0.5),
        1e-8);
    assertAlmostEquals(
        FLAT_RETURN,
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toNonAnnualized(0),
        1e-8);
  }

  @Override
  public AnnuallyCompoundedAnnualizedReturn makeTrivialObject() {
    return annuallyCompoundedAnnualizedReturn(FLAT_RETURN);
  }

  @Override
  public AnnuallyCompoundedAnnualizedReturn makeNontrivialObject() {
    return annuallyCompoundedAnnualizedReturn(onesBasedReturn(0.99));
  }

  @Override
  public AnnuallyCompoundedAnnualizedReturn makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return annuallyCompoundedAnnualizedReturn(onesBasedReturn(0.99 + e));
  }

  @Override
  protected boolean willMatch(AnnuallyCompoundedAnnualizedReturn expected,
                              AnnuallyCompoundedAnnualizedReturn actual) {
    return annuallyCompoundedAnnualizedReturnMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<AnnuallyCompoundedAnnualizedReturn> annuallyCompoundedAnnualizedReturnMatcher(
      AnnuallyCompoundedAnnualizedReturn expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getRawReturn(), DEFAULT_EPSILON_1e_8));
  }

}
