package com.rb.nonbiz.functional;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.IntFunction;

import static com.rb.nonbiz.functional.NPlusOneAryFunction.nPlusOneAryFunction;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.emptyLabel;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

// This test class is not generic, but the typesafe matcher is.
public class NPlusOneAryFunctionTest extends RBTestMatcher<NPlusOneAryFunction<Integer, String>> {

  @Test
  public void testMustHaveNonNegativeN() {
    IntFunction<NPlusOneAryFunction<Integer, String>> maker = N ->
        nPlusOneAryFunction(N, (ignoredList, ignoredItem) -> "", emptyLabel());
    assertIllegalArgumentException( () -> maker.apply(-99));
    assertIllegalArgumentException( () -> maker.apply(-1));
    NPlusOneAryFunction<Integer, String> doesNotThrow;
    doesNotThrow = maker.apply(0);
    doesNotThrow = maker.apply(1);
    doesNotThrow = maker.apply(99);
  }

  @Test
  public void throwsWithIncorrectNumberOfArguments() {
    NPlusOneAryFunction<Double, Integer> function =
        nPlusOneAryFunction(3, (ignoredList, ignoredItem) -> DUMMY_POSITIVE_INTEGER, emptyLabel());
    Function<Integer, Integer> evaluateWith = numItems ->
        function.evaluate(Collections.nCopies(numItems, DUMMY_DOUBLE), DUMMY_DOUBLE);

    assertIllegalArgumentException( () -> evaluateWith.apply(0));
    assertIllegalArgumentException( () -> evaluateWith.apply(1));
    assertIllegalArgumentException( () -> evaluateWith.apply(2));
    int doesNotThrow = evaluateWith.apply(3);
    assertIllegalArgumentException( () -> evaluateWith.apply(4));
  }

  @Override
  public NPlusOneAryFunction<Integer, String> makeTrivialObject() {
    return nPlusOneAryFunction(1, (ignoredList, ignoredItem) -> "", emptyLabel());
  }

  @Override
  public NPlusOneAryFunction<Integer, String> makeNontrivialObject() {
    return nPlusOneAryFunction(
        3,
        (firstList, secondItem) -> Strings.format("%s%s%s_%s",
            firstList.get(0), firstList.get(1), firstList.get(2), secondItem),
        label("concatenation"));
  }

  @Override
  public NPlusOneAryFunction<Integer, String> makeMatchingNontrivialObject() {
    // no way to tweak it with epsilon, because this is a function, not a value.
    return nPlusOneAryFunction(
        3,
        (firstList, secondItem) -> Strings.format("%s%s%s_%s",
            firstList.get(0), firstList.get(1), firstList.get(2), secondItem),
        label("concatenation"));
  }

  @Override
  protected boolean willMatch(NPlusOneAryFunction<Integer, String> expected, NPlusOneAryFunction<Integer, String> actual) {
    return nPlusOneAryFunctionMatcher(expected).matches(actual);
  }

  public static <X, Y> TypeSafeMatcher<NPlusOneAryFunction<X, Y>> nPlusOneAryFunctionMatcher(
      NPlusOneAryFunction<X, Y> expected) {
    // We can't compare the Function object, so matching N is the best we can do.
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getN()));
  }

}
