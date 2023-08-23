package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Iterator;

import static com.rb.nonbiz.math.sequence.GeometricProgression.GeometricProgressionBuilder.geometricProgressionBuilder;
import static com.rb.nonbiz.math.sequence.GeometricProgression.singleValueGeometricProgression;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.assertEquals;

public class GeometricProgressionTest extends RBTestMatcher<GeometricProgression> {

  @Test
  public void testGet() {
    Iterator<Double> iterator = geometricProgressionBuilder()
        .setInitialValue(100.0)
        .setCommonRatio(2.0)
        .build()
        .iterator();
    assertEquals(100, iterator.next(), 1e-8);
    assertEquals(200, iterator.next(), 1e-8);
    assertEquals(400, iterator.next(), 1e-8);
    assertEquals(800, iterator.next(), 1e-8);
  }

  @Override
  public GeometricProgression makeTrivialObject() {
    return singleValueGeometricProgression(1);
  }

  @Override
  public GeometricProgression makeNontrivialObject() {
    return geometricProgressionBuilder()
        .setInitialValue(1.1)
        .setCommonRatio(3.3)
        .build();
  }

  @Override
  public GeometricProgression makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return geometricProgressionBuilder()
        .setInitialValue(1.1 + e)
        .setCommonRatio(3.3 + e)
        .build();
  }

  @Override
  protected boolean willMatch(GeometricProgression expected, GeometricProgression actual) {
    return geometricProgressionMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<GeometricProgression> geometricProgressionMatcher(GeometricProgression expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getInitialValue(), DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getCommonRatio(),  DEFAULT_EPSILON_1e_8));
  }

}
