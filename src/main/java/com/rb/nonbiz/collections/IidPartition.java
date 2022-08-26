package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBStreams.sumAsBigDecimals;
import static com.rb.nonbiz.types.PreciseValue.sumToBigDecimal;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

/**
 * Use this whenever you want to represent having <i>N</i> items with proportions that sum to 1,
 * where the items all have a positive proportion.
 *
 * <p> {@code Partition<T>} is used all over the place. This is a specialized class similar to {@code Partition<T>}.
 * It was added in Sep 2018, after 300k lines of code were written, so it doesn't appear very frequently.
 * It is particularly useful in situations where there are a lot of instrument IDs as keys (e.g. a stock index membership). </p>
 *
 * @see Partition
 */
public class IidPartition implements PrintsInstruments {

  private final IidMap<UnitFraction> fractions;

  private IidPartition(IidMap<UnitFraction> fractions) {
    this.fractions = fractions;
  }

  public static IidPartition iidPartition(IidMap<UnitFraction> fractions) {
    for (UnitFraction unitFraction : fractions.values()) {
      if (unitFraction.isZero()) {
        throw new IllegalArgumentException(
            "Fractions in partitions cannot be zero. If you don't want something, just don't put it into the partition");
      }
    }
    double sum = sumAsBigDecimals(fractions.values()).doubleValue();
    RBPreconditions.checkArgument(
        Math.abs(sum - 1) <= 1e-8,
        "Fractions sum to %s which is not near 1 within an epsilon of 1e-8: %s",
        sum, fractions);
    return new IidPartition(fractions);
  }

  public static IidPartition singletonIidPartition(InstrumentId singleInstrumentId) {
    return new IidPartition(singletonIidMap(
        singleInstrumentId, UNIT_FRACTION_1));
  }

  public static <V extends PreciseValue<V>> IidPartition iidPartitionFromWeights(IidMap<V> weightsMap) {
    BigDecimal sum = sumToBigDecimal(weightsMap.values());
    if (sum.signum() != 1) {
      throw new IllegalArgumentException(Strings.format("Sum of weights must be >0. Input was %s", weightsMap));
    }
    MutableIidMap<UnitFraction> fractionsMap = newMutableIidMapWithExpectedSize(weightsMap.size());
    weightsMap.forEachEntry( (key, value) -> {
      BigDecimal bd = value.asBigDecimal();
      if (bd.signum() == 0) {
        return;
      }
      RBPreconditions.checkArgument(
          bd.signum() == 1,
          "Cannot create a PreciseValue weights IidPartition if it includes a negative or zero weight of %s : input was %s",
          bd, weightsMap);
      fractionsMap.putAssumingAbsent(key, unitFraction(bd.divide(sum, DEFAULT_MATH_CONTEXT)));
    });
    return new IidPartition(newIidMap(fractionsMap));
  }

  public boolean containsKey(InstrumentId instrumentId) {
    return fractions.containsKey(instrumentId);
  }

  public UnitFraction getFractionOrThrow(InstrumentId instrumentId) {
    return fractions.getOrThrow(
        instrumentId,
        "Key %s is not contained in partition's keys : %s",
        instrumentId, Joiner.on(',').join(fractions.instrumentIdKeysIterator()));
  }

  @VisibleForTesting
  public IidMap<UnitFraction> getRawFractionsMap() {
    return fractions;
  }

  public UnitFraction getOrZero(InstrumentId instrumentId) {
    return fractions.getOrDefault(instrumentId, UNIT_FRACTION_0);
  }

  public int size() {
    return fractions.size();
  }

  @Override
  public String toString() {
    return toString(0);
  }

  public String toString(int precision) {
    return toStringInDecreasingMembershipOrder(precision, instrumentId -> instrumentId.toString());
  }

  public String toStringInIncreasingInstrumentIdOrder(int precision) {
    return toStringInIncreasingInstrumentIdOrder(precision, instrumentId -> instrumentId.toString());
  }

  public String toStringInIncreasingInstrumentIdOrder(int precision, InstrumentMaster instrumentMaster, LocalDate date) {
    return toStringInIncreasingInstrumentIdOrder(
        precision,
        instrumentId -> instrumentMaster.getLatestValidSymbolOrInstrumentIdAsSymbol(instrumentId, date).toString());
  }

  public String toStringInIncreasingInstrumentIdOrder(int precision, Function<InstrumentId, String> iidToString) {
    return Joiner.on(" ; ").join(
        fractions.sortedInstrumentIdStream()
            .map(instrumentId -> Strings.format("%s %s",
                fractions.getOrThrow(instrumentId).toPercentString(precision),
                iidToString.apply(instrumentId)))
            .iterator());
  }

  public String toStringInDecreasingMembershipOrder(int precision) {
    return toStringInDecreasingMembershipOrder(precision, instrumentId -> instrumentId.toString());
  }

  public String toStringInDecreasingMembershipOrder(int precision, InstrumentMaster instrumentMaster, LocalDate date) {
    return toStringInDecreasingMembershipOrder(
        precision,
        instrumentId -> instrumentMaster.getLatestValidSymbolOrInstrumentIdAsSymbol(instrumentId, date).toString());
  }

  public String toStringInDecreasingMembershipOrder(int precision, Function<InstrumentId, String> iidToString) {
    return Joiner.on(" ; ").join(
        newArrayList(fractions.instrumentIdKeysIterator())
            .stream()
            .sorted(comparing(instrumentId -> fractions.getOrThrow(instrumentId).doubleValue(), reverseOrder()))
            .map(instrumentId -> Strings.format("%s %s",
                fractions.getOrThrow(instrumentId).toPercentString(precision),
                iidToString.apply(instrumentId)))
            .iterator());
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return toStringInDecreasingMembershipOrder(0, instrumentMaster, date);
  }

}
