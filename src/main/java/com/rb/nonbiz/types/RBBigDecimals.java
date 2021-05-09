package com.rb.nonbiz.types;

import java.math.BigDecimal;

/**
 * Utilities related to {@code BigDecimal}.
 */
public class RBBigDecimals {

  // BigDecimal does not have a max value, so let's define one that should suffice for
  // any situation that we're likely to encounter.
  // Prefix with "RB" to clarify it isn't part of BigDecimal.
  // Note that Double.MIN_VALUE is NOT the most negative Double; rather it's the smallest
  // possible positive value. To get the most negative BigDecimal corresponding to
  // RB_BIG_DECIMAL_MAX_VALUE, use RB_BIG_DECIMAL_MAX_VALUE.negate().
  public static final BigDecimal RB_BIG_DECIMAL_MAX_VALUE = BigDecimal.valueOf(Double.MAX_VALUE);

}
