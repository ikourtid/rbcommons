package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static java.util.Collections.emptyList;

/**
 * A collection of {@link HasIidSet} objects that are mutually disjoint with respect to the {@link InstrumentId}s.
 *
 * <p> For example, we could have two {@link HasIidSet}s (two groupings) of { i1, i2 }, { i3, i4, i5 }. </p>
 *
 * <p> The groupings must be mutually disjoint. </p>
 *
 * <p> The original impetus for creating this is for modeling IRS 'substantially identical' relationships.
 * However, it's a general enough concept. </p>
 *
 * <p> We use a list internally, even though there is no explicit ordering. That's because we don't like to
 * use RBSet with items that cannot be keyed (i.e. for which we have not implemented a nontrivial hashCode / equals).
 * </p>
 */
public class IidGroupings<S extends HasNonEmptyIidSet> implements PrintsInstruments {

  private final IidMap<S> rawMap;
  private final List<S> rawList;

  private IidGroupings(IidMap<S> rawMap, List<S> rawList) {
    this.rawMap = rawMap;
    this.rawList = rawList;
  }

  public static <S extends HasNonEmptyIidSet> IidGroupings<S> iidGroupings(List<S> allHasIidSets) {
    MutableIidMap<S> mutableMap = newMutableIidMapWithExpectedSize(
        allHasIidSets.stream().mapToInt(v -> v.getIidSet().size()).sum());
    allHasIidSets.forEach(hasIidSet -> {
      // putAssumingAbsent also guarantees that no single InstrumentId will appear in more than one hasIidSet,
      // therefore guaranteeing mutual exclusivity.
      IidSet iidSet = hasIidSet.getIidSet();
      RBPreconditions.checkArgument(
          !iidSet.isEmpty(),
          "Cannot have an empty IidSet inside %s",
          allHasIidSets);
      iidSet.forEach(iid -> mutableMap.putAssumingAbsent(iid, hasIidSet));
    });

    return new IidGroupings<>(newIidMap(mutableMap), allHasIidSets);
  }

  public static <S extends HasNonEmptyIidSet> IidGroupings<S> emptyIidGroupings() {
    return iidGroupings(emptyList());
  }

  public IidMap<S> getRawMap() {
    return rawMap;
  }

  public List<S> getRawList() {
    return rawList;
  }

  public boolean containsInstrument(InstrumentId instrumentId) {
    return rawMap.containsKey(instrumentId);
  }

  /**
   * E.g. if there's a grouping for instruments A1, A2, A3, then calling this on A2 will return {A1, A2, A3}.
   * And if the grouping is just { A2 }, then it will return { A2 }.
   *
   * <p> Returns empty optional if there is no grouping for this instrument. </p>
   */
  public Optional<IidSet> getOptionalSiblingsIncludingSelf(InstrumentId instrumentId) {
    return transformOptional(
        rawMap.getOptional(instrumentId),
        v -> v.getIidSet());
  }

  /**
   * E.g. if there's a grouping for instruments A1, A2, A3, then calling this on A2 will return {A1, A3}.
   * And if the grouping is just { A2 }, then it will return { }.
   *
   * <p> Returns empty optional if there is no grouping for this instrument. </p>
   */
  public Optional<IidSet> getOptionalSiblingsExcludingSelf(InstrumentId instrumentId) {
    return transformOptional(
        rawMap.getOptional(instrumentId),
        v -> v.getIidSet().filterOut(instrumentId));
  }

  /**
   * E.g. if there's a grouping for instruments A1, A2, A3, then calling this on A2 will return {A1, A2, A3}.
   * And if the grouping is just { A2 }, then it will return { A2 }.
   *
   * <p> Throws if there is no grouping for this instrument. </p>
   */
  public IidSet getSiblingsIncludingSelfOrThrow(InstrumentId instrumentId) {
    return rawMap.getOrThrow(instrumentId).getIidSet();
  }

  /**
   * E.g. if there's a grouping for instruments A1, A2, A3, then calling this on A2 will return {A1, A3}.
   * And if the grouping is just { A2 }, then it will return { }.
   *
   * <p> Throws if there is no grouping for this instrument. </p>
   */
  public IidSet getSiblingsExcludingSelfOrThrow(InstrumentId instrumentId) {
    return rawMap.getOrThrow(instrumentId).getIidSet().filterOut(instrumentId);
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[IG allHasIidSets= %s ; rawMap= %s IG]", rawList, rawMap);
  }

}
