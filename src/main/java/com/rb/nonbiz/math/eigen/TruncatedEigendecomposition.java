package com.rb.nonbiz.math.eigen;

import com.google.common.annotations.VisibleForTesting;
import com.rb.biz.investing.strategy.optbased.di.RealizedVolatilities;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.stream.Stream;

import static com.rb.nonbiz.math.eigen.FactorLoadings.factorLoadings;

/**
 * The eigendecompositions we construct and store in our protobuf files usually cover something like 70% of
 * 'explainability', i.e. explain 70% of the total variance. This is the point of eigendecompositions anyway,
 * i.e. to store information only about the more important dimensions. But this may result in a lot of eigendimensions
 * being stored. Put another way, the factor loadings for the different instruments in the eigendecomposition
 * may end up being way more than we actually care to use.
 *
 * It is possibly to further restrict this by specifying a maximum number of eigendimensions to look at.
 * As an actual example, an eigendecomposition may have 58 eigendimensions, but we may want to specify that we
 * will only look at the 10 biggest ones. Of course, the fewer of these we look at, the less good we will be about
 * keeping a flat factor risk. On the other hand, this makes optimizations faster, and if the smaller eigendimensions
 * are small enough not to matter, this is a worthwhile tradeoff to make.
 */
public class TruncatedEigendecomposition<K extends Investable> {

  private final Eigendecomposition<K> eigendecomposition;
  private final EigenDimensionIndex maxValidEigenDimensionIndex;

  private TruncatedEigendecomposition(
      Eigendecomposition<K> eigendecomposition,
      EigenDimensionIndex maxValidEigenDimensionIndex) {
    this.eigendecomposition = eigendecomposition;
    this.maxValidEigenDimensionIndex = maxValidEigenDimensionIndex;
  }

  public static <K extends Investable> TruncatedEigendecomposition<K> truncatedEigendecomposition(
      Eigendecomposition<K> eigendecomposition,
      EigenDimensionIndex maxValidEigenDimensionIndex) {
    RBPreconditions.checkArgument(
        maxValidEigenDimensionIndex.getRawEigenDimensionIndex() < eigendecomposition.getNumRetainedEigenpairs(),
        "This eigendecomposition retains %s eigenpairs, so the max index (for truncation purposes) can't be %s , which is higher",
        eigendecomposition.getNumRetainedEigenpairs(), maxValidEigenDimensionIndex);
    return new TruncatedEigendecomposition<>(eigendecomposition, maxValidEigenDimensionIndex);
  }

  // Avoid using this when possible
  @VisibleForTesting
  public Eigendecomposition<K> getEigendecomposition() {
    return eigendecomposition;
  }

  public RealizedVolatilities<K> getRealizedVolatilities() {
    return eigendecomposition.getRealizedVolatilities();
  }

  public EigenDimensionIndex getMaxValidEigenDimensionIndex() {
    return maxValidEigenDimensionIndex;
  }

  public int getNumEigenDimensionsAfterTruncation() {
    return maxValidEigenDimensionIndex.getRawEigenDimensionIndex() + 1;
  }

  // This is safer to use than retrieving a single loading off the plain Eigendecomposition,
  // because we perform an extra check.
  public double getFactorLoading(K key, EigenDimensionIndex eigenDimensionIndex) {
    int index = eigenDimensionIndex.getRawEigenDimensionIndex();
    RBPreconditions.checkArgument(
        index <= maxValidEigenDimensionIndex.getRawEigenDimensionIndex(),
        "For key k, you requested the %s-th loading (0-based), but we truncated the eigendecomposition so max valid is %s",
        key, index, maxValidEigenDimensionIndex);
    return eigendecomposition.getFactorLoadings(key).getLoading(index);
  }

  public FactorLoadings getTruncatedFactorLoadings(K key) {
    double[] truncatedLoadings = new double[maxValidEigenDimensionIndex.getRawEigenDimensionIndex() + 1];
    System.arraycopy(
        eigendecomposition.getFactorLoadings(key).getLoadings(), 0,
        truncatedLoadings, 0,
        truncatedLoadings.length);
    return factorLoadings(truncatedLoadings);
  }

  public Stream<Eigenpair> getTruncatedEigenpairsInDescendingEigenvaluesAsStream() {
    return eigendecomposition.getEigenpairsInDescendingEigenvalues()
        .stream()
        .limit(getNumEigenDimensionsAfterTruncation());
  }

  @Override
  public String toString() {
    return Strings.format("[TE maxIx= %s ; %s TE]", maxValidEigenDimensionIndex, eigendecomposition);
  }

}
