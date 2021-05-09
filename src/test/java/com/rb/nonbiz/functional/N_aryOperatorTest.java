package com.rb.nonbiz.functional;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.IntFunction;

import static com.rb.nonbiz.functional.N_aryFunctionTest.n_aryFunctionMatcher;
import static com.rb.nonbiz.functional.N_aryOperator.n_aryOperator;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.emptyLabel;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static junit.framework.TestCase.assertEquals;

public class N_aryOperatorTest extends RBTestMatcher<N_aryOperator<Double>> {

  public static N_aryOperator<Double> averageOf2() {
    return n_aryOperator(
        2,
        items -> 0.5 * (items.get(0) + items.get(1)),
        label("average of two items"));
  }

  public static N_aryOperator<Double> averageOf3() {
    return n_aryOperator(
        3,
        items -> (items.get(0) + items.get(1) + items.get(2)) / 3.0,
        label("average of three items"));
  }

  @Test
  public void testMustHavePositiveN() {
    IntFunction<N_aryOperator<Integer>> maker = N -> n_aryOperator(N, ignored -> DUMMY_POSITIVE_INTEGER, emptyLabel());
    assertIllegalArgumentException( () -> maker.apply(-99));
    assertIllegalArgumentException( () -> maker.apply(-1));
    assertIllegalArgumentException( () -> maker.apply(0));
    N_aryOperator<Integer> doesNotThrow;
    doesNotThrow = maker.apply(1);
    doesNotThrow = maker.apply(99);
  }

  @Test
  public void testBasicMethods() {
    assertEquals(3, averageOf3().getN());
    assertEquals(
        doubleExplained(5.5, 0.5 * (5.0 + 6.0)),
        averageOf2().evaluate(ImmutableList.of(5.0, 6.0)),
        1e-8);
    assertEquals(
        doubleExplained(7.0, (5.0 + 6.0 + 10.0) / 3),
        averageOf3().evaluate(ImmutableList.of(5.0, 6.0, 10.0)),
        1e-8);
  }

  @Test
  public void throwsWithIncorrectNumberOfArguments() {
    N_aryOperator<Double> ternaryOperator = n_aryOperator(3, ignored -> DUMMY_DOUBLE, emptyLabel());
    Function<Integer, Double> evaluateWith = numItems ->
        ternaryOperator.evaluate(Collections.nCopies(numItems, DUMMY_DOUBLE));
    assertIllegalArgumentException( () -> evaluateWith.apply(0));
    assertIllegalArgumentException( () -> evaluateWith.apply(1));
    assertIllegalArgumentException( () -> evaluateWith.apply(2));
    double doesNotThrow = evaluateWith.apply(3);
    assertIllegalArgumentException( () -> evaluateWith.apply(4));
  }

  @Override
  public N_aryOperator<Double> makeTrivialObject() {
    return averageOf2();
  }

  @Override
  public N_aryOperator<Double> makeNontrivialObject() {
    return averageOf3();
  }

  @Override
  public N_aryOperator<Double> makeMatchingNontrivialObject() {
    // no way to tweak it with epsilon
    return averageOf3();
  }

  @Override
  protected boolean willMatch(N_aryOperator<Double> expected, N_aryOperator<Double> actual) {
    return n_aryOperatorMatcher(expected).matches(actual);
  }

  public static <C extends Comparable<?>> TypeSafeMatcher<N_aryOperator<C>> n_aryOperatorMatcher(
      N_aryOperator<C> expected) {
    // We can't compare the Function object, so matching N is the best we can do.
    return makeMatcher(expected,
        match(v -> (N_aryFunction<C, C>) v, f -> n_aryFunctionMatcher(f)));
  }

}
