package com.rb.nonbiz.math.eigen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.rb.biz.investing.strategy.optbased.di.RealizedVolatilities;
import com.rb.biz.marketdata.index.AdditionalCalculatedFactorLoadings;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.HasRbSet;
import com.rb.nonbiz.collections.ImmutableIndexableArray1D;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.collections.RBSet.newRBSet;

/**
 * There's already a protobuf code-generated class that captures the notion of an eigendecomposition.
 * This class is a developer-friendlier equivalent.
 *
 * This is generic for any investable. It is conceivable that we can have an eigendecomposition of asset classes
 * as well. In fact, this was the case until April 2017. However, I changed all eigendecompositions in the system
 * to be instrument ID eigendecompositions. I used to create {@code Eigendecomposition<AssetClass>} by
 * faking one by assigning the 'most preferred' instrument of every instrument class to be the equivalent.
 * However, it's clearer if we don't use {@code Eigendecomposition<AssetClass>} and instead use an
 * {@code Eigendecomposition<InstrumentId>} directly, and convert 'on the spot'.
 *
 * MUTABLE STATE HACK:
 *
 * Data classes in the codebase are almost always immutable. There is a small exception we are making for Eigendecomposition.
 *
 * We needed to store additional calculated factor loadings for fake ETFs that track a fake index,
 * which we need in order to support direct indexing. Ideally, there should have been a wrapper such as
 *
 * <pre>
 * {@code
 * public class EigendecompositionWithAdditionalCalculatedFactorLoadings<K extends Investable> {
 *
 *   private final Eigendecomposition<K> eigendecomposition;
 *   private final AdditionalCalculatedFactorLoadings<K> additionalCalculatedFactorLoadings;
 *   ...
 *  }
 * }
 * </pre>
 *  but that would require changing a ton of code that currently (Oct 2018) only takes in an Eigendecomposition.
 *  Moreover, we would then also need an equivalent to the EigendecompositionProvider, but for this new class,
 *  which would complicate things further.
 *
 * I decided that the least bad solution to all this is to have the Eigendecomposition be mutable
 * ONLY with respect to a single field (named mutableAdditionalCalculatedFactorLoadings), and give
 * mutableAdditionalCalculatedFactorLoadings a value once during backtest setup (currently in SingleBacktestSingleDayRunner).
 *
 * It is not clear yet if we would ever need to compute AdditionalCalculatedFactorLoadings in production,
 * since there will not be a fake index with fake ETFs that track it. One somewhat far-fetched scenario is if we
 * knew the constituents of an ETF with their specific weights, and wanted to model its factor loadings as a
 * weighted combination of the factor loadings of its constituents - vs. having the ETF itself participate in the
 * eigendecomposition generation, which will give it its own factor loadings. This could be useful (way, way later)
 * to model a newly issued ETF that tracks an index of stocks. In that case, the new ETF wouldn't have any price
 * history, so we wouldn't be able to have it be part of the eigendecomposition generation.
 */
public class Eigendecomposition<K extends Investable> implements HasRbSet<K> {

  private final String humanDescription;
  private final EigenExplainabilityFraction eigenExplainabilityFraction;
  private final double sumOfAllEigenvaluesIncludingSkipped;
  private final List<Eigenpair> eigenpairsInDescendingEigenvalues;
  private final ImmutableIndexableArray1D<K, FactorLoadings> factorLoadingsByKey;
  private final MultiItemQualityOfReturns<K> multiItemQualityOfReturns;
  private final RealizedVolatilities<K> realizedVolatilities;
  private Optional<AdditionalCalculatedFactorLoadings<K>> mutableAdditionalCalculatedFactorLoadings;

