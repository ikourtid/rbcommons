package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.DoubleFunction;

import static com.rb.nonbiz.math.stats.NormalDistribution.NormalDistributionBuilder.normalDistributionBuilder;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.junit.Assert.fail;

public class NormalDistributionTest extends RBTestMatcher<NormalDistribution> {

  public static NormalDistribution standardNormalDistribution() {
    return normalDistributionBuilder()
        .setMean(0)
        .setStandardDeviation(1)
        .build();
  }

  @Test
  public void badStandardDeviations_throws() {
    for (double mean : new double[] { -1.1, -1e-9, 0, 1e-9, 1.1 }) {
      DoubleFunction<NormalDistribution> maker = stdev -> normalDistributionBuilder()
          .setMean(mean)
          .setStandardDeviation(stdev)
          .build();

      assertIllegalArgumentException( () -> maker.apply(-1.23));
      assertIllegalArgumentException( () -> maker.apply(-1e-9));
      NormalDistribution doesNotThrow;
      doesNotThrow = maker.apply(0);
      doesNotThrow = maker.apply(1e-9);
      doesNotThrow = maker.apply(1e-7);
      doesNotThrow = maker.apply(1.23);
    }
  }

  @Override
  public NormalDistribution makeTrivialObject() {
    return standardNormalDistribution();
  }

  @Override
  public NormalDistribution makeNontrivialObject() {
    return normalDistributionBuilder()
        .setMean(-1.2)
        .setStandardDeviation(3.4)
        .build();
  }

  @Override
  public NormalDistribution makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return normalDistributionBuilder()
        .setMean(-1.2 + e)
        .setStandardDeviation(3.4 + e)
        .build();
  }

  @Override
  protected boolean willMatch(NormalDistribution expected, NormalDistribution actual) {
    return normalDistributionMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<NormalDistribution> normalDistributionMatcher(NormalDistribution expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getMean(),              DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getStandardDeviation(), DEFAULT_EPSILON_1e_8));
  }

}
