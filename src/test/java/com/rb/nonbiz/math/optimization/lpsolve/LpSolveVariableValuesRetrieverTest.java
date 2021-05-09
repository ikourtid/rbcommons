package com.rb.nonbiz.math.optimization.lpsolve;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.math.optimization.general.ScalingInstructionsForImprovedLpAccuracy.noScalingForImprovedLpAccuracy;
import static com.rb.nonbiz.math.optimization.general.ScalingInstructionsForImprovedLpAccuracy.scalingInstructionsForImprovedLpAccuracy;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArrayMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

public class LpSolveVariableValuesRetrieverTest extends RBTest<LpSolveVariableValuesRetriever> {

  @Test
  public void collarsValuesIfNeeded() {
    double e = 1e-9; // epsilon

    assertSingleResult(1.234  , Range.all(), 1.234);
    assertSingleResult(7.0 - e, Range.singleton(7.0), 7.0);
    assertSingleResult(7.0 + e, Range.singleton(7.0), 7.0);

    assertTooFarFromRange(6.9,  Range.singleton(7.0));  // value must be within 1e-8 of the closest range boundary
    assertTooFarFromRange(7.1,  Range.singleton(7.0));

    assertSingleResult(7.0 - e, Range.atLeast(7.0),     7.0);
    assertSingleResult(7.0    , Range.atLeast(7.0),     7.0);
    assertSingleResult(7.1    , Range.atLeast(7.0),     7.1);
    assertSingleResult(7.0 - e, Range.greaterThan(7.0), 7.0);
    assertSingleResult(7.0    , Range.greaterThan(7.0), 7.0);
    assertSingleResult(7.1    , Range.greaterThan(7.0), 7.1);

    assertSingleResult(6.9    , Range.atMost(7.0),   6.9);
    assertSingleResult(7.0    , Range.atMost(7.0),   7.0);
    assertSingleResult(7.0 + e, Range.atMost(7.0),   7.0);
    assertSingleResult(6.9,     Range.lessThan(7.0), 6.9);
    assertSingleResult(7.0    , Range.lessThan(7.0), 7.0);
    assertSingleResult(7.0 + e, Range.lessThan(7.0), 7.0);

    assertTooFarFromRange(6.9, Range.atLeast(7.0));
    assertTooFarFromRange(7.1, Range.atMost( 7.0));

    // Note that, technically, if a range is open on one end, the value in that endpoint is not valid per the range
    // semantics. However, the optimizer does not make such distinctions, because we're talking about using
    // doubles anyway. In fact, the optimizer sometimes gives us values that are slightly OUTSIDE the ranges
    // specified in the LP variable constraints - which is the reason why we need this class in the first place!
    rbSetOf(
        Range.closed(    7.0, 8.0),
        Range.closedOpen(7.0, 8.0),
        Range.openClosed(7.0, 8.0),
        Range.open(      7.0, 8.0))
        .forEach(range -> {
          assertSingleResult(7.0 - e, range, 7.0);
          assertSingleResult(7.0    , range, 7.0);
          assertSingleResult(7.1234 , range, 7.1234);
          assertSingleResult(7.9    , range, 7.9);
          assertSingleResult(8.0    , range, 8.0);
          assertSingleResult(8.0 + e, range, 8.0);

          assertTooFarFromRange(6.9, range);
          assertTooFarFromRange(8.1, range);
        });
  }

  private void assertSingleResult(double valueFromLp, Range<Double> range, double valueAfterCollaring) {
    rbSetOf(0.01, 0.1, 1.0, 10.0, 100.0, 1000.0).forEach(multiplier ->
        assertThat(
            makeTestObject().retrieveAndCollarValues(
                new double[] { valueFromLp },
                singletonList(range),
                noScalingForImprovedLpAccuracy()),
            doubleArrayMatcher(new double[] { valueAfterCollaring }, 1e-9)));
  }

  private void assertTooFarFromRange(double valueFromLp, Range<Double> range) {
    rbSetOf(0.01, 0.1, 1.0, 10.0, 100.0, 1000.0).forEach(multiplier ->
        assertIllegalArgumentException( () ->
            makeTestObject().retrieveAndCollarValues(
                new double[] { valueFromLp * multiplier },
                singletonList(range),
                scalingInstructionsForImprovedLpAccuracy(positiveMultiplier(multiplier)))));
  }

  @Test
  public void unequalSizes_throws() {
    assertIllegalArgumentException( () -> makeTestObject().retrieveAndCollarValues(
        new double[] { DUMMY_DOUBLE }, ImmutableList.of(Range.all(), Range.all()), noScalingForImprovedLpAccuracy()));
    assertIllegalArgumentException( () -> makeTestObject().retrieveAndCollarValues(
        new double[] { DUMMY_DOUBLE, DUMMY_DOUBLE }, singletonList(Range.all()), noScalingForImprovedLpAccuracy()));

    double[] doesNotThrow;
    doesNotThrow = makeTestObject().retrieveAndCollarValues(
        new double[] { DUMMY_DOUBLE }, singletonList(Range.all()), noScalingForImprovedLpAccuracy());
    doesNotThrow = makeTestObject().retrieveAndCollarValues(
        new double[] { DUMMY_DOUBLE, DUMMY_DOUBLE }, ImmutableList.of(Range.all(), Range.all()), noScalingForImprovedLpAccuracy());
  }

  @Test
  public void bothEmpty_throws() {
    assertIllegalArgumentException( () -> makeTestObject().retrieveAndCollarValues(new double[] {}, emptyList(), noScalingForImprovedLpAccuracy()));
  }

  @Override
  protected LpSolveVariableValuesRetriever makeTestObject() {
    return new LpSolveVariableValuesRetriever();
  }

}
