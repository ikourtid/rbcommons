package com.rb.nonbiz.functional;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.DoubleFunction;

import static com.rb.nonbiz.functional.RBNumericFunction.rbIdentityNumericFunction;
import static com.rb.nonbiz.functional.RBNumericFunction.rbNumericFunction;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

/**
 * This lets you build a simple step function with one y-value Y0 below a specified X0, and another
 * value Y1 above.
 *
 * <p> This is called "simple" because there is only a single step. In general, a step function could
 * have many regions of constant y-values {Y0, Y1, Y2, ...} separated by x-values {X0, X1, X2 ....}. </p>
 *
 * <p> This class could be named "SingleStepFunction...", except that that might imply that it holds a
 * single many-valued step function. Our usual convention is that a "Single..." class is a single instance held in a
 * higher-level container class that holds many "Singles". </p>
 */
public class SimpleStepFunctionRBNumericFunctionBuilder<X extends Number, Y extends RBNumeric<? super Y>>
    implements RBBuilder<RBNumericFunction<X, Y>> {

  private HumanReadableLabel label;
  private DoubleFunction<Y> instantiator;
  private X x0;
  private Y y0;
  private Y yForXEqualsX0;
  private Y y1;

  private SimpleStepFunctionRBNumericFunctionBuilder() {}

  public static <X extends Number, Y extends RBNumeric<? super Y>> SimpleStepFunctionRBNumericFunctionBuilder<X, Y>
  simpleStepFunctionRBNumericFunctionBuilder() {
    return new SimpleStepFunctionRBNumericFunctionBuilder<>();
  }

  public SimpleStepFunctionRBNumericFunctionBuilder<X, Y> setInstantiator(DoubleFunction<Y> instantiator) {
    this.instantiator = checkNotAlreadySet(this.instantiator, instantiator);
    return this;
  }

  public SimpleStepFunctionRBNumericFunctionBuilder<X, Y> setX0(X xValue) {
    this.x0 = checkNotAlreadySet(this.x0, xValue);
    return this;
  }

  public SimpleStepFunctionRBNumericFunctionBuilder<X, Y> setYForXLessThanX0(Y y0) {
    this.y0 = checkNotAlreadySet(this.y0, y0);
    return this;
  }

  public SimpleStepFunctionRBNumericFunctionBuilder<X, Y> setYForXLessThanOrEqualToX0(Y y0) {
    this.y0 = checkNotAlreadySet(this.y0, y0);
    return setYEqual(y0);
  }

  public SimpleStepFunctionRBNumericFunctionBuilder<X, Y> setYForXGreaterThanX0(Y y1) {
    this.y1 = checkNotAlreadySet(this.y1, y1);
    return this;
  }

  public SimpleStepFunctionRBNumericFunctionBuilder<X, Y> setYForXGreaterThanOrEqualToX0(Y y1) {
    this.y1 = checkNotAlreadySet(this.y1, y1);
    return setYEqual(y1);
  }

  private SimpleStepFunctionRBNumericFunctionBuilder<X, Y> setYEqual(Y yValueEqual) {
    this.yForXEqualsX0 = checkNotAlreadySet(this.yForXEqualsX0, yValueEqual);
    return this;
  }

  public SimpleStepFunctionRBNumericFunctionBuilder<X, Y> setLabel(HumanReadableLabel label) {
    this.label = checkNotAlreadySet(this.label, label);
    return this;
  }

  public SimpleStepFunctionRBNumericFunctionBuilder<X, Y> useAutoLabelWithPrefix(String labelPrefix) {
    this.label = checkNotAlreadySet(this.label, label(
        Strings.format("%s : %s", labelPrefix, generateAutoLabel())));
    return this;
  }

  public SimpleStepFunctionRBNumericFunctionBuilder<X, Y> useAutoLabel() {
    this.label = checkNotAlreadySet(this.label, label(generateAutoLabel()));
    return this;
  }

  private String generateAutoLabel() {
    return Strings.format("Step function with Y0 = %s below X0 %s and Y1 = %s, using %s when x = X0)",
        y0, x0, y1, yForXEqualsX0);
  }

  // do not use this; it's here to help the test matcher
  @VisibleForTesting
  public X getX0() {
    return x0;
  }

  // do not use this; it's here to help the test matcher
  @VisibleForTesting
  public Y getY0() {
    return y0;
  }

  // do not use this; it's here to help the test matcher
  @VisibleForTesting
  public Y getY1() {
    return y1;
  }

  // do not use this; it's here to help the test matcher
  @VisibleForTesting
  public Y getYForXEqualsX0() {
    return yForXEqualsX0;
  }

  // do not use this; it's here to help the test matcher
  @VisibleForTesting
  public RBNumericFunction<X, Y> getInstantiator() {
    return rbIdentityNumericFunction(instantiator);
  }

  @Override
  public void sanityCheckContents() {
    RBPreconditions.checkNotNull(label);
    RBPreconditions.checkNotNull(instantiator);
    RBPreconditions.checkNotNull(x0);
    RBPreconditions.checkNotNull(y0);
    RBPreconditions.checkNotNull(yForXEqualsX0);
    RBPreconditions.checkNotNull(y1);
  }

  @Override
  public RBNumericFunction<X, Y> buildWithoutPreconditions() {
    return rbNumericFunction(
        label,
        x -> x < x0.doubleValue() ? y0.doubleValue()
            : (x > x0.doubleValue() ? y1.doubleValue() : yForXEqualsX0.doubleValue()),
        instantiator);
  }

}
