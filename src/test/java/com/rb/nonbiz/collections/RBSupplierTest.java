package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSupplier.rbSupplierReturningValidValue;
import static com.rb.nonbiz.collections.RBSupplier.rbSupplierThrowingException;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class RBSupplierTest {

  @Test
  public void testPresentItem() {
    assertEquals("foo", rbSupplierReturningValidValue("foo").get());
  }

  @Test
  public void testThrows() {
    assertIllegalArgumentException( () -> rbSupplierThrowingException().get());
  }

  public static <T> TypeSafeMatcher<RBSupplier<T>> rbSupplierMatcher(
      RBSupplier<T> expected,
      MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        matchOptional(v -> v.getRawOptionalUnsafe(), matcherGenerator));
  }

}
