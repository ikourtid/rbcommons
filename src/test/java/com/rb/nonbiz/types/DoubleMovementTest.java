package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.DoubleMovement.doubleMovement;
import static org.junit.Assert.assertEquals;

public class DoubleMovementTest extends RBTestMatcher<DoubleMovement> {

  @Test
  public void testBeforeAndAfter() {
    DoubleMovement doubleMovement = doubleMovement(1.23, 4.56);

    assertEquals(1.23, doubleMovement.getValueBefore(), 1e-8);
    assertEquals(4.56, doubleMovement.getValueAfter(),  1e-8);
  }

  @Test
  public void testJump() {
    assertEquals(   0, doubleMovement(10,  0).getFractionalJump(), 1e-8);
    assertEquals(-0.1, doubleMovement(10,  9).getFractionalJump(), 1e-8);
    assertEquals( 0.1, doubleMovement(10, 11).getFractionalJump(), 1e-8);
  }

  @Override
  public DoubleMovement makeTrivialObject() {
    return doubleMovement(0, 0);
  }

  @Override
  public DoubleMovement makeNontrivialObject() {
    return doubleMovement(1.11, -3.33);
  }

  @Override
  public DoubleMovement makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return doubleMovement(1.11 + e, -3.33 + e);
  }

  @Override
  protected boolean willMatch(DoubleMovement expected, DoubleMovement actual) {
    return doubleMovementMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<DoubleMovement> doubleMovementMatcher(DoubleMovement expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getValueBefore(), 1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getValueAfter(),  1e-8));
  }

}
