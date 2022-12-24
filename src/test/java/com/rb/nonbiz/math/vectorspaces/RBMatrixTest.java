package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.testutils.Epsilons;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndexTest.matrixColumnIndexMatcher;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndexTest.matrixRowIndexMatcher;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrix.rbIndexableMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBIndexableMatrixTest.rbIndexableMatrixMatcher;
import static com.rb.nonbiz.math.vectorspaces.RBMatrix.rbMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrix.diagonalRBSquareMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBSquareMatrix.identityRBSquareMatrix;
import static com.rb.nonbiz.math.vectorspaces.RBVectorTest.rbVector;
import static com.rb.nonbiz.math.vectorspaces.RBVectorTest.rbVectorMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertIndexOutOfBoundsException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.Epsilons.emptyEpsilons;
import static com.rb.nonbiz.testutils.Epsilons.useEpsilonEverywhere;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBMatrixTest extends RBTestMatcher<RBMatrix> {

  public static RBMatrix singletonRBMatrix(double onlyValue) {
    return rbMatrix(new double[][] { { onlyValue } });
  }

  public static RBMatrix rbDiagonalMatrix2by2(double a11, double a22) {
    return rbMatrix2by2(
        a11, 0.0,
        0.0, a22);
  }

  public static RBMatrix rbMatrix2by2(double a11, double a12, double a21, double a22) {
    return rbMatrix(new double[][] {
        { a11, a12 },
        { a21, a22 }});
  }

  public static RBMatrix rbDiagonalMatrix3by3(double a11, double a22, double a33) {
    return rbMatrix3by3(
        a11,  0,   0,
        0,  a22,   0,
        0,    0, a33);
  }

  public static RBMatrix rbMatrix3by3(
      double a11, double a12, double a13,
      double a21, double a22, double a23,
      double a31, double a32, double a33) {
    return rbMatrix(new double[][] {
        { a11, a12, a13 },
        { a21, a22, a23 },
        { a31, a32, a33 }});
  }

  @Test
  public void emptyMatrix_throws() {
    assertIllegalArgumentException( () -> rbMatrix(new double[][] { {} }));
  }

  @Test
  public void testTransformCopy() {
    assertThat(
        rbMatrix(
            new double[][] {
                { 5.7, 5.8, 5.9 },
                { 6.7, 6.8, 6.9 }
            })
            .transformCopy(
                (matrixRowIndex, matrixColumnIndex, existingValue) ->
                    existingValue + 10 * (1 + matrixRowIndex.intValue()) + 100 * (1 + matrixColumnIndex.intValue())),
        rbMatrixMatcher(
            rbMatrix(new double[][] {
                {
                    doubleExplained(115.7, 5.7 + 10 * (1 + 0) + 100 * (1 + 0)),
                    doubleExplained(215.8, 5.8 + 10 * (1 + 0) + 100 * (1 + 1)),
                    doubleExplained(315.9, 5.9 + 10 * (1 + 0) + 100 * (1 + 2))
                },
                {
                    doubleExplained(126.7, 6.7 + 10 * (1 + 1) + 100 * (1 + 0)),
                    doubleExplained(226.8, 6.8 + 10 * (1 + 1) + 100 * (1 + 1)),
                    doubleExplained(326.9, 6.9 + 10 * (1 + 1) + 100 * (1 + 2))
                }
            })));
  }

  @Test
  public void matrixMultiplyByIdentity_noChange() {
    RBMatrix matrix2by3 = rbMatrix(new double[][] {
        { 1.1, 2.1, 3.1 },
        { 1.2, 2.2, 3.2 } });
    assertThat(
        matrix2by3.multiply(identityRBSquareMatrix(3)),
        rbMatrixMatcher(matrix2by3));
    assertThat(
        identityRBSquareMatrix(2).multiply(matrix2by3),
        rbMatrixMatcher(matrix2by3));

    // multiplying an identity matrix by itself results in the same matrix
    assertThat(
        identityRBSquareMatrix(3).multiply(identityRBSquareMatrix(3)),
        rbMatrixMatcher(identityRBSquareMatrix(3)));
  }

  @Test
  public void testMatrixMultiplyByOtherMatrix() {
    RBMatrix matrix1 = rbMatrix2by2(
        1.0, 2.0,
        3.0, 4.0);
    RBMatrix matrix2 = rbMatrix2by2(
        5.0, 6.0,
        7.0, 8.0);
    assertThat(
        matrix1.multiply(matrix2),
        rbMatrixMatcher(rbMatrix(new double[][] {
            { doubleExplained(19, 1 * 5 + 2 * 7), doubleExplained(22, 1 * 6 + 2 * 8) },
            { doubleExplained(43, 3 * 5 + 4 * 7), doubleExplained(50, 3 * 6 + 4 * 8) }})));
  }

  @Test
  public void testMatrixMultiplyByVector() {
    assertThat(
        rbMatrix2by2(
            1.0, 2.0,
            3.0, 4.0)
            .multiply(rbVector(
                5.0,
                7.0)),
        rbVectorMatcher(rbVector(
            doubleExplained(19, 1 * 5 + 2 * 7),
            doubleExplained(43, 3 * 5 + 4 * 7))));
  }

  @Test
  public void testCalculateDeterminant() {
    BiConsumer<Double, RBMatrix> asserter = (expectedResult, matrix) ->
        assertEquals(expectedResult, matrix.calculateDeterminant(), 1e-8);

    asserter.accept(1.0, identityRBSquareMatrix(2));
    asserter.accept(1.0, identityRBSquareMatrix(3));

    // recall that Det({{a, b}, {c, d}}) = ad - bc
    asserter.accept(doubleExplained(-2, 1 * 4 - 2 * 3), rbMatrix2by2(1, 2, 3, 4));
    asserter.accept(doubleExplained( 7, 5 * 2 - 1 * 3), rbMatrix2by2(5, 1, 3, 2));

    // an empty row or column will make the determinant zero
    asserter.accept(0.0, rbMatrix2by2(1, 1, 0, 0));
    asserter.accept(0.0, rbMatrix2by2(0, 0, 1, 1));
    asserter.accept(0.0, rbMatrix2by2(1, 0, 1, 0));
    asserter.accept(0.0, rbMatrix2by2(0, 1, 0, 1));

    // one row or column being equal to another, or a multiple of another, will cause the determinant to be zero
    asserter.accept(0.0, rbMatrix2by2(1,  2,  1,  2));
    asserter.accept(0.0, rbMatrix2by2(1,  2, 10, 20));
    asserter.accept(0.0, rbMatrix2by2(1,  1,  2,  2));
    asserter.accept(0.0, rbMatrix2by2(1, 10,  2, 20));

    // Finally, non-square matrices should throw. Trying a 1 x 2 and then a 2 x 1 matrix.
    assertIllegalArgumentException( () -> rbMatrix(new double[][] { { 1.1, 2.2 }}).calculateDeterminant());
    assertIllegalArgumentException( () -> rbMatrix(new double[][] { { 1.1 }, { 2.2 }}).calculateDeterminant());
  }

  @Test
  public void matrixMultiplyDimensionsMismatched_throws() {
    RBMatrix matrix = rbMatrix2by2(
        1.0, 2.0,
        3.0, 4.0);
    assertIllegalArgumentException( () -> matrix.multiply(identityRBSquareMatrix(3)));
    RBMatrix doesNotThrow = matrix.multiply(identityRBSquareMatrix(2));
  }

  @Test
  public void testGetColumn() {
    RBMatrix matrix = rbMatrix2by2(
        1.0, 2.0,
        3.0, 4.0);

    assertThat(
        matrix.getColumnVector(matrixColumnIndex(0)),
        rbVectorMatcher(rbVector(1.0, 3.0)));

    assertThat(
        matrix.getColumnVector(matrixColumnIndex(1)),
        rbVectorMatcher(rbVector(2.0, 4.0)));

    // can't request the third column of a 2 x 2 matrix
    assertIllegalArgumentException( () -> matrix.getColumnVector(matrixColumnIndex(2)));
  }

  @Test
  public void testTranspose() {
    // the transposition of a 1x1 matrix is itself
    assertThat(
        singletonRBMatrix(123.45),
        rbMatrixMatcher(singletonRBMatrix(123.45)));

    // the transposition of an identity matrix is itself
    assertThat(
        identityRBSquareMatrix(3).transpose(),
        rbMatrixMatcher(identityRBSquareMatrix(3)));

    assertThat(
        rbMatrix2by2(
            1.0, 2.0,
            3.0, 4.0).transpose(),
        rbMatrixMatcher(
            rbMatrix2by2(
                1.0, 3.0,
                2.0, 4.0)));

    // one can transpose non-square matrices
    assertThat(
        rbMatrix(new double[][] {
            { 1.1, 2.1, 3.1 },
            { 1.2, 2.2, 3.2 } }).transpose(),
        rbMatrixMatcher(
            rbMatrix(new double[][] {
                { 1.1, 1.2 },
                { 2.1, 2.2 },
                { 3.1, 3.2 } })));
  }

  @Test
  public void testInverse() {
    BiConsumer<RBMatrix, RBMatrix> asserter = (input, expected) ->
        assertThat(
            input.inverse(),
            rbMatrixMatcher(expected));

    // Simplest case: inverting the 1x1 matrix {1} just returns itself.
    asserter.accept(
        singletonRBMatrix(1.0),
        singletonRBMatrix(1.0));

    // The inverse of the identity matrix is the same identity matrix.
    asserter.accept(
        identityRBSquareMatrix(3),
        identityRBSquareMatrix(3));

    // The inverse of a diagonal matrix is another diagonal matrix, whose elements are reciprocals of the original's.
    asserter.accept(
        diagonalRBSquareMatrix(rbVector(4.0,  5.0,  0.1)),
        diagonalRBSquareMatrix(rbVector(0.25, 0.2, 10.0)));

    // The inverse of a rotation matrix is the inverse rotation.
    asserter.accept(
        rbMatrix2by2(
            0,  1,
            -1, 0),
        rbMatrix2by2(
            0, -1,
            1,  0));

    // General inverse. See https://www.wolframalpha.com/input?i=inverse%7B+%7B1%2C+2%7D%2C+%7B3%2C+4%7D%7D
    asserter.accept(
        rbMatrix2by2(
            1.0, 2.0,
            3.0, 4.0),
        rbMatrix2by2(
            -2.0, 1.0,
            1.5, -0.5));

    // Can't take the inverse of a singular matrix.
    assertIllegalArgumentException( () -> rbMatrix2by2(
        7.0, 70.0,
        8.0, 80.0)
        .inverse());

    // Warning: you CAN take the inverse of a nearly-singular matrix. The result will probably
    // have large delicately-balancing elements of opposite signs. However, it (dangerously) won't fail.
    // This is a case of "garbage in, garbage quietly out".
    // See https://www.wolframalpha.com/input?i=inverse%7B+%7B1%2C+0.99%7D%2C+%7B0.99%2C+1%7D%7D
    assertThat(
        rbMatrix2by2(
            7.001, 70.0,   // as above, but with first column entries increased by 0.001
            8.001, 80.0)
            .inverse(),
        rbMatrixMatcher(
            rbMatrix2by2(
                8_000.0, -7_000.0,
                - 800.1,    700.1),
            // need a larger epsilon here than in asserter()
            1e-6));
  }

  @Test
  public void testDiagonalMatrix() {
    assertThat(
        diagonalRBSquareMatrix(rbVector(55, 66, 77)),
        rbMatrixMatcher(
            rbMatrix3by3(
                55,  0,  0,
                0,  66,  0,
                0,   0, 77)));
    assertThat(
        diagonalRBSquareMatrix(rbVector(1, 1, 1)),
        rbMatrixMatcher(
            identityRBSquareMatrix(3)));
  }

  @Test
  public void testToIndexableMatrix() {
    assertThat(
        rbMatrix(new double[][] {
            { 1.1, 2.1, 3.1 },
            { 1.2, 2.2, 3.2 } })
            .toIndexableMatrix(simpleArrayIndexMapping(77, 88), simpleArrayIndexMapping("a", "b", "c")),
        rbIndexableMatrixMatcher(
            rbIndexableMatrix(
                rbMatrix(new double[][] {
                    { 1.1, 2.1, 3.1 },
                    { 1.2, 2.2, 3.2 } }),
                simpleArrayIndexMapping(77, 88),
                simpleArrayIndexMapping("a", "b", "c"))));
  }

  @Test
  public void testToIndexableMatrixWithTrivialRowMapping() {
    assertThat(
        rbMatrix(new double[][] {
            { 1.1, 2.1, 3.1 },
            { 1.2, 2.2, 3.2 } })
            .toIndexableMatrixWithTrivialRowMapping(simpleArrayIndexMapping("a", "b", "c")),
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
        rbMatrix(new double[][] {
            { 1.1, 2.1, 3.1 },
            { 1.2, 2.2, 3.2 } })
            .toIndexableMatrixWithTrivialColumnMapping(simpleArrayIndexMapping(77, 88)),
        rbIndexableMatrixMatcher(
            rbIndexableMatrix(
                rbMatrix(new double[][] {
                    { 1.1, 2.1, 3.1 },
                    { 1.2, 2.2, 3.2 } }),
                simpleArrayIndexMapping(77, 88),
                simpleArrayIndexMapping(matrixColumnIndex(0), matrixColumnIndex(1), matrixColumnIndex(2)))));
  }

  @Test
  public void testIsSquare() {
    // 1x1 matrices are square
    assertTrue(singletonRBMatrix(DUMMY_DOUBLE).isSquare());

    // identity matrices are square
    assertTrue(identityRBSquareMatrix(3).isSquare());

    // diagonal matrices are square
    assertTrue(diagonalRBSquareMatrix(rbVector(DUMMY_DOUBLE, DUMMY_DOUBLE)).isSquare());

    // a 2x2 matrix is square
    assertTrue(rbMatrix(new double[][] {
        { DUMMY_DOUBLE, DUMMY_DOUBLE },
        { DUMMY_DOUBLE, DUMMY_DOUBLE }})
        .isSquare());

    // a 3x2 matrix is not square
    assertFalse(rbMatrix(new double[][] {
        { DUMMY_DOUBLE, DUMMY_DOUBLE },
        { DUMMY_DOUBLE, DUMMY_DOUBLE },
        { DUMMY_DOUBLE, DUMMY_DOUBLE }})
        .isSquare());

    // a 2x3 matrix is not square
    assertFalse(rbMatrix(new double[][] {
        { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE },
        { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE }})
        .isSquare());
  }

  @Test
  public void testCopyPart() {
    RBMatrix matrixToCopyPartOf = rbMatrix3by3(
        1, 2, 3,
        4, 5, 6,
        7, 8, 9);
    // Copy whole matrix
    assertThat(
        matrixToCopyPartOf.copyPart(
            closedRange(   matrixRowIndex(0),    matrixRowIndex(2)),
            closedRange(matrixColumnIndex(0), matrixColumnIndex(2))),
        rbMatrixMatcher(matrixToCopyPartOf));
    // Copy some 2x2 pieces.
    assertThat(
        matrixToCopyPartOf.copyPart(
            closedRange(   matrixRowIndex(0),    matrixRowIndex(1)),
            closedRange(matrixColumnIndex(0), matrixColumnIndex(1))),
        rbMatrixMatcher(rbMatrix2by2( 1, 2,
            4, 5)));
    assertThat(
        matrixToCopyPartOf.copyPart(
            closedRange(   matrixRowIndex(1),    matrixRowIndex(2)),
            closedRange(matrixColumnIndex(1), matrixColumnIndex(2))),
        rbMatrixMatcher(rbMatrix2by2(
            5, 6,
            8, 9)));
    // Copy some 1x1 pieces
    assertThat(
        matrixToCopyPartOf.copyPart(
            closedRange(   matrixRowIndex(1),    matrixRowIndex(1)),
            closedRange(matrixColumnIndex(1), matrixColumnIndex(1))),
        rbMatrixMatcher(singletonRBMatrix(5)));
    assertThat(
        matrixToCopyPartOf.copyPart(
            closedRange(   matrixRowIndex(1),    matrixRowIndex(1)),
            closedRange(matrixColumnIndex(2), matrixColumnIndex(2))),
        rbMatrixMatcher(singletonRBMatrix(6)));
    assertThat(
        matrixToCopyPartOf.copyPart(
            closedRange(   matrixRowIndex(2),    matrixRowIndex(2)),
            closedRange(matrixColumnIndex(0), matrixColumnIndex(0))),
        rbMatrixMatcher(singletonRBMatrix(7)));
    // Copy tall and wide slices
    assertThat(
        matrixToCopyPartOf.copyPart(
            closedRange(   matrixRowIndex(1),    matrixRowIndex(2)),
            closedRange(matrixColumnIndex(1), matrixColumnIndex(1))),
        rbMatrixMatcher(rbMatrix(new double[][] { { 5 }, { 8 } })));
    assertThat(
        matrixToCopyPartOf.copyPart(
            closedRange(   matrixRowIndex(2),    matrixRowIndex(2)),
            closedRange(matrixColumnIndex(0), matrixColumnIndex(2))),
        rbMatrixMatcher(rbMatrix(new double[][] { { 7, 8, 9 } })));
    assertThat(
        matrixToCopyPartOf.copyPart(
            closedRange(   matrixRowIndex(0),    matrixRowIndex(2)),
            closedRange(matrixColumnIndex(0), matrixColumnIndex(1))),
        rbMatrixMatcher(rbMatrix(new double[][] { { 1, 2 }, { 4, 5 }, { 7, 8 } })));
    // Raise an exception when the slice is too large
    assertIndexOutOfBoundsException( () -> matrixToCopyPartOf.copyPart(
        closedRange( matrixRowIndex(0),        matrixRowIndex(4)),
        closedRange( matrixColumnIndex(0),  matrixColumnIndex(4))));
  }

  @Test
  public void testCopyTopLeftPart() {
    // Using decimals so it's clear in the code below what's a row / column index and what's a matrix element.
    RBMatrix originalMatrix = rbMatrix3by3(
        1.1, 2.2, 3.3,
        4.4, 5.5, 6.6,
        7.7, 8.8, 9.9);
    TriConsumer<Integer, Integer, RBMatrix> asserter = (lastRowIndexInt, lastColumnIndexInt, expectedResult) ->
        assertThat(
            originalMatrix.copyTopLeftPart(
                matrixRowIndex(lastRowIndexInt),
                matrixColumnIndex(lastColumnIndexInt)),
            rbMatrixMatcher(
                expectedResult));
    asserter.accept(0, 0, singletonRBMatrix(1.1));
    asserter.accept(1, 1, rbMatrix2by2(
        1.1, 2.2,
        4.4, 5.5));
    asserter.accept(2, 2, originalMatrix);

    asserter.accept(0, 1, rbMatrix(new double[][] {
        { 1.1, 2.2 }
    }));
    asserter.accept(1, 2, rbMatrix(new double[][] {
        { 1.1, 2.2, 3.3 },
        { 4.4, 5.5, 6.6 }
    }));

    MatrixRowIndex       validRowIndex  = matrixRowIndex(1);
    MatrixRowIndex     invalidRowIndex   = matrixRowIndex(3);
    MatrixColumnIndex   validColumnIndex = matrixColumnIndex(0);
    MatrixColumnIndex invalidColumnIndex = matrixColumnIndex(3);
    assertIndexOutOfBoundsException( () -> originalMatrix.copyTopLeftPart(invalidRowIndex, invalidColumnIndex));
    assertIndexOutOfBoundsException( () -> originalMatrix.copyTopLeftPart(  validRowIndex, invalidColumnIndex));
    assertIndexOutOfBoundsException( () -> originalMatrix.copyTopLeftPart(invalidRowIndex,  validColumnIndex));
    RBMatrix doesNotThrow = originalMatrix.copyTopLeftPart(validRowIndex, validColumnIndex);
  }

  @Test
  public void testGetOnlyElementOrThrow() {
    assertEquals(1.1, singletonRBMatrix(1.1).getOnlyElementOrThrow(), 1e-8);
    rbSetOf(
        rbMatrix(new double[][] {
            { DUMMY_DOUBLE, DUMMY_DOUBLE }
        }),
        rbMatrix(new double[][] {
            { DUMMY_DOUBLE },
            { DUMMY_DOUBLE }
        }),
        rbMatrix(new double[][] {
            { DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE }
        }))
        .forEach(v -> assertIllegalArgumentException( () -> v.getOnlyElementOrThrow()));
  }

  @Test
  public void testLastValidRowAndColumn() {
    TriConsumer<Integer, Integer, RBMatrix> asserter = (expectedLastRowInt, expectedLastColumnInt, rbMatrix) -> {
      assertThat(
          rbMatrix.getLastRowIndex(),
          matrixRowIndexMatcher(
              matrixRowIndex(expectedLastRowInt)));
      assertThat(
          rbMatrix.getLastColumnIndex(),
          matrixColumnIndexMatcher(
              matrixColumnIndex(expectedLastColumnInt)));
    };

    asserter.accept(0, 0, singletonRBMatrix(DUMMY_DOUBLE));
    asserter.accept(1, 2, rbMatrix(new double[][] {
        { 1.1, 2.2, 3.3 },
        { 4.4, 5.5, 6.6 }
    }));
  }

  @Test
  public void testMatrixRowStream() {
    BiConsumer<RBMatrix, List<MatrixRowIndex>> asserter = (rbMatrix, expectedResult) ->
        assertThat(
            rbMatrix.matrixRowIndexStream().collect(Collectors.toList()),
            orderedListMatcher(
                expectedResult,
                f -> matrixRowIndexMatcher(f)));

    asserter.accept(
        singletonRBMatrix(DUMMY_DOUBLE),
        singletonList(matrixRowIndex(0)));
    asserter.accept(
        rbMatrix(new double[][] {
            { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE }
        }),
        ImmutableList.of(matrixRowIndex(0), matrixRowIndex(1)));
  }

  @Test
  public void testMatrixColumnStream() {
    BiConsumer<RBMatrix, List<MatrixColumnIndex>> asserter = (rbMatrix, expectedResult) ->
        assertThat(
            rbMatrix.matrixColumnIndexStream().collect(Collectors.toList()),
            orderedListMatcher(
                expectedResult,
                f -> matrixColumnIndexMatcher(f)));

    asserter.accept(
        singletonRBMatrix(DUMMY_DOUBLE),
        singletonList(matrixColumnIndex(0)));
    asserter.accept(
        rbMatrix(new double[][] {
            { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE },
            { DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE }
        }),
        ImmutableList.of(matrixColumnIndex(0), matrixColumnIndex(1), matrixColumnIndex(2)));
  }

  @Test
  public void testGet() {
    BiFunction<Integer, Integer, Double> getter = (rowAsInt, columnAsInt) ->
        rbMatrix(new double[][] {
            { 0.0, 0.1, 0.2 },
            { 1.0, 1.1, 1.2 }
        })
            .get(matrixRowIndex(rowAsInt), matrixColumnIndex(columnAsInt));

    assertEquals(0.0, getter.apply(0, 0), 1e-8);
    assertEquals(0.1, getter.apply(0, 1), 1e-8);
    assertEquals(0.2, getter.apply(0, 2), 1e-8);

    assertEquals(1.0, getter.apply(1, 0), 1e-8);
    assertEquals(1.1, getter.apply(1, 1), 1e-8);
    assertEquals(1.2, getter.apply(1, 2), 1e-8);

    assertIndexOutOfBoundsException( () -> getter.apply(0, 3));
    assertIndexOutOfBoundsException( () -> getter.apply(2, 0));
    assertIndexOutOfBoundsException( () -> getter.apply(2, 3));
  }

  @Test
  public void testGetRowAsVector() {
    IntFunction<RBVector> getter = row -> rbMatrix(new double[][] {
        { 1.1, 2.2, 3.3 },
        { 4.4, 5.5, 6.6 } })
        .getRowAsVector(matrixRowIndex(row));
    assertThat(getter.apply(0), rbVectorMatcher(rbVector(1.1, 2.2, 3.3)));
    assertThat(getter.apply(1), rbVectorMatcher(rbVector(4.4, 5.5, 6.6)));
    assertIndexOutOfBoundsException( () -> getter.apply(2));
  }

  @Test
  public void testGetColumnAsVector() {
    IntFunction<RBVector> getter = column -> rbMatrix(new double[][] {
        { 1.1, 2.2, 3.3 },
        { 4.4, 5.5, 6.6 } })
        .getColumnAsVector(matrixColumnIndex(column));
    assertThat(getter.apply(0), rbVectorMatcher(rbVector(1.1, 4.4)));
    assertThat(getter.apply(1), rbVectorMatcher(rbVector(2.2, 5.5)));
    assertThat(getter.apply(2), rbVectorMatcher(rbVector(3.3, 6.6)));
    assertIndexOutOfBoundsException( () -> getter.apply(3));
  }

  @Override
  public RBMatrix makeTrivialObject() {
    return singletonRBMatrix(0);
  }

  @Override
  public RBMatrix makeNontrivialObject() {
    return rbMatrix(new double[][] {
        { 1.1, 2.1, 3.1 },
        { 4.1, 5.1, 6.1 }});
  }

  @Override
  public RBMatrix makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbMatrix(new double[][] {
        { 1.1 + e, 2.1 + e, 3.1 + e },
        { 4.1 + e, 5.1 + e, 6.1 + e }});
  }

  @Override
  protected boolean willMatch(RBMatrix expected, RBMatrix actual) {
    return rbMatrixMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RBMatrix> rbMatrixMatcher(RBMatrix expected) {
    return rbMatrixMatcher(expected, emptyEpsilons());
  }

  public static TypeSafeMatcher<RBMatrix> rbMatrixMatcher(RBMatrix expected, double epsilon) {
    return rbMatrixMatcher(expected, useEpsilonEverywhere(epsilon));
  }

  // This matcher is very verbose; we could have just exposed the RBMatrix's DoubleMatrix2D and used
  // matrixMatcher. However, exposing that would be dangerous, because it would also allow other classes in
  // the same package to also access the DoubleMatrix2D, which is a 3rd party class that's not immutable.
  // So this matcher's verbosity is a small price to pay compared to that risk.
  public static TypeSafeMatcher<RBMatrix> rbMatrixMatcher(RBMatrix expected, Epsilons e) {
    return makeMatcher(expected, actual -> {
      if (expected.getNumRows() != actual.getNumRows()) {
        return false;
      }
      if (expected.getNumColumns() != actual.getNumColumns()) {
        return false;
      }
      // For the index streams, we could have used either 'expected' or 'actual'
      return expected.matrixRowIndexStream().allMatch(matrixRowIndex ->
          expected.matrixColumnIndexStream().allMatch(matrixColumnIndex -> {
            double valueInExpected = expected.get(matrixRowIndex, matrixColumnIndex);
            double valueInActual   = actual.get(matrixRowIndex, matrixColumnIndex);
            return Math.abs(
                valueInExpected - valueInActual) < e.get(RBMatrix.class);
          }));
    });
  }

}
