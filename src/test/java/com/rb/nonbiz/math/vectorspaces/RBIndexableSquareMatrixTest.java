package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.emptySimpleArrayIndexMapping;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableSquareMatrix.rbIndexableSquareMatrix;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBColtMatchers.matrixMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;

public class RBIndexableSquareMatrixTest extends RBTestMatcher<RBIndexableSquareMatrix<String>> {

  @Test
  public void disallowsEmptyMatrix() {
    assertIllegalArgumentException( () -> rbIndexableSquareMatrix(
        new DenseDoubleMatrix2D(new double[][] { { } }),
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

    assertIllegalArgumentException( () -> maker.apply(emptySimpleArrayIndexMapping()));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping("a")));
    RBIndexableSquareMatrix<String> doesNotThrow = maker.apply(simpleArrayIndexMapping("a", "b"));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping("a", "b", "c")));
  }

  @Override
  public RBIndexableSquareMatrix<String> makeTrivialObject() {
    return rbIndexableSquareMatrix(
        new DenseDoubleMatrix2D(new double[][] { { 0.0 } }),
        simpleArrayIndexMapping(""));
  }

  @Override
  public RBIndexableSquareMatrix<String> makeNontrivialObject() {
    // Normally, we'd create something like testRBIndexableSquareMatrixWithSeed, and use it here,
    // but this class is generic on the row and column key type, so we can't really create such a test-only
    // constructor that's general enough.
    return rbIndexableSquareMatrix(
        new DenseDoubleMatrix2D(new double[][] {
            { -1.1,  2.2 },
            { -3.3,  4.4 }
        }),
        simpleArrayIndexMapping("a", "b"));
  }

  @Override
  public RBIndexableSquareMatrix<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbIndexableSquareMatrix(
        new DenseDoubleMatrix2D(new double[][] {
            { -1.1 + e,  2.2 + e },
            { -3.3 + e,  4.4 + e }
        }),
        simpleArrayIndexMapping("a", "b"));
  }

  @Override
  protected boolean willMatch(RBIndexableSquareMatrix<String> expected, RBIndexableSquareMatrix<String> actual) {
    return rbIndexableSquareMatrixMatcher(expected).matches(actual);
  }

  public static <K> TypeSafeMatcher<RBIndexableSquareMatrix<K>> rbIndexableSquareMatrixMatcher(
      RBIndexableSquareMatrix<K> expected) {
    return rbIndexableSquareMatrixMatcher(expected, 1e-8);
  }

  public static <K> TypeSafeMatcher<RBIndexableSquareMatrix<K>> rbIndexableSquareMatrixMatcher(
      RBIndexableSquareMatrix<K> expected, double epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawMatrixUnsafe(), f -> matrixMatcher(f, epsilon)));
  }

}
