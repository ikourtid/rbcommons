package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.RationalUnitFraction.rationalUnitFraction;
import static com.rb.nonbiz.types.RationalUnitFraction.zeroRationalUnitFraction;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class RationalUnitFractionTest extends RBTestMatcher<RationalUnitFraction> {

  @Test
  public void testZeroRationalUnitFraction() {
    RationalUnitFraction zeroRationalUnitFraction = zeroRationalUnitFraction(123);

    assertTrue(zeroRationalUnitFraction.isZero());

    assertEquals(  0, zeroRationalUnitFraction.getNumerator());
    assertEquals(123, zeroRationalUnitFraction.getDenominator());
  }

  @Test
  public void validArguments() {
    assertIllegalArgumentException( () -> rationalUnitFraction(-1, 2));
    RationalUnitFraction doesNotThrow0 = rationalUnitFraction(0, 2);
    RationalUnitFraction doesNotThrow1 = rationalUnitFraction(1, 2);
    RationalUnitFraction doesNotThrow2 = rationalUnitFraction(2, 2);
    assertIllegalArgumentException( () -> rationalUnitFraction(3, 2));

    assertIllegalArgumentException( () -> rationalUnitFraction(-1, 0));
    assertIllegalArgumentException( () -> rationalUnitFraction(0, 0));
    assertIllegalArgumentException( () -> rationalUnitFraction(1, 0));

    assertIllegalArgumentException( () -> rationalUnitFraction( -3, -2));
    assertIllegalArgumentException( () -> rationalUnitFraction( -2, -2));
    assertIllegalArgumentException( () -> rationalUnitFraction( -1, -2));
    assertIllegalArgumentException( () -> rationalUnitFraction( 0, -2));
    assertIllegalArgumentException( () -> rationalUnitFraction( 1, -2));
    assertIllegalArgumentException( () -> rationalUnitFraction( 2, -2));
    assertIllegalArgumentException( () -> rationalUnitFraction( 3, -2));
  }

  @Test
  public void testIsZero() {
    RationalUnitFraction fraction0 = rationalUnitFraction(0, 2);
    RationalUnitFraction fraction0_5 = rationalUnitFraction(1, 2);
    RationalUnitFraction fraction1 = rationalUnitFraction(2, 2);

    assertTrue(fraction0.isZero());
    assertFalse(fraction0_5.isZero());
    assertFalse(fraction1.isZero());
  }

  @Test
  public void testIsOne() {
    RationalUnitFraction fraction0 = rationalUnitFraction(0, 2);
    RationalUnitFraction fraction0_5 = rationalUnitFraction(1, 2);
    RationalUnitFraction fraction1 = rationalUnitFraction(2, 2);

    assertFalse(fraction0.isOne());
    assertFalse(fraction0_5.isOne());
    assertTrue(fraction1.isOne());
  }

  @Test
  public void testInverse() {
    assertEquals(3.5, rationalUnitFraction(2, 7).inverse().getAsDouble(), 1e-8);
    assertFalse(rationalUnitFraction(0, 7).inverse().isPresent());
  }

  @Override
  public RationalUnitFraction makeTrivialObject() {
    return rationalUnitFraction(0, 1);
  }

  @Override
  public RationalUnitFraction makeNontrivialObject() {
    return rationalUnitFraction(123, 456);
  }

  @Override
  public RationalUnitFraction makeMatchingNontrivialObject() {
    return rationalUnitFraction(123, 456);
  }

  @Override
  protected boolean willMatch(RationalUnitFraction expected, RationalUnitFraction actual) {
    return rationalUnitFractionMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RationalUnitFraction> rationalUnitFractionMatcher(RationalUnitFraction expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getNumerator()),
        matchUsingEquals(v -> v.getDenominator()));
  }

}
