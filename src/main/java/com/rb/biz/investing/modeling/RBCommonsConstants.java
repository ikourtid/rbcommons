package com.rb.biz.investing.modeling;

import com.rb.nonbiz.text.Strings;

import java.math.MathContext;

/**
 * Various constants used across this repository.
 *
 * <p> We normally avoid top-level constants, which are effectively globals. </p>
 */
public class RBCommonsConstants {

  public static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL64;

  /**
   * Nothing in this repository relies on the filesystem. However, some tests do.
   *
   * <p> The backtester and higher-level repos need this, though, which is why it lives in the prod section
   * of this repo, not test. </p>
   */
  public String getFullFilename(String fromHome) {
    return Strings.format("%s/%s", System.getenv("HOME"), fromHome);
  }

}
