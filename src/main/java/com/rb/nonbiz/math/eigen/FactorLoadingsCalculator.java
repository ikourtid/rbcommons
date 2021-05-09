package com.rb.nonbiz.math.eigen;

import cern.colt.matrix.DoubleMatrix2D;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.ImmutableIndexableArray1D;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.immutableIndexableArray1D;
import static com.rb.nonbiz.math.eigen.FactorLoadings.factorLoadings;

public class FactorLoadingsCalculator {

  /** This operation can be probably accomplished with some elegant matrix multiplication,
   * but this formulation should be clearer.
   */
  public ImmutableIndexableArray1D<InstrumentId, FactorLoadings> calculateFactorLoadingsByInstrument(
      DoubleMatrix2D correlationMatrix,
      List<Eigenpair> retainedEigenpairsDescending,
      ArrayIndexMapping<InstrumentId> arrayIndexMapping) {
    int numFactors = retainedEigenpairsDescending.size();
    int numInstruments = arrayIndexMapping.size();
    FactorLoadings[] factorLoadingsArray = new FactorLoadings[numInstruments];
    for (int instrumentIndex = 0; instrumentIndex < numInstruments; instrumentIndex++) {
      double[] thisInstrumentLoadings = new double[numFactors];
      for (int factorIndex = 0; factorIndex < numFactors; factorIndex++) {
        // The factor loading of an instrument is computed as a dot product.
        // If you think of the (retained) eigenvectors as the basis of the new transformed space, it makes sense;
        // e.g. the projection of any vector onto the x axis is its x coordinate.
        Eigenvector factor = retainedEigenpairsDescending.get(factorIndex).getEigenvector();
        RBPreconditions.checkArgument(factor.size() == numInstruments);
        double dotProduct = 0;
        for (int k = 0; k < factor.size(); k++) {
          dotProduct += factor.getQuick(k) * correlationMatrix.getQuick(instrumentIndex, k);
        }
        thisInstrumentLoadings[factorIndex] = dotProduct;
      }
      factorLoadingsArray[instrumentIndex] = factorLoadings(thisInstrumentLoadings);
    }
    return immutableIndexableArray1D(arrayIndexMapping, factorLoadingsArray);
  }

}
