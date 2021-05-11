package com.rb.biz.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringFunctionsTest {

  @Test
  public void withUnderscores_positiveValues() throws Exception {
    assertEquals("0", StringFunctions.withUnderscores(0));
    assertEquals("1", StringFunctions.withUnderscores(1));
    assertEquals("100", StringFunctions.withUnderscores(100));
    assertEquals("1_000", StringFunctions.withUnderscores(1_000));
    assertEquals("10_000", StringFunctions.withUnderscores(10_000));
    assertEquals("1_000_000", StringFunctions.withUnderscores(1_000_000));
    assertEquals("1_000_000_000_000", StringFunctions.withUnderscores(1_000_000_000_000L));
  }

}
