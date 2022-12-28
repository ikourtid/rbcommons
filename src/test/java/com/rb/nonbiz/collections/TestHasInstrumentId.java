package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;

import java.time.LocalDate;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

/**
 * This helps tests avoid mentioning real production HasInstrumentId implementers,
 * in case we later decide to split off the biz vs nonbiz parts of the codebase.
 */
public class TestHasInstrumentId implements HasInstrumentId {

  private final InstrumentId instrumentId;
  private final double numericValue;

  private TestHasInstrumentId(InstrumentId instrumentId, double numericValue) {
    this.instrumentId = instrumentId;
    this.numericValue = numericValue;
  }

  public static TestHasInstrumentId testHasInstrumentId(InstrumentId instrumentId, double numericValue) {
    return new TestHasInstrumentId(instrumentId, numericValue);
  }

  @Override
  public InstrumentId getInstrumentId() {
    return instrumentId;
  }

  public double getNumericValue() {
    return numericValue;
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("%s %s",
        instrumentMaster.getLatestValidSymbolOrInstrumentIdAsSymbol(instrumentId, date), numericValue);
  }

  public static TypeSafeMatcher<TestHasInstrumentId> testHasInstrumentIdMatcher(TestHasInstrumentId expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getInstrumentId()),
        matchUsingDoubleAlmostEquals(v -> v.getNumericValue(), DEFAULT_EPSILON_1e_8));
  }

}
