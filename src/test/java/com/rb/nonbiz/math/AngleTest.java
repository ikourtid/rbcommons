package com.rb.nonbiz.math;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.Angle.angleInDegrees;
import static com.rb.nonbiz.math.Angle.angleWithCosine;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;

public class AngleTest extends RBTestMatcher<Angle> {

  @Test
  public void mustBeValidCosine() {
    assertIllegalArgumentException( () -> angleWithCosine(-1.01));
    Angle doesNotThrow;
    doesNotThrow = angleWithCosine(-1);
    doesNotThrow = angleWithCosine(-0.5);
    doesNotThrow = angleWithCosine(0);
    doesNotThrow = angleWithCosine(0.5);
    doesNotThrow = angleWithCosine(1);
    assertIllegalArgumentException( () -> angleWithCosine(1.01));
  }

  @Test
  public void mustBeValidDegrees() {
    assertIllegalArgumentException( () -> angleInDegrees(-180.01));
    Angle doesNotThrow;
    doesNotThrow = angleInDegrees(-180);
    doesNotThrow = angleInDegrees(-90);
    doesNotThrow = angleInDegrees(0);
    doesNotThrow = angleInDegrees(90);
    doesNotThrow = angleInDegrees(180);
    assertIllegalArgumentException( () -> angleInDegrees(180.01));
  }

  @Test
  public void equivalenceOfConstructors() {
    assertThat(
        angleInDegrees(0),
        angleMatcher(angleWithCosine(1)));
    assertThat(
        angleInDegrees(90),
        angleMatcher(angleWithCosine(0)));
  }

  @Override
  public Angle makeTrivialObject() {
    return angleWithCosine(0);
  }

  @Override
  public Angle makeNontrivialObject() {
    return angleWithCosine(-0.12345);
  }

  @Override
  public Angle makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return angleWithCosine(-0.12345 + e);
  }

  @Override
  protected boolean willMatch(Angle expected, Angle actual) {
    return angleMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<Angle> angleMatcher(Angle expected) {
    return makeMatcher(expected, actual ->
        Math.abs(expected.getCosine() - actual.getCosine()) < 1e-8);
  }

}
