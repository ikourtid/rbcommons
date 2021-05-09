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
import static com.rb.nonbiz.text.csv.SimpleCsvRow.simpleCsvRow;
import static com.rb.nonbiz.text.csv.SimpleCsvRowTest.simpleCsvRowMatcher;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SimpleCsvTest extends RBTestMatcher<SimpleCsv> {

  @Test
  public void unequalColumns_throws() {
    assertIllegalArgumentException( () -> simpleCsv(
        simpleCsvRow(ImmutableList.of("col1", "col2", "col3")),
        ImmutableList.of(
            simpleCsvRow(ImmutableList.of("l1a", "l1b")),
            simpleCsvRow(ImmutableList.of("l2a", "l2b")))));
    assertIllegalArgumentException( () -> simpleCsv(
        simpleCsvRow(ImmutableList.of("col1")),
        ImmutableList.of(
            simpleCsvRow(ImmutableList.of("l1a", "l1b")),
            simpleCsvRow(ImmutableList.of("l2a", "l2b")))));
  }

  @Test
  public void noDataRows_throws() {
    assertIllegalArgumentException( () -> simpleCsv(
        simpleCsvRow(singletonList("")),
        emptyList()));
  }

  @Override
  public SimpleCsv makeTrivialObject() {
    return simpleCsv(
        simpleCsvRow(singletonList("")),
        singletonList(simpleCsvRow(singletonList(""))));
  }

  @Override
  public SimpleCsv makeNontrivialObject() {
    return simpleCsv(
        simpleCsvRow(ImmutableList.of("col1", "col2")),
        ImmutableList.of(
            simpleCsvRow(ImmutableList.of("l1a", "l1b")),
            simpleCsvRow(ImmutableList.of("l2a", "l2b"))));
  }

  @Override
  public SimpleCsv makeMatchingNontrivialObject() {
    return simpleCsv(
        simpleCsvRow(ImmutableList.of("col1", "col2")),
        ImmutableList.of(
            simpleCsvRow(ImmutableList.of("l1a", "l1b")),
            simpleCsvRow(ImmutableList.of("l2a", "l2b"))));
  }

  @Override
  protected boolean willMatch(SimpleCsv expected, SimpleCsv actual) {
    return simpleCsvMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SimpleCsv> simpleCsvMatcher(SimpleCsv expected) {
    return makeMatcher(expected,
        match(    v -> v.getHeaderRow(), f -> simpleCsvRowMatcher(f)),
        matchList(v -> v.getDataRows(),  f -> simpleCsvRowMatcher(f)));
  }

}
