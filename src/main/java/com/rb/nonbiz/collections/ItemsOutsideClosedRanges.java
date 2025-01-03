package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.AssetId;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;

/**
 * A thin semantic wrapper around a map of {@link ValueOutsideClosedRange}.
 *
 * <p> Its initial usage was to represent {@link AssetId}s whose initial pre-optimization holdings
 * are not within their designated ranges, which means we have to trade. </p>
 */
public class ItemsOutsideClosedRanges<K, V extends Comparable<? super V>> {

  private final RBMap<K, ValueOutsideClosedRange<V>> rawMap;

  public ItemsOutsideClosedRanges(RBMap<K, ValueOutsideClosedRange<V>> rawMap) {
    this.rawMap = rawMap;
  }

  public static <K, T extends Comparable<? super T>> ItemsOutsideClosedRanges<K, T> itemsOutsideClosedRanges(
      RBMap<K, ValueOutsideClosedRange<T>> rawMap) {
    return new ItemsOutsideClosedRanges<>(rawMap);
  }

  public static <K, T extends Comparable<? super T>> ItemsOutsideClosedRanges<K, T> emptyItemsOutsideClosedRanges() {
    return new ItemsOutsideClosedRanges<>(emptyRBMap());
  }

  public RBMap<K, ValueOutsideClosedRange<V>> getRawMap() {
    return rawMap;
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  @Override
  public String toString() {
    return Strings.format("[IOCR %s IOCR]", rawMap);
  }

}
