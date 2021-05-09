package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.PreciseValue;

import java.math.BigDecimal;
import java.util.function.Function;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapWithExpectedSizeFromStream;

public class PartitionMultiplier {

  public <K, V extends PreciseValue<V>> RBMap<K, V> multiplyPartitionBy(
      Partition<K> partition, V totalValueToAllocate, Function<BigDecimal, V> instantiator) {
    return rbMapWithExpectedSizeFromStream(
        partition.size(),
        partition.keySet().stream(),
        key -> key,
        key -> instantiator.apply(
            partition.getFraction(key).asBigDecimal().multiply(totalValueToAllocate.asBigDecimal(), DEFAULT_MATH_CONTEXT)));
  }

}
