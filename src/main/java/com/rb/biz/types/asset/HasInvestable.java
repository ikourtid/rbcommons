package com.rb.biz.types.asset;

import com.rb.nonbiz.math.eigen.Investable;

/**
 * Useful for data classes that pertain to a single {@link Investable}.
 *
 * <p> E.g. a buy amount can be for an {@link AssetId} (e.g. we bought this dollar amount of a stock)
 * or for an {@code AssetClass} (e.g. we bought this dollar amount of an asset class).
 *
 * @see Investable
 * @see HasInstrumentId
 */
public interface HasInvestable<T extends Investable> {

  T getInvestable();

}
