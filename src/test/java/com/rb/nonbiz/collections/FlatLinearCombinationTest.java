package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.FlatLinearCombination.flatLinearCombination;
import static com.rb.nonbiz.collections.FlatLinearCombination.singletonFlatLinearCombination;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.WeightedBy.weightedBy;
import static com.rb.nonbiz.types.WeightedByTest.weightedByMatcher;
import static com.rb.nonbiz.types.WeightedByUnitFraction.weightedByUnitFraction;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This test class is not generic, but the publicly exposed matcher is.
 */
public class FlatLinearCombinationTest extends RBTestMatcher<FlatLinearCombination<UnitFraction, String>> {

  @Test
  public void happyPath() {
    FlatLinearCombination<UnitFraction, String> flatLinearCombination = flatLinearCombination(
        "a", unitFraction(0.4),
        "b", unitFraction(0.6));
    assertThat(
        flatLinearCombination.iterator(),
        iteratorMatcher(
            ImmutableList.of(
                weightedBy("a", unitFraction(0.4)),
                weightedBy("b", unitFraction(0.6)))
            .iterator(),
            f -> weightedByMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

  @Test
  public void fractionsSumToLessThan1_doesNotThrow() {
    FlatLinearCombination<UnitFraction, String> doesNotThrow = flatLinearCombination(
        "a", unitFraction(0.4),
        "b", unitFraction(0.59));
  }

  @Test
  public void fractionsSumToMoreThan1_doesNotThrow() {
    FlatLinearCombination<UnitFraction, String> doesNotThrow;
    doesNotThrow = flatLinearCombination(
        "a", unitFraction(0.4),
        "b", unitFraction(0.61));
    doesNotThrow = flatLinearCombination(
        "a", unitFraction(0.9),
        "b", unitFraction(0.9),
        "c", unitFraction(0.9));
  }

  @Test
  public void hasZeroFraction_throws() {
    assertIllegalArgumentException( () -> flatLinearCombination(
        "a", UNIT_FRACTION_0,
        "b", UNIT_FRACTION_1));
  }

  @Test
  public void getsNoFractionsMap_throws() {
    assertIllegalArgumentException( () -> flatLinearCombination(emptyList()));
  }

  @Override
  public FlatLinearCombination<UnitFraction, String> makeTrivialObject() {
    return singletonFlatLinearCombination("a", UNIT_FRACTION_1);
  }

  @Override
  public FlatLinearCombination<UnitFraction, String> makeNontrivialObject() {
    return flatLinearCombination(ImmutableList.of(
        weightedByUnitFraction("a", unitFraction(0.4)),
        weightedByUnitFraction("b", unitFraction(0.6))));
  }

  @Override
  public FlatLinearCombination<UnitFraction, String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return flatLinearCombination(ImmutableList.of(
        weightedByUnitFraction("a", unitFraction(0.4 + e)),
        weightedByUnitFraction("b", unitFraction(0.6 - e))));
  }

  @Override
  protected boolean willMatch(FlatLinearCombination<UnitFraction, String> expected,
                              FlatLinearCombination<UnitFraction, String> actual) {
    return flatLinearCombinationMatcher(expected, f -> typeSafeEqualTo(f)).matches(actual);
  }

  public static <W extends RBNumeric<W>, T> TypeSafeMatcher<FlatLinearCombination<W, T>> flatLinearCombinationMatcher(
      FlatLinearCombination<W, T> expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        matchList(v -> v.getWeightedItems(), f -> weightedByMatcher(f, itemMatcherGenerator)));
  }

}
