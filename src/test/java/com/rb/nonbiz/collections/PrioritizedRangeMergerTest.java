package com.rb.nonbiz.collections;

import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.RBRangeMatchers.doubleRangeMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;

public class PrioritizedRangeMergerTest extends RBTest<PrioritizedRangeMerger> {

  @Test
  public void testWithOverlap() {
    checkResult(Range.all(),        Range.all(),       Range.all());
    checkResult(Range.all(),        Range.atLeast(2.0), Range.atLeast(2.0));
    checkResult(Range.atLeast(2.0),  Range.all(),        Range.atMost(7.0));
    checkResult(Range.all(),        Range.atMost(7.0), Range.atMost(7.0));
    checkResult(Range.atMost(7.0),  Range.all(),       Range.atMost(7.0));
    checkResult(Range.atLeast(2.0), Range.atMost(7.0), Range.closed(2.0, 7.0));
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
