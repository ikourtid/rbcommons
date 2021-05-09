package com.rb.nonbiz.math.beta;

import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.InterpolationPreference;
import org.junit.Test;

import java.util.Optional;

import static com.rb.nonbiz.math.beta.Beta.BETA_OF_1;
import static com.rb.nonbiz.math.beta.Beta.beta;
import static com.rb.nonbiz.math.beta.Beta.betaWithBackground;
import static com.rb.nonbiz.math.beta.BetaBackground.betaBackground;
import static com.rb.nonbiz.math.beta.BetaTest.betaMatcher;
import static com.rb.nonbiz.math.beta.BetaTransformationInstructions.BetaTransformationInstructionsBuilder.betaTransformationInstructionsBuilder;
import static com.rb.nonbiz.math.beta.BetaTransformationInstructions.FULL_PREFERENCE_FOR_ACTUAL_BETA;
import static com.rb.nonbiz.math.beta.BetaTransformationInstructions.FULL_PREFERENCE_FOR_CORRELATION;
import static com.rb.nonbiz.math.beta.BetaTransformationInstructions.ZERO_PREFERENCE_FOR_ACTUAL_BETA;
import static com.rb.nonbiz.math.beta.BetaTransformationInstructions.ZERO_PREFERENCE_FOR_CORRELATION;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArray2DMatcher;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Correlation.correlation;
import static com.rb.nonbiz.types.InterpolationPreference.preferSuppliedValueBy;
import static org.hamcrest.MatcherAssert.assertThat;

public class BetaTransformationInstructionsApplierTest extends RBTest<BetaTransformationInstructionsApplier> {
  private final double DEFAULT_BETA_1 = 1.0;

  @Test
  public void testCaseBeta1() {
    double beta = 1.0;
    double correlation = 0.4;
    double[][] expectedBetaGrid = new double[][] {
        {
            // preferenceForBeta = 0.00
            doubleExplained(1.0,  doubleExplained(1.00, 0.00 * 1.0 + 1.00 * 1.0) * doubleExplained(1.00, 0.00 * 0.4 + 1.00 * 1.0)), // betaPref 0.00, corrPref 0.00
            doubleExplained(0.82, doubleExplained(1.00, 0.00 * 1.0 + 1.00 * 1.0) * doubleExplained(0.82, 0.30 * 0.4 + 0.70 * 1.0)), // betaPref 0.00, corrPref 0.30
            doubleExplained(0.4,  doubleExplained(1.00, 0.00 * 1.0 + 1.00 * 1.0) * doubleExplained(0.40, 1.00 * 0.4 + 0.00 * 1.0))  // betaPref 0.00, corrPref 1.00
        },
        {
            // preferenceForBeta = 0.25
            doubleExplained(1.0,  doubleExplained(1.00, 0.25 * 1.0 + 0.75 * 1.0) * doubleExplained(1.00, 0.00 * 0.4 + 1.00 * 1.0)), // betaPref 0.25, corrPref 0.00
            doubleExplained(0.82, doubleExplained(1.00, 0.25 * 1.0 + 0.75 * 1.0) * doubleExplained(0.82, 0.30 * 0.4 + 0.70 * 1.0)), // betaPref 0.25, corrPref 0.30
            doubleExplained(0.4,  doubleExplained(1.00, 0.25 * 1.0 + 0.75 * 1.0) * doubleExplained(0.40, 1.00 * 0.4 + 0.00 * 1.0))  // betaPref 0.25, corrPref 1.00
        },
        {
            // preferenceForBeta = 1.00
            doubleExplained(1.0,  doubleExplained(1.00, 1.00 * 1.0 + 0.00 * 1.0) * doubleExplained(1.00, 0.00 * 0.4 + 1.00 * 1.0)), // betaPref 1.00, corrPref 0.00
            doubleExplained(0.82, doubleExplained(1.00, 1.00 * 1.0 + 0.00 * 1.0) * doubleExplained(0.82, 0.30 * 0.4 + 0.70 * 1.0)), // betaPref 1.00, corrPref 0.30
            doubleExplained(0.4,  doubleExplained(1.00, 1.00 * 1.0 + 0.00 * 1.0) * doubleExplained(0.40, 1.00 * 0.4 + 0.00 * 1.0))  // betaPref 1.00, corrPref 1.00
        }
    };

    assertThat(
        getResultGrid(beta, correlation),
        doubleArray2DMatcher(
            expectedBetaGrid,
            1e-8));
  }

