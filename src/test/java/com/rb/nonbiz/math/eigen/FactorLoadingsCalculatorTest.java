package com.rb.nonbiz.math.eigen;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ImmutableIndexableArray1D;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.eigen.Eigenpair.eigenpair;
import static com.rb.nonbiz.math.eigen.Eigenvalue.eigenvalue;
import static com.rb.nonbiz.math.eigen.EigenvectorTest.eigenvector;
import static com.rb.nonbiz.math.eigen.EigenvectorTest.vectorNorm;
import static com.rb.nonbiz.testutils.Asserters.assertDoubleArraysAlmostEqual;
import static org.junit.Assert.assertEquals;

public class FactorLoadingsCalculatorTest extends RBTest<FactorLoadingsCalculator> {

  @Test
  public void happyPath() {
    ImmutableIndexableArray1D<InstrumentId, FactorLoadings> factorLoadingsByInstrument =
        makeTestObject().calculateFactorLoadingsByInstrument(
            new DenseDoubleMatrix2D(new double[][] {
                { 0.9900, 0.9901, 0.9903, 0.9904 },
                { 0.9901, 0.9905, 0.9906, 0.9907 },
                { 0.9903, 0.9906, 0.9908, 0.9909 },
                { 0.9904, 0.9907, 0.9909, 0.9910 }
            }),
            ImmutableList.of(
                eigenpair(eigenvalue(50.0), eigenvector(5.0, 5.1, 5.2, 5.3)),
                eigenpair(eigenvalue(40.0), eigenvector(4.0, 4.1, 4.2, 4.3))),
            simpleArrayIndexMapping(STOCK_A, STOCK_B, STOCK_C, STOCK_D));
    assertEquals(4, factorLoadingsByInstrument.size());
    double eigen1Norm = vectorNorm(5, 5.1, 5.2, 5.3);
    double eigen2Norm = vectorNorm(4, 4.1, 4.2, 4.3);
    assertDoubleArraysAlmostEqual(
        new double[] {
            (0.9900 * 5.0 + 0.9901 * 5.1 + 0.9903 * 5.2 + 0.9904 * 5.3) / eigen1Norm,
            (0.9900 * 4.0 + 0.9901 * 4.1 + 0.9903 * 4.2 + 0.9904 * 4.3) / eigen2Norm },
        factorLoadingsByInstrument.get(STOCK_A).getLoadings(), 1e-8);
    assertDoubleArraysAlmostEqual(
        new double[] {
            (0.9901 * 5.0 + 0.9905 * 5.1 + 0.9906 * 5.2 + 0.9907 * 5.3) / eigen1Norm,
            (0.9901 * 4.0 + 0.9905 * 4.1 + 0.9906 * 4.2 + 0.9907 * 4.3) / eigen2Norm },
        factorLoadingsByInstrument.get(STOCK_B).getLoadings(), 1e-8);
    assertDoubleArraysAlmostEqual(
        new double[] {
            (0.9903 * 5.0 + 0.9906 * 5.1 + 0.9908 * 5.2 + 0.9909 * 5.3) / eigen1Norm,
            (0.9903 * 4.0 + 0.9906 * 4.1 + 0.9908 * 4.2 + 0.9909 * 4.3) / eigen2Norm },
        factorLoadingsByInstrument.get(STOCK_C).getLoadings(), 1e-8);
    assertDoubleArraysAlmostEqual(
        new double[] {
            (0.9904 * 5.0 + 0.9907 * 5.1 + 0.9909 * 5.2 + 0.9910 * 5.3) / eigen1Norm,
            (0.9904 * 4.0 + 0.9907 * 4.1 + 0.9909 * 4.2 + 0.9910 * 4.3) / eigen2Norm },
        factorLoadingsByInstrument.get(STOCK_D).getLoadings(), 1e-8);
  }

  @Override
  protected FactorLoadingsCalculator makeTestObject() {
    return new FactorLoadingsCalculator();
  }

}