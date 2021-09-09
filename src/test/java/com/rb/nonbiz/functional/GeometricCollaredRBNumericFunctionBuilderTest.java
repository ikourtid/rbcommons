package com.rb.nonbiz.functional;

import com.rb.nonbiz.types.PositiveMultiplier;
import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_LABEL;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static org.junit.Assert.assertEquals;

// The builder is not quite a data class, so RBTestMatcher is not appropriate here.
public class GeometricCollaredRBNumericFunctionBuilderTest {

  @Test
  public void testSampleFunction() {
    RBNumericFunction<Double, PositiveMultiplier> rbNumericFunction =
        GeometricCollaredRBNumericFunctionBuilder.<Double, PositiveMultiplier>geometricCollaredRBNumericFunctionBuilder()
            .setLabel(DUMMY_LABEL)
            .setInstantiator(v -> positiveMultiplier(v))
            .setMinX(4.0)
            .setMaxX(6.0)
            .setMinY(positiveMultiplier(1 / 3.0))
            .setMaxY(positiveMultiplier(3.0))
            .build();

    assertAlmostEquals(positiveMultiplier(1 / 3.0), rbNumericFunction.apply(-999.9), 1e-8);
    assertAlmostEquals(positiveMultiplier(1 / 3.0), rbNumericFunction.apply(3.99), 1e-8);
    assertAlmostEquals(positiveMultiplier(1 / 3.0), rbNumericFunction.apply(4.0), 1e-8);

    assertAlmostEquals(positiveMultiplier(1), rbNumericFunction.apply(5.0), 1e-8);

    assertAlmostEquals(positiveMultiplier(3), rbNumericFunction.apply(6.01), 1e-8);
    assertAlmostEquals(positiveMultiplier(3), rbNumericFunction.apply(999.9), 1e-8);
  }

}