  @Test
  public void testCaseOfLowBeta() {
    double beta = 0.6;
    double correlation = 0.4;
    double[][] expectedBetaGrid = new double[][] {
        {
            // preferenceForBeta = 0.00
            doubleExplained(1.0,   doubleExplained(1.00, 0.00 * 0.6 + 1.00 * 1.0) * doubleExplained(1.00, 0.00 * 0.4 + 1.00 * 1.0)), // betaPref 0.00, corrPref 0.00
            doubleExplained(0.82,  doubleExplained(1.00, 0.00 * 0.6 + 1.00 * 1.0) * doubleExplained(0.82, 0.30 * 0.4 + 0.70 * 1.0)), // betaPref 0.00, corrPref 0.30
            doubleExplained(0.4,   doubleExplained(1.00, 0.00 * 0.6 + 1.00 * 1.0) * doubleExplained(0.40, 1.00 * 0.4 + 0.00 * 1.0))  // betaPref 0.00, corrPref 1.00
        },
        {
            // preferenceForBeta = 0.25
            doubleExplained(0.9,   doubleExplained(0.90, 0.25 * 0.6 + 0.75 * 1.0) * doubleExplained(1.00, 0.00 * 0.4 + 1.00 * 1.0)), // betaPref 0.25, corrPref 0.00
            doubleExplained(0.738, doubleExplained(0.90, 0.25 * 0.6 + 0.75 * 1.0) * doubleExplained(0.82, 0.30 * 0.4 + 0.70 * 1.0)), // betaPref 0.25, corrPref 0.30
            doubleExplained(0.36,  doubleExplained(0.90, 0.25 * 0.6 + 0.75 * 1.0) * doubleExplained(0.40, 1.00 * 0.4 + 0.00 * 1.0))  // betaPref 0.25, corrPref 1.00
        },
        {
            // preferenceForBeta = 1.00
            doubleExplained(0.6,   doubleExplained(0.60, 1.00 * 0.6 + 0.00 * 1.0) * doubleExplained(1.00, 0.00 * 0.4 + 1.00 * 1.0)), // betaPref 1.00, corrPref 0.00
            doubleExplained(0.492, doubleExplained(0.60, 1.00 * 0.6 + 0.00 * 1.0) * doubleExplained(0.82, 0.30 * 0.4 + 0.70 * 1.0)), // betaPref 1.00, corrPref 0.30
            doubleExplained(0.24,  doubleExplained(0.60, 1.00 * 0.6 + 0.00 * 1.0) * doubleExplained(0.40, 1.00 * 0.4 + 0.00 * 1.0))  // betaPref 1.00, corrPref 1.00
        }
    };

    assertThat(
        getResultGrid(beta, correlation),
        doubleArray2DMatcher(
            expectedBetaGrid,
            1e-8));
  }

