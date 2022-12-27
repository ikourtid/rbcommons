package com.rb.nonbiz.search;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Money;
import com.rb.biz.types.OnesBasedReturn;
import com.rb.nonbiz.search.BinarySearchParameters.BinarySearchParametersBuilder;
import com.rb.nonbiz.search.BinarySearchResult.BinarySearchResultBuilder;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.search.BinarySearchResult.BinarySearchResultBuilder.binarySearchResultBuilder;
import static com.rb.nonbiz.search.BinarySearchResultTest.binarySearchResultMatcher;
import static com.rb.nonbiz.search.BinarySearchTerminationPredicate.onlyTerminateBasedOnX;
import static com.rb.nonbiz.search.BinarySearchTerminationPredicate.onlyTerminateBasedOnY;
import static com.rb.nonbiz.search.BinarySearchTerminationPredicate.terminateBasedOnXandY;
import static com.rb.nonbiz.search.BinarySearchTerminationPredicate.terminateBasedOnXorY;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static java.util.Comparator.naturalOrder;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

public class BinarySearchTest extends RBTest<BinarySearch> {

  private final BinarySearchTerminationPredicate<OnesBasedReturn, Money> DEFAULT_TERMINATION_PREDICATE =
      onlyTerminateBasedOnX( (ret1, ret2) -> ret1.almostEquals(ret2, 1e-8));
  private final Money TARGET_Y = money(1_600);

  @Test
  public void happyPath_highPrecision() {
    assertResult(
        makeBinarySearchParametersBuilder()
            .setLowerBoundX(onesBasedReturn(0.1))
            .setUpperBoundX(onesBasedReturn(10.0))
            .setTerminationPredicate(DEFAULT_TERMINATION_PREDICATE)
            .build(),
        v -> v
            .setLowerBoundX(onesBasedReturn(doubleExplained(0.8, 1_600 / 2_000.0)))
            .setUpperBoundX(onesBasedReturn(0.8))
            .setLowerBoundY(money(1_600))
            .setUpperBoundY(money(1_600))
            .setNumIterationsUsed(31)); // the binary search happens to take 31 iterations here.
  }

  @Test
  public void happyPath_lowPrecision() {
    assertResult(
        makeBinarySearchParametersBuilder()
            .setLowerBoundX(onesBasedReturn(0.1))
            .setUpperBoundX(onesBasedReturn(10.0))
            .setTerminationPredicate(onlyTerminateBasedOnX( (ret1, ret2) ->
                // using a less tight epsilon of 0.01 for this test
                ret1.asBigDecimal().subtract(ret2.asBigDecimal()).abs().compareTo(BigDecimal.valueOf(1e-2)) < 0))
            .build(), // using lower epsilon for termination
        v -> v
            // ideal is 0.8, but the binary search doesn't iterate enough times in this test (intentionally)
            .setLowerBoundX(onesBasedReturn(0.79609375))
            .setUpperBoundX(onesBasedReturn(0.80576171875))
            .setLowerBoundY(money(1592.1875))
            .setUpperBoundY(money(1611.5234375))
            .setNumIterationsUsed(11)); // the binary search happens to take 11 iterations here.
    assertTrue(doubleExplained(0.00966796875, 0.80576171875 - 0.79609375) < 1e-2);
  }

