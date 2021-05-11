package com.rb.nonbiz.text.csv;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.nonEmptyOptionalMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.text.csv.CsvColumnInclusionFilterTest.csvColumnInclusionFilter;
import static com.rb.nonbiz.text.csv.CsvColumnInclusionFilterTest.singletonCsvColumnInclusionFilter;
import static com.rb.nonbiz.text.csv.SimpleCsv.simpleCsv;
import static com.rb.nonbiz.text.csv.SimpleCsvRow.simpleCsvRow;
import static com.rb.nonbiz.text.csv.SimpleCsvTest.simpleCsvMatcher;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleCsvParserTest extends RBCommonsIntegrationTest<SimpleCsvParser> {

  @Test
  public void noHeaderRow_throws() {
    assertIllegalArgumentException( () -> makeRealObject().parseSimpleCsv(""));
  }

  @Test
  public void hasHeaderRow_butNoDataRows_throws() {
    assertIllegalArgumentException( () -> makeRealObject().parseSimpleCsv("A,B"));
  }

  @Test
  public void generalCase_noFiltering() {
    assertThat(
        makeRealObject().parseSimpleCsv("A,B\na1,b1\na2,b2\na3,b3"),
        simpleCsvMatcher(
            simpleCsv(
                simpleCsvRow(ImmutableList.of("A", "B")),
                ImmutableList.of(
                    simpleCsvRow(ImmutableList.of("a1", "b1")),
                    simpleCsvRow(ImmutableList.of("a2", "b2")),
                    simpleCsvRow(ImmutableList.of("a3", "b3"))))));
  }

  @Test
  public void generalCase_onlyRetainsFirstColumn() {
    Consumer<CsvColumnInclusionFilter> asserter = csvColumnInclusionFilter -> assertThat(
        makeRealObject().parseOnlySomeCsvColumns("A,B\na1,b1\na2,b2\na3,b3", csvColumnInclusionFilter),
        nonEmptyOptionalMatcher(
            simpleCsvMatcher(
                simpleCsv(
                    simpleCsvRow(singletonList("A")),
                    ImmutableList.of(
                        simpleCsvRow(singletonList("a1")),
                        simpleCsvRow(singletonList("a2")),
                        simpleCsvRow(singletonList("a3")))))));
    asserter.accept(singletonCsvColumnInclusionFilter("A"));
    asserter.accept(csvColumnInclusionFilter("A", "NON_EXISTENT_COLUMN"));
    asserter.accept(csvColumnInclusionFilter("NON_EXISTENT_COLUMN", "A"));
  }

  @Test
  public void generalCase_onlyRetainsSecondColumn() {
    Consumer<CsvColumnInclusionFilter> asserter = csvColumnInclusionFilter -> assertThat(
        makeRealObject().parseOnlySomeCsvColumns("A,B\na1,b1\na2,b2\na3,b3", csvColumnInclusionFilter),
        nonEmptyOptionalMatcher(
            simpleCsvMatcher(
                simpleCsv(
                    simpleCsvRow(singletonList("B")),
                    ImmutableList.of(
                        simpleCsvRow(singletonList("b1")),
                        simpleCsvRow(singletonList("b2")),
                        simpleCsvRow(singletonList("b3")))))));
    asserter.accept(singletonCsvColumnInclusionFilter("B"));
    asserter.accept(csvColumnInclusionFilter("B", "NON_EXISTENT_COLUMN"));
    asserter.accept(csvColumnInclusionFilter("NON_EXISTENT_COLUMN", "B"));
  }

  @Test
  public void loadingSubsetOfColumns_csvFileIsBadWithUnequalNumbersOfCellsPerRow_throws() {
    BiFunction<List<String>, CsvColumnInclusionFilter, Optional<SimpleCsv>> maker = (allRows, csvColumnInclusionFilter) ->
        makeRealObject().parseOnlySomeCsvColumns(
            Joiner.on('\n').join(allRows), csvColumnInclusionFilter);

    List<String> wellFormedCsv = ImmutableList.of("A,B", "a1,b1", "a2,b2", "a3,b3");
    assertThat(
        maker.apply(wellFormedCsv, csvColumnInclusionFilter("A", "B")),
        nonEmptyOptionalMatcher(
            simpleCsvMatcher(
                simpleCsv(
                    simpleCsvRow(ImmutableList.of("A", "B")),
                    ImmutableList.of(
                        simpleCsvRow(ImmutableList.of("a1", "b1")),
                        simpleCsvRow(ImmutableList.of("a2", "b2")),
                        simpleCsvRow(ImmutableList.of("a3", "b3")))))));
    assertThat(
        maker.apply(wellFormedCsv, singletonCsvColumnInclusionFilter("A")),
        nonEmptyOptionalMatcher(
            simpleCsvMatcher(
                simpleCsv(
                    simpleCsvRow(singletonList("A")),
                    ImmutableList.of(
                        simpleCsvRow(singletonList("a1")),
                        simpleCsvRow(singletonList("a2")),
                        simpleCsvRow(singletonList("a3")))))));

    rbSetOf(
        singletonCsvColumnInclusionFilter("A"),
        singletonCsvColumnInclusionFilter("B"),
        csvColumnInclusionFilter("A", "B"))
        .forEach(csvColumnInclusionFilter ->
            rbSetOf(
                ImmutableList.of("A,B", "xxxxx", "a2,b2", "a3,b3"),
                ImmutableList.of("A,B", "x,x,x", "a2,b2", "a3,b3"),
                ImmutableList.of("A,B", "a1,b1", "xxxxx", "a3,b3"),
                ImmutableList.of("A,B", "a1,b1", "x,x,x", "a3,b3"),
                ImmutableList.of("A,B", "a1,b1", "a2,b2", "xxxxx"),
                ImmutableList.of("A,B", "a1,b1", "a2,b2", "x,x,x"),
                ImmutableList.of("A,B",     "",  "a2,b2", "a3,b3"),
                ImmutableList.of("A,B", "a1,b1",      "", "a3,b3"))
                // We also had a test case for:
                // ImmutableList.of("A,B", "a1,b1", "a2,b2",      "")
                // but it was removed, because if the last line is empty, it's as if it doesn't exist
                .forEach(malformedCsv -> assertIllegalArgumentException( () ->
                    maker.apply(malformedCsv, csvColumnInclusionFilter))));
  }

  @Test
  public void retainsNoColumns() {
    assertOptionalEmpty(
        makeRealObject().parseOnlySomeCsvColumns("A,B\na1,b1\na2,b2\na3,b3", singletonCsvColumnInclusionFilter("C")));
    assertOptionalEmpty(
        makeRealObject().parseOnlySomeCsvColumns("A,B\na1,b1\na2,b2\na3,b3", csvColumnInclusionFilter("C", "D")));
  }

  @Override
  protected Class<SimpleCsvParser> getClassBeingTested() {
    return SimpleCsvParser.class;
  }

}
