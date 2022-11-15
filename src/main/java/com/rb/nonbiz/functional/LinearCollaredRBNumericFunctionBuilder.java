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
 * This lets us build a linear function with a collar on its Y values (min + max) in a slightly more legible fashion.
 *
 * <p> The function has <i>y</i>-values = </p>
 * <ol>
 *   <li> minY for x in (-inf, minX] </li>
 *   <li> minY + [(x - minX) / (maxX - minX)] * (maxY - minY)  for x in [minX, maxX] </li>
 *   <li> maxY for x in [maxX, +inf) </li>
 * </ol>
 *
 * <p> Note that <i>minY</i> is the y-value at <i>minX</i> (or below) and
 * <i>maxY</i> is the y-value at <i>maxX</i> (or above).
 * As such, <i>minY</i> could be less than, equal to, or greater than <i>maxY</i>. </p>
 *
 * <p> There is nothing preventing you from calling the setters out of order, but if you call them in the order they appear
 * then the resulting code will read a bit like an English sentence. </p>
 */
public class LinearCollaredRBNumericFunctionBuilder<X extends Number, Y extends RBNumeric<? super Y>>
    implements RBBuilder<RBNumericFunction<X, Y>> {

  private HumanReadableLabel label;
  private DoubleFunction<Y> instantiator;
  private X minX;
  private X maxX;
  private Y minY;
  private Y maxY;

  private LinearCollaredRBNumericFunctionBuilder() {}

  public static <X extends Number, Y extends RBNumeric<? super Y>> LinearCollaredRBNumericFunctionBuilder<X, Y>
  linearCollaredRBNumericFunctionBuilder() {
    return new LinearCollaredRBNumericFunctionBuilder<>();
  }

  public LinearCollaredRBNumericFunctionBuilder<X, Y> setInstantiator(DoubleFunction<Y> instantiator) {
    this.instantiator = checkNotAlreadySet(this.instantiator, instantiator);
    return this;
  }

  public LinearCollaredRBNumericFunctionBuilder<X, Y> setMinX(X minX) {
    this.minX = checkNotAlreadySet(this.minX, minX);
    return this;
  }

  public LinearCollaredRBNumericFunctionBuilder<X, Y> setMaxX(X maxX) {
    this.maxX = checkNotAlreadySet(this.maxX, maxX);
    return this;
  }

  public LinearCollaredRBNumericFunctionBuilder<X, Y> setMinY(Y minY) {
    this.minY = checkNotAlreadySet(this.minY, minY);
    return this;
  }

  public LinearCollaredRBNumericFunctionBuilder<X, Y> setMaxY(Y maxY) {
    this.maxY = checkNotAlreadySet(this.maxY, maxY);
    return this;
  }

  public LinearCollaredRBNumericFunctionBuilder<X, Y> setLabel(HumanReadableLabel label) {
    this.label = checkNotAlreadySet(this.label, label);
    return this;
  }

  public LinearCollaredRBNumericFunctionBuilder<X, Y> useAutoLabelWithPrefix(String labelPrefix) {
    this.label = checkNotAlreadySet(this.label, label(
        Strings.format("%s : %s", labelPrefix, generateAutoLabel())));
    return this;
  }

  public LinearCollaredRBNumericFunctionBuilder<X, Y> useAutoLabel() {
    this.label = checkNotAlreadySet(this.label, label(generateAutoLabel()));
    return this;
  }

  private String generateAutoLabel() {
    return Strings.format("Linear between (X, Y) points ( %s , %s ) and ( %s, %s ); flat below minX and above maxX",
        minX, maxX, minY, maxY);
  }

  public HumanReadableLabel getLabel() {
    return label;
  }

  // We wouldn't need these getters if we were just evaluating the function.
  // However, we need them in order to convert the function to/from JSON.
  public X getMinX() {
    return minX;
  }

  public X getMaxX() {
    return maxX;
  }

  public Y getMinY() {
    return minY;
  }

  public Y getMaxY() {
    return maxY;
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
  }

  @Override
  public RBNumericFunction<X, Y> buildWithoutPreconditions() {
    // minor performance improvement: we compute the slope only once
    double slope = (maxY.doubleValue() - minY.doubleValue())
        / (maxX.doubleValue() - minX.doubleValue());
    return rbNumericFunction(
        label,
        x ->
            x <= minX.doubleValue() ? minY.doubleValue() :
            x >= maxX.doubleValue() ? maxY.doubleValue() :
                 minY.doubleValue() + slope * (x - minX.doubleValue()),
        instantiator);
  }

}
