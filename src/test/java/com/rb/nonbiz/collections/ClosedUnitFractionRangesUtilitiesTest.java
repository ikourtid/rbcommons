package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import static com.rb.nonbiz.collections.ClosedUnitFractionRanges.closedUnitFractionRanges;
import static com.rb.nonbiz.collections.ClosedUnitFractionRanges.emptyClosedUnitFractionRanges;
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
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
  public void testClosedUnitFractionRangesIntersectionOrThrow_twoAtATime() {
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

    // Test an empty intersection...we should throw because both maps contain a1, but the ranges have zero overlap
    assertIllegalArgumentException( () -> closedUnitFractionRangesIntersectionOrThrow(
        closedUnitFractionRanges(rbMapOf(
            "a1", closedUnitFractionRange(unitFraction(0.17), unitFraction(0.19)),
            "a2", closedUnitFractionRange(unitFraction(0.27), unitFraction(0.57)))),
        closedUnitFractionRanges(rbMapOf(
            "a1", closedUnitFractionRange(unitFraction(0.1), unitFraction(0.11)),
            "a3", closedUnitFractionRange(unitFraction(0.27), unitFraction(0.57))))));
  }

  @Test
  public void testClosedUnitFractionRangesIntersectionOrThrow_listOverload_twoOrFewerItems() {
    // This is just like the previous test, except that it uses the overload that takes a list.
    assertThat(
        closedUnitFractionRangesIntersectionOrThrow(
            ImmutableList.of(
                closedUnitFractionRanges(rbMapOf(
                    "a1", unitFractionFixedTo(unitFraction(0.11)),
                    "a2", closedUnitFractionRange(unitFraction(0.27), unitFraction(0.57)))),
                closedUnitFractionRanges(rbMapOf(
                    "a2", closedUnitFractionRange(unitFraction(0.28), unitFraction(0.58)),
                    "a3", unitFractionFixedTo(unitFraction(0.33)))))),
        closedUnitFractionRangesMatcher(
            closedUnitFractionRanges(rbMapOf(
                "a1", unitFractionFixedTo(unitFraction(0.11)),
                "a2", closedUnitFractionRange(
                    unitFraction(doubleExplained(0.28, Double.max(0.27, 0.28))),
                    unitFraction(doubleExplained(0.57, Double.min(0.57, 0.58)))),
                "a3", unitFractionFixedTo(unitFraction(0.33))))));

    // 1 item only; returns same
    assertThat(
        closedUnitFractionRangesIntersectionOrThrow(
            singletonList(
                closedUnitFractionRanges(rbMapOf(
                    "a1", unitFractionFixedTo(unitFraction(0.11)),
                    "a2", closedUnitFractionRange(unitFraction(0.27), unitFraction(0.57)))))),
        closedUnitFractionRangesMatcher(
            closedUnitFractionRanges(rbMapOf(
                "a1", unitFractionFixedTo(unitFraction(0.11)),
                "a2", closedUnitFractionRange(unitFraction(0.27), unitFraction(0.57))))));

    // 0 items; returns empty
    assertThat(
        closedUnitFractionRangesIntersectionOrThrow(emptyList()),
        closedUnitFractionRangesMatcher(
            emptyClosedUnitFractionRanges()));

    // Finally, using 3 items
    assertThat(
        closedUnitFractionRangesIntersectionOrThrow(
            ImmutableList.of(
                closedUnitFractionRanges(singletonRBMap(
                    "a1", closedUnitFractionRange(unitFraction(0.51), unitFraction(0.61)))),
                closedUnitFractionRanges(singletonRBMap(
                    "a1", closedUnitFractionRange(unitFraction(0.52), unitFraction(0.62)))),
                closedUnitFractionRanges(rbMapOf(
                    "a1", closedUnitFractionRange(unitFraction(0.53), unitFraction(0.63)),
                    "a2", closedUnitFractionRange(unitFraction(0.54), unitFraction(0.64)),
                    "a3", closedUnitFractionRange(unitFraction(0.55), unitFraction(0.65)))))),
        closedUnitFractionRangesMatcher(
            closedUnitFractionRanges(rbMapOf(
                "a1", closedUnitFractionRange(
                    unitFraction(0.53),  // max of 0.51, 0.52, 0.53, from all 3 mentions of a1
                    unitFraction(0.61)), // min of 0.61, 0.62, 0.63, from all 3 mentions of a1
                "a2", closedUnitFractionRange(unitFraction(0.54), unitFraction(0.64)),
                "a3", closedUnitFractionRange(unitFraction(0.55), unitFraction(0.65))))));
  }

}
