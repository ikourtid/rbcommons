package com.rb.biz.types.asset;

import com.rb.nonbiz.math.eigen.Investable;
import com.rb.nonbiz.text.PrintsInstruments;

/**
 * The base class for both {@link CashId} and {@link InstrumentId}.
 *
 * <p> {@code AssetId} is a concept that covers both instruments (securities) and cash.
 *
 * <p> This allows us to have target allocations that don't special-case cash.
 *
 * <p> Obviously, there are many instruments, but only one kind of cash.
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
