package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.math.Angle;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.math.Angle.angleWithCosine;

public class RawAngleCalculator {

  /**
   * Calculates the angle between 2 vectors, but only looks at up to maxSize items.
   * Note that it *is* possible that the 2 vectors have different sizes. We sometimes compute the angle between
   * the factor loadings of 2 different eigendecompositions. This is not rigorous because the dimensions may not
   * be the same, either in:
   * a) count of dimensions
   * b) the relative rank of the different dimensions. E.g. even if eigendecompositions 1 and 2 each have 10
   * retained eigenvectors each, it's possible that the 6th dimension in the first is 'exposure to interest rates'
   * whereas it is 'exposure to commodity prices' in the second. Obviously with a PCA there are no named factors,
   * so this is just an informal example.
   */
  public Angle calculateAngle(double[] vector1, double[] vector2, int maxSize) {
    RBPreconditions.checkArgument(
        vector1.length >= maxSize && vector2.length >= maxSize,
        "Vectors 1 and 2 should have size >= %s but they are %s and %s respectively",
        maxSize, vector1.length, vector2.length);
    RBPreconditions.checkArgument(
        vector1.length > 0 && vector2.length > 0,
        "Both vectors must have >0 size but they were %s and %s",
        vector1.length, vector2.length);
    double dotProduct = 0;
    double sumOfSquares1 = 0;
    double sumOfSquares2 = 0;
    for (int i = 0; i < maxSize; i++) {
      double element1 = vector1[i];
      double element2 = vector2[i];
      dotProduct += element1 * element2;
      sumOfSquares1 += element1 * element1;
      sumOfSquares2 += element2 * element2;
    }
    double magnitude1 = Math.sqrt(sumOfSquares1);
    double magnitude2 = Math.sqrt(sumOfSquares2);
    double cosine = dotProduct / (magnitude1 * magnitude2);
    RBPreconditions.checkArgument(
        magnitude1 > 1e-8 && magnitude2 > 1e-8,
        "You cannot compute an angle if a vector has magnitude of 0. I was given %s and %s",
        magnitude1, magnitude2);
    return angleWithCosine(cosine);
  }

}
