package com.rb.biz.types;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.biz.investing.quality.AnnuallyCompoundedAnnualizedReturn.annuallyCompoundedAnnualizedReturn;
import static com.rb.biz.investing.quality.AnnuallyCompoundedAnnualizedReturnTest.annuallyCompoundedAnnualizedReturnMatcher;
import static com.rb.biz.types.OnesBasedReturn.FLAT_RETURN;
import static com.rb.biz.types.OnesBasedReturn.onesBasedGain;
import static com.rb.biz.types.OnesBasedReturn.onesBasedLoss;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class OnesBasedReturnTest {

  @Test
  public void returnIsTooBig_bigDecimal_throws() {
    assertIllegalArgumentException( () -> onesBasedReturn(BigDecimal.valueOf(10_000.01)));
  }

  @Test
  public void returnIsTooBig_double_throws() {
    assertIllegalArgumentException( () -> onesBasedReturn(10_000.01));
  }

  @Test
  public void returnIsTooSmall_bigDecimal_throws() {
    assertIllegalArgumentException( () -> onesBasedReturn(BigDecimal.valueOf(0.0001 - 1e-8)));
  }

  @Test
  public void returnIsTooSmall_double_throws() {
    assertIllegalArgumentException( () -> onesBasedReturn(0.0001 - 1e-8));
  }

  @Test
  public void validReturns_doesNotThrow() {
    for (double d : ImmutableList.<Double>of(0.0001, 0.0002, 0.99, 1.0, 1.01, 9_999.0)) {
      OnesBasedReturn unused1 = onesBasedReturn(d);
      OnesBasedReturn unused2 = onesBasedReturn(BigDecimal.valueOf(d));
    }
  }

  @Test
  public void asZeroBasedReturnBigDecimal() {
    assertEquals(BigDecimal.ZERO, FLAT_RETURN.asZeroBasedReturnBigDecimal());
    assertEquals(BigDecimal.valueOf(-0.1), onesBasedReturn(0.9).asZeroBasedReturnBigDecimal());
    assertEquals(BigDecimal.valueOf(0.2), onesBasedReturn(1.2).asZeroBasedReturnBigDecimal());
  }

  @Test
  public void toZeroBasedReturnBasisPoints() {
    assertEquals("3333.33 bps", onesBasedReturn(1 + 1 / 3.0).toBasisPoints(2));
    assertEquals("3333.3 bps", onesBasedReturn(1 + 1 / 3.0).toBasisPoints(1));
    assertEquals("3333 bps", onesBasedReturn(1 + 1 / 3.0).toBasisPoints(0));
    assertEquals("0.00 bps", FLAT_RETURN.toBasisPoints(2));
    assertEquals("0.0 bps", FLAT_RETURN.toBasisPoints(1));
    assertEquals("0 bps", FLAT_RETURN.toBasisPoints(0));
    assertEquals("30000.00 bps", onesBasedReturn(4.0).toBasisPoints(2));
    assertEquals("30000.0 bps", onesBasedReturn(4.0).toBasisPoints(1));
    assertEquals("30000 bps", onesBasedReturn(4.0).toBasisPoints(0));
    assertEquals("1.23 bps", onesBasedReturn(1.000123).toBasisPoints(2));
    assertEquals("1.2 bps", onesBasedReturn(1.000123).toBasisPoints(1));
    assertEquals("1 bps", onesBasedReturn(1.000123).toBasisPoints(0));
  }

  @Test
  public void testToGain() {
    assertAlmostEquals(FLAT_RETURN, FLAT_RETURN.toGain(), DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(onesBasedReturn(doubleExplained(1.25, 1 / 0.8)), onesBasedReturn(0.8).toGain(), DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(onesBasedReturn(1.25), onesBasedReturn(1.25).toGain(), DEFAULT_EPSILON_1e_8);
  }

  @Test
  public void testTimeAdjust() {
    assertIllegalArgumentException( () -> FLAT_RETURN.timeScaleAdjust(0));
    assertIllegalArgumentException( () -> FLAT_RETURN.timeScaleAdjust(-0.1));
    assertAlmostEquals(FLAT_RETURN, FLAT_RETURN.timeScaleAdjust(1), DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        onesBasedGain(doubleExplained(1.10462213, Math.pow(1.01, 10))),
        onesBasedGain(1.01).timeScaleAdjust(10),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        onesBasedGain(1.01),
        onesBasedGain(1.10462213).timeScaleAdjust(1 / 10.0),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        onesBasedLoss(doubleExplained(0.904382075, Math.pow(0.99, 10))),
        onesBasedLoss(0.99).timeScaleAdjust(10),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        onesBasedLoss(0.99),
        onesBasedLoss(0.904382075).timeScaleAdjust(1 / 10.0),
        DEFAULT_EPSILON_1e_8);
  }

  @Test
  public void testAnnualization() {
    assertThat(
        onesBasedGain(1.0001).toAnnuallyCompoundedAnnualizedReturn(5),
        annuallyCompoundedAnnualizedReturnMatcher(
            annuallyCompoundedAnnualizedReturn(onesBasedGain(doubleExplained(
                1.0073263423049925, Math.pow(1.0001, 365 / 5.0))))));
  }

  @Test
  public void testCompoundAndResidualReturnOver() {
    // Using variable names that denote realistic examples, but these don't have to be pre- and after-tax.
    OnesBasedReturn preTax = onesBasedGain(1.2);
    OnesBasedReturn taxAlpha = onesBasedGain(1.05);
    OnesBasedReturn afterTax = onesBasedGain(doubleExplained(1.26, 1.2 * 1.05));
    assertAlmostEquals(afterTax, preTax.compoundWith(taxAlpha), DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(afterTax, taxAlpha.compoundWith(preTax), DEFAULT_EPSILON_1e_8);

    assertAlmostEquals(taxAlpha, afterTax.residualReturnOver(preTax), DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(preTax, afterTax.residualReturnOver(taxAlpha), DEFAULT_EPSILON_1e_8);
  }

}
