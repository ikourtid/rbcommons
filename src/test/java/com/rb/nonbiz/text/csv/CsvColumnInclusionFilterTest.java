package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.collections.RBSets.setUnionOfFirstSecondAndRest;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class CsvColumnInclusionFilterTest extends RBTestMatcher<CsvColumnInclusionFilter> {

  public static CsvColumnInclusionFilter singletonCsvColumnInclusionFilter(String onlyColumnToInclude) {
    // no static import; this refers to the prod-only constructor.
    return CsvColumnInclusionFilter.csvColumnInclusionFilter(singletonRBSet(onlyColumnToInclude));
  }

  public static CsvColumnInclusionFilter csvColumnInclusionFilter(
      String firstColumnToInclude, String secondColumnToInclude, String ... restColumnsToInclude) {
    return CsvColumnInclusionFilter.csvColumnInclusionFilter(
        setUnionOfFirstSecondAndRest(firstColumnToInclude, secondColumnToInclude, restColumnsToInclude));
  }

  @Test
  public void emptyFilter_throws() {
    // no static import; this refers to the prod-only constructor.
    assertIllegalArgumentException( () -> CsvColumnInclusionFilter.csvColumnInclusionFilter(emptyRBSet()));
    CsvColumnInclusionFilter doesNotThrow;
    doesNotThrow = singletonCsvColumnInclusionFilter("x");
    doesNotThrow = csvColumnInclusionFilter("x", "y");
  }

  @Test
  public void emptyColumn_throws() {
    assertIllegalArgumentException( () -> singletonCsvColumnInclusionFilter(""));
    assertIllegalArgumentException( () -> csvColumnInclusionFilter("", "x"));
    assertIllegalArgumentException( () -> csvColumnInclusionFilter("x", ""));
  }

  @Override
  public CsvColumnInclusionFilter makeTrivialObject() {
    return singletonCsvColumnInclusionFilter("x");
  }

  @Override
  public CsvColumnInclusionFilter makeNontrivialObject() {
    return csvColumnInclusionFilter("x", "y", "z");
  }

  @Override
  public CsvColumnInclusionFilter makeMatchingNontrivialObject() {
    return csvColumnInclusionFilter("x", "y", "z");
  }

  @Override
  protected boolean willMatch(CsvColumnInclusionFilter expected, CsvColumnInclusionFilter actual) {
    return csvColumnInclusionFilterMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<CsvColumnInclusionFilter> csvColumnInclusionFilterMatcher(
      CsvColumnInclusionFilter expected) {
    return makeMatcher(expected,
        match(v -> v.getColumnsToInclude(), f -> rbSetEqualsMatcher(f)));
  }

}
