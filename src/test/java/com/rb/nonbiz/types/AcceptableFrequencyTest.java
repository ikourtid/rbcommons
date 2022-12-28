package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.preciseValueRangeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.AcceptableFrequency.always;
import static com.rb.nonbiz.types.AcceptableFrequency.atLeastKTimesOutOfN;
import static com.rb.nonbiz.types.AcceptableFrequency.atLeastOnce;
import static com.rb.nonbiz.types.AcceptableFrequency.kTimesOutOfN;
import static com.rb.nonbiz.types.AcceptableFrequency.lessOftenThan;
import static com.rb.nonbiz.types.AcceptableFrequency.moreOftenThan;
import static com.rb.nonbiz.types.AcceptableFrequency.never;
import static com.rb.nonbiz.types.AcceptableFrequency.notAlways;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AcceptableFrequencyTest extends RBTestMatcher<AcceptableFrequency> {

  @Test
  public void checkStandardValues() {
    assertTrue(never().isAcceptable(UNIT_FRACTION_0));
    assertFalse(never().isAcceptable(unitFraction(0.01)));
    assertFalse(never().isAcceptable(unitFraction(0.29)));
    assertFalse(never().isAcceptable(unitFraction(0.3)));
    assertFalse(never().isAcceptable(unitFraction(0.31)));
    assertFalse(never().isAcceptable(unitFraction(0.99)));
    assertFalse(never().isAcceptable(UNIT_FRACTION_1));

    assertFalse(atLeastOnce().isAcceptable(UNIT_FRACTION_0));
    assertTrue(atLeastOnce().isAcceptable(unitFraction(0.01)));
    assertTrue(atLeastOnce().isAcceptable(unitFraction(0.29)));
    assertTrue(atLeastOnce().isAcceptable(unitFraction(0.3)));
    assertTrue(atLeastOnce().isAcceptable(unitFraction(0.31)));
    assertTrue(atLeastOnce().isAcceptable(unitFraction(0.99)));
    assertTrue(atLeastOnce().isAcceptable(UNIT_FRACTION_1));

    assertFalse(always().isAcceptable(UNIT_FRACTION_0));
    assertFalse(always().isAcceptable(unitFraction(0.01)));
    assertFalse(always().isAcceptable(unitFraction(0.29)));
    assertFalse(always().isAcceptable(unitFraction(0.3)));
    assertFalse(always().isAcceptable(unitFraction(0.31)));
    assertFalse(always().isAcceptable(unitFraction(0.99)));
    assertTrue(always().isAcceptable(UNIT_FRACTION_1));

    assertTrue(notAlways().isAcceptable(UNIT_FRACTION_0));
    assertTrue(notAlways().isAcceptable(unitFraction(0.01)));
    assertTrue(notAlways().isAcceptable(unitFraction(0.29)));
    assertTrue(notAlways().isAcceptable(unitFraction(0.3)));
    assertTrue(notAlways().isAcceptable(unitFraction(0.31)));
    assertTrue(notAlways().isAcceptable(unitFraction(0.99)));
    assertFalse(notAlways().isAcceptable(UNIT_FRACTION_1));

    assertFalse(moreOftenThan(unitFraction(0.3)).isAcceptable(UNIT_FRACTION_0));
    assertFalse(moreOftenThan(unitFraction(0.3)).isAcceptable(unitFraction(0.01)));
    assertFalse(moreOftenThan(unitFraction(0.3)).isAcceptable(unitFraction(0.29)));
    assertTrue(moreOftenThan(unitFraction(0.3)).isAcceptable(unitFraction(0.3))); // it really means >=
    assertTrue(moreOftenThan(unitFraction(0.3)).isAcceptable(unitFraction(0.31)));
    assertTrue(moreOftenThan(unitFraction(0.3)).isAcceptable(unitFraction(0.99)));
    assertTrue(moreOftenThan(unitFraction(0.3)).isAcceptable(UNIT_FRACTION_1));

    assertTrue(lessOftenThan(unitFraction(0.3)).isAcceptable(UNIT_FRACTION_0));
    assertTrue(lessOftenThan(unitFraction(0.3)).isAcceptable(unitFraction(0.01)));
    assertTrue(lessOftenThan(unitFraction(0.3)).isAcceptable(unitFraction(0.29)));
    assertTrue(lessOftenThan(unitFraction(0.3)).isAcceptable(unitFraction(0.3))); // it really means <=
    assertFalse(lessOftenThan(unitFraction(0.3)).isAcceptable(unitFraction(0.31)));
    assertFalse(lessOftenThan(unitFraction(0.3)).isAcceptable(unitFraction(0.99)));
    assertFalse(lessOftenThan(unitFraction(0.3)).isAcceptable(UNIT_FRACTION_1));
  }

  @Test
  public void testKTimesOutOfN() {
    // must have 0 < k < n
    assertIllegalArgumentException( () -> kTimesOutOfN( 0, -2));
    assertIllegalArgumentException( () -> kTimesOutOfN( 1, -2));

    assertIllegalArgumentException( () -> kTimesOutOfN( 0,  0));

    assertIllegalArgumentException( () -> kTimesOutOfN( 0,  1));
    assertIllegalArgumentException( () -> kTimesOutOfN( 1,  1));

    assertIllegalArgumentException( () -> kTimesOutOfN(-1,  2));
    assertIllegalArgumentException( () -> kTimesOutOfN( 0,  2));
    assertIllegalArgumentException( () -> kTimesOutOfN( 3,  2));

    assertTrue( kTimesOutOfN(1, 3).isAcceptable(unitFraction(1, 3)));
    assertFalse(kTimesOutOfN(1, 3).isAcceptable(UNIT_FRACTION_0));
    assertFalse(kTimesOutOfN(1, 3).isAcceptable(UNIT_FRACTION_1));
  }

  @Test
  public void testAtLeastKTimesOutOfN() {
    // must have 0 < k < n
    assertIllegalArgumentException( () -> atLeastKTimesOutOfN( 0, -2));
    assertIllegalArgumentException( () -> atLeastKTimesOutOfN( 1, -2));

    assertIllegalArgumentException( () -> atLeastKTimesOutOfN( 0,  0));

    assertIllegalArgumentException( () -> atLeastKTimesOutOfN( 0,  1));
    assertIllegalArgumentException( () -> atLeastKTimesOutOfN( 1,  1));

    assertIllegalArgumentException( () -> atLeastKTimesOutOfN(-1,  2));
    assertIllegalArgumentException( () -> atLeastKTimesOutOfN( 0,  2));
    assertIllegalArgumentException( () -> atLeastKTimesOutOfN( 3,  2));

    assertFalse(atLeastKTimesOutOfN(1, 3).isAcceptable(UNIT_FRACTION_0));
    assertTrue( atLeastKTimesOutOfN(1, 3).isAcceptable(unitFraction(1, 3)));
    assertTrue( atLeastKTimesOutOfN(1, 3).isAcceptable(unitFraction(2, 3)));
    assertTrue( atLeastKTimesOutOfN(1, 3).isAcceptable(UNIT_FRACTION_1));
  }

  @Test
  public void testToString() {
    assertEquals("always", always().toString());
    assertEquals("never", never().toString());
    assertEquals("at least 80 % of times", moreOftenThan(unitFraction(0.8)).toString());
    assertEquals("at least 81 % of times", moreOftenThan(unitFraction(0.81)).toString());
    assertEquals("at least 81 % of times", moreOftenThan(unitFraction(0.8111)).toString());
    assertEquals("at most 80 % of times", lessOftenThan(unitFraction(0.8)).toString());
    assertEquals("at most 81 % of times", lessOftenThan(unitFraction(0.81)).toString());
    assertEquals("at most 81 % of times", lessOftenThan(unitFraction(0.8111)).toString());
  }

  @Override
  public AcceptableFrequency makeTrivialObject() {
    return always();
  }

  @Override
  public AcceptableFrequency makeNontrivialObject() {
    return atLeastOnce();
  }

  @Override
  public AcceptableFrequency makeMatchingNontrivialObject() {
    return atLeastOnce();
  }

  @Override
  protected boolean willMatch(AcceptableFrequency expected, AcceptableFrequency actual) {
    return acceptableFrequencyMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<AcceptableFrequency> acceptableFrequencyMatcher(AcceptableFrequency expected) {
    return makeMatcher(expected, actual ->
        preciseValueRangeMatcher(expected.getFrequencyRange(), DEFAULT_EPSILON_1e_8)
            .matches(actual.getFrequencyRange()));
  }

}
