package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.csv.SimpleCsvHeaderRow.simpleCsvHeaderRow;
import static com.rb.nonbiz.text.csv.SimpleCsvRowTest.simpleCsvRowMatcher;
import static com.rb.nonbiz.text.csv.SimpleCsvRowTest.singletonSimpleCsvRow;
import static com.rb.nonbiz.text.csv.SimpleCsvRowTest.testSimpleCsvRow;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class SimpleCsvHeaderRowTest extends RBTestMatcher<SimpleCsvHeaderRow> {

  @Test
  public void hasEmptyColumnHeader_throws() {
    assertIllegalArgumentException( () -> simpleCsvHeaderRow(singletonSimpleCsvRow("")));
    assertIllegalArgumentException( () -> simpleCsvHeaderRow(testSimpleCsvRow("", "c")));
    assertIllegalArgumentException( () -> simpleCsvHeaderRow(testSimpleCsvRow("c", "")));
  }

  @Override
  public SimpleCsvHeaderRow makeTrivialObject() {
    return simpleCsvHeaderRow(singletonSimpleCsvRow("c"));
  }

  @Override
  public SimpleCsvHeaderRow makeNontrivialObject() {
    return simpleCsvHeaderRow(testSimpleCsvRow("colA", "colB"));
  }

  @Override
  public SimpleCsvHeaderRow makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return simpleCsvHeaderRow(testSimpleCsvRow("colA", "colB"));
  }

  @Override
  protected boolean willMatch(SimpleCsvHeaderRow expected, SimpleCsvHeaderRow actual) {
    return simpleCsvHeaderRowMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SimpleCsvHeaderRow> simpleCsvHeaderRowMatcher(SimpleCsvHeaderRow expected) {
    return makeMatcher(expected,
        match(v -> v.getSimpleCsvRow(), f -> simpleCsvRowMatcher(f)));
  }

}
