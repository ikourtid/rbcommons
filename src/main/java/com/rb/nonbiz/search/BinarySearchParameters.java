package com.rb.nonbiz.search;

import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBComparables.monotonic;

/**
 * This is an object that includes all the parameters needed to do a binary search.
 *
 * @see BinarySearch
 */
public class BinarySearchParameters<X, Y> {

  private final X lowerBoundX;
  private final X upperBoundX;
  private final Comparator<? super X> comparatorForX;
  private final Comparator<? super Y> comparatorForY;
  private final Y targetY;
  private final Function<X, Y> evaluatorOfX;
  private final BinarySearchTerminationPredicate<X, Y> terminationPredicate;
  private final BinaryOperator<X> midpointGenerator;
  private final int maxIterations;

  private BinarySearchParameters(
      X lowerBoundX,
      X upperBoundX,
      Comparator<? super X> comparatorForX,
      Comparator<? super Y> comparatorForY,
      Y targetY,
      Function<X, Y> evaluatorOfX,
      BinarySearchTerminationPredicate<X, Y> terminationPredicate,
      BinaryOperator<X> midpointGenerator,
      int maxIterations) {
    this.lowerBoundX = lowerBoundX;
    this.upperBoundX = upperBoundX;
    this.comparatorForX = comparatorForX;
    this.comparatorForY = comparatorForY;
    this.targetY = targetY;
    this.evaluatorOfX = evaluatorOfX;
    this.terminationPredicate = terminationPredicate;
    this.midpointGenerator = midpointGenerator;
    this.maxIterations = maxIterations;
  }

  public X getLowerBoundX() {
    return lowerBoundX;
  }

  public X getUpperBoundX() {
    return upperBoundX;
  }

  public Comparator<? super X> getComparatorForX() {
    return comparatorForX;
  }

  public Comparator<? super Y> getComparatorForY() {
    return comparatorForY;
  }

  public Y getTargetY() {
    return targetY;
  }

  public Function<X, Y> getEvaluatorOfX() {
    return evaluatorOfX;
  }

  public BinarySearchTerminationPredicate<X, Y> getTerminationPredicate() {
    return terminationPredicate;
  }

  public BinaryOperator<X> getMidpointGenerator() {
    return midpointGenerator;
  }

  public int getMaxIterations() {
    return maxIterations;
  }


  public static class BinarySearchParametersBuilder<X, Y> implements RBBuilder<BinarySearchParameters<X, Y>> {

    private X lowerBoundX;
    private X upperBoundX;
    private Comparator<? super X> comparatorForX;
    private Comparator<? super Y> comparatorForY;
    private Y targetY;
    private Function<X, Y> evaluatorOfX;
    private BinarySearchTerminationPredicate<X, Y> terminationPredicate;
    private BinaryOperator<X> midpointGenerator;
    private Integer maxIterations;

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

    public BinarySearchParametersBuilder<X, Y> setComparatorForX(Comparator<? super X> comparatorForX) {
      // For some generics-related reason related to the <? super X>, I can't use checkNotAlreadySet here,
      // so I'm inlining it.
      RBPreconditions.checkArgument(
          this.comparatorForX == null,
          "You are trying to set the X Comparator twice in BinarySearchParametersBuilder, which is probably a bug)");
      this.comparatorForX = comparatorForX;
      return this;
    }

    public BinarySearchParametersBuilder<X, Y> setComparatorForY(Comparator<? super Y> comparatorForY) {
      // For some generics-related reason related to the <? super X>, I can't use checkNotAlreadySet here,
      // so I'm inlining it.
      RBPreconditions.checkArgument(
          this.comparatorForY == null,
          "You are trying to set the Y Comparator twice in BinarySearchParametersBuilder, which is probably a bug)");
      this.comparatorForY = comparatorForY;
      return this;
    }

    public BinarySearchParametersBuilder<X, Y> setTargetY(Y targetY) {
      this.targetY = checkNotAlreadySet(this.targetY, targetY);
      return this;
    }

    public BinarySearchParametersBuilder<X, Y> setEvaluatorOfX(Function<X, Y> evaluatorOfX) {
      this.evaluatorOfX = checkNotAlreadySet(this.evaluatorOfX, evaluatorOfX);
      return this;
    }

    public BinarySearchParametersBuilder<X, Y> setTerminationPredicate(
        BinarySearchTerminationPredicate<X, Y> terminationPredicate) {
      this.terminationPredicate = checkNotAlreadySet(this.terminationPredicate, terminationPredicate);
      return this;
    }

    public BinarySearchParametersBuilder<X, Y> setMidpointGenerator(BinaryOperator<X> midpointGenerator) {
      this.midpointGenerator = checkNotAlreadySet(this.midpointGenerator, midpointGenerator);
      return this;
    }

    public BinarySearchParametersBuilder<X, Y> setMaxIterations(int maxIterations) {
      this.maxIterations = checkNotAlreadySet(this.maxIterations, maxIterations);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(lowerBoundX);
      RBPreconditions.checkNotNull(upperBoundX);
      RBPreconditions.checkNotNull(comparatorForX);
      RBPreconditions.checkNotNull(comparatorForY);
      RBPreconditions.checkNotNull(targetY);
      RBPreconditions.checkNotNull(evaluatorOfX);
      RBPreconditions.checkNotNull(terminationPredicate);
      RBPreconditions.checkNotNull(midpointGenerator);
      RBPreconditions.checkNotNull(maxIterations);
      RBPreconditions.checkArgument(
          maxIterations >= 2,
          "max iterations in binary search must be at least 2, otherwise it's not really a search; was %s",
          maxIterations);

      Y lowerValue = evaluatorOfX.apply(lowerBoundX);
      Y upperValue = evaluatorOfX.apply(upperBoundX);
      RBPreconditions.checkArgument(
          comparatorForX.compare(lowerBoundX, upperBoundX) != 0,
          "lower and upper bounds for X may be inverted; can also be ==, but that probably indicates a problem : %s and %s",
          lowerBoundX, upperBoundX);
      RBPreconditions.checkArgument(
          comparatorForY.compare(lowerValue, targetY) <= 0,
          "The lower input of %s gives a lower bound of %s which is greater than the final value of %s",
          lowerBoundX, lowerValue, targetY);
      RBPreconditions.checkArgument(
          comparatorForY.compare(targetY, upperValue) <= 0,
          "The upper input of %s gives an upper bound of %s which is less than the final value of %s",
          upperBoundX, upperValue, targetY);
      X initialMidpoint = midpointGenerator.apply(lowerBoundX, upperBoundX);
      RBPreconditions.checkArgument(
          monotonic(comparatorForX, lowerBoundX, initialMidpoint, upperBoundX),
          "Midpoint generator is probably bad: lower / initial mid / upper should be monotonic (not strictly) but were %s %s %s",
          lowerBoundX, initialMidpoint, upperBoundX);
    }

    @Override
    public BinarySearchParameters<X, Y> buildWithoutPreconditions() {
      return new BinarySearchParameters<>(
          lowerBoundX, upperBoundX, comparatorForX, comparatorForY, targetY, evaluatorOfX,
          terminationPredicate, midpointGenerator, maxIterations);
    }
  }

}
