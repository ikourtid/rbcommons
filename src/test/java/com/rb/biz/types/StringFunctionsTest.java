package com.rb.biz.types;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.biz.types.StringFunctions.intuitiveStringSplit;
import static com.rb.biz.types.StringFunctions.isAllWhiteSpace;
import static com.rb.biz.types.StringFunctions.isTrimmed;
import static com.rb.biz.types.StringFunctions.isValidJavaIdentifier;
import static com.rb.biz.types.StringFunctions.isValidRowboatJavaIdentifier;
import static com.rb.biz.types.StringFunctions.withUnderscores;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.arrayEqualityMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringFunctionsTest {

  @Test
  public void withUnderscores_positiveValues() {
    assertEquals("0", withUnderscores(0));
    assertEquals("1", withUnderscores(1));
    assertEquals("100", withUnderscores(100));
    assertEquals("1_000", withUnderscores(1_000));
    assertEquals("10_000", withUnderscores(10_000));
    assertEquals("1_000_000", withUnderscores(1_000_000));
    assertEquals("1_000_000_000_000", withUnderscores(1_000_000_000_000L));
  }

  @Test
  public void withUnderscores_negativeValues() {
    assertEquals("-1", withUnderscores(-1));
    assertEquals("-100", withUnderscores(-100));
    assertEquals("-1_000", withUnderscores(-1_000));
    assertEquals("-10_000", withUnderscores(-10_000));
    assertEquals("-1_000_000", withUnderscores(-1_000_000));
    assertEquals("-1_000_000_000_000", withUnderscores(-1_000_000_000_000L));
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

  @Test
  public void testIsValidRowboatJavaIdentifier() {
    ImmutableList.of(
            "foo", "foo1", "foo123", "_foo", "foo_", "fooBar", "foo_bar",
            "Foo", "Foo1", "Foo123", "_Foo", "Foo_", "FooBar", "Foo_bar")
        .forEach(v -> assertTrue(isValidRowboatJavaIdentifier(v)));

    // Strangely, and I didn't know this, foo$ is a valid identifier in Java.
    // But isValidRowboatJavaIdentifier intentionally prohibits it.
    ImmutableList.of(
            "foo ", " foo", "1foo", "123foo", "foo*", "foo-bar", "foo$",
            "Foo ", " Foo", "1Foo", "123Foo", "Foo*", "Foo-bar", "Foo$")
        .forEach(v -> assertFalse(
            v + " must be an invalid identifier",
            isValidRowboatJavaIdentifier(v)));
  }

  @Test
  public void testIntuitiveStringSplit() {
    BiConsumer<String, String[]> asserter = (inputString, expectedResult) ->
        assertThat(
            intuitiveStringSplit(inputString, '`'),
            arrayEqualityMatcher(expectedResult));

    asserter.accept("`ClassA` `ClassB`", new String[] { "", "ClassA", " ", "ClassB", "" });

    asserter.accept("", new String[] { "" });

    asserter.accept("`",                 new String[] { "", "" });
    asserter.accept("`ClassA",           new String[] { "", "ClassA" });
    asserter.accept( "ClassA`",          new String[] { "ClassA", "" });
    asserter.accept("`ClassA`",          new String[] { "", "ClassA", "" });
    asserter.accept("`ClassA` `ClassB",  new String[] { "", "ClassA", " ", "ClassB" });
    asserter.accept("`ClassA` `ClassB`", new String[] { "", "ClassA", " ", "ClassB", "" });

    // Same test, but with a leading space
    asserter.accept(" `",                 new String[] { " ", "" });
    asserter.accept(" `ClassA",           new String[] { " ", "ClassA" });
    asserter.accept( " ClassA`",          new String[] { " ClassA", "" });
    asserter.accept(" `ClassA`",          new String[] { " ", "ClassA", "" });
    asserter.accept(" `ClassA` `ClassB",  new String[] { " ", "ClassA", " ", "ClassB" });
    asserter.accept(" `ClassA` `ClassB`", new String[] { " ", "ClassA", " ", "ClassB", "" });

    // Same, but with a trailing space
    asserter.accept("` ",                 new String[] { "", " " });
    asserter.accept("`ClassA ",           new String[] { "", "ClassA " });
    asserter.accept( "ClassA` ",          new String[] { "ClassA", " " });
    asserter.accept("`ClassA` ",          new String[] { "", "ClassA", " " });
    asserter.accept("`ClassA` `ClassB ",  new String[] { "", "ClassA", " ", "ClassB " });
    asserter.accept("`ClassA` `ClassB` ", new String[] { "", "ClassA", " ", "ClassB", " " });

  }

}
