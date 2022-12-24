package com.rb.nonbiz.math.eigen;

import cern.colt.matrix.DoubleMatrix2D;
import com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex;
import com.rb.nonbiz.math.vectorspaces.MatrixRowIndex;
import com.rb.nonbiz.math.vectorspaces.RBMatrix;

import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;

public class StandardDeviationsCalculator {

  public double[] getSampleStandardDeviationsForColumns(RBMatrix matrix) {
    double[] standardDeviations = new double[matrix.getNumColumns()];
    for (int column = 0; column < matrix.getNumColumns(); column++) {
      double sum = 0;
      double sumOfSquares = 0;
      for (int row = 0; row < matrix.getNumRows(); row++) {
        double matrixValue = matrix.get(matrixRowIndex(row), matrixColumnIndex(column));
        sum += matrixValue;
        sumOfSquares += matrixValue * matrixValue;
      }
      int size = matrix.getNumRows();
      double mean = sum / size;
      double sampleVariance = (sumOfSquares - mean * sum) / (size - 1);
      standardDeviations[column] = Math.sqrt(sampleVariance);
    }
    return standardDeviations;
  }

}
