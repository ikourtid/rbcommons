package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.csv.SimpleCsvRow.simpleCsvRow;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SimpleCsvRowTest extends RBTestMatcher<SimpleCsvRow> {

  public static SimpleCsvRow singletonSimpleCsvRow(String onlyCell) {
    return simpleCsvRow(singletonList(onlyCell));
  }

  public static SimpleCsvRow testSimpleCsvRow(String first, String second, String ... rest) {
    return simpleCsvRow(concatenateFirstSecondAndRest(first, second, rest));
  }

  @Test
  public void noCells_throws() {
    assertIllegalArgumentException( () -> simpleCsvRow(emptyList()));
  }

  @Override
  public SimpleCsvRow makeTrivialObject() {
    return singletonSimpleCsvRow("");
  }

  @Override
  public SimpleCsvRow makeNontrivialObject() {
    return testSimpleCsvRow("abc", "123", "456.789");
  }

  @Override
  public SimpleCsvRow makeMatchingNontrivialObject() {
    return testSimpleCsvRow("abc", "123", "456.789");
  }

  @Override
  protected boolean willMatch(SimpleCsvRow expected, SimpleCsvRow actual) {
    return simpleCsvRowMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SimpleCsvRow> simpleCsvRowMatcher(SimpleCsvRow expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getCellsInRow())); // equals works for List<String>
  }

}
