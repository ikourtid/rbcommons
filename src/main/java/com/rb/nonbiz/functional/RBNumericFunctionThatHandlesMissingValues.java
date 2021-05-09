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

import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.functional.RBNumericFunction.rbIdentityNumericFunction;

/**
 * A data class representing a (mathematical) function from Number (Double or Long currently - Dec 2020),
 * where it's also possible to have a 'missing' X.
 */
public class RBNumericFunctionThatHandlesMissingValues<X extends Number, Y extends RBNumeric<? super Y>>
    implements Function<AllowsMissingValues<X>, Y>, HasHumanReadableLabel {

  private final RBNumericFunction<X, Y> functionForPresentValues;
  private final Optional<Y> yForMissingX;

  private RBNumericFunctionThatHandlesMissingValues(
      RBNumericFunction<X, Y> functionForPresentValues,
      Optional<Y> yForMissingX) {
    this.functionForPresentValues = functionForPresentValues;
    this.yForMissingX = yForMissingX;
  }

  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<X, Y>
  rbNumericFunctionThatHandlesMissingValues(
      RBNumericFunction<X, Y> functionForPresentValues, Optional<Y> yForMissingX) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, yForMissingX);
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<Double, Y>
  rbDoubleNumericFunctionThatHandlesMissingValues(
      RBNumericFunction<Double, Y> functionForPresentValues, Optional<Y> yForMissingX) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, yForMissingX);
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<Long, Y>
  rbLongNumericFunctionThatHandlesMissingValues(
      RBNumericFunction<Long, Y> functionForPresentValues, Optional<Y> yForMissingX) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, yForMissingX);
  }


  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<X, Y>
  rbNumericFunctionThatAllowsMissingValues(
      RBNumericFunction<X, Y> functionForPresentValues, Y yForMissingX) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, Optional.of(yForMissingX));
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<Double, Y>
  rbDoubleNumericFunctionThatAllowsMissingValues(
      RBNumericFunction<Double, Y> functionForPresentValues, Y yForMissingX) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, Optional.of(yForMissingX));
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<Long, Y>
  rbLongNumericFunctionThatAllowsMissingValues(
      RBNumericFunction<Long, Y> functionForPresentValues, Y yForMissingX) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, Optional.of(yForMissingX));
  }


  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<X, Y>
  rbNumericFunctionThatThrowsOnMissingValues(
      RBNumericFunction<X, Y> functionForPresentValues) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, Optional.empty());
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<Double, Y>
  rbDoubleNumericFunctionThatThrowsOnMissingValues(
      RBNumericFunction<Double, Y> functionForPresentValues) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, Optional.empty());
  }

  public static <Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<Long, Y>
  rbLongNumericFunctionThatThrowsOnMissingValues(
      RBNumericFunction<Long, Y> functionForPresentValues) {
    return new RBNumericFunctionThatHandlesMissingValues<>(functionForPresentValues, Optional.empty());
  }


  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<X, Y>
  rbIdentityNumericFunctionThatAllowsMissingValues(
      DoubleFunction<Y> instantiator, Y yForMissingX) {
    return rbNumericFunctionThatAllowsMissingValues(
        rbIdentityNumericFunction(instantiator), yForMissingX);
  }

  public static <X extends Number, Y extends RBNumeric<? super Y>> RBNumericFunctionThatHandlesMissingValues<X, Y>
  rbIdentityNumericFunctionThatThrowsOnMissing(
      DoubleFunction<Y> instantiator) {
    return rbNumericFunctionThatThrowsOnMissingValues(rbIdentityNumericFunction(instantiator));
  }

  @Override
  public Y apply(AllowsMissingValues<X> allowsMissingValues) {
    return allowsMissingValues.visit(new Visitor<Y, X>() {
      @Override
      public Y visitPresentValue(X presentValue) {
        return functionForPresentValues.apply(presentValue);
      }

      @Override
      public Y visitMissingValue() {
        return getOrThrow(
            yForMissingX,
            "%s : we are intentionally throwing an exception on missing X value",
            getHumanReadableLabel());
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
