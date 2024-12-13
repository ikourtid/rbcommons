package com.rb.nonbiz.search;

import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBComparables.monotonic;

/**
 * A portion of the parameters needed to do a binary search or to do the tightening of the initial lower or upper
 * bounds for the binary search, in certain special cases.
 *
 * @see BinarySearch
 */
public class BinarySearchParameters<X, Y> {

  private final X lowerBoundX;
  private final X upperBoundX;
  private final BinarySearchTerminationPredicate<X, Y> terminationPredicate;
  private final BinarySearchRawParameters<X, Y> binarySearchRawParameters;

  private BinarySearchParameters(
      X lowerBoundX,
      X upperBoundX,
      BinarySearchTerminationPredicate<X, Y> terminationPredicate,
      BinarySearchRawParameters<X, Y> binarySearchRawParameters) {
    this.lowerBoundX = lowerBoundX;
    this.upperBoundX = upperBoundX;
    this.terminationPredicate = terminationPredicate;
    this.binarySearchRawParameters = binarySearchRawParameters;
  }

  public X getLowerBoundX() {
    return lowerBoundX;
  }

  public X getUpperBoundX() {
    return upperBoundX;
  }

  public Comparator<? super X> getComparatorForX() {
    return binarySearchRawParameters.getComparatorForX();
  }

  public Comparator<? super Y> getComparatorForY() {
    return binarySearchRawParameters.getComparatorForY();
  }

  public Y getTargetY() {
    return binarySearchRawParameters.getTargetY();
  }

  public Function<X, Y> getEvaluatorOfX() {
    return binarySearchRawParameters.getEvaluatorOfX();
  }

  public BinarySearchTerminationPredicate<X, Y> getTerminationPredicate() {
    return terminationPredicate;
  }

  public BinaryOperator<X> getMidpointGenerator() {
    return binarySearchRawParameters.getMidpointGenerator();
  }

  public int getMaxIterations() {
    return binarySearchRawParameters.getMaxIterations();
  }


  /**
   * An {@link RBBuilder} that helps you build a {@link BinarySearchParameters} object.
   */
  public static class BinarySearchParametersBuilder<X, Y> implements RBBuilder<BinarySearchParameters<X, Y>> {

    private X lowerBoundX;
    private X upperBoundX;
    private BinarySearchTerminationPredicate<X, Y> terminationPredicate;
    private BinarySearchRawParameters<X, Y> binarySearchRawParameters;

    private BinarySearchParametersBuilder() {}

    public static <X, Y> BinarySearchParametersBuilder<X, Y> binarySearchParametersBuilder() {
      return new BinarySearchParametersBuilder<>();
    }

    public BinarySearchParametersBuilder<X, Y> setLowerBoundX(X lowerBoundX) {
      this.lowerBoundX = checkNotAlreadySet(this.lowerBoundX, lowerBoundX);
      return this;
    }

    public BinarySearchParametersBuilder<X, Y> setUpperBoundX(X upperBoundX) {
      this.upperBoundX = checkNotAlreadySet(this.upperBoundX, upperBoundX);
      return this;
    }

    public BinarySearchParametersBuilder<X, Y> setTerminationPredicate(
        BinarySearchTerminationPredicate<X, Y> terminationPredicate) {
      this.terminationPredicate = checkNotAlreadySet(this.terminationPredicate, terminationPredicate);
      return this;
    }

    public BinarySearchParametersBuilder<X, Y> setBinarySearchRawParameters(BinarySearchRawParameters<X, Y> binarySearchRawParameters) {
      this.binarySearchRawParameters = checkNotAlreadySet(this.binarySearchRawParameters, binarySearchRawParameters);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(lowerBoundX);
      RBPreconditions.checkNotNull(upperBoundX);
      RBPreconditions.checkNotNull(terminationPredicate);

      Y lowerValue = binarySearchRawParameters.getEvaluatorOfX().apply(lowerBoundX);
      Y upperValue = binarySearchRawParameters.getEvaluatorOfX().apply(upperBoundX);
      Comparator<? super X> comparatorForX = binarySearchRawParameters.getComparatorForX();
      Comparator<? super Y> comparatorForY = binarySearchRawParameters.getComparatorForY();
      Y targetY = binarySearchRawParameters.getTargetY();

      RBPreconditions.checkArgument(
          // can also be == ; that probably indicates a problem, but it's too conservative to throw an exception for.
          comparatorForX.compare(lowerBoundX, upperBoundX) <= 0,
          "lower and upper bounds for X may not be inverted : %s and %s",
          lowerBoundX, upperBoundX);
      RBPreconditions.checkArgument(
          comparatorForY.compare(lowerValue, targetY) <= 0,
          "The lower input of %s gives a lower bound of %s which is greater than the final value of %s",
          lowerBoundX, lowerValue, targetY);
      RBPreconditions.checkArgument(
          comparatorForY.compare(targetY, upperValue) <= 0,
          "The upper input of %s gives an upper bound of %s which is less than the final value of %s",
          upperBoundX, upperValue, targetY);
      X initialMidpoint = binarySearchRawParameters.getMidpointGenerator().apply(lowerBoundX, upperBoundX);
      RBPreconditions.checkArgument(
          monotonic(comparatorForX, lowerBoundX, initialMidpoint, upperBoundX),
          "Midpoint generator is probably bad: lower / initial mid / upper should be monotonic (not strictly) but were %s %s %s",
          lowerBoundX, initialMidpoint, upperBoundX);
    }

    @Override
    public BinarySearchParameters<X, Y> buildWithoutPreconditions() {
      return new BinarySearchParameters<>(
          lowerBoundX, upperBoundX, terminationPredicate, binarySearchRawParameters);
    }

  }

}
