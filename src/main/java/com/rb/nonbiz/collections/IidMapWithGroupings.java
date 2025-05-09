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
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidMapWithGroupings.IidMapForSingleGrouping.iidMapForSingleGrouping;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
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
 * <p> That is, this could be used to e.g. store tax lots for SPY and VOO individually,
 * (both S&P 500 ETFs, so arguably 'substantially identical')
 * but also let us see SPY and VOO grouped together.
 * Same for other groupings such as e.g. SCHB and VTI (broad-market ETFs).
 * That is, this data class doesn't only allow a single grouping. </p>
 *
 * <p> In a way, this creates a partition (in the math sense, not in the data class {@link Partition} sense)
 * within the {@link IidMap}. We decided against calling this e.g. PartitionedIidMap because it may sound
 * too similar to our more specific {@link Partition} class. </p>
 */
public class IidMapWithGroupings<V, S extends HasNonEmptyIidSet> implements PrintsInstruments {


  /**
   * Say we have an {@link IidMapWithGroupings} that has values for instruments {A1, A2, B1, B2},
   * and groups together { A1, A2 } and { B1, B2 }. If so, then this would contain an {@link IidMap} with entries
   * for A1 and A2, and also the {@link HasNonEmptyIidSet} object that describes the grouping { A1, A2 }.
   *
   * <p> The reason we need the grouping is that the {@link IidMap} is also allowed to contain fewer entries;
   * it doesn't have to have one entry per instrument in the {@link HasNonEmptyIidSet} grouping. </p>
   */
  public static class IidMapForSingleGrouping<V, S extends HasNonEmptyIidSet> implements PrintsInstruments {

    private final IidMap<V> iidMap;
    private final S iidGrouping;

    private IidMapForSingleGrouping(IidMap<V> iidMap, S iidGrouping) {
      this.iidMap = iidMap;
      this.iidGrouping = iidGrouping;
    }

    public static <V, S extends HasNonEmptyIidSet> IidMapForSingleGrouping<V, S> iidMapForSingleGrouping(
        IidMap<V> iidMap, S iidGrouping) {
      iidMap.instrumentIdStream().forEach( instrumentId ->
          RBPreconditions.checkArgument(
              iidGrouping.contains(instrumentId),
              "%s is not in the grouping %s ; map was %s",
              instrumentId, iidGrouping, iidMap));
      return new IidMapForSingleGrouping<>(iidMap, iidGrouping);
    }

    public IidMap<V> getIidMap() {
      return iidMap;
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
      return Strings.format("[IMFSG %s %s IMFSG]",
          iidMap.toString(instrumentMaster, date),
          iidGrouping.toString(instrumentMaster, date));
    }

  }


  private final IidMap<V> topLevelIidMap;
  private final IidGroupings<S> iidGroupings;
  private final IidMap<IidMapForSingleGrouping<V, S>> groupedIidMap;

  private IidMapWithGroupings(
      IidMap<V> topLevelIidMap,
      IidGroupings<S> iidGroupings,
      IidMap<IidMapForSingleGrouping<V, S>> groupedIidMap) {
    this.topLevelIidMap = topLevelIidMap;
    this.iidGroupings = iidGroupings;
    this.groupedIidMap = groupedIidMap;
  }

  /**
   * Use this constructor for cases where, if an instrument appears in the top-level {@link IidMap} but not in the
   * {@link IidGroupings}, you want to just create a trivial grouping on the fly.
   *
   * <p> For example, 'substantially identical' (per IRS rules) may include pairs or triples such as
   * { VOO, SPY } (both S&P 500 ETFs). However, any individual stock could be seen as 'substantially identical'
   * to itself. So even if the input data does not specify this trivial relationship (and it probably shouldn't),
   * we should construct it on the fly, so that calling code won't have to special-case situations where there
   * is only one instrument in the (trivial) 'substantially identical' relationship. </p>
   */
  public static <V, S extends HasNonEmptyIidSet> IidMapWithGroupings<V, S> iidMapWithGroupings(
      IidMap<V> topLevelIidMap,
      IidGroupings<S> iidGroupings,
      Function<InstrumentId, S> trivialGroupingGenerator) {
    IidGroupings<S> includingAdditionalIidGroupings = iidGroupings(
        Stream.concat(
                iidGroupings.getRawList().stream(),
                topLevelIidMap.keySet()
                    .stream()
                    .filter(instrumentId -> !iidGroupings.containsInstrument(instrumentId))
                    .map(instrumentId -> trivialGroupingGenerator.apply(instrumentId)))
            .collect(Collectors.toList()));
    return iidMapWithGroupingsHelper(topLevelIidMap, includingAdditionalIidGroupings);
  }

