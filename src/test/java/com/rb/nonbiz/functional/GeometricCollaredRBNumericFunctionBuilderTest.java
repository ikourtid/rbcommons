package com.rb.nonbiz.functional;

import com.rb.nonbiz.types.PositiveMultiplier;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_LABEL;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;

// The builder is not quite a data class, so RBTestMatcher is not appropriate here.
public class GeometricCollaredRBNumericFunctionBuilderTest {

  @Test
  public void testSymmetricCase() {
    RBNumericFunction<Double, PositiveMultiplier> rbNumericFunction =
        GeometricCollaredRBNumericFunctionBuilder.<Double, PositiveMultiplier>geometricCollaredRBNumericFunctionBuilder()
            .setLabel(DUMMY_LABEL)
            .setInstantiator(v -> positiveMultiplier(v))
            .setMinX(4.0)
            .setMaxX(6.0)
            .setMinY(positiveMultiplier(1 / 3.0))
            .setMaxY(positiveMultiplier(3.0))
            .build();

    BiConsumer<Double, Double> asserter = (x, y) ->
        assertAlmostEquals(positiveMultiplier(y), rbNumericFunction.apply(x), DEFAULT_EPSILON_1e_8);

    asserter.accept(-999.9, 1 / 3.0);
    asserter.accept(   3.9, 1 / 3.0);
    asserter.accept(   4.0, 1 / 3.0);
    asserter.accept(   4.5, doubleExplained(0.577350269, 1 / Math.sqrt(3.0))); // geometric average for 4 above and 5 below
    asserter.accept(   5.0, 1.0);
    asserter.accept(   5.5, doubleExplained(1.73205081, Math.sqrt(3.0)));
    asserter.accept(   6.0, 3.0);
    asserter.accept(   6.1, 3.0);
    asserter.accept( 999.9, 3.0);
  }

  @Test
  public void testAsymmetricCase() {
    // This tests the example mentioned in the comments of the prod class
    RBNumericFunction<Double, PositiveMultiplier> rbNumericFunction =
        GeometricCollaredRBNumericFunctionBuilder.<Double, PositiveMultiplier>geometricCollaredRBNumericFunctionBuilder()
            .setLabel(DUMMY_LABEL)
            .setInstantiator(v -> positiveMultiplier(v))
            .setMinX(1.0)
            .setMaxX(9.0)
            .setMinY(positiveMultiplier(1 / 7.0))
            .setMaxY(positiveMultiplier(3.0))
            .build();

    BiConsumer<Double, Double> asserter = (x, y) ->
        assertAlmostEquals(positiveMultiplier(y), rbNumericFunction.apply(x), DEFAULT_EPSILON_1e_8);

    asserter.accept(-999.9, 1 / 7.0);
    asserter.accept(   0.9, 1 / 7.0);
    asserter.accept(   1.0, 1 / 7.0);

    asserter.accept(   5.0, doubleExplained(0.654653671, Math.sqrt(1 / 7.0 * 3))); // middle of range, i.e. midway between 1 and 9

    asserter.accept(   9.0, 3.0);
    asserter.accept(   9.1, 3.0);
    asserter.accept( 999.9, 3.0);
  }

}
