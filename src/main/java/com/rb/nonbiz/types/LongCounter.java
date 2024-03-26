package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import static java.lang.Math.toIntExact;

/**
 * A non-negative {@link Long} number that represents how many times something has happened.
 *
 * <p> Use this when you modify a value e.g. inside some inner method in a lambda. Because it's mutable (which is rare
 * in our codebase), we should never pass around an {@link LongCounter}, or store it. It's not meant to be a C++ pointer! </p>
 *
 * <p> If you ever find yourself using a {@code Pointer<Integer>} or {@code Pointer<Long>},
 * chances are that this will be a better specialized version. </p>
 */
 public class LongCounter {

  private long currentValue;

  private LongCounter() {
    this.currentValue = 0;
  }

  public static LongCounter longCounter() {
    return new LongCounter();
  }

  public static LongCounter longCounter(long initialValue) {
    LongCounter longCounter = new LongCounter();
    longCounter.incrementBy(initialValue);
    return longCounter;
  }

  public LongCounter increment() {
    this.currentValue++;
    return this;
  }

  public LongCounter decrement() {
    this.currentValue--;
    return this;
  }

  public long getAndThenIncrement() {
    return this.currentValue++;
  }

  public LongCounter incrementBy(long incrementAmount) {
    RBPreconditions.checkArgument(
        incrementAmount > 0,
        "You are trying to increment the LongCounter by a non-positive amount of %s",
        incrementAmount);
    this.currentValue += incrementAmount;
    return this;
  }

  public LongCounter incrementOrDecrementBy(long netIncrementAmount) {
    this.currentValue += netIncrementAmount;
    return this;
  }

  public long get() {
    return currentValue;
  }

  public int getAsIntOrThrow() {
    // toIntExact() checks for overflow
    return toIntExact(currentValue);
  }

  @Override
  public String toString() {
    return Strings.format("[LC %s LC]", currentValue);
  }

}
