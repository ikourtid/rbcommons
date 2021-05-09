package com.rb.nonbiz.types;

import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Correlation.BIGGEST_ANTI_CORRELATION;
import static com.rb.nonbiz.types.Correlation.PERFECT_CORRELATION;
import static com.rb.nonbiz.types.Correlation.correlation;

public class CorrelationTest {

  @Test
  public void rangeIsABitForgiving() {
    assertAlmostEquals(
        correlation(-1 - 1e-9),
        BIGGEST_ANTI_CORRELATION,
        0);
    assertAlmostEquals(
        correlation(1 + 1e-9),
        PERFECT_CORRELATION,
        0);
  }

  @Test
  public void correlationMustBeValid() {
    assertIllegalArgumentException( () -> correlation(-999));
    assertIllegalArgumentException( () -> correlation(-1.01));
    Correlation doesNotThrow;
    doesNotThrow = correlation(-1.0);
    doesNotThrow = correlation(-0.99);
    doesNotThrow = correlation(-0.01);
    doesNotThrow = correlation(0.0);
    doesNotThrow = correlation(0.01);
    doesNotThrow = correlation(0.99);
    doesNotThrow = correlation(1);
    assertIllegalArgumentException( () -> correlation(1.01));
    assertIllegalArgumentException( () -> correlation(999));
  }

}
