package com.rb.nonbiz.functional;

import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.DoubleFunction;

import static com.rb.nonbiz.functional.RBNumericFunction.rbNumericFunction;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

/**
 * This lets us build a function with a collar on its Y values (min + max) in a slightly more legible fashion.
 * I don't know if there's a term for 'geometric interpolation', but one way to think about this intuitively is what
 * happens in the middle of the x range, i.e. (minX + maxX) / 2:
 *
 * With linear interpolation, the value in the middle is (minY + maxY) / 2, i.e. the arithmetic average of the Y range.
 * With geometric interpolation, the value in the middle is sqrt(minY * maxY), i.e. the geometric average of the Y range.
 *
 * There is nothing preventing you from calling the setters out of order, but if you call them in the order they appear
 * then the resulting code will read a bit like an English sentence.
 *
 * @see GeometricCollaredRBNumericFunctionBuilder
 */
public class GeometricCollaredRBNumericFunctionBuilder<X extends Number, Y extends RBNumeric<? super Y>>
    implements RBBuilder<RBNumericFunction<X, Y>> {

  private HumanReadableLabel label;
  private DoubleFunction<Y> instantiator;
  private X minX;
  private X maxX;
  private Y minY;
  private Y maxY;

  private GeometricCollaredRBNumericFunctionBuilder() {}

  public static <X extends Number, Y extends RBNumeric<? super Y>> GeometricCollaredRBNumericFunctionBuilder<X, Y>
  geometricCollaredRBNumericFunctionBuilder() {
    return new GeometricCollaredRBNumericFunctionBuilder<>();
  }

  public GeometricCollaredRBNumericFunctionBuilder<X, Y> setInstantiator(DoubleFunction<Y> instantiator) {
    this.instantiator = checkNotAlreadySet(this.instantiator, instantiator);
    return this;
  }

  public GeometricCollaredRBNumericFunctionBuilder<X, Y> setMinX(X minX) {
    this.minX = checkNotAlreadySet(this.minX, minX);
    return this;
  }

  public GeometricCollaredRBNumericFunctionBuilder<X, Y> setMaxX(X maxX) {
    this.maxX = checkNotAlreadySet(this.maxX, maxX);
    return this;
  }

  public GeometricCollaredRBNumericFunctionBuilder<X, Y> setMinY(Y minY) {
    this.minY = checkNotAlreadySet(this.minY, minY);
    return this;
  }

  public GeometricCollaredRBNumericFunctionBuilder<X, Y> setMaxY(Y maxY) {
    this.maxY = checkNotAlreadySet(this.maxY, maxY);
    return this;
  }

  public GeometricCollaredRBNumericFunctionBuilder<X, Y> setLabel(HumanReadableLabel label) {
    this.label = checkNotAlreadySet(this.label, label);
    return this;
  }

  public GeometricCollaredRBNumericFunctionBuilder<X, Y> useAutoLabelWithPrefix(String labelPrefix) {
    this.label = checkNotAlreadySet(this.label, label(
        Strings.format("%s : %s", labelPrefix, generateAutoLabel())));
    return this;
  }

  public GeometricCollaredRBNumericFunctionBuilder<X, Y> useAutoLabel() {
    this.label = checkNotAlreadySet(this.label, label(generateAutoLabel()));
    return this;
  }

  private String generateAutoLabel() {
    return Strings.format("Geometric between (X, Y) points ( %s , %s ) and ( %s, %s ); flat below minX and above maxX",
        minX, maxX, minY, maxY);
  }

  @Override
  public void sanityCheckContents() {
    RBPreconditions.checkNotNull(label);
    RBPreconditions.checkNotNull(instantiator);
    RBPreconditions.checkNotNull(minX);
    RBPreconditions.checkNotNull(maxX);
    RBPreconditions.checkNotNull(minY);
    RBPreconditions.checkNotNull(maxY);

    // minX, maxX must be ordered, but minY, maxY do not have to be ordered; they can be increasing or decreasing,
    // depending on whether the slope is upwards or downwards, respectively.
    RBPreconditions.checkArgument(
        maxX.doubleValue() > minX.doubleValue(),
        "%s : max must be bigger than min in the collar, but min= %s and max= %s",
        label, minX, maxX);
    RBPreconditions.checkArgument(
        maxX.doubleValue() - minX.doubleValue() > 1e-8,
        "%s : max must be sufficiently bigger than min in the collar, but min= %s and max= %s",
        label, minX, maxX);
    RBPreconditions.checkArgument(
        Math.abs(minY.doubleValue() - maxY.doubleValue()) > 1e-8,
        "%s : The multipliers for the min and max value cannot be the same: %s and %s ; range of numeric values was %s to %s",
        label, minY, maxY, minX, maxX);

    // FIXME IAK INTERPOLATION we may need more preconditions, depending on what the calculation below will do.
  }

  @Override
  public RBNumericFunction<X, Y> buildWithoutPreconditions() {
    double dxTotal = maxX.doubleValue() - minX.doubleValue();
    double valuesRatio = maxY.doubleValue() / minY.doubleValue();

    // Example 1: general example, where Y in the midpoint of minX and maxX is not E.g. say X is in [1, 9] and Y is in [1/7, 3].
    // We want a function that will give
    // f(1) = 1/7
    // f(9) = 3
    //
    // Let's simplify by shifting so that the 'starting point' is x=0.
    // g(x - 1) = f(x), or equivalently g(x) = f(x+1), so
    // g(0) = 1/7
    // g(8) = 3
    // Per above, valuesRatio = 3 / (1/7) = 21.
    // Let's look at 1/7 * Math.pow(valuesRatio, dxFraction):
    // g(0) = 1/7 * (21 ^ 0) = 1/7 since dxFraction = 0
    // g(8) = 1/7 * (21 ^ 1) = 3  since dxFraction = 1
    // and
    // g(4) = 1/7 * sqrt(21) = 0.654653671, which kind of makes sense, since this tilt is not 'symmetric'.

    return rbNumericFunction(
        label,
        x ->
            x <= minX.doubleValue() ? minY.doubleValue() :
            x >= maxX.doubleValue() ? maxY.doubleValue() :
                 minY.doubleValue() * Math.pow(valuesProduct, (x - maxX.doubleValue()) / dxTotal),
        instantiator);
  }

}
