package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.matchIntegerValue;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.RandomNumberGeneratorSeed.randomNumberGeneratorSeed;
import static org.junit.Assert.assertEquals;

public class RandomNumberGeneratorSeedTest extends RBTestMatcher<RandomNumberGeneratorSeed> {

  @Test
  public void implementsEquals() {
    assertEquals(makeTrivialObject(), makeTrivialObject());
    assertEquals(makeNontrivialObject(), makeNontrivialObject());
  }

  @Test
  public void mustBePositive_otherwiseThrows() {
    assertIllegalArgumentException( () -> randomNumberGeneratorSeed(-123));
    assertIllegalArgumentException( () -> randomNumberGeneratorSeed(-1));
    RandomNumberGeneratorSeed doesNotThrow;
    doesNotThrow = randomNumberGeneratorSeed(0);
    doesNotThrow = randomNumberGeneratorSeed(1);
    doesNotThrow = randomNumberGeneratorSeed(100);
    doesNotThrow = randomNumberGeneratorSeed(100_000_000);
  }

  @Override
  public RandomNumberGeneratorSeed makeTrivialObject() {
    return randomNumberGeneratorSeed(0);
  }

  @Override
  public RandomNumberGeneratorSeed makeNontrivialObject() {
    return randomNumberGeneratorSeed(123);
  }

  @Override
  public RandomNumberGeneratorSeed makeMatchingNontrivialObject() {
    return randomNumberGeneratorSeed(123);
  }

  @Override
  protected boolean willMatch(RandomNumberGeneratorSeed expected, RandomNumberGeneratorSeed actual) {
    return randomNumberGeneratorSeedMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RandomNumberGeneratorSeed> randomNumberGeneratorSeedMatcher(
      RandomNumberGeneratorSeed expected) {
    return makeMatcher(expected,
        matchIntegerValue(v -> v));
  }

}
