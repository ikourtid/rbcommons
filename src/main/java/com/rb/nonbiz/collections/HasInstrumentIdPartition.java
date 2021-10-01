package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.AssetId;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.RBLog;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.function.BiConsumer;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.collections.HasInstrumentIdMaps.newHasInstrumentIdMap;
import static com.rb.nonbiz.collections.HasInstrumentIdMaps.singletonHasInstrumentIdMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBStreams.sumAsBigDecimals;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static com.rb.nonbiz.text.RBLog.rbLog;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

/**
 * <p> Use this whenever you want to represent having <i>N</i> items with proportions that sum to 1,
 * where the items all have a positive proportion. </p>
 *
 * {@code Partition<T>} is used all over the place. This is a specialized class similar to {@code Partition<T>}.
 * It was added in Sep 2018, after 300k lines of code were written, so it doesn't appear very frequently.
 * It is particularly useful in situations where there are a lot of instrument IDs as keys (e.g. a stock index membership).
 *
 * @see Partition
 * @see HasInstrumentIdMap
 */
public class HasInstrumentIdPartition<T extends HasInstrumentId> implements PrintsInstruments {

  private final static RBLog log = rbLog(HasInstrumentIdPartition.class);

  private final HasInstrumentIdMap<T, UnitFraction> rawMap;

  private HasInstrumentIdPartition(HasInstrumentIdMap<T, UnitFraction> rawMap) {
    this.rawMap = rawMap;
  }

  public static <T extends HasInstrumentId> HasInstrumentIdPartition<T> hasInstrumentIdPartition(
      HasInstrumentIdMap<T, UnitFraction> fractions) {
    fractions
        .valuesStream()
        .forEach(unitFraction -> RBPreconditions.checkArgument(
            !unitFraction.isZero(),
            "Fractions in partitions cannot be zero. If you don't want something, just don't put it into the partition"));
    double sum = sumAsBigDecimals(fractions.valuesStream()).doubleValue();
    RBPreconditions.checkArgument(
        Math.abs(sum - 1) <= 1e-8,
        "Fractions sum to %s which is not near 1 within an epsilon of 1e-8: %s",
        sum, fractions);
    return new HasInstrumentIdPartition<>(fractions);
  }

  public static <T extends HasInstrumentId> HasInstrumentIdPartition<T> singletonHasInstrumentIdPartition(
      T singleHasInstrumentId) {
    return new HasInstrumentIdPartition<>(singletonHasInstrumentIdMap(
        singleHasInstrumentId, UNIT_FRACTION_1));
  }

  /**
   * Starting from (double) weights that do not necessarily sum to one, create a partition with weights
   * normalized so that they sum to 1.
   */
  public static <T extends HasInstrumentId> HasInstrumentIdPartition<T> hasInstrumentIdPartitionFromWeights(
      HasInstrumentIdMap<T, Double> weightsMap) {
    // epsilon of 1e-12 is tighter than the usual 1e-8; note that doubles normally have precision of around 1e-14 to 1e-15.
    double e = 1e-12;
    double sum = weightsMap.valuesStream().mapToDouble(v -> v).sum();
    if (sum <= e) {
      throw new IllegalArgumentException(Strings.format("Sum of weights must be >0 (actually, 1e-12). Input was %s", weightsMap));
    }

    log.debug( () -> String.format("sum of weights %.4f %% normalization %.4f %%", 100 * sum, 100 / sum));
    MutableIidMap<Pair<T, UnitFraction>> mutableMap = newMutableIidMapWithExpectedSize(weightsMap.size());
    weightsMap.forEachEntry( (hasInstrumentId, weight) -> {
      if (Math.abs(weight) < e) {
        return;
      }
      RBPreconditions.checkArgument(
          weight > e,
          "Cannot create a PreciseValue weights partition if it includes a negative or zero weight of %s : input was %s",
          weight, weightsMap);
      mutableMap.putAssumingAbsent(hasInstrumentId.getInstrumentId(), pair(hasInstrumentId, unitFraction(weight / sum)));
    });
    return new HasInstrumentIdPartition<>(newHasInstrumentIdMap(mutableMap));
  }

