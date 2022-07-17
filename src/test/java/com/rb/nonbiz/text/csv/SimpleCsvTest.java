package com.rb.nonbiz.text.csv;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.csv.SimpleCsv.simpleCsv;
import static com.rb.nonbiz.text.csv.SimpleCsvHeaderRowTest.simpleCsvHeaderRowMatcher;
import static com.rb.nonbiz.text.csv.SimpleCsvHeaderRowTest.singletonSimpleCsvHeaderRow;
import static com.rb.nonbiz.text.csv.SimpleCsvHeaderRowTest.testSimpleCsvHeaderRow;
import static com.rb.nonbiz.text.csv.SimpleCsvRow.simpleCsvRow;
import static com.rb.nonbiz.text.csv.SimpleCsvRowTest.simpleCsvRowMatcher;
import static com.rb.nonbiz.text.csv.SimpleCsvRowTest.testSimpleCsvRow;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleCsvTest extends RBTestMatcher<SimpleCsv> {

  @Test
  public void unequalColumns_throws() {
    assertIllegalArgumentException( () -> simpleCsv(
        testSimpleCsvHeaderRow("col1", "col2", "col3"),
        ImmutableList.of(
            testSimpleCsvRow("l1a", "l1b"),
            testSimpleCsvRow("l2a", "l2b"))));
    assertIllegalArgumentException( () -> simpleCsv(
        singletonSimpleCsvHeaderRow("col1"),
        ImmutableList.of(
            testSimpleCsvRow("l1a", "l1b"),
            testSimpleCsvRow("l2a", "l2b"))));
  }

  @Test
  public void test_copyWithFilteredRows() {
    assertThat(
        simpleCsv(
            testSimpleCsvHeaderRow("col1", "col2"),
            ImmutableList.of(
                testSimpleCsvRow("l1a", "l1b"),
                testSimpleCsvRow("l2a", "l2b")))
            .copyWithFilteredRows(row -> row.getCell(0).equals("l1a")),
        simpleCsvMatcher(
            simpleCsv(
                testSimpleCsvHeaderRow("col1", "col2"),
                ImmutableList.of(
                    simpleCsvRow(ImmutableList.of("l1a", "l1b"))))));
  }

  @Test
  public void noDataRows_throws() {
    assertIllegalArgumentException( () -> simpleCsv(
        singletonSimpleCsvHeaderRow("x"),
        emptyList()));
  }

  @Override
  public SimpleCsv makeTrivialObject() {
    return simpleCsv(
        singletonSimpleCsvHeaderRow("x"),
        singletonList(simpleCsvRow(singletonList(""))));
  }

  @Override
  public SimpleCsv makeNontrivialObject() {
    return simpleCsv(
        testSimpleCsvHeaderRow("col1", "col2"),
        ImmutableList.of(
            testSimpleCsvRow("l1a", "l1b"),
            testSimpleCsvRow("l2a", "l2b")));
  }

  @Override
  public SimpleCsv makeMatchingNontrivialObject() {
    return simpleCsv(
        testSimpleCsvHeaderRow("col1", "col2"),
        ImmutableList.of(
            testSimpleCsvRow("l1a", "l1b"),
            testSimpleCsvRow("l2a", "l2b")));
  }

  @Override
  protected boolean willMatch(SimpleCsv expected, SimpleCsv actual) {
    return simpleCsvMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SimpleCsv> simpleCsvMatcher(SimpleCsv expected) {
    return makeMatcher(expected,
        match(    v -> v.getHeaderRow(), f -> simpleCsvHeaderRowMatcher(f)),
        matchList(v -> v.getDataRows(),  f -> simpleCsvRowMatcher(f)));
  }

}
