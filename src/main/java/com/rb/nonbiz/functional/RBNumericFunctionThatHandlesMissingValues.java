package com.rb.nonbiz.functional;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.functional.AllowsMissingValues.Visitor;
import com.rb.nonbiz.text.HasHumanReadableLabel;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RBNumeric;

import java.util.Optional;
import java.util.function.DoubleFunction;
import java.util.function.Function;

import static com.rb.nonbiz.functional.RBNumericFunction.rbIdentityNumericFunction;

/**
 * A data class representing a (mathematical) function from Number (Double or Long currently - Dec 2020),
 * where it's also possible to have a 'missing' X.
 */
public class RBNumericFunctionThatHandlesMissingValues<X extends Number, Y extends RBNumeric<? super Y>>
    implements Function<AllowsMissingValues<X>, Optional<Y>>, HasHumanReadableLabel {

  private final RBNumericFunction<X, Y> functionForPresentValues;
  private final Optional<Y> yForMissingX;

  private RBNumericFunctionThatHandlesMissingValues(
      RBNumericFunction<X, Y> functionForPresentValues,
      Optional<Y> yForMissingX) {
    this.functionForPresentValues = functionForPresentValues;
    this.yForMissingX = yForMissingX;
  }

  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<X, Y>
  rbNumericFunctionThatReturnsSpecifiedDefaultOnMissingValues(
      RBNumericFunction<X, Y> functionForPresentValues, Y yForMissingX) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, Optional.of(yForMissingX));
  }

  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<X, Y>
  rbNumericFunctionThatReturnsEmptyOnMissingValues(RBNumericFunction<X, Y> functionForPresentValues) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, Optional.empty());
  }


  public static <Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<Double, Y>
  rbDoubleFunctionThatReturnsSpecifiedDefaultOnMissingValues(
      RBNumericFunction<Double, Y> functionForPresentValues, Y yForMissingX) {
    return rbNumericFunctionThatReturnsSpecifiedDefaultOnMissingValues(functionForPresentValues, yForMissingX);
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<Double, Y>
  rbDoubleFunctionThatReturnsEmptyOnMissingValues(RBNumericFunction<Double, Y> functionForPresentValues) {
    return rbNumericFunctionThatReturnsEmptyOnMissingValues(functionForPresentValues);
  }


  public static <Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<Long, Y>
  rbLongFunctionThatReturnsSpecifiedDefaultOnMissingValues(
      RBNumericFunction<Long, Y> functionForPresentValues, Y yForMissingX) {
    return rbNumericFunctionThatReturnsSpecifiedDefaultOnMissingValues(functionForPresentValues, yForMissingX);
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<Long, Y>
  rbLongFunctionThatReturnsEmptyOnMissingValues(RBNumericFunction<Long, Y> functionForPresentValues) {
    return rbNumericFunctionThatReturnsEmptyOnMissingValues(functionForPresentValues);
  }


  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<X, Y>
  rbIdentityNumericFunctionThatReturnsSpecifiedDefaultOnMissingValues(
      DoubleFunction<Y> instantiator, Y yForMissingX) {
    return rbNumericFunctionThatReturnsSpecifiedDefaultOnMissingValues(
        rbIdentityNumericFunction(instantiator), yForMissingX);
  }

  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<X, Y>
  rbIdentityNumericFunctionThatReturnsEmptyOnMissingValues(
      DoubleFunction<Y> instantiator) {
    return rbNumericFunctionThatReturnsEmptyOnMissingValues(rbIdentityNumericFunction(instantiator));
  }


  @Override
  public Optional<Y> apply(AllowsMissingValues<X> allowsMissingValues) {
    return allowsMissingValues.visit(new Visitor<Optional<Y>, X>() {
      @Override
      public Optional<Y> visitPresentValue(X presentValue) {
        return Optional.of(functionForPresentValues.apply(presentValue));
      }

      @Override
      public Optional<Y> visitMissingValue() {
        return yForMissingX;
      }
    });
  }

  @Override
  public HumanReadableLabel getHumanReadableLabel() {
    return functionForPresentValues.getHumanReadableLabel();
  }

  @VisibleForTesting // do not use this; it's here to help the test matcher
  RBNumericFunction<X, Y> getFunctionForPresentValues() {
    return functionForPresentValues;
  }

  @VisibleForTesting // do not use this; it's here to help the test matcher
  Optional<Y> getYForMissingX() {
    return yForMissingX;
  }

  @Override
  public String toString() {
    return Strings.format("[RBNFTHMV %s ; yForMissingX= %s RBNFTHMV]",
        functionForPresentValues, yForMissingX);
  }

}
