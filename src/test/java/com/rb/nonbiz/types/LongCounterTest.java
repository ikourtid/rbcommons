package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
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

  @Test
  public void testGetAsIntOrThrow() {
    LongCounter longCounter = longCounter();
    // set to max int value (2_147_483_647)
    longCounter.incrementBy(Integer.MAX_VALUE);

    // can get this longCounter value as either a long or as an int
    assertEquals(Integer.MAX_VALUE, longCounter.get());
    assertEquals(Integer.MAX_VALUE, longCounter.getAsIntOrThrow());

    // increment by one; the value is now larger than Integer.MAX_VALUE
    longCounter.increment();

    // can get this as a long
    assertEquals( Integer.MAX_VALUE + 1L, longCounter.get());

    // can't get this as an int
    assertThrowsAnyException( () -> longCounter.getAsIntOrThrow());

    // Now check for -Integer.MAX_VALUE
    // decrement to zero
    longCounter.incrementOrDecrementBy(-(Integer.MAX_VALUE + 1L));
    assertEquals(0, longCounter.getAsIntOrThrow());

    // decrement to Integer.MIN_VALUE (-2_147_483_648)
    longCounter.incrementOrDecrementBy(Integer.MIN_VALUE);

    // can get this as int
    assertEquals(Integer.MIN_VALUE, longCounter.getAsIntOrThrow());

    // decrement again
    longCounter.incrementOrDecrementBy(-1L);
    // can get this as long
    assertEquals(Integer.MIN_VALUE - 1L, longCounter.get());
    // can't get this as int
    assertThrowsAnyException( () -> longCounter.getAsIntOrThrow());
  }

  @Test
  public void simpleTestIncrementAndDecrement() {
    LongCounter longCounter = longCounter(10);
    assertEquals(10, longCounter.get());
    longCounter.decrement();
    assertEquals(9, longCounter.get());
    longCounter.increment();
    assertEquals(10, longCounter.get());
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
