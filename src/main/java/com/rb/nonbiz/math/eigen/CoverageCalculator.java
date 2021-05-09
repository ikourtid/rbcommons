package com.rb.nonbiz.math.eigen;

import cern.colt.matrix.DoubleMatrix1D;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class CoverageCalculator {

  public int getNumEigenpairsForMinimumCoverage(
      DoubleMatrix1D allEigenvaluesAscending, UnitFraction minimumCoverageUnitFraction) {
    RBPreconditions.checkArgument(
        !minimumCoverageUnitFraction.isZero(),
        "You cannot request 0 coverage");
    double sumOfPositiveEigenvalues = getSumOfPositiveEigenvaluesAndSanityCheck(allEigenvaluesAscending);
    // going from end to start, as biggest eigenvalues are towards the end (sorted ascending)
    double sumOfPositiveEigenvaluesSoFar = 0;
    int countOfPositiveEigenvaluesSoFar = 0;
    for (int i = allEigenvaluesAscending.size() - 1; i >= 0; i--) {
      double thisEigenvalue = allEigenvaluesAscending.get(i);
      if (thisEigenvalue < 0) {
        continue;
      }
      sumOfPositiveEigenvaluesSoFar += thisEigenvalue;
      countOfPositiveEigenvaluesSoFar++;
      if (unitFraction(sumOfPositiveEigenvaluesSoFar / sumOfPositiveEigenvalues).isGreaterThanOrEqualTo(minimumCoverageUnitFraction)) {
        return countOfPositiveEigenvaluesSoFar;
      }
    }
    throw new IllegalArgumentException(Strings.format(
        "Could not perform an eigendecomposition and maintain > %s of coverage",
        minimumCoverageUnitFraction));
  }

  private double getSumOfPositiveEigenvaluesAndSanityCheck(DoubleMatrix1D allEigenvaluesAscending) {
    double sumOfPositiveEigenvalues = 0;
    Double previousEigenvalue = null;
    for (int i = 0; i < allEigenvaluesAscending.size(); i++) {
      double thisEigenvalue = allEigenvaluesAscending.get(i);
      if (thisEigenvalue > 0) {
        sumOfPositiveEigenvalues += thisEigenvalue;
      } else {
        RBPreconditions.checkArgument(
            thisEigenvalue > -1e-6,
            "Eigenvalues can be negative due to numerical reasons, but not %s",
            thisEigenvalue);
      }
      if (i > 0) {
        RBPreconditions.checkArgument(
            previousEigenvalue < thisEigenvalue,
            "Eigenvalues should be ascending, but %s < %s in position %s",
            previousEigenvalue, thisEigenvalue, i);
      }
      previousEigenvalue = thisEigenvalue;
    }
    return sumOfPositiveEigenvalues;
  }

}
