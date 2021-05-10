package com.rb.nonbiz.json;

import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.types.PreciseValue;

import static com.rb.nonbiz.json.RBJsonDoubleArray.rbJsonDoubleArray;

public class RBJsonDoubleArrays {

  public static <P extends PreciseValue<? super P>> RBJsonDoubleArray convertClosedRangeToRBJsonDoubleArray(ClosedRange<P> range) {
    return rbJsonDoubleArray(
        range.lowerEndpoint().doubleValue(),
        range.upperEndpoint().doubleValue());
  }

}
