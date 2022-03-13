package com.rb.nonbiz.testutils;

import com.google.common.collect.ImmutableSet;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidSet;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.IidSetTest.iidSetMatcher;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRestDoubles;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.nonEmptyOptionalDoubleMatcher;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Asserters {

  public static <T extends RBNumeric<? super T>> void assertAlmostEquals(T value1, T value2, double epsilon) {
    assertAlmostEquals(
        Strings.format("Values must be within %s but were %s and %s", epsilon, value1, value2),
        value1, value2, epsilon);
  }

  public static <T extends RBNumeric<? super T>> void assertAlmostEquals(String message, T value1, T value2, double epsilon) {
    if (value1 == null && value2 == null) {
      return;
    }
    assertNotNull(value1);
    assertEquals(
        Strings.format("%s is not within %s of %s ; %s", value1, epsilon, value2, message),
        value1.doubleValue(),
        value2.doubleValue(),
        epsilon);
  }

  public static <V extends ImpreciseValue<V>> void assertAlmostEquals(
      String message, V value1, V value2, double epsilon) {
    if (value1 == null && value2 == null) {
      return;
    }
    assertNotNull(value1);
    assertTrue(
        Strings.format("%s is not within %s of %s ; %s", value1, epsilon, value2, message),
        value1.almostEquals(value2, epsilon));
  }

  public static <T> void assertCollectionEqualsIgnoringOrder(Collection<? extends T> expected, Collection<? extends T> actual) {
    assertEquals(ImmutableSet.copyOf(expected), ImmutableSet.copyOf(actual));
  }

  public static void assertDoubleArraysAlmostEqual(double[] expected, double[] actual, double epsilon) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(
          Strings.format("expected array %s ; got %s", ArrayUtils.toString(expected), ArrayUtils.toString(actual)),
          expected[i], actual[i], epsilon);
    }
  }

  public static void assertDoubleListsAlmostEqual(List<Double> expected, List<Double> actual, double epsilon) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(
          Strings.format("expected array %s ; got %s", ArrayUtils.toString(expected), ArrayUtils.toString(actual)),
          expected.get(i), actual.get(i), epsilon);
    }
  }

  public static void assertEmpty(Collection<?> collection) {
    assertTrue(collection.isEmpty());
  }

  public static void assertEmptyIterator(Iterator<?> iterator) {
    assertFalse(iterator.hasNext());
  }

  public static <K, V> void assertEmptyRBMap(RBMap<K, V> map) {
    assertTrue(map.isEmpty());
  }

  public static void assertIllegalArgumentException(Runnable runnable) {
    assertThrows(IllegalArgumentException.class, runnable);
  }

  public static void assertPossiblyIllegalArgumentException(boolean shouldThrow, Runnable runnable) {
    assertPossiblyThrows(shouldThrow, IllegalArgumentException.class, runnable);
  }

  public static void assertNullPointerException(Runnable runnable) {
    assertThrows(NullPointerException.class, runnable);
  }

  public static void assertRuntimeException(Runnable runnable) {
    assertThrows(RuntimeException.class, runnable);
  }

  public static void assertThrowsAnyException(Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception exception) {
      return;
    }
    fail("Expected exception of any type; got no exception");
  }

  public static void assertPossiblyThrowsAnyException(boolean shouldThrow, Runnable runnable) {
    if (!shouldThrow) {
      runnable.run();
      return;
    }
    try {
      runnable.run();
    } catch (Exception exception) {
      return;
    }
    fail("Expected exception of any type; got no exception");
  }

  public static <T> void assertOptionalEquals(T expected, Optional<T> actual) {
    assertTrue(actual.isPresent());
    assertEquals(expected, actual.get());
  }

  public static void assertOptionalIntEquals(int expected, OptionalInt actual) {
    assertTrue(actual.isPresent());
    assertEquals(expected, actual.getAsInt());
  }

  public static void assertOptionalLongEquals(long expected, OptionalLong actual) {
    assertTrue(actual.isPresent());
    assertEquals(expected, actual.getAsLong());
  }

  public static <T extends RBNumeric<? super T>> void assertOptionalAlmostEquals(
      T expected, Optional<T> actual, double epsilon) {
    assertTrue(actual.isPresent());
    assertAlmostEquals(expected, actual.get(), epsilon);
  }

  public static <T> void assertOptionalNonEmpty(Optional<T> actual) {
    assertTrue(actual.isPresent());
  }

  public static <T> void assertOptionalNonEmpty(Optional<T> actual, TypeSafeMatcher<T> valueMatcher) {
    assertTrue(actual.isPresent());
    assertThat(actual.get(), valueMatcher);
  }

  /** This is trivial, but it aids clarity in case the expression for 'actual' is long,
   * and the .isPresent() might be hard to see at the end.
   */
  public static <T> void assertOptionalEmpty(Optional<T> actual) {
    assertFalse(actual.isPresent());
  }

  public static void assertOptionalDoubleEmpty(OptionalDouble actual) {
    assertFalse(actual.isPresent());
  }

  public static void assertOptionalDoubleAlmostEquals(double expected, OptionalDouble actual, double epsilon) {
    RBPreconditions.checkArgument(epsilon >= 0);
    assertThat(actual, nonEmptyOptionalDoubleMatcher(expected, epsilon));
  }

  public static void assertOptionalIntEmpty(OptionalInt actual) {
    assertFalse(actual.isPresent());
  }

  public static void assertOptionalLongEmpty(OptionalLong actual) {
    assertFalse(actual.isPresent());
  }

  public static <T> void assertSingletonRBSet(T expected, RBSet<T> actual) {
    assertEquals(singleton(expected), actual.asSet());
  }

  public static void assertSingletonIidSet(InstrumentId expected, IidSet actual) {
    assertThat(
        actual,
        iidSetMatcher(singletonIidSet(expected)));
  }

  public static void assertIidSetEquals(IidSet expected, IidSet actual) {
    assertThat(
        actual,
        iidSetMatcher(expected));
  }

  public static <T> void assertSingletonRBList(T expected, RBSet<T> actual) {
    assertEquals(singletonList(expected), actual.asSet());
  }

  public static <T> void assertSingletonSet(T expected, Set<T> actual) {
    assertEquals(singleton(expected), actual);
  }

  public static <T> void assertSingletonList(T expected, List<T> actual) {
    assertEquals(singletonList(expected), actual);
  }

  public static void assertSize(int expectedSize, Collection collection) {
    assertEquals(expectedSize, collection.size());
  }

  public static void assertStringContains(String expectedSuperset, String infix) {
    assertTrue(expectedSuperset.contains(infix));
  }

  /**
   * Some classes may try to print a set (whose ordering is not guaranteed) in a toString() method.
   * Ideally, sets should be sorted deterministically in *some* way so that toString() is deterministic.
   * Otherwise, just print in whatever order the set will get printed, and then use this looser test to see
   * what the toString() output would look like.
   */
  public static void assertStringContainsAll(String expectedSuperset, RBSet<String> infixes) {
    assertTrue(infixes.asSet()
        .stream()
        .allMatch(infix -> expectedSuperset.contains(infix)));
  }

  public static void assertThrows(Class<? extends Exception> expectedExceptionClass, Runnable runnable) {
    assertPossiblyThrows(true, expectedExceptionClass, runnable);
  }

  public static void assertPossiblyThrows(
      boolean shouldThrow, Class<? extends Exception> expectedExceptionClass, Runnable runnable) {
    if (!shouldThrow) {
      runnable.run();
      return;
    }
    try {
      runnable.run();
    } catch (Exception exception) {
      if (exception.getClass().equals(expectedExceptionClass)) {
        return;
      } else {
        fail(Strings.format("Expected exception of type %s ; got an exception, but of type %s",
            expectedExceptionClass, exception.getClass()));
      }
    }
    fail(Strings.format("Expected exception of type %s ; got no exception", expectedExceptionClass));
  }

  public static void assertThrowsWithMessage(String expectedMessage, Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      assertEquals(
          Strings.format("Expected exception with message %s ; got %s (type %s )",
              expectedMessage, e.getMessage(), e.getClass()),
          expectedMessage,
          e.getMessage());
      return;
    }
    fail(Strings.format("Expected exception with message %s ; got no exception", expectedMessage));
  }

  public static double doubleExplained(double expected, double actualFirst, double...actualRest) {
    return doubleApproximatelyExplained(1e-8, expected, actualFirst, actualRest);
  }

  public static double doubleApproximatelyExplained(double epsilon, double expected, double actualFirst, double...actualRest) {
    List<Double> actual = concatenateFirstAndRestDoubles(actualFirst, actualRest);
    for (int i = 0; i < actual.size(); i++) {
      assertEquals(
          Strings.format(
              "You have a mistake in your calculations (hopefully not in the test itself: expected= %s ; actual[%s]= %s",
              expected, i, actual.get(i)),
          expected, actual.get(i), epsilon);
    }
    return expected;
  }

  public static int intExplained(int expected, int actual) {
    assertEquals(
        Strings.format(
            "You have a mistake in your calculations (hopefully not in the test itself: expected= %s ; actual= %s",
            expected, actual),
        expected, actual);
    return expected;
  }

  public static long longExplained(long expected, long actual) {
    assertEquals(
        Strings.format(
            "You have a mistake in your calculations (hopefully not in the test itself: expected= %s ; actual= %s",
            expected, actual),
        expected, actual);
    return expected;
  }

  public static <T> T valueExplained(T expected, T actualFirst, T ... actualRest) {
    List<T> actuals = concatenateFirstAndRest(actualFirst, actualRest);
    for (int i = 0; i < actuals.size(); i++) {
      assertEquals(
          Strings.format(
              "You have a mistake in your calculations (hopefully not in the test itself: expected= %s ; actual[ %s ]= %s",
              expected, i, actuals.get(i)),
          expected, actuals.get(i));
    }
    return expected;
  }

  // Like valueExplained, but for cases where we can't rely on #equals, and we have to use a matcher.
  public static <T> T valueExplainedByMatcher(T expected, TypeSafeMatcher<T> matcher) {
    assertThat(
        Strings.format(
            "You have a mistake in your calculations (hopefully not in the test itself) for value= %s", expected),
        expected,
        matcher);
    return expected;
  }

}