  public Iterator<InstrumentId> instrumentIdIterator() {
    return rawMap.instrumentIdKeysIterator();
  }

  public IidSet getKeysAsIidSet() {
    return newIidSet(instrumentIdIterator(), rawMap.size());
  }

  /**
   * Use this if you don't care about the order you will iterate over the map; it's faster.
   * Otherwise, use forEachInInstrumentIdOrder.
   */
  public void forEachEntry(BiConsumer<T, UnitFraction> biConsumer) {
    rawMap.forEachEntry(biConsumer);
  }

  /**
   * Use forEachEntry instead, if you don't care about the order you will iterate over the map; it's faster.
   */
  public void forEachInInstrumentIdOrder(BiConsumer<T, UnitFraction> biConsumer) {
    rawMap.forEachInInstrumentIdOrder(biConsumer);
  }

  // This could have been called containsInstrumentId for clarity, but containsKey is standard for map-like classes.
  public boolean containsKey(InstrumentId instrumentId) {
    return rawMap.containsKey(instrumentId);
  }

  public UnitFraction getFractionOrThrow(InstrumentId instrumentId) {
    return rawMap.getValueOrThrow(
        instrumentId,
        "Key %s is not contained in partition's keys : %s",
        instrumentId, Joiner.on(',').join(rawMap.instrumentIdKeysIterator()));
  }

  @VisibleForTesting
  public HasInstrumentIdMap<T, UnitFraction> getRawFractionsMap() {
    return rawMap;
  }

  public UnitFraction getFractionOrZero(InstrumentId instrumentId) {
    return rawMap.getValueOrDefault(instrumentId, UNIT_FRACTION_0);
  }

  public int size() {
    return rawMap.size();
  }

  /**
   * Converts to a {@link Partition} by only keeping the {@link InstrumentId} portion of the {@link HasInstrumentId} key.
   * Of course, the resulting Partition will not have enough information in it to let us convert back to this
   * {@link HasInstrumentIdPartition}.
   */
  public Partition<InstrumentId> toInstrumentIdPartition() {
    return partition(rawMap.toIidMap().toRBMap());
  }

  /**
   * Converts to a {@link Partition} by only keeping the {@link InstrumentId} portion of the {@link HasInstrumentId} key.
   * Of course, the resulting Partition will not have enough information in it to let us convert back to this
   * {@link HasInstrumentIdPartition}.
   */
  public Partition<AssetId> toAssetIdPartition() {
    return partition(rawMap.toIidMap().toRBMap().transformKeysCopy(instrumentId -> (AssetId) instrumentId));
  }

  @Override
  public String toString() {
    return toString(0);
  }

  public String toString(int precision) {
    return toStringInDecreasingMembershipOrder(precision);
  }

  public String toStringInIncreasingInstrumentIdOrder(int precision) {
    return toStringInIncreasingInstrumentIdOrder(precision, NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  public String toStringInIncreasingInstrumentIdOrder(int precision, InstrumentMaster instrumentMaster, LocalDate date) {
    return Joiner.on(" ; ").join(
        rawMap.sortedInstrumentIdStream()
            .map(instrumentId -> Strings.format("%s %s",
                rawMap.getValueOrThrow(instrumentId).toPercentString(precision),
                rawMap.getHasInstrumentIdOrThrow(instrumentId).toString(instrumentMaster, date)))
            .iterator());
  }

  public String toStringInDecreasingMembershipOrder(int precision) {
    return toStringInDecreasingMembershipOrder(precision, NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  public String toStringInDecreasingMembershipOrder(int precision, InstrumentMaster instrumentMaster, LocalDate date) {
    return Joiner.on(" ; ").join(
        newArrayList(rawMap.instrumentIdKeysIterator())
            .stream()
            .sorted(comparing(instrumentId -> rawMap.getValueOrThrow(instrumentId).doubleValue(), reverseOrder()))
            .map(instrumentId -> Strings.format("%s %s",
                rawMap.getValueOrThrow(instrumentId).toPercentString(precision),
                rawMap.getHasInstrumentIdOrThrow(instrumentId).toString(instrumentMaster, date)))
            .iterator());
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return toStringInDecreasingMembershipOrder(0, instrumentMaster, date);
  }

}
