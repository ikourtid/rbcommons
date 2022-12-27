package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.RBRanges.rbNumericRangeIsAlmostThisSinglePoint;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.preciseValueRangeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.ClosedSignedFractionRange.closedSignedFractionRange;
import static com.rb.nonbiz.types.ClosedSignedFractionRange.signedFractionFixedTo;
import static com.rb.nonbiz.types.ClosedSignedFractionRange.signedFractionFixedToOne;
import static com.rb.nonbiz.types.ClosedSignedFractionRange.signedFractionFixedToZero;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClosedSignedFractionRangeTest extends RBTestMatcher<ClosedSignedFractionRange> {

  @Test
  public void testSpecialRanges() {
    BiConsumer<ClosedSignedFractionRange, SignedFraction> asserter =
        (closedSignedFractionRange, signedFraction) ->
            assertTrue(
                rbNumericRangeIsAlmostThisSinglePoint(
                    closedSignedFractionRange.asSignedFractionRange(), signedFraction, 1e-8));

    asserter.accept(signedFractionFixedToZero(), SIGNED_FRACTION_0);
    asserter.accept(signedFractionFixedToOne(),  SIGNED_FRACTION_1);

    asserter.accept(signedFractionFixedTo(signedFraction(-0.123)), signedFraction(-0.123));
    asserter.accept(signedFractionFixedTo(signedFraction( 0.123)), signedFraction( 0.123));
  }

  @Test
  public void testBounds() {
    ClosedSignedFractionRange closedSignedFractionRange = closedSignedFractionRange(
        signedFraction(-0.123),
        signedFraction( 0.789));

    assertEquals(
        signedFraction(-0.123),
        closedSignedFractionRange.lowerEndpoint());

    assertEquals(
        signedFraction(0.789),
        closedSignedFractionRange.upperEndpoint());
  }

  @Test
  public void testAsDoubleRange() {
    assertEquals(
        Range.closed(-0.123, 0.789),
        closedSignedFractionRange(
            signedFraction(-0.123),
            signedFraction( 0.789)).asDoubleRange());
  }

  @Test
  public void valuesReversed_throws() {
    assertIllegalArgumentException( () -> closedSignedFractionRange(signedFraction(0.33), signedFraction(0.22)));
  }

  @Override
  public ClosedSignedFractionRange makeTrivialObject() {
    return closedSignedFractionRange(SIGNED_FRACTION_0, SIGNED_FRACTION_0);
  }

  @Override
  public ClosedSignedFractionRange makeNontrivialObject() {
    return closedSignedFractionRange(signedFraction(-1.11), signedFraction(2.22));
  }

  @Override
  public ClosedSignedFractionRange makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return closedSignedFractionRange(signedFraction(-1.11 + e), signedFraction(2.22 + e));
  }

  @Override
  protected boolean willMatch(ClosedSignedFractionRange expected, ClosedSignedFractionRange actual) {
    return closedSignedFractionRangeMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ClosedSignedFractionRange> closedSignedFractionRangeMatcher(
      ClosedSignedFractionRange expected) {
    return makeMatcher(expected,
        match(v -> v.asSignedFractionRange(), f -> preciseValueRangeMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

}
