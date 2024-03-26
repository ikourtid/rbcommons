package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a CSV file which has been read and loaded into memory.
 *
 * <p> This contains both a {@link SimpleCsvHeaderRow} and a list of {@link SimpleCsvRow}. </p>
 */
public class SimpleCsv {

  private final SimpleCsvHeaderRow headerRow;
  private final List<SimpleCsvRow> dataRows;
  private final int numColumns;

  private SimpleCsv(SimpleCsvHeaderRow headerRow, List<SimpleCsvRow> dataRows, int numColumns) {
    this.headerRow = headerRow;
    this.dataRows = dataRows;
    this.numColumns = numColumns;
  }

  public static SimpleCsv simpleCsv(SimpleCsvHeaderRow headerRow, List<SimpleCsvRow> dataRows) {
    int numColumns = headerRow.getNumColumns();

    for (int i = 0; i < dataRows.size(); i++) {
      SimpleCsvRow row = dataRows.get(i);
      RBSimilarityPreconditions.checkBothSame(
          row.getNumColumns(), numColumns,
          "Row # %s in CSV has %s instead of %s columns; headerRow= %s ; row was %s",
          i, row.getNumColumns(), numColumns, headerRow, row);
    }
    RBPreconditions.checkArgument(
        !dataRows.isEmpty(),
        "We must have at least 1 data row, but we only had the column header row of %s",
        headerRow);
    return new SimpleCsv(headerRow, dataRows, numColumns);
  }

  public SimpleCsvHeaderRow getHeaderRow() {
    return headerRow;
  }

  public List<SimpleCsvRow> getDataRows() {
    return dataRows;
  }

  public int getNumColumns() {
    return numColumns;
  }

  public SimpleCsv copyWithFilteredRows(Predicate<SimpleCsvRow> mustKeepRow) {
    return simpleCsv(
        headerRow,
        dataRows.stream()
            .filter(mustKeepRow)
            .collect(Collectors.toList()));
  }

  @Override
  public String toString() {
    return Strings.format("csv with %s columns, %s data rows, and headers %s",
        numColumns, dataRows.size(), headerRow);
  }

}
