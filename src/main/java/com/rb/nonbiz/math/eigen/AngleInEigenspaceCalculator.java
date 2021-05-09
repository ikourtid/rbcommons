package com.rb.nonbiz.math.eigen;

import com.google.inject.Inject;
import com.rb.nonbiz.math.Angle;
import com.rb.nonbiz.text.Strings;

/**
 * See RawAngleCalculator for more.
 */
public class AngleInEigenspaceCalculator {

  @Inject RawAngleCalculator rawAngleCalculator;

  public Angle calculateAngleInSameEigenspace(
      FactorLoadings referenceLoadings, FactorLoadings comparedLoadings) {
    if (referenceLoadings.size() != comparedLoadings.size()) {
      throw new IllegalArgumentException(Strings.format(
          "reference loadings had %s items but compared loadings had %s",
          comparedLoadings.size(), referenceLoadings.size()));
    }
    return rawAngleCalculator.calculateAngle(
        referenceLoadings.getLoadings(),
        comparedLoadings.getLoadings(),
        referenceLoadings.getLoadings().length);
  }

  public Angle calculateAngleInDifferentEigenspaces(
      FactorLoadings referenceLoadings, FactorLoadings comparedLoadings, int maxDimensionsToLookAt) {
    return rawAngleCalculator.calculateAngle(
        referenceLoadings.getLoadings(),
        comparedLoadings.getLoadings(),
        Integer.min(maxDimensionsToLookAt, Integer.min(referenceLoadings.size(), comparedLoadings.size())));
  }

}
