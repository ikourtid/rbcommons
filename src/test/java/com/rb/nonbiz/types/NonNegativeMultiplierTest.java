package com.rb.nonbiz.types;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.NonNegativeMultiplier.NON_NEGATIVE_MULTIPLIER_0;
import static com.rb.nonbiz.types.NonNegativeMultiplier.NON_NEGATIVE_MULTIPLIER_1;
import static com.rb.nonbiz.types.NonNegativeMultiplier.nonNegativeMultiplier;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class NonNegativeMultiplierTest extends RBTestMatcher<NonNegativeMultiplier> {

  @Test
  public void negativeMultiplier_throws() {
    Function<Double, NonNegativeMultiplier> maker = multiplier ->
        nonNegativeMultiplier(multiplier);

    assertIllegalArgumentException( () -> maker.apply(-1.23e9));
    assertIllegalArgumentException( () -> maker.apply(-1.23));
    assertIllegalArgumentException( () -> maker.apply(-1e-9));

    NonNegativeMultiplier doesNotThrow;
    doesNotThrow = maker.apply(0.0);
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
        nonNegativeMultiplier(doubleExplained(1.2, 1.5 * 0.8)),
        nonNegativeMultiplier(1.5).multiply(nonNegativeMultiplier(0.8)),
        1e-8);

    assertAlmostEquals(
        nonNegativeMultiplier(doubleExplained(1.5, 1.5 * 1.0)),
        nonNegativeMultiplier(1.5).multiply(NON_NEGATIVE_MULTIPLIER_1),
        1e-8);

    assertAlmostEquals(
        NON_NEGATIVE_MULTIPLIER_0,
        nonNegativeMultiplier(1.5).multiply(NON_NEGATIVE_MULTIPLIER_0),
        1e-8);

    assertAlmostEquals(
        NON_NEGATIVE_MULTIPLIER_0,
        NON_NEGATIVE_MULTIPLIER_0.multiply(nonNegativeMultiplier(0.8)),
        1e-8);
  }

  @Test
  public void testSum() {
    BiConsumer<Double, List<Double>> asserter = (expectedSum, inputList) ->
        assertAlmostEquals(
            nonNegativeMultiplier(expectedSum),
            NonNegativeMultiplier.sum(inputList.stream().map(v -> nonNegativeMultiplier(v)).collect(Collectors.toList())),
            1e-8);
    asserter.accept(0.0, emptyList());
    asserter.accept(1.1, singletonList(1.1));
    asserter.accept(4.4, ImmutableList.of(3.3, 1.1));
    asserter.accept(3.3, ImmutableList.of(3.3, 0.0));
    asserter.accept(3.3, ImmutableList.of(0.0, 3.3));
  }

  @Override
  public NonNegativeMultiplier makeTrivialObject() {
    return NON_NEGATIVE_MULTIPLIER_0;
  }

  @Override
  public NonNegativeMultiplier makeNontrivialObject() {
    return nonNegativeMultiplier(1.23);
  }

  @Override
  public NonNegativeMultiplier makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return nonNegativeMultiplier(1.23 + e);
  }

  @Override
  protected boolean willMatch(NonNegativeMultiplier expected, NonNegativeMultiplier actual) {
    return impreciseValueMatcher(expected, 1e-8).matches(actual);
  }

}
