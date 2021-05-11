package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBSimilarityPreconditions;
import org.apache.commons.math3.util.MultidimensionalCounter;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.collections.MultiDimensionalArray.newMultiDimensionalArray;
import static com.rb.nonbiz.collections.MultiDimensionalArrays.multiDimensionalArray;
import static com.rb.nonbiz.collections.MutableMultiDimensionalArray.mutableMultiDimensionalArray;
import static com.rb.nonbiz.collections.MutableMultiDimensionalArrayTest.mutableMultiDimensionalArrayMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class MultiDimensionalArrayTest extends RBTestMatcher<MultiDimensionalArray<Double>> {

  public static <T> MultiDimensionalArray<T> singleItemMultiDimensionalArray(T item) {
    return newMultiDimensionalArray(MutableMultiDimensionalArray.<T>mutableMultiDimensionalArray(1)
        .setAssumingAbsent(item, coordinates(0)));
  }

  /**
   * Returns a multidimensional array of numSingletonDimensions dimensions, where each dimension has a size of 1,
   * and therefore there is only 1 = 1 ^ numSingletonDimensions item.
   */
  public static <T> MultiDimensionalArray<T> singleItemMultiDimensionalArray(T item, int numSingletonDimensions) {
    int[] nOnes = IntStream.range(0, numSingletonDimensions).map(i -> 1).toArray();
    int[] nZeros = IntStream.range(0, numSingletonDimensions).map(i -> 0).toArray();
    return newMultiDimensionalArray(MutableMultiDimensionalArray.<T>mutableMultiDimensionalArray(nOnes)
        .setAssumingAbsent(item, coordinates(nZeros)));
  }

  @SafeVarargs
  public static <T> MultiDimensionalArray<T> singleDimensionMultiDimensionalArray(T...items) {
    MutableMultiDimensionalArray<T> mutableArray = MutableMultiDimensionalArray.<T>mutableMultiDimensionalArray(items.length);
    for (int i = 0; i < items.length; i++) {
      mutableArray.setAssumingAbsent(items[i], coordinates(i));
    }
    return newMultiDimensionalArray(mutableArray);
  }

  public static <T> MultiDimensionalArray<T> twoDimensionalMultiDimensionalArray(T[][] rawArray) {
    int dim0 = rawArray.length;
    int dim1 = rawArray[0].length;
    MutableMultiDimensionalArray<T> mutableArray = mutableMultiDimensionalArray(dim0, dim1);
    for (int i = 0; i < dim0; i++) {
      RBSimilarityPreconditions.checkBothSame(
          dim1,
          rawArray[i].length,
          "Array passed in must be rectangular, not jagged");
      for (int j = 0; j < dim1; j++) {
        mutableArray.setAssumingAbsent(rawArray[i][j], coordinates(i, j));
      }
    }
    return newMultiDimensionalArray(mutableArray);
  }

  @Test
  public void testTheTestConstructor() {
    assertIllegalArgumentException( () -> twoDimensionalMultiDimensionalArray(new String[][] {
        { "00", "01" },
        { "10", "11", "12" }
    }));
    assertIllegalArgumentException( () -> twoDimensionalMultiDimensionalArray(new String[][] {
        { "00", "01", "02" },
        { "10", "11" }
    }));
    assertThat(
        twoDimensionalMultiDimensionalArray(new String[][] {
            { "00", "01", "02" },
            { "10", "11", "12" }
        }),
    multiDimensionalArrayEqualityMatcher(
        multiDimensionalArray(
            new MultidimensionalCounter(2, 3),
            (ignoredFlatIndex, coordinates) ->
                Strings.format("%s%s", coordinates.getNthCoordinate(0), coordinates.getNthCoordinate(1)))));
  }

  @Test
  public void usingInvalidIndices_throws() {
    MultiDimensionalArray<String> arr = newMultiDimensionalArray(mutableMultiDimensionalArray(2, 1, 3));
    String doesNotThrow;
    assertThrowsAnyException( () -> arr.get(coordinates(-999, 0, 0)));
    assertThrowsAnyException( () -> arr.get(coordinates(-1, 0, 0)));
    doesNotThrow = arr.get(coordinates(0, 0, 0));
    doesNotThrow = arr.get(coordinates(1, 0, 0));
    assertThrowsAnyException( () -> arr.get(coordinates(2, 0, 0)));
    assertThrowsAnyException( () -> arr.get(coordinates(999, 0, 0)));

    assertThrowsAnyException( () -> arr.get(coordinates(0, -999, 0)));
    assertThrowsAnyException( () -> arr.get(coordinates(0, -1, 0)));
    doesNotThrow = arr.get(coordinates(0, 0, 0));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 1, 0)));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 2, 0)));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 999, 0)));

    assertThrowsAnyException( () -> arr.get(coordinates(0, 0, -999)));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 0, -1)));
    doesNotThrow = arr.get(coordinates(0, 0, 0));
    doesNotThrow = arr.get(coordinates(0, 0, 1));
    doesNotThrow = arr.get(coordinates(0, 0, 2));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 0, 3)));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 0, 999)));
  }

  @Test
  public void usingFewerOrMoreIndicesThanExistingDimensions_throws() {
    MultiDimensionalArray<String> arr = newMultiDimensionalArray(mutableMultiDimensionalArray(1, 1, 1));
    assertThrowsAnyException( () -> arr.get(coordinates()));
    assertThrowsAnyException( () -> arr.get(coordinates(0)));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 0)));
    String doesNotThrow = arr.get(coordinates(0, 0, 0));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 0, 0, 0)));
  }

  @Test
  public void testSetAndGet() {
    MultiDimensionalArray<String> arr = multiDimensionalArray(
        new MultidimensionalCounter(1, 2, 1, 4, 1, 3, 1),
        (flatIndex, coordinates) -> Strings.format("%s%s%s",
            coordinates.getNthCoordinate(1),
            coordinates.getNthCoordinate(3),
            coordinates.getNthCoordinate(5)));

    assertEquals("000", arr.get(coordinates(0, 0, 0, 0, 0, 0, 0)));
    assertEquals("001", arr.get(coordinates(0, 0, 0, 0, 0, 1, 0)));
    assertEquals("002", arr.get(coordinates(0, 0, 0, 0, 0, 2, 0)));
    assertEquals("010", arr.get(coordinates(0, 0, 0, 1, 0, 0, 0)));
    assertEquals("011", arr.get(coordinates(0, 0, 0, 1, 0, 1, 0)));
    assertEquals("012", arr.get(coordinates(0, 0, 0, 1, 0, 2, 0)));
    assertEquals("020", arr.get(coordinates(0, 0, 0, 2, 0, 0, 0)));
    assertEquals("021", arr.get(coordinates(0, 0, 0, 2, 0, 1, 0)));
    assertEquals("022", arr.get(coordinates(0, 0, 0, 2, 0, 2, 0)));
    assertEquals("030", arr.get(coordinates(0, 0, 0, 3, 0, 0, 0)));
    assertEquals("031", arr.get(coordinates(0, 0, 0, 3, 0, 1, 0)));
    assertEquals("032", arr.get(coordinates(0, 0, 0, 3, 0, 2, 0)));

    assertEquals("100", arr.get(coordinates(0, 1, 0, 0, 0, 0, 0)));
    assertEquals("101", arr.get(coordinates(0, 1, 0, 0, 0, 1, 0)));
    assertEquals("102", arr.get(coordinates(0, 1, 0, 0, 0, 2, 0)));
    assertEquals("110", arr.get(coordinates(0, 1, 0, 1, 0, 0, 0)));
    assertEquals("111", arr.get(coordinates(0, 1, 0, 1, 0, 1, 0)));
    assertEquals("112", arr.get(coordinates(0, 1, 0, 1, 0, 2, 0)));
    assertEquals("120", arr.get(coordinates(0, 1, 0, 2, 0, 0, 0)));
    assertEquals("121", arr.get(coordinates(0, 1, 0, 2, 0, 1, 0)));
    assertEquals("122", arr.get(coordinates(0, 1, 0, 2, 0, 2, 0)));
    assertEquals("130", arr.get(coordinates(0, 1, 0, 3, 0, 0, 0)));
    assertEquals("131", arr.get(coordinates(0, 1, 0, 3, 0, 1, 0)));
    assertEquals("132", arr.get(coordinates(0, 1, 0, 3, 0, 2, 0)));
  }

  @Override
  public MultiDimensionalArray<Double> makeTrivialObject() {
    return singleItemMultiDimensionalArray(0.0);
  }

  @Override
  public MultiDimensionalArray<Double> makeNontrivialObject() {
    return newMultiDimensionalArray(new MutableMultiDimensionalArrayTest().makeNontrivialObject());
  }

  @Override
  public MultiDimensionalArray<Double> makeMatchingNontrivialObject() {
    return newMultiDimensionalArray(new MutableMultiDimensionalArrayTest().makeMatchingNontrivialObject());
  }

  @Override
  protected boolean willMatch(MultiDimensionalArray<Double> expected, MultiDimensionalArray<Double> actual) {
    return multiDimensionalArrayMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  public static <T> TypeSafeMatcher<MultiDimensionalArray<T>> multiDimensionalArrayEqualityMatcher(
      MultiDimensionalArray<T> expected) {
    return multiDimensionalArrayMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <T> TypeSafeMatcher<MultiDimensionalArray<T>> multiDimensionalArrayMatcher(
      MultiDimensionalArray<T> expected, MatcherGenerator<T> arrayItemMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawMutableArray(), f -> mutableMultiDimensionalArrayMatcher(f, arrayItemMatcherGenerator)));
  }

}
