package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.ClosedUnitFractionRange;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRangeTest.closedUnitFractionRangeMatcher;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClosedUnitFractionRangeShrinkerTest extends RBTest<ClosedUnitFractionRangeShrinker> {

  private final UnitFraction NON_ZERO_MULTIPLIER = unitFraction(0.1);

  @Test
  public void multiplierIsAlmostZero_throws() {
    rbSetOf(
        closedUnitFractionRange(unitFraction(0.3), unitFraction(0.7)),
        closedUnitFractionRange(UNIT_FRACTION_0, unitFraction(0.7)),
        closedUnitFractionRange(unitFraction(0.3), UNIT_FRACTION_1),
        closedUnitFractionRange(UNIT_FRACTION_0, UNIT_FRACTION_1))
        .forEach(originalRange ->
            rbSetOf(UNIT_FRACTION_0, unitFraction(1e-9))
                .forEach(fractionOfOriginal ->
                    assertIllegalArgumentException( () ->
                        makeTestObject().shrink(originalRange, fractionOfOriginal))));
  }

  @Test
  public void shrinksBothSides() {
    assertShrinksToOneTenth(
        closedUnitFractionRange(unitFraction(0.3), unitFraction(0.7)),
        closedUnitFractionRange(unitFraction(0.03), unitFraction(0.07)));
    assertShrinksToOneTenth(
        closedUnitFractionRange(UNIT_FRACTION_0, unitFraction(0.7)),
        closedUnitFractionRange(UNIT_FRACTION_0, unitFraction(0.07)));
    assertShrinksToOneTenth(
        closedUnitFractionRange(unitFraction(0.3), UNIT_FRACTION_1),
        closedUnitFractionRange(unitFraction(0.03), unitFraction(0.1)));
    assertShrinksToOneTenth(
        closedUnitFractionRange(UNIT_FRACTION_0, UNIT_FRACTION_1),
        closedUnitFractionRange(UNIT_FRACTION_0, unitFraction(0.1)));
  }

  private void assertShrinksToOneTenth(ClosedUnitFractionRange original, ClosedUnitFractionRange shrunk) {
    assertThat(
        makeTestObject().shrink(original, NON_ZERO_MULTIPLIER),
        closedUnitFractionRangeMatcher(shrunk));
  }

  @Override
  protected ClosedUnitFractionRangeShrinker makeTestObject() {
    return new ClosedUnitFractionRangeShrinker();
  }

}
