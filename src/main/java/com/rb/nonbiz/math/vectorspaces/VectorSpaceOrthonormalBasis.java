package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;

/**
 * An orthonormal basis for a vector space of size n, i.e. a set of n unit vectors that are mutually orthogonal.
 * If you're not mathy, this is basically the 3 x-y-z unit vectors for a 3-d space (in the case of 3 dimensions),
 * but it can also be any rotation of these axes; e.g. in 2d, it could be the two vectors
 * (1/sqrt(2), 1/sqrt(2)) and (1/sqrt(2), -1/sqrt(2)), both of which are unit vectors and orthogonal to each other,
 * but not the (1, 0) and (0, 1) vectors. The latter is called the canonical basis (also 'standard basis').
 *
 * We could have stored this as a {@code List<RBVector>}, but a matrix representation is a standard math way to do this,
 * and there's a chance that a matrix form may aid in other ways in the future (e.g. applying transformations).
 *
 * I went back-and-forth between using inheritance (from {@link VectorSpaceBasis} and object composition,
 * and settled for the latter.
 * Even though this is clearly an "is a" relationship, composition is useful because it saves us from having to
 * duplicate the linear independence preconditions.
 */
public class VectorSpaceOrthonormalBasis {

  private final VectorSpaceBasis vectorSpaceBasis;

  private VectorSpaceOrthonormalBasis(VectorSpaceBasis vectorSpaceBasis) {
    this.vectorSpaceBasis = vectorSpaceBasis;
  }

  public static VectorSpaceOrthonormalBasis vectorSpaceOrthonormalBasis(VectorSpaceBasis vectorSpaceBasis) {
    vectorSpaceBasis.basisVectorIterator().forEachRemaining(basisVector ->
        RBPreconditions.checkArgument(
            basisVector.isAlmostUnitVector(1e-8),
            "Not all basis vectors in VectorSpaceOrthonormalBasis were unit vectors: %s",
            vectorSpaceBasis));

    for (int i = 0; i < vectorSpaceBasis.getNumDimensions(); i++) {
      for (int j = i + 1; j < vectorSpaceBasis.getNumDimensions(); j++) {
        RBVector vector1 = vectorSpaceBasis.getBasisVector(matrixColumnIndex(i));
        RBVector vector2 = vectorSpaceBasis.getBasisVector(matrixColumnIndex(j));
        double dotProduct = vector1.dotProduct(vector2);
        RBPreconditions.checkArgument(
            Math.abs(dotProduct) < 1e-8,
            "Vector space basis was not orthonormal: vectors # %s and # %s have a dot product of (almost) zero= %s : %s %s",
            i, j, dotProduct, vector1, vector2);
      }
    }
    return new VectorSpaceOrthonormalBasis(vectorSpaceBasis);
  }

  public VectorSpaceBasis getVectorSpaceBasis() {
    return vectorSpaceBasis;
  }

  public int getNumDimensions() {
    return vectorSpaceBasis.getNumDimensions();
  }

  @Override
  public String toString() {
    return Strings.format("[VSOB %s VSOB]", vectorSpaceBasis);
  }

}
