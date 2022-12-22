package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.testutils.Epsilons;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.DoubleFunction;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.emptySimpleArrayIndexMapping;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableSquareMatrix.rbIndexableSquareMatrix;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBColtMatchers.matrixMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Epsilons.emptyEpsilons;
import static com.rb.nonbiz.testutils.Epsilons.useEpsilonEverywhere;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBIndexableSquareMatrixTest extends RBTestMatcher<RBIndexableSquareMatrix<String>> {

  public static <K> RBIndexableSquareMatrix<K> singletonRBIndexableSquareMatrix(double onlyValue, K onlyKey) {
    return rbIndexableSquareMatrix(
        new DenseDoubleMatrix2D(new double[][] { { onlyValue } }),
        simpleArrayIndexMapping(onlyKey));
  }

  @SafeVarargs
  public static <K> RBIndexableSquareMatrix<K> testRBIndexableSquareMatrix(
      double[][] rawMatrix,
      K first,
      K second,
      K... rest) {
    return rbIndexableSquareMatrix(
        new DenseDoubleMatrix2D(rawMatrix),
        simpleArrayIndexMapping(concatenateFirstSecondAndRest(first, second, rest)));
  }

  @Test
  public void disallowsEmptyMatrix() {
    assertIllegalArgumentException(() -> rbIndexableSquareMatrix(
        new DenseDoubleMatrix2D(new double[][] { {} }),
        emptySimpleArrayIndexMapping()));
  }

  @Test
  public void matrixDimensionsMustMatchArrayIndexMappingDimensions() {
    Function<ArrayIndexMapping<String>, RBIndexableSquareMatrix<String>> maker = mappingForRowsAndColumns ->
        rbIndexableSquareMatrix(
            new DenseDoubleMatrix2D(new double[][] {
                { DUMMY_DOUBLE, DUMMY_DOUBLE },
                { DUMMY_DOUBLE, DUMMY_DOUBLE }
            }),
            mappingForRowsAndColumns);

    assertIllegalArgumentException(() -> maker.apply(emptySimpleArrayIndexMapping()));
    assertIllegalArgumentException(() -> maker.apply(simpleArrayIndexMapping("a")));
    RBIndexableSquareMatrix<String> doesNotThrow = maker.apply(simpleArrayIndexMapping("a", "b"));
    assertIllegalArgumentException(() -> maker.apply(simpleArrayIndexMapping("a", "b", "c")));
  }

  @Test
  public void testIsSymmetric_varyEpsilon() {
    DoubleFunction<Boolean> maker = epsilon ->
        testRBIndexableSquareMatrix(
            new double[][] {
                { 1.1, 2.2, 3.3 + epsilon },
                { 2.2, 4.4, 5.5 + epsilon },
                { 3.3, 5.5, 6.6 + epsilon } // this is on the diagonal, so it doesn't matter, but let's keep it
            },
            "a", "b", "c")
            .isSymmetric(1e-8);

    assertTrue(
        "exactly equal",
        maker.apply(0));

    assertTrue(
        "off by a tiny epsilon; still symmetric",
        maker.apply(1e-9));

    assertFalse(
        "off by an amount more than epsilon; not symmetric",
        maker.apply(1e-7));
  }

  @Override
  public RBIndexableSquareMatrix<String> makeTrivialObject() {
    return singletonRBIndexableSquareMatrix(0.0, "");
  }

  @Override
  public RBIndexableSquareMatrix<String> makeNontrivialObject() {
    // Normally, we'd create something like testRBIndexableSquareMatrixWithSeed, and use it here,
    // but this class is generic on the row and column key type, so we can't really create such a test-only
    // constructor that's general enough.
    return testRBIndexableSquareMatrix(
        new double[][] {
            { -1.1, 2.2 },
            { -3.3, 4.4 }
        },
        "a", "b");
  }

  @Override
  public RBIndexableSquareMatrix<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return testRBIndexableSquareMatrix(
        new double[][] {
            { -1.1 + e, 2.2 + e },
            { -3.3 + e, 4.4 + e }
        },
        "a", "b");
  }

  @Override
  protected boolean willMatch(RBIndexableSquareMatrix<String> expected, RBIndexableSquareMatrix<String> actual) {
    return rbIndexableSquareMatrixMatcher(expected).matches(actual);
  }

  public static <K> TypeSafeMatcher<RBIndexableSquareMatrix<K>> rbIndexableSquareMatrixMatcher(
      RBIndexableSquareMatrix<K> expected) {
    return rbIndexableSquareMatrixMatcher(expected, emptyEpsilons());
  }

  public static <K> TypeSafeMatcher<RBIndexableSquareMatrix<K>> rbIndexableSquareMatrixMatcher(
      RBIndexableSquareMatrix<K> expected, double epsilon) {
    return rbIndexableSquareMatrixMatcher(expected, useEpsilonEverywhere(epsilon));
  }

  public static <K> TypeSafeMatcher<RBIndexableSquareMatrix<K>> rbIndexableSquareMatrixMatcher(
      RBIndexableSquareMatrix<K> expected, Epsilons e) {
    return makeMatcher(expected,
        match(v -> v.getRawMatrixUnsafe(), f -> matrixMatcher(f, e)));
  }

}