  @Test
  public void testCaseOfHighBeta() {
    double beta = 5.0;
    double correlation = 0.4;
    double[][] expectedBetaGrid = new double[][] {
        {
            // preferenceForBeta = 0.00
            doubleExplained(1.0,  doubleExplained(1.00, 0.00 * 5.0 + 1.00 * 1.0) * doubleExplained(1.00, 0.00 * 0.4 + 1.00 * 1.0)), // betaPref 0.00, corrPref 0.00
            doubleExplained(0.82, doubleExplained(1.00, 0.00 * 5.0 + 1.00 * 1.0) * doubleExplained(0.82, 0.30 * 0.4 + 0.70 * 1.0)), // betaPref 0.00, corrPref 0.30
            doubleExplained(0.4,  doubleExplained(1.00, 0.00 * 5.0 + 1.00 * 1.0) * doubleExplained(0.40, 1.00 * 0.4 + 0.00 * 1.0))  // betaPref 0.00, corrPref 1.00
        },
        {
            // preferenceForBeta = 0.25
            doubleExplained(2.0,  doubleExplained(2.00, 0.25 * 5.0 + 0.75 * 1.0) * doubleExplained(1.00, 0.00 * 0.4 + 1.00 * 1.0)), // betaPref 0.25, corrPref 0.00
            doubleExplained(1.64, doubleExplained(2.00, 0.25 * 5.0 + 0.75 * 1.0) * doubleExplained(0.82, 0.30 * 0.4 + 0.70 * 1.0)), // betaPref 0.25, corrPref 0.30
            doubleExplained(0.8,  doubleExplained(2.00, 0.25 * 5.0 + 0.75 * 1.0) * doubleExplained(0.40, 1.00 * 0.4 + 0.00 * 1.0))  // betaPref 0.25, corrPref 1.00
        },
        {
            // preferenceForBeta = 1.00
            doubleExplained(5.0,  doubleExplained(5.00, 1.00 * 5.0 + 0.00 * 1.0) * doubleExplained(1.00, 0.00 * 0.4 + 1.00 * 1.0)), // betaPref 1.00, corrPref 0.00
            doubleExplained(4.1,  doubleExplained(5.00, 1.00 * 5.0 + 0.00 * 1.0) * doubleExplained(0.82, 0.30 * 0.4 + 0.70 * 1.0)), // betaPref 1.00, corrPref 0.30
            doubleExplained(2.0,  doubleExplained(5.00, 1.00 * 5.0 + 0.00 * 1.0) * doubleExplained(0.40, 1.00 * 0.4 + 0.00 * 1.0))  // betaPref 1.00, corrPref 1.00
        }
    };

    assertThat(
        getResultGrid(beta, correlation),
        doubleArray2DMatcher(
            expectedBetaGrid,
            1e-8));
  }

  private double[][] getResultGrid(double beta, double corr) {
    return new double[][] {
        { f(beta, 0.00, corr, 0.0), f(beta, 0.00, corr, 0.3), f(beta, 0.00, corr, 1.0) },
        { f(beta, 0.25, corr, 0.0), f(beta, 0.25, corr, 0.3), f(beta, 0.25, corr, 1.0) },
        { f(beta, 1.00, corr, 0.0), f(beta, 1.00, corr, 0.3), f(beta, 1.00, corr, 1.0) },
    };
  }

  private double f(double actualBeta, double preferenceBeta, double actualCorrelation, double preferenceCorr) {
    return makeTestObject().getTransformedBeta(
        beta(actualBeta, Optional.of(betaBackground(DUMMY_POSITIVE_INTEGER, correlation(actualCorrelation)))),
        betaTransformationInstructionsBuilder()
            .setLabel(DUMMY_LABEL)
            .resetDefaultBeta(BETA_OF_1)
            .setPreferenceForActualBeta(preferSuppliedValueBy(preferenceBeta))
            .setPreferenceForCorrelation(preferSuppliedValueBy(preferenceCorr))
            .build())
        .getValue();
  }

  /** If defaultBeta is 1, and the actual beta for a stock is 0.4, and the preferenceForCorrelation is 0.0, then
   *     preferenceForActualBeta = 0    then beta we will use is 1    = 1 * 1.00 + 0.4 * 0.00
   *     preferenceForActualBeta = 0.25 then beta we will use is 0.85 = 1 * 0.75 + 0.4 * 0.25
   *     preferenceForActualBeta = 1    then beta we will use is 0.4  = 1 * 0.00 + 0.4 * 1.00
   */