  private Eigendecomposition(String humanDescription,
                             EigenExplainabilityFraction eigenExplainabilityFraction,
                             double sumOfAllEigenvaluesIncludingSkipped,
                             List<Eigenpair> eigenpairsInDescendingEigenvalues,
                             ImmutableIndexableArray1D<K, FactorLoadings> factorLoadingsByKey,
                             MultiItemQualityOfReturns<K> multiItemQualityOfReturns,
                             RealizedVolatilities<K> realizedVolatilities) {
    this.humanDescription = humanDescription;
    this.eigenExplainabilityFraction = eigenExplainabilityFraction;
    this.sumOfAllEigenvaluesIncludingSkipped = sumOfAllEigenvaluesIncludingSkipped;
    this.eigenpairsInDescendingEigenvalues = eigenpairsInDescendingEigenvalues;
    this.factorLoadingsByKey = factorLoadingsByKey;
    this.multiItemQualityOfReturns = multiItemQualityOfReturns;
    this.realizedVolatilities = realizedVolatilities;
    this.mutableAdditionalCalculatedFactorLoadings = Optional.empty();
  }

  public static Eigendecomposition<InstrumentId> instrumentIdEigendecomposition(
      String humanDescription,
      EigenExplainabilityFraction eigenExplainabilityFraction,
      double sumOfAllEigenvaluesIncludingSkipped,
      List<Eigenpair> eigenpairsInDescendingEigenvalues,
      ImmutableIndexableArray1D<InstrumentId, FactorLoadings> factorLoadingsByKey,
      MultiItemQualityOfReturns<InstrumentId> multiItemQualityOfReturns,
      RealizedVolatilities<InstrumentId> realizedVolatilities) {
    return untypedEigendecomposition(true, humanDescription, eigenExplainabilityFraction,
        sumOfAllEigenvaluesIncludingSkipped, eigenpairsInDescendingEigenvalues, factorLoadingsByKey,
        multiItemQualityOfReturns, realizedVolatilities);
  }

  /**
   * This is called 'withPossibleFilter' because its caller may have filtered out some instruments before calling this.
   * If that is the case, we need to relax some assertion. Otherwise the code is the same.
   */
  public static Eigendecomposition<InstrumentId> instrumentIdEigendecompositionWithPossibleFilter(
      String humanDescription,
      EigenExplainabilityFraction eigenExplainabilityFraction,
      double sumOfAllEigenvaluesIncludingSkipped,
      List<Eigenpair> eigenpairsInDescendingEigenvalues,
      ImmutableIndexableArray1D<InstrumentId, FactorLoadings> factorLoadingsByKey,
      MultiItemQualityOfReturns<InstrumentId> multiItemQualityOfReturns,
      RealizedVolatilities<InstrumentId> realizedVolatilities) {
    return untypedEigendecomposition(false, humanDescription, eigenExplainabilityFraction,
        sumOfAllEigenvaluesIncludingSkipped, eigenpairsInDescendingEigenvalues, factorLoadingsByKey,
        multiItemQualityOfReturns, realizedVolatilities);
  }

