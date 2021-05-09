package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.LongCounter.longCounter;
import static org.junit.Assert.assertEquals;

public class LongCounterTest extends RBTestMatcher<LongCounter> {

  @Test
  public void generalUsage() {
    LongCounter longCounter = longCounter();
    assertEquals(0, longCounter.get());
    longCounter.increment();
    assertEquals(1, longCounter.get());
    longCounter.increment();
    longCounter.increment();
    assertEquals(3, longCounter.get());
    longCounter.incrementBy(10);
    assertEquals(13, longCounter.get());
    assertIllegalArgumentException( () -> longCounter.incrementBy(0));
    assertIllegalArgumentException( () -> longCounter.incrementBy(-1));
    assertIllegalArgumentException( () -> longCounter.incrementBy(-999));
    assertEquals(13, longCounter.get());

    // using an argument n in longCounter(n) calls longCounter.incrementBy(n); n must be positive
    assertIllegalArgumentException( () -> longCounter(-1));
    assertIllegalArgumentException( () -> longCounter( 0));
    assertEquals(1,   longCounter(  1).get());
    assertEquals(123, longCounter(123).get());
  }

  @Test
  public void getAndThenIncrement() {
    LongCounter longCounter = longCounter();
    assertEquals(0, longCounter.get());
    assertEquals(0, longCounter.get());
    assertEquals(0, longCounter.getAndThenIncrement());
    assertEquals(1, longCounter.get());
    assertEquals(1, longCounter.get());
    assertEquals(1, longCounter.getAndThenIncrement());
    assertEquals(2, longCounter.getAndThenIncrement());
    assertEquals(3, longCounter.getAndThenIncrement());
  }

  @Test
  public void incrementOrDecrement() {
    LongCounter longCounter = longCounter();
    assertEquals( 10, longCounter.incrementOrDecrementBy( 10).get());
    assertEquals(-20, longCounter.incrementOrDecrementBy(-30).get());
    assertEquals(  1, longCounter.incrementOrDecrementBy( 21).get());
  }

  @Override
  public LongCounter makeTrivialObject() {
    return longCounter();
  }

  @Override
  public LongCounter makeNontrivialObject() {
    return longCounter().increment().increment();
  }

  @Override
  public LongCounter makeMatchingNontrivialObject() {
    return longCounter().increment().increment();
  }

  @Override
  protected boolean willMatch(LongCounter expected, LongCounter actual) {
    return longCounterMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<LongCounter> longCounterMatcher(LongCounter expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.get()));
  }

}