  @Test
  public void varyBetaPref_ignoreCorrelation_lowBeta() {
    double dummyCorrelation = 0.12345;
    double lowBeta = 0.4;

    double expectedBetaZeroPref = doubleExplained(1.00, 0.00 * lowBeta + 1.0 * 1.0);
    betaTransformationInstructionsApplier_checker(expectedBetaZeroPref, lowBeta, DEFAULT_BETA_1, ZERO_PREFERENCE_FOR_ACTUAL_BETA, dummyCorrelation, ZERO_PREFERENCE_FOR_CORRELATION);

    double expectedBetaPref025  = doubleExplained(0.85, 0.25 * lowBeta + (1-0.25) * 1);
    betaTransformationInstructionsApplier_checker(expectedBetaPref025,  lowBeta, DEFAULT_BETA_1, preferSuppliedValueBy(0.25),     dummyCorrelation, ZERO_PREFERENCE_FOR_CORRELATION);

    double expectedBetaFullPref = doubleExplained(0.4, 1.00 * lowBeta + 0 * 1);
    betaTransformationInstructionsApplier_checker(expectedBetaFullPref, lowBeta, DEFAULT_BETA_1, FULL_PREFERENCE_FOR_ACTUAL_BETA, dummyCorrelation, ZERO_PREFERENCE_FOR_CORRELATION);
  }

  /** Additionally, we allow for the correlation to decrease the effective beta
   * If the preferenceForActualBeta = 0.75, and the actual beta is 0.4, and the correlation is 0.3, then
   *   interpolatedBeta = (1 * 0.25 + 0.4 * 0.75) = 0.25 + 0.3 = 0.55
   *  preferenceForCorrelation = 0     then  beta we will use is 0.5500 = (1*0.25 + 0.4 * 0.75) * (1.0 * 1.00  + 0.3 * 0.000)
   *  preferenceForCorrelation = 0.333 then  beta we will use is 0.4219 = (1*0.25 + 0.4 * 0.75) * (1.0 * 0.667 + 0.3 * 0.333)
   *  preferenceForCorrelation = 1     then  beta we will use is 0.1650 = (1*0.25 + 0.4 * 0.75) * (1.0 * 0.00  + 0.3 * 1.000)
   */

  @Test
  public void varyCorrelationPreference() {
    double beta = 0.4;
    InterpolationPreference betaPref = preferSuppliedValueBy(0.75);
    double betaPrefVal = betaPref.getRawPreferenceForSuppliedValue().doubleValue();
    double correlation = 0.3;

    double betaInterpolated = doubleExplained(
        0.55,
        betaPref.getRawPreferenceForSuppliedValue().doubleValue() * beta + (1-betaPref.getRawPreferenceForSuppliedValue().doubleValue()) * 1.0);

    double corrReductionPrefZero = doubleExplained(1, 0*0.3+1*1);
    double expectedBetaZeroPref  = corrReductionPrefZero * betaInterpolated;
    betaTransformationInstructionsApplier_checker(expectedBetaZeroPref, beta, DEFAULT_BETA_1, betaPref, correlation, ZERO_PREFERENCE_FOR_CORRELATION);

    double corrReductionPref333 = doubleExplained(0.7666666667, 0.3333333333*0.3+0.6666666667*1.0);
    double expectedBetaPref333  = corrReductionPref333 * betaInterpolated;
    betaTransformationInstructionsApplier_checker(expectedBetaPref333,  beta, DEFAULT_BETA_1, betaPref, correlation, preferSuppliedValueBy(0.3333333333));

    double corrReductionPrefFull = doubleExplained(0.3, 1.0*0.3+0*1.0);
    double expectedBetaPrefFull  = corrReductionPrefFull * betaInterpolated;
    betaTransformationInstructionsApplier_checker(expectedBetaPrefFull, beta, DEFAULT_BETA_1, betaPref, correlation, FULL_PREFERENCE_FOR_CORRELATION);
  }

  @Test
  public void typicalCase() {
    double beta = 0.8;
    double correlation = 0.4;
    InterpolationPreference preferenceForBeta = preferSuppliedValueBy(0.75);
    InterpolationPreference preferenceForCorrelation = preferSuppliedValueBy(0.6);

    double betaInterpolated = doubleExplained(0.85, 0.75 * 0.8 + 0.25 * 1.0);
    double corrReduction    = doubleExplained(0.64, 0.60 * 0.4 + 0.40 * 1.0);
    double expectedTransformedBeta = doubleExplained(0.85 * 0.64, betaInterpolated * corrReduction);
    betaTransformationInstructionsApplier_checker(
        expectedTransformedBeta,
        beta,
        DEFAULT_BETA_1,
        preferenceForBeta,
        correlation,
        preferenceForCorrelation);
  }

