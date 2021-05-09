package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

import java.util.Comparator;
import java.util.OptionalInt;
import java.util.function.Function;

public class PartialComparisonResult {

  private final OptionalInt rawResult;

  private PartialComparisonResult(OptionalInt rawResult) {
    this.rawResult = rawResult;
  }

  public static PartialComparisonResult partialComparisonResult(OptionalInt rawResult) {
    return new PartialComparisonResult(rawResult);
  }

  public static PartialComparisonResult definedPartialComparison(int rawResult) {
    return new PartialComparisonResult(OptionalInt.of(rawResult));
  }

  /**
   * Using this constructor will often shorten the code a bit
   *
   * V is value, F is field
   */
  public static <V, F> PartialComparisonResult definedPartialComparison(
      V v1, V v2, Function<V, F> fieldExtractor, Comparator<F> comparator) {
    return new PartialComparisonResult(OptionalInt.of(comparator.compare(
        fieldExtractor.apply(v1), fieldExtractor.apply(v2))));
  }

  public static PartialComparisonResult noOrderingDefined() {
    return new PartialComparisonResult(OptionalInt.empty());
  }

  /**
   * Do not static import this, as it is not clear it refers to a PartialComparisonResult.
   */
  public static PartialComparisonResult equal() {
    return new PartialComparisonResult(OptionalInt.of(0));
  }

  /**
   * This can be any value that's &lt; 0, but if we don't have a reason to use a specific one,
   * we'll use this to make the code more readable.
   *
   * Do not static import this, as it is not clear it refers to a PartialComparisonResult.
   */
  public static PartialComparisonResult lessThan() {
    return new PartialComparisonResult(OptionalInt.of(-1));
  }

  /**
   * This can be any value that's &gt; 0, but if we don't have a reason to use a specific one,
   * we'll use this to make the code more readable.
   *
   * Do not static import this, as it is not clear it refers to a PartialComparisonResult.
   */
  public static PartialComparisonResult greaterThan() {
    return new PartialComparisonResult(OptionalInt.of(1));
  }

  public boolean isEqual() {
    return rawResult.isPresent() && rawResult.getAsInt() == 0;
  }

  public boolean isLessThan() {
    return rawResult.isPresent() && rawResult.getAsInt() < 0;
  }

  public boolean isLessThanOrEqualTo() {
    return rawResult.isPresent() && rawResult.getAsInt() <= 0;
  }

  public boolean isGreaterThan() {
    return rawResult.isPresent() && rawResult.getAsInt() > 0;
  }

  public boolean isGreaterThanOrEqualTo() {
    return rawResult.isPresent() && rawResult.getAsInt() > 0;
  }

  public boolean isDefined() {
    return rawResult.isPresent();
  }

  public static PartialComparisonResult orderingDefined(int rawOrdering) {
    return new PartialComparisonResult(OptionalInt.of(rawOrdering));
  }

  public OptionalInt getRawResult() {
    return rawResult;
  }

  @Override
  public String toString() {
    return Strings.format("[PCR %s PCR]", rawResult);
  }

}
