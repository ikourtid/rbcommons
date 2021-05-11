package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.BiFunction;

import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.SplitMultiplier.MAX_SPLIT_MULTIPLIER;
import static com.rb.nonbiz.types.SplitMultiplier.MIN_SPLIT_MULTIPLIER;
import static com.rb.nonbiz.types.SplitMultiplier.splitMultiplier;

public class SplitMultiplierTest extends RBTestMatcher<SplitMultiplier> {

  // convenience constructor from double
  public static SplitMultiplier splitMultiplierFromDouble(double multiplier) {
    return splitMultiplier(BigDecimal.valueOf(multiplier));
  }

  @Test
  public void nonPositiveMultiplier_throws() {
    SplitMultiplier doesNotThrow;
    doesNotThrow = splitMultiplierFromDouble(0.0001);
    doesNotThrow = splitMultiplierFromDouble(0.1);
    doesNotThrow = splitMultiplierFromDouble(0.99); // slightly less than 1.0; can't have 1:1 split
    doesNotThrow = splitMultiplierFromDouble(1.01); // slightly more than 1.0
    doesNotThrow = splitMultiplierFromDouble(100.0);
    doesNotThrow = splitMultiplierFromDouble(10_000.0);

    // split multiplier must be positive
    assertIllegalArgumentException( () -> splitMultiplierFromDouble(-1.0));
    assertIllegalArgumentException( () -> splitMultiplierFromDouble(-1e-8));
    assertIllegalArgumentException( () -> splitMultiplierFromDouble( 0.0));
  }

  @Test
  public void singleMultiplierCTOR_multiplierTooLarge_orTooSmall_throws() {
    double e = 1e-9; // epsilon
    // multiplier must not be unreasonably small or large
    assertIllegalArgumentException( () -> splitMultiplierFromDouble(1e-8));
    assertIllegalArgumentException( () -> splitMultiplierFromDouble(1e-5));
    assertIllegalArgumentException( () -> splitMultiplierFromDouble(1e-4 - e));

    SplitMultiplier doesNotThrow;
    doesNotThrow = splitMultiplier(BigDecimal.valueOf(
        doubleExplained(1e-4, MIN_SPLIT_MULTIPLIER.doubleValue())));
    doesNotThrow = splitMultiplier(BigDecimal.valueOf(
        doubleExplained(1e+4, MAX_SPLIT_MULTIPLIER.doubleValue())));

    assertIllegalArgumentException( () -> splitMultiplierFromDouble(1e+4 + e));
    assertIllegalArgumentException( () -> splitMultiplierFromDouble(1e+5));
    assertIllegalArgumentException( () -> splitMultiplierFromDouble(1e+8));
  }

  @Test
  public void numeratorAndDenominatorCTOR_ratioTooLarge_orTooSmall_throws() {
    BiFunction<Double, Double, SplitMultiplier> maker = (numerator, denominator) ->
        splitMultiplier(positiveQuantity(numerator), positiveQuantity(denominator));

    double e = 1e-9; // epsilon
    // multiplier must not be unreasonably small or large
    assertIllegalArgumentException( () -> maker.apply(1.0, 1_000_000.0));
    assertIllegalArgumentException( () -> maker.apply(1.0,   100_000.0));
    assertIllegalArgumentException( () -> maker.apply(1.0,    10_000.0 + e));

    assertIllegalArgumentException( () -> maker.apply(1.234, 1_234_000.0));
    assertIllegalArgumentException( () -> maker.apply(1.234,   123_400.0));
    assertIllegalArgumentException( () -> maker.apply(1.234,    12_340.0 + e));

    SplitMultiplier doesNotThrow;
    doesNotThrow = maker.apply(1.0, doubleExplained(10_000.0, MAX_SPLIT_MULTIPLIER.doubleValue()));
    doesNotThrow = maker.apply(doubleExplained(10_000.0, MAX_SPLIT_MULTIPLIER.doubleValue()), 1.0);
    doesNotThrow = maker.apply(1.234, 12_340.0);
    doesNotThrow = maker.apply(12_340.0, 1.234);

    assertIllegalArgumentException( () -> maker.apply(1_000_000.0,     1.0));
    assertIllegalArgumentException( () -> maker.apply(  100_000.0,     1.0));
    assertIllegalArgumentException( () -> maker.apply(   10_000.0 + e, 1.0));

    assertIllegalArgumentException( () -> maker.apply(1_234_000.0, 1.234));
    assertIllegalArgumentException( () -> maker.apply(  123_400.0, 1.234));
    assertIllegalArgumentException( () -> maker.apply(   12_340.0, 1.234 - e));
  }

  @Test
  public void split1To1_orNToN_throws() {
    BiFunction<Double, Double, SplitMultiplier> maker = (numerator, denominator) ->
        splitMultiplier(positiveQuantity(numerator), positiveQuantity(denominator));

    SplitMultiplier doesNotThrow;
    // a split ratio should not be 1.0
    doesNotThrow = maker.apply(1.0, 2.0);
    doesNotThrow = maker.apply(2.0, 1.0);
    doesNotThrow = maker.apply(123.456, 789.123);
    doesNotThrow = maker.apply(789.123, 123.456);

    // all of the following represent an X : X split
    assertIllegalArgumentException( () -> maker.apply(0.123, 0.123));
    assertIllegalArgumentException( () -> maker.apply(1.0, 1.0));
    assertIllegalArgumentException( () -> maker.apply(2.0, 2.0));
    assertIllegalArgumentException( () -> maker.apply(789.0, 789.0));
  }

  @Override
  public SplitMultiplier makeTrivialObject() {
    return splitMultiplier(BigDecimal.valueOf(2.0)); // 2:1 split
  }

  @Override
  public SplitMultiplier makeNontrivialObject() {
    return splitMultiplier(BigDecimal.valueOf(1.234));
  }

  @Override
  public SplitMultiplier makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return splitMultiplier(BigDecimal.valueOf(1.234 + e));
  }

  @Override
  protected boolean willMatch(SplitMultiplier expected, SplitMultiplier actual) {
    return splitMultiplierMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SplitMultiplier> splitMultiplierMatcher(
      SplitMultiplier expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v,                  1e-8),
        matchUsingAlmostEquals(v -> v.getNumerator(),   1e-8),
        matchUsingAlmostEquals(v -> v.getDenominator(), 1e-8));
  }

}
