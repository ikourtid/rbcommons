package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.types.UnitFraction;

import java.math.BigDecimal;

/**
 * A PCA is mainly about dimensionality reduction. The eigendecomposition files only store the eigenvectors
 * whose corresponding eigenvalues sum to up to (say) 70% of the total. The idea is that a few eigenvectors with
 * large eigenvalues will suffice, and there will be lots of little ones that make up for the remaining 30% (example only).
 *
 * If an Eigendecomposition object has an EigenExplainabilityFraction of 0.7, it means that we only retained eigenvectors
 * that explain 70% of the variance in the underlying data.
 *
 * This is a more typesafe way of expressing that concept than a plain UnitFraction.
 *
 * The name is not great, but we already use the prefix 'eigen' to mean 'related to the eigendecomposition-based
 * risk model', so it will do. In the protobuf, we call this 'eigenvector coverage fraction', which in some ways
 * is a better name (and in some other ways isn't!) but that's too long of a name to use for a PreciseValue subclass.
 */
public class EigenExplainabilityFraction extends UnitFraction {

  protected EigenExplainabilityFraction(BigDecimal value) {
    super(value);
  }

  public static EigenExplainabilityFraction eigenExplainabilityFraction(BigDecimal value) {
    return new EigenExplainabilityFraction(value);
  }

  public static EigenExplainabilityFraction eigenExplainabilityFraction(double value) {
    return eigenExplainabilityFraction(BigDecimal.valueOf(value));
  }

}
