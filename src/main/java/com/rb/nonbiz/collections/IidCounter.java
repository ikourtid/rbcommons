package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.IidCounter.IidCounterBuilder.iidCounterBuilder;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMap;
import static com.rb.nonbiz.text.Strings.formatIidMap;

/**
 * Helps us keep a track of the # of times we have seen a bunch of items, per InstrumentId.
 *
 * <p> It is like a histogram. It's not like a general map of InstrumentId to value.
 * We can only increment the counters of individual instruments and see their counts, but not overwrite the counts. </p>
 *
 * <p> This is a special, InstrumentId-specific implementation of {@link Counter}. It is a rare example of a mutable class
 * in our codebase. </p>
 *
 * @see Counter
 */
public class IidCounter {

  private final IidMap<Integer> rawMap;

  private IidCounter(IidMap<Integer> rawMap) {
    this.rawMap = rawMap;
  }

  public static <T extends HasInstrumentId> IidCounter iidCounterFromStream(
      Stream<T> stream, Predicate<T> predicate) {
    IidCounterBuilder builder = iidCounterBuilder();
    stream.forEach(item -> {
      if (predicate.test(item)) {
        builder.increment(item.getInstrumentId());
      }
    });
    return builder.build();
  }

  public static IidCounter iidCounterFromMap(IidMap<Integer> rawMap) {
    return new IidCounter(rawMap);
  }

  public static <T extends HasInstrumentId> IidCounter iidCounterFromStream(
      Stream<T> stream) {
    return iidCounterFromStream(stream, v -> true);
  }

  public int getSumOfCounts() {
    return rawMap.values()
        .stream()
        .reduce(0, Integer::sum);
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  public int getCountOrThrow(InstrumentId instrumentId) {
    return rawMap.getOrThrow(instrumentId);
  }

  public int getCountOrZero(InstrumentId instrumentId) {
    return rawMap.getOrDefault(instrumentId, 0);
  }

  public IidSet getItemsWithNonZeroCounts() {
    return getItemsWithCountOfAtLeast(1);
  }

  public IidSet getItemsWithCountOfAtLeast(int minCount) {
    return newIidSet(rawMap.keySet()
        .stream()
        .filter(instrumentId -> getCountOrThrow(instrumentId) >= minCount)
        .collect(Collectors.toSet()));
  }

  public IidSet getItemsWithCountOf(int countFilter) {
    RBPreconditions.checkArgument(
        countFilter >= 0,
        "You can't ask for the items with a negative count in an IidCounter: %s",
        countFilter);
    return rawMap.keySet()
        .filter(instrumentId -> getCountOrThrow(instrumentId) == countFilter);
  }

  /**
   * Ideally do not use this; it is here to help the test matcher.
   */
  public IidMap<Integer> getRawMap() {
    return rawMap;
  }

  @Override
  public String toString() {
    return Strings.format("[IIC %s IIC", formatIidMap(rawMap));
  }

  /**
   * An {@link RBBuilder} that constructs an {@link IidCounter}.
   */
  public static class IidCounterBuilder implements RBBuilder<IidCounter> {

    private final MutableIidMap<Integer> rawMutableMap;

    private IidCounterBuilder() {
      this.rawMutableMap = newMutableIidMap();
    }

    public static IidCounterBuilder iidCounterBuilder() {
      return new IidCounterBuilder();
    }

    public IidCounterBuilder add(InstrumentId instrumentId, int value) {
      Optional<Integer> currentCount = rawMutableMap.getOptional(instrumentId);
      rawMutableMap.put(instrumentId, currentCount.isPresent() ? currentCount.get() + value : value);
      return this;
    }

    public IidCounterBuilder increment(InstrumentId instrumentId) {
      return add(instrumentId, 1);
    }

    @Override
    public void sanityCheckContents() {
      // there's no invalid state for this, so nothing to assert on
    }

    @Override
    public IidCounter buildWithoutPreconditions() {
      return new IidCounter(newIidMap(rawMutableMap));
    }

  }

}
