package com.rb.nonbiz.search;

import com.rb.nonbiz.search.BinarySearchRawParameters.BinarySearchRawParametersBuilder;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.hamcrest.MatcherAssert.assertThat;

public class BinarySearchInitialUpperBoundTightenerTest extends RBTest<BinarySearchInitialUpperBoundTightener> {

  private final Function<BigDecimal, Double> EVALUATE_INPUT_TO_SQUARE = x -> x.multiply(x).doubleValue();
  private final int MAX_ITERATIONS = 10;

  private final BigDecimal STARTING_LOWER_BOUND_FOR_SEARCH_ZERO = BigDecimal.ZERO;

  @Test
  public void noEarlyTermination_runsAllIterations() {
    assertResult(
        DEFAULT_EPSILON_1e_8,
        // Hard to explain via doubleExplained, but 0.999023438 is what you get if you keep halving the distance from
        // 0 to 1: 0.5, 0.75, ...
        // Actually, for N iterations, that will give us 1 + 2^(-N).
        BigDecimal.valueOf(doubleExplained(0.999023438, 1 - Math.pow(2, -1 * MAX_ITERATIONS))));
  }

  @Test
  public void earlyTerminates() {
    assertResult(
        epsilon(0.01),
        // This is what you get if you keep halving the distance from
        // 0 to 1: 0.5, 0.75, ... but also stop at N = 7 iterations, which is the # of iterations we happen to need
        // to get an x that's within 0.01 of 1.0.
        // For N iterations, that will give us 1 + 2^(-N).
        BigDecimal.valueOf(doubleExplained(0.9921875, 1 - Math.pow(2, -1 * 7))));
  }

  private void assertResult(
      Epsilon terminationPredicateEpsilon,
      BigDecimal expectedResult) {
    assertThat(
        makeTestObject().tighten(
            STARTING_LOWER_BOUND_FOR_SEARCH_ZERO,
            BinarySearchRawParametersBuilder.<BigDecimal, Double>binarySearchRawParametersBuilder()
                .setComparatorForX(BigDecimal::compareTo)
                .setComparatorForY(Double::compare)
                .setTargetY(doubleExplained(1.0, EVALUATE_INPUT_TO_SQUARE.apply(BigDecimal.ONE)))
                .setEvaluatorOfX(EVALUATE_INPUT_TO_SQUARE)
                .setMidpointGenerator( (v1, v2) -> v1.add(v2).multiply(BigDecimal.valueOf(0.5)))
                .setMaxIterations(MAX_ITERATIONS)
                .build(),
            BigDecimal.ONE,
            (x, ignoredY) -> terminationPredicateEpsilon.valuesAreWithin(x.doubleValue(), 1.0)),
        bigDecimalMatcher(expectedResult, DEFAULT_EPSILON_1e_8));
  }

  @Override
  protected BinarySearchInitialUpperBoundTightener makeTestObject() {
    return new BinarySearchInitialUpperBoundTightener();
  }
  
}
