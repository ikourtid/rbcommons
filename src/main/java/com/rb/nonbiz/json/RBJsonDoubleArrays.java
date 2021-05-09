package com.rb.nonbiz.json;

import com.rb.biz.types.trading.RoundingScale;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.json.RBJsonDoubleArray.RBJsonDoubleArrayBuilder;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;

import static com.rb.nonbiz.json.RBJsonDoubleArray.RBJsonDoubleArrayBuilder.rbJsonDoubleArrayBuilder;
import static com.rb.nonbiz.json.RBJsonDoubleArray.rbJsonDoubleArray;

public class RBJsonDoubleArrays {

  public static <P extends PreciseValue<? super P>> RBJsonDoubleArray convertClosedRangeToRBJsonDoubleArray(ClosedRange<P> range) {
    return rbJsonDoubleArray(
        range.lowerEndpoint().doubleValue(),
        range.upperEndpoint().doubleValue());
  }

}
