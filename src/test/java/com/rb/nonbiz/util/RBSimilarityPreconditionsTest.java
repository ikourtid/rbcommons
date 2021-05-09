package com.rb.nonbiz.util;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.collections.ClosedRange;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.ClosedRange.singletonClosedRange;
import static com.rb.nonbiz.collections.ClosedRangeTest.closedRangeEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.doubleClosedRangeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.util.RBSimilarityPreconditions.checkWithinLimitedRange;
import static com.rb.nonbiz.util.RBSimilarityPreconditions.checkWithinSeconds;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBSimilarityPreconditionsTest {

  @Test
  public void testCheckWithinLimitedRange_iterator_noArgs() {
    testCheckWithinLimitedRange_helper(list -> checkWithinLimitedRange(
        list.iterator(),
        range -> range.upperEndpoint() - range.lowerEndpoint() <= 10.0));
  }

  @Test
  public void testCheckWithinLimitedRange_iterator_withArgs() {
    testCheckWithinLimitedRange_helper(list -> checkWithinLimitedRange(
        list.iterator(),
        range -> range.upperEndpoint() - range.lowerEndpoint() <= 10.0,
        "test format"));
  }

  @Test
  public void testCheckWithinLimitedRange_iterable_noArgs() {
    testCheckWithinLimitedRange_helper(list -> checkWithinLimitedRange(
        list,
        range -> range.upperEndpoint() - range.lowerEndpoint() <= 10.0));
  }

  @Test
  public void testCheckWithinLimitedRange_iterable_withArgs() {
    testCheckWithinLimitedRange_helper(list -> checkWithinLimitedRange(
        list,
        range -> range.upperEndpoint() - range.lowerEndpoint() <= 10.0,
        "test format"));
  }

  private void testCheckWithinLimitedRange_helper(Function<List<Double>, ClosedRange<Double>> maker) {
    assertIllegalArgumentException( () -> maker.apply(emptyList())); // no items to compute a range for
    assertThat(
        maker.apply(singletonList(7.7)),
        doubleClosedRangeMatcher(singletonClosedRange(7.7), 1e-8));
    assertThat(
        maker.apply(ImmutableList.of(7.7, 8.8)),
        doubleClosedRangeMatcher(closedRange(7.7, 8.8), 1e-8));
    assertThat(
        maker.apply(ImmutableList.of(8.8, 7.7)),
        doubleClosedRangeMatcher(closedRange(7.7, 8.8), 1e-8));
    assertThat(
        maker.apply(ImmutableList.of(9.9, 7.7, 8.8)),
        doubleClosedRangeMatcher(closedRange(7.7, 9.9), 1e-8));

    ClosedRange<Double> doesNotThrow;
    doesNotThrow =
        maker.apply(ImmutableList.of(7.7, doubleExplained(17.6, 7.7 + 10 - 0.1)));
    doesNotThrow =
        maker.apply(ImmutableList.of(7.7, doubleExplained(17.7, 7.7 + 10)));
    assertIllegalArgumentException( () ->
        maker.apply(ImmutableList.of(7.7, doubleExplained(17.8, 7.7 + 10 + 0.1))));
  }

  @Test
  public void testCheckWithinSeconds_iterator() {
    testCheckWithinTenSeconds_helper(list -> checkWithinSeconds(list, 10));
  }

  @Test
  public void testCheckWithinSeconds_iterable() {
    testCheckWithinTenSeconds_helper(list -> checkWithinSeconds(list.iterator(), 10));
  }

  private void testCheckWithinTenSeconds_helper(Function<List<LocalDateTime>, ClosedRange<LocalDateTime>> maker) {
    assertIllegalArgumentException( () -> maker.apply(emptyList())); // no items to compute a range for
    LocalDateTime start = LocalDateTime.of(1974, 4, 4, 9, 30, 0);
    assertThat(
        maker.apply(singletonList(start)),
        closedRangeEqualityMatcher(closedRange(start, start)));
    assertThat(
        maker.apply(ImmutableList.of(start, start.plusSeconds(3))),
        closedRangeEqualityMatcher(closedRange(start, start.plusSeconds(3))));
    assertThat(
        maker.apply(ImmutableList.of(start.plusSeconds(3), start)),
        closedRangeEqualityMatcher(closedRange(start, start.plusSeconds(3))));
    assertThat(
        maker.apply(ImmutableList.of(start.plusSeconds(9), start, start.plusSeconds(5))),
        closedRangeEqualityMatcher(closedRange(start, start.plusSeconds(9))));

    ClosedRange<LocalDateTime> doesNotThrow;
    doesNotThrow =
        maker.apply(ImmutableList.of(start, start.plusSeconds(9)));
    doesNotThrow =
        maker.apply(ImmutableList.of(start, start.plusSeconds(10)));
    assertIllegalArgumentException( () ->
        maker.apply(ImmutableList.of(start, start.plusSeconds(11))));
  }

}
