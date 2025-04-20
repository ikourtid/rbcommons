package com.rb.nonbiz.search;

import com.rb.nonbiz.search.BinarySearchRawParameters.BinarySearchRawParametersBuilder;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Comparator.comparing;

// This test class is not generic, but the publicly exposed static matcher is.
public class BinarySearchRawParametersTest extends RBTestMatcher<BinarySearchRawParameters<Double, String>> {

  @Test
  public void tooFewMaxIterationSteps_throws() {
    Function<Integer, BinarySearchRawParameters<Double, String>> maker = maxIterations -> makeBuilderWithDummyLambdas()
        .setTargetY("1.2")
        .setMaxIterations(maxIterations)
        .build();
    assertIllegalArgumentException( () -> maker.apply(-123));
    assertIllegalArgumentException( () -> maker.apply(-1));
    assertIllegalArgumentException( () -> maker.apply(0));
    assertIllegalArgumentException( () -> maker.apply(1));
    BinarySearchRawParameters<Double, String> doesNotThrow;
    doesNotThrow = maker.apply(2);
    doesNotThrow = maker.apply(3);
    doesNotThrow = maker.apply(123);
  }

  private BinarySearchRawParametersBuilder<Double, String> makeBuilderWithDummyLambdas() {
    return BinarySearchRawParametersBuilder.<Double, String>binarySearchRawParametersBuilder()
        .setComparatorForX(comparing(v -> v))
        .setComparatorForY(comparing(v -> v))
        .setEvaluatorOfX(x -> Strings.format("%s", x)) // not dummy; yValuesMustBeInOrder_otherwiseThrows relies on it
        .setMidpointGenerator( (x1, x2) -> x1);
  }

  @Override
  public BinarySearchRawParameters<Double, String> makeTrivialObject() {
    // normally we don't use dummy stuff in the make*rivialObject methods of RBTestMatcher,
    // but this is a special case, because the missing fields are lambdas, which the matcher does not look at anyway.
    return makeBuilderWithDummyLambdas()
        .setTargetY("0.5")
        .setMaxIterations(2)
        .build();
  }

  @Override
  public BinarySearchRawParameters<Double, String> makeNontrivialObject() {
    // normally we don't use dummy stuff in the make*rivialObject methods of RBTestMatcher,
    // but this is a special case, because the missing fields are lambdas, which the matcher does not look at anyway.
    return makeBuilderWithDummyLambdas()
        .setTargetY("2.1")
        .setMaxIterations(123)
        .build();
  }

  @Override
  public BinarySearchRawParameters<Double, String> makeMatchingNontrivialObject() {
    // normally we don't use dummy stuff in the make*rivialObject methods of RBTestMatcher,
    // but this is a special case, because the missing fields are lambdas, which the matcher does not look at anyway.
    // Nothing to tweak here, though.
    return makeBuilderWithDummyLambdas()
        .setTargetY("2.1")
        .setMaxIterations(123)
        .build();
  }

  @Override
  protected boolean willMatch(
      BinarySearchRawParameters<Double, String> expected,
      BinarySearchRawParameters<Double, String> actual) {
    return binarySearchRawParametersPartialMatcher(
        expected,
        f -> typeSafeEqualTo(f))
        .matches(actual);
  }

  // This ends in 'PartialMatcher' because this is a rare data class that stores lambdas, which are not comparable.
  // Therefore, two objects that differ in the lambdas will still match.
  public static <X, Y> TypeSafeMatcher<BinarySearchRawParameters<X, Y>> binarySearchRawParametersPartialMatcher(
      BinarySearchRawParameters<X, Y> expected,
      MatcherGenerator<Y> yMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getTargetY(), yMatcherGenerator),
        matchUsingEquals(v -> v.getMaxIterations()));
  }

}
