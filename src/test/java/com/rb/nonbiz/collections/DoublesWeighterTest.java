package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import java.util.List;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertEquals;

public class DoublesWeighterTest extends RBTest<DoublesWeighter> {

  @Test
  public void weightedAverage_happyPath_3valuesAndWeights_producesCorrectResults() {
    assertProducesWeightedAverageValue(doubleExplained(0.6, (0 + 1 + 0.8) / 3),
        ImmutableList.of(0.0, 1.0, 0.8),
        ImmutableList.of(1.0, 1.0, 1.0));
    assertProducesWeightedAverageValue(doubleExplained(0.6, (0 + 1 + 0.8) / 3),
        ImmutableList.of(0.0, 1.0, 0.8),
        ImmutableList.of(999.0, 999.0, 999.0));
    assertProducesWeightedAverageValue(doubleExplained(0.6, (0 + 1 + 0.8) / 3),
        ImmutableList.of(0.0, 1.0, 0.8),
        ImmutableList.of(1 / 3.0, 1 / 3.0, 1 / 3.0));
    assertProducesWeightedAverageValue(0.0,
        ImmutableList.of(0.0, 1.0, 0.8),
        ImmutableList.of(DUMMY_DOUBLE, 0.0, 0.0));
    assertProducesWeightedAverageValue(1.0,
        ImmutableList.of(0.0, 1.0, 0.8),
        ImmutableList.of(0.0, DUMMY_DOUBLE, 0.0));
    assertProducesWeightedAverageValue(0.8,
        ImmutableList.of(0.0, 1.0, 0.8),
        ImmutableList.of(0.0, 0.0, DUMMY_DOUBLE));
    assertProducesWeightedAverageValue(doubleExplained(0.52, (0.5 * 0.0 + 2 * 0.3 + 2.5 * 0.8) / (0.5 + 2.0 + 2.5)),
        ImmutableList.of(0.0, 0.3, 0.8),
        ImmutableList.of(0.5, 2.0, 2.5));
  }

  @Test
  public void weightedAverage_singleItem_resultIsExact_valueDoesntMatter_weightDoesntMatter() {
    for (double w : ImmutableList.of(0.1, 0.99, 1.0, 1.01, 100.0)) {
      assertProducesWeightedAverageValue(0.3, singletonList(0.3), singletonList(w), 0);
    }
    for (double w : ImmutableList.of(0.1, 0.99, 1.0, 1.01, 100.0)) {
      assertProducesWeightedAverageValue(0.0, singletonList(0.0), singletonList(w), 0);
    }
    for (double w : ImmutableList.of(0.1, 0.99, 1.0, 1.01, 100.0)) {
      assertProducesWeightedAverageValue(1.0, singletonList(1.0), singletonList(w), 0);
    }
  }

  @Test
  public void weightedAverage_moreWeightsThanValues_throws() {
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, singletonList(0.3), ImmutableList.of(0.4, 0.6)));
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, emptyList(), singletonList(0.4)));
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, emptyList(), singletonList(1.0)));
  }

  @Test
  public void weightedAverage_moreValuesThanWeights_throws() {
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, singletonList(0.3), emptyList()));
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, ImmutableList.of(0.3, 0.3), singletonList(0.4)));
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, ImmutableList.of(0.3, 0.3), singletonList(1.0)));
  }

  @Test
  public void weightedAverage_noValuesOrWeights_throws() {
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, emptyList(), emptyList()));
  }

  @Test
  public void weightedAverage_allWeightsAreNegative_throws() {
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, ImmutableList.of(0.3, 0.3), ImmutableList.of(-1.0, -1.0)));
  }

  @Test
  public void weightedAverage_weightOf0_notAllWeightsAreZero_doesNotThrow() {
    assertProducesWeightedAverageValue(0.3, ImmutableList.of(0.3, 0.4), ImmutableList.of(999.0, 0.0));
    assertProducesWeightedAverageValue(0.4, ImmutableList.of(0.3, 0.4), ImmutableList.of(0.0, 999.0));
  }

  @Test
  public void weightedAverage_allWeights0_throws() {
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, ImmutableList.of(0.3, 0.4), ImmutableList.of(0.0, 0.0)));
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, singletonList(0.3), singletonList(0.0)));
  }

  // This is just to make the tests more concise so it's easier to see what's going on.
  // PreciseValueWeighter is generic, but for the test I need to use some concrete classes.
  private void assertProducesWeightedAverageValue(double expected, List<Double> values, List<Double> weights) {
    assertProducesWeightedAverageValue(expected, values, weights, DEFAULT_EPSILON_1e_8);
  }

  private void assertProducesWeightedAverageValue(double expected, List<Double> values, List<Double> weights, Epsilon epsilon) {
    assertEquals(expected, makeTestObject().makeWeightedAverage(values, weights), epsilon.doubleValue());
  }

  @Override
  protected DoublesWeighter makeTestObject() {
    return new DoublesWeighter();
  }

}
