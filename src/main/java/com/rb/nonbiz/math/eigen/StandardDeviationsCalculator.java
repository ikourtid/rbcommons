package com.rb.nonbiz.math.eigen;

import cern.colt.matrix.DoubleMatrix2D;

public class StandardDeviationsCalculator {

  public double[] getSampleStandardDeviationsForColumns(DoubleMatrix2D matrix) {
    double[] standardDeviations = new double[matrix.columns()];
    for (int column = 0; column < matrix.columns(); column++) {
      double sum = 0;
      double sumOfSquares = 0;
      for (int row = 0; row < matrix.rows(); row++) {
        double matrixValue = matrix.getQuick(row, column);
        sum += matrixValue;
        sumOfSquares += matrixValue * matrixValue;
      }
      int size = matrix.rows();
      double mean = sum / size;
      double sampleVariance = (sumOfSquares - mean * sum) / (size - 1);
      standardDeviations[column] = Math.sqrt(sampleVariance);
    }
    return standardDeviations;
  }

}
