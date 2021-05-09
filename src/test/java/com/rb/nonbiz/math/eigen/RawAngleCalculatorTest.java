package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.math.Angle;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.math.Angle.angleInDegrees;
import static com.rb.nonbiz.math.AngleTest.angleMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class RawAngleCalculatorTest extends RBTest<RawAngleCalculator> {

  private final double SQRT_2 = Math.sqrt(2);
  private final double[] VECTOR_A = new double[] {           0,           1 };
  private final double[] VECTOR_B = new double[] {  SQRT_2 / 2,  SQRT_2 / 2 };
  private final double[] VECTOR_C = new double[] {           1,           0 };
  private final double[] VECTOR_D = new double[] { -SQRT_2 / 2, -SQRT_2 / 2 };

  @Test
  public void anglesOfSpecificPairsAreCorrectInUntransformedSpace() {
    assertThat(makeTestObject().calculateAngle(VECTOR_A, VECTOR_B, 2), angleMatcher(angleInDegrees(45)));
    assertThat(makeTestObject().calculateAngle(VECTOR_A, VECTOR_C, 2), angleMatcher(angleInDegrees(90)));
    assertThat(makeTestObject().calculateAngle(VECTOR_A, VECTOR_D, 2), angleMatcher(angleInDegrees(135)));
    assertThat(makeTestObject().calculateAngle(VECTOR_B, VECTOR_C, 2), angleMatcher(angleInDegrees(45)));
    assertThat(makeTestObject().calculateAngle(VECTOR_B, VECTOR_D, 2), angleMatcher(angleInDegrees(180)));
    assertThat(makeTestObject().calculateAngle(VECTOR_C, VECTOR_D, 2), angleMatcher(angleInDegrees(135)));
  }

  @Test
  public void anglesToOneselfAreZero() {
    for (double[] vector : rbSetOf(VECTOR_A, VECTOR_B, VECTOR_C, VECTOR_D)) {
      assertThat(
          "Every vector has an angle of 0 to itself",
          makeTestObject().calculateAngle(vector, vector, 2),
          angleMatcher(angleInDegrees(0)));
    }
  }

  @Test
  public void angleCalculationIsCommutative() {
    for (double[] vector1 : rbSetOf(VECTOR_A, VECTOR_B, VECTOR_C, VECTOR_D)) {
      for (double[] vector2 : rbSetOf(VECTOR_A, VECTOR_B, VECTOR_C, VECTOR_D)) {
        assertThat(
            "The angle calculation is commutative, i.e. the angle between A and B is the same as the angle between B and A",
            makeTestObject().calculateAngle(vector1, vector2, 2),
            angleMatcher(
                makeTestObject().calculateAngle(vector2, vector1, 2)));
      }
    }
  }

  @Test
  public void has3dimensions_onlyLooksAt2_computesCorrectAngles() {
    double[] vector1 = new double[] {           0,           1, 123.45 };
    double[] vector2 = new double[] {  SQRT_2 / 2,  SQRT_2 / 2, 54.321 };
    assertThat(
        makeTestObject().calculateAngle(vector1, vector2, 2),
        angleMatcher(angleInDegrees(45)));
    assertThat(
        makeTestObject().calculateAngle(vector1, vector2, 3),
        not(angleMatcher(angleInDegrees(45))));
  }

  @Test
  public void requestsTooManyDimensions_throws() {
    assertIllegalArgumentException( () -> makeTestObject().calculateAngle(VECTOR_A, VECTOR_B, 3));
  }

  @Test
  public void oneOrMoreVectorsAreZero_throws() {
    double[] zero = new double[] { 0, 0 };
    assertIllegalArgumentException( () -> makeTestObject().calculateAngle(VECTOR_A, zero, 2));
    assertIllegalArgumentException( () -> makeTestObject().calculateAngle(zero, VECTOR_A, 2));
    assertIllegalArgumentException( () -> makeTestObject().calculateAngle(zero, zero, 2));
  }

  @Test
  public void requestsLessThanOneDimension_throws() {
    assertIllegalArgumentException( () -> makeTestObject().calculateAngle(VECTOR_B, VECTOR_D, 0));
    assertIllegalArgumentException( () -> makeTestObject().calculateAngle(VECTOR_B, VECTOR_D, -1));
    Angle doesNotThrow;
    doesNotThrow = makeTestObject().calculateAngle(VECTOR_B, VECTOR_D, 1);
    doesNotThrow = makeTestObject().calculateAngle(VECTOR_B, VECTOR_D, 2);
  }

  @Test
  public void emptyVectors_throws() {
    assertIllegalArgumentException( () -> makeTestObject().calculateAngle(new double[] {}, new double[] {}, 0));
    assertIllegalArgumentException( () -> makeTestObject().calculateAngle(new double[] {}, new double[] {}, 1));
  }

  @Override
  protected RawAngleCalculator makeTestObject() {
    return new RawAngleCalculator();
  }

}
