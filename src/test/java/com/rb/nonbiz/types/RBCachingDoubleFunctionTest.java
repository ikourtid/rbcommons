package com.rb.nonbiz.types;

import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.LongCounter.longCounter;
import static com.rb.nonbiz.types.RBCachingDoubleFunction.rbCachingDoubleFunction;
import static org.junit.Assert.assertEquals;

public class RBCachingDoubleFunctionTest {

  @Test
  public void doesNotRecalculateWhenValueIsThere() {
    LongCounter counter = longCounter();
    RBCachingDoubleFunction<String> cachingFunction = rbCachingDoubleFunction(
        x -> {
          counter.increment(); // intentionally generating a side effect, so we know when this got invoked
          return Double.toString(x * x);
        },
        epsilon(1.1));
    
    assertEquals(0, counter.get());

    assertEquals("16.0", cachingFunction.apply(4.0));
    assertEquals("This call must increment the counter from 0 to 1", 1, counter.get());

    assertEquals("16.0", cachingFunction.apply(4.0));
    assertEquals("The previous call used the existing pre-calculated value", 1, counter.get());

    assertEquals("16.0", cachingFunction.apply(doubleExplained(5.0999, 4.0 + 1.0999)));
    assertEquals("16.0", cachingFunction.apply(doubleExplained(2.9001, 4.0 - 1.0999)));
    assertEquals("The previous two calls used the existing value; keys are within epsilon", 1, counter.get());

    assertEquals("36.0", cachingFunction.apply(6));
    assertEquals(2, counter.get());

    // The midpoint between x = 4 and x = 6 is x = 5.
    assertEquals("16.0", cachingFunction.apply(3.0));
    assertEquals("16.0", cachingFunction.apply(3.5));
    assertEquals("16.0", cachingFunction.apply(4.0));
    assertEquals("16.0", cachingFunction.apply(4.9));
    assertEquals("36.0", cachingFunction.apply(5.1));
    assertEquals("36.0", cachingFunction.apply(6.0));
    assertEquals("36.0", cachingFunction.apply(7.0));

    assertEquals(2, counter.get()); // no recalculation in any of the above steps
  }

}
