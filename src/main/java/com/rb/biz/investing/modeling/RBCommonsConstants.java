package com.rb.biz.investing.modeling;

import com.rb.nonbiz.text.Strings;

import java.math.MathContext;

public class RBCommonsConstants {

  public static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL64;

  public String getFullFilename(String fromHome) {
    return Strings.format("%s/%s", System.getenv("HOME"), fromHome);
  }

}
