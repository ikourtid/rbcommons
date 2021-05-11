package com.rb.nonbiz.types;

import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;
import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.types.RBDoubles.average;
import static com.rb.nonbiz.types.RBDoubles.epsilonCompareDoubles;
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
  public void testAverage() {
    assertEquals(0, average(0, 0), 1e-8);
    assertEquals(5.5, average(4.4, 6.6), 1e-8);
    assertEquals(-5.5, average(-4.4, -6.6), 1e-8);
    assertEquals(4, average(-0.5, 8.5), 1e-8);
  }

}
