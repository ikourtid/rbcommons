package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.MutableRBDoubleKeyedMap.BehaviorWhenTwoDoubleKeysAreClose;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;

import java.util.Optional;

/**
 * A map from a double to a value.
 *
 * <p> We can't use {@link RBMap} for this. In general, it's a bad idea to use doubles as keys, because double equality
 * can be approximate in case a number is the result of a calculation (e.g. 0.8 is not the same as 0.2 * 0.4),
 * so behavior can be unexpected. </p>
 *
 * <p> This special map lets you look up values an 'almost equal' keys, subject to an epsilon. </p>
 *
 * <p> Unlike {@link MutableRBDoubleKeyedMap}, this is immutable. </p>
 */
public class RBDoubleKeyedMap<V> {

  private final MutableRBDoubleKeyedMap<V> rawMutableMap;

  private RBDoubleKeyedMap(MutableRBDoubleKeyedMap<V> rawMutableMap) {
    this.rawMutableMap = rawMutableMap;
  }

  /**
   * Our static constructors don't normally have 'new' prepended, but this parallels newRBMap, newIidMap, etc.
   */
  public static <V> RBDoubleKeyedMap<V> newRBDoubleKeyedMap(MutableRBDoubleKeyedMap<V> rawMutableMap) {
    return new RBDoubleKeyedMap<>(rawMutableMap);
  }

  public V getOrThrow(
      double key,
      Epsilon epsilon,
      BehaviorWhenTwoDoubleKeysAreClose behaviorWhenTwoDoubleKeysAreClose) {
    return rawMutableMap.getOrThrow(key, epsilon, behaviorWhenTwoDoubleKeysAreClose);
  }

  public Optional<V> getOptional(
      double key,
      Epsilon epsilon,
      BehaviorWhenTwoDoubleKeysAreClose behaviorWhenTwoDoubleKeysAreClose) {
    return rawMutableMap.getOptional(key, epsilon, behaviorWhenTwoDoubleKeysAreClose);
  }

  // Do not use this; it's here to help the test matcher
  @VisibleForTesting
  MutableRBDoubleKeyedMap<V> getRawMutableMapUnsafe() {
    return rawMutableMap;
  }

  @Override
  public String toString() {
    return Strings.format(
        "[RBDKM %s : %s RBKDM]",
        rawMutableMap.size(), rawMutableMap.toString());
  }

}
