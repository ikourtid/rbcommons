package com.rb.biz.investing.quality;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.investing.quality.AnnuallyCompoundedAnnualizedReturn.annuallyCompoundedAnnualizedReturn;
import static com.rb.biz.investing.quality.AnnuallyCompoundedAnnualizedReturnTest.annuallyCompoundedAnnualizedReturnMatcher;
import static com.rb.biz.investing.quality.ContinuouslyCompoundedAnnualizedReturn.continuouslyCompoundedAnnualizedReturn;
import static com.rb.biz.types.OnesBasedReturn.FLAT_RETURN;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;

public class ContinuouslyCompoundedAnnualizedReturnTest extends RBTestMatcher<ContinuouslyCompoundedAnnualizedReturn> {

  @Test
  public void testSimpleConversions() {
    assertThat(
        continuouslyCompoundedAnnualizedReturn(FLAT_RETURN)
            .toAnnuallyCompoundedAnnualizedReturn(),
        annuallyCompoundedAnnualizedReturnMatcher(
            annuallyCompoundedAnnualizedReturn(FLAT_RETURN)));
  }

  @Test
  public void testSpecificConversions() {
    assertThat(
        continuouslyCompoundedAnnualizedReturn(onesBasedReturn(1.048790164))
            .toAnnuallyCompoundedAnnualizedReturn(),
        annuallyCompoundedAnnualizedReturnMatcher(
            annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))));
    assertThat(
        continuouslyCompoundedAnnualizedReturn(onesBasedReturn(0.9487067056))
            .toAnnuallyCompoundedAnnualizedReturn(),
        annuallyCompoundedAnnualizedReturnMatcher(
            annuallyCompoundedAnnualizedReturn(onesBasedReturn(0.95))));
  }

  @Test
  public void testConversionToNonAnnualized() {
    assertAlmostEquals(
        onesBasedReturn(1.05),
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toContinuouslyCompoundedAnnualizedReturn()
            .toNonAnnualized(1.0),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        onesBasedReturn(1.05 * 1.05),
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toContinuouslyCompoundedAnnualizedReturn()
            .toNonAnnualized(2.0),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        onesBasedReturn(1.05 * 1.05 * 1.05),
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toContinuouslyCompoundedAnnualizedReturn()
            .toNonAnnualized(3.0),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        onesBasedReturn(doubleExplained(1.02469508, Math.sqrt(1.05))),
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toContinuouslyCompoundedAnnualizedReturn()
            .toNonAnnualized(0.5),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        FLAT_RETURN,
        annuallyCompoundedAnnualizedReturn(onesBasedReturn(1.05))
            .toContinuouslyCompoundedAnnualizedReturn()
            .toNonAnnualized(0),
        DEFAULT_EPSILON_1e_8);
  }

  @Test
  public void testConversions() {
    for (double d : ImmutableList.of(0.9, 0.99, 1.0, 1.01, 1.1)) {
      ContinuouslyCompoundedAnnualizedReturn continuously = continuouslyCompoundedAnnualizedReturn(onesBasedReturn(d));
      assertThat(
          continuously.toAnnuallyCompoundedAnnualizedReturn().toContinuouslyCompoundedAnnualizedReturn(),
          continuouslyCompoundedAnnualizedReturnMatcher(continuously));
    }
    for (double d : ImmutableList.of(0.9, 0.99, 1.0, 1.01, 1.1)) {
      AnnuallyCompoundedAnnualizedReturn annually = annuallyCompoundedAnnualizedReturn(onesBasedReturn(d));
      assertThat(
          annually.toContinuouslyCompoundedAnnualizedReturn().toAnnuallyCompoundedAnnualizedReturn(),
          annuallyCompoundedAnnualizedReturnMatcher(annually));
    }
  }

  @Override
  public ContinuouslyCompoundedAnnualizedReturn makeTrivialObject() {
    return continuouslyCompoundedAnnualizedReturn(FLAT_RETURN);
  }

  @Override
  public ContinuouslyCompoundedAnnualizedReturn makeNontrivialObject() {
    return continuouslyCompoundedAnnualizedReturn(onesBasedReturn(0.99));
  }

  @Override
  public ContinuouslyCompoundedAnnualizedReturn makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return continuouslyCompoundedAnnualizedReturn(onesBasedReturn(0.99 + e));
  }

  @Override
  protected boolean willMatch(ContinuouslyCompoundedAnnualizedReturn expected,
                              ContinuouslyCompoundedAnnualizedReturn actual) {
    return continuouslyCompoundedAnnualizedReturnMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ContinuouslyCompoundedAnnualizedReturn> continuouslyCompoundedAnnualizedReturnMatcher(
      ContinuouslyCompoundedAnnualizedReturn expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getRawReturn(), DEFAULT_EPSILON_1e_8));
  }

}
