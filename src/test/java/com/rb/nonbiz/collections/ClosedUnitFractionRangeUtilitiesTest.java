package com.rb.nonbiz.collections;

import com.google.common.collect.Range;
import com.rb.nonbiz.functional.QuadriConsumer;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.types.ClosedUnitFractionRange;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.ClosedUnitFractionRangeUtilities.loosenClosedUnitFractionRangeByFixedAmount;
import static com.rb.nonbiz.collections.ClosedUnitFractionRangeUtilities.optionalClosedUnitFractionRangeIntersection;
import static com.rb.nonbiz.collections.ClosedUnitFractionRangeUtilities.possiblyLoosenToContainPoint;
import static com.rb.nonbiz.collections.ClosedUnitFractionRangeUtilities.tightenClosedUnitFractionRangeAround;
import static com.rb.nonbiz.collections.ClosedUnitFractionRangeUtilities.tightenClosedUnitFractionRangeProportionally;
import static com.rb.nonbiz.collections.RBRanges.transformRange;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.ClosedUnitFractionHardToSoftRangeTighteningInstructions.closedUnitFractionHardToSoftRangeTighteningInstructions;
import static com.rb.nonbiz.types.ClosedUnitFractionHardToSoftRangeTighteningInstructions.setClosedUnitFractionSoftRangeToSameAsHard;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionAtMost;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionFixedTo;
import static com.rb.nonbiz.types.ClosedUnitFractionRangeTest.closedUnitFractionRangeMatcher;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInBps;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClosedUnitFractionRangeUtilitiesTest {

  @Test
  public void testTightenClosedUnitFractionRangeAround_invalidCenterOfRange_throws() {
    ClosedUnitFractionRange initialRange = closedUnitFractionRange(unitFraction(0.2), unitFraction(0.6));
    rbSetOf(UNIT_FRACTION_0, unitFraction(0.1), unitFraction(0.2), unitFraction(0.6), unitFraction(0.8), UNIT_FRACTION_1)
        .forEach(badCenterOfRange -> assertIllegalArgumentException( () ->
            tightenClosedUnitFractionRangeAround(initialRange, badCenterOfRange, unitFraction(0.9))));
    rbSetOf(unitFraction(0.21), unitFraction(0.5), unitFraction(0.59))
        .forEach(validCenterOfRange -> {
          ClosedUnitFractionRange doesNotThrow =
              tightenClosedUnitFractionRangeAround(initialRange, validCenterOfRange, unitFraction(0.9));
        });
  }

  @Test
  public void testTightenClosedUnitFractionRangeAround_invalidTighteningFraction_throws() {
    ClosedUnitFractionRange initialRange = closedUnitFractionRange(unitFraction(0.2), unitFraction(0.6));
    assertIllegalArgumentException( () -> tightenClosedUnitFractionRangeAround(initialRange, unitFraction(0.5), UNIT_FRACTION_1));
    ClosedUnitFractionRange doesNotThrow;
    doesNotThrow = tightenClosedUnitFractionRangeAround(initialRange, unitFraction(0.5), unitFraction(0.99));
    doesNotThrow = tightenClosedUnitFractionRangeAround(initialRange, unitFraction(0.5), unitFraction(0.01));
    assertIllegalArgumentException( () -> tightenClosedUnitFractionRangeAround(initialRange, unitFraction(0.5), UNIT_FRACTION_0));
  }

  @Test
  public void testTightenClosedUnitFractionRangeAround_generalCase() {
    ClosedUnitFractionRange initialRange = closedUnitFractionRange(unitFraction(0.2), unitFraction(0.6));
    assertThat(
        tightenClosedUnitFractionRangeAround(initialRange, unitFraction(0.5), unitFraction(0.1)),
        closedUnitFractionRangeMatcher(
            closedUnitFractionRange(
                unitFraction(doubleExplained(0.23, 0.5 - 0.9 * (0.5 - 0.2))),
                unitFraction(doubleExplained(0.59, 0.5 + 0.9 * (0.6 - 0.5))))));
  }

  @Test
  public void testLoosenClosedUnitFractionRangeBy() {
    // Using Range<Double> allows each test case to fit in a single line & therefore align vertically.
    BiConsumer<Range<Double>, Range<Double>> asserter =
        (initialRange, expectedResult) -> assertThat(
            loosenClosedUnitFractionRangeByFixedAmount(
                closedUnitFractionRange(transformRange(initialRange, v -> unitFraction(v))),
                unitFraction(0.1)), // always loosening by 0.1 in this test
            closedUnitFractionRangeMatcher(
                closedUnitFractionRange(transformRange(expectedResult, v -> unitFraction(v)))));

    // The 'everything' range never needs to be loosened, because it contains all unit fractions
    asserter.accept(Range.closed(0.0,  1.0), Range.closed(0.0, 1.0));

    asserter.accept(Range.closed(0.05, 1.0), Range.closed(0.0, 1.0));
    asserter.accept(Range.closed(0.1,  1.0), Range.closed(0.0, 1.0));
    asserter.accept(Range.closed(0.7,  1.0), Range.closed(0.6, 1.0));

    asserter.accept(Range.closed(0.05, 0.95), Range.closed(0.0, 1.0));
    asserter.accept(Range.closed(0.1,  0.95), Range.closed(0.0, 1.0));
    asserter.accept(Range.closed(0.7,  0.95), Range.closed(0.6, 1.0));

    asserter.accept(Range.closed(0.05, 0.8), Range.closed(0.0, 0.9));
    asserter.accept(Range.closed(0.1,  0.8), Range.closed(0.0, 0.9));
    asserter.accept(Range.closed(0.7,  0.8), Range.closed(0.6, 0.9));
  }

  @Test
  public void testLoosenClosedUnitFractionRangeByZero_leavesSame() {
    // Using Range<Double> allows each test case to fit in a single line & therefore align vertically.
    BiConsumer<Range<Double>, Range<Double>> asserter =
        (initialRange, expectedResult) -> assertThat(
            loosenClosedUnitFractionRangeByFixedAmount(
                closedUnitFractionRange(transformRange(initialRange, v -> unitFraction(v))),
                UNIT_FRACTION_0), // always loosening by 0 in this test
            closedUnitFractionRangeMatcher(
                closedUnitFractionRange(transformRange(expectedResult, v -> unitFraction(v)))));
    asserter.accept(Range.closed(0.0,  1.0), Range.closed(0.0, 1.0));
    asserter.accept(Range.closed(0.0,  0.8), Range.closed(0.0, 0.8));
    asserter.accept(Range.closed(0.2,  1.0), Range.closed(0.2, 1.0));
    asserter.accept(Range.closed(0.2,  0.8), Range.closed(0.2, 0.8));
  }

  @Test
  public void testPossiblyLoosenToContainPoint() {
    // Using Range<Double> allows each test case to fit in a single line & therefore align vertically.
    TriConsumer<Range<Double>, Double, Range<Double>> asserter =
        (initialRange, pointToContain, expectedResult) -> assertThat(
            possiblyLoosenToContainPoint(
                closedUnitFractionRange(transformRange(initialRange, v -> unitFraction(v))),
                unitFraction(pointToContain)),
            closedUnitFractionRangeMatcher(
                closedUnitFractionRange(transformRange(expectedResult, v -> unitFraction(v)))));

    // The 'everything' range never needs to be loosened, because it contains all unit fractions
    asserter.accept(Range.closed(0.0, 1.0), 0.0, Range.closed(0.0, 1.0));
    asserter.accept(Range.closed(0.0, 1.0), 0.5, Range.closed(0.0, 1.0));
    asserter.accept(Range.closed(0.0, 1.0), 1.0, Range.closed(0.0, 1.0));

    asserter.accept(Range.closed(0.2, 1.0), 0.0, Range.closed(0.0, 1.0));
    asserter.accept(Range.closed(0.2, 1.0), 0.1, Range.closed(0.1, 1.0));
    asserter.accept(Range.closed(0.2, 1.0), 0.2, Range.closed(0.2, 1.0));
    asserter.accept(Range.closed(0.2, 1.0), 0.5, Range.closed(0.2, 1.0));
    asserter.accept(Range.closed(0.2, 1.0), 1.0, Range.closed(0.2, 1.0));

    asserter.accept(Range.closed(0.0, 0.8), 0.0, Range.closed(0.0, 0.8));
    asserter.accept(Range.closed(0.0, 0.8), 0.5, Range.closed(0.0, 0.8));
    asserter.accept(Range.closed(0.0, 0.8), 0.8, Range.closed(0.0, 0.8));
    asserter.accept(Range.closed(0.0, 0.8), 0.9, Range.closed(0.0, 0.9));
    asserter.accept(Range.closed(0.0, 0.8), 1.0, Range.closed(0.0, 1.0));

    asserter.accept(Range.closed(0.2, 0.8), 0.0, Range.closed(0.0, 0.8));
    asserter.accept(Range.closed(0.2, 0.8), 0.1, Range.closed(0.1, 0.8));
    asserter.accept(Range.closed(0.2, 0.8), 0.2, Range.closed(0.2, 0.8));
    asserter.accept(Range.closed(0.2, 0.8), 0.5, Range.closed(0.2, 0.8));
    asserter.accept(Range.closed(0.2, 0.8), 0.8, Range.closed(0.2, 0.8));
    asserter.accept(Range.closed(0.2, 0.8), 0.9, Range.closed(0.2, 0.9));
    asserter.accept(Range.closed(0.2, 0.8), 1.0, Range.closed(0.2, 1.0));
  }

  @Test
  public void testTightenClosedUnitFractionRangeUpperAndLower() {
    // Using Range<Double> allows each test case to fit in a single line & therefore align vertically.
    QuadriConsumer<Range<Double>, Double, Double, Range<Double>> asserter =
        (initialRange, multiplierOnLower, multiplierOnUpper, expectedResult) -> assertThat(
            tightenClosedUnitFractionRangeProportionally(
                closedUnitFractionRange(transformRange(initialRange, v -> unitFraction(v))),
                unitFraction(0.5 * (initialRange.lowerEndpoint() + initialRange.upperEndpoint())),
                closedUnitFractionHardToSoftRangeTighteningInstructions(
                    unitFraction(multiplierOnLower),
                    unitFraction(multiplierOnUpper))),
            closedUnitFractionRangeMatcher(
                closedUnitFractionRange(transformRange(expectedResult, v -> unitFraction(v)))));
    // Unit multiplier...no change.
    asserter.accept(Range.closed(0.0, 1.0),   1.0, 1.0, Range.closed(0.0,  1.0));
    asserter.accept(Range.closed(0.25, 0.75), 1.0, 1.0, Range.closed(0.25, 0.75));

    // Upper only and lower only. Shrink around the midpoint of 0.5.
    asserter.accept(Range.closed(0.0, 1.0), 1.0, 0.6, Range.closed(0.0, 0.8));
    asserter.accept(Range.closed(0.0, 1.0), 0.6, 1.0, Range.closed(0.2, 1.0));

    // Multiplier for upper only, midpoint 0.55.
    asserter.accept(Range.closed(0.15, 0.95), 1.0, 0.75, Range.closed(0.15, doubleExplained(0.85, 0.55 + 0.75 * 0.4)));

    // Mid point 0.3, half width 0.1.  Shrink bottom by half and top to 10%.
    asserter.accept(Range.closed(0.2, 0.4), 0.5, 0.1,
        Range.closed(doubleExplained(0.25, 0.3 - 0.5 * 0.1), doubleExplained(0.31, 0.3 + 0.1 * 0.1)));

    // Mid point is 0.7, half width 0.1.
    asserter.accept(Range.closed(0.6, 0.8), 0.2, 0.5,
        Range.closed(doubleExplained(0.68, 0.7 - 0.2 * 0.1), doubleExplained(0.75, 0.7 + 0.5 * 0.1)));
  }

  @Test
  public void testTightenClosedUnitFractionRangeProportionally() {
    // Using Range<Double> allows each test case to fit in a single line & therefore align vertically.
    TriConsumer<Range<Double>, Double, Range<Double>> asserter =
        (initialRange, multiplierOnInitialRangeWidth, expectedResult) -> assertThat(
            tightenClosedUnitFractionRangeProportionally(
                closedUnitFractionRange(transformRange(initialRange, v -> unitFraction(v))),
                unitFraction(0.5 * (initialRange.lowerEndpoint() + initialRange.upperEndpoint())),
                closedUnitFractionHardToSoftRangeTighteningInstructions(
                    unitFraction(multiplierOnInitialRangeWidth),
                    unitFraction(multiplierOnInitialRangeWidth))),
            closedUnitFractionRangeMatcher(
                closedUnitFractionRange(transformRange(expectedResult, v -> unitFraction(v)))));
    asserter.accept(Range.closed(0.0, 1.0), 1.0, Range.closed(0.0,  1.0));
    asserter.accept(Range.closed(0.0, 1.0), 0.9, Range.closed(0.05, 0.95));
    // We can't do this, because closedUnitFractionHardToSoftRangeTighteningInstructions doesn't support a 0 multiplier.
    // asserter.accept(Range.closed(0.0, 1.0), 0.0, Range.closed(0.5,  0.5));

    asserter.accept(Range.closed(0.4, 1.0), 1.0, Range.closed(0.4,  1.0));
    asserter.accept(Range.closed(0.4, 1.0), 0.9, Range.closed(0.43, 0.97));
    // We can't do this, because closedUnitFractionHardToSoftRangeTighteningInstructions doesn't support a 0 multiplier.
    // asserter.accept(Range.closed(0.4, 1.0), 0.0, Range.closed(0.7,  0.7));

    asserter.accept(Range.closed(0.0, 0.6), 1.0, Range.closed(0.0,  0.6));
    asserter.accept(Range.closed(0.0, 0.6), 0.9, Range.closed(0.03, 0.57));
    // We can't do this, because closedUnitFractionHardToSoftRangeTighteningInstructions doesn't support a 0 multiplier.
    // asserter.accept(Range.closed(0.0, 0.6), 0.0, Range.closed(0.3,  0.3));

    asserter.accept(Range.closed(0.0, 0.0), 1.0, Range.closed(0.0,  0.0));
    asserter.accept(Range.closed(0.0, 0.0), 0.9, Range.closed(0.0,  0.0));
    // We can't do this, because closedUnitFractionHardToSoftRangeTighteningInstructions doesn't support a 0 multiplier.
    // asserter.accept(Range.closed(0.0, 0.0), 0.0, Range.closed(0.0,  0.0));

    asserter.accept(Range.closed(0.4, 0.4), 1.0, Range.closed(0.4,  0.4));
    asserter.accept(Range.closed(0.4, 0.4), 0.9, Range.closed(0.4,  0.4));
    // We can't do this, because closedUnitFractionHardToSoftRangeTighteningInstructions doesn't support a 0 multiplier.
    // asserter.accept(Range.closed(0.4, 0.4), 0.0, Range.closed(0.4,  0.4));

    asserter.accept(Range.closed(1.0, 1.0), 1.0, Range.closed(1.0,  1.0));
    asserter.accept(Range.closed(1.0, 1.0), 0.9, Range.closed(1.0,  1.0));
    // We can't do this, because closedUnitFractionHardToSoftRangeTighteningInstructions doesn't support a 0 multiplier.
    // asserter.accept(Range.closed(1.0, 1.0), 0.0, Range.closed(1.0,  1.0));
  }

  @Test
  public void softRangeSameAsHard_doesNotThrowDueToNumericalIssues() {
    // Unfortunately this test does not fail if we remove the fix about special-casing
    // ClosedUnitFractionHardToSoftRangeTighteningInstructions#setClosedUnitFractionSoftRangeToSameAsHard.
    // This looks identical to a situation that was throwing an exception in DirectIndexingJapanBacktest,
    // which is fixed by the prod code fix we're trying to test here.
    // But I might as well keep it in.
    ClosedUnitFractionRange range = unitFractionAtMost(unitFraction(new BigDecimal("0.00695779989577999128")));
    assertThat(
        tightenClosedUnitFractionRangeProportionally(
            range,
            unitFraction(0.003), // This middle argument was added later; just using a value between 0 and the max.
            setClosedUnitFractionSoftRangeToSameAsHard()),
        closedUnitFractionRangeMatcher(range));
  }

  @Test
  public void testOptionalIntersection() {
    assertOptionalEmpty(optionalClosedUnitFractionRangeIntersection(
        closedUnitFractionRange(unitFractionInBps(1), unitFractionInBps(3)),
        closedUnitFractionRange(unitFractionInBps(5), unitFractionInBps(7))));
    assertOptionalEmpty(optionalClosedUnitFractionRangeIntersection(
        closedUnitFractionRange(unitFractionInBps(5), unitFractionInBps(7)),
        closedUnitFractionRange(unitFractionInBps(1), unitFractionInBps(3))));
  }

  @Test
  public void testOptionalIntersection_intersectionIsNotSingleton_returnsNonEmptyIntersection() {
    assertOptionalNonEmpty(
        optionalClosedUnitFractionRangeIntersection(
            closedUnitFractionRange(unitFractionInBps(1.1), unitFractionInBps(3.3)),
            closedUnitFractionRange(unitFractionInBps(2.2), unitFractionInBps(4.4))),
        closedUnitFractionRangeMatcher(
            closedUnitFractionRange(unitFractionInBps(2.2), unitFractionInBps(3.3))));
    assertOptionalNonEmpty(
        optionalClosedUnitFractionRangeIntersection(
            closedUnitFractionRange(unitFractionInBps(2.2), unitFractionInBps(4.4)),
            closedUnitFractionRange(unitFractionInBps(1.1), unitFractionInBps(3.3))),
        closedUnitFractionRangeMatcher(
            closedUnitFractionRange(unitFractionInBps(2.2), unitFractionInBps(3.3))));
  }

  @Test
  public void testOptionalIntersection_intersectionResultIsSingleton_returnsNonEmptyIntersection() {
    assertOptionalNonEmpty(
        optionalClosedUnitFractionRangeIntersection(
            unitFractionFixedTo(unitFractionInBps(1.1)),
            closedUnitFractionRange(unitFractionInBps(1.1), unitFractionInBps(3.3))),
        closedUnitFractionRangeMatcher(
            unitFractionFixedTo(unitFractionInBps(1.1))));
    assertOptionalNonEmpty(
        optionalClosedUnitFractionRangeIntersection(
            closedUnitFractionRange(unitFractionInBps(1.1), unitFractionInBps(3.3)),
            unitFractionFixedTo(unitFractionInBps(1.1))),
        closedUnitFractionRangeMatcher(
            unitFractionFixedTo(unitFractionInBps(1.1))));

    assertOptionalNonEmpty(
        optionalClosedUnitFractionRangeIntersection(
            closedUnitFractionRange(unitFractionInBps(1.1), unitFractionInBps(3.3)),
            closedUnitFractionRange(unitFractionInBps(3.3), unitFractionInBps(5.5))),
        closedUnitFractionRangeMatcher(
            unitFractionFixedTo(unitFractionInBps(3.3))));
    assertOptionalNonEmpty(
        optionalClosedUnitFractionRangeIntersection(
            closedUnitFractionRange(unitFractionInBps(3.3), unitFractionInBps(5.5)),
            closedUnitFractionRange(unitFractionInBps(1.1), unitFractionInBps(3.3))),
        closedUnitFractionRangeMatcher(
            unitFractionFixedTo(unitFractionInBps(3.3))));
  }

  @Test
  public void testOptionalIntersection_intersectionOfTwoSingletons_returnsNonEmptyIntersection() {
    assertOptionalNonEmpty(
        optionalClosedUnitFractionRangeIntersection(
            unitFractionFixedTo(unitFractionInBps(1.1)),
            unitFractionFixedTo(unitFractionInBps(1.1))),
        closedUnitFractionRangeMatcher(
            unitFractionFixedTo(unitFractionInBps(1.1))));
  }

}
