package com.rb.nonbiz.types;

import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.PositiveEpsilon.positiveEpsilon;

public class PositiveEpsilonTest {

  @Test
  public void testPositiveEpsilon() {
    assertIllegalArgumentException( () -> positiveEpsilon(-999));
    assertIllegalArgumentException( () -> positiveEpsilon(-1e-9));
    assertIllegalArgumentException( () -> positiveEpsilon(0));
    assertIllegalArgumentException( () -> positiveEpsilon(0.0));
    assertIllegalArgumentException( () -> positiveEpsilon(2.0 - 2.0));
    PositiveEpsilon doesNotThrow;
    doesNotThrow = positiveEpsilon(1e-9);
    doesNotThrow = positiveEpsilon(1e-7);
    doesNotThrow = positiveEpsilon(1);
    doesNotThrow = positiveEpsilon(99);
    assertIllegalArgumentException( () -> positiveEpsilon(1e9));
  }

}