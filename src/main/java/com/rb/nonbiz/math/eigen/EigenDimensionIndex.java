package com.rb.nonbiz.math.eigen;

import com.rb.biz.investing.namedfactormodel.OrthogonalLoadingIndex;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A numeric index that refers to the nth principal component in an eigendecomposition.
 *
 * See also {@link OrthogonalLoadingIndex}.
 */
public class EigenDimensionIndex {

  private final int rawEigenDimensionIndex;

  private EigenDimensionIndex(int rawEigenDimensionIndex) {
    this.rawEigenDimensionIndex = rawEigenDimensionIndex;
  }
  
  public static EigenDimensionIndex eigenDimensionIndex(int rawEigenDimensionIndex) {
    RBPreconditions.checkArgument(
        rawEigenDimensionIndex >= 0,
        "Dimensions start at 0, so we can't have negative values: %s",
        rawEigenDimensionIndex);
    RBPreconditions.checkArgument(
        rawEigenDimensionIndex <= 500,
        "We probably never want to retain more than 500 (super-loose upper bound) dimensions: %s",
        rawEigenDimensionIndex);
    return new EigenDimensionIndex(rawEigenDimensionIndex);
  }

  public int getRawEigenDimensionIndex() {
    return rawEigenDimensionIndex;
  }

  @Override
  public String toString() {
    return Integer.toString(rawEigenDimensionIndex);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EigenDimensionIndex that = (EigenDimensionIndex) o;

    return rawEigenDimensionIndex == that.rawEigenDimensionIndex;
  }

  @Override
  public int hashCode() {
    return rawEigenDimensionIndex;
  }

}
