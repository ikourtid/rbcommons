package com.rb.nonbiz.collections;

import org.junit.Test;

import static com.rb.nonbiz.collections.RBComparables.monotonic;
import static com.rb.nonbiz.collections.RBComparables.strictlyMonotonic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBComparablesTest {

  @Test
  public void testMin() {
    assertEquals("a", RBComparables.min("a", "b"));
    assertEquals("a", RBComparables.min("b", "a"));
    assertEquals("a", RBComparables.min("a", "a"));

    assertEquals("a", RBComparables.min("a", "b", "c"));
    assertEquals("a", RBComparables.min("b", "a", "c"));
    assertEquals("a", RBComparables.min("a", "a", "c"));

    assertEquals("a", RBComparables.min("a", "c", "b"));
    assertEquals("a", RBComparables.min("b", "c", "a"));
    assertEquals("a", RBComparables.min("a", "c", "a"));

    assertEquals("a", RBComparables.min("c", "a", "b"));
    assertEquals("a", RBComparables.min("c", "b", "a"));
    assertEquals("a", RBComparables.min("c", "a", "a"));
  }

  @Test
  public void testMax() {
    assertEquals("b", RBComparables.max("a", "b"));
    assertEquals("b", RBComparables.max("b", "a"));
    assertEquals("b", RBComparables.max("b", "b"));

    assertEquals("c", RBComparables.max("a", "b", "c"));
    assertEquals("c", RBComparables.max("b", "a", "c"));
    assertEquals("c", RBComparables.max("b", "b", "c"));

    assertEquals("c", RBComparables.max("a", "c", "b"));
    assertEquals("c", RBComparables.max("b", "c", "a"));
    assertEquals("c", RBComparables.max("b", "c", "b"));

    assertEquals("c", RBComparables.max("c", "a", "b"));
    assertEquals("c", RBComparables.max("c", "b", "a"));
    assertEquals("c", RBComparables.max("c", "b", "b"));
  }

  @Test
  public void testStrictlyMonotonic() {
    assertTrue(strictlyMonotonic(1, 2));
    assertFalse(strictlyMonotonic(2, 2));
    assertTrue(strictlyMonotonic(2, 1)); // it may be decreasing, but it's still monotonic

    assertTrue(strictlyMonotonic(1, 2, 3));

    assertFalse(strictlyMonotonic(1, 3, 2));
    assertFalse(strictlyMonotonic(2, 1, 3));
    assertFalse(strictlyMonotonic(2, 3, 1));
    assertFalse(strictlyMonotonic(3, 1, 2));
    assertTrue(strictlyMonotonic(3, 2, 1)); // it may be decreasing, but it's still monotonic

    assertFalse(strictlyMonotonic(1, 2, 2));
    assertFalse(strictlyMonotonic(2, 1, 1));

    assertFalse(strictlyMonotonic(2, 2, 1));
    assertFalse(strictlyMonotonic(1, 1, 2));
    assertFalse(strictlyMonotonic(1, 1, 1));
  }

  @Test
  public void testMonotonic() {
    assertTrue(monotonic(1, 2));
    assertTrue(monotonic(2, 2));
    assertTrue(monotonic(2, 1)); // it may be decreasing, but it's still monotonic

    assertTrue(monotonic(1, 2, 3));

    assertFalse(monotonic(1, 3, 2));
    assertFalse(monotonic(2, 1, 3));
    assertFalse(monotonic(2, 3, 1));
    assertFalse(monotonic(3, 1, 2));
    assertTrue(monotonic(3, 2, 1)); // it may be decreasing, but it's still monotonic

    assertTrue(monotonic(1, 2, 2));
    assertTrue(monotonic(2, 1, 1));

    assertTrue(monotonic(2, 2, 1));
    assertTrue(monotonic(1, 1, 2));
    assertTrue(monotonic(1, 1, 1));
  }

}
