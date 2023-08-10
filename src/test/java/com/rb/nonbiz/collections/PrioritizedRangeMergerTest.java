package com.rb.nonbiz.collections;

import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.RBRangeMatchers.doubleRangeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;

public class PrioritizedRangeMergerTest extends RBTest<PrioritizedRangeMerger> {

  @Test
  public void testWithOverlap() {
    checkResult(Range.all(),            Range.all(),            Range.all());
    checkResult(Range.all(),            Range.atLeast(2.0),     Range.atLeast(2.0));
    checkResult(Range.atLeast(2.0),     Range.all(),            Range.atLeast(2.0));
    checkResult(Range.all(),            Range.atMost( 7.0),     Range.atMost( 7.0));
    checkResult(Range.atMost(7.0),      Range.all(),            Range.atMost( 7.0));
    checkResult(Range.atLeast(2.0),     Range.atMost(7.0),      Range.closed(2.0, 7.0));

    // we don't expect to use open bounds, but they also work:
    checkResult(Range.all(),            Range.greaterThan(2.0), Range.greaterThan(2.0));
    checkResult(Range.greaterThan(2.0), Range.all(),            Range.greaterThan(2.0));
    checkResult(Range.all(),            Range.lessThan( 7.0),   Range.lessThan( 7.0));
    checkResult(Range.lessThan(7.0),    Range.all(),            Range.lessThan( 7.0));
    checkResult(Range.greaterThan(2.0), Range.lessThan(7.0),    Range.open(2.0, 7.0));
  }

  @Test
  public void test_noOverlap_openHighPriorityRange_throws() {
    assertIllegalArgumentException( () -> makeTestObject().merge(Range.lessThan(2.0),     Range.closed(3.0, 5.0)));
    assertIllegalArgumentException( () -> makeTestObject().merge(Range.greaterThan(10.0), Range.closed(3.0, 5.0)));
  }

  @Test
  public void test_noOverlap_returnsSingletonUpperBound_fromHighPriorityRange() {
    // the expected use case: the high-priority restriction is to sell everything
    checkResult(Range.singleton(0.0), Range.atLeast(    5.0), Range.singleton(0.0));
    checkResult(Range.singleton(0.0), Range.greaterThan(5.0), Range.singleton(0.0));
    checkResult(Range.singleton(0.0), Range.closed(5.0, 7.0), Range.singleton(0.0));

    checkResult(Range.atMost(     2.0), Range.atLeast(    5.0), Range.singleton(2.0));
    checkResult(Range.atMost(     2.0), Range.greaterThan(5.0), Range.singleton(2.0));
    checkResult(Range.atMost(     2.0), Range.closed(5.0, 7.0), Range.singleton(2.0));

    checkResult(Range.closed(0.5, 2.0), Range.atLeast(    5.0), Range.singleton(2.0));
    checkResult(Range.closed(0.5, 2.0), Range.greaterThan(5.0), Range.singleton(2.0));
    checkResult(Range.closed(0.5, 2.0), Range.closed(5.0, 7.0), Range.singleton(2.0));

    // we don't usually use open endpoints, but "openClosed" works since the upper bound is closed
    checkResult(Range.openClosed(0.5, 2.0), Range.atLeast(    5.0), Range.singleton(2.0));
    checkResult(Range.openClosed(0.5, 2.0), Range.greaterThan(5.0), Range.singleton(2.0));
    checkResult(Range.openClosed(0.5, 2.0), Range.closed(5.0, 7.0), Range.singleton(2.0));
  }

  @Test
  public void test_noOverlap_returnsSingletonLowerBound_fromHighPriorityRange() {
    checkResult(Range.atLeast(9.0), Range.atMost(     7.0), Range.singleton(9.0));
    checkResult(Range.atLeast(9.0), Range.lessThan(   7.0), Range.singleton(9.0));
    checkResult(Range.atLeast(9.0), Range.closed(5.0, 7.0), Range.singleton(9.0));

    checkResult(Range.closed(9.0, 11.0), Range.atMost(     7.0), Range.singleton(9.0));
    checkResult(Range.closed(9.0, 11.0), Range.lessThan(   7.0), Range.singleton(9.0));
    checkResult(Range.closed(9.0, 11.0), Range.closed(3.0, 7.0), Range.singleton(9.0));

    // we don't usually use open endpoints, but "closedOpen" works since the lower bound is closed
    checkResult(Range.closedOpen(9.0, 11.0), Range.atMost(     7.0), Range.singleton(9.0));
    checkResult(Range.closedOpen(9.0, 11.0), Range.lessThan(   7.0), Range.singleton(9.0));
    checkResult(Range.closedOpen(9.0, 11.0), Range.closed(3.0, 7.0), Range.singleton(9.0));
  }

  private void checkResult(
      Range<Double> highPriorityRange,
      Range<Double> lowPriorityRange,
      Range<Double> expectedMergedRange) {
    assertThat(
        makeTestObject().merge(highPriorityRange, lowPriorityRange),
        doubleRangeMatcher(expectedMergedRange, DEFAULT_EPSILON_1e_8));
  }

  @Override
  protected PrioritizedRangeMerger makeTestObject() {
    return new PrioritizedRangeMerger();
  }

}
