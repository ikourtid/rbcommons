package com.rb.nonbiz.functional;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.PositiveMultiplier;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.DoubleFunction;
import java.util.function.Function;

import static com.rb.nonbiz.functional.RBNumericFunction.rbIdentityNumericFunction;
import static com.rb.nonbiz.functional.RBNumericFunctionTest.rbNumericFunctionUsingSamplingMatcher;
import static com.rb.nonbiz.functional.UnitFractionFunction.unitFractionFunction;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_LABEL;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_MULTIPLIER;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInBps;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;

public class UnitFractionFunctionTest extends RBTestMatcher<UnitFractionFunction<PositiveMultiplier>> {

  @Test
  public void hasValuesForAllValidUnitFractions_otherwiseThrows() {
    Function<DoubleFunction<PositiveMultiplier>, UnitFractionFunction<PositiveMultiplier>> maker =
        doubleFunction -> unitFractionFunction(rbIdentityNumericFunction(doubleFunction));

    UnitFractionFunction<PositiveMultiplier> doesNotThrow = maker.apply(v -> DUMMY_POSITIVE_MULTIPLIER);

    assertIllegalArgumentException( () -> maker.apply(v -> {
      // throw if the valid range of unit fractions starts at 0.123, i.e. does not cover 0 to 1.
      if (v < 0.123) {
        throw new IllegalArgumentException();
      } else {
        return DUMMY_POSITIVE_MULTIPLIER;
      }
    }));

    // complement of the above
    assertIllegalArgumentException( () -> maker.apply(v -> {
      if (v >= 0.123) {
        throw new IllegalArgumentException();
      } else {
        return DUMMY_POSITIVE_MULTIPLIER;
      }
    }));
  }

  @Override
  public UnitFractionFunction<PositiveMultiplier> makeTrivialObject() {
    // Unfortunately, we cannot use rbIdentityNumericFunction here, because UNIT_FRACTION_0 is a valid UnitFraction,
    // but PositiveMultiplier can only be positive. So the trivial object will look a bit less trivial here.
    return unitFractionFunction(
        RBNumericFunction.<UnitFraction, PositiveMultiplier>rbNumericFunction(
            DUMMY_LABEL,
            v -> 1 + v,
            v -> positiveMultiplier(v)));
  }

  @Override
  public UnitFractionFunction<PositiveMultiplier> makeNontrivialObject() {
    return unitFractionFunction(
        RBNumericFunction.<UnitFraction, PositiveMultiplier>rbNumericFunction(
            DUMMY_LABEL,
            v -> 0.123 + 0.456 * v,
            v -> positiveMultiplier(v)));
  }

  @Override
  public UnitFractionFunction<PositiveMultiplier> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return unitFractionFunction(
        RBNumericFunction.<UnitFraction, PositiveMultiplier>rbNumericFunction(
            DUMMY_LABEL,
            v -> 0.123 + 0.456 * v + e,
            v -> positiveMultiplier(v)));
  }

  @Override
  protected boolean willMatch(UnitFractionFunction<PositiveMultiplier> expected,
                              UnitFractionFunction<PositiveMultiplier> actual) {
    return unitFractionFunctionUsingSamplingMatcher(
        expected,
        UNIT_FRACTION_0,
        unitFractionInBps(1),
        unitFractionInBps(10),
        unitFractionInPct(1),
        unitFractionInPct(10),
        UNIT_FRACTION_1)
        .matches(actual);
  }

  // We can never match function objects f and g exactly, because we cannot enumerate all X. However, for testing purposes,
  // it is convenient to be able to at least that f(x_i) ~= g(x_i) for i = 0, ... (for a few points).
  // We will never be 100% sure that the functions match, but it's better than not checking anything at all.
  public static <Y extends RBNumeric<? super Y>> TypeSafeMatcher<UnitFractionFunction<Y>>
  unitFractionFunctionUsingSamplingMatcher(
      UnitFractionFunction<Y> expected, UnitFraction firstSamplePoint, UnitFraction ... restSamplePoints) {
    return makeMatcher(expected,
        match(v -> v.getRawRbNumericFunction(), f -> rbNumericFunctionUsingSamplingMatcher(
            f, firstSamplePoint, restSamplePoints)));
  }

}
