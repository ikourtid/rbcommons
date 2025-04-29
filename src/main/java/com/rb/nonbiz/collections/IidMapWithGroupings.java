package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.collections.IidGroupings.emptyIidGroupings;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static com.rb.nonbiz.text.Strings.formatIidMap;

/**
 * An {@link IidMap} where the instrument keys are meant to be grouped together in disjoint groups,
 * as per {@link IidGroupings}.
 *
 * <p> Although this is a business-level concept so it's a weird example to give here,
 * the first use of this is to represent tax lots of instruments that are recorded per instrument,
 * but also grouped together by 'substantially identical' (per IRS rules) securities. </p>
 *
 * <p> That is, this can store tax lots for SPY and VOO (both S&P 500 ETFs, so arguably 'substantially identical')
 * but also let us see SPY and VOO together. Same for other groupings (i.e. this doesn't only store a single
 * grouping). </p>
 */
public class IidMapWithGroupings<V, S extends HasIidSet> implements PrintsInstruments {

  private final IidMap<V> iidMap;
  private final IidGroupings<S> iidGroupings;

  private IidMapWithGroupings(IidMap<V> iidMap, IidGroupings<S> iidGroupings) {
    this.iidMap = iidMap;
    this.iidGroupings = iidGroupings;
  }

  public static <V, S extends HasIidSet> IidMapWithGroupings<V, S> iidMapWithGroupings(
      IidMap<V> iidMap, IidGroupings<S> iidGroupings) {
    return new IidMapWithGroupings<>(iidMap, iidGroupings);
  }

  public static <V, S extends HasIidSet> IidMapWithGroupings<V, S> emptyIidMapWithGroupings() {
    return iidMapWithGroupings(emptyIidMap(), emptyIidGroupings());
  }

  public IidMap<V> getIidMap() {
    return iidMap;
  }

  public IidGroupings<S> getIidGroupings() {
    return iidGroupings;
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[IMWG %s %s IMWG]",
        formatIidMap(iidMap, instrumentMaster, date),
        iidGroupings.toString(instrumentMaster, date));
  }

}
