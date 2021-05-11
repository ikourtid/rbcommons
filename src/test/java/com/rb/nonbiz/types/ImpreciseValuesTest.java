package com.rb.nonbiz.types;

import com.rb.nonbiz.math.stats.ZScore;
import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;
import org.junit.Test;

import static com.rb.nonbiz.math.stats.ZScore.zScore;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.types.ImpreciseValues.epsilonCompareImpreciseValues;
import static com.rb.nonbiz.types.RBDoublesTest.comparisonSignVisitor;
import static org.junit.Assert.assertEquals;

public class ImpreciseValuesTest {

  @Test
  public void testEpsilonCompare_epsilonMustBePositiveAndSmall() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    ZScore dummy = zScore(DUMMY_DOUBLE);
    assertIllegalArgumentException( () -> epsilonCompareImpreciseValues(dummy, dummy, -999, visitor));
    assertIllegalArgumentException( () -> epsilonCompareImpreciseValues(dummy, dummy, -1, visitor));
    assertIllegalArgumentException( () -> epsilonCompareImpreciseValues(dummy, dummy, -1e-9, visitor));
    assertIllegalArgumentException( () -> epsilonCompareImpreciseValues(dummy, dummy, 0, visitor));
    assertIllegalArgumentException( () -> epsilonCompareImpreciseValues(dummy, dummy, 10_000, visitor));
    assertIllegalArgumentException( () -> epsilonCompareImpreciseValues(dummy, dummy, 1e9, visitor));

    assertEquals("==", epsilonCompareImpreciseValues(dummy, dummy, 1e-8, visitor));
  }

  @Test
  public void testEpsilonCompare_generalCase() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    ZScore one = zScore(1);
    ZScore ten = zScore(10);
    ZScore slightlyMoreThan10 = zScore(10 + 1e-9);

    assertEquals("<",  epsilonCompareImpreciseValues(one,                ten,                1e-8, visitor));
    assertEquals(">",  epsilonCompareImpreciseValues(ten,                one,                1e-8, visitor));
    assertEquals("==", epsilonCompareImpreciseValues(ten,                ten,                1e-8, visitor));
    assertEquals("==", epsilonCompareImpreciseValues(slightlyMoreThan10, ten,                1e-8, visitor));
    assertEquals("==", epsilonCompareImpreciseValues(ten,                slightlyMoreThan10, 1e-8, visitor));
    assertEquals(">",  epsilonCompareImpreciseValues(slightlyMoreThan10, ten,                1e-10, visitor));
    assertEquals("<",  epsilonCompareImpreciseValues(ten,                slightlyMoreThan10, 1e-10, visitor));
  }

}