  @Test
  public void ignoresBeta_returnsCorrelation() {
    double beta = 0.9;
    for (double correlation : new double[] { -1, -0.99, -0.01, 0, 0.01, 0.99, 1 }) {
      for (double preferenceForCorrelation : new double[] { 0, 0.01, 0.5, 0.99, 1 }) {
        double expectedTransformedBeta = Math.abs(correlation);
        betaTransformationInstructionsApplier_checker(
            expectedTransformedBeta,
            beta,
            DEFAULT_BETA_1,
            ZERO_PREFERENCE_FOR_ACTUAL_BETA,
            correlation,
            FULL_PREFERENCE_FOR_CORRELATION);
      }
    }
  }

  @Test
  public void setsFullPreferenceForBetaAndCorrelation_returnsBetaTimesCorrelation() {
    double beta = 0.9;
    double correlation = 0.6;
    double expectedTransformedBeta = beta * correlation;
    betaTransformationInstructionsApplier_checker(
        expectedTransformedBeta,
        beta,
        DEFAULT_BETA_1,
        FULL_PREFERENCE_FOR_ACTUAL_BETA,
        correlation,
        FULL_PREFERENCE_FOR_CORRELATION);
  }

  @Test
  public void doNotRelyOnBetaOrCorrelation_returnsDefaultBeta() {
    double dummyCorrelation = 0.123;
    for (double beta : new double[] { -1.0, 0.0, 0.6, 1.0, 1.6 }) {
      for (double defaultBeta : new double[] {0.5, 1.0, 2.0}) {
        double expectedTransformedBeta = defaultBeta;
        betaTransformationInstructionsApplier_checker(
            expectedTransformedBeta,
            beta,
            defaultBeta,
            ZERO_PREFERENCE_FOR_ACTUAL_BETA,
            dummyCorrelation,
            ZERO_PREFERENCE_FOR_CORRELATION);
      }
    }
  }

  // Full reliance on actual beta with no correlation preference means that the
  //   transformed beta is just the original beta
  @Test
  public void fullyReliesOnBeta_ignoresCorrelation_returnsActualBeta() {
    double dummyCorrelation = 0.123;
    for (double originalBeta : new double[] { -1.0, 0.0, 0.6, 1.0, 1.6 }) {
      double expectedTransformedBeta = originalBeta;
      betaTransformationInstructionsApplier_checker(
          expectedTransformedBeta,
          originalBeta,
          DEFAULT_BETA_1,
          FULL_PREFERENCE_FOR_ACTUAL_BETA,
          dummyCorrelation,
          ZERO_PREFERENCE_FOR_CORRELATION);
    }
  }

  private void betaTransformationInstructionsApplier_checker(double expectedTransformedBeta,
                                                             double actualBeta,
                                                             double defaultBeta,
                                                             InterpolationPreference preferenceForBeta,
                                                             double actualCorrelation,
                                                             InterpolationPreference preferenceForCorrelation) {
    BetaTransformationInstructionsApplier btia = makeTestObject();
    Beta transformedBeta = btia.getTransformedBeta(
        betaWithBackground(actualBeta,
            betaBackground(DUMMY_POSITIVE_INTEGER, correlation(actualCorrelation))),
        betaTransformationInstructionsBuilder()
            .setLabel(DUMMY_LABEL)
            .resetDefaultBeta(beta(defaultBeta))
            .setPreferenceForActualBeta(preferenceForBeta)
            .setPreferenceForCorrelation(preferenceForCorrelation)
            .build());

    assertThat(transformedBeta,
        betaMatcher(
            betaWithBackground(
                expectedTransformedBeta,
                betaBackground(DUMMY_POSITIVE_INTEGER, correlation(actualCorrelation)))));
  }

  @Override
  protected BetaTransformationInstructionsApplier makeTestObject() {
    return new BetaTransformationInstructionsApplier();
  }

}
