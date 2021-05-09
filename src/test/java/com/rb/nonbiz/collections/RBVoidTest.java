package com.rb.nonbiz.collections;

import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBVoid.rbVoid;
import static com.rb.nonbiz.testmatchers.RBMatchers.alwaysMatchingMatcher;
import static org.junit.Assert.assertEquals;

public class RBVoidTest {

  @Test
  public void implementsEquals() {
    assertEquals(rbVoid(), rbVoid());
  }

  // RBVoid only has one instance, so we could always use typeSafeEqualTo instead,
  // but this is useful for clarity.
  public static TypeSafeMatcher<RBVoid> rbVoidMatcher(RBVoid expected) {
    return alwaysMatchingMatcher();
  }

}
