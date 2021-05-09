package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
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
import static com.rb.nonbiz.types.WeightedByUnitFraction.weightedByUnitFraction;
import static com.rb.nonbiz.types.WeightedByUnitFractionTest.weightedByUnitFractionMatcher;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This test class is not generic, but the publicly exposed matcher is.
 */
public class FlatLinearCombinationTest extends RBTestMatcher<FlatLinearCombination<String>> {

  @Test
  public void happyPath() {
    FlatLinearCombination<String> flatLinearCombination = flatLinearCombination(
        "a", unitFraction(0.4),
        "b", unitFraction(0.6));
    assertThat(
        flatLinearCombination.iterator(),
        iteratorMatcher(
            ImmutableList.of(
                weightedByUnitFraction("a", unitFraction(0.4)),
                weightedByUnitFraction("b", unitFraction(0.6)))
            .iterator(),
            f -> weightedByUnitFractionMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

  @Test
  public void fractionsSumToLessThan1_doesNotThrow() {
    FlatLinearCombination<String> doesNotThrow = flatLinearCombination(
        "a", unitFraction(0.4),
        "b", unitFraction(0.59));
  }

  @Test
  public void fractionsSumToMoreThan1_doesNotThrow() {
    FlatLinearCombination<String> doesNotThrow;
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
  public FlatLinearCombination<String> makeTrivialObject() {
    return singletonFlatLinearCombination("a");
  }

  @Override
  public FlatLinearCombination<String> makeNontrivialObject() {
    return flatLinearCombination(ImmutableList.of(
        weightedByUnitFraction("a", unitFraction(0.4)),
        weightedByUnitFraction("b", unitFraction(0.6))));
  }

  @Override
  public FlatLinearCombination<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return flatLinearCombination(ImmutableList.of(
        weightedByUnitFraction("a", unitFraction(0.4 + e)),
        weightedByUnitFraction("b", unitFraction(0.6 - e))));
  }

  @Override
  protected boolean willMatch(FlatLinearCombination<String> expected, FlatLinearCombination<String> actual) {
    return flatLinearCombinationMatcher(expected, f -> typeSafeEqualTo(f)).matches(actual);
  }

  public static <T> TypeSafeMatcher<FlatLinearCombination<T>> flatLinearCombinationMatcher(
      FlatLinearCombination<T> expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        matchList(v -> v.getWeightedItems(), f -> weightedByUnitFractionMatcher(f, itemMatcherGenerator)));
  }

}
