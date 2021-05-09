package com.rb.nonbiz.search;

import com.rb.nonbiz.search.BinarySearchResult.BinarySearchResultBuilder;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static java.util.Comparator.naturalOrder;
import static junit.framework.TestCase.assertEquals;

// This test class is not generic, but the publicly exposed static matcher is.
public class BinarySearchResultTest extends RBTestMatcher<BinarySearchResult<Double, String>> {

  // Before running a binary search, we may check that the targetY is inside [lowerBoundY, upperBoundY].
  // If it is NOT, we might choose to skip the binary search and just return a BinarySearchResult
  // that simply contains the bounds and numIterationsUsed = 0.
  // We do so by building with .buildForTargetOutsideOfBounds(), which replaces the check that targetY is
  // inside the bounds [lowerBoundY, upperBoundY] with the opposite; that targetY is NOT inside the bounds.
  @Test
  public void targetYNotInsideBoundsY_specialCase() {
    Comparator<String> ascendingStringComparator = naturalOrder();
    Function<String, BinarySearchResult<Double, String>> maker = targetY ->
        BinarySearchResultBuilder.<Double, String>binarySearchResultBuilder()
            .setLowerBoundX(0.0)
            .setUpperBoundX(1.0)
            .setLowerBoundY("b")
            .setUpperBoundY("d")
            .setNumIterationsUsed(0)
            .setTargetY(targetY)
            .setComparatorForY(ascendingStringComparator)
            .buildForTargetOutsideOfBounds();
    // In the following, targetY = "c" would be in ["b", "d"], while .buildForTargetOutsideOfBounds() says it is not
    assertIllegalArgumentException( () -> maker.apply("c"));
    BinarySearchResult<Double, String> doesNotThrow;
    doesNotThrow = maker.apply("a");  // "a" is outside of ["b", "d"]
    doesNotThrow = maker.apply("e");  // "e" is outside of ["b", "d"]
  }

  @Test
  public void yMustBeIncreasing_otherwiseThrows_comparatorUsesNaturalOrder() {
    Comparator<String> ascendingStringComparator = naturalOrder();
    Function<String, BinarySearchResult<Double, String>> maker = upperBoundY ->
        BinarySearchResultBuilder.<Double, String>binarySearchResultBuilder()
            .setLowerBoundX(0.0)
            .setUpperBoundX(1.0)
            .setLowerBoundY("b")
            .setUpperBoundY(upperBoundY)
            .setNumIterationsUsed(0)
            .setTargetY("b")
            .setComparatorForY(ascendingStringComparator)
            .build();
    assertIllegalArgumentException( () -> maker.apply("a")); // this would cause b -> a
    BinarySearchResult<Double, String> doesNotThrow;
    doesNotThrow = maker.apply("b");
    doesNotThrow = maker.apply("c");
  }

  // This is like yMustBeIncreasing_otherwiseThrows_comparatorUsesNaturalOrder, but the point is that the only
  // thing that matters in determining the valid order of Y is the comparator of Y, not the natural ordering of Y
  // in the cases where Y implements Comparable.
  @Test
  public void yMustBeNondecreasing_otherwiseThrows_comparatorUsesReversedNaturalOrder() {
    Comparator<String> descendingStringComparator = Comparator.<String>naturalOrder().reversed();
    Function<String, BinarySearchResult<Double, String>> maker = upperBoundY ->
        BinarySearchResultBuilder.<Double, String>binarySearchResultBuilder()
            .setLowerBoundX(0.0)
            .setUpperBoundX(1.0)
            .setLowerBoundY("b")
            .setUpperBoundY(upperBoundY)
            .setNumIterationsUsed(0)
            .setTargetY("b")
            .setComparatorForY(descendingStringComparator)
            .build();
    BinarySearchResult<Double, String> doesNotThrow;
    doesNotThrow = maker.apply("a");    // a -> b  increasing
    doesNotThrow = maker.apply("b");    // b -> b  not decreasing
    assertIllegalArgumentException( () -> maker.apply("c")); // this would cause b -> c, but we assert reversed order.
  }

  // even for the case when targetY is not inside [lowerBoundsY, upperBoundsY], the y-bounds must be non-decreasing
  @Test
  public void yMustBeNondecreasing_targetYNotInBounds_comparatorUsesNaturalOrder() {
    Comparator<String> ascendingStringComparator = naturalOrder();
    Function<String, BinarySearchResult<Double, String>> maker = lowerBoundY ->
        BinarySearchResultBuilder.<Double, String>binarySearchResultBuilder()
            .setLowerBoundX(0.0)
            .setUpperBoundX(1.0)
            .setLowerBoundY(lowerBoundY)
            .setUpperBoundY("b")
            .setNumIterationsUsed(0)
            .setTargetY("d")
            .setComparatorForY(ascendingStringComparator)
            .buildForTargetOutsideOfBounds();
    assertIllegalArgumentException( () -> maker.apply("c")); // this would cause c -> b; decreasing
    BinarySearchResult<Double, String> doesNotThrow;
    doesNotThrow = maker.apply("a");  // a -> b   increasing
    doesNotThrow = maker.apply("b");  // b -> b   non-decreasing
  }

