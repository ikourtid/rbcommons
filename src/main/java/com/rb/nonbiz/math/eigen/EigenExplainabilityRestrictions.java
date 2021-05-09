package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Our eigendecomposition-based risk model will typically have many eigenvectors, enough that they explain up to
 * e.g. 70% of the variance in the data.
 * However, we may decide to restrict our eigen subobjective to only use the N eigenvectors with the N biggest
 * eigenvalues, or the N eigenvectors with the largest eigenvalues that explain at least x% of the total variance
 * in the data. We may do this for:
 * * performance, as it reduces the size of the linear program while arguably not reducing accuracy much
 * * avoiding overfitting, as perhaps the smaller principal components are less stable.
 */
public class EigenExplainabilityRestrictions {

  private final OptionalInt maxNumEigenvectors;
  private final Optional<EigenExplainabilityFraction> maxEigenExplainabilityFraction;

  private EigenExplainabilityRestrictions(
      OptionalInt maxNumEigenvectors, Optional<EigenExplainabilityFraction> maxEigenExplainabilityFraction) {
    this.maxNumEigenvectors = maxNumEigenvectors;
    this.maxEigenExplainabilityFraction = maxEigenExplainabilityFraction;
  }

  public static EigenExplainabilityRestrictions restrictBothNumEigenvectorsAndExplainability(
      int maxNumEigenvectors, EigenExplainabilityFraction maxEigenExplainabilityFraction) {
    RBPreconditions.checkArgument(
        maxNumEigenvectors >= 1,
        "If you specify a maximum number of eigenvectors, it must be >= 1; was %s",
        maxNumEigenvectors);
    return new EigenExplainabilityRestrictions(OptionalInt.of(maxNumEigenvectors), Optional.of(maxEigenExplainabilityFraction));
  }

  public static EigenExplainabilityRestrictions restrictNumEigenvectors(int maxNumEigenvectors) {
    RBPreconditions.checkArgument(
        maxNumEigenvectors >= 1,
        "If you specify a maximum number of eigenvectors, it must be >= 1; was %s",
        maxNumEigenvectors);
    return new EigenExplainabilityRestrictions(OptionalInt.of(maxNumEigenvectors), Optional.empty());
  }

  public static EigenExplainabilityRestrictions restrictEigenExplainability(EigenExplainabilityFraction maxEigenExplainabilityFraction) {
    return new EigenExplainabilityRestrictions(OptionalInt.empty(), Optional.of(maxEigenExplainabilityFraction));
  }

  public static EigenExplainabilityRestrictions emptyEigenExplainabilityRestrictions() {
    return new EigenExplainabilityRestrictions(OptionalInt.empty(), Optional.empty());
  }

  public OptionalInt getMaxNumEigenvectors() {
    return maxNumEigenvectors;
  }

  public Optional<EigenExplainabilityFraction> getMaxEigenExplainabilityFraction() {
    return maxEigenExplainabilityFraction;
  }

  @Override
  public String toString() {
    return Strings.format("[EER %s %s EER]", maxNumEigenvectors, maxEigenExplainabilityFraction);
  }

}
