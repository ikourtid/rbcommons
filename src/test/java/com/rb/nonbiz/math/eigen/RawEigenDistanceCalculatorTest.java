package com.rb.nonbiz.math.eigen;

import com.rb.biz.investing.strategy.optbased.rebal.lp.EigenSubObjectiveInstructionsForCoefficients;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.Arrays;

import static com.rb.biz.investing.strategy.optbased.rebal.lp.EigenSubObjectiveInstructionsForCoefficients.EigenSubObjectiveMinimizesStandardDeviation.eigenSubObjectiveMinimizesStandardDeviation;
import static com.rb.biz.investing.strategy.optbased.rebal.lp.EigenSubObjectiveInstructionsForCoefficients.EigenSubObjectiveMinimizesVariance.eigenSubObjectiveMinimizesVariance;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.math.EigenDistance.eigenDistance;
import static com.rb.nonbiz.math.EigenDistance.eigenDistanceInVarianceSpace;
import static com.rb.nonbiz.math.EigenDistanceTest.eigenDistanceMatcher;
import static com.rb.nonbiz.math.EigenDistanceTest.zeroEigenDistance;
import static com.rb.nonbiz.math.eigen.DecreasingPositiveDoubles.decreasingPositiveDoubles;
import static com.rb.nonbiz.math.eigen.EigenDistanceCalculationInstructions.eigenDistanceCalculationInstructions;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class RawEigenDistanceCalculatorTest extends RBTest<RawEigenDistanceCalculator> {

  private final double[] VECTOR_A = new double[] { 50, 60 };
  private final double[] VECTOR_B = new double[] { 53, 64 };
  private final double[] VECTOR_C = new double[] { -50, -60 };
  private final double[] VECTOR_D = new double[] { -53, -64 };

  private final EigenDistanceCalculationInstructions DUMMY_MULTIPLIERS_2 = makeInstructions(4.4, 3.3);
  private final EigenDistanceCalculationInstructions NO_MULTIPLIERS = makeInstructions(1.0, 1.0);

  @Test
  public void distanceToSelfIsZero() {
    for (double[] vector : rbSetOf(VECTOR_A, VECTOR_B, VECTOR_C, VECTOR_D)) {
      assertThat(
          makeTestObject().calculateEigenDistance(vector, vector, DUMMY_MULTIPLIERS_2, 2),
          eigenDistanceMatcher(zeroEigenDistance()));
      assertThat(
          makeTestObject().calculateEigenDistance(vector, vector, DUMMY_MULTIPLIERS_2, 1),
          eigenDistanceMatcher(zeroEigenDistance()));
    }
  }

  @Test
  public void distancesOfSpecificPairsAreCorrect_noMultipliers() {
    assertThat(
        makeTestObject().calculateEigenDistance(new double[] { 50, 60 }, new double[] { 53, 64 }, NO_MULTIPLIERS, 2),
        eigenDistanceMatcher(eigenDistanceInVarianceSpace(
            doubleExplained(7, (53 - 50) + (64 - 60)), doubleExplained(5, Math.sqrt((53 - 50) * (53 - 50) + (64 - 60) * (64 - 60)))
        )));
    assertThat(
        makeTestObject().calculateEigenDistance(new double[] { 30, 0 }, new double[] { 40, 0 }, NO_MULTIPLIERS, 2),
        eigenDistanceMatcher(eigenDistanceInVarianceSpace(10, 10)));
    assertThat(
        makeTestObject().calculateEigenDistance(new double[] { 0, 30 }, new double[] { 0, 40 }, NO_MULTIPLIERS, 2),
        eigenDistanceMatcher(eigenDistanceInVarianceSpace(10, 10)));
    assertThat(
        makeTestObject().calculateEigenDistance(new double[] { -50, -60 }, new double[] { -53, -64 }, NO_MULTIPLIERS, 2),
        eigenDistanceMatcher(eigenDistanceInVarianceSpace(7, 5)));
  }

  @Test
  public void distancesOfSpecificPairsAreCorrect_hasMultipliers() {
    for (EigenSubObjectiveInstructionsForCoefficients eigenSubObjectiveInstructionsForCoefficients : rbSetOf(
        eigenSubObjectiveMinimizesVariance(),
        eigenSubObjectiveMinimizesStandardDeviation())) {
      assertThat(
          makeTestObject().calculateEigenDistance(
              new double[] { 60, 50 },
              new double[] { 68, 62 },
              makeInstructions(eigenSubObjectiveInstructionsForCoefficients, 0.5, 0.25),
              2),
          eigenDistanceMatcher(eigenDistance(
              eigenSubObjectiveInstructionsForCoefficients,
              doubleExplained(7, 0.25 * (62 - 50) + 0.5 * (68 - 60)), doubleExplained(5, Math.sqrt(Math.pow(0.25 * (62 - 50), 2) + Math.pow(0.5 * (68 - 60), 2)))
          )));
    }
  }

  @Test
  public void calculationIsCommutativeForEqualMultipliers() {
    for (EigenSubObjectiveInstructionsForCoefficients eigenSubObjectiveInstructionsForCoefficients : rbSetOf(
        eigenSubObjectiveMinimizesVariance(),
        eigenSubObjectiveMinimizesStandardDeviation())) {
      for (EigenDistanceCalculationInstructions equalMultipliers : rbSetOf(
          makeInstructions(eigenSubObjectiveInstructionsForCoefficients, 1.1, 1.1),
          makeInstructions(eigenSubObjectiveInstructionsForCoefficients, 2.2, 2.2))) {
        for (double[] vector1 : rbSetOf(VECTOR_A, VECTOR_B, VECTOR_C, VECTOR_D)) {
          for (double[] vector2 : rbSetOf(VECTOR_A, VECTOR_B, VECTOR_C, VECTOR_D)) {
            assertThat(
                "The calculation is commutative, i.e. the distance between A and B is the same as between B and A",
                makeTestObject().calculateEigenDistance(vector1, vector2, equalMultipliers, 2),
                eigenDistanceMatcher(
                    makeTestObject().calculateEigenDistance(vector2, vector1, equalMultipliers, 2)));
            assertThat(
                "The calculation is commutative, i.e. the distance between A and B is the same as between B and A",
                makeTestObject().calculateEigenDistance(vector1, vector2, equalMultipliers, 1),
                eigenDistanceMatcher(
                    makeTestObject().calculateEigenDistance(vector2, vector1, equalMultipliers, 1)));
          }
        }
      }
    }
  }

  @Test
  public void has3dimensions_onlyLooksAt2_computesCorrectAngles() {
    double[] vector1 = new double[] { 50, 60, 123.45 };
    double[] vector2 = new double[] { 53, 64, 54.321 };
    assertThat(
        makeTestObject().calculateEigenDistance(vector1, vector2, makeInstructions(1.0, 1.0), 2),
        eigenDistanceMatcher(eigenDistanceInVarianceSpace(7, 5)));
    assertThat(
        makeTestObject().calculateEigenDistance(vector1, vector2, makeInstructions(1.0, 1.0, 0.1234), 3),
        not(eigenDistanceMatcher(eigenDistanceInVarianceSpace(7, 5))));
  }

  @Test
  public void requestsTooManyDimensions_throws() {
    assertIllegalArgumentException( () -> makeTestObject().calculateEigenDistance(VECTOR_A, VECTOR_B, DUMMY_MULTIPLIERS_2, 3));
  }

  @Test
  public void notEnoughMultipliers_throws() {
    assertIllegalArgumentException( () -> makeTestObject().calculateEigenDistance(
        VECTOR_A, VECTOR_B, makeInstructions(DUMMY_DOUBLE), 2));
    EigenDistance doesNotThrow = makeTestObject().calculateEigenDistance(
        VECTOR_A, VECTOR_B, DUMMY_MULTIPLIERS_2, 2);

  }

  // Contrast this with angle calculation, where we can't have a zero vector
  @Test
  public void oneOrMoreVectorsAreZero_doesNotThrow() {
    double[] zero = new double[] { 0, 0 };
    EigenDistance doesNotThrow;
    doesNotThrow = makeTestObject().calculateEigenDistance(VECTOR_A, zero, DUMMY_MULTIPLIERS_2, 2);
    doesNotThrow = makeTestObject().calculateEigenDistance(zero, VECTOR_A, DUMMY_MULTIPLIERS_2, 2);
    doesNotThrow = makeTestObject().calculateEigenDistance(zero, zero, DUMMY_MULTIPLIERS_2, 2);
  }

  @Test
  public void requestsLessThanOneDimension_throws() {
    assertIllegalArgumentException( () -> makeTestObject().calculateEigenDistance(VECTOR_B, VECTOR_D, DUMMY_MULTIPLIERS_2, 0));
    assertIllegalArgumentException( () -> makeTestObject().calculateEigenDistance(VECTOR_B, VECTOR_D, DUMMY_MULTIPLIERS_2, -1));
    EigenDistance doesNotThrow;
    doesNotThrow = makeTestObject().calculateEigenDistance(VECTOR_B, VECTOR_D, DUMMY_MULTIPLIERS_2, 1);
    doesNotThrow = makeTestObject().calculateEigenDistance(VECTOR_B, VECTOR_D, DUMMY_MULTIPLIERS_2, 2);
  }

  @Test
  public void emptyVectors_throws() {
    assertIllegalArgumentException( () -> makeTestObject().calculateEigenDistance(new double[] {}, new double[] {}, DUMMY_MULTIPLIERS_2, 0));
    assertIllegalArgumentException( () -> makeTestObject().calculateEigenDistance(new double[] {}, new double[] {}, DUMMY_MULTIPLIERS_2, 1));
  }

  private final EigenDistanceCalculationInstructions makeInstructions(
      EigenSubObjectiveInstructionsForCoefficients eigenSubObjectiveInstructionsForCoefficients,
      Double...multipliers) {
    return eigenDistanceCalculationInstructions(
        eigenSubObjectiveInstructionsForCoefficients,
        decreasingPositiveDoubles(Arrays.asList(multipliers)));
  }

  private final EigenDistanceCalculationInstructions makeInstructions(
      Double...multipliers) {
    return eigenDistanceCalculationInstructions(
        eigenSubObjectiveMinimizesVariance(),
        decreasingPositiveDoubles(Arrays.asList(multipliers)));
  }

  @Override
  protected RawEigenDistanceCalculator makeTestObject() {
    return new RawEigenDistanceCalculator();
  }

}
