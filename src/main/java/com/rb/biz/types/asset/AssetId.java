package com.rb.biz.types.asset;

import com.rb.nonbiz.math.eigen.Investable;
import com.rb.nonbiz.text.PrintsInstruments;

/**
 * The base class for both {@link CashId} and {@link InstrumentId}.
 *
 * <p> {@code AssetId} is a concept that covers both instruments (securities) and cash.
 * This allows us to have target allocations that don't special-case cash. However, in some cases, we only want to
 * limit ourselves to cash. For example, an order to trade can never mention cash; we can't trade cash, whereas we
 * can trade {@link InstrumentId}s. </p>
 *
 * <p> There are many instances of {@link InstrumentId}, but only one kind of cash. </p>
 *
 * @see CashId
 * @see InstrumentId
 */
public abstract class AssetId implements Investable, PrintsInstruments {

  public interface AssetIdVisitor<T> {

    T visitInstrumentId(InstrumentId instrumentId);
    T visitCash(CashId cash);

  }

  public abstract <T> T visit(AssetIdVisitor<T> visitor);

}
