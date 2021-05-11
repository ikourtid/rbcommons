package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBSet.rbSet;

/**
 * Utilities for working with {@link DoubleMap}s.
 */
public class DoubleMaps {

  public static <K, N extends RBNumeric<? super N>> DoubleMap<K> doubleMapOfRBNumeric(RBMap<K, N> map) {
    return doubleMap(map.transformValuesCopy(v -> v.doubleValue()));
  }

  public static <K> DoubleMap<K> sumDoubleMaps(DoubleMap<K> map1, DoubleMap<K> map2) {
    return linearlyCombineDoubleMaps(
        ImmutableList.of(map1, map2),
        ImmutableList.of(1.0, 1.0));
  }

  public static <K> DoubleMap<K> linearlyCombineDoubleMaps(List<DoubleMap<K>> maps, List<Double> coefficients) {
    RBPreconditions.checkArgument(
        maps.size() == coefficients.size(),
        "There are %s maps but %s coefficients supplied",
        maps.size(), coefficients.size());
    RBSet<K> allKeys = rbSet(maps.stream()
        .map(theMap -> theMap.getRawMap().keySet())
        .flatMap(mapKeys -> mapKeys.stream())
        .collect(Collectors.toSet()));
    MutableRBMap<K, Double> mutableMap = newMutableRBMapWithExpectedSize(allKeys.size());
    for (K key : allKeys) {
      double coeff = 0;
      for (int i = 0; i < maps.size(); i++) {
        coeff += maps.get(i).getRawMap().getOrDefault(key, 0.0) * coefficients.get(i);
      }
      if (coeff != 0) {
        mutableMap.putAssumingAbsent(key, coeff);
      }
    }
    return doubleMap(newRBMap(mutableMap));
  }

}
