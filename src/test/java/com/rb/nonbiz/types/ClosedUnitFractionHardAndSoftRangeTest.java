package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange.closedUnitFractionHardAndPossiblySameSoftRange;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange.closedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unrestrictedClosedUnitFractionRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRangeTest.closedUnitFractionRangeMatcher;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class ClosedUnitFractionHardAndSoftRangeTest extends RBTestMatcher<ClosedUnitFractionHardAndSoftRange> {

  public static ClosedUnitFractionHardAndSoftRange testClosedUnitFractionHardAndSoftRangeWithSeed(double seed) {
    return closedUnitFractionHardAndSoftRange(
        closedUnitFractionRange(unitFraction(0.1 + seed), unitFraction(0.8 + seed)),
        closedUnitFractionRange(unitFraction(0.4 + seed), unitFraction(0.6 + seed)));
  }

  public static ClosedUnitFractionHardAndSoftRange unrestrictedClosedUnitFractionHardAndSoftRange() {
    return closedUnitFractionHardAndSoftRange(
        unrestrictedClosedUnitFractionRange(),
        unrestrictedClosedUnitFractionRange());
  }

  public static ClosedUnitFractionHardAndSoftRange closedUnitFractionHardAndDummySoftRange(
      UnitFraction hardLowerEndpoint, UnitFraction hardUpperEndpoint) {
    double width = hardUpperEndpoint.doubleValue - hardLowerEndpoint.doubleValue;
    return closedUnitFractionHardAndSoftRange(
        closedUnitFractionRange(hardLowerEndpoint, hardUpperEndpoint),
        // We compute the soft range this way, to make it different than the hard one, but also tight
        // in some arbitrary manner, hence the 'dummy' in the method name.
        closedUnitFractionRange(
            hardLowerEndpoint.add(unitFraction(width * 0.123)),
            hardUpperEndpoint.subtract(unitFraction(width * 0.456))));
  }

  @Test
  public void softRangeMustBeNarrowerThanHardRange() {
    Function<ClosedUnitFractionRange, ClosedUnitFractionHardAndSoftRange> maker = softRange ->
        closedUnitFractionHardAndSoftRange(
            closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6)),
            softRange);
    assertIllegalArgumentException( () -> maker.apply(closedUnitFractionRange(unitFraction(0.4),  unitFraction(0.6))));  // same range
    assertIllegalArgumentException( () -> maker.apply(closedUnitFractionRange(unitFraction(0.39), unitFraction(0.61)))); // soft range is wider
    assertIllegalArgumentException( () -> maker.apply(closedUnitFractionRange(unitFraction(0.4),  unitFraction(0.59)))); // same on left
    assertIllegalArgumentException( () -> maker.apply(closedUnitFractionRange(unitFraction(0.41), unitFraction(0.60)))); // same on right
    ClosedUnitFractionHardAndSoftRange doesNotThrow =
        maker.apply(closedUnitFractionRange(unitFraction(0.41), unitFraction(0.59))); // soft range is narrower
  }

  @Test
  public void testIsUnrestricted() {
    assertTrue(unrestrictedClosedUnitFractionHardAndSoftRange().isUnrestricted());
    assertFalse(closedUnitFractionHardAndSoftRange(
        closedUnitFractionRange(unitFraction(0.1), unitFraction(0.8)),
        closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6))).isUnrestricted());
  }

  @Test
  public void testClosedUnitFractionHardAndPossiblySameSoftRange() {
    Function<ClosedUnitFractionRange, ClosedUnitFractionHardAndSoftRange> maker = softRange ->
        closedUnitFractionHardAndPossiblySameSoftRange(
            closedUnitFractionRange(unitFraction(0.40), unitFraction(0.60)),
            softRange);

    ClosedUnitFractionHardAndSoftRange doesNotThrow;
    doesNotThrow = maker.apply(closedUnitFractionRange(unitFraction(0.41), unitFraction(0.59)));
    // hard and soft ranges matching on one boundary is OK
    doesNotThrow = maker.apply(closedUnitFractionRange(unitFraction(0.40), unitFraction(0.59)));
    doesNotThrow = maker.apply(closedUnitFractionRange(unitFraction(0.41), unitFraction(0.60)));
    // equal hard and soft ranges is OK
    doesNotThrow = maker.apply(closedUnitFractionRange(unitFraction(0.40), unitFraction(0.60)));

    assertIllegalArgumentException( () -> maker.apply(closedUnitFractionRange(unitFraction(0.39), unitFraction(0.60))));
    assertIllegalArgumentException( () -> maker.apply(closedUnitFractionRange(unitFraction(0.40), unitFraction(0.61))));
  }

  @Test
  public void testTightenToSoftOrCurrent() {
    ClosedUnitFractionRange hardRange = closedUnitFractionRange(unitFraction(0.10), unitFraction(0.30));
    ClosedUnitFractionRange softRange = closedUnitFractionRange(unitFraction(0.14), unitFraction(0.26));
    BiConsumer<Double, ClosedUnitFractionRange> asserter = (pointToInclude, expectedResult) ->
        assertThat(
            closedUnitFractionHardAndSoftRange(hardRange, softRange)
                .tightenToSoftOrCurrent(unitFraction(pointToInclude)),
            closedUnitFractionRangeMatcher(
                expectedResult));
    double e = 1e-9;
    // When the point is outside the hard range, we must return the soft range.
    // Trading-wise, this means 'if too misallocated, force trading to be within the soft range (the tighter of the two)'
    // The idea is that you don't want to sell and be exactly at the limit; if prices move tomorrow,
    // you could end up over the limit again, and have to sell a small amount again, etc.
    // This is like a thermostat; if you want the temperature to be at 70, then turn on the heat when it's 69
    // and let the house heat up to 71, otherwise the heat would be going on and off all the time.
    asserter.accept(1.0,      softRange);
    asserter.accept(0.31,     softRange);
    asserter.accept(0.3 + e,  softRange);

    // When the point is inside the hard range, but outside the soft range, just loosen the soft range to include it.
    asserter.accept(0.3,      closedUnitFractionRange(unitFraction(0.14), unitFraction(0.3)));
    asserter.accept(0.3 - e,  closedUnitFractionRange(unitFraction(0.14), unitFraction(0.3 - e)));
    asserter.accept(0.28,     closedUnitFractionRange(unitFraction(0.14), unitFraction(0.28)));
    asserter.accept(0.26 + e, closedUnitFractionRange(unitFraction(0.14), unitFraction(0.26 + e)));

    // When the point is inside the soft range, return the soft range.
    asserter.accept(0.26,     softRange);
    asserter.accept(0.26 - e, softRange);
    asserter.accept(0.25,     softRange);
    asserter.accept(0.15,     softRange);
    asserter.accept(0.14 + e, softRange);
    asserter.accept(0.14,     softRange);

    // When the point is inside the hard range, but outside the soft range, just loosen the soft range to include it.
    asserter.accept(0.14 - e, closedUnitFractionRange(unitFraction(0.14 - e), unitFraction(0.26)));
    asserter.accept(0.12,     closedUnitFractionRange(unitFraction(0.12),     unitFraction(0.26)));
    asserter.accept(0.10 + e, closedUnitFractionRange(unitFraction(0.10 + e), unitFraction(0.26)));
    asserter.accept(0.10,     closedUnitFractionRange(unitFraction(0.10),     unitFraction(0.26)));

    // When the point is outside the hard range, we must return the soft range.
    // Trading-wise, this means 'if too misallocated, force trading to be within the soft range (the tighter of the two)'.
    asserter.accept(0.10 - e, softRange);
    asserter.accept(0.05,     softRange);
    asserter.accept(e,        softRange);
    asserter.accept(0.0,      softRange);
  }

  @Override
  public ClosedUnitFractionHardAndSoftRange makeTrivialObject() {
    return unrestrictedClosedUnitFractionHardAndSoftRange();
  }

  @Override
  public ClosedUnitFractionHardAndSoftRange makeNontrivialObject() {
    return testClosedUnitFractionHardAndSoftRangeWithSeed(ZERO_SEED);
  }

  @Override
  public ClosedUnitFractionHardAndSoftRange makeMatchingNontrivialObject() {
    return testClosedUnitFractionHardAndSoftRangeWithSeed(EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(ClosedUnitFractionHardAndSoftRange expected, ClosedUnitFractionHardAndSoftRange actual) {
    return closedUnitFractionHardAndSoftRangeMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ClosedUnitFractionHardAndSoftRange> closedUnitFractionHardAndSoftRangeMatcher(
      ClosedUnitFractionHardAndSoftRange expected) {
    return makeMatcher(expected,
        match(v -> v.getHardRange(), f -> closedUnitFractionRangeMatcher(f)),
        match(v -> v.getSoftRange(), f -> closedUnitFractionRangeMatcher(f)));
  }

}
