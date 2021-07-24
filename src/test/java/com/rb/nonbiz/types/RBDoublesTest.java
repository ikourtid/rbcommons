package com.rb.nonbiz.types;

import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.types.RBDoubles.average;
import static com.rb.nonbiz.types.RBDoubles.epsilonCompareDoubles;
import static com.rb.nonbiz.types.RBDoubles.epsilonCompareDoublesAllowingEpsilonOfZero;
import static com.rb.nonbiz.types.RBDoubles.getDoubleAsLongAssumingIsRound;
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
  public void testEpsilonCompare_epsilonMustBePositiveAndSmall() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    assertIllegalArgumentException( () -> epsilonCompareDoubles(DUMMY_DOUBLE, DUMMY_DOUBLE, -999, visitor));
    assertIllegalArgumentException( () -> epsilonCompareDoubles(DUMMY_DOUBLE, DUMMY_DOUBLE, -1, visitor));
    assertIllegalArgumentException( () -> epsilonCompareDoubles(DUMMY_DOUBLE, DUMMY_DOUBLE, -1e-9, visitor));
    assertIllegalArgumentException( () -> epsilonCompareDoubles(DUMMY_DOUBLE, DUMMY_DOUBLE, 0, visitor));
    assertIllegalArgumentException( () -> epsilonCompareDoubles(DUMMY_DOUBLE, DUMMY_DOUBLE, 10_000, visitor));
    assertIllegalArgumentException( () -> epsilonCompareDoubles(DUMMY_DOUBLE, DUMMY_DOUBLE, 1e9, visitor));

    assertEquals("==", epsilonCompareDoubles(DUMMY_DOUBLE, DUMMY_DOUBLE, 1e-8, visitor));
  }

  @Test
  public void testEpsilonCompareAllowingEpsilonOfZero_epsilonMustBeNonNegativeAndSmall() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    assertIllegalArgumentException( () -> epsilonCompareDoublesAllowingEpsilonOfZero(DUMMY_DOUBLE, DUMMY_DOUBLE, -999, visitor));
    assertIllegalArgumentException( () -> epsilonCompareDoublesAllowingEpsilonOfZero(DUMMY_DOUBLE, DUMMY_DOUBLE, -1, visitor));
    assertIllegalArgumentException( () -> epsilonCompareDoublesAllowingEpsilonOfZero(DUMMY_DOUBLE, DUMMY_DOUBLE, -1e-9, visitor));
    assertIllegalArgumentException( () -> epsilonCompareDoublesAllowingEpsilonOfZero(DUMMY_DOUBLE, DUMMY_DOUBLE, 10_000, visitor));
    assertIllegalArgumentException( () -> epsilonCompareDoublesAllowingEpsilonOfZero(DUMMY_DOUBLE, DUMMY_DOUBLE, 1e9, visitor));

    assertEquals("==", epsilonCompareDoublesAllowingEpsilonOfZero(DUMMY_DOUBLE, DUMMY_DOUBLE, 0,    visitor));
    assertEquals("==", epsilonCompareDoublesAllowingEpsilonOfZero(DUMMY_DOUBLE, DUMMY_DOUBLE, 1e-8, visitor));
  }

  @Test
  public void testEpsilonCompare_generalCase() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    assertEquals("<",  epsilonCompareDoubles(77,        88,        1e-8, visitor));
    assertEquals(">",  epsilonCompareDoubles(88,        77,        1e-8, visitor));
    assertEquals("==", epsilonCompareDoubles(88,        88,        1e-8, visitor));
    assertEquals("==", epsilonCompareDoubles(88 + 1e-9, 88,        1e-8, visitor));
    assertEquals("==", epsilonCompareDoubles(88,        88 + 1e-9, 1e-8, visitor));
    assertEquals(">",  epsilonCompareDoubles(88 + 1e-9, 88,        1e-10, visitor));
    assertEquals("<",  epsilonCompareDoubles(88,        88 + 1e-9, 1e-10, visitor));
  }

  @Test
  public void testEpsilonCompareAllowingEpsilonOfZero_generalCase() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    assertEquals("<",  epsilonCompareDoublesAllowingEpsilonOfZero(77,        88,        1e-8, visitor));
    assertEquals(">",  epsilonCompareDoublesAllowingEpsilonOfZero(88,        77,        1e-8, visitor));
    assertEquals("==", epsilonCompareDoublesAllowingEpsilonOfZero(88,        88,        1e-8, visitor));
    assertEquals("==", epsilonCompareDoublesAllowingEpsilonOfZero(88 + 1e-9, 88,        1e-8, visitor));
    assertEquals("==", epsilonCompareDoublesAllowingEpsilonOfZero(88,        88 + 1e-9, 1e-8, visitor));
    assertEquals(">",  epsilonCompareDoublesAllowingEpsilonOfZero(88 + 1e-9, 88,        1e-10, visitor));
    assertEquals("<",  epsilonCompareDoublesAllowingEpsilonOfZero(88,        88 + 1e-9, 1e-10, visitor));
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
    double e = 1e-8; // epsilon
    BiConsumer<Double, Integer> assertResult = (doubleValue, expectedLong) ->
        assertEquals((long) expectedLong, getDoubleAsLongAssumingIsRound(doubleValue, e));
    DoubleConsumer assertIllegal = doubleValue ->
        assertIllegalArgumentException( () -> getDoubleAsLongAssumingIsRound(doubleValue, e));

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

}
