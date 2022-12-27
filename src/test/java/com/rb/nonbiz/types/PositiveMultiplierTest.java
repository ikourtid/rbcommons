package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PositiveMultiplier.POSITIVE_MULTIPLIER_1;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;

public class PositiveMultiplierTest extends RBTestMatcher<PositiveMultiplier> {

  @Test
  public void nonPositiveMultiplier_throws() {
    Function<Double, PositiveMultiplier> maker = multiplier ->
        positiveMultiplier(multiplier);

    assertIllegalArgumentException( () -> maker.apply(-1.23e9));
    assertIllegalArgumentException( () -> maker.apply(-1.23));
    assertIllegalArgumentException( () -> maker.apply(-1e-9));
    assertIllegalArgumentException( () -> maker.apply( 0.0));

    PositiveMultiplier doesNotThrow;
    doesNotThrow = maker.apply(1e-9);
    doesNotThrow = maker.apply(0.123);
    doesNotThrow = maker.apply(0.999);
    doesNotThrow = maker.apply(1.0);
    doesNotThrow = maker.apply(2.345);
    doesNotThrow = maker.apply(1.23e9);
  }

  @Test
  public void testMultiply() {
    assertAlmostEquals(
        positiveMultiplier(doubleExplained(1.2, 1.5 * 0.8)),
        positiveMultiplier(1.5).multiply(positiveMultiplier(0.8)),
        1e-8);
  }

  @Test
  public void testInverse() {
    Consumer<Double> asserter = multiplier -> {
      assertAlmostEquals(
          positiveMultiplier(1.0 / multiplier),
          positiveMultiplier(multiplier).inverse(),
          1e-8);
      // the inverse of the inverse should be the original
      assertAlmostEquals(
          positiveMultiplier(multiplier),
          positiveMultiplier(multiplier).inverse().inverse(),
          1e-8);
    };

    asserter.accept(0.123);
    asserter.accept(0.5);
    asserter.accept(1.0);
    asserter.accept(2.0);
    asserter.accept(123.0);
  }

  @Override
  public PositiveMultiplier makeTrivialObject() {
    return POSITIVE_MULTIPLIER_1;
  }

  @Override
  public PositiveMultiplier makeNontrivialObject() {
    return positiveMultiplier(1.23);
  }

  @Override
  public PositiveMultiplier makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return positiveMultiplier(1.23 + e);
  }

  @Override
  protected boolean willMatch(PositiveMultiplier expected, PositiveMultiplier actual) {
    return impreciseValueMatcher(expected, DEFAULT_EPSILON_1e_8).matches(actual);
  }

}
