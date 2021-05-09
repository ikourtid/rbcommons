package com.rb.biz.types.asset;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.StringFunctions;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.time.LocalDate;
import java.util.List;

import static com.rb.biz.types.asset.CashId.CASH_ID;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static java.util.Collections.emptyList;

/**
 * The unique numeric ID of a tradable instrument.
 *
 * <p> In practice, an {@code InstrumentId} can represent individual stocks, ETFs, ETFns, or mutual funds.
 *
 * <p> Unlike a ticker symbol (e.g. QQQ), which can change for a security, the {@code InstrumentId} is supposed to be stable.
 * E.g. the Nasdaq 100 ETF used to be QQQ, then became QQQQ (4 Qs), then became QQQ again. In such a case,
 * the {@code InstrumentId} will be the same for the underlying security, despite the ticker changes.
 *
 * @see AssetId
 * @see CashId
 */
public class InstrumentId extends AssetId implements PrintsInstruments {

  public static final RBSet<InstrumentId> EMPTY_INSTRUMENT_ID_SET = emptyRBSet();
  public static final List<InstrumentId> EMPTY_INSTRUMENT_ID_LIST = emptyList();

  /**
   * {@code InstrumentIds} appear everywhere in the code, and there's only a few thousand of them, so it is good to
   * 'intern' them. Otherwise the code will be generating new {@code InstrumentID}s frequently.
   *
   * <p> We set the initial size of the cache to 3,000 instrument IDs, which is more than we'd ever need;
   * it's unlikely that we'll use more than ~1,000 in any backtest. But the space for this cache is cheap.
   * The 3k number is 30k (size of cache) * load factor of 0.1.
   */
  private static final TLongObjectHashMap<InstrumentId> INTERN_MAP = new TLongObjectHashMap<InstrumentId>(30_000, 0.1f);

  private final long rawId;

  private InstrumentId(long rawId) {
    this.rawId = rawId;
  }

  public static InstrumentId instrumentId(long rawId) {
    // This performance optimization allows us to return the instrument ID (when already existing)
    // without the need for locking. We only lock if there is no instrument ID.
    InstrumentId existingValue = INTERN_MAP.get(rawId);
    if (existingValue != null) {
      return existingValue;
    }

    synchronized (INTERN_MAP) {
      existingValue = INTERN_MAP.get(rawId);
      if (existingValue != null) {
        return existingValue;
      }
      InstrumentId instrumentId = instrumentIdAlwaysConstructed(rawId);
      INTERN_MAP.put(rawId, instrumentId);
      existingValue = instrumentId;
    }
    return existingValue;
  }

  private static InstrumentId instrumentIdAlwaysConstructed(long rawId) {
    if (rawId == CASH_ID) {
      throw new IllegalArgumentException(Strings.format(
          "ID %s cannot be used as an instrument ID; it is reserved to mean cash", rawId));
    }
    if (rawId < 0) {
      throw new IllegalArgumentException(Strings.format(
          "Instrument ID %s cannot be negative", rawId));
    }
    return new InstrumentId(rawId);
  }

  @Override
  public long asLong() {
    return rawId;
  }

  public String asString() {
    return Long.toString(rawId);
  }

  @Override
  public <T> T visit(AssetIdVisitor<T> visitor) {
    return visitor.visitInstrumentId(this);
  }

  @Override
  public String toString() {
    return Strings.format("iid %s", StringFunctions.withUnderscores(rawId));
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("%s (iid %s )",
        instrumentMaster.getLatestValidSymbolOrInstrumentIdAsSymbol(this, date),
        StringFunctions.withUnderscores(rawId));
  }

  // IDE-generated
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InstrumentId that = (InstrumentId) o;

    return rawId == that.rawId;
  }

  @Override
  public int hashCode() {
    // Integer.hashCode just returns the int itself. Makes sense; the hashcode doesn't make things more random.
    // Let's do it here as well.
    return (int) rawId;
  }

}
