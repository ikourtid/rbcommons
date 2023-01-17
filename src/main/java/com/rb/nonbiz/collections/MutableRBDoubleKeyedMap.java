package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import static com.google.common.collect.Maps.newTreeMap;

/**
 * A map of double to some value.
 *
 * <p> We can't use {@link RBMap} for this. In general, it's a bad idea to use doubles as keys, because double equality
 * can be approximate in case a number is the result of a calculation (e.g. 0.8 is not the same as 0.2 * 0.4),
 * so behavior can be unexpected. </p>
 *
 * <p> This special map lets you look up values an 'almost equal' keys, subject to an epsilon. </p>
 */
public class MutableRBDoubleKeyedMap<V> {

  /**
   * If more than one key / value pair is within epsilon of the requested double key value, what should we do?
   */
  public enum BehaviorWhenTwoDoubleKeysAreClose {

    THROW_EXCEPTION,
    USE_FLOOR,
    USE_CEILING,
    USE_NEAREST_OR_FLOOR,
    USE_NEAREST_OR_CEILING

  }

  private final TreeMap<Double, V> rawMap;

  private MutableRBDoubleKeyedMap(TreeMap<Double, V> rawMap) {
    this.rawMap = rawMap;
  }

  public static <V> MutableRBDoubleKeyedMap<V> newMutableRBDoubleKeyedMap() {
    return new MutableRBDoubleKeyedMap<>(newTreeMap());
  }

  /**
   * If an exactly equal double key exists, then this replaces what's there. Otherwise, it creates a new entry.
   * Unlike {@link RBMap} etc., we don't do putAssumingAbsent etc., because checking whether a key already exists
   * is somewhat expensive, plus it's not needed in the current (Jan 2023) use case. We could add that later as needed.
   */
  public void put(double key, V value) {
    rawMap.put(key, value);
  }

  public V getOrThrow(
      double key,
      Epsilon epsilon,
      BehaviorWhenTwoDoubleKeysAreClose behaviorWhenTwoDoubleKeysAreClose) {
    return RBPreconditions.checkNotNull(
        getOrNull(key, epsilon, behaviorWhenTwoDoubleKeysAreClose),
        "Cannot find value using key= %s ; epsilon= %s ; behaviorOnAlmostEqualDoubleKeys= %s",
        key, epsilon, behaviorWhenTwoDoubleKeysAreClose);
  }

  public Optional<V> getOptional(
      double key,
      Epsilon epsilon,
      BehaviorWhenTwoDoubleKeysAreClose behaviorWhenTwoDoubleKeysAreClose) {
    return Optional.ofNullable(getOrNull(key, epsilon, behaviorWhenTwoDoubleKeysAreClose));
  }

  // Note that we never return null, and we avoid doing that even in private methods such as this one,
  // but this makes it easy to centralize the logic so that both getOptional and getOrThrow can rely on this.
  private V getOrNull(double key, Epsilon epsilon, BehaviorWhenTwoDoubleKeysAreClose behaviorWhenTwoDoubleKeysAreClose) {
    Entry<Double, V>   floorEntry = rawMap.floorEntry(key);
    Entry<Double, V> ceilingEntry = rawMap.ceilingEntry(key);

    if (floorEntry == null && ceilingEntry == null) {
      return null;
    }
    if (floorEntry == null) { // so ceilingEntry must be non-null
      return epsilon.valuesAreWithin(ceilingEntry.getKey(), key)
          ? ceilingEntry.getValue()
          : null;
    } else if (ceilingEntry == null) { // floorEntry is non-null
      return epsilon.valuesAreWithin(floorEntry.getKey(), key)
          ? floorEntry.getValue()
          : null;
    }

    // OK, at this point, we have two values to choose from - one below and one above.
    // First, check to see if it's the exact same double key (rare). If so, there's really no choice.
    // This is an intentional double comparison, so it will look like a warning in the IDE.
    if (floorEntry.getKey() == ceilingEntry.getKey()) {
      return RBSimilarityPreconditions.checkBothSame(
          floorEntry.getValue(),
          ceilingEntry.getValue(),
          "Internal error; same keys but different values in the MutableRBDoubleKeyedMap");
    }

    switch (behaviorWhenTwoDoubleKeysAreClose) {
      case THROW_EXCEPTION:
        throw new IllegalArgumentException(Strings.format(
            "Lookup key %s is within epsilon %s of consecutive keys %s and %s ; throwing exception, as requested",
            key, floorEntry.getKey(), ceilingEntry.getKey()));

      case USE_FLOOR:
        return floorEntry.getValue();

      case USE_CEILING:
        return ceilingEntry.getValue();

      case USE_NEAREST_OR_FLOOR:
        // In the event both keys are equally far from each other, it's a tie, so using the floor, as per this enum.
        return Math.abs(key - floorEntry.getKey()) <= Math.abs(key - ceilingEntry.getKey())
            ? floorEntry.getValue()
            : ceilingEntry.getValue();

      case USE_NEAREST_OR_CEILING:
        // In the event both keys are equally far from each other, it's a tie, so using the floor, as per this enum.
        return Math.abs(key - ceilingEntry.getKey()) <= Math.abs(key - floorEntry.getKey())
            ? ceilingEntry.getValue()
            : floorEntry.getValue();

      default:
        throw new IllegalArgumentException(Strings.format(
            "Internal error: not handled: %s", behaviorWhenTwoDoubleKeysAreClose));
    }
  }

}
