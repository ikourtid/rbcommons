package com.rb.biz.investing.strategy.optbased.di;

import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.investing.strategy.optbased.di.BlendedRealizedVolatility.blendedRealizedVolatility;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

// This implements ImpreciseValue so we don't need RBTestMatcher.
public class BlendedRealizedVolatilityTest {

  @Test
  public void valueIsNegative_throws() {
    Function<Double, BlendedRealizedVolatility> maker = v -> blendedRealizedVolatility(v);

    assertIllegalArgumentException( () -> maker.apply(-1.23e9));
    assertIllegalArgumentException( () -> maker.apply(-1.23));
    assertIllegalArgumentException( () -> maker.apply(-1e-9));

    BlendedRealizedVolatility doesNotThrow;
    doesNotThrow = maker.apply(0.0);
    doesNotThrow = maker.apply(1e-9);
    doesNotThrow = maker.apply(0.123);
    doesNotThrow = maker.apply(0.999);
    doesNotThrow = maker.apply(1.0);
    doesNotThrow = maker.apply(2.345);
    doesNotThrow = maker.apply(1.23e9);
  }

}
