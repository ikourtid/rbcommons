package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.MutableRBSortedSet.newMutableRBSortedSet;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.mutableRBSortedSetMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MutableRBSortedSetTest extends RBTestMatcher<MutableRBSortedSet<Double>> {

  @Test
  public void testBasicOperations_usingNaturalOrder() {
    MutableRBSortedSet<Integer> mutableSet = newMutableRBSortedSet(Integer::compare);
    assertTrue(mutableSet.isEmpty());
    mutableSet.addAssumingAbsent(11);
    assertFalse(mutableSet.isEmpty());
    mutableSet.addAssumingAbsent(10);
    assertIllegalArgumentException( () -> mutableSet.addAssumingAbsent(10));
    mutableSet.addAssumingAbsent(12);
    mutableSet.addAssumingAbsent(13);
    // This shows that insertion order does not matter
    assertEquals(10, mutableSet.first().intValue());
    assertEquals(13, mutableSet.last().intValue());
    assertEquals(4, mutableSet.size());
    assertTrue(mutableSet.contains(10));
    assertTrue(mutableSet.contains(11));
    assertTrue(mutableSet.contains(12));
    assertTrue(mutableSet.contains(13));

    mutableSet.removeAssumingPresent(11);
    assertIllegalArgumentException( () -> mutableSet.removeAssumingPresent(11)); // we just removed it
    assertFalse("False = it wasn't there before", mutableSet.remove(11));
    assertEquals(10, mutableSet.first().intValue());
    assertEquals(13, mutableSet.last().intValue());
    assertEquals(3, mutableSet.size());
    assertTrue(mutableSet.contains(10));
    assertFalse(mutableSet.contains(11));
    assertTrue(mutableSet.contains(12));
    assertTrue(mutableSet.contains(13));

    mutableSet.removeAssumingPresent(10);
    assertEquals(12, mutableSet.first().intValue());
    assertEquals(13, mutableSet.last().intValue());
    assertEquals(2, mutableSet.size());
  }

  @Test
  public void testBasicOperations_usingReverseOrder() {
    // -1 makes us use reverse sorting order from the normal Integer comparator
    MutableRBSortedSet<Integer> mutableSet = newMutableRBSortedSet( (v1, v2) -> -1 * Integer.compare(v1, v2));
    assertTrue(mutableSet.isEmpty());
    mutableSet.addAssumingAbsent(11);
    assertFalse(mutableSet.isEmpty());
    mutableSet.addAssumingAbsent(10);
    assertIllegalArgumentException( () -> mutableSet.addAssumingAbsent(10));
    mutableSet.addAssumingAbsent(12);
    mutableSet.addAssumingAbsent(13);
    // This shows that insertion order does not matter
    assertEquals(13, mutableSet.first().intValue());
    assertEquals(10, mutableSet.last().intValue());
    assertEquals(4, mutableSet.size());
    assertTrue(mutableSet.contains(10));
    assertTrue(mutableSet.contains(11));
    assertTrue(mutableSet.contains(12));
    assertTrue(mutableSet.contains(13));

    mutableSet.removeAssumingPresent(11);
    assertIllegalArgumentException( () -> mutableSet.removeAssumingPresent(11)); // we just removed it
    assertFalse("False = it wasn't there before", mutableSet.remove(11));
    assertEquals(13, mutableSet.first().intValue());
    assertEquals(10, mutableSet.last().intValue());
    assertEquals(3, mutableSet.size());
    assertTrue(mutableSet.contains(10));
    assertFalse(mutableSet.contains(11));
    assertTrue(mutableSet.contains(12));
    assertTrue(mutableSet.contains(13));

    mutableSet.removeAssumingPresent(10);
    assertEquals(13, mutableSet.first().intValue());
    assertEquals(12, mutableSet.last().intValue());
    assertEquals(2, mutableSet.size());
  }

  @Test
  public void firstOrLast_emptySet_throws() {
    MutableRBSortedSet<Integer> mutableSet = newMutableRBSortedSet(Integer::compare);
    assertIllegalArgumentException( () -> mutableSet.first());
    assertIllegalArgumentException( () -> mutableSet.last());
    mutableSet.add(10);
    assertEquals(10, mutableSet.first().intValue());
    assertEquals(10, mutableSet.last().intValue());
    mutableSet.add(11);
    assertEquals(10, mutableSet.first().intValue());
    assertEquals(11, mutableSet.last().intValue());
    mutableSet.removeAssumingPresent(10);
    assertEquals(11, mutableSet.first().intValue());
    assertEquals(11, mutableSet.last().intValue());
    mutableSet.removeAssumingPresent(11);
    assertIllegalArgumentException( () -> mutableSet.first());
    assertIllegalArgumentException( () -> mutableSet.last());
  }

  @Override
  public MutableRBSortedSet<Double> makeTrivialObject() {
    return newMutableRBSortedSet(Double::compare);
  }

  @Override
  public MutableRBSortedSet<Double> makeNontrivialObject() {
    MutableRBSortedSet<Double> mutableRBSortedSet = newMutableRBSortedSet(Double::compare);
    mutableRBSortedSet.addAssumingAbsent(7.7);
    mutableRBSortedSet.addAssumingAbsent(1.1);
    mutableRBSortedSet.addAssumingAbsent(3.3);
    return mutableRBSortedSet;
  }

  @Override
  public MutableRBSortedSet<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    MutableRBSortedSet<Double> mutableRBSortedSet = newMutableRBSortedSet(Double::compare);
    mutableRBSortedSet.addAssumingAbsent(3.3 + e);
    mutableRBSortedSet.addAssumingAbsent(7.7 + e);
    mutableRBSortedSet.addAssumingAbsent(1.1 + e);
    return mutableRBSortedSet;
  }

  @Override
  protected boolean willMatch(MutableRBSortedSet<Double> expected, MutableRBSortedSet<Double> actual) {
    return mutableRBSortedSetMatcher(expected, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  // The TypeSafeMatcher that normally is found at the end of a data class's test file
  // can be found in RBCollectionMatchers.java in this case.

}
