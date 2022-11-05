package com.rb.biz.types.asset;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.Symbol;

import java.time.LocalDate;

import static com.rb.biz.types.Symbol.symbol;

/**
 * The special {@link AssetId} for CASH.
 *
 * <p> This class was created to prevent bigger hacks. It allows us to avoid special-casing cash in the code.
 *
 * <p> Unlike {@link InstrumentId}, one cannot construct a {@link CashId}. The only available instance is
 * {@code CASH}, which is given the special numeric ID of {@code 0L}.
 *
 * @see AssetId
 * @see InstrumentId
 */
public class CashId extends AssetId {

  public static CashId CASH = new CashId();
  public static long CASH_ID = 0L;

  public static Symbol cashSymbol() {
    return symbol("$");
  }

  private CashId() {
    /* This exists to prevent instantiation via a default CashId() constructor */
  }

  @Override
  public <T> T visit(AssetIdVisitor<T> visitor) {
    return visitor.visitCash(this);
  }

  @Override
  public long asLong() {
    return CASH_ID;
  }

  @Override
  public String toString() {
    return "CASH";
  }

  @Override
  public String toString(InstrumentMaster ignoredInstrumentMaster, LocalDate ignoredDate) {
    // CashId extends AssetId, so it has to implement PrintsInstruments. Obviously, in the case of cash,
    // this is trivial. However, it's expedient to have CashId implement PrintsInstruments, so that if we ever want
    // to log some object such as a partition of AssetId, we can utilize methods such as Strings#format* ones.
    return toString();
  }

}
