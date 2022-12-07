package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.SimpleArrayIndexMapping;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiFunction;

import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.emptySimpleArrayIndexMapping;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrix.rbIndexableMatrix;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBColtMatchers.matrixMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;

public class RBIndexableMatrixTest extends RBTestMatcher<RBIndexableMatrix<String, Integer>> {

  @Test
  public void disallowsEmptyMatrix() {
    assertIllegalArgumentException( () -> rbIndexableMatrix(
        new DenseDoubleMatrix2D(new double[][] { { } }),
        emptySimpleArrayIndexMapping(),
        emptySimpleArrayIndexMapping()));
  }

  @Test
  public void matrixDimensionsMustMatchArrayIndexMappingDimensions() {
    BiFunction<ArrayIndexMapping<String>, ArrayIndexMapping<Integer>, RBIndexableMatrix<String, Integer>> maker =
        (rowMapping, columnMapping) ->
            rbIndexableMatrix(
                new DenseDoubleMatrix2D(new double[][] {
                    { DUMMY_DOUBLE, DUMMY_DOUBLE },
                    { DUMMY_DOUBLE, DUMMY_DOUBLE },
                    { DUMMY_DOUBLE, DUMMY_DOUBLE }
                }),
                rowMapping,
                columnMapping);

    ArrayIndexMapping<String> validRowMapping = simpleArrayIndexMapping("a", "b", "c");
    ArrayIndexMapping<Integer> validColumnMapping = simpleArrayIndexMapping(77, 88);

    RBIndexableMatrix<String, Integer> doesNotThrow = maker.apply(validRowMapping, validColumnMapping);

    // Trying too many & too few (respectively) rows in the row mapping
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping("a", "b", "c", "d"), validColumnMapping));
    assertIllegalArgumentException( () -> maker.apply(simpleArrayIndexMapping("a", "b"),           validColumnMapping));

    // Trying too many & too few (respectively) rows in the row mapping
    assertIllegalArgumentException( () -> maker.apply(validRowMapping, simpleArrayIndexMapping(77, 88, 99)));
    assertIllegalArgumentException( () -> maker.apply(validRowMapping, simpleArrayIndexMapping(77)));
  }

  @Override
  public RBIndexableMatrix<String, Integer> makeTrivialObject() {
    return rbIndexableMatrix(
        new DenseDoubleMatrix2D(new double[][] { { 0.0 } }),
        simpleArrayIndexMapping(""),
        simpleArrayIndexMapping(0));
  }

  @Override
  public RBIndexableMatrix<String, Integer> makeNontrivialObject() {
    // Normally, we'd create something like testRBIndexableMatrixWithSeed, and use it here,
    // but this class is generic on the row and column key type, so we can't really create such a test-only
    // constructor that's general enough.
    return rbIndexableMatrix(
        new DenseDoubleMatrix2D(new double[][] {
            { -1.1,  2.2 },
            { -3.3,  4.4 },
            {  5.5, -6.6 }
        }),
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(77, 88));
  }

  @Override
  public RBIndexableMatrix<String, Integer> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbIndexableMatrix(
        new DenseDoubleMatrix2D(new double[][] {
            { -1.1 + e,  2.2 + e },
            { -3.3 + e,  4.4 + e },
            {  5.5 + e, -6.6 + e }
        }),
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(77, 88));
  }

  @Override
  protected boolean willMatch(RBIndexableMatrix<String, Integer> expected, RBIndexableMatrix<String, Integer> actual) {
    return rbIndexableMatrixMatcher(expected).matches(actual);
  }

  public static <R, C> TypeSafeMatcher<RBIndexableMatrix<R, C>> rbIndexableMatrixMatcher(
      RBIndexableMatrix<R, C> expected) {
    return rbIndexableMatrixMatcher(expected, 1e-8);
  }

  public static <R, C> TypeSafeMatcher<RBIndexableMatrix<R, C>> rbIndexableMatrixMatcher(
      RBIndexableMatrix<R, C> expected, double epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawMatrixUnsafe(), f -> matrixMatcher(f, epsilon)));
  }

}
