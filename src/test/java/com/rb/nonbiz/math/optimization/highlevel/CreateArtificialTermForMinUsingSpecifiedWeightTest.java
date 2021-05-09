package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiFunction;

import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMinUsingSpecifiedWeight.CreateArtificialTermForMinUsingSpecifiedWeightBuilder.createArtificialTermForMinUsingSpecifiedWeightBuilder;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class CreateArtificialTermForMinUsingSpecifiedWeightTest extends RBTestMatcher<CreateArtificialTermForMinUsingSpecifiedWeight> {

  @Test
  public void badValues_throws() {
    BiFunction<UnitFraction, Double, CreateArtificialTermForMinUsingSpecifiedWeight> maker = (weightMultiplier, maxWeight) ->
        createArtificialTermForMinUsingSpecifiedWeightBuilder()
        .setWeightMultiplier(weightMultiplier)
        .setMaxWeight(maxWeight)
        .build();
    CreateArtificialTermForMinUsingSpecifiedWeight doesNotThrow;
    double validMaxWeight = -1e-7;
    assertIllegalArgumentException( () -> maker.apply(UNIT_FRACTION_0, validMaxWeight));
    doesNotThrow = maker.apply(unitFraction(1e-10), validMaxWeight);
    doesNotThrow = maker.apply(unitFraction(0.009), validMaxWeight);
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.011), validMaxWeight));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.1), validMaxWeight));
    assertIllegalArgumentException( () -> maker.apply(UNIT_FRACTION_1, validMaxWeight));

    UnitFraction validWeightMultiplier = unitFraction(1e-3);
    assertIllegalArgumentException( () -> maker.apply(validWeightMultiplier, 999.0));
    assertIllegalArgumentException( () -> maker.apply(validWeightMultiplier, 1.0));
    assertIllegalArgumentException( () -> maker.apply(validWeightMultiplier, 1e-8));
    assertIllegalArgumentException( () -> maker.apply(validWeightMultiplier, 0.0));
    doesNotThrow = maker.apply(validWeightMultiplier, -1e-8);
    doesNotThrow = maker.apply(validWeightMultiplier, -1e-3);
    doesNotThrow = maker.apply(validWeightMultiplier, -999.0);
  }

  @Override
  public CreateArtificialTermForMinUsingSpecifiedWeight makeTrivialObject() {
    return createArtificialTermForMinUsingSpecifiedWeightBuilder()
        .setWeightMultiplier(unitFraction(1e-3))
        .setMaxWeight(-1.0)
        .build();
  }

  @Override
  public CreateArtificialTermForMinUsingSpecifiedWeight makeNontrivialObject() {
    return createArtificialTermForMinUsingSpecifiedWeightBuilder()
        .setWeightMultiplier(unitFraction(1e-6))
        .setMaxWeight(-1e-7)
        .build();
  }

  @Override
  public CreateArtificialTermForMinUsingSpecifiedWeight makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return createArtificialTermForMinUsingSpecifiedWeightBuilder()
        .setWeightMultiplier(unitFraction(1e-6 + e))
        .setMaxWeight(-1e-7 + e)
        .build();
  }

  @Override
  protected boolean willMatch(CreateArtificialTermForMinUsingSpecifiedWeight expected,
                              CreateArtificialTermForMinUsingSpecifiedWeight actual) {
    return createArtificialTermForMinUsingSpecifiedWeightMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<CreateArtificialTermForMinUsingSpecifiedWeight> createArtificialTermForMinUsingSpecifiedWeightMatcher(
      CreateArtificialTermForMinUsingSpecifiedWeight expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getWeightMultiplier(), 1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getMaxWeight(), 1e-8));
  }

}
