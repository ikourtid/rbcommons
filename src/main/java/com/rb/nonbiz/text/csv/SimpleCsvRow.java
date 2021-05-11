package com.rb.nonbiz.text.csv;

import com.google.common.base.Joiner;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

public class SimpleCsvRow {

  private final List<String> cellsInRow;

  private SimpleCsvRow(List<String> cellsInRow) {
    this.cellsInRow = cellsInRow;
  }

  public static SimpleCsvRow simpleCsvRow(List<String> cellsInRow) {
    RBPreconditions.checkArgument(
        !cellsInRow.isEmpty(),
        "You cannot have an empty csv row");
    return new SimpleCsvRow(cellsInRow);
  }

  public List<String> getCellsInRow() {
    return cellsInRow;
  }

  public String getCell(int column) {
    return cellsInRow.get(column);
  }

  public int getNumColumns() {
    return cellsInRow.size();
  }

  @Override
  public String toString() {
    return Strings.format("[SCR %s : %s SCR]", getNumColumns(), Joiner.on(',').join(cellsInRow));
  }

}
