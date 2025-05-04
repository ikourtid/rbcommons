package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.collections.IidGroupings.emptyIidGroupings;
import static com.rb.nonbiz.collections.IidGroupings.iidGroupings;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.IidSetWithGroupings.IidSetForSingleGrouping.iidSetForSingleGrouping;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableIidSet.newMutableIidSetWithExpectedSize;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static com.rb.nonbiz.text.Strings.formatIidMap;

/**
 * See {@link IidMapWithGroupings}. This is just for the case where there are no values, and we only care
 * about the groupings within the instruments.
 */
public class IidSetWithGroupings<S extends HasNonEmptyIidSet> implements PrintsInstruments {


  /**
   * Say we have an {@link IidSetWithGroupings} that has values for instruments {A1, A2, B1, B2},
   * and groups together { A1, A2 } and { B1, B2 }. If so, then this would contain an {@link IidMap} with entries
   * for A1 and A2, and also the {@link HasNonEmptyIidSet} object that describes the grouping { A1, A2 }.
   *
   * <p> The reason we need the grouping is that the {@link IidMap} is also allowed to contain fewer entries;
   * it doesn't have to have one entry per instrument in the {@link HasNonEmptyIidSet} grouping. </p>
   */
  public static class IidSetForSingleGrouping<S extends HasNonEmptyIidSet> implements PrintsInstruments {

    private final IidSet iidSet;
    private final S iidGrouping;

    private IidSetForSingleGrouping(IidSet iidSet, S iidGrouping) {
      this.iidSet = iidSet;
      this.iidGrouping = iidGrouping;
    }

    static <S extends HasNonEmptyIidSet> IidSetForSingleGrouping<S> iidSetForSingleGrouping(
        IidSet iidSet, S iidGrouping) {
      iidSet.forEach( instrumentId ->
          RBPreconditions.checkArgument(
              iidGrouping.contains(instrumentId),
              "%s is not in the grouping %s ; set was %s",
              instrumentId, iidGrouping, iidSet));
      return new IidSetForSingleGrouping<>(iidSet, iidGrouping);
    }

    public IidSet getIidSet() {
      return iidSet;
    }

    public S getIidGrouping() {
      return iidGrouping;
    }

    @Override
    public String toString() {
      return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
    }

    @Override
    public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
      return Strings.format("[ISFSG %s %s ISFSG]",
          iidSet.toString(instrumentMaster, date),
          iidGrouping.toString(instrumentMaster, date));
    }

  }


  private final IidSet topLevelIidSet;
  private final IidGroupings<S> iidGroupings;
  private final IidMap<IidSetForSingleGrouping<S>> groupedIidMap;

  private IidSetWithGroupings(
      IidSet topLevelIidSet,
      IidGroupings<S> iidGroupings,
      IidMap<IidSetForSingleGrouping<S>> groupedIidMap) {
    this.topLevelIidSet = topLevelIidSet;
    this.iidGroupings = iidGroupings;
    this.groupedIidMap = groupedIidMap;
  }

  /**
   * Use this constructor for cases where, if an instrument appears in the top-level {@link IidSet} but not in the
   * {@link IidGroupings}, you want to just create a trivial grouping on the fly.
   *
   * <p> For example, 'substantially identical' (per IRS rules) may include pairs or triples such as
   * { VOO, SPY } (both S&P 500 ETFs). However, any individual stock could be seen as 'substantially identical'
   * to itself. So even if the input data does not specify this trivial relationship (and it probably shouldn't),
   * we should construct it on the fly, so that calling code won't have to special-case situations where there
   * is only one instrument in the (trivial) 'substantially identical' relationship. </p>
   */
  public static <S extends HasNonEmptyIidSet> IidSetWithGroupings<S> iidSetWithGroupings(
      IidSet topLevelIidSet,
      IidGroupings<S> iidGroupings,
      Function<InstrumentId, S> trivialGroupingGenerator) {
    IidGroupings<S> includingAdditionalIidGroupings = iidGroupings(
        Stream.concat(
                iidGroupings.getRawList().stream(),
                topLevelIidSet
                    .stream()
                    .filter(instrumentId -> !iidGroupings.containsInstrument(instrumentId))
                    .map(instrumentId -> trivialGroupingGenerator.apply(instrumentId)))
            .collect(Collectors.toList()));
    return iidSetWithGroupingsHelper(topLevelIidSet, includingAdditionalIidGroupings);
  }

  /**
   * Use this constructor for cases where, if an instrument appears in the top-level {@link IidMap} but not in the
   * {@link IidGroupings}, you want to throw an exception, instead of just creating a trivial grouping on the fly.
   * If you want the latter, use the other constructor.
   */
  public static <S extends HasNonEmptyIidSet> IidSetWithGroupings<S> iidSetWithGroupings(
      IidSet topLevelIidSet,
      IidGroupings<S> iidGroupings) {
    topLevelIidSet.forEach(instrumentId -> RBPreconditions.checkArgument(
        iidGroupings.containsInstrument(instrumentId),
        "IidSet contains instrument %s which is not contained in any grouping: %s %s",
        instrumentId, iidGroupings, topLevelIidSet));
    return iidSetWithGroupingsHelper(topLevelIidSet, iidGroupings);
  }

  private static <S extends HasNonEmptyIidSet> IidSetWithGroupings<S> iidSetWithGroupingsHelper(
      IidSet topLevelIidSet,
      IidGroupings<S> iidGroupings) {
    MutableIidMap<IidSetForSingleGrouping<S>> mutableMapForAllGroupings =
        newMutableIidMapWithExpectedSize(iidGroupings.getRawList().size());
    iidGroupings.getRawList().forEach(iidGrouping -> {
      MutableIidSet mutableSetForSingleGrouping =
          newMutableIidSetWithExpectedSize(iidGrouping.getIidSet().size());
      iidGrouping.getIidSet().forEach(instrumentId -> {
        if (topLevelIidSet.contains(instrumentId)) {
          mutableSetForSingleGrouping.addAssumingAbsent(instrumentId);
        }
      });

      if (!mutableSetForSingleGrouping.isEmpty()) {
        IidSet iidSubSet = newIidSet(mutableSetForSingleGrouping);
        IidSetForSingleGrouping<S> iidSetForSingleGrouping =
            iidSetForSingleGrouping(
                iidSubSet,
                iidGrouping);
        iidSubSet.forEach(instrumentId ->
            mutableMapForAllGroupings.putAssumingAbsent(instrumentId, iidSetForSingleGrouping));
      }
    });
    return new IidSetWithGroupings<>(topLevelIidSet, iidGroupings, newIidMap(mutableMapForAllGroupings));
  }

  public static <S extends HasNonEmptyIidSet> IidSetWithGroupings<S> emptyIidSetWithGroupings() {
    return iidSetWithGroupings(emptyIidSet(), emptyIidGroupings());
  }

  public IidSet getTopLevelIidSet() {
    return topLevelIidSet;
  }

  public IidGroupings<S> getIidGroupings() {
    return iidGroupings;
  }

  public IidMap<IidSetForSingleGrouping<S>> getGroupedIidMap() {
    return groupedIidMap;
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[ISWG %s %s %s ISWG]",
        topLevelIidSet.toString(instrumentMaster, date),
        iidGroupings.toString(instrumentMaster, date),
        groupedIidMap.toString(instrumentMaster, date));
  }


}
