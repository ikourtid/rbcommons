package com.rb.nonbiz.math.beta;

import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.math.beta.Beta.beta;
import static com.rb.nonbiz.types.Interpolator.interpolateUsingPreference;

/**
 * Adjusts the actual beta to be something between a 'central beta' (typically 1) and the actual beta.
 * Can decrease beta by an amount proportional to the correlation coefficient
 *
 * @see BetaTransformationInstructions
 */
public class BetaTransformationInstructionsApplier {

  private final static double MAX_CORRELATION = 1;

  public Beta getTransformedBeta(Beta actualBeta,
                                 BetaTransformationInstructions betaTransformationInstructions) {
    double absCorrelation = Math.abs(
        getOrThrow(actualBeta.getBetaBackground(), "actualBeta here must always have a BetaBackground b/c it's real")
            .getCorrelation().doubleValue());
    double interpolatedBeta =
        interpolateUsingPreference(betaTransformationInstructions.getPreferenceForActualBeta())
            .betweenSuppliedValue(actualBeta.getValue())
            .andDefaultValue(betaTransformationInstructions.getDefaultBeta().getValue());
    double reductionFactorDueToCorrelation =
        interpolateUsingPreference(betaTransformationInstructions.getPreferenceForCorrelation())
            .betweenSuppliedValue(absCorrelation)
            .andDefaultValue(MAX_CORRELATION);
    double transformedBeta = interpolatedBeta * reductionFactorDueToCorrelation;
    return beta(transformedBeta, actualBeta.getBetaBackground());
  }

}
