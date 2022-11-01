package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import static com.rb.nonbiz.collections.ClosedUnitFractionRanges.closedUnitFractionRanges;
import static com.rb.nonbiz.collections.ClosedUnitFractionRangesTest.closedUnitFractionRangesMatcher;
import static com.rb.nonbiz.collections.ClosedUnitFractionRangesUtilities.closedUnitFractionRangesIntersectionOrThrow;
import static com.rb.nonbiz.collections.ClosedUnitFractionRangesUtilities.tightenClosedUnitFractionRangesAround;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unitFractionFixedTo;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.unrestrictedClosedUnitFractionRange;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClosedUnitFractionRangesUtilitiesTest {

  @Test
  public void testTightenClosedUnitFractionRangesAround_keysMustBeSame() {
    ClosedUnitFractionRanges<String> rangesAB = closedUnitFractionRanges(rbMapOf(
        "a", closedUnitFractionRange(unitFraction(0.2), unitFraction(0.6)),
        "b", unrestrictedClosedUnitFractionRange()));
    RBMap<String, UnitFraction> centersAB = rbMapOf(
        "a", unitFraction(0.5),
        "b", unitFraction(0.6));

    assertIllegalArgumentException( () -> tightenClosedUnitFractionRangesAround(
        closedUnitFractionRanges(singletonRBMap(
            "a", closedUnitFractionRange(unitFraction(0.2), unitFraction(0.6)))),
        centersAB,
        unitFraction(0.1)));
    assertIllegalArgumentException( () -> tightenClosedUnitFractionRangesAround(
        rangesAB,
        singletonRBMap(
            "a", unitFraction(0.5)),
        unitFraction(0.1)));
    assertIllegalArgumentException( () -> tightenClosedUnitFractionRangesAround(
        rangesAB,
        rbMapOf(
            "a", unitFraction(0.5),
            "c", unitFraction(0.6)),
        unitFraction(0.1)));

    ClosedUnitFractionRanges<String> doesNotThrow = tightenClosedUnitFractionRangesAround(
        rangesAB, centersAB, unitFraction(0.1));
  }

  @Test
  public void testTightenClosedUnitFractionRangesAround_generalCase() {
    assertThat(
        tightenClosedUnitFractionRangesAround(
            closedUnitFractionRanges(rbMapOf(
                "a", closedUnitFractionRange(unitFraction(0.2), unitFraction(0.6)),
                "b", unrestrictedClosedUnitFractionRange())),
            rbMapOf(
                "a", unitFraction(0.5),
                "b", unitFraction(0.5)),
            unitFraction(0.1)),
        closedUnitFractionRangesMatcher(
            closedUnitFractionRanges(rbMapOf(
                "a", closedUnitFractionRange(
                    unitFraction(doubleExplained(0.23, 0.2 + 0.1 * (0.5 - 0.2))),
                    unitFraction(doubleExplained(0.59, 0.6 - 0.1 * (0.6 - 0.5)))),
                "b", closedUnitFractionRange(
                    unitFraction(doubleExplained(0.05, 0 + 0.1 * (0.5 - 0.0))),
                    unitFraction(doubleExplained(0.95, 1 - 0.1 * (1.0 - 0.5))))))));
  }

  @Test
  public void testIntersectionOrThrow() {
    assertThat(
        closedUnitFractionRangesIntersectionOrThrow(
            closedUnitFractionRanges(rbMapOf(
                "a1", unitFractionFixedTo(unitFraction(0.11)),
                "a2", closedUnitFractionRange(unitFraction(0.27), unitFraction(0.57)))),
            closedUnitFractionRanges(rbMapOf(
                "a2", closedUnitFractionRange(unitFraction(0.28), unitFraction(0.58)),
                "a3", unitFractionFixedTo(unitFraction(0.33))))),
        closedUnitFractionRangesMatcher(
            closedUnitFractionRanges(rbMapOf(
                "a1", unitFractionFixedTo(unitFraction(0.11)),
                "a2", closedUnitFractionRange(
                    unitFraction(doubleExplained(0.28, Double.max(0.27, 0.28))),
                    unitFraction(doubleExplained(0.57, Double.min(0.57, 0.58)))),
                "a3", unitFractionFixedTo(unitFraction(0.33))))));
  }

}