  /**
   * This is private so we can force all callers to get an Eigendecomposition&lt;InstrumentId&gt; with
   * the other public static constructors. This way, we know there will be no Eigendecomposition of any other type
   * in the system, until we change it (if ever).
   *
   * <p> Currently (June 2018) we only have Eigendecomposition&lt;InstrumentId&gt;, but previously we also had
   * Eigendecomposition&lt;InstrumentClass&gt; in the system. We are leaving this function as type-generic, in case
   * we later re-enable the ability to have Eigendecomposition&lt;InstrumentClass&gt;. </p>
   */
  private static <K extends Investable> Eigendecomposition<K> untypedEigendecomposition(
      boolean assertEigenvectorSize,
      String humanDescription,
      EigenExplainabilityFraction eigenExplainabilityFraction,
      double sumOfAllEigenvaluesIncludingSkipped,
      List<Eigenpair> eigenpairsInDescendingEigenvalues,
      ImmutableIndexableArray1D<K, FactorLoadings> factorLoadingsByKey,
      MultiItemQualityOfReturns<K> multiItemQualityOfReturns,
      RealizedVolatilities<K> realizedVolatilities) {
    int numRetainedEigenpairs = eigenpairsInDescendingEigenvalues.size();
    RBPreconditions.checkArgument(
        numRetainedEigenpairs > 0,
        "We must have at least 1 eigenpair!");
    int numKeys = factorLoadingsByKey.size();
    RBPreconditions.checkArgument(
        numKeys > 0,
        "We must have at least 1 key!");
    double previousEigenvalue = Double.POSITIVE_INFINITY;

    double sumOfRetainedEigenvalues = 0;
    for (Eigenpair eigenpair : eigenpairsInDescendingEigenvalues) {
      if (assertEigenvectorSize) {
        RBPreconditions.checkArgument(
            eigenpair.getEigenvector().size() == numKeys,
            "All eigenvectors must have numKeys= %s elements, but found one with %s",
            numKeys, eigenpair.getEigenvector().size());
      }
      double thisEigenvalue = eigenpair.getEigenvalue().doubleValue();
      sumOfRetainedEigenvalues += thisEigenvalue;
      RBPreconditions.checkArgument(
          thisEigenvalue < previousEigenvalue,
          "Eigenvalues must be descending, but %s > %s (previous)",
          thisEigenvalue, previousEigenvalue);
      previousEigenvalue = thisEigenvalue;
      // in the case of eigenpairUnsafe, we store an empty eigenvector (hack for performance).
      // So only do the check for the other cases.
      int eigenvectorSize = eigenpairsInDescendingEigenvalues.get(0).getEigenvector().size();
      if (eigenvectorSize > 0) {
        RBPreconditions.checkArgument(
            eigenpair.getEigenvector().size() == eigenvectorSize,
            "All eigenvectors must have %s coordinates but I found one with %s",
            eigenvectorSize, eigenpair.getEigenvector().size());
      }
    }
    for (int i = 0; i < factorLoadingsByKey.size(); i++) {
      RBPreconditions.checkArgument(
          factorLoadingsByKey.getByIndex(i).size() == numRetainedEigenpairs,
          "Factor loadings should have 1 element for each of the %s retained eigenpairs, but found one with %s",
          numRetainedEigenpairs, factorLoadingsByKey.getByIndex(i).size());
    }
    RBPreconditions.checkArgument(
        !eigenExplainabilityFraction.isZero(),
        "Eigenvector coverage fraction can't be 0 (no explainability)");
    RBPreconditions.checkArgument(
        !eigenExplainabilityFraction.isOne(),
        "Eigenvector coverage fraction can't be 1 (100% explainability - you should be reducing dimensionality here)");
    // This could have been a <= instead of a < , but in practice we should be dropping at least some eigenpairs
    RBPreconditions.checkArgument(
        sumOfRetainedEigenvalues < sumOfAllEigenvaluesIncludingSkipped,
        "retained eigenvalues sum %s must be a subset (PROPER, to be safe) of all (incl. skipped) eigenvalues %s ; %s ; %s eigenpairs",
        String.format("%20.10f", sumOfRetainedEigenvalues),
        String.format("%20.10f", sumOfAllEigenvaluesIncludingSkipped),
        eigenExplainabilityFraction, eigenpairsInDescendingEigenvalues.size());

    RBSimilarityPreconditions.checkAllSame(
        ImmutableList.of(
            factorLoadingsByKey.getKeysRBSet(),
            newRBSet(multiItemQualityOfReturns.getQualityOfReturnsMap().keySet()),
            realizedVolatilities.getDailyizedStandardDeviations().getKeysRBSet()),
        "keys with loadings, quality of returns, and standard deviations do not match");
    return new Eigendecomposition<>(
        humanDescription, eigenExplainabilityFraction, sumOfAllEigenvaluesIncludingSkipped,
        eigenpairsInDescendingEigenvalues, factorLoadingsByKey,
        multiItemQualityOfReturns, realizedVolatilities);
  }

