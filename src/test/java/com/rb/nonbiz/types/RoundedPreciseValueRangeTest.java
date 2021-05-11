package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.biz.types.trading.SignedQuantity;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.types.trading.RoundingScale.INTEGER_ROUNDING_SCALE;
import static com.rb.biz.types.trading.RoundingScale.roundingScale;
import static com.rb.biz.types.trading.SignedQuantity.ZERO_SIGNED_QUANTITY;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rangeEqualityMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.RoundedPreciseValueRange.roundedPreciseValueRange;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class RoundedPreciseValueRangeTest extends RBTestMatcher <RoundedPreciseValueRange<SignedQuantity>>{

  @Test
  public void notRoundToScale_throws() {
    // set the upper bound to a very round number to focus on the lower bound
    Function<Integer, RoundedPreciseValueRange<SignedQuantity>> maker = scale ->
        roundedPreciseValueRange(Range.closed(signedQuantity(0.123), signedQuantity(1_000)), roundingScale(scale));

    assertIllegalArgumentException( () -> maker.apply(-2));  // 0.123 not rounded to hundreds
    assertIllegalArgumentException( () -> maker.apply(-1));  // 0.123 not rounded to tens
    assertIllegalArgumentException( () -> maker.apply( 0));  // 0.123 not rounded to integer
    assertIllegalArgumentException( () -> maker.apply( 1));  // 0.123 not rounded to 0.1
    assertIllegalArgumentException( () -> maker.apply( 2));  // 0.123 not rounded to 0.01

    RoundedPreciseValueRange<SignedQuantity> doesNotThrow;
    doesNotThrow =  maker.apply(3);  // 0.123 IS rounded to 3 digits
    doesNotThrow =  maker.apply(4);  // 0.123 IS rounded to 4 digits
    doesNotThrow =  maker.apply(8);  // 0.123 IS rounded to 8 digits
  }

  @Test
  public void scaleTooSmall_orTooLarge_throws() {
    Function<Integer, RoundedPreciseValueRange<SignedQuantity>> maker = scale ->
        roundedPreciseValueRange(Range.atLeast(ZERO_SIGNED_QUANTITY), roundingScale(scale));

    assertIllegalArgumentException( () -> maker.apply(-11));
    RoundedPreciseValueRange<SignedQuantity> doesNotThrow;
    doesNotThrow = maker.apply(-10);
    doesNotThrow = maker.apply(-1);
    doesNotThrow = maker.apply(0);
    doesNotThrow = maker.apply(1);
    doesNotThrow = maker.apply(10);
    assertIllegalArgumentException( () -> maker.apply(11));
  }

  public void invertedRange_throws() {
    // set the upper bound to a very round number to focus on the lower bound
    Function<Integer, RoundedPreciseValueRange<SignedQuantity>> maker = scale ->
        roundedPreciseValueRange(Range.closed(signedQuantity(0.1), signedQuantity(1_000)), roundingScale(scale));

    assertIllegalArgumentException( () -> maker.apply(-2));  // 0.1 not rounded to hundreds
    assertIllegalArgumentException( () -> maker.apply(-1));  // 0.1 not rounded to tens
    assertIllegalArgumentException( () -> maker.apply( 0));  // 0.1 not rounded to integer

    RoundedPreciseValueRange<SignedQuantity> doesNotThrow;
    doesNotThrow =  maker.apply(1);  // 0.1 IS rounded to 1 digit
    doesNotThrow =  maker.apply(2);  // 0.1 IS rounded to 2 digits
    doesNotThrow =  maker.apply(8);  // 0.1 IS rounded to 8 digits
  }

  @Override
  public RoundedPreciseValueRange<SignedQuantity> makeTrivialObject() {
    return RoundedPreciseValueRange.<SignedQuantity>roundedPreciseValueRange(Range.all(), INTEGER_ROUNDING_SCALE);
  }

  @Override
  public RoundedPreciseValueRange<SignedQuantity> makeNontrivialObject() {
    return roundedPreciseValueRange(Range.closed(signedQuantity(-1.23), signedQuantity(4.56)), roundingScale(2));
  }

  @Override
  public RoundedPreciseValueRange<SignedQuantity> makeMatchingNontrivialObject() {
    // no epsilon offset; range endpoints must be exactly round
    return roundedPreciseValueRange(Range.closed(signedQuantity(-1.23), signedQuantity(4.56)), roundingScale(2));
  }

  @Override
  protected boolean willMatch(
      RoundedPreciseValueRange<SignedQuantity> expected,
      RoundedPreciseValueRange<SignedQuantity> actual) {
    return roundedPreciseValueRangeMatcher(expected).matches(actual);
  }

  public static <P extends PreciseValue<? super P>> TypeSafeMatcher<RoundedPreciseValueRange<P>>
  roundedPreciseValueRangeMatcher(RoundedPreciseValueRange<P> expected) {
    return makeMatcher(expected,
        match(v -> v.getRawRange(), f -> rangeEqualityMatcher(f)),
        matchUsingEquals(v -> v.getRoundingScale().getRawInt()));
  }

}
