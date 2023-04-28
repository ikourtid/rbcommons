package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.ClosedUnitFractionRange;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;

/**
 * A map whose values are {@link ClosedUnitFractionRange}s.
 *
 * @see ClosedUnitFractionRange
 */
public class ClosedUnitFractionRanges<K> {

  private final RBMap<K, ClosedUnitFractionRange> rangesMap;

  private ClosedUnitFractionRanges(RBMap<K, ClosedUnitFractionRange> rangesMap) {
    this.rangesMap = rangesMap;
  }

  public static <K> ClosedUnitFractionRanges<K> closedUnitFractionRanges(RBMap<K, ClosedUnitFractionRange> rangesMap) {
    return new ClosedUnitFractionRanges<>(rangesMap);
  }

  public static <K> ClosedUnitFractionRanges<K> nonEmptyClosedUnitFractionRanges(RBMap<K, ClosedUnitFractionRange> rangesMap) {
    RBPreconditions.checkArgument(!rangesMap.isEmpty());
    return new ClosedUnitFractionRanges<>(rangesMap);
  }

  public static <K> ClosedUnitFractionRanges<K> emptyClosedUnitFractionRanges() {
    return closedUnitFractionRanges(emptyRBMap());
  }

  public Optional<ClosedUnitFractionRange> getClosedRange(K key) {
    return rangesMap.getOptional(key);
  }

  public Optional<UnitFraction> getNearestValueInRange(K key, UnitFraction startingValue) {
    return transformOptional(
        rangesMap.getOptional(key),
        closedRange -> closedRange.getNearestValueInRange(startingValue));
  }

  public boolean containsKey(K key) {
    return rangesMap.containsKey(key);
  }

  public Set<K> keySet() {
    return rangesMap.keySet();
  }

  public Set<Entry<K, ClosedUnitFractionRange>> entrySet() {
    return rangesMap.entrySet();
  }

  public RBMap<K, ClosedUnitFractionRange> getRawMap() {
    return rangesMap;
  }

  public boolean isEmpty() {
    return rangesMap.isEmpty();
  }

  @Override
  public String toString() {
    return rangesMap.toString();
  }
}
