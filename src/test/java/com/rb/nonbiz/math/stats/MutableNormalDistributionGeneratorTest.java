package com.rb.nonbiz.math.stats;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.stream.IntStream;

import static com.rb.nonbiz.math.stats.MutableNormalDistributionGenerator.mutableNormalDistributionGeneratorWithSeed;
import static com.rb.nonbiz.math.stats.NormalDistribution.NormalDistributionBuilder.normalDistributionBuilder;
import static com.rb.nonbiz.math.stats.NormalDistributionTest.normalDistributionMatcher;
import static com.rb.nonbiz.math.stats.NormalDistributionTest.standardNormalDistribution;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.RandomNumberGeneratorSeed.randomNumberGeneratorSeed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

public class MutableNormalDistributionGeneratorTest {

  @Test
  public void seedGuaranteesDeterminism() {
    // 123 is the seed for the random number generator
    MutableNormalDistributionGenerator generator =
        mutableNormalDistributionGeneratorWithSeed(standardNormalDistribution(), randomNumberGeneratorSeed(123));

    double[] expected = {
        -1.505603669780131,
         0.18330437656907184,
        -1.0499474918446852,
         0.5210058089715774,
        -0.08687486395603258
    };
    assertThat(
        IntStream.range(0, 5)
            .mapToDouble(ignored -> generator.nextDouble())
            .toArray(),
        doubleArrayMatcher(
            expected,
            DEFAULT_EPSILON_1e_8));

    // If we reuse the generator object, the next 5 numbers will of course be different...
    // note the 'not' below.
    assertThat(
        IntStream.range(0, 5)
            .mapToDouble(ignored -> generator.nextDouble())
            .toArray(),
        not(doubleArrayMatcher(
            expected,
            DEFAULT_EPSILON_1e_8)));

    // Finally, if we use a random number generator with a different seed (456), the numbers will also be different.
    MutableNormalDistributionGenerator generator2 =
        mutableNormalDistributionGeneratorWithSeed(standardNormalDistribution(), randomNumberGeneratorSeed(456));
    assertThat(
        IntStream.range(0, 5)
            .mapToDouble(ignored -> generator2.nextDouble())
            .toArray(),
        not(doubleArrayMatcher(
            expected,
            DEFAULT_EPSILON_1e_8)));
  }

  @Test
  public void generatesFromNormalDistribution() {
    double mean = -1.23;
    double stdev = 4.56;
    MutableNormalDistributionGenerator generator =
        mutableNormalDistributionGeneratorWithSeed(
            normalDistributionBuilder()
                .setMean(mean)
                .setStandardDeviation(stdev)
                .build(),
            randomNumberGeneratorSeed(789)); // just using a fixed seed for this test to be deterministic

    SummaryStatistics summaryStatistics = new SummaryStatistics();
    for (int i = 0; i < 1_000_000; i++) {
      summaryStatistics.addValue(generator.nextDouble());
    }
    // The observed mean and stdev from the generated numbers should be similar, but not identical.
    // Even with a million numbers, these aren't too close.
    // 2nd args are epsilons.
    assertEquals(mean, summaryStatistics.getMean(), 0.0005);
    assertEquals(stdev, summaryStatistics.getStandardDeviation(), 0.002);
  }

  // This ignores the member 'Normal normal', but it's better than nothing.
  // Remember that MutableNormalDistributionGenerator is a bit special, because it's kind of like a data class,
  // but also like a verb class.
  public static TypeSafeMatcher<MutableNormalDistributionGenerator> mutableNormalDistributionGeneratorMatcher(
      MutableNormalDistributionGenerator expected) {
    return makeMatcher(expected,
        match(v -> v.getNormalDistribution(), f -> normalDistributionMatcher(f)));
  }

}
