package com.rb.nonbiz.types;

import org.junit.Test;

import static com.rb.nonbiz.types.LazyValue.lazyValue;
import static com.rb.nonbiz.types.LongCounter.longCounter;
import static org.junit.Assert.assertEquals;

public class LazyValueTest {

  @Test
  public void valueNeverGetsRetrieved_doesNotGetComputed() {
    LongCounter numCalculations = longCounter();
    LazyValue<String> lazyValueNeverRetrieved = lazyValue( () -> {
      numCalculations.increment();
      return "xyz";
    });
    assertEquals(0, numCalculations.get());
  }

  @Test
  public void valueGetsRetrievedOne_getsComputedOnce() {
    LongCounter numCalculations = longCounter();
    LazyValue<String> lazyValue = lazyValue( () -> {
      numCalculations.increment();
      return "xyz";
    });
    assertEquals("xyz", lazyValue.get());
    assertEquals(1, numCalculations.get());
  }

  @Test
  public void valueGetsRetrievedTwice_getsComputedOnce() {
    LongCounter numCalculations = longCounter();
    LazyValue<String> lazyValue = lazyValue( () -> {
      numCalculations.increment();
      return "xyz";
    });
    assertEquals("xyz", lazyValue.get());
    assertEquals("xyz", lazyValue.get());
    assertEquals(1, numCalculations.get());
  }

}
