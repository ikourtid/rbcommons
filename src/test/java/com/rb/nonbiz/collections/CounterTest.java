package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableSet;
import com.rb.nonbiz.collections.Counter.CounterBuilder;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.Counter.CounterBuilder.counterBuilder;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * The test class is not generic, but the publicly exposed static TypeSafeMatcher is.
 */
public class CounterTest extends RBTestMatcher<Counter<String>> {

  public static <T> Counter<T> emptyCounter() {
    return CounterBuilder.<T>counterBuilder().build();
  }

  // In tests, it's often convenient and more terse to use a map, instead of CounterBuilder.
  public static <T> Counter<T> counterFromMap(RBMap<T, Integer> countsAsMap) {
    CounterBuilder<T> counter = counterBuilder();
    countsAsMap.forEachEntry( (key, countForKey) -> counter.add(key, countForKey));
    return counter.build();
  }

  @Test
  public void happyPath() {
    Counter<String> counter = CounterBuilder.<String>counterBuilder().build();
    assertTrue(counter.isEmpty());

    assertEquals(0, counter.getCountOrZero("A"));
    assertEquals(0, counter.getCountOrZero("B"));
    assertEquals(emptySet(), counter.getItemsWithNonZeroCounts());
    assertEquals(0, counter.getSumOfCounts());

    counter = CounterBuilder.<String>counterBuilder()
        .increment("A")
        .build();
    assertEquals(1, counter.getCountOrZero("A"));
    assertEquals(0, counter.getCountOrZero("B"));
    assertEquals(singleton("A"), counter.getItemsWithNonZeroCounts());
    assertEquals(1, counter.getSumOfCounts());
    assertEquals(1, counter.getCountOrThrow("A"));

    counter = CounterBuilder.<String>counterBuilder()
        .increment("A")
        .increment("B")
        .increment("B")
        .increment("B")
        .build();
    assertFalse(counter.isEmpty());
    assertEquals(1, counter.getCountOrZero("A"));
    assertEquals(3, counter.getCountOrZero("B"));
    assertEquals(ImmutableSet.of("A", "B"), counter.getItemsWithNonZeroCounts());
    assertEquals(4, counter.getSumOfCounts());
  }

  @Test
  public void testGetCountOrThrow() {
    Counter<String> counter = CounterBuilder.<String>counterBuilder()
        .add("A", 1)
        .build();

    assertEquals(1, counter.getCountOrThrow("A"));
    assertIllegalArgumentException( () -> counter.getCountOrThrow("Z"));
  }

  @Test
  public void testGetItemsWithCountOf() {
    Counter<String> counter = CounterBuilder.<String>counterBuilder()
        .add("A", 0)
        .add("B", 1)
        .add("C", 1)
        .add("D", 2)
        .add("E", 2)
        .add("F", 3)
        .build();
    assertEquals(singleton("A"),            counter.getItemsWithCountOf(0));
    assertEquals(ImmutableSet.of("B", "C"), counter.getItemsWithCountOf(1));
    assertEquals(ImmutableSet.of("D", "E"), counter.getItemsWithCountOf(2));
    assertEquals(singleton("F"),            counter.getItemsWithCountOf(3));
    assertEmpty(counter.getItemsWithCountOf(4));

    assertIllegalArgumentException( () -> counter.getItemsWithCountOf(-1));
    assertIllegalArgumentException( () -> counter.getItemsWithCountOf(-999));
  }

  @Test
  public void testGetItemsWithCountOfAtLeast() {
    Counter<String> counter = CounterBuilder.<String>counterBuilder()
        .add("A", 0)
        .add("B", 1)
        .add("C", 2)
        .add("D", 2)
        .add("E", 3)
        .build();
    assertEquals(ImmutableSet.of("A", "B", "C", "D", "E"), counter.getItemsWithCountOfAtLeast(0));
    assertEquals(ImmutableSet.of(     "B", "C", "D", "E"), counter.getItemsWithCountOfAtLeast(1));
    assertEquals(ImmutableSet.of(          "C", "D", "E"), counter.getItemsWithCountOfAtLeast(2));
    assertEquals(singleton("E"),                           counter.getItemsWithCountOfAtLeast(3));
    assertEmpty(counter.getItemsWithCountOfAtLeast(4));

    // the following is equivalent to getItemsWithCountOfAtLeast(1):
    assertEquals(ImmutableSet.of(     "B", "C", "D", "E"), counter.getItemsWithNonZeroCounts());

    assertIllegalArgumentException( () -> counter.getItemsWithCountOf(-999));
    assertIllegalArgumentException( () -> counter.getItemsWithCountOf(-1));
  }

  @Test
  public void testAdd() {
    Counter<String> counter = CounterBuilder.<String>counterBuilder()
        .add("A", 7)
        .add("B", 0) // can add zero to initialize a Counter entry
        .build();
    assertEquals(7, counter.getCountOrZero("A"));
    assertEquals(7, counter.getSumOfCounts());
    assertEquals(0, counter.getCountOrZero("B"));

    counter = CounterBuilder.<String>counterBuilder()
        .add("A", 7)
        .add("A", 100)
        .add("A", 0)     // can add 0 without changing the count
        .build();
    assertEquals(107, counter.getCountOrZero("A"));
    assertEquals(107, counter.getSumOfCounts());

    assertIllegalArgumentException( () -> CounterBuilder.<String>counterBuilder()
        .add("A",  7)
        .add("A", -1));  // cannot add a negative count, even if the resulting count is positive
  }

  @Test
  public void testAddAllUnsafe() {
    CounterBuilder<String> builder = CounterBuilder.<String>counterBuilder()
        .add("A", 7)
        .add("B", 8);

    assertThat(
        builder.addAll(rbMapOf(
                "A",  3,   // "A" already has an entry: 7
                "B", 12,   // "B" already has an entry: 8
                "C", 30))  // "C" has no entry
            .build()
            .getRawMapUnsafe(),
        rbMapMatcher(
            rbMapOf(
                "A", intExplained(10, 7 +  3),
                "B", intExplained(20, 8 + 12),
                "C", 30),
            f -> typeSafeEqualTo(f)));
  }

  @Override
  public Counter<String> makeTrivialObject() {
    return CounterBuilder.<String>counterBuilder().build();
  }

  @Override
  public Counter<String> makeNontrivialObject() {
    return CounterBuilder.<String>counterBuilder()
        .increment("A")
        .increment("B")
        .add("A", 100)
        .add("B", 200)
        .build();
  }

  @Override
  public Counter<String> makeMatchingNontrivialObject() {
    return CounterBuilder.<String>counterBuilder()
        .add("A", 100)
        .add("B", 200)
        .increment("A")
        .increment("B")
        .build();
  }

  @Override
  protected boolean willMatch(Counter<String> expected, Counter<String> actual) {
    return counterMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<Counter<T>> counterMatcher(Counter<T> expected) {
    return makeMatcher(expected,
        // RBMap actually implements equals, and the assumption is that map keys also implement equals/hashcode.
        matchUsingEquals(v -> v.getRawMapUnsafe()));
  }

}
