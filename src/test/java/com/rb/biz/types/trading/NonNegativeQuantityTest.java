package com.rb.biz.types.trading;

import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.types.PreciseValue;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.biz.types.trading.NonNegativeQuantity.ZERO_NON_NEGATIVE_QUANTITY;
import static com.rb.biz.types.trading.NonNegativeQuantity.nonNegativeQuantity;
import static com.rb.biz.types.trading.NonNegativeQuantity.sumNonNegativeQuantities;
import static com.rb.biz.types.trading.SignedQuantity.ZERO_SIGNED_QUANTITY;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.Asserters.valueExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class NonNegativeQuantityTest {

  @Test
  public void slightlyNegativeBecomesZero() {
    valueExplained(BigDecimal.valueOf(1e-12), PreciseValue.EPSILON_FOR_SNAPPING_TO_ROUND_NUMBER);
    assertIllegalArgumentException( () -> nonNegativeQuantity(-1e-11));
    assertEquals(ZERO_NON_NEGATIVE_QUANTITY, nonNegativeQuantity(-1e-13));
    assertEquals(ZERO_NON_NEGATIVE_QUANTITY, nonNegativeQuantity(0));
    assertEquals(ZERO_NON_NEGATIVE_QUANTITY, nonNegativeQuantity(1e-13));
    assertNotEquals(nonNegativeQuantity(10), nonNegativeQuantity(10 - 1e-13));
    assertEquals(nonNegativeQuantity(10), nonNegativeQuantity(10));
    assertNotEquals(nonNegativeQuantity(10), nonNegativeQuantity(10 + 1e-13));
  }

  @Test
  public void testAdditionSubtraction() {
    NonNegativeQuantity nonNegativeQuantity11 = nonNegativeQuantity(11);
    NonNegativeQuantity nonNegativeQuantity44 = nonNegativeQuantity(44);

    assertEquals(ZERO_NON_NEGATIVE_QUANTITY, ZERO_NON_NEGATIVE_QUANTITY.add(     ZERO_NON_NEGATIVE_QUANTITY));
    assertEquals(ZERO_NON_NEGATIVE_QUANTITY, ZERO_NON_NEGATIVE_QUANTITY.subtract(ZERO_NON_NEGATIVE_QUANTITY));
    assertEquals(
        ZERO_SIGNED_QUANTITY,
        ZERO_NON_NEGATIVE_QUANTITY.subtractToSigned(ZERO_NON_NEGATIVE_QUANTITY));

    assertEquals(nonNegativeQuantity11,   ZERO_NON_NEGATIVE_QUANTITY .add(nonNegativeQuantity11));
    assertEquals(nonNegativeQuantity(55), nonNegativeQuantity11.add(nonNegativeQuantity44));
    assertEquals(nonNegativeQuantity(55), nonNegativeQuantity44.add(nonNegativeQuantity11));

    assertEquals(ZERO_NON_NEGATIVE_QUANTITY, nonNegativeQuantity11.subtract(nonNegativeQuantity11));
    assertEquals(nonNegativeQuantity(33),    nonNegativeQuantity44.subtract(nonNegativeQuantity11));
    assertIllegalArgumentException( () -> nonNegativeQuantity11.subtract(nonNegativeQuantity44));

    assertEquals(ZERO_SIGNED_QUANTITY, nonNegativeQuantity11.subtractToSigned(nonNegativeQuantity11));
    assertEquals(signedQuantity( 33),  nonNegativeQuantity44.subtractToSigned(nonNegativeQuantity11));
    assertEquals(signedQuantity(-33),  nonNegativeQuantity11.subtractToSigned(nonNegativeQuantity44));
  }

  @Test
  public void testMinMax() {
    NonNegativeQuantity small = nonNegativeQuantity(1.23);
    NonNegativeQuantity large = nonNegativeQuantity(78.9);

    assertEquals(small, NonNegativeQuantity.min(small, small));
    assertEquals(small, NonNegativeQuantity.min(small, large));
    assertEquals(small, NonNegativeQuantity.min(large, small));

    assertEquals(large, NonNegativeQuantity.max(small, large));
    assertEquals(large, NonNegativeQuantity.max(large, small));
    assertEquals(large, NonNegativeQuantity.max(large, large));
  }

  @Test
  public void test_sumUsingNumbers() {
    // Tests with two arguments.
    TriConsumer<NonNegativeQuantity, NonNegativeQuantity, NonNegativeQuantity> asserter =
        (q1, q2, total) -> {
          assertAlmostEquals(sumNonNegativeQuantities(q1, q2), total, DEFAULT_EPSILON_1e_8);
          assertAlmostEquals(sumNonNegativeQuantities(q2, q1), total, DEFAULT_EPSILON_1e_8);
        };
    asserter.accept(ZERO_NON_NEGATIVE_QUANTITY, ZERO_NON_NEGATIVE_QUANTITY, ZERO_NON_NEGATIVE_QUANTITY);
    asserter.accept(nonNegativeQuantity(10.0),  ZERO_NON_NEGATIVE_QUANTITY, nonNegativeQuantity(10.0));
    asserter.accept(nonNegativeQuantity(12.0),  nonNegativeQuantity(34.0),  nonNegativeQuantity(46.0));
    asserter.accept(nonNegativeQuantity(0.5),   nonNegativeQuantity(1.5),   nonNegativeQuantity(2.0));
    asserter.accept(nonNegativeQuantity(12.3),  nonNegativeQuantity(45.6),  nonNegativeQuantity(57.9));

    // Tests with more than 2 arguments.
    // All zeroes...trivial
    assertAlmostEquals(
        ZERO_NON_NEGATIVE_QUANTITY,
        sumNonNegativeQuantities(ZERO_NON_NEGATIVE_QUANTITY, ZERO_NON_NEGATIVE_QUANTITY, ZERO_NON_NEGATIVE_QUANTITY),
    DEFAULT_EPSILON_1e_8);

    // Test a few more scenarios with 3 and 4 arguments, with decimals.
    assertAlmostEquals(
        nonNegativeQuantity(doubleExplained(10.2, 1.2 + 3.4 + 5.6)),
        sumNonNegativeQuantities(nonNegativeQuantity(1.2), nonNegativeQuantity(3.4), nonNegativeQuantity(5.6)),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        nonNegativeQuantity(doubleExplained(111.0, 11.1 + 22.2 + 33.3 + 44.4)),
        sumNonNegativeQuantities(
            nonNegativeQuantity(11.1),
            nonNegativeQuantity(22.2),
            nonNegativeQuantity(33.3),
            nonNegativeQuantity(44.4)),
        DEFAULT_EPSILON_1e_8);
    assertAlmostEquals(
        nonNegativeQuantity(doubleExplained(55.5, 0.0 + 0.0 + 55.5)),
        sumNonNegativeQuantities(ZERO_NON_NEGATIVE_QUANTITY, ZERO_NON_NEGATIVE_QUANTITY, nonNegativeQuantity(55.5)),
        DEFAULT_EPSILON_1e_8);
  }

  @Test
  public void sum_usingIterators() {
    RBSet<NonNegativeQuantity> emptySet = emptyRBSet();
    assertEquals(
        ZERO_NON_NEGATIVE_QUANTITY,
        sumNonNegativeQuantities(emptySet.asSet().iterator()));
    assertEquals(
        nonNegativeQuantity(10),
        sumNonNegativeQuantities(singletonRBSet(nonNegativeQuantity(10)).asSet().iterator()));
    assertEquals(
        nonNegativeQuantity(30),
        sumNonNegativeQuantities(rbSetOf(nonNegativeQuantity(20), nonNegativeQuantity(10)).asSet().iterator()));
  }

  @Test
  public void sum_usingStreams() {
    RBSet<NonNegativeQuantity> emptySet = emptyRBSet();
    assertEquals(
        ZERO_NON_NEGATIVE_QUANTITY,
        sumNonNegativeQuantities(emptySet.asSet().stream()));
    assertEquals(
        nonNegativeQuantity(10),
        sumNonNegativeQuantities(singletonRBSet(nonNegativeQuantity(10)).asSet().stream()));
    assertEquals(
        nonNegativeQuantity(30),
        sumNonNegativeQuantities(rbSetOf(nonNegativeQuantity(20), nonNegativeQuantity(10)).asSet().stream()));
  }

}
