package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

// FIXME IAK Issue #1532 explain
public class ItemsOutsideClosedRanges<K, T extends Comparable<? super T>> {

  private final RBMap<K, ValueOutsideClosedRange<T>> rawMap;

  public ItemsOutsideClosedRanges(RBMap<K, ValueOutsideClosedRange<T>> rawMap) {
    this.rawMap = rawMap;
  }

  public static <K, T extends Comparable<? super T>> ItemsOutsideClosedRanges<K, T> itemsOutsideClosedRanges(
      RBMap<K, ValueOutsideClosedRange<T>> rawMap) {
    return new ItemsOutsideClosedRanges<>(rawMap);
  }

  public RBMap<K, ValueOutsideClosedRange<T>> getRawMap() {
    return rawMap;
  }

  @Override
  public String toString() {
    return Strings.format("[IOCR %s IOCR]", rawMap);
  }

}
