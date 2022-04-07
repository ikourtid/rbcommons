package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;

/**
 * Represents the header row (column headers) in a .csv file.
 */
public class SimpleCsvHeaderRow {

  private final SimpleCsvRow simpleCsvRow;
  private final ArrayIndexMapping<String> arrayIndexMapping;

  private SimpleCsvHeaderRow(SimpleCsvRow simpleCsvRow, ArrayIndexMapping<String> arrayIndexMapping) {
    this.simpleCsvRow = simpleCsvRow;
    this.arrayIndexMapping = arrayIndexMapping;
  }

  public static SimpleCsvHeaderRow simpleCsvHeaderRow(
      SimpleCsvRow simpleCsvRow) {
    RBPreconditions.checkArgument(
        simpleCsvRow.getCellsInRow().stream().noneMatch(v -> v.isEmpty()),
        "Cannot have empty column headers: %s",
        simpleCsvRow);
    return new SimpleCsvHeaderRow(simpleCsvRow, simpleArrayIndexMapping(simpleCsvRow.getCellsInRow()));
  }

  public SimpleCsvRow getSimpleCsvRow() {
    return simpleCsvRow;
  }

  public ArrayIndexMapping<String> getArrayIndexMapping() {
    return arrayIndexMapping;
  }

  public int getNumColumns() {
    return simpleCsvRow.getCellsInRow().size();
  }

  public int getColumnIndex(String columnHeader) {
    return arrayIndexMapping.getIndex(columnHeader);
  }

  public String getColumnHeaderAt(int index) {
    return simpleCsvRow.getCell(index);
  }

  @Override
  public String toString() {
    return Strings.format("[SCHR %s SCHR]", simpleCsvRow);
  }

}
