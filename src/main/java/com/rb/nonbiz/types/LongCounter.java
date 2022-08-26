package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * Use this when you modify a value e.g. inside some inner method in a lambda.
 * We should never pass around an LongCounter, or store it. It's not meant to be a C++ pointer!
 *
 * <p> If you ever find yourself using a {@code Pointer<Integer>} or {@code Pointer<Long>},
 * chances are that this will be a better specialized version. </p>
 *
 * <p> This is one of those rare mutable classes in our system. </p>
 */
 public class LongCounter {

  private int currentValue;

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

  public int get() {
    return currentValue;
  }

  @Override
  public String toString() {
    return Strings.format("[LC %s LC]", currentValue);
  }

}
