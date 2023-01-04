package com.rb.nonbiz.math.vectorspaces;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.MatcherEpsilons;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRestDoubles;
import static com.rb.nonbiz.math.vectorspaces.RBVector.zeroRBVectorWithDimension;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrows;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.MatcherEpsilons.emptyMatcherEpsilons;
import static com.rb.nonbiz.testutils.MatcherEpsilons.useEpsilonInAllMatchers;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RBVectorTest extends RBTestMatcher<RBVector> {

  public static RBVector rbVector(double first, double second, double ... rest) {
    return RBVector.rbVector(new DenseDoubleMatrix1D(concatenateFirstSecondAndRestDoubles(first, second, rest).toArray()));
  }

  public static RBVector singletonRBVector(double onlyValue) {
    return RBVector.rbVector(new DenseDoubleMatrix1D(new double[] { onlyValue }));
  }

  public static RBVector rbVectorWithSizeAndSharedValue(int size, double sharedValue) {
    assertTrue(size >= 1);
    double[] rawArray = new double[size];
    for (int i = 0; i < size; i++) {
      rawArray[i] = sharedValue;
    }
    return RBVector.rbVector(rawArray);
  }

  @Test
  public void emptyVector_throws() {
    assertIllegalArgumentException( () -> RBVector.rbVector(new DenseDoubleMatrix1D(new double[] { })));
  }

  @Test
  public void testIsAlmostUnitVector_1d() {
    rbSetOf(0.0, 1e-9, 1e-7, 1 - 1e-7, 1 + 1e-7, 999.0)
        .forEach(x -> {
          assertFalse(singletonRBVector(x).isAlmostUnitVector(DEFAULT_EPSILON_1e_8));
          assertFalse(singletonRBVector(-1 * x).isAlmostUnitVector(DEFAULT_EPSILON_1e_8));
        });

    rbSetOf(1 - 1e-9, 1.0, 1 + 1e-9)
        .forEach(x -> assertTrue(singletonRBVector(x).isAlmostUnitVector(DEFAULT_EPSILON_1e_8)));
  }

  @Test
  public void assertIsAlmostUnitVector_2d() {
    assertTrue( rbVector(1 / Math.sqrt(2),        1 / Math.sqrt(2)       ).isAlmostUnitVector(DEFAULT_EPSILON_1e_8));
    assertTrue( rbVector(1 / Math.sqrt(2) + 1e-9, 1 / Math.sqrt(2) + 1e-9).isAlmostUnitVector(DEFAULT_EPSILON_1e_8));
    assertFalse(rbVector(1 / Math.sqrt(2) + 1e-7, 1 / Math.sqrt(2) + 1e-7).isAlmostUnitVector(DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testGetMagnitude_1d() {
    rbSetOf(0.0, 1e-9, 1e-7, 0.5, 1.0, 2.0, 100.0)
        .forEach(x -> {
          assertEquals(x, singletonRBVector(x).calculateMagnitude(), 1e-9);
          assertEquals(x, singletonRBVector(-x).calculateMagnitude(), 1e-9);
        });
  }

  @Test
  public void zeroVector_multipliedByAnyScalar_returnsZeroVectorOfSameDimension() {
    rbSetOf(singletonRBVector(0), rbVector(0, 0), rbVector(0, 0, 0))
        .forEach(zeroVector -> rbSetOf(-999.0, -2.0, -1.0, -0.5, 0.0, 0.5, 1.0, 2.0, 999.0)
            .forEach(multiplier -> assertThat(
                zeroVector.multiplyByScalar(multiplier),
                rbVectorMatcher(zeroVector))));
  }

  @Test
  public void multiplyByScalar_generalCaseOfNonZeroVector() {
    assertThat(
        singletonRBVector(1.1).multiplyByScalar(2.0),
        rbVectorMatcher(singletonRBVector(doubleExplained(2.2, 1.1 * 2.0))));
    assertThat(
        singletonRBVector(1.1).multiplyByScalar(-2.0),
        rbVectorMatcher(singletonRBVector(doubleExplained(-2.2, 1.1 * -2.0))));
    assertThat(
        singletonRBVector(-1.1).multiplyByScalar(2.0),
        rbVectorMatcher(singletonRBVector(doubleExplained(-2.2, -1.1 * 2.0))));
    assertThat(
        singletonRBVector(1.1).multiplyByScalar(0),
        rbVectorMatcher(singletonRBVector(0)));

    assertThat(
        rbVector(2.2, -4.4, 1.6).multiplyByScalar(1.5),
        rbVectorMatcher(
            rbVector(
                doubleExplained( 3.3,  2.2 * 1.5),
                doubleExplained(-6.6, -4.4 * 1.5),
                doubleExplained( 2.4,  1.6 * 1.5))));
    assertThat(
        rbVector(2.2, -4.4, 1.6).multiplyByScalar(0),
        rbVectorMatcher(
            rbVector(0, 0, 0)));
  }

  @Test
  public void testZeroRBVectorWithDimension() {
    assertIllegalArgumentException( () -> zeroRBVectorWithDimension(-999));
    assertIllegalArgumentException( () -> zeroRBVectorWithDimension(-1));
    assertIllegalArgumentException( () -> zeroRBVectorWithDimension(0));

    RBVector zero1d = zeroRBVectorWithDimension(1);
    assertEquals(1, zero1d.size());
    assertEquals(0, zero1d.get(0), 0.0);

    RBVector zero3d = zeroRBVectorWithDimension(3);
    assertEquals(3, zero3d.size());
    assertEquals(0, zero3d.get(0), 0.0);
    assertEquals(0, zero3d.get(1), 0.0);
    assertEquals(0, zero3d.get(2), 0.0);
  }

  @Test
  public void testDoubleArrayConstructor() {
    assertThat(
        RBVector.rbVector(new double[] {1.1, 2.2, 3.3}),
        rbVectorMatcher(rbVector(1.1, 2.2, 3.3)));
  }

  @Test
  public void testGet() {
    RBVector rbVector = rbVector(1.1, -3.3, 7.7);
    assertEquals(3, rbVector.size());
    assertThrows(IndexOutOfBoundsException.class, () -> rbVector.get(-999));
    assertThrows(IndexOutOfBoundsException.class, () -> rbVector.get(-1));
    assertEquals( 1.1, rbVector.get(0), 0.0);
    assertEquals(-3.3, rbVector.get(1), 0.0);
    assertEquals( 7.7, rbVector.get(2), 0.0);
    assertThrows(IndexOutOfBoundsException.class, () -> rbVector.get(3));
    assertThrows(IndexOutOfBoundsException.class, () -> rbVector.get(999));
  }

  @Test
  public void testGetQuick() {
    RBVector rbVector = rbVector(1.1, -3.3, 7.7);
    assertEquals(3, rbVector.size());
    // Note inconsistency of IndexOutOfBoundsException in #get vs ArrayIndexOutOfBoundsException in #getQuick.
    // This is what Colt does. It's not worth catching and rethrowing... either way, if we get an exception,
    // the type won't really matter, because it's not like we'll catch one specific type further up in the stack.
    // This will just become a runtime error in either case.
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> rbVector.getQuick(-999));
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> rbVector.getQuick(-1));
    assertEquals( 1.1, rbVector.getQuick(0), 0.0);
    assertEquals(-3.3, rbVector.getQuick(1), 0.0);
    assertEquals( 7.7, rbVector.getQuick(2), 0.0);
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> rbVector.getQuick(3));
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> rbVector.getQuick(999));
  }

  @Test
  public void testProjectOnto_specialCaseOfXaxis_easyToRead() {
    // projection is in same direction
    rbSetOf(-100, -1, 0, 1, 100).forEach(yCoordinate ->
        assertThat(
            rbVector(77, yCoordinate).projectOnto(rbVector(12.34, 0)),
            rbVectorMatcher(
                rbVector(77, 0))));

    // projection is in same direction
    rbSetOf(-100, -1, 0, 1, 100).forEach(yCoordinate ->
        assertThat(
            rbVector(-77, yCoordinate).projectOnto(rbVector(12.34, 0)),
            rbVectorMatcher(
                rbVector(-77, 0))));

    // vectors are perpendicular, so projection is 0 vector by definition
    rbSetOf(-100, -1, 0, 1, 100).forEach(yCoordinate ->
        assertThat(
            rbVector(0, yCoordinate).projectOnto(rbVector(12.34, 0)),
            rbVectorMatcher(
                rbVector(0, 0))));
  }

  @Test
  public void testProjectOnto_moreGeneral() {
    assertThat(
        rbVector(3, 6).projectOnto(rbVector(4, 2)),
        rbVectorMatcher(
            // Hard to explain with doubleExplained here, but note that P=(4.8, 2.4) has the same direction as vector
            // U=(4, 2), and the magnitude is a bit bigger. The following ASCII art gives you some idea.
            // Note that V = (3,6), and the location of P is shown as (4.8, 2.5) which is not identical to (4.8, 2.4),
            // but close enough for purposes of this illustration.
            //
            // .    .    .    V    .    .
            //
            // .    .    .    .    .    .
            //
            // .    .    .    .    .    .
            //
            // .    .    .    .    .    .
            //                         P
            // .    .    .    .    U    .
            //
            // .    .    .    .    .    .
            //
            // o    .    .    .    .    .
            rbVector(4.8, 2.4)));

    assertThat(
        rbVector(-3, -6).projectOnto(rbVector(4, 2)),
        rbVectorMatcher(rbVector(-4.8, -2.4)));
    assertThat(
        rbVector(3, 6).projectOnto(rbVector(-4, -2)),
        rbVectorMatcher(rbVector(4.8, 2.4)));
  }

  @Test
  public void projectOnto_zeroLengthVector_returnsZeros() {
    assertThat(
        rbVector(12.34, 78.90).projectOnto(rbVector(0.0, 0.0)),
        rbVectorMatcher(rbVector(0, 0)));
  }

  @Test
  public void projectOnto_differentNumberOfDimensions_throws() {
    assertIllegalArgumentException( () ->
        rbVector(DUMMY_DOUBLE, DUMMY_DOUBLE).projectOnto(singletonRBVector(DUMMY_DOUBLE)));
    assertIllegalArgumentException( () ->
        rbVector(DUMMY_DOUBLE, DUMMY_DOUBLE).projectOnto(rbVector(DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE)));
  }

  @Test
  public void testDoubleStream() {
    assertThat(
        rbVector(-1.1, 0, 3.3)
            .doubleStream()
            .boxed()
            .collect(Collectors.toList()),
        doubleListMatcher(
            ImmutableList.of(-1.1, 0.0, 3.3),
            DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testAsList() {
    BiConsumer<RBVector, List<Double>> asserter = (rbVector, expectedResult) ->
        assertThat(
            rbVector.asList(),
            doubleListMatcher(
                expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(
        singletonRBVector(1.1),
        singletonList(1.1));
    asserter.accept(
        rbVector(-1.1, 0.0, 3.3),
        ImmutableList.of(-1.1, 0.0, 3.3));
  }

  @Test
  public void testToArray() {
    BiConsumer<RBVector, double[]> asserter = (rbVector, expectedResult) ->
        assertThat(
            rbVector.toArray(),
            doubleArrayMatcher(
                expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(
        singletonRBVector(1.1),
        new double[] { 1.1 });
    asserter.accept(
        rbVector(-1.1, 0.0, 3.3),
        new double[] { -1.1, 0.0, 3.3 });
  }

  @Test
  public void testSumElements() {
    BiConsumer<Double, RBVector> asserter = (expectedResult, rbVector) ->
        assertEquals(
            expectedResult,
            rbVector.sumElements(),
            1e-8);

    asserter.accept(1.1, singletonRBVector(1.1));
    asserter.accept(
        doubleExplained(2.2, -1.1 + 0.0 + 3.3),
        rbVector(-1.1, 0.0, 3.3));
  }

  @Test
  public void reminder_testMultiplyOnLeft() {
    BiConsumer<DoubleMatrix2D, RBVector> asserter = (doubleMatrix2D, expectedResult) ->
        assertThat(
            rbVector(1, 2, 3).multiplyOnLeft(doubleMatrix2D),
            rbVectorMatcher(expectedResult));

    asserter.accept(
        DoubleFactory2D.dense.identity(3),
        rbVector(1, 2, 3));
    asserter.accept(
        DoubleFactory2D.dense.diagonal(DoubleFactory1D.dense.make(new double[] { 4.0, 5.0, 6.0 })),
        rbVector(
            doubleExplained( 4.0, 4.0 * 1),
            doubleExplained(10.0, 5.0 * 2),
            doubleExplained(18.0, 6.0 * 3)));
    asserter.accept(
        DoubleFactory2D.dense.make(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 },
            { 7.0, 8.0, 9.0 } }),
        rbVector(
            doubleExplained(14, 1.0 * 1 + 2.0 * 2 + 3.0 * 3),
            doubleExplained(32, 4.0 * 1 + 5.0 * 2 + 6.0 * 3),
            doubleExplained(50, 7.0 * 1 + 8.0 * 2 + 9.0 * 3)));
  }

  @Override
  public RBVector makeTrivialObject() {
    return singletonRBVector(0);
  }

  @Override
  public RBVector makeNontrivialObject() {
    return rbVector(-1.1, 0, 3.3);
  }

  @Override
  public RBVector makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbVector(-1.1 + e, 0 + e, 3.3 + e);
  }

  @Override
  protected boolean willMatch(RBVector expected, RBVector actual) {
    return rbVectorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RBVector> rbVectorMatcher(RBVector expected) {
    return rbVectorMatcher(expected, emptyMatcherEpsilons());
  }

  public static TypeSafeMatcher<RBVector> rbVectorMatcher(RBVector expected, Epsilon epsilon) {
    return rbVectorMatcher(expected, useEpsilonInAllMatchers(epsilon));
  }

  public static TypeSafeMatcher<RBVector> rbVectorMatcher(RBVector expected, MatcherEpsilons e) {
    // This matcher is a bit unusual, but we didn't want to expose the DoubleMatrix1D contents of the RBVector
    // in test, because prod code could also have access to it, and we don't want that, because DoubleMatrix1D is
    // a 3rd party mutable class.
    return makeMatcher(expected, actual ->
        doubleArrayMatcher(expected.doubleStream().toArray(), e.get(RBVector.class))
        .matches(actual.doubleStream().toArray()));
  }

}
