package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMaxUsingDefaultWeight.createArtificialTermForMaxUsingDefaultWeight;
import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMaxUsingSpecifiedWeight.CreateArtificialTermForMaxUsingSpecifiedWeightBuilder.createArtificialTermForMaxUsingSpecifiedWeightBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.DoNotCreateArtificialTermForMax.doNotCreateArtificialTermForMax;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalDoubleEmpty;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static junit.framework.TestCase.assertEquals;

public class MaxSuperVarGenerationInstructionsApplierTest extends RBTest<MaxSuperVarGenerationInstructionsApplier> {

  @Test
  public void toldNotToCreateArtificialTerms_returnsEmpty() {
    assertOptionalDoubleEmpty(makeTestObject().getWeightForArtificialTermForMaxOfConstantAndNonConstant(
        constantTerm(DUMMY_DOUBLE), doNotCreateArtificialTermForMax()));
    assertOptionalDoubleEmpty(makeTestObject().getWeightForArtificialTermForMaxOfTwoNonConstants(
        doNotCreateArtificialTermForMax()));
  }

  @Test
  public void toldToUseDefaultWeight_returnsDefaultWeight() {
    assertEquals(
        0.12345,
        makeTestObject().getWeightForArtificialTermForMaxOfConstantAndNonConstant(
            constantTerm(DUMMY_DOUBLE), createArtificialTermForMaxUsingDefaultWeight(makeConstantsObject(0.12345)))
        .getAsDouble(),
        1e-8);
    assertEquals(
        0.12345,
        makeTestObject().getWeightForArtificialTermForMaxOfTwoNonConstants(
            createArtificialTermForMaxUsingDefaultWeight(makeConstantsObject(0.12345)))
        .getAsDouble(),
        1e-8);
  }

  @Test
  public void toldToUseDifferentWeightThanDefault_maxOfTwoNonConstants_usesDifferentWeight() {
    for (double minWeight : ImmutableList.of(1e-2, 1e-3, 1e-4)) {
      assertEquals(
          1e-3,
          makeTestObject().getWeightForArtificialTermForMaxOfTwoNonConstants(
              createArtificialTermForMaxUsingSpecifiedWeightBuilder()
                  .setWeightMultiplier(unitFraction(1e-3))
                  .setMinWeight(minWeight)
                  .build())
              .getAsDouble(),
          1e-8);
    }
  }

  @Test
  public void toldToUseDifferentWeightThanDefault_maxOfConstantAndNonConstant_resultIsBasedOffConstantExpression() {
    BiConsumer<ConstantTerm, Double> asserter = (constantTerm, expectedResult) ->
        assertEquals(
            expectedResult,
            makeTestObject().getWeightForArtificialTermForMaxOfConstantAndNonConstant(
                constantTerm,
                createArtificialTermForMaxUsingSpecifiedWeightBuilder()
                    .setWeightMultiplier(unitFraction(1e-5))
                    .setMinWeight(1e-7)
                    .build())
            .getAsDouble(),
            1e-8);
    asserter.accept(constantTerm(-1_000_000), 10.0);
    asserter.accept(constantTerm(-100_000), 1.0);
    asserter.accept(constantTerm(-10_000), 0.1);
    asserter.accept(constantTerm(-1_000), 1e-2);
    asserter.accept(constantTerm(-100), 1e-3);
    asserter.accept(constantTerm(-10), 1e-4);
    asserter.accept(constantTerm(-1), 1e-5);
    asserter.accept(constantTerm(-0.1), 1e-6);
    asserter.accept(constantTerm(-0.01), 1e-7);
    asserter.accept(constantTerm(-1e-3), 1e-7);
    asserter.accept(constantTerm(-1e-4), 1e-7);
    asserter.accept(constantTerm(-1e-5), 1e-7);
    asserter.accept(constantTerm(-1e-6), 1e-7);
    asserter.accept(constantTerm(-1e-7), 1e-7);
    asserter.accept(constantTerm(-1e-8), 1e-7);
    asserter.accept(constantTerm(0), 1e-7);
    asserter.accept(constantTerm(1e-8), 1e-7);
    asserter.accept(constantTerm(1e-7), 1e-7);
    asserter.accept(constantTerm(1e-6), 1e-7);
    asserter.accept(constantTerm(1e-5), 1e-7);
    asserter.accept(constantTerm(1e-4), 1e-7);
    asserter.accept(constantTerm(1e-3), 1e-7);
    asserter.accept(constantTerm(0.01), 1e-7);
    asserter.accept(constantTerm(0.1), 1e-6);
    asserter.accept(constantTerm(1), 1e-5);
    asserter.accept(constantTerm(10), 1e-4);
    asserter.accept(constantTerm(100), 1e-3);
    asserter.accept(constantTerm(1_000), 1e-2);
    asserter.accept(constantTerm(10_000), 0.1);
    asserter.accept(constantTerm(100_000), 1.0);
    asserter.accept(constantTerm(1_000_000), 10.0);
  }

  private RBCommonsConstants makeConstantsObject(double value) {
    return new RBCommonsConstants() {
      @Override
      public double getDefaultWeightForMinAndMaxArtificialTerms() {
        return value;
      }
    };
  }

  @Override
  protected MaxSuperVarGenerationInstructionsApplier makeTestObject() {
    return new MaxSuperVarGenerationInstructionsApplier();
  }

}
