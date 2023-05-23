package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;

/**
 * Helps us keep a track of the number of times we have seen set of items.
 *
 * <p> It is like a histogram. It's not like a general map of object to value.
 * We can only increment the counters of individual items and see their counts, but not overwrite the counts. </p>
 *
 */
public class Counter<T> {

  public static class CounterBuilder<T> implements RBBuilder<Counter<T>> {

    private final MutableRBMap<T, Integer> rawMutableMap;

    private CounterBuilder() {
      this.rawMutableMap = newMutableRBMap();
    }

    public static <T> CounterBuilder<T> counterBuilder() {
      return new CounterBuilder<>();
    }

    public CounterBuilder<T> add(T key, int value) {
      RBPreconditions.checkArgument(
          value >= 0,
          "cannot add value %s to counter; must be non-negative",
          value);
      Optional<Integer> currentCount = rawMutableMap.getOptional(key);
      rawMutableMap.put(key, currentCount.isPresent() ? currentCount.get() + value : value);
      return this;
    }

    public CounterBuilder<T> addAll(RBMap<T, Integer> rbMap) {
      rbMap.forEachEntry( (key, value) -> add(key, value));
      return this;
    }

    public CounterBuilder<T> increment(T key) {
      return add(key, 1);
    }

    @Override
    public void sanityCheckContents() {
      // there's no invalid state for this, so nothing to assert on
    }

    @Override
    public Counter<T> buildWithoutPreconditions() {
      return new Counter<T>(newRBMap(rawMutableMap));
    }

  }


  private final RBMap<T, Integer> rawMap;

  private Counter(RBMap<T, Integer> rawMap) {
    this.rawMap = rawMap;
  }

  public int getSumOfCounts() {
    return rawMap.values()
        .stream()
        .reduce(0, Integer::sum);
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  public int getCountOrThrow(T key) {
    return rawMap.getOrThrow(key);
  }

  public int getCountOrZero(T key) {
    return rawMap.getOrDefault(key, 0);
  }

  public Set<T> getItemsWithNonZeroCounts() {
    return getItemsWithCountOfAtLeast(1);
  }

  public Set<T> getItemsWithCountOfAtLeast(int minCount) {
    return rawMap.keySet()
        .stream()
        .filter(instrumentId -> getCountOrThrow(instrumentId) >= minCount)
        .collect(Collectors.toSet());
  }

  public Set<T> getItemsWithCountOf(int countFilter) {
    RBPreconditions.checkArgument(
        countFilter >= 0,
        "You can't ask for the items with a negative count in the Counter: %s",
        countFilter);
    return rawMap.keySet()
        .stream()
        .filter(instrumentId -> getCountOrThrow(instrumentId) == countFilter)
        .collect(Collectors.toSet());
  }

  /**
   * Do not use this; it is here to help the test matcher and by a JSON API converter.
   */
  public RBMap<T, Integer> getRawMapUnsafe() {
    return rawMap;
  }

  @Override
  public String toString() {
    return rawMap.toString();
  }

}
