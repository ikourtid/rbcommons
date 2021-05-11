package com.rb.nonbiz.functional;

import com.rb.biz.types.Money;
import org.junit.Test;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_LABEL;
import static org.junit.Assert.assertEquals;

// The builder is not quite a data class, so RBTestMatcher is not appropriate here.
public class LinearCollaredRBNumericFunctionBuilderTest {

  @Test
  public void testSampleFunction() {
    RBNumericFunction<Double, Money> rbNumericFunction =
        LinearCollaredRBNumericFunctionBuilder.<Double, Money>linearCollaredRBNumericFunctionBuilder()
            .setLabel(DUMMY_LABEL)
            .setInstantiator(v -> money(v))
            .setMinX(0.3)
            .setMaxX(1.7)
            .setMinY(money(30))
            .setMaxY(money(170))
            .build();

    assertEquals(money(30), rbNumericFunction.apply(-999.9));
    assertEquals(money(30), rbNumericFunction.apply(0.29));
    assertEquals(money(30), rbNumericFunction.apply(0.30));
    assertAlmostEquals(money(40), rbNumericFunction.apply(0.4), 1e-8);
    assertAlmostEquals(money(100), rbNumericFunction.apply(1.0), 1e-8);
    assertAlmostEquals(money(100), rbNumericFunction.apply(1.0), 1e-8);
    assertAlmostEquals(money(169), rbNumericFunction.apply(1.69), 1e-8);
    assertAlmostEquals(money(170), rbNumericFunction.apply(1.7), 1e-8);
    assertEquals(money(170), rbNumericFunction.apply(1.71));
    assertEquals(money(170), rbNumericFunction.apply(999.9));
  }

}
