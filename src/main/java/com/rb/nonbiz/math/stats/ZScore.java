package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.types.ImpreciseValue;

public class ZScore extends ImpreciseValue<ZScore> {

  public static ZScore Z_SCORE_0 = zScore(0);

  protected ZScore(double value) {
    super(value);
  }

  public static ZScore zScore(double rawValue) {
    return new ZScore(rawValue);
  }

}
