package com.rb.nonbiz.types;

import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;
import org.junit.Test;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.RBDoubles.average;
import static com.rb.nonbiz.types.RBDoubles.epsilonCompareDoubles;
import static com.rb.nonbiz.types.RBDoubles.epsilonCompareDoublesAllowingEpsilonOfZero;
import static com.rb.nonbiz.types.RBDoubles.getDoubleAsLongAssumingIsRound;
import static com.rb.nonbiz.types.RBDoubles.maxAllowingOptionalDouble;
import static com.rb.nonbiz.types.RBDoubles.minAllowingOptionalDouble;
import static com.rb.nonbiz.types.RBIntegers.maxAllowingOptionalInt;
import static com.rb.nonbiz.types.RBIntegers.minAllowingOptionalInt;
import static org.junit.Assert.assertEquals;

public class RBDoublesTest {

  public static EpsilonComparisonVisitor<String> comparisonSignVisitor() {
    return new EpsilonComparisonVisitor<String>() {
      @Override
      public String visitRightIsGreater(double rightMinusLeft) {
        return "<";
      }

      @Override
      public String visitAlmostEqual() {
        return "==";
      }

      @Override
      public String visitLeftIsGreater(double rightMinusLeft) {
        return ">";
      }
    };
  }

  @Test
  public void testEpsilonCompare_generalCase() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    assertEquals("<",  epsilonCompareDoubles(77,        88,        epsilon(1e-8), visitor));
    assertEquals(">",  epsilonCompareDoubles(88,        77,        epsilon(1e-8), visitor));
    assertEquals("==", epsilonCompareDoubles(88,        88,        epsilon(1e-8), visitor));
    assertEquals("==", epsilonCompareDoubles(88 + 1e-9, 88,        epsilon(1e-8), visitor));
    assertEquals("==", epsilonCompareDoubles(88,        88 + 1e-9, epsilon(1e-8), visitor));
    assertEquals(">",  epsilonCompareDoubles(88 + 1e-9, 88,        epsilon(1e-10), visitor));
    assertEquals("<",  epsilonCompareDoubles(88,        88 + 1e-9, epsilon(1e-10), visitor));
  }

  @Test
  public void testEpsilonCompareAllowingEpsilonOfZero_generalCase() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    assertEquals("<",  epsilonCompareDoublesAllowingEpsilonOfZero(77,        88,        epsilon(1e-8), visitor));
    assertEquals(">",  epsilonCompareDoublesAllowingEpsilonOfZero(88,        77,        epsilon(1e-8), visitor));
    assertEquals("==", epsilonCompareDoublesAllowingEpsilonOfZero(88,        88,        epsilon(1e-8), visitor));
    assertEquals("==", epsilonCompareDoublesAllowingEpsilonOfZero(88 + 1e-9, 88,        epsilon(1e-8), visitor));
    assertEquals("==", epsilonCompareDoublesAllowingEpsilonOfZero(88,        88 + 1e-9, epsilon(1e-8), visitor));
    assertEquals(">",  epsilonCompareDoublesAllowingEpsilonOfZero(88 + 1e-9, 88,        epsilon(1e-10), visitor));
    assertEquals("<",  epsilonCompareDoublesAllowingEpsilonOfZero(88,        88 + 1e-9, epsilon(1e-10), visitor));
  }

  @Test
  public void testAverage() {
    assertEquals(0, average(0, 0), 1e-8);
    assertEquals(5.5, average(4.4, 6.6), 1e-8);
    assertEquals(-5.5, average(-4.4, -6.6), 1e-8);
    assertEquals(4, average(-0.5, 8.5), 1e-8);
  }

  @Test
  public void testDoubleIsRound() {
    BiConsumer<Double, Integer> assertResult = (doubleValue, expectedLong) ->
        assertEquals((long) expectedLong, getDoubleAsLongAssumingIsRound(doubleValue, DEFAULT_EPSILON_1e_8));
    DoubleConsumer assertIllegal = doubleValue ->
        assertIllegalArgumentException( () -> getDoubleAsLongAssumingIsRound(doubleValue, DEFAULT_EPSILON_1e_8));

    assertIllegal.accept(-7.9);
    assertIllegal.accept(-7.5);
    assertIllegal.accept(-7.1);
    assertIllegal.accept(-7 - 1e-7);
    assertResult .accept(-7 - 1e-9, -7);
    assertResult .accept(-7.0,      -7);
    assertResult .accept(-7 + 1e-9, -7);
    assertIllegal.accept(-7 + 1e-7);
    assertIllegal.accept(-6.9);
    assertIllegal.accept(-6.5);
    assertIllegal.accept(-6.1);

    assertIllegal.accept(-0.9);
    assertIllegal.accept(-0.5);
    assertIllegal.accept(-0.1);
    assertIllegal.accept(-1e-7);
    assertResult .accept(-1e-9, 0);
    assertResult .accept(0.0,   0);
    assertResult .accept(1e-9,  0);
    assertIllegal.accept(1e-7);
    assertIllegal.accept(0.1);
    assertIllegal.accept(0.5);
    assertIllegal.accept(0.9);

    assertIllegal.accept(6.1);
    assertIllegal.accept(6.5);
    assertIllegal.accept(6.9);
    assertIllegal.accept(7 - 1e-7);
    assertResult .accept(7 - 1e-9, 7);
    assertResult .accept(7.0,      7);
    assertResult .accept(7 + 1e-9, 7);
    assertIllegal.accept(7 + 1e-7);
    assertIllegal.accept(7.1);
    assertIllegal.accept(7.5);
    assertIllegal.accept(7.9);
  }

  @Test
  public void testMaxAllowingOptionalDouble() {
    TriConsumer<OptionalDouble, Double, Double> asserter = (optionalValue1, value2, expectedResult) -> {
      assertEquals(expectedResult, maxAllowingOptionalDouble(optionalValue1, value2), 1e-8);
      assertEquals(expectedResult, maxAllowingOptionalDouble(value2, optionalValue1), 1e-8);
    };

    asserter.accept(OptionalDouble.of(2.2), 1.1, 2.2);
    asserter.accept(OptionalDouble.of(2.2), 3.3, 3.3);

    asserter.accept(OptionalDouble.empty(), -3.3, -3.3);
    asserter.accept(OptionalDouble.empty(),  0.0,  0.0);
    asserter.accept(OptionalDouble.empty(),  3.3,  3.3);
  }

  @Test
  public void testMinAllowingOptionalDouble() {
    TriConsumer<OptionalDouble, Double, Double> asserter = (optionalValue1, value2, expectedResult) -> {
      assertEquals(expectedResult, minAllowingOptionalDouble(optionalValue1, value2), 1e-8);
      assertEquals(expectedResult, minAllowingOptionalDouble(value2, optionalValue1), 1e-8);
    };

    asserter.accept(OptionalDouble.of(2.2), 1.1, 1.1);
    asserter.accept(OptionalDouble.of(2.2), 3.3, 2.2);

    asserter.accept(OptionalDouble.empty(), -3.3, -3.3);
    asserter.accept(OptionalDouble.empty(),  0.0,  0.0);
    asserter.accept(OptionalDouble.empty(),  3.3,  3.3);
  }

}
