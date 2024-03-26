package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.text.csv.SimpleCsvRow.simpleCsvRow;

/**
 * Parsing a single row of CSV is simple, in the common case:
 * abc,xyz,123
 *
 * <p> However, CSV is allowed to have commas inside the cells, if the cell is surrounded by doublequotes.
 * For example:
 * {@code  x,y }
 * is represented as
 * {@code  "x,y" }
 *
 * and
 * {@code  "a" }
 * gets represented as:
 * {@code  """a""" }
 * </p>
 *
 * <p> This is useful for cases where we have a file with many columns, but we only want to read a subset of them.
 * Instead of loading the entire file in memory as a string, like {@link SimpleCsvParser} does,
 * we will read one line at a time, and only keep the columns we care about. </p>
 */
public class SimpleCsvRowParser {

  public SimpleCsvRow parseLine(
      String csvRow,
      Optional<BitSet> positionalInclusionFilter,
      OptionalInt totalColumnsExpectedBeforeFiltering) {
    return parseLine(csvRow, positionalInclusionFilter, newArrayList(), totalColumnsExpectedBeforeFiltering);
  }

  public SimpleCsvRow parseLine(
      String csvRow,
      Optional<BitSet> positionalInclusionFilter,
      int expectedNumColumnsInOutput,
      OptionalInt totalColumnsExpectedBeforeFiltering) {
    SimpleCsvRow simpleCsvRow = parseLine(
        csvRow,
        positionalInclusionFilter,
        newArrayListWithExpectedSize(expectedNumColumnsInOutput),
        totalColumnsExpectedBeforeFiltering);
    RBSimilarityPreconditions.checkBothSame(
        simpleCsvRow.getNumColumns(),
        expectedNumColumnsInOutput,
        "We expected %s columns but this row has %s : %s",
        expectedNumColumnsInOutput, simpleCsvRow.getNumColumns(), simpleCsvRow);
    return simpleCsvRow;
  }

  private SimpleCsvRow parseLine(
      String csvRow,
      Optional<BitSet> positionalInclusionFilter,
      List<String> mutableResult,
      OptionalInt totalColumnsExpectedBeforeFiltering) {
    if (csvRow.isEmpty()) {
      return simpleCsvRow(mutableResult); // This currently (Nov 2020) becomes an exception inside simpleCsvRow
    }

    StringBuffer curVal = new StringBuffer();
    boolean inQuotes = false;
    boolean startCollectChar = false;
    boolean doubleQuotesInColumn = false;

    char[] chars = csvRow.toCharArray();

    int numCells = 0;

    for (char ch : chars) {

      if (inQuotes) {
        startCollectChar = true;
        if (ch == '"') {
          inQuotes = false;
          doubleQuotesInColumn = false;
        } else {

          //Fixed : allow "" in custom quote enclosed
          if (ch == '\"') {
            if (!doubleQuotesInColumn) {
              curVal.append(ch);
              doubleQuotesInColumn = true;
            }
          } else {
            curVal.append(ch);
          }

        }
      } else {
        if (ch == '"') {

          inQuotes = true;

          /*
          The following was in the code we originally borrowed from, but it seems to have weird results.
          Our tests are passing only if we comment this out.

          //Fixed : allow "" in empty quote enclosed
          if (chars[0] != '"' && '"' == '\"') {
            curVal.append('"');
          }
          */

          //double quotes in column will hit this!
          if (startCollectChar) {
            curVal.append('"');
          }

        } else if (ch == ',') {

          if (!positionalInclusionFilter.isPresent() || positionalInclusionFilter.get().get(numCells)) {
            mutableResult.add(curVal.toString());
          }
          numCells++;

          curVal = new StringBuffer();
          startCollectChar = false;

        } else if (ch == '\r') {
          //ignore LF characters
        } else if (ch == '\n') {
          //the end, break!
          break;
        } else {
          curVal.append(ch);
        }
      }
    }

    // The last cell needs special handling
    if (!positionalInclusionFilter.isPresent() || positionalInclusionFilter.get().get(numCells)) {
      mutableResult.add(curVal.toString());
    }

    numCells++; // the above does not count the last value I think
    if (totalColumnsExpectedBeforeFiltering.isPresent()) {
      RBSimilarityPreconditions.checkBothSame(
          numCells,
          totalColumnsExpectedBeforeFiltering.getAsInt(),
          "Bad line in csv with %s instead of expected %s cells: %s",
          numCells, totalColumnsExpectedBeforeFiltering.getAsInt(), csvRow);
    }
    return simpleCsvRow(mutableResult);
  }

}
