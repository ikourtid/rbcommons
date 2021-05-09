package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.util.RBPreconditions;

import java.util.OptionalInt;

import static com.rb.nonbiz.math.eigen.EigenDimensionIndex.eigenDimensionIndex;

/**
 * Even though the {@link Eigendecomposition}{@code <K>} may only explain up to (say) 70% of the variance of the underlying data,
 * sometimes we want to further restrict things. We do this via the EigenExplainabilityRestrictions.
 * This will tell us how many eigendimensions we should keep.
 */
public class MaximumEigenDimensionIndexCalculator {

  public <K extends Investable> EigenDimensionIndex calculate(
      Eigendecomposition<K> eigenDecomposition,
      EigenExplainabilityRestrictions eigenExplainabilityRestrictions) {
    int maxCount = eigenDecomposition.getNumRetainedEigenpairs();
    OptionalInt maxSpecified = eigenExplainabilityRestrictions.getMaxNumEigenvectors();
    if (maxSpecified.isPresent()) {
      maxCount = Math.min(maxCount, maxSpecified.getAsInt());
    }
    RBPreconditions.checkArgument(
        !eigenExplainabilityRestrictions.getMaxEigenExplainabilityFraction().isPresent(),
        "Issue #982 : we do not yet support MaxEigenExplainabilityFraction");
    return eigenDimensionIndex(maxCount - 1); // 0-based numbering here
  }

}
