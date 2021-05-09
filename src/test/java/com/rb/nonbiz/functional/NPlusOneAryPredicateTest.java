package com.rb.nonbiz.functional;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.IntFunction;

import static com.rb.nonbiz.functional.NPlusOneAryPredicate.nPlusOneAryPredicate;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_BOOLEAN;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.emptyLabel;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

// This test class is not generic, but the typesafe matcher is.
public class NPlusOneAryPredicateTest extends RBTestMatcher<NPlusOneAryPredicate<Integer>> {

  @Test
  public void testMustHaveNonNegativeN() {
    IntFunction<NPlusOneAryPredicate<Integer>> maker = N ->
        nPlusOneAryPredicate(N, (ignoredList, ignoredItem) -> DUMMY_BOOLEAN, emptyLabel());
    assertIllegalArgumentException( () -> maker.apply(-99));
    assertIllegalArgumentException( () -> maker.apply(-1));
    NPlusOneAryPredicate<Integer> doesNotThrow;
    doesNotThrow = maker.apply(0);
    doesNotThrow = maker.apply(1);
    doesNotThrow = maker.apply(99);
  }

  @Test
  public void throwsWithIncorrectNumberOfArguments() {
    NPlusOneAryPredicate<Double> predicate = nPlusOneAryPredicate(3, (ignoredList, ignoredItem) -> DUMMY_BOOLEAN, emptyLabel());
    Function<Integer, Boolean> evaluateWith = numItems ->
        predicate.evaluate(Collections.nCopies(numItems, DUMMY_DOUBLE), DUMMY_DOUBLE);

    assertIllegalArgumentException( () -> evaluateWith.apply(0));
    assertIllegalArgumentException( () -> evaluateWith.apply(1));
    assertIllegalArgumentException( () -> evaluateWith.apply(2));
    boolean doesNotThrow = evaluateWith.apply(3);
    assertIllegalArgumentException( () -> evaluateWith.apply(4));
  }

  @Override
  public NPlusOneAryPredicate<Integer> makeTrivialObject() {
    return nPlusOneAryPredicate(1, (ignoredList, ignoredItem) -> false, emptyLabel());
  }

  @Override
  public NPlusOneAryPredicate<Integer> makeNontrivialObject() {
    return nPlusOneAryPredicate(
        3,
        (firstList, secondItem) -> (firstList.get(0) + firstList.get(1) + firstList.get(2)) < secondItem,
        label("concatenation"));
  }

  @Override
  public NPlusOneAryPredicate<Integer> makeMatchingNontrivialObject() {
    // no way to tweak it with epsilon, because this is a function, not a value.
    return nPlusOneAryPredicate(
        3,
        (firstList, secondItem) -> (firstList.get(0) + firstList.get(1) + firstList.get(2)) < secondItem,
        label("concatenation"));
  }

  @Override
  protected boolean willMatch(NPlusOneAryPredicate<Integer> expected, NPlusOneAryPredicate<Integer> actual) {
    return nPlusOneAryPredicateMatcher(expected).matches(actual);
  }

  public static <X> TypeSafeMatcher<NPlusOneAryPredicate<X>> nPlusOneAryPredicateMatcher(
      NPlusOneAryPredicate<X> expected) {
    // We can't compare the Function object, so matching N is the best we can do.
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getN()));
  }

}
