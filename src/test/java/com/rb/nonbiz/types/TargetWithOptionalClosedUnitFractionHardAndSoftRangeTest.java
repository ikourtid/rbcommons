package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange.closedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRangeTest.closedUnitFractionHardAndSoftRangeMatcher;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.TargetWithOptionalClosedUnitFractionHardAndSoftRange.targetWithClosedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.TargetWithOptionalClosedUnitFractionHardAndSoftRange.targetWithoutClosedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class TargetWithOptionalClosedUnitFractionHardAndSoftRangeTest
    extends RBTestMatcher<TargetWithOptionalClosedUnitFractionHardAndSoftRange> {

  public static TargetWithOptionalClosedUnitFractionHardAndSoftRange testTargetWithClosedUnitFractionHardAndSoftRangeWithSeed(
      double seed) {
    return targetWithClosedUnitFractionHardAndSoftRange(
        unitFraction(0.5 + seed),
        closedUnitFractionHardAndSoftRange(
            closedUnitFractionRange(unitFraction(0.1 + seed), unitFraction(0.8 + seed)),
            closedUnitFractionRange(unitFraction(0.4 + seed), unitFraction(0.6 + seed))));
  }

  @Test
  public void targetMustBeInsideSoftRange() {
    Function<UnitFraction, TargetWithOptionalClosedUnitFractionHardAndSoftRange> maker = target ->
        targetWithClosedUnitFractionHardAndSoftRange(
            target,
            closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(unitFraction(0.1), unitFraction(0.8)),
                closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6))));
    assertIllegalArgumentException( () -> maker.apply(UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.4 - 1e-9)));
    TargetWithOptionalClosedUnitFractionHardAndSoftRange doesNotThrow;
    doesNotThrow = maker.apply(unitFraction(0.4));
    doesNotThrow = maker.apply(unitFraction(0.5));
    doesNotThrow = maker.apply(unitFraction(0.6));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.6 + 1e-9)));
  }

  @Override
  public TargetWithOptionalClosedUnitFractionHardAndSoftRange makeTrivialObject() {
    return targetWithoutClosedUnitFractionHardAndSoftRange(UNIT_FRACTION_0);
  }

  @Override
  public TargetWithOptionalClosedUnitFractionHardAndSoftRange makeNontrivialObject() {
    return testTargetWithClosedUnitFractionHardAndSoftRangeWithSeed(ZERO_SEED);
  }

  @Override
  public TargetWithOptionalClosedUnitFractionHardAndSoftRange makeMatchingNontrivialObject() {
    return testTargetWithClosedUnitFractionHardAndSoftRangeWithSeed(EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(TargetWithOptionalClosedUnitFractionHardAndSoftRange expected,
                              TargetWithOptionalClosedUnitFractionHardAndSoftRange actual) {
    return targetWithClosedUnitFractionHardAndSoftRangeMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<TargetWithOptionalClosedUnitFractionHardAndSoftRange>
      targetWithClosedUnitFractionHardAndSoftRangeMatcher(TargetWithOptionalClosedUnitFractionHardAndSoftRange expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getTarget(),                             DEFAULT_EPSILON_1e_8),
        matchOptional(         v -> v.getClosedUnitFractionHardAndSoftRange(), f -> closedUnitFractionHardAndSoftRangeMatcher(f)));
  }

}
