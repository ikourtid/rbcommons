package com.rb.nonbiz.functional;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.IntFunction;

import static com.rb.nonbiz.functional.N_aryFunction.n_aryFunction;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.emptyLabel;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

// This test class is not generic, but the typesafe matcher is.
public class N_aryFunctionTest extends RBTestMatcher<N_aryFunction<Integer, String>> {

  @Test
  public void testMustHavePositiveN() {
    IntFunction<N_aryFunction<Integer, String>> maker = N -> n_aryFunction(N, ignored -> "", emptyLabel());
    assertIllegalArgumentException( () -> maker.apply(-99));
    assertIllegalArgumentException( () -> maker.apply(-1));
    assertIllegalArgumentException( () -> maker.apply(0));
    N_aryFunction<Integer, String> doesNotThrow;
    doesNotThrow = maker.apply(1);
    doesNotThrow = maker.apply(99);
  }

  @Test
  public void throwsWithIncorrectNumberOfArguments() {
    N_aryFunction<Double, String> ternaryFunction = n_aryFunction(3, ignored -> "", emptyLabel());
    Function<Integer, String> evaluateWith = numItems ->
        ternaryFunction.evaluate(Collections.nCopies(numItems, DUMMY_DOUBLE));
    assertIllegalArgumentException( () -> evaluateWith.apply(0));
    assertIllegalArgumentException( () -> evaluateWith.apply(1));
    assertIllegalArgumentException( () -> evaluateWith.apply(2));
    String doesNotThrow = evaluateWith.apply(3);
    assertIllegalArgumentException( () -> evaluateWith.apply(4));
  }

  @Override
  public N_aryFunction<Integer, String> makeTrivialObject() {
    return n_aryFunction(1, ignored -> "", emptyLabel());
  }

  @Override
  public N_aryFunction<Integer, String> makeNontrivialObject() {
    return n_aryFunction(
        3,
        items -> Strings.format("%s%s%s", items.get(0), items.get(1), items.get(2)),
        label("concatenation"));
  }

  @Override
  public N_aryFunction<Integer, String> makeMatchingNontrivialObject() {
    // no way to tweak it with epsilon
    return makeNontrivialObject();
  }

  @Override
  protected boolean willMatch(N_aryFunction<Integer, String> expected, N_aryFunction<Integer, String> actual) {
    return n_aryFunctionMatcher(expected).matches(actual);
  }

  public static <X, Y> TypeSafeMatcher<N_aryFunction<X, Y>> n_aryFunctionMatcher(
      N_aryFunction<X, Y> expected) {
    // We can't compare the Function object, so matching N is the best we can do.
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getN()));
  }

}
