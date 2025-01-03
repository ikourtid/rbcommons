package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.ClosedRange.singletonClosedRange;
import static com.rb.nonbiz.collections.ClosedRangeTest.closedRangeMatcher;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.ValueOutsideClosedRange.valueOutsideClosedRange;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

// This test class is not generic, but the static matcher is.
public class ValueOutsideClosedRangeTest extends RBTestMatcher<ValueOutsideClosedRange<UnitFraction>> {

  @Test
  public void testValueMustBeContained() {
    BiConsumer<UnitFraction, ClosedRange<UnitFraction>> assertThrows = (unitFraction, closedRange) ->
        assertIllegalArgumentException( () -> valueOutsideClosedRange(unitFraction, closedRange));
    BiConsumer<UnitFraction, ClosedRange<UnitFraction>> assertDoesNotThrow = (unitFraction, closedRange) -> {
      ValueOutsideClosedRange<UnitFraction> doesNotThrow = valueOutsideClosedRange(unitFraction, closedRange);
    };

    assertDoesNotThrow.accept(UNIT_FRACTION_0,    closedRange(unitFraction(0.1), unitFraction(0.2)));
    assertDoesNotThrow.accept(unitFraction(0.09), closedRange(unitFraction(0.1), unitFraction(0.2)));
    assertDoesNotThrow.accept(unitFraction(0.21), closedRange(unitFraction(0.1), unitFraction(0.2)));
    assertDoesNotThrow.accept(UNIT_FRACTION_1,    closedRange(unitFraction(0.1), unitFraction(0.2)));

    assertDoesNotThrow.accept(UNIT_FRACTION_0,    singletonClosedRange(unitFraction(0.1)));
    assertDoesNotThrow.accept(unitFraction(0.09), singletonClosedRange(unitFraction(0.1)));
    assertDoesNotThrow.accept(unitFraction(0.21), singletonClosedRange(unitFraction(0.1)));
    assertDoesNotThrow.accept(UNIT_FRACTION_1,    singletonClosedRange(unitFraction(0.1)));

    assertThrows.accept(unitFraction(0.1),  closedRange(unitFraction(0.1), unitFraction(0.2)));
    assertThrows.accept(unitFraction(0.15), closedRange(unitFraction(0.1), unitFraction(0.2)));
    assertThrows.accept(unitFraction(0.2),  closedRange(unitFraction(0.1), unitFraction(0.2)));

    assertThrows.accept(unitFraction(0.1),  singletonClosedRange(unitFraction(0.1)));
  }

  @Override
  public ValueOutsideClosedRange<UnitFraction> makeTrivialObject() {
    return valueOutsideClosedRange(UNIT_FRACTION_0, singletonClosedRange(UNIT_FRACTION_1));
  }

  @Override
  public ValueOutsideClosedRange<UnitFraction> makeNontrivialObject() {
    return valueOutsideClosedRange(unitFraction(0.11), closedRange(unitFraction(0.21), unitFraction(0.31)));
  }

  @Override
  public ValueOutsideClosedRange<UnitFraction> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return valueOutsideClosedRange(
        unitFraction(0.11 + e), closedRange(unitFraction(0.21 + e), unitFraction(0.31 + e)));
  }

  @Override
  protected boolean willMatch(ValueOutsideClosedRange<UnitFraction> expected,
                              ValueOutsideClosedRange<UnitFraction> actual) {
    return valueOutsideClosedRangeMatcher(expected, f -> preciseValueMatcher(f, Epsilon.DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <T extends Comparable<? super T>> TypeSafeMatcher<ValueOutsideClosedRange<T>> valueOutsideClosedRangeMatcher(
      ValueOutsideClosedRange<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getValue(),       matcherGenerator),
        match(v -> v.getClosedRange(), f -> closedRangeMatcher(f, matcherGenerator)));
  }

}
