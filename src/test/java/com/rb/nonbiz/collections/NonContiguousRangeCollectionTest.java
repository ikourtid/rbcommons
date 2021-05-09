package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static com.rb.nonbiz.collections.NonContiguousRangeCollection.nonContiguousRangeCollection;
import static com.rb.nonbiz.collections.NonContiguousRangeCollection.nonContiguousRangeCollectionWithEnd;
import static com.rb.nonbiz.collections.NonContiguousRangeCollection.nonContiguousRangeCollectionWithNoEnd;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rangeEqualityMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class NonContiguousRangeCollectionTest extends RBTestMatcher<NonContiguousRangeCollection<LocalDate>> {

  @Test
  public void cannotBeEmpty() {
    assertIllegalArgumentException( () -> nonContiguousRangeCollection(emptyList()));
  }

  @Test
  public void testSpecialConstructorsWithOrWithoutEnd() {
    List<Range<LocalDate>> withEnd = singletonList(
        Range.closed(LocalDate.of(1974, 4, 4), LocalDate.of(1975, 5, 5)));
    List<Range<LocalDate>> withNoEnd = singletonList(
        Range.atLeast(LocalDate.of(1974, 4, 4)));

    NonContiguousRangeCollection<LocalDate> doesNotThrow;

    doesNotThrow = nonContiguousRangeCollectionWithEnd(withEnd);
    assertIllegalArgumentException( () -> nonContiguousRangeCollectionWithEnd(withNoEnd));

    assertIllegalArgumentException( () -> nonContiguousRangeCollectionWithNoEnd(withEnd));
    doesNotThrow = nonContiguousRangeCollectionWithNoEnd(withNoEnd);
  }

  @Test
  public void nonLastRangeIsNotBounded_throws() {
    assertIllegalArgumentException( () -> nonContiguousRangeCollectionWithEnd(
        ImmutableList.of(
            Range.atLeast(LocalDate.of(1974, 4, 4)),
            Range.closed(LocalDate.of(1977, 7, 7), LocalDate.of(1978, 8, 8)))));
  }

  @Test
  public void rangesNotIncreasing_throws() {
    assertIllegalArgumentException( () -> nonContiguousRangeCollectionWithEnd(
        ImmutableList.of(
            Range.closed(LocalDate.of(1977, 7, 7), LocalDate.of(1978, 8, 8)),
            Range.closed(LocalDate.of(1974, 4, 4), LocalDate.of(1975, 5, 5)))));
  }

  @Test
  public void testRangeWithGap() {
    NonContiguousRangeCollection<LocalDate> doesNotThrow = nonContiguousRangeCollectionWithEnd(
        ImmutableList.of(
            Range.closed(LocalDate.of(1974, 4, 4), LocalDate.of(1975, 5, 5)),
            Range.closed(LocalDate.of(1977, 7, 7), LocalDate.of(1978, 8, 8))));
  }

  @Override
  public NonContiguousRangeCollection<LocalDate> makeTrivialObject() {
    return nonContiguousRangeCollection(
        singletonList(Range.singleton(LocalDate.of(1974, 4, 4))));
  }

  @Override
  public NonContiguousRangeCollection<LocalDate> makeNontrivialObject() {
    return nonContiguousRangeCollectionWithNoEnd(
        ImmutableList.of(
            Range.closed(LocalDate.of(1974, 4, 4), LocalDate.of(1975, 5, 5)),
            Range.closed(LocalDate.of(1976, 6, 6), LocalDate.of(1977, 7, 7)),
            Range.atLeast(LocalDate.of(1978, 8, 8))));
  }

  @Override
  public NonContiguousRangeCollection<LocalDate> makeMatchingNontrivialObject() {
    return nonContiguousRangeCollectionWithNoEnd(
        ImmutableList.of(
            Range.closed(LocalDate.of(1974, 4, 4), LocalDate.of(1975, 5, 5)),
            Range.closed(LocalDate.of(1976, 6, 6), LocalDate.of(1977, 7, 7)),
            Range.atLeast(LocalDate.of(1978, 8, 8))));
  }

  @Override
  protected boolean willMatch(NonContiguousRangeCollection<LocalDate> expected,
                              NonContiguousRangeCollection<LocalDate> actual) {
    return nonContiguousRangeCollectionMatcher(expected).matches(actual);
  }

  public static <K extends Comparable<? super K>> TypeSafeMatcher<NonContiguousRangeCollection<K>>
  nonContiguousRangeCollectionMatcher(NonContiguousRangeCollection<K> expected) {
    return makeMatcher(expected,
        matchList(v -> v.getRawRangeCollection(), f -> rangeEqualityMatcher(f)));
  }

}
