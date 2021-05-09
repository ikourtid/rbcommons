package com.rb.nonbiz.math.eigen;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertEquals;

public class CoverageCalculatorTest extends RBTest<CoverageCalculator> {

  @Test
  public void happyPath() {
    assertEquals(1, getNumForCoverage(new double[] { 10, 20, 30, 40 }, 0.01));
    assertEquals(1, getNumForCoverage(new double[] { 10, 20, 30, 40 }, 0.39));
    assertEquals(2, getNumForCoverage(new double[] { 10, 20, 30, 40 }, 0.41));
    assertEquals(2, getNumForCoverage(new double[] { 10, 20, 30, 40 }, 0.69));
    assertEquals(3, getNumForCoverage(new double[] { 10, 20, 30, 40 }, 0.71));
    assertEquals(3, getNumForCoverage(new double[] { 10, 20, 30, 40 }, 0.89));
    assertEquals(4, getNumForCoverage(new double[] { 10, 20, 30, 40 }, 0.91));
    assertEquals(4, getNumForCoverage(new double[] { 10, 20, 30, 40 }, 0.99));
  }

  @Test
  public void request0coverage_throws() {
    assertIllegalArgumentException( () -> getNumForCoverage(new double[] { 10, 20, 30, 40 }, 0));
  }

  @Test
  public void request100PercentCoverage_includesAllEigenvalues() {
    assertEquals(4, getNumForCoverage(new double[] { 10, 20, 30, 40 }, 1));
  }

  @Test
  public void noEigenvalues_throws() {
    assertIllegalArgumentException( () -> getNumForCoverage(new double[] {}, 1));
  }

  @Test
  public void eigenvaluesNotSorted_throws() {
    assertIllegalArgumentException( () -> getNumForCoverage(new double[] { 10, 20, 40, 30 }, 0.01));
  }

  @Test
  public void hasSomeZeroEigenvalues_doesNotThrow() {
    assertEquals(1, getNumForCoverage(new double[] { 0, 20, 30, 40 }, 0.01));
  }

  @Test
  public void hasSomeBarelyNegativeEigenvalues_doesNotThrow() {
    assertEquals(1, getNumForCoverage(new double[] { -1e-9, 20, 30, 40 }, 0.01));
  }

  @Test
  public void hasSomeVeryNegativeEigenvalues_throws() {
    assertIllegalArgumentException( () -> getNumForCoverage(new double[] { -1, 20, 30, 40 }, 0.01));
  }

  private int getNumForCoverage(double[] eigenvaluesAscending, double coverageFraction) {
    return makeTestObject().getNumEigenpairsForMinimumCoverage(
        new DenseDoubleMatrix1D(eigenvaluesAscending), unitFraction(coverageFraction));
  }

  @Override
  protected CoverageCalculator makeTestObject() {
    return new CoverageCalculator();
  }


}