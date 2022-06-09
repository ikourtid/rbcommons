package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.FlatSignedLinearCombination.flatSignedLinearCombination;
import static com.rb.nonbiz.collections.FlatSignedLinearCombination.singletonFlatSignedLinearCombination;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.WeightedBySignedFraction.weightedBySignedFraction;
import static com.rb.nonbiz.types.WeightedBySignedFractionTest.weightedBySignedFractionMatcher;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This test class is not generic, but the publicly exposed matcher is.
 */
public class FlatSignedLinearCombinationTest extends RBTestMatcher<FlatSignedLinearCombination<String>> {

  @Test
  public void happyPath() {
    FlatSignedLinearCombination<String> flatSignedLinearCombination = flatSignedLinearCombination(
        "a", signedFraction(0.4),
        "b", signedFraction(-0.6));
    assertThat(
        flatSignedLinearCombination.iterator(),
        iteratorMatcher(
            ImmutableList.of(
                weightedBySignedFraction("a", signedFraction(0.4)),
                weightedBySignedFraction("b", signedFraction(-0.6)))
            .iterator(),
            f -> weightedBySignedFractionMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

  @Test
  public void fractionsSumToLessThan1_doesNotThrow() {
    FlatSignedLinearCombination<String> doesNotThrow = flatSignedLinearCombination(
        "a", signedFraction(0.4),
        "b", signedFraction(0.59));
  }

  @Test
  public void fractionsSumToMoreThan1_doesNotThrow() {
    FlatSignedLinearCombination<String> doesNotThrow;
    doesNotThrow = flatSignedLinearCombination(
        "a", signedFraction(0.4),
        "b", signedFraction(0.61));
    doesNotThrow = flatSignedLinearCombination(
        "a", signedFraction(0.9),
        "b", signedFraction(0.9),
        "c", signedFraction(0.9));
  }

  @Test
  public void hasZeroFraction_throws() {
    assertIllegalArgumentException( () -> flatSignedLinearCombination(
        "a", SIGNED_FRACTION_0,
        "b", SIGNED_FRACTION_1));
  }

  @Test
  public void getsNoFractionsMap_throws() {
    assertIllegalArgumentException( () -> flatSignedLinearCombination(emptyList()));
  }

  @Test
  public void testSize() {
    FlatSignedLinearCombination<String> oneElement = flatSignedLinearCombination(
        singletonList(weightedBySignedFraction("aaa", signedFraction(0.111))));
    assertEquals(1, oneElement.size());

    FlatSignedLinearCombination<String> twoElements = flatSignedLinearCombination(
        ImmutableList.of(
            weightedBySignedFraction("aaa", signedFraction(0.111)),
            weightedBySignedFraction("bbb", signedFraction(0.222))));
    assertEquals(2, twoElements.size());
  }

  @Override
  public FlatSignedLinearCombination<String> makeTrivialObject() {
    return singletonFlatSignedLinearCombination("a");
  }

  @Override
  public FlatSignedLinearCombination<String> makeNontrivialObject() {
    return flatSignedLinearCombination(ImmutableList.of(
        weightedBySignedFraction("a", signedFraction(0.4)),
        weightedBySignedFraction("b", signedFraction(0.6))));
  }

  @Override
  public FlatSignedLinearCombination<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return flatSignedLinearCombination(ImmutableList.of(
        weightedBySignedFraction("a", signedFraction(0.4 + e)),
        weightedBySignedFraction("b", signedFraction(0.6 - e))));
  }

  @Override
  protected boolean willMatch(FlatSignedLinearCombination<String> expected, FlatSignedLinearCombination<String> actual) {
    return flatSignedLinearCombinationMatcher(expected, f -> typeSafeEqualTo(f)).matches(actual);
  }

  public static <T> TypeSafeMatcher<FlatSignedLinearCombination<T>> flatSignedLinearCombinationMatcher(
      FlatSignedLinearCombination<T> expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        matchList(v -> v.getWeightedItems(), f -> weightedBySignedFractionMatcher(f, itemMatcherGenerator)));
  }

}
