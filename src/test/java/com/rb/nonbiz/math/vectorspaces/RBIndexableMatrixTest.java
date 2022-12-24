package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.ArrayIndexMappingTest;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.collections.ArrayIndexMappingTest.arrayIndexMappingMatcher;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.emptySimpleArrayIndexMapping;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrix.rbIndexableMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrix.rbIndexableMatrixWithTrivialColumnMapping;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrix.rbIndexableMatrixWithTrivialRowMapping;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBMatrixTest.rbMatrixMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBColtMatchers.matrixMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBIndexableMatrixTest extends RBTestMatcher<RBIndexableMatrix<String, Integer>> {

  public static <R, C> RBIndexableMatrix<R, C> singletonRBIndexableMatrix(
      R onlyRowKey, C onlyColumnKey, double onlyMatrixElement) {
    return rbIndexableMatrix(
        rbMatrix(new double[][] { { onlyMatrixElement } }),
        simpleArrayIndexMapping(onlyRowKey),
        simpleArrayIndexMapping(onlyColumnKey));
  }

  @Test
  public void disallowsEmptyMatrix() {
    assertIllegalArgumentException( () -> rbIndexableMatrix(
        rbMatrix(new double[][] { { } }),
        emptySimpleArrayIndexMapping(),
        emptySimpleArrayIndexMapping()));
  }

  @Test
  public void matrixDimensionsMustMatchArrayIndexMappingDimensions() {
    BiFunction<ArrayIndexMapping<String>, ArrayIndexMapping<Integer>, RBIndexableMatrix<String, Integer>> maker =
        (rowMapping, columnMapping) ->
            rbIndexableMatrix(
                rbMatrix(new double[][] {
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

  @Test
  public void testToIndexableMatrixWithTrivialRowMapping() {
    assertThat(
        rbIndexableMatrixWithTrivialRowMapping(
            rbMatrix(new double[][] {
                { 1.1, 2.1, 3.1 },
                { 1.2, 2.2, 3.2 } }),
            simpleArrayIndexMapping("a", "b", "c")),
        rbIndexableMatrixMatcher(
            rbIndexableMatrix(
                rbMatrix(new double[][] {
                    { 1.1, 2.1, 3.1 },
                    { 1.2, 2.2, 3.2 } }),
                simpleArrayIndexMapping(matrixRowIndex(0), matrixRowIndex(1)),
                simpleArrayIndexMapping("a", "b", "c"))));
  }

  @Test
  public void testToIndexableMatrixWithTrivialColumnMapping() {
    assertThat(
        rbIndexableMatrixWithTrivialColumnMapping(
            rbMatrix(new double[][] {
                { 1.1, 2.1, 3.1 },
                { 1.2, 2.2, 3.2 } }),
            simpleArrayIndexMapping(77, 88)),
        rbIndexableMatrixMatcher(
            rbIndexableMatrix(
                rbMatrix(new double[][] {
                    { 1.1, 2.1, 3.1 },
                    { 1.2, 2.2, 3.2 } }),
                simpleArrayIndexMapping(77, 88),
                simpleArrayIndexMapping(matrixColumnIndex(0), matrixColumnIndex(1), matrixColumnIndex(2)))));
  }

  @Test
  public void testMultiply_happyPath() {
    assertThat(
        "Matrix multiplication of 2 x 3 by 3 x 2 will give 2 x 2",
        rbIndexableMatrix(
            rbMatrix(new double[][] {
                { 71.1, 71.2, 71.3 },
                { 72.1, 72.2, 72.3 }
            }),
            simpleArrayIndexMapping(false, true),
            simpleArrayIndexMapping("a", "b", "c"))
            .multiply(
                rbIndexableMatrix(
                    rbMatrix(new double[][] {
                        { 81.1, 81.2 },
                        { 82.1, 82.2 },
                        { 83.1, 83.2 }
                    }),
                    simpleArrayIndexMapping("a", "b", "c"),
                    simpleArrayIndexMapping(55, 66))),
        rbIndexableMatrixMatcher(
            rbIndexableMatrix(
                rbMatrix(new double[][] {
                    {
                        doubleExplained(17_536.76, 71.1 * 81.1 + 71.2 * 82.1 + 71.3 * 83.1 ),
                        doubleExplained(17_558.12, 71.1 * 81.2 + 71.2 * 82.2 + 71.3 * 83.2 )
                    },
                    {
                        doubleExplained(17_783.06, 72.1 * 81.1 + 72.2 * 82.1 + 72.3 * 83.1 ),
                        doubleExplained(17_804.72, 72.1 * 81.2 + 72.2 * 82.2 + 72.3 * 83.2 )
                    }
                }),
                simpleArrayIndexMapping(false, true),
                simpleArrayIndexMapping(55, 66))));
  }

  @Test
  public void columnsOnLeftMatrix_mustHaveSameKeysAs_rowsOnRightMatrix() {
    Function<RBIndexableMatrix<String, Integer>, RBIndexableMatrix<Boolean, Integer>> maker = rightMatrix ->
        rbIndexableMatrix(
            rbMatrix(new double[][] {
                { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE },
                { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE }
            }),
            simpleArrayIndexMapping(false, true),
            simpleArrayIndexMapping("a", "b", "c"))
            .multiply(rightMatrix);

    // One less row
    assertIllegalArgumentException( () -> maker.apply(rbIndexableMatrix(
        rbMatrix(new double[][] {
            { DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE }
        }),
        simpleArrayIndexMapping("a", "b"),
        simpleArrayIndexMapping(55, 66))));

    // One more row
    assertIllegalArgumentException( () -> maker.apply(rbIndexableMatrix(
        rbMatrix(new double[][] {
            { DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE }
        }),
        simpleArrayIndexMapping("a", "b", "c", "d"),
        simpleArrayIndexMapping(55, 66))));

    RBIndexableMatrix<Boolean, Integer> doesNotThrow;
    // A 3 x 2 matrix, just like previous test
    doesNotThrow = maker.apply(rbIndexableMatrix(
        rbMatrix(new double[][] {
            { DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE }
        }),
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(55, 66)));

    // one more column; irrelevant
    doesNotThrow = maker.apply(rbIndexableMatrix(
        rbMatrix(new double[][] {
            { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE }
        }),
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(55, 66, 77)));

    // one less column; irrelevant
    doesNotThrow = maker.apply(rbIndexableMatrix(
        rbMatrix(new double[][] {
            { DUMMY_DOUBLE },
            { DUMMY_DOUBLE },
            { DUMMY_DOUBLE }
        }),
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(55)));

    // Finally, if the keys are shuffled, or just different, then even if the sizes are proper,
    // we should have an exception.
    rbSetOf(
        simpleArrayIndexMapping("X", "b", "c"),
        simpleArrayIndexMapping("a", "X", "c"),
        simpleArrayIndexMapping("a", "b", "X"),
        simpleArrayIndexMapping("a", "c", "b"))
        .forEach(rowMappingOfRightMatrix ->
            assertIllegalArgumentException( () -> maker.apply(rbIndexableMatrix(
                rbMatrix(new double[][] {
                    { DUMMY_DOUBLE },
                    { DUMMY_DOUBLE },
                    { DUMMY_DOUBLE }
                }),
                rowMappingOfRightMatrix,
                simpleArrayIndexMapping(55)))));
  }

  @Test
  public void testInverse_generalCase() {
    // General inverse. See https://www.wolframalpha.com/input?i=inverse%7B+%7B1%2C+2%7D%2C+%7B3%2C+4%7D%7D
    assertThat(
        rbIndexableMatrix(
            rbMatrix(new double[][] {
                { 1.0, 2.0 },
                { 3.0, 4.0 }
            }),
            simpleArrayIndexMapping("a", "b"),
            simpleArrayIndexMapping(77, 88))
            .inverse(),
        rbIndexableMatrixMatcher(
            rbIndexableMatrix(
                rbMatrix(new double[][] {
                    { -2.0,  1.0 },
                    {  1.5, -0.5 }
                }),
                simpleArrayIndexMapping(77, 88),
                simpleArrayIndexMapping("a", "b"))));
  }

  @Test
  public void testTranspose() {
    assertThat(
        rbIndexableMatrix(
            rbMatrix(new double[][] {
                { 71.1, 71.2, 71.3 },
                { 72.1, 72.2, 72.3 }
            }),
            simpleArrayIndexMapping(false, true),
            simpleArrayIndexMapping("a", "b", "c"))
            .transpose(),
        rbIndexableMatrixMatcher(
            rbIndexableMatrix(
                rbMatrix(new double[][] {
                    { 71.1, 72.1 },
                    { 71.2, 72.2 },
                    { 71.3, 72.3 }
                }),
                simpleArrayIndexMapping("a", "b", "c"),
                simpleArrayIndexMapping(false, true))));
  }

  @Test
  public void testAsRbMatrix() {
    RBMatrix rawMatrix = rbMatrix(new double[][] {
        { 71.1, 71.2, 71.3 },
        { 72.1, 72.2, 72.3 }
    });
    // Make sure the content of asRmMatrix matches rawMatrix.
    assertThat(
        rbIndexableMatrix(
            rawMatrix,
            simpleArrayIndexMapping("1", "2"),
            simpleArrayIndexMapping("a", "b", "c")).asRbMatrix(),
        rbMatrixMatcher(rawMatrix));
  }

  @Override
  public RBIndexableMatrix<String, Integer> makeTrivialObject() {
    return singletonRBIndexableMatrix("", 0, 0.0);
  }

  @Override
  public RBIndexableMatrix<String, Integer> makeNontrivialObject() {
    // Normally, we'd create something like testRBIndexableMatrixWithSeed, and use it here,
    // but this class is generic on the row and column key type, so we can't really create such a test-only
    // constructor that's general enough.
    return rbIndexableMatrix(
        rbMatrix(new double[][] {
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
        rbMatrix(new double[][] {
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
        match(v -> v.asRbMatrix(),       f -> rbMatrixMatcher(f, epsilon)),
        // in theory we could be using matchers for R and C here, but in practice R and C have to implement a
        // non-trivial (i.e. not just a pointer comparison) equals / hashCode in order to appear inside an
        // ArrayIndexMapping, so it's fine to just use typeSafeEqualTo here.
        match(v -> v.getRowMapping(),    f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))),
        match(v -> v.getColumnMapping(), f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

}
