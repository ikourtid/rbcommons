package com.rb.nonbiz.collections;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.AssetId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.rb.nonbiz.collections.RBRanges.hasEitherBoundOpen;
import static com.rb.nonbiz.collections.RBRanges.rangeIsUnrestricted;
import static com.rb.nonbiz.text.Strings.formatRange;

/**
 * Represents a general expression such as e.g. {@literal 0.4 * AAPL - 0.1 * GOOG > 12.34 } (although it's generic,
 * so the value type could be anything).
 *
 * <p> Although this data class is general, its first use is to have it convert eventually to a constraint inside an
 * optimization.
 * Therefore, the supplied {@link Range} cannot be fully unrestricted (because that wouldn't translate to a
 * constraint). Also, for any (or both) endpoints supplied, they have to be {@link BoundType#CLOSED} for
 * clarity, because an optimization works with epsilons, and there's no distinction between inequality
 * and strict inequality. </p>
 *
 * <p> Note that this class can be used to generate a constraint, but is not intended to directly represent a
 * constraint. That is, the value type in the generic specification could be e.g. {@link AssetId},
 * not necessarily some low-level class such as RawVariable (in higher repo) that is directly involved in an
 * optimization. </p>
 */
public class FlatSignedLinearCombinationWithRange<T> {

  private final FlatSignedLinearCombination<T> flatSignedLinearCombination;
  private final Range<BigDecimal> range;

  private FlatSignedLinearCombinationWithRange(
      FlatSignedLinearCombination<T> flatSignedLinearCombination,
      Range<BigDecimal> range) {

    this.flatSignedLinearCombination = flatSignedLinearCombination;
    this.range = range;
  }

  public static <T> FlatSignedLinearCombinationWithRange<T> flatSignedLinearCombinationWithRange(
      FlatSignedLinearCombination<T> flatSignedLinearCombination,
      Range<BigDecimal> range) {
    RBPreconditions.checkArgument(
        !rangeIsUnrestricted(range),
        "Range %s cannot be unrestricted; flatSignedLinearCombination= %s",
        range, flatSignedLinearCombination);
    RBPreconditions.checkArgument(
        !hasEitherBoundOpen(range),
        "Range %s cannot have either bound be open; flatSignedLinearCombination= %s",
        range, flatSignedLinearCombination);
    return new FlatSignedLinearCombinationWithRange<>(flatSignedLinearCombination, range);
  }

  public FlatSignedLinearCombination<T> getFlatSignedLinearCombination() {
    return flatSignedLinearCombination;
  }

  public Range<BigDecimal> getRange() {
    return range;
  }

  /**
   * Because this class is generic, there's no way to specify code that's specific to the cases where
   * the generic type implements {@link PrintsInstruments}. Therefore, we'll use a static method instead.
   */
  public static <T extends PrintsInstruments> String toString(
      FlatSignedLinearCombinationWithRange<T> flatSignedLinearCombinationWithRange,
      InstrumentMaster instrumentMaster,
      LocalDate date) {
    return Strings.format("[FSLCWR range= %s ; expression = %s FSLCWR]",
        formatRange(flatSignedLinearCombinationWithRange.getRange()),
        FlatSignedLinearCombination.toString(
            flatSignedLinearCombinationWithRange.getFlatSignedLinearCombination(),
            instrumentMaster,
            date));
  }

  @Override
  public String toString() {
    return Strings.format("[FSLCWR %s %s FSLCWR]", flatSignedLinearCombination, range);
  }

}
