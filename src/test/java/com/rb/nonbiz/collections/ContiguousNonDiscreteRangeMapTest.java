package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBValueMatchers;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.ContiguousNonDiscreteRangeMap.contiguousNonDiscreteRangeMapWithEnd;
import static com.rb.nonbiz.collections.ContiguousNonDiscreteRangeMap.contiguousNonDiscreteRangeMapWithNoEnd;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rangeMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_DOUBLE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This concrete test class is not generic, but the publicly exposed static matcher is.
 */
public class ContiguousNonDiscreteRangeMapTest extends RBTestMatcher<ContiguousNonDiscreteRangeMap<Double, String>> {

  @Test
  public void hasNoRangesOrItems_throws() {
    assertIllegalArgumentException( () ->
        // The specific classes don't matter here, but we need something, otherwise there's a compiler error.
        ContiguousNonDiscreteRangeMap.<Double, String>
            contiguousNonDiscreteRangeMapWithNoEnd(emptyList(), emptyList()));
    assertIllegalArgumentException( () ->
        contiguousNonDiscreteRangeMapWithEnd(emptyList(), emptyList(), DUMMY_DOUBLE));
  }

  @Test
  public void numItemsDoesNotMatchNumRanges_throws() {
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithNoEnd(
        ImmutableList.of(1.1, 2.2),
        singletonList("a")));
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithNoEnd(
        ImmutableList.of(1.1, 2.2),
        ImmutableList.of("a", "b", "c")));
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(1.1, 2.2),
        singletonList("a"),
        1.1));
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(1.1, 2.2),
        ImmutableList.of("a", "b", "c"),
        1.1));
  }

  @Test
  public void rangeWithNoEnd_comparableKeysMustBeIncreasing_otherwiseThrows() {
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithNoEnd(
        ImmutableList.of(1.1, 1.1),
        ImmutableList.of("a", "b")));
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithNoEnd(
        ImmutableList.of(2.2, 1.1),
        ImmutableList.of("a", "b")));
  }

  @Test
  public void rangeWithEnd_comparableKeysMustBeIncreasing_otherwiseThrows() {
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(1.1, 1.1),
        ImmutableList.of("a", "b"),
        3.3));
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(2.2, 1.1),
        ImmutableList.of("a", "b"),
        3.3));
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(1.1, 3.3),
        ImmutableList.of("a", "b"),
        2.2));
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(1.1, 2.2),
        ImmutableList.of("a", "b"),
        2.2 - 1e-8));
    assertIllegalArgumentException( () -> contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(1.1, 2.2),
        ImmutableList.of("a", "b"),
        2.2));
    ContiguousNonDiscreteRangeMap<Double, String> doesNotThrow = contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(1.1, 2.2),
        ImmutableList.of("a", "b"),
        2.2 + 1e-8);
  }

  @Test
  public void happyPath_keysAreDoubles_contiguousNonDiscreteRangeMapWithNoEnd_returnsValuesInRange() {
    ContiguousNonDiscreteRangeMap<Double, String> map = contiguousNonDiscreteRangeMapWithNoEnd(
        ImmutableList.of(1.5, 2.5),
        ImmutableList.of("[1.5, 2.5)", "[2.5, +inf)"));
    assertOptionalEmpty(map.getOptional(-999.0));
    assertOptionalEmpty(map.getOptional(1.4999));
    assertEquals("[1.5, 2.5)",  map.getOrThrow(1.5));
    assertEquals("[1.5, 2.5)",  map.getOrThrow(1.5001));
    assertEquals("[1.5, 2.5)",  map.getOrThrow(2.4999));
    assertEquals("[2.5, +inf)", map.getOrThrow(2.5));
    assertEquals("[2.5, +inf)", map.getOrThrow(2.5001));
    assertEquals("[2.5, +inf)", map.getOrThrow(999.0));
  }

  @Test
  public void happyPath_keysAreDoubles_contiguousNonDiscreteRangeMapWithEnd_returnsValuesInRange() {
    ContiguousNonDiscreteRangeMap<Double, String> map = contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(1.5, 2.5),
        ImmutableList.of("[1.5, 2.5)", "[2.5, 3.5)"),
        3.5);
    assertOptionalEmpty(map.getOptional(-999.0));
    assertOptionalEmpty(map.getOptional(1.4999));
    assertEquals("[1.5, 2.5)", map.getOrThrow(1.5));
    assertEquals("[1.5, 2.5)", map.getOrThrow(1.5001));
    assertEquals("[1.5, 2.5)", map.getOrThrow(2.4999));
    assertEquals("[2.5, 3.5)", map.getOrThrow(2.5));
    assertEquals("[2.5, 3.5)", map.getOrThrow(2.5001));
    assertEquals("[2.5, 3.5)", map.getOrThrow(3.4999));
    assertOptionalEmpty(map.getOptional(3.5));
    assertOptionalEmpty(map.getOptional(3.5001));
    assertOptionalEmpty(map.getOptional(999.0));
  }

  @Test
  public void test_hasEnd() {
    assertFalse(
        contiguousNonDiscreteRangeMapWithNoEnd(
            singletonList(0.0),
            singletonList("[0,inf)"))
            .hasEnd());
    assertTrue(
        contiguousNonDiscreteRangeMapWithEnd(
            singletonList(0.0),
            singletonList("[0,7)"),
            7.0)
            .hasEnd());
  }

  @Test
  public void test_getFirstInvalidPointAfterRange() {
    assertOptionalEmpty(
        contiguousNonDiscreteRangeMapWithNoEnd(
            singletonList(0.0),
            singletonList("[0,inf)"))
            .getFirstInvalidPointAfterRange());
    assertOptionalNonEmpty(
        contiguousNonDiscreteRangeMapWithEnd(
            singletonList(0.0),
            singletonList("[0,7)"),
            7.0)
            .getFirstInvalidPointAfterRange(),
        doubleAlmostEqualsMatcher(7.0, 1e-8));
  }

  @Override
  public ContiguousNonDiscreteRangeMap<Double, String> makeTrivialObject() {
    return contiguousNonDiscreteRangeMapWithNoEnd(
        singletonList(0.0),
        singletonList("[0,inf)"));
  }

  @Override
  public ContiguousNonDiscreteRangeMap<Double, String> makeNontrivialObject() {
    return contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(-1.1, 2.2, 5.5),
        ImmutableList.of("[-1.1, 2.2)", "[2.2, 5.5)", "[5.5, 6.6)"),
        6.6);
  }

  @Override
  public ContiguousNonDiscreteRangeMap<Double, String> makeMatchingNontrivialObject() {
    /**
     * There's no general concept of 'almost equals' for a range map, so matching will rely on equality
     * of the ranges in the underlying RangeMap keys.
     */
    return contiguousNonDiscreteRangeMapWithEnd(
        ImmutableList.of(-1.1, 2.2, 5.5),
        ImmutableList.of("[-1.1, 2.2)", "[2.2, 5.5)", "[5.5, 6.6)"),
        6.6);
  }

  @Override
  protected boolean willMatch(ContiguousNonDiscreteRangeMap<Double, String> expected, 
                              ContiguousNonDiscreteRangeMap<Double, String> actual) {
    return contiguousNonDiscreteRangeMapMatcher(expected, v -> typeSafeEqualTo(v)).matches(actual);
  }

  /**
   * There's no general concept of 'almost equals' for a range map, so matching will rely on equality
   * of the ranges in the underlying RangeMap keys.
   */
  public static <K extends Comparable<? super K>, V> TypeSafeMatcher<ContiguousNonDiscreteRangeMap<K, V>> contiguousNonDiscreteRangeMapMatcher(
      ContiguousNonDiscreteRangeMap<K, V> expected, MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected, actual ->
        rangeMapMatcher(expected.getRawRangeMap(), valueMatcherGenerator).matches(actual.getRawRangeMap()));
  }

}
