package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.PrintsMultilineString;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Weighted;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.types.PreciseValue.formatWithoutCommas;
import static com.rb.nonbiz.types.Weighted.weighted;

/**
 * A thin layer on top of a plain RBMap, for the special case where the values are doubles.
 * It lets you express more explicitly what a map contains.
 * Also has well-defined functionality for summing, combining etc. - see DoubleMapMath.java
 */
public class DoubleMap<K> implements Iterable<Weighted<K>>, PrintsMultilineString {

  private final RBMap<K, Double> rawMap;

  private DoubleMap(RBMap<K, Double> rawMap) {
    this.rawMap = rawMap;
  }

  public static <K> DoubleMap<K> doubleMap(RBMap<K, Double> rawMap) {
    return new DoubleMap<>(rawMap);
  }

  public static <K> DoubleMap<K> singletonDoubleMap(K key, double weight) {
    return new DoubleMap<K>(singletonRBMap(key, weight));
  }

  @VisibleForTesting
  public static <K> DoubleMap<K> emptyDoubleMap() {
    return doubleMap(emptyRBMap());
  }

  public RBMap<K, Double> getRawMap() {
    return rawMap;
  }

  public int size() {
    return rawMap.size();
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  public Set<Entry<K, Double>> entrySet() {
    return rawMap.entrySet();
  }

  public Set<K> keySet() {
    return rawMap.keySet();
  }

  public Collection<Double> values() {
    return rawMap.values();
  }

  public double sum() {
    double sum = 0;
    for (double d : values()) {
      sum += d;
    }
    return sum;
  }

  @Override
  public String toString() {
    return rawMap.toString();
  }

  @Override
  public Iterator<Weighted<K>> iterator() {
    return entrySet()
        .stream()
        .map(entry -> weighted(entry.getKey(), entry.getValue()))
        .iterator();
  }

  @Override
  public String toMultilineString() {
    NumberFormat numberFormat = formatWithoutCommas(8);  // max 8 digits
    return Strings.formatMapInValueOrder(rawMap, Double::compare, "\n", v -> numberFormat.format(v));
  }

}
