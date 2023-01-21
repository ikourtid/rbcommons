package com.rb.nonbiz.text.csv;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.csv.SimpleCsvRow.simpleCsvRow;
import static com.rb.nonbiz.text.csv.SimpleCsvRowTest.simpleCsvRowMatcher;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleCsvRowParserTest extends RBTest<SimpleCsvRowParser> {

  @Test
  public void emptyLine_resultsInDelegatedException() {
    assertIllegalArgumentException( () -> makeTestObject().parseLine("", Optional.empty(), OptionalInt.empty()));
  }

  @Test
  public void singleColumn_noFiltering() {
    assertResultWithoutFiltering("x",       singletonList("x"));
    assertResultWithoutFiltering("a b",     singletonList("a b"));
    assertResultWithoutFiltering("\"a\"",   singletonList("a")); // unnecessary doublequotes, but should still get parsed correctly
    assertResultWithoutFiltering("\"x,y\"", singletonList("x,y")); // doublequotes are needed to escape the comma
    assertResultWithoutFiltering("\"Iraklis \"\"Hercules\"\" Kourtidis\"", singletonList("Iraklis \"Hercules\" Kourtidis"));
    assertResultWithoutFiltering("\"Rowboat Advisors, Inc.\"", singletonList("Rowboat Advisors, Inc."));
    assertResultWithoutFiltering("\"\"", singletonList("")); // empty string, doublequoted

    // I would think that ""a"" should convert to just "a", but it converts to a"
    // This looks wrong, but hopefuly we won't encounter it soon; it is too corner-case-y for our current
    // (Nov 2020) use cases.
    assertResultWithoutFiltering("\"\"a\"\"", singletonList("a\""));

    // No trimming of spaces. Sounds like the right thing to do; otherwise one has to define what a space is
    // (e.g. is the tab character also whitespace? probably).
    assertResultWithoutFiltering(" abc ", singletonList(" abc "));
  }

  @Test
  public void singleColumn_usesFiltering_includesColumn() {
    BiConsumer<String, List<String>> asserter = (rawLine, parsedCells) -> {
      BitSet inclusionFilter = new BitSet(1);
      inclusionFilter.set(0, true);
      assertThat(
          makeTestObject().parseLine(rawLine, Optional.of(inclusionFilter), OptionalInt.empty()),
          simpleCsvRowMatcher(
              simpleCsvRow(parsedCells)));
    };

    asserter.accept("x",       singletonList("x"));
    asserter.accept("a b",     singletonList("a b"));
    asserter.accept("\"a\"",   singletonList("a")); // unnecessary doublequotes, but should still get parsed correctly
    asserter.accept("\"x,y\"", singletonList("x,y")); // doublequotes are needed to escape the comma
    asserter.accept("\"Iraklis \"\"Hercules\"\" Kourtidis\"", singletonList("Iraklis \"Hercules\" Kourtidis"));
    asserter.accept("\"Rowboat Advisors, Inc.\"", singletonList("Rowboat Advisors, Inc."));
    asserter.accept("\"\"", singletonList("")); // empty string, doublequoted
  }

  @Test
  public void singleColumn_usesFiltering_excludesColumn_emptyCsvRowResultsInException() {
    BitSet inclusionFilter = new BitSet(1);
    inclusionFilter.set(0, false);
    rbSetOf(
        "x",
        "a b",
        "\"a\"",
        "\"x,y\"",
        "\"Iraklis \"\"Hercules\"\" Kourtidis\"",
        "\"Rowboat Advisors, Inc.\"",
        "\"\"")
        .forEach(rawLine -> assertIllegalArgumentException( () ->
            makeTestObject().parseLine(rawLine, Optional.of(inclusionFilter), OptionalInt.empty())));
  }

  @Test
  public void testMultipleColumns_noFiltering() {
    assertResultWithoutFiltering("x,\"a,b\"",       ImmutableList.of("x", "a,b")); // doublequotes are needed to escape the comma

    assertResultWithoutFiltering("x,y",             ImmutableList.of("x", "y"));
    assertResultWithoutFiltering("a b,x y",         ImmutableList.of("a b", "x y"));
    assertResultWithoutFiltering("\"a\",\"b\"",     ImmutableList.of("a", "b")); // unnecessary doublequotes, but should still get parsed correctly
    assertResultWithoutFiltering("\"x,y\",\"a,b\"", ImmutableList.of("x,y", "a,b")); // doublequotes are needed to escape the comma
    assertResultWithoutFiltering("x,\"a,b\"",       ImmutableList.of("x", "a,b")); // doublequotes are needed to escape the comma
    assertResultWithoutFiltering("\"x,y\",a",       ImmutableList.of("x,y", "a")); // doublequotes are needed to escape the comma
    assertResultWithoutFiltering("\"Rowboat Advisors, Inc.\",123", ImmutableList.of("Rowboat Advisors, Inc.", "123"));

    assertResultWithoutFiltering(",", ImmutableList.of("", ""));
    assertResultWithoutFiltering(",,", ImmutableList.of("", "", ""));
  }

  @Test
  public void testMultipleColumns_onlyKeepsOneColumn() {
    TriConsumer<String, String, String> asserter = (rawLine, firstItem, secondItem) -> {
      BitSet includeFirst = new BitSet(2);
      includeFirst.set(0);
      includeFirst.clear(1);

      BitSet includeSecond = new BitSet(2);
      includeSecond.clear(0);
      includeSecond.set(1);
      
      assertThat(
          makeTestObject().parseLine(rawLine, Optional.of(includeFirst), OptionalInt.empty()),
          simpleCsvRowMatcher(
              simpleCsvRow(singletonList(firstItem))));

      assertThat(
          makeTestObject().parseLine(rawLine, Optional.of(includeSecond), OptionalInt.empty()),
          simpleCsvRowMatcher(
              simpleCsvRow(singletonList(secondItem))));
    };

    asserter.accept("x,\"a,b\"",       "x",   "a,b");

    asserter.accept("x,y",             "x",   "y");
    asserter.accept("a b,x y",         "a b", "x y");
    asserter.accept("\"a\",\"b\"",     "a",   "b");
    asserter.accept("\"x,y\",\"a,b\"", "x,y", "a,b");
    asserter.accept("x,\"a,b\"",       "x",   "a,b");
    asserter.accept("\"x,y\",a",       "x,y", "a");
    asserter.accept("\"Rowboat Advisors, Inc.\",123", "Rowboat Advisors, Inc.", "123");

    asserter.accept(",", "", "");
  }

  @Test
  public void testRespectsExpectedNumColumns() {
    BiConsumer<String, Integer> assertIsBad = (rawLine, expectedColumns) -> {
      BitSet includeFirst = new BitSet(2);
      includeFirst.set(0);
      includeFirst.clear(1);

      BitSet includeSecond = new BitSet(2);
      includeSecond.clear(0);
      includeSecond.set(1);

      assertIllegalArgumentException( () ->
          makeTestObject().parseLine(rawLine, Optional.of(includeFirst), OptionalInt.of(expectedColumns)));
      assertIllegalArgumentException( () ->
          makeTestObject().parseLine(rawLine, Optional.of(includeSecond), OptionalInt.of(expectedColumns)));
    };

    assertIsBad.accept("A", 2);
    assertIsBad.accept("A,B", 1);
    assertIsBad.accept("A,B", 3);
    assertIsBad.accept("A,B", 4);
  }
  
  private void assertResultWithoutFiltering(String rawLine, List<String> parsedCells) {
    assertThat(
        makeTestObject().parseLine(rawLine, Optional.empty(), OptionalInt.empty()),
        simpleCsvRowMatcher(
            simpleCsvRow(parsedCells)));
  }

  @Override
  protected SimpleCsvRowParser makeTestObject() {
    return new SimpleCsvRowParser();
  }

}
