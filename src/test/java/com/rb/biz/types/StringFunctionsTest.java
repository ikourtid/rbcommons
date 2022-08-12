package com.rb.biz.types;

import org.junit.Test;

import static com.rb.biz.types.StringFunctions.isAllWhiteSpace;
import static com.rb.biz.types.StringFunctions.isTrimmed;
import static com.rb.biz.types.StringFunctions.isValidJavaIdentifier;
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

  @Test
  public void test_isTrimmed() {
    rbSetOf(
        "\t",
        "\r",
        "\n",
        "  ",
        " \t\r\n ",
        " abc",
        "abc ",
        "\nabc",
        "abc\n")
        .forEach(str -> assertFalse(isTrimmed(str)));
    rbSetOf(
        "",
        "X",
        "X Y",  // can have interior whitespace
        "X\tY",
        "X\nY",
        "X  \t  \n \r Y")
        .forEach(str -> assertTrue(isTrimmed(str)));
  }

  @Test
  public void testIsValidJavaIdentifier() {
    // Strangely, and I didn't know this, foo$ is a valid identifier in Java.
    rbSetOf("foo", "foo1", "foo123", "_foo", "foo_", "fooBar", "foo_bar", "foo$")
        .forEach(v -> assertTrue(isValidJavaIdentifier(v)));

    rbSetOf("foo ", " foo", "1foo", "123foo", "foo*", "foo-bar")
        .forEach(v -> assertFalse(
            v + " must be an invalid identifier",
            isValidJavaIdentifier(v)));
  }

}
