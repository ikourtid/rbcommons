package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.text.Strings;

import java.util.Optional;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;

/**
 * A simple data class that tells you how to extract a value from a field in a {@link SimpleCsvRow}.
 */
public class CsvColumnExtractor<T> {

  private final String columnName;
  private final Function<String, Optional<T>> valueExtractor;

  private CsvColumnExtractor(String columnName, Function<String, Optional<T>> valueExtractor) {
    this.columnName = columnName;
    this.valueExtractor = valueExtractor;
  }

  public static <T> CsvColumnExtractor<T> csvColumnExtractorAllowingInvalidValues(
      String columnName, Function<String, Optional<T>> valueExtractor) {
    return new CsvColumnExtractor<>(columnName, valueExtractor);
  }

  public static <T> CsvColumnExtractor<T> csvColumnExtractorThatThrowsOnInvalidValues(
      String columnName, Function<String, T> valueExtractor) {
    return new CsvColumnExtractor<>(columnName, v -> Optional.of(valueExtractor.apply(v)));
  }

  public String getColumnName() {
    return columnName;
  }

  public Optional<T> extractOptionalValue(String cellContents) {
    return valueExtractor.apply(cellContents);
  }

  public T extractValueOrThrow(String cellContents) {
    return getOrThrow(
        valueExtractor.apply(cellContents),
        "Could not extract value for %s from cell contents %s",
        columnName, cellContents);
  }

  @Override
  public String toString() {
    return Strings.format("[CCE %s CCE]", columnName);
  }

}
