package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;
import java.util.Optional;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;

/**
 * Similar to {@link ClosedUnitFractionHardAndSoftRanges}.
 *
 * <p> This is the same, except that it's for the case where the map keys are {@link InstrumentId}.
 * It uses {@link IidMap} which is faster than {@link RBMap}. </p>
 *
 * @see ClosedUnitFractionHardAndSoftRange
 */
public class ClosedUnitFractionIidHardAndSoftRanges implements PrintsInstruments {

  private final IidMap<ClosedUnitFractionHardAndSoftRange> rawMap;

  private ClosedUnitFractionIidHardAndSoftRanges(
      IidMap<ClosedUnitFractionHardAndSoftRange> rawMap) {
    this.rawMap = rawMap;
  }

  public static  ClosedUnitFractionIidHardAndSoftRanges closedUnitFractionIidHardAndSoftRanges(
      IidMap<ClosedUnitFractionHardAndSoftRange> rawMap) {
    // The following is additionally very convenient when we convert back and forth from JSON,
    // because we do not like our API having empty entries just to represent unrestricted asset classes,
    // since that will likely happen a lot.
    RBPreconditions.checkArgument(
        rawMap.values().stream().noneMatch(range -> range.isUnrestricted()),
        "If a range is unrestricted, don't add it in: %s",
        rawMap);
    return new ClosedUnitFractionIidHardAndSoftRanges(rawMap);
  }

  public static  ClosedUnitFractionIidHardAndSoftRanges emptyClosedUnitFractionIidHardAndSoftRanges() {
    return closedUnitFractionIidHardAndSoftRanges(emptyIidMap());
  }

  public IidMap<ClosedUnitFractionHardAndSoftRange> getRawMap() {
    return rawMap;
  }

  public Optional<ClosedUnitFractionHardAndSoftRange> getOptionalHardAndSoftRange(InstrumentId instrumentId) {
    return rawMap.getOptional(instrumentId);
  }

  public IidSet getSharedKeys() {
    return rawMap.keySet();
  }

  public int size() {
    return rawMap.size();
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[CUFIHASR %s CUFIHASR]", rawMap.toString(instrumentMaster, date));
  }

}
