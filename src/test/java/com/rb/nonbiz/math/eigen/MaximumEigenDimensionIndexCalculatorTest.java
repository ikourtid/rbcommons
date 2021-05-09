package com.rb.nonbiz.math.eigen;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.math.eigen.EigenDimensionIndex.eigenDimensionIndex;
import static com.rb.nonbiz.math.eigen.EigenDimensionIndexTest.eigenDimensionIndexMatcher;
import static com.rb.nonbiz.math.eigen.EigenExplainabilityRestrictions.emptyEigenExplainabilityRestrictions;
import static com.rb.nonbiz.math.eigen.EigenExplainabilityRestrictions.restrictNumEigenvectors;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static org.hamcrest.MatcherAssert.assertThat;

public class MaximumEigenDimensionIndexCalculatorTest extends RBTest<MaximumEigenDimensionIndexCalculator> {

  private final Eigendecomposition<InstrumentId> EIGENDECOMPOSITION =
      new EigendecompositionTest().makeNontrivialObject();
  private final int NUM_EIGEN_DIMENSIONS_UNRESTRICTED = intExplained(2, EIGENDECOMPOSITION.getNumRetainedEigenpairs());

  @Test
  public void noRestrictions_returnsSame() {
    assertResult(eigenDimensionIndex(1), emptyEigenExplainabilityRestrictions());
  }

  @Test
  public void hasMaxNumEigenvectors_returnsMin() {
    assertResult(eigenDimensionIndex(1), restrictNumEigenvectors(999));
    assertResult(eigenDimensionIndex(1), restrictNumEigenvectors(intExplained(2, NUM_EIGEN_DIMENSIONS_UNRESTRICTED)));
    assertResult(eigenDimensionIndex(0), restrictNumEigenvectors(1));

    // Issue #982 : MaxEigenExplainabilityFraction is not handled yet
    //    assertResult(eigenDimensionIndex(1), restrictBothNumEigenvectorsAndExplainability(999, eigenExplainabilityFraction(0.99)));
    //    assertResult(eigenDimensionIndex(1), restrictBothNumEigenvectorsAndExplainability(2, eigenExplainabilityFraction(0.99)));
    //    assertResult(eigenDimensionIndex(0), restrictBothNumEigenvectorsAndExplainability(1, eigenExplainabilityFraction(0.99)));
  }

  @Test
  public void hasMaxEigenExplainabilityFraction_returnsMin() {
    // Issue #982 : not handled yet
  }

  private void assertResult(
      EigenDimensionIndex expected, EigenExplainabilityRestrictions eigenExplainabilityRestrictions) {
    assertThat(
        makeTestObject().calculate(EIGENDECOMPOSITION, eigenExplainabilityRestrictions),
        eigenDimensionIndexMatcher(expected));
  }

  @Override
  protected MaximumEigenDimensionIndexCalculator makeTestObject() {
    return new MaximumEigenDimensionIndexCalculator();
  }

}
