package com.rb.nonbiz.types;

import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.math.stats.ZScore.zScore;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.ZERO_EPSILON;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
  public void testValuesAreWithin_double() {
    assertTrue(DEFAULT_EPSILON_1e_8.valuesAreWithin(100, 100 + 1e-9));
    assertTrue(DEFAULT_EPSILON_1e_8.valuesAreWithin(100 + 1e-9, 100));

    assertFalse(DEFAULT_EPSILON_1e_8.valuesAreWithin(100, 100 + 1e-7));
    assertFalse(DEFAULT_EPSILON_1e_8.valuesAreWithin(100 + 1e-7, 100));
  }

  @Test
  public void testValuesAreWithin_PreciseValue() {
    // Nothing special about Money - it's just some sample ImpreciseValue we can use for this test.
    assertTrue(DEFAULT_EPSILON_1e_8.valuesAreWithin(money(100), money(100 + 1e-9)));
    assertTrue(DEFAULT_EPSILON_1e_8.valuesAreWithin(money(100 + 1e-9), money(100)));

    assertFalse(DEFAULT_EPSILON_1e_8.valuesAreWithin(money(100), money(100 + 1e-7)));
    assertFalse(DEFAULT_EPSILON_1e_8.valuesAreWithin(money(100 + 1e-7), money(100)));
  }

  @Test
  public void testValuesAreWithin_ImpreciseValue() {
    // Nothing special about ZScore - it's just some sample ImpreciseValue we can use for this test.
    assertTrue(DEFAULT_EPSILON_1e_8.valuesAreWithin(zScore(100), zScore(100 + 1e-9)));
    assertTrue(DEFAULT_EPSILON_1e_8.valuesAreWithin(zScore(100 + 1e-9), zScore(100)));

    assertFalse(DEFAULT_EPSILON_1e_8.valuesAreWithin(zScore(100), zScore(100 + 1e-7)));
    assertFalse(DEFAULT_EPSILON_1e_8.valuesAreWithin(zScore(100 + 1e-7), zScore(100)));
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
