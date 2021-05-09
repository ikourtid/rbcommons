package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.collections.MutableMultiDimensionalArray.mutableMultiDimensionalArray;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.arrayMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.multidimensionalCounterMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static junit.framework.TestCase.assertEquals;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class MutableMultiDimensionalArrayTest extends RBTestMatcher<MutableMultiDimensionalArray<Double>> {

  @Test
  public void invalidSize_throws() {
    MutableMultiDimensionalArray<?> doesNotThrow = mutableMultiDimensionalArray(1, 1, 1);
    assertThrowsAnyException( () -> mutableMultiDimensionalArray(0, 1, 1));
    assertThrowsAnyException( () -> mutableMultiDimensionalArray(-1, 1, 1));
    assertThrowsAnyException( () -> mutableMultiDimensionalArray(1, 0, 1));
    assertThrowsAnyException( () -> mutableMultiDimensionalArray(1, -1, 1));
    assertThrowsAnyException( () -> mutableMultiDimensionalArray(1, 1, 0));
    assertThrowsAnyException( () -> mutableMultiDimensionalArray(1, 1, -1));
  }

  @Test
  public void usingInvalidIndices_throws() {
    MutableMultiDimensionalArray<String> arr = mutableMultiDimensionalArray(2, 1, 3);
    String doesNotThrow;
    assertThrowsAnyException( () -> arr.get(coordinates(-999, 0, 0)));
    assertThrowsAnyException( () -> arr.get(coordinates(-1, 0, 0)));
    doesNotThrow = arr.get(coordinates(0, 0, 0));
    doesNotThrow = arr.get(coordinates(1, 0, 0));
    assertThrowsAnyException( () -> arr.get(coordinates(2, 0, 0)));
    assertThrowsAnyException( () -> arr.get(coordinates(999, 0, 0)));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 0)));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 0, 0, 0)));

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
    MutableMultiDimensionalArray<String> arr = mutableMultiDimensionalArray(1, 1, 1);
    assertThrowsAnyException( () -> arr.get(coordinates(0)));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 0)));
    String doesNotThrow = arr.get(coordinates(0, 0, 0));
    assertThrowsAnyException( () -> arr.get(coordinates(0, 0, 0, 0)));
  }

  @Test
  public void testSetAssumingAbsent_invalidCoordinates_throws() {
    MutableMultiDimensionalArray<String> arr =
        MutableMultiDimensionalArray.<String>mutableMultiDimensionalArray(1, 1, 1);
    assertThrowsAnyException( () -> arr.setAssumingAbsent(DUMMY_STRING, coordinates(0)));
    assertThrowsAnyException( () -> arr.setAssumingAbsent(DUMMY_STRING, coordinates(0, 0)));
    MutableMultiDimensionalArray<String> doesNotThrow = arr.setAssumingAbsent(DUMMY_STRING, coordinates(0, 0, 0));
    assertThrowsAnyException( () -> arr.setAssumingAbsent(DUMMY_STRING, coordinates(0, 0, 0, 0)));
  }

  @Test
  public void testSetAndGet() {
    MutableMultiDimensionalArray<String> arr =
        MutableMultiDimensionalArray.<String>mutableMultiDimensionalArray(1, 2, 1, 4, 1, 3, 1)
            .setAssumingAbsent("000", coordinates(0, 0, 0, 0, 0, 0, 0))
            .setAssumingAbsent("001", coordinates(0, 0, 0, 0, 0, 1, 0))
            .setAssumingAbsent("002", coordinates(0, 0, 0, 0, 0, 2, 0))
            .setAssumingAbsent("010", coordinates(0, 0, 0, 1, 0, 0, 0))
            .setAssumingAbsent("011", coordinates(0, 0, 0, 1, 0, 1, 0))
            .setAssumingAbsent("012", coordinates(0, 0, 0, 1, 0, 2, 0))
            .setAssumingAbsent("020", coordinates(0, 0, 0, 2, 0, 0, 0))
            .setAssumingAbsent("021", coordinates(0, 0, 0, 2, 0, 1, 0))
            .setAssumingAbsent("022", coordinates(0, 0, 0, 2, 0, 2, 0))
            .setAssumingAbsent("030", coordinates(0, 0, 0, 3, 0, 0, 0))
            .setAssumingAbsent("031", coordinates(0, 0, 0, 3, 0, 1, 0))
            .setAssumingAbsent("032", coordinates(0, 0, 0, 3, 0, 2, 0))

            .setAssumingAbsent("100", coordinates(0, 1, 0, 0, 0, 0, 0))
            .setAssumingAbsent("101", coordinates(0, 1, 0, 0, 0, 1, 0))
            .setAssumingAbsent("102", coordinates(0, 1, 0, 0, 0, 2, 0))
            .setAssumingAbsent("110", coordinates(0, 1, 0, 1, 0, 0, 0))
            .setAssumingAbsent("111", coordinates(0, 1, 0, 1, 0, 1, 0))
            .setAssumingAbsent("112", coordinates(0, 1, 0, 1, 0, 2, 0))
            .setAssumingAbsent("120", coordinates(0, 1, 0, 2, 0, 0, 0))
            .setAssumingAbsent("121", coordinates(0, 1, 0, 2, 0, 1, 0))
            .setAssumingAbsent("122", coordinates(0, 1, 0, 2, 0, 2, 0))
            .setAssumingAbsent("130", coordinates(0, 1, 0, 3, 0, 0, 0))
            .setAssumingAbsent("131", coordinates(0, 1, 0, 3, 0, 1, 0))
            .setAssumingAbsent("132", coordinates(0, 1, 0, 3, 0, 2, 0));

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
  public MutableMultiDimensionalArray<Double> makeTrivialObject() {
    return MutableMultiDimensionalArray.<Double>mutableMultiDimensionalArray(1)
        .setAssumingAbsent(0.0, coordinates(0));
  }

  @Override
  public MutableMultiDimensionalArray<Double> makeNontrivialObject() {
    return MutableMultiDimensionalArray.<Double>mutableMultiDimensionalArray(2, 1, 3)
        .setAssumingAbsent(1_000.0, coordinates(0, 0, 0))
        .setAssumingAbsent(1_000.1, coordinates(0, 0, 1))
        .setAssumingAbsent(1_000.2, coordinates(0, 0, 2))

        .setAssumingAbsent(1_001.0, coordinates(1, 0, 0))
        .setAssumingAbsent(1_001.1, coordinates(1, 0, 1))
        .setAssumingAbsent(1_001.2, coordinates(1, 0, 2));
  }

  @Override
  public MutableMultiDimensionalArray<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return MutableMultiDimensionalArray.<Double>mutableMultiDimensionalArray(2, 1, 3)
        .setAssumingAbsent(1_000.0 + e, coordinates(0, 0, 0))
        .setAssumingAbsent(1_000.1 + e, coordinates(0, 0, 1))
        .setAssumingAbsent(1_000.2 + e, coordinates(0, 0, 2))

        .setAssumingAbsent(1_001.0 + e, coordinates(1, 0, 0))
        .setAssumingAbsent(1_001.1 + e, coordinates(1, 0, 1))
        .setAssumingAbsent(1_001.2 + e, coordinates(1, 0, 2));
  }

  @Override
  protected boolean willMatch(MutableMultiDimensionalArray<Double> expected,
                              MutableMultiDimensionalArray<Double> actual) {
    return mutableMultiDimensionalArrayMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }
  
  public static <T> TypeSafeMatcher<MutableMultiDimensionalArray<T>> mutableMultiDimensionalArrayMatcher(
      MutableMultiDimensionalArray<T> expected, MatcherGenerator<T> arrayItemMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getMultidimensionalCounter(), f -> multidimensionalCounterMatcher(f)),
        match(v -> v.getValues(), f -> arrayMatcher(f, arrayItemMatcherGenerator)));
  }
  
}
