package com.rb.nonbiz.math.beta;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.InterpolationPreference;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.beta.Beta.beta;
import static com.rb.nonbiz.math.beta.BetaTest.betaMatcher;
import static com.rb.nonbiz.math.beta.BetaTransformationInstructions.BetaTransformationInstructionsBuilder.betaTransformationInstructionsBuilder;
import static com.rb.nonbiz.math.beta.BetaTransformationInstructions.ZERO_PREFERENCE_FOR_ACTUAL_BETA;
import static com.rb.nonbiz.math.beta.BetaTransformationInstructions.ZERO_PREFERENCE_FOR_CORRELATION;
import static com.rb.nonbiz.math.beta.BetaTransformationInstructions.alwaysUseActualBetas;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.types.InterpolationPreference.USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED;
import static com.rb.nonbiz.types.InterpolationPreference.USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT;
import static com.rb.nonbiz.types.InterpolationPreference.preferSuppliedValueBy;
import static com.rb.nonbiz.types.InterpolationPreferenceTest.interpolationPreferenceMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class BetaTransformationInstructionsTest extends RBTestMatcher<BetaTransformationInstructions> {

  public static BetaTransformationInstructions alwaysUseBetaOf(double betaAsDouble) {
    InterpolationPreference dummyInterpolationPreference = ZERO_PREFERENCE_FOR_CORRELATION;
    return betaTransformationInstructionsBuilder()
        .setLabel(label(Strings.format("fixed beta of %s", betaAsDouble)))
        .resetDefaultBeta(beta(betaAsDouble))
        .setPreferenceForActualBeta(ZERO_PREFERENCE_FOR_ACTUAL_BETA)
        .setPreferenceForCorrelation(dummyInterpolationPreference)
        .build();
  }

  public static BetaTransformationInstructions alwaysUseBetaOf1() {
    return alwaysUseBetaOf(1.0);
  }

  @Test
  public void testSpecialCaseStaticConstructors() {
    assertThat(
        USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT,
        interpolationPreferenceMatcher(
            alwaysUseActualBetas().getPreferenceForActualBeta()));
    assertThat(
        USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED,
        interpolationPreferenceMatcher(
            alwaysUseBetaOf1().getPreferenceForActualBeta()));
  }

  @Override
  public BetaTransformationInstructions makeTrivialObject() {
    return alwaysUseActualBetas();
  }

  @Override
  public BetaTransformationInstructions makeNontrivialObject() {
    return betaTransformationInstructionsBuilder()
        .setLabel(label("nontrivial BTI"))
        .resetDefaultBeta(beta(-1.1))
        .setPreferenceForActualBeta( preferSuppliedValueBy(0.12345))
        .setPreferenceForCorrelation(preferSuppliedValueBy(0.54321))
        .build();
  }

  @Override
  public BetaTransformationInstructions makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return betaTransformationInstructionsBuilder()
        .setLabel(label("matching nonttrivial BTI")) // we never match on labels
        .resetDefaultBeta(beta(-1.1 + e))
        .setPreferenceForActualBeta( preferSuppliedValueBy(0.12345 + e))
        .setPreferenceForCorrelation(preferSuppliedValueBy(0.54321 + e))
        .build();
  }

  @Override
  protected boolean willMatch(BetaTransformationInstructions expected, BetaTransformationInstructions actual) {
    return betaTransformationInstructionsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<BetaTransformationInstructions> betaTransformationInstructionsMatcher(
      BetaTransformationInstructions expected) {
    return makeMatcher(expected,
        match(v -> v.getDefaultBeta(),              f -> betaMatcher(f)),
        match(v -> v.getPreferenceForActualBeta(),  f -> interpolationPreferenceMatcher(f)),
        match(v -> v.getPreferenceForCorrelation(), f -> interpolationPreferenceMatcher(f)));
  }

}
