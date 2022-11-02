package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.DoubleFunction;
import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange.closedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRangeTest.closedUnitFractionHardAndSoftRangeMatcher;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRangeTest.unrestrictedClosedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.TargetWithClosedUnitFractionHardAndSoftRange.targetWithClosedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class TargetWithClosedUnitFractionHardAndSoftRangeTest
    extends RBTestMatcher<TargetWithClosedUnitFractionHardAndSoftRange> {

  public static TargetWithClosedUnitFractionHardAndSoftRange testTargetWithClosedUnitFractionHardAndSoftRangeWithSeed(
      double seed) {
    return targetWithClosedUnitFractionHardAndSoftRange(
        unitFraction(0.5 + seed),
        closedUnitFractionHardAndSoftRange(
            closedUnitFractionRange(unitFraction(0.1 + seed), unitFraction(0.8 + seed)),
            closedUnitFractionRange(unitFraction(0.4 + seed), unitFraction(0.6 + seed))));
  }

  @Test
  public void targetMustBeInsideRange() {
    Function<UnitFraction, TargetWithClosedUnitFractionHardAndSoftRange> maker = target ->
        targetWithClosedUnitFractionHardAndSoftRange(
            target,
            closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(unitFraction(0.1), unitFraction(0.8)),
                closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6))));
    assertIllegalArgumentException( () -> maker.apply(UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.4 - 1e-9)));
    TargetWithClosedUnitFractionHardAndSoftRange doesNotThrow;
    doesNotThrow = maker.apply(unitFraction(0.4));
    doesNotThrow = maker.apply(unitFraction(0.5));
    doesNotThrow = maker.apply(unitFraction(0.6));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.6 + 1e-9)));
  }

  @Override
  public TargetWithClosedUnitFractionHardAndSoftRange makeTrivialObject() {
    return targetWithClosedUnitFractionHardAndSoftRange(
        UNIT_FRACTION_0,
        unrestrictedClosedUnitFractionHardAndSoftRange());
  }

  @Override
  public TargetWithClosedUnitFractionHardAndSoftRange makeNontrivialObject() {
    return testTargetWithClosedUnitFractionHardAndSoftRangeWithSeed(ZERO_SEED);
  }

  @Override
  public TargetWithClosedUnitFractionHardAndSoftRange makeMatchingNontrivialObject() {
    return testTargetWithClosedUnitFractionHardAndSoftRangeWithSeed(EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(TargetWithClosedUnitFractionHardAndSoftRange expected,
                              TargetWithClosedUnitFractionHardAndSoftRange actual) {
    return targetWithClosedUnitFractionHardAndSoftRangeMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<TargetWithClosedUnitFractionHardAndSoftRange>
      targetWithClosedUnitFractionHardAndSoftRangeMatcher(TargetWithClosedUnitFractionHardAndSoftRange expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getTarget(), 1e-8),
        match(v -> v.getClosedUnitFractionHardAndSoftRange(), f -> closedUnitFractionHardAndSoftRangeMatcher(f)));
  }

}
