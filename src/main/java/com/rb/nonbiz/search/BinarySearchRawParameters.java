package com.rb.nonbiz.search;

import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * This is an object that includes some the parameters needed to do a binary search,
 * and also all the parameters needed to tighten the initial lower and/or upper bounds for the binary search
 * in certain special cases (see {@link BinarySearchInitialXBoundsResult} and Issue #1527).
 *
 * @see BinarySearch
 */
public class BinarySearchRawParameters<X, Y> {

  private final Comparator<? super X> comparatorForX;
  private final Comparator<? super Y> comparatorForY;
  private final Y targetY;
  private final Function<X, Y> evaluatorOfX;
  private final BinaryOperator<X> midpointGenerator;
  private final int maxIterations;

  private BinarySearchRawParameters(
      Comparator<? super X> comparatorForX,
      Comparator<? super Y> comparatorForY,
      Y targetY,
      Function<X, Y> evaluatorOfX,
      BinaryOperator<X> midpointGenerator,
      int maxIterations) {
    this.comparatorForX = comparatorForX;
    this.comparatorForY = comparatorForY;
    this.targetY = targetY;
    this.evaluatorOfX = evaluatorOfX;
    this.midpointGenerator = midpointGenerator;
    this.maxIterations = maxIterations;
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

  public BinaryOperator<X> getMidpointGenerator() {
    return midpointGenerator;
  }

  public int getMaxIterations() {
    return maxIterations;
  }


  /**
   * An {@link RBBuilder} that helps you build a {@link BinarySearchRawParameters} object.
   */
  public static class BinarySearchRawParametersBuilder<X, Y> implements RBBuilder<BinarySearchRawParameters<X, Y>> {

    private Comparator<? super X> comparatorForX;
    private Comparator<? super Y> comparatorForY;
    private Y targetY;
    private Function<X, Y> evaluatorOfX;
    private BinaryOperator<X> midpointGenerator;
    private Integer maxIterations;

    private BinarySearchRawParametersBuilder() {}

    public static <X, Y> BinarySearchRawParametersBuilder<X, Y> binarySearchRawParametersBuilder() {
      return new BinarySearchRawParametersBuilder<>();
    }

    public BinarySearchRawParametersBuilder<X, Y> setComparatorForX(Comparator<? super X> comparatorForX) {
      this.comparatorForX = checkNotAlreadySet(this.comparatorForX, comparatorForX);
      return this;
    }

    public BinarySearchRawParametersBuilder<X, Y> setComparatorForY(Comparator<? super Y> comparatorForY) {
      this.comparatorForY = checkNotAlreadySet(this.comparatorForY, comparatorForY);
      return this;
    }

    public BinarySearchRawParametersBuilder<X, Y> setTargetY(Y targetY) {
      this.targetY = checkNotAlreadySet(this.targetY, targetY);
      return this;
    }

    public BinarySearchRawParametersBuilder<X, Y> setEvaluatorOfX(Function<X, Y> evaluatorOfX) {
      this.evaluatorOfX = checkNotAlreadySet(this.evaluatorOfX, evaluatorOfX);
      return this;
    }

    public BinarySearchRawParametersBuilder<X, Y> setMidpointGenerator(BinaryOperator<X> midpointGenerator) {
      this.midpointGenerator = checkNotAlreadySet(this.midpointGenerator, midpointGenerator);
      return this;
    }

    public BinarySearchRawParametersBuilder<X, Y> setMaxIterations(int maxIterations) {
      this.maxIterations = checkNotAlreadySet(this.maxIterations, maxIterations);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(comparatorForX);
      RBPreconditions.checkNotNull(comparatorForY);
      RBPreconditions.checkNotNull(targetY);
      RBPreconditions.checkNotNull(evaluatorOfX);
      RBPreconditions.checkNotNull(midpointGenerator);
      RBPreconditions.checkNotNull(maxIterations);
      RBPreconditions.checkArgument(
          maxIterations >= 2,
          "max iterations in binary search must be at least 2, otherwise it's not really a search; was %s",
          maxIterations);
    }

    @Override
    public BinarySearchRawParameters<X, Y> buildWithoutPreconditions() {
      return new BinarySearchRawParameters<>(
          comparatorForX, comparatorForY, targetY, evaluatorOfX, midpointGenerator, maxIterations);
    }
  }

}
