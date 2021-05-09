package com.rb.nonbiz.math.eigen;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.stream.DoubleStream;

/**
 * The reason the eigenvector uses a DoubleMatrix1D instead of a double[] is that the Colt eigendecomposition
 * returns DoubleMatrix1D, so there's no point in converting to a double[].
 */
public class Eigenvector {

  private final DoubleMatrix1D rawMatrix1D;

  private Eigenvector(DoubleMatrix1D rawMatrix1D) {
    this.rawMatrix1D = rawMatrix1D;
  }

  public static Eigenvector eigenvector(DoubleMatrix1D rawMatrix1D) {
    RBPreconditions.checkArgument(
        rawMatrix1D.size() > 0,
        "Encountered eigenvector with length 0!");
    double vectorLengthSq = DoubleStream.of(rawMatrix1D.toArray())
        .map(x -> x * x)
        .sum();
    RBPreconditions.checkArgument(
        Math.abs(vectorLengthSq - 1.0) < 1e-8,
        "eigenvector should have length 1; has %s",
        Math.sqrt(vectorLengthSq));
    return new Eigenvector(rawMatrix1D);
  }

  public static Eigenvector eigenvector(double[] values) {
    return eigenvector(new DenseDoubleMatrix1D(values));
  }

  /**
   * This is not valid but it's needed for some special case.
   */
  public static Eigenvector emptyEigenvector() {
    return new Eigenvector(new DenseDoubleMatrix1D(new double[] {}));
  }

  public double get(int index) {
    return rawMatrix1D.get(index);
  }

  /**
   * It's a bit ugly that we are exposing the get/getQuick distinction here, but on net it is better I think.
   */
  public double getQuick(int index) {
    return rawMatrix1D.getQuick(index);
  }

  public int size() {
    return rawMatrix1D.size();
  }

  @VisibleForTesting // don't use this; its here to help the matcher
  DoubleMatrix1D getRawMatrix1D() {
    return rawMatrix1D;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[ ");
    sb.append(Strings.formatDoubleArray("%11.8f", rawMatrix1D.toArray(), ", "));
    sb.append(" ]");
    return sb.toString();
  }

}
