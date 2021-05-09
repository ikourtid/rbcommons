package com.rb.nonbiz.math.vectorspaces;

/**
 * Takes a non-orthonormal vector basis (i.e. a coordinate system where the x and y are not at right angles)
 * and produces an orthonormal one.
 *
 * This could be e.g. ModifiedGramSchmidtOrthonormalizer or SingularValueDecompositionOrthonormalizer,
 * since there are multiple ways to orthonormalize.
 */
public interface Orthonormalizer {

  VectorSpaceOrthonormalBasis orthonormalize(VectorSpaceBasis vectorSpaceBasis);

}
