package com.rb.nonbiz.math.optimization.general;

import com.rb.biz.investing.strategy.optbased.EigenSubObjective;
import com.rb.biz.investing.strategy.optbased.EsgSubObjective;
import com.rb.biz.investing.strategy.optbased.HoldingCostSubObjective;
import com.rb.biz.investing.strategy.optbased.NaiveSubObjective;
import com.rb.biz.investing.strategy.optbased.SingleNamedFactorModelSubObjective;
import com.rb.biz.investing.strategy.optbased.TaxSubObjective;
import com.rb.biz.investing.strategy.optbased.TransactionCostSubObjective;
import com.rb.biz.investing.strategy.optbased.rebal.lp.RawObjectiveValue;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplier.eigenSubObjectiveNormalizationMultiplier;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplier.esgSubObjectiveNormalizationMultiplier;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplier.holdingCostSubObjectiveNormalizationMultiplier;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplier.naiveSubObjectiveNormalizationMultiplier;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplier.objectiveValueNormalizationMultiplier;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplier.singleNamedFactorModelSubObjectiveNormalizationMultiplier;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplier.taxSubObjectiveNormalizationMultiplier;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplier.transactionCostSubObjectiveNormalizationMultiplier;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static junit.framework.TestCase.assertEquals;

/**
 * This test class is not generic, but the publicly exposed test matcher is.
 */
public class ObjectiveValueNormalizationMultiplierTest extends RBTestMatcher<ObjectiveValueNormalizationMultiplier<NaiveSubObjective>> {

  private static final double UNIT_NORMALIZATION = 1.0;

  public static ObjectiveValueNormalizationMultiplier<NaiveSubObjective> unitNaiveSubObjectiveNormalization() {
    return naiveSubObjectiveNormalizationMultiplier(UNIT_NORMALIZATION);
  }

  public static ObjectiveValueNormalizationMultiplier<EigenSubObjective> unitEigenSubObjectiveNormalization() {
    return eigenSubObjectiveNormalizationMultiplier(UNIT_NORMALIZATION);
  }

  public static ObjectiveValueNormalizationMultiplier<SingleNamedFactorModelSubObjective> unitSingleNamedFactorModelSubObjectiveNormalization() {
    return singleNamedFactorModelSubObjectiveNormalizationMultiplier(UNIT_NORMALIZATION);
  }

  public static ObjectiveValueNormalizationMultiplier<TaxSubObjective> unitTaxSubObjectiveNormalization() {
    return taxSubObjectiveNormalizationMultiplier(UNIT_NORMALIZATION);
  }

  public static ObjectiveValueNormalizationMultiplier<TransactionCostSubObjective> unitTransactionCostSubObjectiveNormalization() {
    return transactionCostSubObjectiveNormalizationMultiplier(UNIT_NORMALIZATION);
  }

  public static ObjectiveValueNormalizationMultiplier<HoldingCostSubObjective> unitHoldingCostSubObjectiveNormalization() {
    return holdingCostSubObjectiveNormalizationMultiplier(UNIT_NORMALIZATION);
  }

  public static ObjectiveValueNormalizationMultiplier<EsgSubObjective> unitEsgSubObjectiveNormalization() {
    return esgSubObjectiveNormalizationMultiplier(UNIT_NORMALIZATION);
  }