  @Test
  public void terminationCausedByAllReasonCombinations() {
    BinarySearchTerminationPredicate<OnesBasedReturn, Money> onlyX_onlyXmatters =
        onlyTerminateBasedOnX( (ret1, ret2) -> ret1.almostEquals(ret2, 1e-8));
    BinarySearchTerminationPredicate<OnesBasedReturn, Money> xOrY_onlyXmatters =
        terminateBasedOnXorY(
            (ret1, ret2) -> ret1.almostEquals(ret2, 1e-8),
            (amt1, amt2) -> false);
    BinarySearchTerminationPredicate<OnesBasedReturn, Money> xAndY_onlyXmatters=
        terminateBasedOnXandY(
            (ret1, ret2) -> ret1.almostEquals(ret2, 1e-8),
            (amt1, amt2) -> true);

    BinarySearchTerminationPredicate<OnesBasedReturn, Money> onlyY_onlyYmatters =
        onlyTerminateBasedOnY( (amt1, amt2) -> amt1.almostEquals(amt2, 1e-4));
    BinarySearchTerminationPredicate<OnesBasedReturn, Money> xOrY_onlyYmatters =
        terminateBasedOnXorY(
            (ret1, ret2) -> false,
            (amt1, amt2) -> amt1.almostEquals(amt2, 1e-4));
    BinarySearchTerminationPredicate<OnesBasedReturn, Money> xAndY_onlyYmatters=
        terminateBasedOnXandY(
            (ret1, ret2) -> true,
            (amt1, amt2) -> amt1.almostEquals(amt2, 1e-4));

    UnaryOperator<BinarySearchResultBuilder<OnesBasedReturn, Money>> resultTerminatedByX = v -> v
        .setLowerBoundX(onesBasedReturn(doubleExplained(0.8, 1_600 / 2_000.0)))
        .setUpperBoundX(onesBasedReturn(0.8))
        .setLowerBoundY(money(1_600))
        .setUpperBoundY(money(1_600))
        .setNumIterationsUsed(31);

    UnaryOperator<BinarySearchResultBuilder<OnesBasedReturn, Money>> resultTerminatedByY = v -> v
        // ideal is 0.8, but the binary search doesn't iterate enough times in this test (intentionally)
        .setLowerBoundX(onesBasedReturn(0.7999999716877939))
        .setUpperBoundX(onesBasedReturn(0.8000000085681679))
        .setLowerBoundY(money(1599.9999433755878))
        .setUpperBoundY(money(1600.0000171363358))
        .setNumIterationsUsed(29);
    assertTrue(doubleExplained(0.000073761, 1600.0000171363358 - 1599.9999433755878) < 1e-4);

    BiConsumer<
        BinarySearchTerminationPredicate<OnesBasedReturn, Money>,
        UnaryOperator<BinarySearchResultBuilder<OnesBasedReturn, Money>>> asserter =
        (terminationPredicate, expectedResult) ->
            assertResult(
                makeBinarySearchParametersBuilder()
                    .setLowerBoundX(onesBasedReturn(0.1))
                    .setUpperBoundX(onesBasedReturn(10.0))
                    // terminating based on Y here
                    .setTerminationPredicate(terminationPredicate)
                    .build(), // using lower epsilon for termination
                expectedResult);

    asserter.accept(onlyX_onlyXmatters, resultTerminatedByX);
    asserter.accept(xOrY_onlyXmatters,  resultTerminatedByX);
    asserter.accept(xAndY_onlyXmatters, resultTerminatedByX);

    asserter.accept(onlyY_onlyYmatters, resultTerminatedByY);
    asserter.accept(xOrY_onlyYmatters,  resultTerminatedByY);
    asserter.accept(xAndY_onlyYmatters, resultTerminatedByY);
  }

  @Test
  public void terminatingBasedOnXorY() {
    assertResult(
        makeBinarySearchParametersBuilder()
            .setLowerBoundX(onesBasedReturn(0.1))
            .setUpperBoundX(onesBasedReturn(10.0))
            // terminating based on X or Y here, but X will never be true
            .setTerminationPredicate(onlyTerminateBasedOnY( (amt1, amt2) -> amt1.almostEquals(amt2, 1e-4)))
            .build(), // using lower epsilon for termination
        v -> v
            // ideal is 0.8, but the binary search doesn't iterate enough times in this test (intentionally)
            .setLowerBoundX(onesBasedReturn(0.7999999716877939))
            .setUpperBoundX(onesBasedReturn(0.8000000085681679))
            .setLowerBoundY(money(1599.9999433755878))
            .setUpperBoundY(money(1600.0000171363358))
            .setNumIterationsUsed(29)); // the binary search happens to take 29 iterations here.
    assertTrue(doubleExplained(0.000073761, 1600.0000171363358 - 1599.9999433755878) < 1e-4);
  }

  @Test
  public void neverTerminates_tooFewIterations_throws() {
    assertIllegalArgumentException( () ->
        makeTestObject().performBinarySearch(
            // choosing 17 iterations; if we have more than 55 iterations, then the binary search will get close enough
            // that it will terminate NOT because the termination predicate has been triggered,
            // but because the comparator of Y will tell us that the Y values are so similar that they are equal.
            makeBinarySearchParametersBuilder(17)
                .setLowerBoundX(onesBasedReturn(0.1))
                .setUpperBoundX(onesBasedReturn(10.0))
                .setTerminationPredicate(onlyTerminateBasedOnX( (ret1, ret2) -> false)) // never terminate
                .build()));
  }