  public String getHumanDescription() {
    return humanDescription;
  }

  public EigenExplainabilityFraction getEigenExplainabilityFraction() {
    return eigenExplainabilityFraction;
  }

  public double getSumOfAllEigenvaluesIncludingSkipped() {
    return sumOfAllEigenvaluesIncludingSkipped;
  }

  public List<Eigenpair> getEigenpairsInDescendingEigenvalues() {
    return eigenpairsInDescendingEigenvalues;
  }

  public int getNumKeys() {
    return factorLoadingsByKey.size();
  }

  public List<K> getKeys() {
    return factorLoadingsByKey.getKeys();
  }

  public boolean containsKey(K key) {
    return factorLoadingsByKey.containsKey(key) ||
        transformOptional(getMutableAdditionalCalculatedFactorLoadings(), v -> v.getRawMap().containsKey(key))
            .orElse(false);
  }

  @Override
  public RBSet<K> getRbSet() {
    return newRBSet(getKeys());
  }

  public FactorLoadings getFactorLoadings(int index) {
    return factorLoadingsByKey.getByIndex(index);
  }

  public K getKey(int index) {
    return factorLoadingsByKey.getKey(index);
  }

  // See comment labeled 'MUTABLE STATE HACK' on top of Eigendecomposition.java for an explanation
  public void setMutableAdditionalCalculatedFactorLoadings(AdditionalCalculatedFactorLoadings<K> additionalCalculatedFactorLoadings) {
    RBSimilarityPreconditions.checkBothSame(
        additionalCalculatedFactorLoadings.getSharedFactorLoadingsSize(),
        getNumRetainedEigenpairs(),
        "The additional factor loadings should match that of this eigendecomposition");
    this.mutableAdditionalCalculatedFactorLoadings = Optional.of(additionalCalculatedFactorLoadings);
  }

  @VisibleForTesting // this is here for the matcher only; do not use this
  Optional<AdditionalCalculatedFactorLoadings<K>> getMutableAdditionalCalculatedFactorLoadings() {
    return mutableAdditionalCalculatedFactorLoadings;
  }

  // See comment labeled 'MUTABLE STATE HACK' on top of Eigendecomposition.java for an explanation
  public FactorLoadings getFactorLoadings(K key) {
    if (factorLoadingsByKey.containsKey(key)) {
      return factorLoadingsByKey.get(key);
    }
    AdditionalCalculatedFactorLoadings<K> additionalCalculatedFactorLoadings = getOrThrow(mutableAdditionalCalculatedFactorLoadings,
        "No factor loadings for %s in the eigendecomposition, and there's no AdditionalCalculatedFactorLoadings",
        key);
    return additionalCalculatedFactorLoadings.getRawMap().getOrThrow(key,
        "Could not find factor loadings for %s either in the eigendecomposition, " +
        "or in the additional calculated loadings for '%s'",
        key, getHumanDescription());
  }

  public int getNumRetainedEigenpairs() {
    return eigenpairsInDescendingEigenvalues.size();
  }

  public MultiItemQualityOfReturns<K> getMultiItemQualityOfReturns() {
    return multiItemQualityOfReturns;
  }

  @VisibleForTesting
  ImmutableIndexableArray1D<K, FactorLoadings> getRawFactorLoadingsByKey() {
    return factorLoadingsByKey;
  }

  public RealizedVolatilities<K> getRealizedVolatilities() {
    return realizedVolatilities;
  }

  @Override
  public String toString() {
    return Strings.format("%s %s %s %s %s %s %s",
        this.getHumanDescription(),
        this.eigenExplainabilityFraction,
        this.sumOfAllEigenvaluesIncludingSkipped,
        Joiner.on(" , ").join(eigenpairsInDescendingEigenvalues),
        factorLoadingsByKey,
        multiItemQualityOfReturns,
        realizedVolatilities);
  }

}