  @Test
  public void mustBePositive() {
    assertIllegalArgumentException( () -> objectiveValueNormalizationMultiplier(-0.01));
    assertIllegalArgumentException( () -> objectiveValueNormalizationMultiplier(0));

    assertIllegalArgumentException( () -> naiveSubObjectiveNormalizationMultiplier(-0.01));
    assertIllegalArgumentException( () -> naiveSubObjectiveNormalizationMultiplier(0));

    assertIllegalArgumentException( () -> eigenSubObjectiveNormalizationMultiplier(-0.01));
    assertIllegalArgumentException( () -> eigenSubObjectiveNormalizationMultiplier(0));

    assertIllegalArgumentException( () -> taxSubObjectiveNormalizationMultiplier(-0.01));
    assertIllegalArgumentException( () -> taxSubObjectiveNormalizationMultiplier(0));

    assertIllegalArgumentException( () -> transactionCostSubObjectiveNormalizationMultiplier(-0.01));
    assertIllegalArgumentException( () -> transactionCostSubObjectiveNormalizationMultiplier(0));

    assertIllegalArgumentException( () -> holdingCostSubObjectiveNormalizationMultiplier(-0.01));
    assertIllegalArgumentException( () -> holdingCostSubObjectiveNormalizationMultiplier(0));

    assertIllegalArgumentException( () -> esgSubObjectiveNormalizationMultiplier(-0.01));
    assertIllegalArgumentException( () -> esgSubObjectiveNormalizationMultiplier(0));

    for (double validNormalizationMultiplier : rbSetOf(1e-9, 0.01, 0.99, 1.0, 1.01, 10.0, 1e8)) {
      ObjectiveValueNormalizationMultiplier<?> doesNotThrow;
      doesNotThrow = naiveSubObjectiveNormalizationMultiplier(validNormalizationMultiplier);
      doesNotThrow = eigenSubObjectiveNormalizationMultiplier(validNormalizationMultiplier);
      doesNotThrow = taxSubObjectiveNormalizationMultiplier(validNormalizationMultiplier);
      doesNotThrow = transactionCostSubObjectiveNormalizationMultiplier(validNormalizationMultiplier);
      doesNotThrow = holdingCostSubObjectiveNormalizationMultiplier(validNormalizationMultiplier);
      doesNotThrow = esgSubObjectiveNormalizationMultiplier(validNormalizationMultiplier);
    }
  }

  @Test
  public void testNormalization() {
    ObjectiveValueNormalizationMultiplier<HoldingCostSubObjective> objectiveValueNormalization = holdingCostSubObjectiveNormalizationMultiplier(0.123);
    assertEquals(objectiveValueNormalization.getNormalizingMultiplier(), 0.123, 1e-8);

    RawObjectiveValue<HoldingCostSubObjective> rawObjectiveValue = new RawObjectiveValue<>(1_000);
    assertEquals(objectiveValueNormalization.toNormalized(rawObjectiveValue).doubleValue(), doubleExplained(123, 1_000 * 0.123), 1e-8);
  }

  @Override
  public ObjectiveValueNormalizationMultiplier<NaiveSubObjective> makeTrivialObject() {
    return unitNaiveSubObjectiveNormalization();
  }

  @Override
  public ObjectiveValueNormalizationMultiplier<NaiveSubObjective> makeNontrivialObject() {
    return naiveSubObjectiveNormalizationMultiplier(0.987);
  }

  @Override
  public ObjectiveValueNormalizationMultiplier<NaiveSubObjective> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return naiveSubObjectiveNormalizationMultiplier(0.987 + e);
  }

  @Override
  protected boolean willMatch(ObjectiveValueNormalizationMultiplier<NaiveSubObjective> expected,
                              ObjectiveValueNormalizationMultiplier<NaiveSubObjective> actual) {
    return objectiveValueNormalizationMultiplierOfSameTypeMatcher(expected).matches(actual);
  }

  public static <T extends LinearSubObjectiveFunction> TypeSafeMatcher<ObjectiveValueNormalizationMultiplier<T>>
  objectiveValueNormalizationMultiplierOfSameTypeMatcher(ObjectiveValueNormalizationMultiplier<T> expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getNormalizingMultiplier(), 1e-8));
  }

  public static TypeSafeMatcher<ObjectiveValueNormalizationMultiplier<? extends LinearSubObjectiveFunction>>
  objectiveValueNormalizationMultiplierMatcher(ObjectiveValueNormalizationMultiplier<? extends LinearSubObjectiveFunction> expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getNormalizingMultiplier(), 1e-8));
  }

}
