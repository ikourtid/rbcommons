package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.Coordinates.coordinates;
import static com.rb.nonbiz.collections.Coordinates.coordinatesAllZero;
import static com.rb.nonbiz.testmatchers.Match.matchIntArray;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static org.hamcrest.MatcherAssert.assertThat;

public class CoordinatesTest extends RBTestMatcher<Coordinates> {

  @Test
  public void emptyCoordinates_throws() {
    assertIllegalArgumentException( () -> coordinates());
    Coordinates doesNotThrow = coordinates(123);
  }

  @Test
  public void negativeCoordinates_throws() {
    assertIllegalArgumentException( () -> coordinates(-999));
    assertIllegalArgumentException( () -> coordinates(-999, DUMMY_POSITIVE_INTEGER));
    assertIllegalArgumentException( () -> coordinates(DUMMY_POSITIVE_INTEGER, -999));

    assertIllegalArgumentException( () -> coordinates(-1));
    assertIllegalArgumentException( () -> coordinates(-1, DUMMY_POSITIVE_INTEGER));
    assertIllegalArgumentException( () -> coordinates(DUMMY_POSITIVE_INTEGER, -1));

    Coordinates doesNotThrow;
    doesNotThrow = coordinates(0);
    doesNotThrow = coordinates(1);
    doesNotThrow = coordinates(999);

    doesNotThrow = coordinates(0, DUMMY_POSITIVE_INTEGER);
    doesNotThrow = coordinates(1, DUMMY_POSITIVE_INTEGER);
    doesNotThrow = coordinates(999, DUMMY_POSITIVE_INTEGER);

    doesNotThrow = coordinates(DUMMY_POSITIVE_INTEGER, 0);
    doesNotThrow = coordinates(DUMMY_POSITIVE_INTEGER, 1);
    doesNotThrow = coordinates(DUMMY_POSITIVE_INTEGER, 999);
  }

  @Test
  public void testGetSubset() {
    Coordinates oneD = coordinates(70);
    assertThat(oneD.getSubset(0, 1), coordinatesMatcher(coordinates(70)));
    assertThrowsAnyException( () -> oneD.getSubset(0, 0));
    assertThrowsAnyException( () -> oneD.getSubset(0, 2));
    assertThrowsAnyException( () -> oneD.getSubset(1, 1));
    assertThrowsAnyException( () -> oneD.getSubset(-1, 0));
    assertThrowsAnyException( () -> oneD.getSubset(-1, -1));
    assertThrowsAnyException( () -> oneD.getSubset(-1, 1));
    
    Coordinates twoD = coordinates(70, 71);
    assertThat(twoD.getSubset(0, 1), coordinatesMatcher(coordinates(70)));
    assertThat(twoD.getSubset(1, 2), coordinatesMatcher(coordinates(71)));
    assertThat(twoD.getSubset(0, 2), coordinatesMatcher(coordinates(70, 71)));
    assertThrowsAnyException( () -> twoD.getSubset(0, 3));
    assertThrowsAnyException( () -> twoD.getSubset(1, 3));
    assertThrowsAnyException( () -> twoD.getSubset(-1, 0));
    assertThrowsAnyException( () -> twoD.getSubset(-1, -1));
    assertThrowsAnyException( () -> twoD.getSubset(-1, 1));
  }

  @Test
  public void test_copyWithChangedNthItem() {
    Coordinates coordinates = coordinates(4, 3, 2, 1, 0);
    assertThat(
        coordinates.copyWithChangedNthItem(0, 11),
        coordinatesMatcher(coordinates(11, 3, 2, 1, 0)));
    assertThat(
        coordinates.copyWithChangedNthItem(1, 11),
        coordinatesMatcher(coordinates(4, 11, 2, 1, 0)));
    assertThat(
        coordinates.copyWithChangedNthItem(2, 11),
        coordinatesMatcher(coordinates(4, 3, 11, 1, 0)));
    assertThat(
        coordinates.copyWithChangedNthItem(3, 11),
        coordinatesMatcher(coordinates(4, 3, 2, 11, 0)));
    assertThat(
        coordinates.copyWithChangedNthItem(4, 11),
        coordinatesMatcher(coordinates(4, 3, 2, 1, 11)));

    // Make sure we didn't change the original object.
    assertThat(
        coordinates,
        coordinatesMatcher(coordinates(4, 3, 2, 1, 0)));

    // Check some illegal arguments.
    assertIllegalArgumentException( () -> coordinates.copyWithChangedNthItem(-1, 0)); // Negative index
    assertIllegalArgumentException( () -> coordinates.copyWithChangedNthItem(5, 0));  // Index too big by 1
    assertIllegalArgumentException( () -> coordinates.copyWithChangedNthItem(50, 0)); // Index too big by a lot
    assertIllegalArgumentException( () -> coordinates.copyWithChangedNthItem(0, -1)); // Set to negative value
  }

  @Test
  public void test_coordinatesOfNZeroes() {
    assertThat(
        coordinatesAllZero(1),
        coordinatesMatcher(coordinates(0)));
    assertThat(
        coordinatesAllZero(2),
        coordinatesMatcher(coordinates(0, 0)));
    assertThat(
        coordinatesAllZero(8),
        coordinatesMatcher(coordinates(0, 0, 0, 0, 0, 0, 0, 0)));

    // Check some illegal arguments.
    assertIllegalArgumentException( () -> coordinatesAllZero(0));
    assertIllegalArgumentException( () -> coordinatesAllZero(-1));
  }

  @Override
  public Coordinates makeTrivialObject() {
    return coordinates(0);
  }

  @Override
  public Coordinates makeNontrivialObject() {
    return coordinates(11, 22, 33);
  }

  @Override
  public Coordinates makeMatchingNontrivialObject() {
    return coordinates(11, 22, 33);
  }

  @Override
  protected boolean willMatch(Coordinates expected, Coordinates actual) {
    return coordinatesMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<Coordinates> coordinatesMatcher(Coordinates expected) {
    return makeMatcher(expected,
        matchIntArray(v -> v.getRawCoordinatesArray()));
  }

}
