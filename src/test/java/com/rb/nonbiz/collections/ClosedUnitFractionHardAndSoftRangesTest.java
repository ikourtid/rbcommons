package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange;
import com.rb.nonbiz.types.ClosedUnitFractionRange;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.collections.ClosedUnitFractionHardAndSoftRanges.closedUnitFractionHardAndSoftRanges;
import static com.rb.nonbiz.collections.ClosedUnitFractionHardAndSoftRanges.emptyClosedUnitFractionHardAndSoftRanges;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange.closedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRangeTest.closedUnitFractionHardAndSoftRangeMatcher;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRangeTest.unrestrictedClosedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unrestrictedClosedUnitFractionRange;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

// This test class is not generic, but the publicly exposed static matcher is
public class ClosedUnitFractionHardAndSoftRangesTest extends RBTestMatcher<ClosedUnitFractionHardAndSoftRanges<String>> {

  @Test
  public void unrestrictedRangesAreDisallowed() {
    Function<ClosedUnitFractionRange, ClosedUnitFractionHardAndSoftRanges> maker =
        softRange -> closedUnitFractionHardAndSoftRanges(singletonRBMap(
            DUMMY_STRING, closedUnitFractionHardAndSoftRange(unrestrictedClosedUnitFractionRange(), softRange)));
    assertIllegalArgumentException( () -> maker.apply(
        unrestrictedClosedUnitFractionRange()));
    ClosedUnitFractionHardAndSoftRanges doesNotThrow = maker.apply(
        closedUnitFractionRange(unitFraction(0.98), unitFraction(0.99)));
  }

  @Test
  public void testCopyWithOverrideIfUnrestricted() {
    ClosedUnitFractionHardAndSoftRanges<String> initial = closedUnitFractionHardAndSoftRanges(singletonRBMap(
        "R", closedUnitFractionHardAndSoftRange(
            closedUnitFractionRange(unitFraction(0.1), unitFraction(0.8)),
            closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6)))));
    ClosedUnitFractionHardAndSoftRange notUnrestricted = closedUnitFractionHardAndSoftRange(
        closedUnitFractionRange(unitFraction(0.11), unitFraction(0.81)),
        closedUnitFractionRange(unitFraction(0.41), unitFraction(0.61)));

    assertThat(
        "If we try to override with an unrestricted range, we remove 'R' to mean 'no ranges'",
        initial.copyWithOverrideIfUnrestricted("R", unrestrictedClosedUnitFractionHardAndSoftRange()),
        closedUnitFractionHardAndSoftRangesMatcher(emptyClosedUnitFractionHardAndSoftRanges()));

    assertThat(
        "Replacing R to the new value",
        initial.copyWithOverrideIfUnrestricted("R", notUnrestricted),
        closedUnitFractionHardAndSoftRangesMatcher(
            closedUnitFractionHardAndSoftRanges(singletonRBMap(
                "R", notUnrestricted))));

    // Adding an altogether new value will throw
    assertIllegalArgumentException( () ->
        initial.copyWithOverrideIfUnrestricted("new", unrestrictedClosedUnitFractionHardAndSoftRange()));
    assertIllegalArgumentException( () ->
        initial.copyWithOverrideIfUnrestricted("new", notUnrestricted));
  }

  @Override
  public ClosedUnitFractionHardAndSoftRanges<String> makeTrivialObject() {
    return emptyClosedUnitFractionHardAndSoftRanges();
  }

  @Override
  public ClosedUnitFractionHardAndSoftRanges<String> makeNontrivialObject() {
    return closedUnitFractionHardAndSoftRanges(
        rbMapOf(
            // the soft range for A is much narrower than the hard range, but that's valid.
            "a", closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(UNIT_FRACTION_0, UNIT_FRACTION_1),
                closedUnitFractionRange(unitFraction(0.98), unitFraction(0.99))),
            "b", closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(unitFraction(0.1), unitFraction(0.9)),
                closedUnitFractionRange(unitFraction(0.11), unitFraction(0.89))),
            "c", closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6)),
                closedUnitFractionRange(unitFraction(0.41), unitFraction(0.59)))));
  }

  @Override
  public ClosedUnitFractionHardAndSoftRanges<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return closedUnitFractionHardAndSoftRanges(
        rbMapOf(
            // the soft range for A is much narrower than the hard range, but that's valid.
            "a", closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(UNIT_FRACTION_0, UNIT_FRACTION_1),
                closedUnitFractionRange(unitFraction(0.98 + e), unitFraction(0.99 + e))),
            "b", closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(unitFraction(0.1 + e), unitFraction(0.9 + e)),
                closedUnitFractionRange(unitFraction(0.11 + e), unitFraction(0.89 + e))),
            "c", closedUnitFractionHardAndSoftRange(
                closedUnitFractionRange(unitFraction(0.4 + e), unitFraction(0.6 + e)),
                closedUnitFractionRange(unitFraction(0.41 + e), unitFraction(0.59 + e)))));
  }

  @Override
  protected boolean willMatch(ClosedUnitFractionHardAndSoftRanges<String> expected,
                              ClosedUnitFractionHardAndSoftRanges<String> actual) {
    return closedUnitFractionHardAndSoftRangesMatcher(expected).matches(actual);
  }

  public static <K> TypeSafeMatcher<ClosedUnitFractionHardAndSoftRanges<K>> closedUnitFractionHardAndSoftRangesMatcher(
      ClosedUnitFractionHardAndSoftRanges<K> expected) {
    return makeMatcher(expected,
        matchRBMap(v -> v.getRawMap(), f -> closedUnitFractionHardAndSoftRangeMatcher(f)));
  }

}
