package com.rb.nonbiz.math.optimization.highlevel;

import java.util.function.DoubleUnaryOperator;

/**
 * A simple wrapper around a DoubleUnaryOperator.
 * This is 'function' as in f(x), not as in programming.
 *
 * Classes that extend FunctionDescriptor should really be parametrizable functions.
 * That would be helpful in testing, because we can use matchers on them; one cannot use a matcher on a general
 * function. For instance, I can represent a * x ^ 2 + b * x + c with 3 numbers a, b, c, and I can therefore
 * compare 2 such function descriptors by comparing the data in them (a, b, c). However, it's not possible to
 * compare programmatically 2 DoubleUnaryOperator objects, i.e. 2 general mathematical functions. E.g. how do you know
 * that f(x) = x ^ 2 + x and g(x) = 2 * x ^ 3 aren't the same function?
 * Do you evaluate the functions in a bunch of points, and if the
 * values are the same then just declare the two functions being the same? In the example here, f and x have the same
 * values at 0 and 1, but are not the same. So how many points should we try? Anyway, it should be clear that one
 * cannot compare non-parametrizable functions.
 */
public abstract class FunctionDescriptor {

  public interface BaseVisitor<T> {}

  /**
   * The water slide function generates slopes that already incorporate whether an asset class has a small or large
   * target in the allocation. Therefore, we want to somehow signify that it's an error to try to modify
   * the coefficients in the various terms. So this should return false then.
   *
   * In the old (pre-May 2018) code, where we use a quadratic function, the coefficients get generated separately,
   * and can be inversely proportional to the target fraction of an asset class, but in general, they are allowed
   * to be non-1. So this should return true in those cases.
   */
  public boolean canAllowCoefficientsOtherThan1() {
    return true;
  }

  public abstract DoubleUnaryOperator asFunction();

  public abstract String asFunctionText();

  public abstract <T> T visit(BaseVisitor<T> visitor);

}
