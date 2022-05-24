package com.rb.nonbiz.text.csv;

import com.google.inject.Inject;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.text.csv.SimpleCsv.simpleCsv;
import static com.rb.nonbiz.text.csv.SimpleCsvHeaderRow.simpleCsvHeaderRow;
import static com.rb.nonbiz.text.csv.SimpleCsvRow.simpleCsvRow;

/**
 * A CSV parser that is simple in the sense that the entire CSV file is loaded into memory, instead of using a
 * buffered file reader.
 *
 * There are other CSV libraries, but we are using ours because of the above simplification, and also for the
 * ability to only load a subset of the columns.
 */
public class SimpleCsvParser {

  @Inject SimpleCsvRowParser simpleCsvRowParser;

  /**
   * Returns empty optional if none of the columns pass the inclusion filter.
   *
   * We have to allow for the possibility that the inclusion filter means that nothing will be loaded from a csv.
   * In the case of ESG, it could be that attributes are spread across different csv files, and we are only interested
   * in loading a single one, which means that we shouldn't load anything from the other types of files.
   * Or, that an attribute appears only for the most recent e.g. 2 years, but not before that (e.g. it was added
   * recently).
   */
  public Optional<SimpleCsv> parseOnlySomeCsvColumns(String entireCsvAsString, CsvColumnInclusionFilter csvColumnInclusionFilter) {
    return parseSimpleCsv(entireCsvAsString, Optional.of(csvColumnInclusionFilter));
  }

  public SimpleCsv parseSimpleCsv(String entireCsvAsString) {
    return getOrThrow(
        parseSimpleCsv(entireCsvAsString, Optional.empty()), // Optional.empty => no filtering
        "Internal error; the CSV had no columns");
  }

  private Optional<SimpleCsv> parseSimpleCsv(String entireCsvAsString, Optional<CsvColumnInclusionFilter> csvColumnInclusionFilter) {
    String[] lines = entireCsvAsString.split("\n");
    RBPreconditions.checkArgument(
        lines.length >= 2,
        "CSV has no data rows: contents were %s",
        entireCsvAsString);
    // OptionalInt.empty() means we have no expectation for a # of cells (which makes sense, because we haven't
    // started reading the .csv yet).
    // Optional.empty() means we want to load all the column headers starting out, without filtering...
    SimpleCsvHeaderRow headerRow = simpleCsvHeaderRow(
        simpleCsvRowParser.parseLine(lines[0], Optional.empty(), OptionalInt.empty()));

    // ... up until this point, where we determine which subset of the columns we will keep.
    // If present, positionalInclusionFilter will tell us which columns to include (e.g. the 0th, 3rd, and 5th).
    Optional<BitSet> positionalInclusionFilter = transformOptional(
        csvColumnInclusionFilter, v -> calculatePositionalInclusionFilter(headerRow, v));
    int numColumnsInOutput = transformOptional(
        // We can't use csvColumnInclusionFilter.size() here, because it's possible that there are some columns
        // specified in csvColumnInclusionFilter that aren't also in the .csv file.
        positionalInclusionFilter, v -> v.cardinality())
        .orElse(headerRow.getNumColumns());

    if (positionalInclusionFilter.isPresent() && positionalInclusionFilter.get().cardinality() == 0) {
      // No columns from the csv will survive; return empty optional.
      return Optional.empty();
    }

    OptionalInt totalColumnsExpectedBeforeFiltering = OptionalInt.of(headerRow.getNumColumns());
    return Optional.of(simpleCsv(
        filterHeaderRow(headerRow, positionalInclusionFilter),
        IntStream.range(1, lines.length)
            // we also pass in the # of columns in the .csv (before filtering)
            // so we can sanity check that all rows have the same # of cells.
            .mapToObj(i -> simpleCsvRowParser.parseLine(
                lines[i], positionalInclusionFilter, numColumnsInOutput, totalColumnsExpectedBeforeFiltering))
            .collect(Collectors.toList())));
  }

  private SimpleCsvHeaderRow filterHeaderRow(
      SimpleCsvHeaderRow originalHeaderRow, Optional<BitSet> positionalInclusionFilter) {
    return transformOptional(
        positionalInclusionFilter,
        v -> {
          List<String> filteredHeaderRow = newArrayListWithExpectedSize(v.cardinality());
          for (int i = 0; i < v.size(); i++) {
            if (v.get(i)) {
              filteredHeaderRow.add(originalHeaderRow.getColumnHeader(i));
            }
          }
          return simpleCsvHeaderRow(simpleCsvRow(filteredHeaderRow));
        })
        .orElse(originalHeaderRow); // no filtering
  }

  private BitSet calculatePositionalInclusionFilter(
      SimpleCsvHeaderRow headerRow,
      CsvColumnInclusionFilter csvColumnInclusionFilter) {
    BitSet positionalInclusionFilter = new BitSet(headerRow.getNumColumns());
    for (int i = 0; i < headerRow.getNumColumns(); i++) {
      String columnHeader = headerRow.getColumnHeader(i);
      positionalInclusionFilter.set(i, csvColumnInclusionFilter.getColumnsToInclude().contains(columnHeader));
    }
    return positionalInclusionFilter;
  }

}
