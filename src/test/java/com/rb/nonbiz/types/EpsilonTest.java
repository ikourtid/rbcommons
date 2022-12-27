package com.rb.nonbiz.types;

import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class EpsilonTest {

  @Test
  public void testValidValues() {
    assertIllegalArgumentException( () -> epsilon(-999));
    assertIllegalArgumentException( () -> epsilon(-1e-9));
    Epsilon doesNotThrow;
    doesNotThrow = epsilon(0);
    doesNotThrow = epsilon(1e-9);
    doesNotThrow = epsilon(1e-7);
    doesNotThrow = epsilon(1);
    doesNotThrow = epsilon(99);
    assertIllegalArgumentException( () -> epsilon(100));
  }

  @Test
  public void testWithin() {
    assertTrue(DEFAULT_EPSILON_1e_8.areWithin(100, 100 + 1e-9));
    assertFalse(DEFAULT_EPSILON_1e_8.areWithin(100, 100 + 1e-7));
  }

}
