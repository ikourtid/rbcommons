package com.rb.nonbiz.math.beta;

import com.rb.nonbiz.text.HasHumanReadableLabel;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.types.InterpolationPreference;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.math.beta.Beta.BETA_OF_1;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.types.InterpolationPreference.USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED;
import static com.rb.nonbiz.types.InterpolationPreference.USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT;

/**
 * We want a way to tweak the investing logic so that it uses a beta between some 'central' value (typically 1) and the
 * actual beta we have computed.
 *
 * If e.g. defaultBeta is 1, and the actual beta for a stock is 0.4, and the preferenceForCorrelation is 0.0, then
 *  preferenceForActualBeta = 0    {@code =>}  beta we will use is 1    = 1 * 1.00 + 0.4 * 0.00
 *  preferenceForActualBeta = 0.25 {@code =>}  beta we will use is 0.85 = 1 * 0.75 + 0.4 * 0.25
 *  preferenceForActualBeta = 1    {@code =>}  beta we will use is 0.4  = 1 * 0.00 + 0.4 * 1.00
 *
 * similar example, but for a high-beta stock of 1.6:
 *  preferenceForActualBeta = 0    {@code =>}  beta we will use is 1    = 1 * 1.00 + 1.6 * 0.00
 *  preferenceForActualBeta = 0.25 {@code =>}  beta we will use is 1.15 = 1 * 0.75 + 1.6 * 0.25
 *  preferenceForActualBeta = 1    {@code =>}  beta we will use is 1.6  = 1 * 0.00 + 1.7 * 1.00
 *
 * Additionally, we allow for the correlation to decrease the effective beta
 * If the preferenceForActualBeta = 0.75, and the actual beta is 0.4, and the correlation is 0.3, then
 *   interpolatedBeta = (1 * 0.25 + 0.4 * 0.75) = 0.25 + 0.3 = 0.55
 *  preferenceForCorrelation = 0     {@code =>}  beta we will use is 0.5500 = (1*0.25 + 0.4 * 0.75) * (1.0 * 1.00  + 0.3 * 0.000)
 *  preferenceForCorrelation = 0.333 {@code =>}  beta we will use is 0.4219 = (1*0.25 + 0.4 * 0.75) * (1.0 * 0.667 + 0.3 * 0.333)
 *  preferenceForCorrelation = 1     {@code =>}  beta we will use is 0.1650 = (1*0.25 + 0.4 * 0.75) * (1.0 * 0.00  + 0.3 * 1.000)
 */
public class BetaTransformationInstructions implements HasHumanReadableLabel {

  public static final InterpolationPreference ZERO_PREFERENCE_FOR_ACTUAL_BETA = USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED;
  public static final InterpolationPreference FULL_PREFERENCE_FOR_ACTUAL_BETA = USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT;
  public static final InterpolationPreference ZERO_PREFERENCE_FOR_CORRELATION = USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED;
  public static final InterpolationPreference FULL_PREFERENCE_FOR_CORRELATION = USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT;

  private final HumanReadableLabel label;
  private final Beta defaultBeta;
  private final InterpolationPreference preferenceForActualBeta;
  private final InterpolationPreference preferenceForCorrelation;

  private BetaTransformationInstructions(
      HumanReadableLabel label,
      Beta defaultBeta,
      InterpolationPreference preferenceForActualBeta,
      InterpolationPreference preferenceForCorrelation) {
    this.label = label;
    this.defaultBeta = defaultBeta;
    this.preferenceForActualBeta = preferenceForActualBeta;
    this.preferenceForCorrelation = preferenceForCorrelation;
  }

  /**
   * Always try to use the real betas; don't try to push them towards a default value such as 1.
   */
  public static BetaTransformationInstructions alwaysUseActualBetas() {
    Beta dummyDefaultBeta = BETA_OF_1;
    return new BetaTransformationInstructions(
        label("actual_betas"), dummyDefaultBeta, FULL_PREFERENCE_FOR_ACTUAL_BETA, ZERO_PREFERENCE_FOR_CORRELATION);
  }

  @Override
  public HumanReadableLabel getHumanReadableLabel() {
    return label;
  }

  public Beta getDefaultBeta() {
    return defaultBeta;
  }

  public InterpolationPreference getPreferenceForActualBeta() {
    return preferenceForActualBeta;
  }

  public InterpolationPreference getPreferenceForCorrelation() {
    return preferenceForCorrelation;
  }

  @Override
  public String toString() {
    return label.toString();
  }

  /**
   * You can only use it in cases where neither alwaysUseActualBetas nor alwaysUseDefaultBeta are applicable.
   * Usually, this will mean something like 'use a beta between the actual value and 1'.
   */
  public static class BetaTransformationInstructionsBuilder implements RBBuilder<BetaTransformationInstructions> {

    private HumanReadableLabel label;
    private Beta defaultBeta = BETA_OF_1;
    private InterpolationPreference preferenceForActualBeta;
    private InterpolationPreference preferenceForCorrelation;

    private BetaTransformationInstructionsBuilder() { }

    public static BetaTransformationInstructionsBuilder betaTransformationInstructionsBuilder() {
      return new BetaTransformationInstructionsBuilder();
    }

    public BetaTransformationInstructionsBuilder setLabel(HumanReadableLabel label) {
      this.label = checkNotAlreadySet(
          this.label,
          label);
      return this;
    }

    public BetaTransformationInstructionsBuilder resetDefaultBeta(Beta defaultBeta) {
      this.defaultBeta = checkAlreadySet(this.defaultBeta, defaultBeta);
      return this;
    }

    public BetaTransformationInstructionsBuilder setPreferenceForActualBeta(InterpolationPreference preferenceForActualBeta) {
      this.preferenceForActualBeta = checkNotAlreadySet(
          this.preferenceForActualBeta,
          preferenceForActualBeta);
      return this;
    }

    public BetaTransformationInstructionsBuilder setZeroPreferenceForActualBeta() {
      return setPreferenceForActualBeta(USE_DEFAULT_VALUE_INSTEAD_OF_SUPPLIED);
    }

    public BetaTransformationInstructionsBuilder setFullPreferenceForActualBeta() {
      return setPreferenceForActualBeta(USE_SUPPLIED_VALUE_INSTEAD_OF_DEFAULT);
    }

    public BetaTransformationInstructionsBuilder setPreferenceForCorrelation(InterpolationPreference preferenceForCorrelation) {
      this.preferenceForCorrelation = checkNotAlreadySet(
          this.preferenceForCorrelation,
          preferenceForCorrelation);
      return this;
    }

    public BetaTransformationInstructionsBuilder setZeroPreferenceForCorrelation() {
      return setPreferenceForCorrelation(ZERO_PREFERENCE_FOR_CORRELATION);
    }

    public BetaTransformationInstructionsBuilder setFullPreferenceForCorrelation() {
      return setPreferenceForCorrelation(FULL_PREFERENCE_FOR_CORRELATION);
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(label);
      RBPreconditions.checkNotNull(defaultBeta);
      RBPreconditions.checkNotNull(preferenceForActualBeta);
      RBPreconditions.checkNotNull(preferenceForCorrelation);
    }

    @Override
    public BetaTransformationInstructions buildWithoutPreconditions() {
      return new BetaTransformationInstructions(label, defaultBeta, preferenceForActualBeta, preferenceForCorrelation);
    }

  }

}
