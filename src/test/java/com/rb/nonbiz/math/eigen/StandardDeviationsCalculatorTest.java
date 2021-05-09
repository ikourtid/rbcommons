package com.rb.nonbiz.math.eigen;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.stats.RBStats;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArrayMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static org.hamcrest.MatcherAssert.assertThat;

public class StandardDeviationsCalculatorTest extends RBTest<StandardDeviationsCalculator> {

  @Test
  public void generalCase() {
    assertThat(
        makeTestObject().getSampleStandardDeviationsForColumns(new DenseDoubleMatrix2D(new double[][] {
            { 7.0, 8.0 },
            { 7.1, 8.2 },
            { 7.2, 8.4 },
            { 7.3, 8.6 }
        })),
        doubleArrayMatcher(
            new double[] {
                doubleExplained(0.12909944, RBStats.toStatisticalSummary(ImmutableList.of(7.0, 7.1, 7.2, 7.3)).getStandardDeviation()),
                doubleExplained(0.25819889, RBStats.toStatisticalSummary(ImmutableList.of(8.0, 8.2, 8.4, 8.6)).getStandardDeviation()),
            },
            1e-8));
  }

  @Override
  protected StandardDeviationsCalculator makeTestObject() {
    return new StandardDeviationsCalculator();
  }

}
