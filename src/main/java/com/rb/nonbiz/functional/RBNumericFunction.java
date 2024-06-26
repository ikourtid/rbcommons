package com.rb.nonbiz.functional;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.HasHumanReadableLabel;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RBNumeric;

import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

/**
 * This is a special class that lets us declare a (mathematical) function that maps a {@link Number}
 * (currently - Mar 2024 - we only care about Long or Double) to another {@link RBNumeric}.
 *
 * <p> The reason we let the user specify a less-typesafe {@link DoubleUnaryOperator} in the construction parameters
 * is that it makes it possible to write generalized code that generates {@link DoubleUnaryOperator} objects for arbitrary
 * double -> double functions. This way, we can reuse such code for all kinds of cases of X and Y. </p>
 *
 * <p> We normally dislike storing function objects, because they are not data that can be compared, and we like having
 * test-only TypeSafeMatchers for comparing objects in test. This is why we typically use limited functional objects
 * such as {@code FunctionDescriptor}. However, the downside of such 'limited' objects is that they only allow for
 * one class of function (albeit parametrizable), but not any arbitrary function, e.g. a logarithmic one.
 * Therefore, in order to keep things general, we will store a {@link Function} object here. </p>
 */
public class RBNumericFunction<X extends Number, Y extends RBNumeric<? super Y>>
    implements Function<X, Y>, HasHumanReadableLabel {

  private final HumanReadableLabel label;
  private final DoubleUnaryOperator rawFunction;
  private final DoubleFunction<Y> instantiator;

  private RBNumericFunction(HumanReadableLabel label, DoubleUnaryOperator rawFunction, DoubleFunction<Y> instantiator) {
    this.label = label;
    this.rawFunction = rawFunction;
    this.instantiator = instantiator;
  }

  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunction<X, Y> rbNumericFunction(
      HumanReadableLabel label, DoubleUnaryOperator rawFunction, DoubleFunction<Y> instantiator) {
    return new RBNumericFunction<>(label, rawFunction, instantiator);
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunction<Double, Y> rbDoubleNumericFunction(
      HumanReadableLabel label, DoubleUnaryOperator rawFunction, DoubleFunction<Y> instantiator) {
    return new RBNumericFunction<>(label, rawFunction, instantiator);
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunction<Long, Y> rbLongNumericFunction(
      HumanReadableLabel label, DoubleUnaryOperator rawFunction, DoubleFunction<Y> instantiator) {
    return new RBNumericFunction<>(label, rawFunction, instantiator);
  }

  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunction<X, Y> rbIdentityNumericFunction(
      DoubleFunction<Y> instantiator) {
    return rbNumericFunction(label("identity"), DoubleUnaryOperator.identity(), instantiator);
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunction<Double, Y> rbDoubleIdentityNumericFunction(
      DoubleFunction<Y> instantiator) {
    return rbNumericFunction(label("identity"), DoubleUnaryOperator.identity(), instantiator);
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunction<Long, Y> rbLongIdentityNumericFunction(
      DoubleFunction<Y> instantiator) {
    return rbNumericFunction(label("identity"), DoubleUnaryOperator.identity(), instantiator);
  }

  @Override
  public Y apply(X x) {
    return instantiator.apply(rawFunction.applyAsDouble(x.doubleValue()));
  }

  @VisibleForTesting // do not use this method; it is here to help the test matcher
  DoubleUnaryOperator getRawFunction() {
    return rawFunction;
  }

  @Override
  public HumanReadableLabel getHumanReadableLabel() {
    return label;
  }

  @Override
  public String toString() {
    return Strings.format("[RBNF %s RBNF]", label);
  }

}
