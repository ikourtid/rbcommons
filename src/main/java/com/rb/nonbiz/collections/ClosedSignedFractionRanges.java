package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.ClosedSignedFractionRange;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;

/**
 * A map whose values are {@link ClosedSignedFractionRange}s.
 *
 * @see ClosedSignedFractionRange
 */
public class ClosedSignedFractionRanges<K> {

  private final RBMap<K, ClosedSignedFractionRange> rangesMap;

  private ClosedSignedFractionRanges(RBMap<K, ClosedSignedFractionRange> rangesMap) {
    this.rangesMap = rangesMap;
  }

  public static <K> ClosedSignedFractionRanges<K> closedSignedFractionRanges(RBMap<K, ClosedSignedFractionRange> rangesMap) {
    return new ClosedSignedFractionRanges<>(rangesMap);
  }

  public static <K> ClosedSignedFractionRanges<K> nonEmptyClosedSignedFractionRanges(RBMap<K, ClosedSignedFractionRange> rangesMap) {
    RBPreconditions.checkArgument(!rangesMap.isEmpty());
    return new ClosedSignedFractionRanges<>(rangesMap);
  }
  public static <K> ClosedSignedFractionRanges<K> emptyClosedSignedFractionRanges() {
    return closedSignedFractionRanges(emptyRBMap());
  }

  public Optional<ClosedSignedFractionRange> getClosedSignedFractionRange(K key) {
    return rangesMap.getOptional(key);
  }

  public Set<K> keySet() {
    return rangesMap.keySet();
  }

  public Set<Entry<K, ClosedSignedFractionRange>> entrySet() {
    return rangesMap.entrySet();
  }

  public RBMap<K, ClosedSignedFractionRange> getRawMap() {
    return rangesMap;
  }

  @Override
  public String toString() {
    return rangesMap.toString();
  }
}
