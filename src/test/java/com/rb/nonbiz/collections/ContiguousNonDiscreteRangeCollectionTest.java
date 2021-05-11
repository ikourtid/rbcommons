package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.collections.ContiguousNonDiscreteRangeCollection.LastPointInRangeTreatment;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.ContiguousNonDiscreteRangeCollection.contiguousNonDiscreteRangeCollectionWithEnd;
import static com.rb.nonbiz.collections.ContiguousNonDiscreteRangeCollection.contiguousNonDiscreteRangeCollectionWithNoEnd;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.doubleRangeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rangeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This concrete test class is not generic, but the publicly exposed static matcher is.
 */
public class ContiguousNonDiscreteRangeCollectionTest extends RBTestMatcher<ContiguousNonDiscreteRangeCollection<Double>> {

  @Test
  public void hasNoRangesOrItems_throws() {
    assertIllegalArgumentException( () ->
        contiguousNonDiscreteRangeCollectionWithNoEnd(emptyList()));
    for (LastPointInRangeTreatment lastPointInRangeTreatment : LastPointInRangeTreatment.values()) {
      assertIllegalArgumentException( () ->
          contiguousNonDiscreteRangeCollectionWithEnd(emptyList(), lastPointInRangeTreatment));
      assertIllegalArgumentException( () ->
          contiguousNonDiscreteRangeCollectionWithEnd(ImmutableList.of(1.1), lastPointInRangeTreatment));
    }
  }

  @Test
  public void handlesLastPointCorrectly() {
    assertThat(
        contiguousNonDiscreteRangeCollectionWithEnd(ImmutableList.of(0.0, 1.1), LastPointInRangeTreatment.INCLUDE)
            .getRawRangeCollection(),
        orderedListMatcher(
            ImmutableList.of(Range.closed(0.0, 1.1)),
            r -> doubleRangeMatcher(r, 1e-8)));
    assertThat(
        contiguousNonDiscreteRangeCollectionWithEnd(ImmutableList.of(0.0, 1.1), LastPointInRangeTreatment.EXCLUDE)
            .getRawRangeCollection(),
        orderedListMatcher(
            ImmutableList.of(Range.closedOpen(0.0, 1.1)),
            r -> doubleRangeMatcher(r, 1e-8)));
    assertThat(
        contiguousNonDiscreteRangeCollectionWithEnd(ImmutableList.of(0.0, 1.1, 2.2), LastPointInRangeTreatment.INCLUDE)
            .getRawRangeCollection(),
        orderedListMatcher(
            ImmutableList.of(Range.closedOpen(0.0, 1.1), Range.closed(1.1, 2.2)),
            r -> doubleRangeMatcher(r, 1e-8)));
    assertThat(
        contiguousNonDiscreteRangeCollectionWithEnd(ImmutableList.of(0.0, 1.1, 2.2), LastPointInRangeTreatment.EXCLUDE)
            .getRawRangeCollection(),
        orderedListMatcher(
            ImmutableList.of(Range.closedOpen(0.0, 1.1), Range.closedOpen(1.1, 2.2)),
            r -> doubleRangeMatcher(r, 1e-8)));
  }

  @Test
  public void rangeWithNoEnd_comparableKeysMustBeIncreasing_otherwiseThrows() {
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeCollectionWithNoEnd(
        ImmutableList.of(0.0, 0.0)));
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeCollectionWithNoEnd(
        ImmutableList.of(1.1, 0.0)));
  }

  @Test
  public void rangeWithEnd_comparableKeysMustBeIncreasing_otherwiseThrows() {
    for (LastPointInRangeTreatment lastPointInRangeTreatment : LastPointInRangeTreatment.values()) {
      assertIllegalArgumentException( () -> contiguousNonDiscreteRangeCollectionWithEnd(
          ImmutableList.of(0.0, 0.0, 2.2), lastPointInRangeTreatment));
      assertIllegalArgumentException( () -> contiguousNonDiscreteRangeCollectionWithEnd(
          ImmutableList.of(1.1, 0.0, 2.2), lastPointInRangeTreatment));
      assertIllegalArgumentException( () -> contiguousNonDiscreteRangeCollectionWithEnd(
          ImmutableList.of(0.0, 2.2, 1.1), lastPointInRangeTreatment));
      ContiguousNonDiscreteRangeCollection<Double> doesNotThrow = contiguousNonDiscreteRangeCollectionWithEnd(
          ImmutableList.of(0.0, 1.1, 2.2), lastPointInRangeTreatment);
    }
  }

  @Override
  public ContiguousNonDiscreteRangeCollection<Double> makeTrivialObject() {
    return contiguousNonDiscreteRangeCollectionWithNoEnd(
        singletonList(0.0));
  }

  @Override
  public ContiguousNonDiscreteRangeCollection<Double> makeNontrivialObject() {
    return contiguousNonDiscreteRangeCollectionWithEnd(
        ImmutableList.of(0.0, 1.1, 2.2), LastPointInRangeTreatment.INCLUDE);
  }

  @Override
  public ContiguousNonDiscreteRangeCollection<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return contiguousNonDiscreteRangeCollectionWithEnd(
        ImmutableList.of(0.0 + e, 1.1 + e, 2.2 + e), LastPointInRangeTreatment.INCLUDE);
  }

  @Override
  protected boolean willMatch(ContiguousNonDiscreteRangeCollection<Double> expected,
                              ContiguousNonDiscreteRangeCollection<Double> actual) {
    return contiguousNonDiscreteRangeCollectionMatcher(expected, v -> doubleAlmostEqualsMatcher(v, 1e-8)).matches(actual);
  }

  public static <K extends Comparable> TypeSafeMatcher<ContiguousNonDiscreteRangeCollection<K>> contiguousNonDiscreteRangeCollectionMatcher(
      ContiguousNonDiscreteRangeCollection<K> expected, MatcherGenerator<K> valueMatcherGenerator) {
    return makeMatcher(expected,
        matchList(v -> v.getRawRangeCollection(), f -> rangeMatcher(f, valueMatcherGenerator)));
  }

}
