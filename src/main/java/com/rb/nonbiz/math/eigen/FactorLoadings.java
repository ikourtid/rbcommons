package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

public class FactorLoadings {

  private final double[] loadings;

  private FactorLoadings(double[] loadings) {
    this.loadings = loadings;
  }

  public static FactorLoadings factorLoadings(double ... loadings) {
    RBPreconditions.checkArgument(
        loadings.length > 0,
        "You probably don't want an empty factor loadings vector");
    return new FactorLoadings(loadings);
  }

  public double[] getLoadings() {
    return loadings;
  }

  public double getLoading(int index) {
    return loadings[index];
  }

  public int size() {
    return loadings.length;
  }

  @Override
  public String toString() {
    return Strings.format("Factor loadings: %s", Strings.formatDoubleArray(loadings));
  }

}