  @Test
  public void upperAndLowerAreOnTarget_probablyUnintended_throws() {
    OnesBasedReturn expectedReturn = onesBasedReturn(doubleExplained(0.8, 1_600 / 2_000.0));
    // Here, the lower and upper bounds are set far apart, so we iterate quite a bit to get to the solution.
    assertResult(
        makeBinarySearchParametersBuilder()
            .setLowerBoundX(onesBasedReturn(0.1))
            .setUpperBoundX(onesBasedReturn(10.0))
            .setTerminationPredicate(DEFAULT_TERMINATION_PREDICATE)
            .build(),
        v -> v
            .setLowerBoundX(expectedReturn)
            .setUpperBoundX(expectedReturn)
            .setLowerBoundY(money(1_600))
            .setUpperBoundY(money(1_600))
            .setNumIterationsUsed(31)); // the binary search happens to take 31 iterations here.

    // Here, the lower and upper bound are already at the solution, so no need to iterate (conceptually),
    // but we flag it as an exception, because it is probably unintended and indicates a bug.
    // Note that we don't even need to bother running the binary search here; the BinarySearchParameters object itself
    // will throw upon construction.
    assertIllegalArgumentException( () -> makeBinarySearchParametersBuilder()
        .setLowerBoundX(expectedReturn)
        .setUpperBoundX(expectedReturn)
        .setTerminationPredicate(DEFAULT_TERMINATION_PREDICATE)
        .build());
  }

  @Test
  public void invalidMidpointGenerator_returnsValueOutsideOpenLowerUpperInterval_throws() {
    for (BinaryOperator<OnesBasedReturn> badMidpointGenerator :
        ImmutableList.<BinaryOperator<OnesBasedReturn>>of(
            (l, u) -> onesBasedReturn(l.doubleValue() - 0.01),
            (l, u) -> l,
            (l, u) -> u,
            (l, u) -> onesBasedReturn(u.doubleValue() + 0.01))) {
      assertIllegalArgumentException( () -> makeTestObject().performBinarySearch(
          BinarySearchParametersBuilder.<OnesBasedReturn, Money>binarySearchParametersBuilder()
              .setLowerBoundX(onesBasedReturn(0.1))
              .setUpperBoundX(onesBasedReturn(10.0))
              .setComparatorForX(naturalOrder())
              .setComparatorForY(naturalOrder())
              .setTargetY(money(1_600))
              .setEvaluatorOfX(ret -> money(2_000).multiply(ret))
              .setTerminationPredicate(onlyTerminateBasedOnX( (ret1, ret2) ->
                  ret1.asBigDecimal().subtract(ret2.asBigDecimal()).abs().compareTo(BigDecimal.valueOf(1e-8)) < 0))
              .setMidpointGenerator(badMidpointGenerator) // this is key; the rest is the same
              .setMaxIterations(100)
              .build()));
    }
  }

