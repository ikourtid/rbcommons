package com.rb.nonbiz.types;

import org.junit.Test;

import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.ZERO_EPSILON;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class EpsilonTest {

  public static final Epsilon DUMMY_EPSILON = epsilon(0.1234);

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
    assertTrue(DEFAULT_EPSILON_1e_8.areWithin(100 + 1e-9, 100));

    assertFalse(DEFAULT_EPSILON_1e_8.areWithin(100, 100 + 1e-7));
    assertFalse(DEFAULT_EPSILON_1e_8.areWithin(100 + 1e-7, 100));
  }

  /**
   * {@link Epsilon} extends {@link ImpreciseValue}, which has its own rich set of matchers.
   * Also, we usually don't have separate matchers for {@link ImpreciseValue} and {@link PreciseValue}.
   * However, in those few cases where we need to match epsilons, it's clearer to use a named matcher,
   * otherwise there would be two epsilons: the value to compare against, and also the epsilon used for comparing the
   * epsilons themselves. Note that we use an epsilon of 0 here, because epsilons are almost never the result of a
   * calculation that can cause numbers to be off; instead, their numeric literals are specified in the code.
   */
  public static TypeSafeMatcher<Epsilon> epsilonMatcher(Epsilon expected) {
    return impreciseValueMatcher(expected, ZERO_EPSILON);
  }

}
