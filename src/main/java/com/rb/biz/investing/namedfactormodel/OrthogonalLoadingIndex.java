package com.rb.biz.investing.namedfactormodel;

import com.rb.nonbiz.math.eigen.EigenDimensionIndex;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A numeric array-like index to the n-th orthogonal loading in a {@link NamedFactorModel}.
 *
 * Similar to {@link EigenDimensionIndex}, except that it applies to the orthogonal loadings created from
 * orthogonalizing a named-factor model's (typically) non-orthogonal loadings.
 */
public class OrthogonalLoadingIndex {

  private final int rawOrthogonalLoadingIndex;

  private OrthogonalLoadingIndex(int rawOrthogonalLoadingIndex) {
    this.rawOrthogonalLoadingIndex = rawOrthogonalLoadingIndex;
  }
  
  public static OrthogonalLoadingIndex orthogonalLoadingIndex(int rawOrthogonalLoadingIndex) {
    RBPreconditions.checkArgument(
        rawOrthogonalLoadingIndex >= 0,
        "Dimensions start at 0, so we can't have negative values: %s",
        rawOrthogonalLoadingIndex);
    RBPreconditions.checkArgument(
        rawOrthogonalLoadingIndex <= 500,
        "We will probably never have more than 500 factors in any named-factor model: %s",
        rawOrthogonalLoadingIndex);
    return new OrthogonalLoadingIndex(rawOrthogonalLoadingIndex);
  }

  public int getRawOrthogonalLoadingIndex() {
    return rawOrthogonalLoadingIndex;
  }

  @Override
  public String toString() {
    return Integer.toString(rawOrthogonalLoadingIndex);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OrthogonalLoadingIndex that = (OrthogonalLoadingIndex) o;

    return rawOrthogonalLoadingIndex == that.rawOrthogonalLoadingIndex;
  }

  @Override
  public int hashCode() {
    return rawOrthogonalLoadingIndex;
  }

}