  @Test
  public void test_getBestX_getBestY() {
    // You can think of this example as a binary search on x where f(x)=x^2; the final x will be sqrt(x).
    BinarySearchResult<Double, BigDecimal> bestIsLower =
        BinarySearchResultBuilder.<Double, BigDecimal>binarySearchResultBuilder()
            .setLowerBoundX(1.4)
            .setUpperBoundX(1.6)
            .setLowerBoundY(BigDecimal.valueOf(doubleExplained(1.96, 1.4 * 1.4)))
            .setUpperBoundY(BigDecimal.valueOf(doubleExplained(2.56, 1.6 * 1.6)))
            .setNumIterationsUsed(DUMMY_POSITIVE_INTEGER)
            .setTargetY(BigDecimal.valueOf(2.0))
            .setComparatorForY(naturalOrder())
            .build();
    BinarySearchResult<Double, BigDecimal> bestIsUpper =
        BinarySearchResultBuilder.<Double, BigDecimal>binarySearchResultBuilder()
            .setLowerBoundX(1.3)
            .setUpperBoundX(1.5)
            .setLowerBoundY(BigDecimal.valueOf(doubleExplained(1.69, 1.3 * 1.3)))
            .setUpperBoundY(BigDecimal.valueOf(doubleExplained(2.25, 1.5 * 1.5)))
            .setNumIterationsUsed(DUMMY_POSITIVE_INTEGER)
            .setTargetY(BigDecimal.valueOf(2.0))
            .setComparatorForY(naturalOrder())
            .build();
    // Using Float to illustrate that the distance metric type does not have to be the same type as either X or Y
    BiFunction<BigDecimal, BigDecimal, Float> distanceMetric = (v1, v2) -> (float) v1.subtract(v2).abs().doubleValue();

    // 1.96 (lower X) is closer to 2.0 than 2.56 is
    assertEquals(1.4,  bestIsLower.getBestX(distanceMetric),               1e-8);
    assertEquals(1.96, bestIsLower.getBestY(distanceMetric).doubleValue(), 1e-8);

    // 2.25 (upper X) is closer to 2.0 than 1.69 is
    assertEquals(1.5,  bestIsUpper.getBestX(distanceMetric),               1e-8);
    assertEquals(2.25, bestIsUpper.getBestY(distanceMetric).doubleValue(), 1e-8);
  }

  @Override
  public BinarySearchResult<Double, String> makeTrivialObject() {
    return BinarySearchResultBuilder.<Double, String>binarySearchResultBuilder()
        .setLowerBoundX(0.0)
        .setUpperBoundX(1.0)
        .setLowerBoundY("0")
        .setUpperBoundY("1")
        .setNumIterationsUsed(0)
        .setTargetY("1")
        .setComparatorForY(String::compareTo)
        .build();
  }

  @Override
  public BinarySearchResult<Double, String> makeNontrivialObject() {
    return BinarySearchResultBuilder.<Double, String>binarySearchResultBuilder()
        .setLowerBoundX(1.1)
        .setUpperBoundX(2.2)
        .setLowerBoundY("a")
        .setUpperBoundY("c")
        .setNumIterationsUsed(123)
        .setTargetY("b")
        .setComparatorForY(String::compareTo)
        .build();
  }

  @Override
  public BinarySearchResult<Double, String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return BinarySearchResultBuilder.<Double, String>binarySearchResultBuilder()
        .setLowerBoundX(1.1 + e)
        .setUpperBoundX(2.2 + e)
        .setLowerBoundY("a")
        .setUpperBoundY("c")
        .setNumIterationsUsed(123)
        .setTargetY("b")
        .setComparatorForY(String::compareTo)
        .build();
  }

  @Override
  protected boolean willMatch(BinarySearchResult<Double, String> expected, BinarySearchResult<Double, String> actual) {
    return binarySearchResultMatcher(
        expected,
        f -> doubleAlmostEqualsMatcher(f, 1e-8),
        f -> typeSafeEqualTo(f))
        .matches(actual);
  }

  public static <X, Y>TypeSafeMatcher<BinarySearchResult<X, Y>> binarySearchResultMatcher(
      BinarySearchResult<X, Y> expected,
      MatcherGenerator<X> xMatcherGenerator,
      MatcherGenerator<Y> yMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getLowerBoundX(), xMatcherGenerator),
        match(v -> v.getUpperBoundX(), xMatcherGenerator),
        match(v -> v.getLowerBoundY(), yMatcherGenerator),
        match(v -> v.getUpperBoundY(), yMatcherGenerator),
        matchUsingEquals(v -> v.getNumIterationsUsed()),
        match(v -> v.getTargetY(),     yMatcherGenerator));
  }

}
