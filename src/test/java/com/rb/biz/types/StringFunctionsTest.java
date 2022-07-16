package com.rb.biz.types;

import org.junit.Test;

import static com.rb.biz.types.StringFunctions.isAllWhiteSpace;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

  @Test
  public void test_isAllWhiteSpace() {
    rbSetOf(
        "",
        "\t",
        "\r",
        "\n",
        "  ",
        " \t\r\n ")
        .forEach(str -> assertTrue(isAllWhiteSpace(str)));
    rbSetOf(
        "X",
        "X\t",
        "X\r",
        "X\n",
        "X  ",
        "X \t\r\n ",
        "\tX",
        "\rX",
        "\nX",
        "  X" ,
        " \t\r\n X ")
        .forEach(str -> assertFalse(isAllWhiteSpace(str)));
  }

}
