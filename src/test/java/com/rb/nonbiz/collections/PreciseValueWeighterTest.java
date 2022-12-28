package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.OnesBasedReturn;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.OnesBasedReturn.FLAT_RETURN;
import static com.rb.biz.types.OnesBasedReturn.onesBasedReturn;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class PreciseValueWeighterTest extends RBTest<PreciseValueWeighter> {

  @Test
  public void testUsingOnesBasedReturns() {
    List<OnesBasedReturn> returns = ImmutableList.of(
        onesBasedReturn(0.99),
        FLAT_RETURN,
        onesBasedReturn(1.04));
    assertAlmostEquals(
        onesBasedReturn(doubleExplained(1.01, (0.99 + 1.00 + 1.04) / 3)),
        onesBasedReturn(makeTestObject().makeUnweightedAverage(returns)),
        1e-8);
    assertAlmostEquals(
        onesBasedReturn(doubleExplained(1.0025, (60 * 0.99 + 0 * 1.00 + 20 * 1.04) / (60 + 0 + 20))),
        onesBasedReturn(makeTestObject().makeWeightedAverage(
            returns,
            ImmutableList.of(money(60), ZERO_MONEY, money(20)))),
        1e-8);
    assertAlmostEquals(
        onesBasedReturn(doubleExplained(1.0025, (60 * 0.99 + 0 * 1.00 + 20 * 1.04) / (60 + 0 + 20))),
        onesBasedReturn(makeTestObject().makeWeightedAverageWithBigDecimalWeights(
            returns,
            ImmutableList.of(BigDecimal.valueOf(60), BigDecimal.ZERO, BigDecimal.valueOf(20)))),
        1e-8);
  }

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
  public void unweightedAverage_happyPath() {
    assertProducesUnweightedAverageValue(doubleExplained(0.6, (0.0 + 1.0 + 0.8) / 3), ImmutableList.of(0.0, 1.0, 0.8));
    assertProducesUnweightedAverageValue(doubleExplained(0.0, (0.0 + 0.0 + 0.0) / 3), ImmutableList.of(0.0, 0.0, 0.0));
    assertProducesUnweightedAverageValue(doubleExplained(1.0, (1.0 + 1.0 + 1.0) / 3), ImmutableList.of(1.0, 1.0, 1.0));
  }

  @Test
  public void unweightedAverageSingletonList_resultIsExact() {
    assertProducesUnweightedAverageValue(0.0, singletonList(0.0), 0);
    assertProducesUnweightedAverageValue(0.6, singletonList(0.6), 0);
    assertProducesUnweightedAverageValue(1.0, singletonList(1.0), 0);
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
    assertIllegalArgumentException( () -> assertProducesUnweightedAverageValue(0.12345, emptyList()));
  }

  @Test
  public void weightedAverage_negativeWeightExists_throws() {
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, ImmutableList.of(0.3, 0.3), ImmutableList.of(-1.0, -1.0)));
    assertIllegalArgumentException( () -> assertProducesWeightedAverageValue(0.12345, ImmutableList.of(0.3, 0.3), ImmutableList.of(999.0, -1.0)));
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
    assertProducesWeightedAverageValue(expected, values, weights, 1e-8);
  }

  private void assertProducesWeightedAverageValue(double expected, List<Double> values, List<Double> weights, Epsilon epsilon) {
    assertAlmostEquals(
        signedQuantity(expected),
        signedQuantity(makeTestObject().makeWeightedAverage(
            values.stream().map(v -> signedQuantity(v)).collect(Collectors.toList()),
            weights.stream().map(w -> money(w)).collect(Collectors.toList()))),
        epsilon);
  }

  // This is just to make the tests more concise so it's easier to see what's going on.
  // PreciseValueWeighter is generic, but for the test I need to use some concrete classes.
  private void assertProducesUnweightedAverageValue(double expected, List<Double> values) {
    assertProducesUnweightedAverageValue(expected, values, 1e-8);
  }

  private void assertProducesUnweightedAverageValue(double expected, List<Double> values, Epsilon epsilon) {
    assertAlmostEquals(
        signedQuantity(expected),
        signedQuantity(makeTestObject().makeUnweightedAverage(
            values.stream().map(v -> signedQuantity(v)).collect(Collectors.toList()))),
        epsilon);
  }

  @Override
  protected PreciseValueWeighter makeTestObject() {
    return new PreciseValueWeighter();
  }

}
