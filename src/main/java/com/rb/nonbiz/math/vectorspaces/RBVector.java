package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * This is just a thin wrapper around a Colt DoubleMatrix1D, except that we do not expose any methods that could
 * result in mutating the object.
 *
 * We don't have 'double' in the name, because we never use floats, and I don't even know if it ever makes sense
 * to have an int. And we certainly don't use complex numbers. So 'double' is implied here.
 *
 * Also, since our usage pattern does not allow for empty vectors, we prohibit that as well.
 */
public class RBVector {

  private final DoubleMatrix1D rawDoubleMatrix1D;

  private RBVector(DoubleMatrix1D rawDoubleMatrix1D) {
    this.rawDoubleMatrix1D = rawDoubleMatrix1D;
  }

  public static RBVector rbVector(DoubleMatrix1D rawDoubleMatrix1D) {
    RBPreconditions.checkArgument(
        rawDoubleMatrix1D.size() > 0,
        "Cannot have 0 items in a vector");
    return new RBVector(rawDoubleMatrix1D);
  }

  public static RBVector rbVector(double[] values) {
    return rbVector(new DenseDoubleMatrix1D(values));
  }

  public static RBVector zeroRBVectorWithDimension(int dimension) {
    return rbVector(new DenseDoubleMatrix1D(dimension));
  }

  public double dotProduct(RBVector other) {
    return rawDoubleMatrix1D.zDotProduct(other.rawDoubleMatrix1D);
  }

  public int size() {
    return rawDoubleMatrix1D.size();
  }

  public boolean isAlmostUnitVector(double epsilon) {
    return Math.abs(calculateMagnitude() - 1) <= epsilon;
  }

  /**
   * Using 'calculate' to clarify that this is not cached, which means you should avoid calling this multiple times,
   * and/or cache it yourself.
   */
  public double calculateMagnitude() {
    return Math.sqrt(rawDoubleMatrix1D.zDotProduct(rawDoubleMatrix1D));
  }

  public RBVector multiplyByScalar(double scalar) {
    double[] newCoordinates = new double[rawDoubleMatrix1D.size()];
    Arrays.setAll(newCoordinates, i -> scalar * rawDoubleMatrix1D.getQuick(i));
    return new RBVector(new DenseDoubleMatrix1D(newCoordinates));
  }

  /**
   *
   *            V
   *          /
   *        /
   *      /
   *    /
   *  /
   * O----------P-----U
   *
   * The ASCII art here shows the projection of vector V onto vector U, which is point P.
   * All vectors are assumed to start at the origin O above.
   *
   * The formula for projection of V onto U is {@code (<V, U> / <U, U>) * U}.
   * That is, the resulting vector has the same direction as U, but its magnitude is the length of the projection.
   */
  public RBVector projectOnto(RBVector u) {
    RBSimilarityPreconditions.checkBothSame(
        this.size(),
        u.size(),
        "Dot product can only happen for vectors of the same size");
    double uDotU = u.dotProduct(u);
    if (Math.abs(uDotU) < 1e-8) {
      return zeroRBVectorWithDimension(u.size()); // this weird case is by definition of projection; see Wikipedia
    }
    double vDotU = this.dotProduct(u);
    return u.multiplyByScalar(vDotU / uDotU);
  }

  public double get(int index) {
    return rawDoubleMatrix1D.get(index);
  }

  /**
   * This breaks the abstraction of the Colt library a bit, but since Colt offers this, let's expose it.
   */
  public double getQuick(int index) {
    return rawDoubleMatrix1D.getQuick(index);
  }

  /**
   * This is here to help the test matcher, hence the 'Unsafe' in the name, and the package-private status.
   */
  DoubleMatrix1D getRawDoubleMatrix1DUnsafe() {
    return rawDoubleMatrix1D;
  }

  /**
   * Returns a {@link DoubleStream} with all values in this vector.
   */
  public DoubleStream doubleStream() {
    return Arrays.stream(rawDoubleMatrix1D.toArray());
  }

  @Override
  public String toString() {
    return Strings.format("[RBV %s RBV]", Strings.formatDoubleArray(rawDoubleMatrix1D.toArray()));
  }

}
