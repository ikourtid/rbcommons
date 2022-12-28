package com.rb.nonbiz.search;

import com.rb.nonbiz.search.BinarySearchParameters.BinarySearchParametersBuilder;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.search.BinarySearchTerminationPredicate.onlyTerminateBasedOnX;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Comparator.comparing;

// This test class is not generic, but the publicly exposed static matcher is.
public class BinarySearchParametersTest extends RBTestMatcher<BinarySearchParameters<Double, String>> {

  @Test
  public void lowerAndUpperBoundsSame_throws() {
    Function<Double, BinarySearchParameters<Double, String>> maker = upperBound -> makeBuilderWithDummyLambdas()
        .setLowerBoundX(1.1)
        .setUpperBoundX(upperBound)
        .setTargetY("1.1")
        .setMaxIterations(2)
        .build();
    // It's possible in general to set things up so that X is increasing while Y is decreasing
    // This throws, but for a different reason: not because 1.0999999 < 1.1, but because
    // f(1.09999999) < f(1.1).
    assertIllegalArgumentException( () -> maker.apply(1.1 - 1e-9));
    assertIllegalArgumentException( () -> maker.apply(1.1));
    BinarySearchParameters<Double, String> doesNotThrow = maker.apply(1.1 + 1e-9);
  }

  @Test
  public void tooFewMaxIterationSteps_throws() {
    Function<Integer, BinarySearchParameters<Double, String>> maker = maxIterations -> makeBuilderWithDummyLambdas()
        .setLowerBoundX(1.1)
        .setUpperBoundX(2.2)
        .setTargetY("1.2")
        .setMaxIterations(maxIterations)
        .build();
    assertIllegalArgumentException( () -> maker.apply(-123));
    assertIllegalArgumentException( () -> maker.apply(-1));
    assertIllegalArgumentException( () -> maker.apply(0));
    assertIllegalArgumentException( () -> maker.apply(1));
    BinarySearchParameters<Double, String> doesNotThrow;
    doesNotThrow = maker.apply(2);
    doesNotThrow = maker.apply(3);
    doesNotThrow = maker.apply(123);
  }

  @Test
  public void yValuesMustBeInOrder_otherwiseThrows() {
    // f(9) is "9" and f(11) is "11"; the numbers are increasing, but using string comparison, they are decreasing.
    assertIllegalArgumentException( () -> makeBuilderWithDummyLambdas()
        .setLowerBoundX(9.0)
        .setUpperBoundX(11.0)
        .setTargetY("10.0")
        .setMaxIterations(2)
        .build());
    assertIllegalArgumentException( () -> makeBuilderWithDummyLambdas()
        .setLowerBoundX(2.2)
        .setUpperBoundX(3.3)
        .setTargetY("2.1")
        .setMaxIterations(2)
        .build());
    assertIllegalArgumentException( () -> makeBuilderWithDummyLambdas()
        .setLowerBoundX(2.2)
        .setUpperBoundX(3.3)
        .setTargetY("3.4")
        .setMaxIterations(2)
        .build());
    BinarySearchParameters<Double, String> doesNotThrow = makeBuilderWithDummyLambdas()
        .setLowerBoundX(2.2)
        .setUpperBoundX(3.3)
        .setTargetY("2.7")
        .setMaxIterations(2)
        .build();
  }

  private BinarySearchParametersBuilder<Double, String> makeBuilderWithDummyLambdas() {
    return BinarySearchParametersBuilder.<Double, String>binarySearchParametersBuilder()
        .setComparatorForX(comparing(v -> v))
        .setComparatorForY(comparing(v -> v))
        .setEvaluatorOfX(x -> Strings.format("%s", x)) // not dummy; yValuesMustBeInOrder_otherwiseThrows relies on it
        .setTerminationPredicate(onlyTerminateBasedOnX( (x1, x2) -> true))
        .setMidpointGenerator( (x1, x2) -> x1);
  }

  @Override
  public BinarySearchParameters<Double, String> makeTrivialObject() {
    // normally we don't use dummy stuff in the make*rivialObject methods of RBTestMatcher,
    // but this is a special case, because the missing fields are lambdas, which the matcher does not look at anyway.
    return makeBuilderWithDummyLambdas()
        .setLowerBoundX(0.0)
        .setUpperBoundX(1.0)
        .setTargetY("0.5")
        .setMaxIterations(2)
        .build();
  }

  @Override
  public BinarySearchParameters<Double, String> makeNontrivialObject() {
    // normally we don't use dummy stuff in the make*rivialObject methods of RBTestMatcher,
    // but this is a special case, because the missing fields are lambdas, which the matcher does not look at anyway.
    return makeBuilderWithDummyLambdas()
        .setLowerBoundX(1.1)
        .setUpperBoundX(3.3)
        .setTargetY("2.1")
        .setMaxIterations(123)
        .build();
  }

  @Override
  public BinarySearchParameters<Double, String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    // normally we don't use dummy stuff in the make*rivialObject methods of RBTestMatcher,
    // but this is a special case, because the missing fields are lambdas, which the matcher does not look at anyway.
    return makeBuilderWithDummyLambdas()
        .setLowerBoundX(1.1 + e)
        .setUpperBoundX(3.3 + e)
        .setTargetY("2.1")
        .setMaxIterations(123)
        .build();
  }

  @Override
  protected boolean willMatch(
      BinarySearchParameters<Double, String> expected,
      BinarySearchParameters<Double, String> actual) {
    return binarySearchParametersPartialMatcher(
        expected,
        f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8),
        f -> typeSafeEqualTo(f))
        .matches(actual);
  }

  // This ends in 'PartialMatcher' because this is a rare data class that stores lambdas, which are not comparable.
  // Therefore, two objects that differ in the lambdas will still match.
  public static <X, Y> TypeSafeMatcher<BinarySearchParameters<X, Y>> binarySearchParametersPartialMatcher(
      BinarySearchParameters<X, Y> expected,
      MatcherGenerator<X> xMatcherGenerator,
      MatcherGenerator<Y> yMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getLowerBoundX(), xMatcherGenerator),
        match(v -> v.getUpperBoundX(), xMatcherGenerator),
        match(v -> v.getTargetY(), yMatcherGenerator),
        matchUsingEquals(v -> v.getMaxIterations()));
  }

}
