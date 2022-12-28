package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.collections.ClosedRangeTest.closedRangeMatcher;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange.closedUnitFractionHardAndPossiblySameSoftRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionAtLeast;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionAtMost;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionFixedTo;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionFixedToOne;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionFixedToZero;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unrestrictedClosedUnitFractionRange;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ClosedUnitFractionRangeTest extends RBTestMatcher<ClosedUnitFractionRange> {

  @Test
  public void valuesReversed_throws() {
    assertIllegalArgumentException( () -> closedUnitFractionRange(unitFraction(0.33), unitFraction(0.22)));
  }

  @Test
  public void testIsFixedToZero() {
    assertTrue(unitFractionFixedToZero().isFixedToZero());
    assertFalse(unitFractionFixedTo(unitFraction(1e-9)).isFixedToZero());
    assertFalse(unitFractionAtLeast(unitFraction(0.1)).isFixedToZero());
    assertFalse(unitFractionAtMost(unitFraction(0.1)).isFixedToZero());
    assertFalse(closedUnitFractionRange(unitFraction(0.1), unitFraction(0.3)).isFixedToZero());
  }

  @Test
  public void testUnitFractionFixedToZero() {
    assertEquals(UNIT_FRACTION_0, unitFractionFixedToZero().lowerEndpoint());
    assertEquals(UNIT_FRACTION_0, unitFractionFixedToZero().upperEndpoint());

    double e = 1e-9; // epsilon
    assertFalse(unitFractionFixedToZero().contains(unitFraction(e)));
    assertTrue( unitFractionFixedToZero().contains(UNIT_FRACTION_0));
  }

  @Test
  public void testUnitFractionFixedToOne() {
    assertEquals(UNIT_FRACTION_1, unitFractionFixedToOne().lowerEndpoint());
    assertEquals(UNIT_FRACTION_1, unitFractionFixedToOne().upperEndpoint());

    double e = 1e-9; // epsilon
    assertFalse(unitFractionFixedToOne().contains(unitFraction(1 - e)));
    assertTrue( unitFractionFixedToOne().contains(UNIT_FRACTION_1));
  }

  @Test
  public void testConstructFromRawRange() {
    ClosedUnitFractionRange closedUnitFractionRange = closedUnitFractionRange(Range.closed(unitFraction(0.123), unitFraction(0.789)));

    assertEquals(unitFraction(0.123), closedUnitFractionRange.lowerEndpoint());
    assertEquals(unitFraction(0.789), closedUnitFractionRange.upperEndpoint());
  }

  @Test
  public void testStrictlyLooser() {
    ClosedUnitFractionRange range = closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6));

    assertFalse(range.isStrictlyLooser(closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6))));
    assertFalse(range.isStrictlyLooser(closedUnitFractionRange(unitFraction(0.39), unitFraction(0.61)))); // soft range is wider
    assertFalse(range.isStrictlyLooser(closedUnitFractionRange(unitFraction(0.4), unitFraction(0.59)))); // same on left
    assertFalse(range.isStrictlyLooser(closedUnitFractionRange(unitFraction(0.41), unitFraction(0.6)))); // same on right

    assertTrue(range.isStrictlyLooser(closedUnitFractionRange(unitFraction(0.41), unitFraction(0.59)))); // soft range is narrower
  }

  @Test
  public void testStrictlyLooser_noUpperBound() {
    ClosedUnitFractionRange atLeast40 = unitFractionAtLeast(unitFraction(0.4));

    assertFalse(atLeast40.isStrictlyLooser(unrestrictedClosedUnitFractionRange()));
    assertFalse(atLeast40.isStrictlyLooser(unitFractionAtLeast(unitFraction(0.39))));
    assertFalse(atLeast40.isStrictlyLooser(unitFractionAtLeast(unitFraction(0.4))));
    assertTrue(atLeast40.isStrictlyLooser(unitFractionAtLeast(unitFraction(0.41))));
  }

  @Test
  public void testStrictlyLooser_noLowerBound() {
    ClosedUnitFractionRange atMost60 = unitFractionAtMost(unitFraction(0.6));

    assertTrue(atMost60.isStrictlyLooser(unitFractionAtMost(unitFraction(0.59))));
    assertFalse(atMost60.isStrictlyLooser(unitFractionAtMost(unitFraction(0.6))));
    assertFalse(atMost60.isStrictlyLooser(unitFractionAtMost(unitFraction(0.61))));
    assertFalse(atMost60.isStrictlyLooser(unrestrictedClosedUnitFractionRange()));
  }

  @Test
  public void testStrictlyLooser_bothUnrestricted_returnsTrue() {
    ClosedUnitFractionRange unrestricted = unrestrictedClosedUnitFractionRange();
    ClosedUnitFractionRange atLeast40 = unitFractionAtLeast(unitFraction(0.4));
    ClosedUnitFractionRange atMost60 = unitFractionAtMost(unitFraction(0.6));

    assertTrue(unrestricted.isStrictlyLooser(unrestricted));

    assertFalse(atLeast40.isStrictlyLooser(unrestricted));
    assertFalse(atMost60.isStrictlyLooser(unrestricted));

    assertTrue(unrestricted.isStrictlyLooser(atLeast40));
    assertTrue(unrestricted.isStrictlyLooser(atMost60));
  }

  @Test
  public void testClosedUnitFractionHardAndPossiblySameSoftRange() {
    BiFunction<ClosedUnitFractionRange, ClosedUnitFractionRange, ClosedUnitFractionHardAndSoftRange> maker =
        (hardRange, softRange) -> closedUnitFractionHardAndPossiblySameSoftRange(hardRange, softRange);

    ClosedUnitFractionRange loose = closedUnitFractionRange(unitFraction(0.1), unitFraction(0.9));
    ClosedUnitFractionRange tight = closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6));

    ClosedUnitFractionRange tightMatchingLowerLoose = closedUnitFractionRange(unitFraction(0.1), unitFraction(0.6));
    ClosedUnitFractionRange tightMatchingUpperLoose = closedUnitFractionRange(unitFraction(0.4), unitFraction(0.9));

    // the hard limit cannot be tighter than the soft limit
    assertIllegalArgumentException( () -> maker.apply(tight, loose));

    ClosedUnitFractionHardAndSoftRange doesNotThrow;
    doesNotThrow = maker.apply(loose, tight);

    // but the hard and soft ranges can be equal
    doesNotThrow = maker.apply(loose, loose);
    doesNotThrow = maker.apply(tight, tight);

    // the ranges can match at one end and be strictly looser at the other
    doesNotThrow = maker.apply(loose, tightMatchingLowerLoose);
    doesNotThrow = maker.apply(loose, tightMatchingUpperLoose);
  }

  @Test
  public void testClosedUnitFractionHardAndPossiblySameSoftRange_unrestricted() {
    BiFunction<ClosedUnitFractionRange, ClosedUnitFractionRange, ClosedUnitFractionHardAndSoftRange> maker =
        (hardRange, softRange) -> closedUnitFractionHardAndPossiblySameSoftRange(hardRange, softRange);

    ClosedUnitFractionRange unrestricted = unrestrictedClosedUnitFractionRange();
    ClosedUnitFractionRange atLeast39    = unitFractionAtLeast(unitFraction(0.39));
    ClosedUnitFractionRange atLeast40    = unitFractionAtLeast(unitFraction(0.40));
    ClosedUnitFractionRange atLeast41    = unitFractionAtLeast(unitFraction(0.41));

    ClosedUnitFractionRange atMost59     = unitFractionAtMost( unitFraction(0.59));
    ClosedUnitFractionRange atMost60     = unitFractionAtMost( unitFraction(0.60));
    ClosedUnitFractionRange atMost61     = unitFractionAtMost( unitFraction(0.61));

    ClosedUnitFractionRange between40And60 = closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6));

    ClosedUnitFractionHardAndSoftRange doesNotThrow;
    doesNotThrow = maker.apply(unrestricted, unrestricted);
    doesNotThrow = maker.apply(unrestricted, atLeast40);
    doesNotThrow = maker.apply(unrestricted, atMost60);

    doesNotThrow = maker.apply(atLeast39, atLeast40);
    doesNotThrow = maker.apply(atLeast40, atLeast40);
    doesNotThrow = maker.apply(atLeast40, atLeast41);

    doesNotThrow = maker.apply(atMost60, atMost59);
    doesNotThrow = maker.apply(atMost60, atMost60);
    doesNotThrow = maker.apply(atMost61, atMost60);

    doesNotThrow = maker.apply(between40And60, between40And60);
    doesNotThrow = maker.apply(atLeast39, between40And60);
    doesNotThrow = maker.apply(atLeast40, between40And60);
    doesNotThrow = maker.apply(atMost60,  between40And60);
    doesNotThrow = maker.apply(atMost61,  between40And60);

    assertIllegalArgumentException( () -> maker.apply(atLeast40, unrestricted));
    assertIllegalArgumentException( () -> maker.apply(atMost60,  unrestricted));

    assertIllegalArgumentException( () -> maker.apply(atLeast41, atLeast40));
    assertIllegalArgumentException( () -> maker.apply(atMost59,  atMost60));

    assertIllegalArgumentException( () -> maker.apply(between40And60, unrestricted));
    assertIllegalArgumentException( () -> maker.apply(between40And60, atLeast40));
    assertIllegalArgumentException( () -> maker.apply(between40And60, atMost60));
  }

  @Test
  public void testContains_unitFraction() {
    ClosedUnitFractionRange range = closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6));
    rbSetOf(
        UNIT_FRACTION_0,
        unitFraction(0.3),
        unitFraction(0.7),
        UNIT_FRACTION_1)
        .forEach(notContainedSafely -> assertFalse(range.contains(notContainedSafely)));
    rbSetOf(
        unitFraction(0.4),
        unitFraction(0.4 + 1e-9),
        unitFraction(0.5),
        unitFraction(0.6 - 1e-9),
        unitFraction(0.6))
        .forEach(containedSafely -> assertTrue(range.contains(containedSafely)));
  }

  @Test
  public void testContains_signedFraction() {
    ClosedUnitFractionRange range = closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6));
    rbSetOf(
        signedFraction(-12.34),
        signedFraction(-1e-9),
        SIGNED_FRACTION_0,
        signedFraction(0.3),
        signedFraction(0.7),
        SIGNED_FRACTION_1,
        signedFraction(1 + 1e-9),
        signedFraction(12.34))
        .forEach(notContainedSafely -> assertFalse(range.contains(notContainedSafely)));
    rbSetOf(
        signedFraction(0.4),
        signedFraction(0.4 + 1e-9),
        signedFraction(0.5),
        signedFraction(0.6 - 1e-9),
        signedFraction(0.6))
        .forEach(containedSafely -> assertTrue(range.contains(containedSafely)));
  }

  @Test
  public void testContainsInteriorPoint() {
    ClosedUnitFractionRange range = closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6));
    rbSetOf(
        UNIT_FRACTION_0,
        unitFraction(0.3),
        unitFraction(0.4),
        unitFraction(0.6),
        unitFraction(0.7),
        UNIT_FRACTION_1)
        .forEach(notContainedSafely -> assertFalse(range.containsInteriorPoint(notContainedSafely)));
    rbSetOf(
        unitFraction(0.4 + 1e-9),
        unitFraction(0.5),
        unitFraction(0.6 - 1e-9))
        .forEach(containedSafely -> assertTrue(range.containsInteriorPoint(containedSafely)));
  }

  @Test
  public void testAsDoubleRange() {
    assertEquals(
        Range.closed(0.25, 0.75),
        closedUnitFractionRange(unitFraction(0.25), unitFraction(0.75)).asDoubleRange());
    assertEquals(
        Range.closed(0.0, 0.0),
        unitFractionFixedToZero().asDoubleRange());
    assertEquals(
        Range.closed(0.123, 0.123),
        unitFractionFixedTo(unitFraction(0.123)).asDoubleRange());
    assertEquals(
        Range.closed(1.0, 1.0),
        unitFractionFixedToOne().asDoubleRange());
  }

  @Test
  public void testGetNearestValueInRange() {
    Function<UnitFraction, UnitFraction> closestValueFinder = value ->
        closedUnitFractionRange(unitFraction(0.25), unitFraction(0.75)).getNearestValueInRange(value);

    double e = 1e-9; // epsilon
    assertEquals(unitFraction(0.25), closestValueFinder.apply(UNIT_FRACTION_0));
    assertEquals(unitFraction(0.25), closestValueFinder.apply(unitFraction(0.25 - e)));
    assertEquals(unitFraction(0.25), closestValueFinder.apply(unitFraction(0.25)));
    assertEquals(unitFraction(0.50), closestValueFinder.apply(unitFraction(0.5)));
    assertEquals(unitFraction(0.75), closestValueFinder.apply(unitFraction(0.75)));
    assertEquals(unitFraction(0.75), closestValueFinder.apply(unitFraction(0.75 + e)));
    assertEquals(unitFraction(0.75), closestValueFinder.apply(UNIT_FRACTION_1));
  }

  @Test
  public void testIsUnrestricted() {
    assertFalse(unitFractionFixedToZero().isUnrestricted());
    assertFalse(unitFractionFixedTo(unitFraction(1e-9)).isUnrestricted());
    assertFalse(unitFractionAtLeast(unitFraction(0.1)).isUnrestricted());
    assertFalse(unitFractionAtMost(unitFraction(0.1)).isUnrestricted());
    assertFalse(closedUnitFractionRange(unitFraction(0.1), unitFraction(0.3)).isUnrestricted());

    assertTrue(closedUnitFractionRange(UNIT_FRACTION_0, UNIT_FRACTION_1).isUnrestricted());
    assertTrue(unrestrictedClosedUnitFractionRange().isUnrestricted());
  }

  @Test
  public void testPossiblyTightenLowerBound() {
    ClosedUnitFractionRange initialRange = closedUnitFractionRange(unitFraction(0.5), unitFraction(0.7));
    BiConsumer<Double, ClosedUnitFractionRange> asserter = (newLowerBound, expectedResult) ->
        assertThat(
            initialRange
                .withPossiblyTightenedLowerBound(unitFraction(newLowerBound)),
            closedUnitFractionRangeMatcher(
                expectedResult));
    double e = 1e-9; // epsilon
    asserter.accept(0.0,     initialRange);
    asserter.accept(0.5 - e, initialRange);
    asserter.accept(0.5,     initialRange);
    asserter.accept(0.5 + e, closedUnitFractionRange(unitFraction(0.5 + e), unitFraction(0.7)));
    asserter.accept(0.6,     closedUnitFractionRange(unitFraction(0.6), unitFraction(0.7)));
    asserter.accept(0.7 - e, closedUnitFractionRange(unitFraction(0.7 - e), unitFraction(0.7)));
    asserter.accept(0.7,     unitFractionFixedTo(unitFraction(0.7)));
    assertIllegalArgumentException( () -> initialRange.withPossiblyTightenedLowerBound(unitFraction(0.7 +  e)));
    assertIllegalArgumentException( () -> initialRange.withPossiblyTightenedLowerBound(unitFraction(0.8)));
    assertIllegalArgumentException( () -> initialRange.withPossiblyTightenedLowerBound(unitFraction(1 - e)));
    assertIllegalArgumentException( () -> initialRange.withPossiblyTightenedLowerBound(UNIT_FRACTION_1));
  }

  @Test
  public void testPossiblyTightenUpperBound() {
    ClosedUnitFractionRange initialRange = closedUnitFractionRange(unitFraction(0.5), unitFraction(0.7));
    BiConsumer<Double, ClosedUnitFractionRange> asserter = (newUpperBound, expectedResult) ->
        assertThat(
            initialRange
                .withPossiblyTightenedUpperBound(unitFraction(newUpperBound)),
            closedUnitFractionRangeMatcher(
                expectedResult));
    double e = 1e-9; // epsilon
    asserter.accept(1.0,     initialRange);
    asserter.accept(0.7 + e, initialRange);
    asserter.accept(0.7,     initialRange);
    asserter.accept(0.7 - e, closedUnitFractionRange(unitFraction(0.5), unitFraction(0.7 - e)));
    asserter.accept(0.6,     closedUnitFractionRange(unitFraction(0.5), unitFraction(0.6)));
    asserter.accept(0.5 + e, closedUnitFractionRange(unitFraction(0.5), unitFraction(0.5 + e)));
    asserter.accept(0.5,     unitFractionFixedTo(unitFraction(0.5)));
    assertIllegalArgumentException( () -> initialRange.withPossiblyTightenedUpperBound(unitFraction(0.5 - e)));
    assertIllegalArgumentException( () -> initialRange.withPossiblyTightenedUpperBound(unitFraction(0.4)));
    assertIllegalArgumentException( () -> initialRange.withPossiblyTightenedUpperBound(unitFraction(e)));
    assertIllegalArgumentException( () -> initialRange.withPossiblyTightenedUpperBound(UNIT_FRACTION_0));
  }

  @Test
  public void testToPercentString() {
    ClosedUnitFractionRange closedUnitFractionRange = closedUnitFractionRange(
        unitFraction(0.123456789), unitFraction(0.987654321));

    assertEquals("[CUFR [12 %..99 %] CUFR]",                 closedUnitFractionRange.toString(0));
    assertEquals("[CUFR [12.3 %..98.8 %] CUFR]",             closedUnitFractionRange.toString(1));
    assertEquals("[CUFR [12.346 %..98.765 %] CUFR]",         closedUnitFractionRange.toString(3));
    assertEquals("[CUFR [12.34568 %..98.76543 %] CUFR]",     closedUnitFractionRange.toString(5));
    assertEquals("[CUFR [12.3456789 %..98.7654321 %] CUFR]", closedUnitFractionRange.toString(7));
    // asking for additional precision doesn't change the output; no more digits to print
    assertEquals("[CUFR [12.3456789 %..98.7654321 %] CUFR]", closedUnitFractionRange.toString(19));

    // negative precision not supported
    assertIllegalArgumentException( () -> closedUnitFractionRange.toString(-1));
  }

  @Override
  public ClosedUnitFractionRange makeTrivialObject() {
    return closedUnitFractionRange(UNIT_FRACTION_0, UNIT_FRACTION_0);
  }

  @Override
  public ClosedUnitFractionRange makeNontrivialObject() {
    return closedUnitFractionRange(unitFraction(0.11), unitFraction(0.22));
  }

  @Override
  public ClosedUnitFractionRange makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return closedUnitFractionRange(unitFraction(0.11 + e), unitFraction(0.22 + e));
  }

  @Override
  protected boolean willMatch(ClosedUnitFractionRange expected, ClosedUnitFractionRange actual) {
    return closedUnitFractionRangeMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ClosedUnitFractionRange> closedUnitFractionRangeMatcher(ClosedUnitFractionRange expected) {
    return makeMatcher(expected,
        match(v -> v.asClosedRangeOfUnitFraction(), f -> closedRangeMatcher(f, f2 -> preciseValueMatcher(f2, DEFAULT_EPSILON_1e_8))));
  }

}
