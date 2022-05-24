package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.collections.RBStreams;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.collections.RBLists.listConcatenation;
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

  public static List<SimpleCsvRow> makeSingletonRowListFromCsvLine(String onlyCsvLine) {
    return singletonList(convertSingle(onlyCsvLine));
  }

  public static List<SimpleCsvRow> makeRowListFromCsvLines(String first, String second, String ... rest) {
    return RBStreams.concatenateFirstSecondAndRest(first, second, rest)
        .map(stringAsRow -> convertSingle(stringAsRow))
        .collect(Collectors.toList());
  }

  private static SimpleCsvRow convertSingle(String csvRowAsString) {
    List<String> components = Arrays.asList(csvRowAsString.split(","));
    // We need this hackery because String#split doesn't seem to work if the trailing
    // component is the empty string.
    return simpleCsvRow(csvRowAsString.endsWith(",")
        ? listConcatenation(components, singletonList(""))
        : components);
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
