package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

public class Eigenvalue extends ImpreciseValue<Eigenvalue> {

  protected Eigenvalue(double value) {
    super(value);
  }

  /**
   * In general, eigenvalues of an eigendecomposition could also be negative,
   * However, we currently (June 2018) only do eigendecompositions of correlation matrices. In those cases, the
   * eigenvalues are guaranteed to be positive. That's why the default constructor of a positive eigenvalue is
   * called just 'eigenvalue', not 'positiveEigenvalue'.
   * We can still instantiate a negative eigenvalue using possiblyNegativeEigenvalue.
   */
  public static Eigenvalue eigenvalue(double value) {
    RBPreconditions.checkArgument(
        value > 0,
        "Encountered non-positive eigenvalue %s ; should only see positive ones in our particular case",
        value);
    return new Eigenvalue(value);
  }

  public static Eigenvalue possiblyNegativeEigenvalue(double value) {
    return new Eigenvalue(value);
  }

  @Override
  public String toString() {
    return String.format("%11.8f", doubleValue());
  }

}