  @Test
  public void inputEvaluatorIsNotMonotonic_throws() {
    // These are specifically created to not be monotonic;
    // f(x) for any x (other than the lower or upper bound) is not between the f(lower) & f(upper).
    Function<OnesBasedReturn, Money> badNonMonotonicInputEvaluator1 = ret ->
        ret.equals(onesBasedReturn(0.1))  ? money(1_000) :
        ret.equals(onesBasedReturn(10.0)) ? money(2_000) :
                                            money(  999);
    Function<OnesBasedReturn, Money> badNonMonotonicInputEvaluator2 = ret ->
        ret.equals(onesBasedReturn(0.1))  ? money(1_000) :
        ret.equals(onesBasedReturn(10.0)) ? money(2_000) :
                                            money(2_001);
    Function<OnesBasedReturn, Money> badNonMonotonicInputEvaluator3 = ret ->
        ret.equals(onesBasedReturn(0.1))  ? money(2_000) :
        ret.equals(onesBasedReturn(10.0)) ? money(1_000) :
                                            money(  999);
    Function<OnesBasedReturn, Money> badNonMonotonicInputEvaluator4 = ret ->
        ret.equals(onesBasedReturn(0.1))  ? money(2_000) :
        ret.equals(onesBasedReturn(10.0)) ? money(1_000) :
                                            money(2_001);
    for (Function<OnesBasedReturn, Money> badNonMonotonicInputEvaluator : rbSetOf(
        badNonMonotonicInputEvaluator1,
        badNonMonotonicInputEvaluator2,
        badNonMonotonicInputEvaluator3,
        badNonMonotonicInputEvaluator4)) {
      // This actually throws within BinarySearchParametersBuilder#build, but we want this test
      // because it's possible there can be failing preconditions inside BinarySearch#performBinarySearch
      assertIllegalArgumentException( () -> makeTestObject().performBinarySearch(
          BinarySearchParametersBuilder.<OnesBasedReturn, Money>binarySearchParametersBuilder()
              .setLowerBoundX(onesBasedReturn(0.1))
              .setUpperBoundX(onesBasedReturn(10.0))
              .setComparatorForX(naturalOrder())
              .setComparatorForY(naturalOrder())
              .setTargetY(money(1_600))
              .setEvaluatorOfX(badNonMonotonicInputEvaluator) // this is key
              .setTerminationPredicate(onlyTerminateBasedOnX( (ret1, ret2) ->
                  ret1.asBigDecimal().subtract(ret2.asBigDecimal()).abs().compareTo(BigDecimal.valueOf(1e-8)) < 0))
              .setMidpointGenerator((lower, upper) -> onesBasedReturn(0.5 * (
                  lower.doubleValue() + upper.doubleValue())))
              .setMaxIterations(100)
              .build()));
    }
  }

  @Test
  public void lowerBoundInvalid_throws() {
    assertIllegalArgumentException( () -> makeTestObject().performBinarySearch(
        makeBinarySearchParametersBuilder()
            .setLowerBoundX(onesBasedReturn(0.8 + 0.01))
            .setUpperBoundX(onesBasedReturn(10.0))
            .setTerminationPredicate(DEFAULT_TERMINATION_PREDICATE)
            .build()));
  }

  @Test
  public void upperBoundInvalid_throws() {
    assertIllegalArgumentException( () -> makeTestObject().performBinarySearch(
        makeBinarySearchParametersBuilder()
            .setLowerBoundX(onesBasedReturn(0.1))
            .setUpperBoundX(onesBasedReturn(0.8 - 0.01))
            .setTerminationPredicate(DEFAULT_TERMINATION_PREDICATE)
            .build()));
  }

  private BinarySearchParametersBuilder<OnesBasedReturn, Money> makeBinarySearchParametersBuilder() {
    return makeBinarySearchParametersBuilder(100);
  }

  private BinarySearchParametersBuilder<OnesBasedReturn, Money> makeBinarySearchParametersBuilder(int maxIterations) {
    return BinarySearchParametersBuilder.<OnesBasedReturn, Money>binarySearchParametersBuilder()
        .setComparatorForX(naturalOrder())
        .setComparatorForY(naturalOrder())
        .setTargetY(TARGET_Y)
        .setEvaluatorOfX(ret -> money(2_000).multiply(ret))
        .setMidpointGenerator((lower, upper) -> onesBasedReturn(0.5 * (
            lower.doubleValue() + upper.doubleValue())))
        .setMaxIterations(maxIterations);
  }

  private void assertResult(
      BinarySearchParameters<OnesBasedReturn, Money> binarySearchParameters,
      // the UnaryOperator is a bit weird, but it keeps the calling code shorter.
      UnaryOperator<BinarySearchResultBuilder<OnesBasedReturn, Money>> expectedResult) {
    assertThat(
        makeTestObject().performBinarySearch(binarySearchParameters),
        binarySearchResultMatcher(
            expectedResult
                .apply(binarySearchResultBuilder())
                .setTargetY(TARGET_Y)
                .setComparatorForY(Money::compareTo)
                .build(),
            f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8),
            f -> preciseValueMatcher(f, epsilon(1e-4)))); // We need a less tight epsilon on the Y for the test to work
  }

  @Override
  protected BinarySearch makeTestObject() {
    return new BinarySearch();
  }

}