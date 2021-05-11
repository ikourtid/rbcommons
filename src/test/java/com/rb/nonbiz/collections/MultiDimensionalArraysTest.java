package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import org.apache.commons.math3.util.MultidimensionalCounter;
import org.junit.Test;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.collections.MultiDimensionalArray.newMultiDimensionalArray;
import static com.rb.nonbiz.collections.MultiDimensionalArrayTest.multiDimensionalArrayEqualityMatcher;
import static com.rb.nonbiz.collections.MultiDimensionalArrays.getSliceOfMultiDimensionalArray;
import static com.rb.nonbiz.collections.MultiDimensionalArrays.multiDimensionalArray;
import static com.rb.nonbiz.collections.MultiDimensionalArrays.multiDimensionalArraysHaveSameSize;
import static com.rb.nonbiz.collections.MultiDimensionalArrays.transformMultiDimensionalArray;
import static com.rb.nonbiz.collections.MutableMultiDimensionalArray.mutableMultiDimensionalArray;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class MultiDimensionalArraysTest {

  @Test
  public void testMultiDimensionalArraysHaveSameSize() {
    assertIs2x1x3(true, 2, 1, 3);
    assertIs2x1x3(false, 2, 1, 3, 1);
    assertIs2x1x3(false, 2, 1, 3, 2);
    assertIs2x1x3(false, 1, 1, 3);
    assertIs2x1x3(false, 3, 1, 3);
    assertIs2x1x3(false, 2, 2, 3);
    assertIs2x1x3(false, 2, 1, 2);
    assertIs2x1x3(false, 2, 1, 4);

    assertIs2x1x3(false, 2);
    assertIs2x1x3(false, 2, 1);
    assertIs2x1x3(false, 1, 3);
    assertIs2x1x3(false, 1);
    assertIs2x1x3(false, 3);
  }

  @Test
  public void testGetSliceInputValidation() {
    MultiDimensionalArray<String> originalArray = makeStringArray(2, 1, 3);
    MultiDimensionalArray<String> doesNotThrow;
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, -1, -1));
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, -1, 0));
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, -1, 1));
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, 0, -1));
    doesNotThrow = getSliceOfMultiDimensionalArray(originalArray, 0, 0);
    doesNotThrow = getSliceOfMultiDimensionalArray(originalArray, 0, 1);
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, 0, 2));
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, 1, -1));
    doesNotThrow = getSliceOfMultiDimensionalArray(originalArray, 1, 0);
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, 1, 1));
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, 2, -1));
    doesNotThrow = getSliceOfMultiDimensionalArray(originalArray, 2, 0);
    doesNotThrow = getSliceOfMultiDimensionalArray(originalArray, 2, 1);
    doesNotThrow = getSliceOfMultiDimensionalArray(originalArray, 2, 2);
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, 2, 3));
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, 3, -1));
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, 3, 0));
    assertThrowsAnyException( () -> getSliceOfMultiDimensionalArray(originalArray, 3, 1));
  }

  @Test
  public void testGetSlice() {
    MultiDimensionalArray<String> originalArray = makeStringArray(2, 1, 3);
    // The following are all the valid cases; see testGetSliceInputValidation for validation of the invalid cases.
    assertThat(
        getSliceOfMultiDimensionalArray(originalArray, 0, 0),
        multiDimensionalArrayEqualityMatcher(makeStringArray(1, 1, 3)));
    assertThat(
        getSliceOfMultiDimensionalArray(originalArray, 0, 1),
        multiDimensionalArrayEqualityMatcher(newMultiDimensionalArray(
            MutableMultiDimensionalArray.<String>mutableMultiDimensionalArray(1, 1, 3)
                .setAssumingAbsent("100", coordinates(0, 0, 0))
                .setAssumingAbsent("101", coordinates(0, 0, 1))
                .setAssumingAbsent("102", coordinates(0, 0, 2)))));
    // If we try to vary dimension 1 (starting at 0, so the 2nd one)
    // then we get back the original array, because dimension 1 has size 1,
    // so there are no multiple values in that dimension to vary.
    assertThat(
        getSliceOfMultiDimensionalArray(originalArray, 1, 0),
        multiDimensionalArrayEqualityMatcher(originalArray));
    assertThat(
        getSliceOfMultiDimensionalArray(originalArray, 2, 0),
        multiDimensionalArrayEqualityMatcher(makeStringArray(2, 1, 1)));
    assertThat(
        getSliceOfMultiDimensionalArray(originalArray, 2, 1),
        multiDimensionalArrayEqualityMatcher(newMultiDimensionalArray(
            MutableMultiDimensionalArray.<String>mutableMultiDimensionalArray(2, 1, 1)
                .setAssumingAbsent("001", coordinates(0, 0, 0))
                .setAssumingAbsent("101", coordinates(1, 0, 0)))));
    assertThat(
        getSliceOfMultiDimensionalArray(originalArray, 2, 2),
        multiDimensionalArrayEqualityMatcher(newMultiDimensionalArray(
            MutableMultiDimensionalArray.<String>mutableMultiDimensionalArray(2, 1, 1)
                .setAssumingAbsent("002", coordinates(0, 0, 0))
                .setAssumingAbsent("102", coordinates(1, 0, 0)))));
  }

  @Test
  public void testTransformMultiDimensionalArray() {
    assertThat(
        transformMultiDimensionalArray(
            newMultiDimensionalArray(
                MutableMultiDimensionalArray.<Double>mutableMultiDimensionalArray(2, 1, 3)
                    .setAssumingAbsent(7.0, coordinates(0, 0, 0))
                    .setAssumingAbsent(7.1, coordinates(0, 0, 1))
                    .setAssumingAbsent(7.2, coordinates(0, 0, 2))

                    .setAssumingAbsent(8.0, coordinates(1, 0, 0))
                    .setAssumingAbsent(8.1, coordinates(1, 0, 1))
                    .setAssumingAbsent(8.2, coordinates(1, 0, 2))),
            v -> Strings.format("_%s_", v)),
        multiDimensionalArrayEqualityMatcher(
            newMultiDimensionalArray(
                MutableMultiDimensionalArray.<String>mutableMultiDimensionalArray(2, 1, 3)
                    .setAssumingAbsent("_7.0_", coordinates(0, 0, 0))
                    .setAssumingAbsent("_7.1_", coordinates(0, 0, 1))
                    .setAssumingAbsent("_7.2_", coordinates(0, 0, 2))

                    .setAssumingAbsent("_8.0_", coordinates(1, 0, 0))
                    .setAssumingAbsent("_8.1_", coordinates(1, 0, 1))
                    .setAssumingAbsent("_8.2_", coordinates(1, 0, 2)))));
  }

  private void assertIs2x1x3(boolean expectedAnswer, int...sizesOfOtherArray) {
    MultiDimensionalArray<String> array      = makeMultiDimensionalArray(2, 1, 3);
    MultiDimensionalArray<String> otherArray = makeMultiDimensionalArray(sizesOfOtherArray);
    assertEquals(expectedAnswer, multiDimensionalArraysHaveSameSize(array, otherArray));
    assertEquals(expectedAnswer, multiDimensionalArraysHaveSameSize(otherArray, array));
  }

  private MultiDimensionalArray<String> makeStringArray(int...sizes) {
    return multiDimensionalArray(
        new MultidimensionalCounter(sizes),
        (flatIndex, coordinates) -> Strings.format("%s%s%s",
            coordinates.getNthCoordinate(0),
            coordinates.getNthCoordinate(1),
            coordinates.getNthCoordinate(2)));

  }

  private MultiDimensionalArray<String> makeMultiDimensionalArray(int...sizes) {
    return newMultiDimensionalArray(mutableMultiDimensionalArray(sizes));
  }

}
