package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiFunction;

import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMaxUsingSpecifiedWeight.CreateArtificialTermForMaxUsingSpecifiedWeightBuilder.createArtificialTermForMaxUsingSpecifiedWeightBuilder;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class CreateArtificialTermForMaxUsingSpecifiedWeightTest extends RBTestMatcher<CreateArtificialTermForMaxUsingSpecifiedWeight> {

  @Test
  public void badValues_throws() {
    BiFunction<UnitFraction, Double, CreateArtificialTermForMaxUsingSpecifiedWeight> maker = (weightMultiplier, minWeight) ->
        createArtificialTermForMaxUsingSpecifiedWeightBuilder()
        .setWeightMultiplier(weightMultiplier)
        .setMinWeight(minWeight)
        .build();
    CreateArtificialTermForMaxUsingSpecifiedWeight doesNotThrow;
    double validMinWeight = 1e-7;
    assertIllegalArgumentException( () -> maker.apply(UNIT_FRACTION_0, validMinWeight));
    doesNotThrow = maker.apply(unitFraction(1e-10), validMinWeight);
    doesNotThrow = maker.apply(unitFraction(0.009), validMinWeight);
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.011), validMinWeight));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.1), validMinWeight));
    assertIllegalArgumentException( () -> maker.apply(UNIT_FRACTION_1, validMinWeight));

    UnitFraction validWeightMultiplier = unitFraction(1e-3);
    assertIllegalArgumentException( () -> maker.apply(validWeightMultiplier, -999.0));
    assertIllegalArgumentException( () -> maker.apply(validWeightMultiplier, -1.0));
    assertIllegalArgumentException( () -> maker.apply(validWeightMultiplier, -1e-8));
    assertIllegalArgumentException( () -> maker.apply(validWeightMultiplier, 0.0));
    doesNotThrow = maker.apply(validWeightMultiplier, 1e-8);
    doesNotThrow = maker.apply(validWeightMultiplier, 1e-3);
    doesNotThrow = maker.apply(validWeightMultiplier, 999.0);
  }

  @Override
  public CreateArtificialTermForMaxUsingSpecifiedWeight makeTrivialObject() {
    return createArtificialTermForMaxUsingSpecifiedWeightBuilder()
        .setWeightMultiplier(unitFraction(1e-3))
        .setMinWeight(1.0)
        .build();
  }

  @Override
  public CreateArtificialTermForMaxUsingSpecifiedWeight makeNontrivialObject() {
    return createArtificialTermForMaxUsingSpecifiedWeightBuilder()
        .setWeightMultiplier(unitFraction(1e-6))
        .setMinWeight(1e-7)
        .build();
  }

  @Override
  public CreateArtificialTermForMaxUsingSpecifiedWeight makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return createArtificialTermForMaxUsingSpecifiedWeightBuilder()
        .setWeightMultiplier(unitFraction(1e-6 + e))
        .setMinWeight(1e-7 + e)
        .build();
  }

  @Override
  protected boolean willMatch(CreateArtificialTermForMaxUsingSpecifiedWeight expected,
                              CreateArtificialTermForMaxUsingSpecifiedWeight actual) {
    return createArtificialTermForMaxUsingSpecifiedWeightMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<CreateArtificialTermForMaxUsingSpecifiedWeight> createArtificialTermForMaxUsingSpecifiedWeightMatcher(
      CreateArtificialTermForMaxUsingSpecifiedWeight expected) {
    return makeMatcher(expected,
        matchUsingAlmostEquals(v -> v.getWeightMultiplier(), 1e-8),
        matchUsingDoubleAlmostEquals(v -> v.getMinWeight(), 1e-8));
  }

}
