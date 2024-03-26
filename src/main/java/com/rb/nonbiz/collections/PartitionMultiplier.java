package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.PreciseValue;

import java.math.BigDecimal;
import java.util.function.Function;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapWithExpectedSizeFromStream;

/**
 * 'Multiplies' (per the lambda specified) a {@link Partition} by a value, resulting in an {@link RBMap}.
 *
 * <p> For example, multiplying a partition that's 40% A, 60% B by $1000 should give us a map of
 * A $400, B $600. </p>
 */
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
