package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A typesafe thin wrapper around a set of strings to help us specify which CSV columns to bother reading
 * from a file. This is useful for cases where there are tons of them, and we only care about a small subset.
 */
public class CsvColumnInclusionFilter {

  private final RBSet<String> columnsToInclude;

  private CsvColumnInclusionFilter(RBSet<String> columnsToInclude) {
    this.columnsToInclude = columnsToInclude;
  }

  public static CsvColumnInclusionFilter csvColumnInclusionFilter(RBSet<String> columnsToInclude) {
    RBPreconditions.checkArgument(
        !columnsToInclude.isEmpty(),
        "You must include at least one CSV column in the CsvColumnInclusionFilter");
    RBPreconditions.checkArgument(
        columnsToInclude.stream().noneMatch(v -> v.isEmpty()),
        "You cannot have empty columns in CsvColumnInclusionFilter: %s",
        columnsToInclude);
    return new CsvColumnInclusionFilter(columnsToInclude);
  }

  public RBSet<String> getColumnsToInclude() {
    return columnsToInclude;
  }

  @Override
  public String toString() {
    return Strings.format("[CCIF %s CCIF]", Strings.formatCollectionInOrder(columnsToInclude.asSet()));
  }

}
