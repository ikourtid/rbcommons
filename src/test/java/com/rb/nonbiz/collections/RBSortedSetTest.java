package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static com.rb.nonbiz.collections.RBSortedSet.emptyRBSortedSet;
import static com.rb.nonbiz.collections.RBSortedSet.newRBSortedSet;
import static com.rb.nonbiz.collections.RBSortedSet.newRBSortedSetFromPossibleDuplicates;
import static com.rb.nonbiz.collections.RBSortedSet.rbSortedSetOf;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSortedSetMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.doubleIteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBSortedSetTest extends RBTestMatcher<RBSortedSet<Double>> {

  @Test
  public void testHandlingOfDuplicatesInIput() {
    assertEquals(rbSortedSetOf(String::compareTo, "a", "b"), newRBSortedSet(String::compareTo, "a", "b"));
    List<String> ab = ImmutableList.of("a", "b", "a");
    assertIllegalArgumentException( () -> newRBSortedSet(String::compareTo, "a", "b", "a"));
    assertIllegalArgumentException( () -> newRBSortedSet(String::compareTo, ab));
    assertIllegalArgumentException( () -> newRBSortedSet(String::compareTo, ab.iterator()));
    assertIllegalArgumentException( () -> newRBSortedSet(String::compareTo, ab.stream()));

    assertEquals(rbSortedSetOf(String::compareTo, "a", "b"), newRBSortedSetFromPossibleDuplicates(String::compareTo, "a", "b", "a"));
    assertEquals(rbSortedSetOf(String::compareTo, "a", "b"), newRBSortedSetFromPossibleDuplicates(String::compareTo, ab));
    assertEquals(rbSortedSetOf(String::compareTo, "a", "b"), newRBSortedSetFromPossibleDuplicates(String::compareTo, ab.iterator()));
    assertEquals(rbSortedSetOf(String::compareTo, "a", "b"), newRBSortedSetFromPossibleDuplicates(String::compareTo, ab.stream()));
  }

  @Test
  public void comparatorDifferent_meansStoreOrderDifferent_stillEquals() {
    Comparator<String> comparator = String::compareTo;
    assertEquals(
        rbSortedSetOf(comparator, "a", "b"),
        rbSortedSetOf(comparator, "a", "b"));
    assertEquals(
        "This looks odd, but Java says the two underlying SortedSet objects are equal, even though the ordering is different",
        rbSortedSetOf(comparator, "a", "b"),
        rbSortedSetOf(comparator.reversed(), "a", "b"));
  }

  @Test
  public void testFirstAndLast() {
    Comparator<Integer> comparator = Integer::compareTo;

    RBSortedSet<Integer> usingNaturalOrder = newRBSortedSet(comparator, 13, 11, 14, 12);
    assertEquals(11, usingNaturalOrder.first().intValue());
    assertEquals(14, usingNaturalOrder.last().intValue());

    RBSortedSet<Integer> usingReverseOrder = newRBSortedSet(comparator.reversed(), 13, 11, 14, 12);
    assertEquals(14, usingReverseOrder.first().intValue());
    assertEquals(11, usingReverseOrder.last().intValue());
  }
  
  @Test
  public void testIteratorReturnsItemsInOrder() {
    Comparator<Double> comparator = Double::compareTo;

    assertThat(
        newRBSortedSet(comparator, 3.3, 1.1, 5.5, 4.4, 2.2).iterator(),
        doubleIteratorMatcher(
            ImmutableList.of(1.1, 2.2, 3.3, 4.4, 5.5).iterator(),
            1e-8));
    assertThat(
        newRBSortedSet(comparator.reversed(), 3.3, 1.1, 5.5, 4.4, 2.2).iterator(),
        doubleIteratorMatcher(
            ImmutableList.of(5.5, 4.4, 3.3, 2.2, 1.1).iterator(),
            1e-8));
  }

  @Test
  public void firstOrLast_emptySet_throws() {
    assertIllegalArgumentException( () -> emptyRBSortedSet().first());
    assertIllegalArgumentException( () -> emptyRBSortedSet().last());
  }

  @Override
  public RBSortedSet<Double> makeTrivialObject() {
    return emptyRBSortedSet();
  }

  @Override
  public RBSortedSet<Double> makeNontrivialObject() {
    return rbSortedSetOf(Double::compare, 7.7, 1.1, 3.3);
  }

  @Override
  public RBSortedSet<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbSortedSetOf(Double::compare, 3.3 + e, 7.7 + e, 1.1 + e);
  }

  @Override
  protected boolean willMatch(RBSortedSet<Double> expected, RBSortedSet<Double> actual) {
    return rbSortedSetMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  // The TypeSafeMatcher that normally is found at the end of a data class's test file
  // can be found in RBCollectionMatchers.java in this case.

}
