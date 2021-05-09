package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RationalUnitFraction;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.types.RationalUnitFraction.rationalUnitFraction;

/**
 * When we compute returns for the purpose of eigendecomposition, it's nice to have a mechanism
 * to record the normal cases (day 1 {@code ->} day 2 prices both available), vs returns filled due to halts,
 * or due to back-filling historical returns for days on and before the 1st day of prices.
 */
public class SingleItemQualityOfReturns<T extends Investable> {

  private final T key;
  private final int numActual;
  private final int numGapFilled;

  // This includes any history backfills we may do for the first day of PRICES, since the first day of
  // RETURNS is the next day (since we need a previous price). It also includes any previous returns (e.g.
  // averages of the whole market) we use to make up for the fact that there are no returns before the start
  // of an instrument's first day when it starts trading.
  private final int numBackFilled;

  private SingleItemQualityOfReturns(T key, int numActual, int numGapFilled, int numBackFilled) {
    this.key = key;
    this.numActual = numActual;
    this.numGapFilled = numGapFilled;
    this.numBackFilled = numBackFilled;
  }

  /**
   * This does not include gap-filled. E.g. if we have returns for days 1, 2, 3, 4, 5
   * but there was a price halt on day 3 that cause the returns of days 3 and 4 to be gap-filled,
   * then #getNumActual will return 3, and #getNumGapFilled will return 2.
   */
  public int getNumActual() {
    return numActual;
  }

  public int getNumGapFilled() {
    return numGapFilled;
  }

  public int getNumBackFilled() {
    return numBackFilled;
  }

  public RationalUnitFraction getBackFilledFraction() {
    return rationalUnitFraction(numBackFilled, numBackFilled + numGapFilled + numActual);
  }

  public T getKey() {
    return key;
  }

  @Override
  public String toString() {
    return Strings.format("[GFS %s actual= %s ; gap-filled due to bad prices= %s ; back-filled for history= %s GFS]",
        key, numActual, numGapFilled, numBackFilled);
  }

  public static class SingleItemQualityOfReturnsBuilder<T extends Investable> implements RBBuilder<SingleItemQualityOfReturns<T>> {

    private T key;
    private Integer numActual;
    private Integer numGapFilled;
    private Integer numBackFilled;

    private SingleItemQualityOfReturnsBuilder() { }

    public static <T extends Investable> SingleItemQualityOfReturnsBuilder<T> singleItemQualityOfReturnsBuilder(T key) {
      SingleItemQualityOfReturnsBuilder singleItemQualityOfReturnsBuilder = new SingleItemQualityOfReturnsBuilder();
      singleItemQualityOfReturnsBuilder.key = key;
      return singleItemQualityOfReturnsBuilder;
    }

    public SingleItemQualityOfReturnsBuilder<T> setNumActual(int numActual) {
      this.numActual = numActual;
      return this;
    }

    public SingleItemQualityOfReturnsBuilder<T> setNumGapFilled(int numGapFilled) {
      this.numGapFilled = numGapFilled;
      return this;
    }

    public SingleItemQualityOfReturnsBuilder<T> setNumBackFilled(int numBackFilled) {
      this.numBackFilled = numBackFilled;
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(key);
      RBPreconditions.checkNotNull(numActual);
      RBPreconditions.checkNotNull(numGapFilled);
      RBPreconditions.checkNotNull(numBackFilled);
      RBPreconditions.checkArgument(
          numActual + numGapFilled > 0,
          "%s data consists of no actual/gap-filled data; just %s backfills",
          key, numBackFilled);
    }

    @Override
    public SingleItemQualityOfReturns<T> buildWithoutPreconditions() {
      return new SingleItemQualityOfReturns(
          key, numActual, numGapFilled, numBackFilled);
    }

  }

}