  /**
   * Use this constructor for cases where, if an instrument appears in the top-level {@link IidMap} but not in the
   * {@link IidGroupings}, you want to throw an exception, instead of just creating a trivial grouping on the fly.
   * If you want the latter, use the other constructor.
   */
  public static <V, S extends HasNonEmptyIidSet> IidMapWithGroupings<V, S> iidMapWithGroupings(
      IidMap<V> topLevelIidMap,
      IidGroupings<S> iidGroupings) {
    topLevelIidMap.keySet().forEach(instrumentId -> RBPreconditions.checkArgument(
        iidGroupings.containsInstrument(instrumentId),
        "Map contains instrument %s which is not contained in any grouping: %s %s",
        instrumentId, iidGroupings, topLevelIidMap));
    return iidMapWithGroupingsHelper(topLevelIidMap, iidGroupings);
  }

  private static <V, S extends HasNonEmptyIidSet> IidMapWithGroupings<V, S> iidMapWithGroupingsHelper(
      IidMap<V> topLevelIidMap,
      IidGroupings<S> iidGroupings) {
    MutableIidMap<IidMapForSingleGrouping<V, S>> mutableMapForAllGroupings =
        newMutableIidMapWithExpectedSize(iidGroupings.getRawList().size());
    iidGroupings.getRawList().forEach(iidGrouping -> {
      MutableIidMap<V> mutableMapForSingleGrouping =
          newMutableIidMapWithExpectedSize(iidGrouping.getIidSet().size());
      iidGrouping.getIidSet().forEach(instrumentId ->
          topLevelIidMap.getOptional(instrumentId).ifPresent(value ->
              mutableMapForSingleGrouping.putAssumingAbsent(instrumentId, value)));

      if (!mutableMapForSingleGrouping.isEmpty()) {
        IidMap<V> iidSubMap = newIidMap(mutableMapForSingleGrouping);
        IidMapForSingleGrouping<V, S> iidMapForSingleGrouping =
            iidMapForSingleGrouping(
                iidSubMap,
                iidGrouping);
        iidSubMap.instrumentIdStream().forEach(instrumentId ->
            mutableMapForAllGroupings.putAssumingAbsent(instrumentId, iidMapForSingleGrouping));
      }
    });
    return new IidMapWithGroupings<>(topLevelIidMap, iidGroupings, newIidMap(mutableMapForAllGroupings));
  }

  public static <V, S extends HasNonEmptyIidSet> IidMapWithGroupings<V, S> emptyIidMapWithGroupings() {
    return iidMapWithGroupings(emptyIidMap(), emptyIidGroupings());
  }

  public IidMap<V> getTopLevelIidMap() {
    return topLevelIidMap;
  }

  public IidGroupings<S> getIidGroupings() {
    return iidGroupings;
  }

  public IidMap<IidMapForSingleGrouping<V, S>> getGroupedIidMap() {
    return groupedIidMap;
  }

  /**
   * E.g. if you pass in a grouping of {A1, A2, A3}, and the top-level map has values for A1, A3, B1,
   * this will return the subset of the top-level map with A1 and A3.
   *
   * <p> Note that this data class does not expect that type S will implement a non-trivial
   * (i.e. not just a pointer comparison) hashCode / equals. Therefore, we can't confirm that grouping S
   * already exists. Well, technically we can confirm that there is another grouping that contains the exact same
   * set of instruments, even if the rest of the object S (which could have more members) is not equal.
   * But to keep things simple, we won't check for that case. </p>
   */
  public IidMap<V> getForIidGrouping(S iidGrouping) {
    return iidGrouping.getIidSet()
        .filter(v -> topLevelIidMap.containsKey(v))
        .toIidMap(v -> topLevelIidMap.getOrThrow(v));
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[IMWG %s %s %s IMWG]",
        formatIidMap(topLevelIidMap, instrumentMaster, date),
        iidGroupings.toString(instrumentMaster, date),
        groupedIidMap.toString(instrumentMaster, date));
  }


}
