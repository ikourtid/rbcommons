package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.vectorspaces.IsArrayIndex;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.emptySimpleArrayIndexMapping;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * It's weird to have a test for an interface, but it's the natural place to put the matcher.
 * It's possible to have a matcher on the interface itself, as it exposes everything we need to check for
 * 'matching equality'.
 */
public class ArrayIndexMappingTest {

  public static <T> TypeSafeMatcher<ArrayIndexMapping<T>> arrayIndexMappingMatcher(
      ArrayIndexMapping<T> expected, MatcherGenerator<T> matcherGenerator) {
    return new TypeSafeMatcher<ArrayIndexMapping<T>>() {
      @Override
      protected boolean matchesSafely(ArrayIndexMapping<T> actual) {
        if (expected.size() != actual.size()) {
          return false;
        }
        return IntStream.range(0, expected.size())
            .allMatch(i -> matcherGenerator.apply(expected.getKey(i)).matches(actual.getKey(i)));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("ArrayIndexMapping expected: %s", expected));
      }
    };
  }

  @Test
  public void testIsTrivialIdentityMapping() {
    ImmutableList.of(
            emptySimpleArrayIndexMapping(),    // unknown type
            simpleArrayIndexMapping("a"),      // wrong type
            simpleArrayIndexMapping("a", "b"), // wrong type
            simpleArrayIndexMapping(0L),       // wrong type (long, not int)
            simpleArrayIndexMapping(0L, 1L),   // wrong type (long, not int)
            simpleArrayIndexMapping(0),        // wrong type (must implement IsArrayIndex)
            simpleArrayIndexMapping(0, 1),
            simpleArrayIndexMapping(0, 1, 2),
            simpleArrayIndexMapping(1),        // wrong type (int), plus should start with 0
            simpleArrayIndexMapping(-1),       // wrong type (int), plus should start with 0
            simpleArrayIndexMapping(1, 0),     // wrong type (int), plus should start with 0
            simpleArrayIndexMapping(-1, 0),    // wrong type (int), plus should start with 0
            simpleArrayIndexMapping(0, 2),     // wrong type (int), plus not consecutive ints starting with 0
            simpleArrayIndexMapping(0, -2),    // wrong type (int), plus not consecutive ints starting with 0
            simpleArrayIndexMapping(0, -1),    // wrong type (int), plus not consecutive ints starting with 0

            // The following use the correct type, but are invalid for other reasons (per comments):
            simpleArrayIndexMapping(matrixColumnIndex( 1)),                          // should start with 0
            simpleArrayIndexMapping(matrixColumnIndex( 0),  matrixColumnIndex( 0)),  // should be increasing
            simpleArrayIndexMapping(matrixColumnIndex( 1),  matrixColumnIndex( 0)),  // should start with 0
            simpleArrayIndexMapping(matrixColumnIndex( 0),  matrixColumnIndex( 2)),  // not consecutive ints starting with 0
            simpleArrayIndexMapping(matrixColumnIndex( 0),  matrixRowIndex(    1)),  // mismatched types
            simpleArrayIndexMapping(matrixRowIndex(    0),  matrixColumnIndex( 1)))  // mismatched types
        .forEach(arrayIndexMapping ->
            assertFalse(arrayIndexMapping.isTrivialIdentityMapping()));

    rbSetOf(
        simpleArrayIndexMapping(matrixColumnIndex(0)),
        simpleArrayIndexMapping(matrixColumnIndex(0), matrixColumnIndex(1)),
        simpleArrayIndexMapping(matrixColumnIndex(0), matrixColumnIndex(1), matrixColumnIndex(2)))
        .forEach(arrayIndexMapping ->
            assertTrue(arrayIndexMapping.isTrivialIdentityMapping()));

    rbSetOf(
        simpleArrayIndexMapping(matrixRowIndex(0)),
        simpleArrayIndexMapping(matrixRowIndex(0), matrixRowIndex(1)),
        simpleArrayIndexMapping(matrixRowIndex(0), matrixRowIndex(1), matrixRowIndex(2)))
        .forEach(arrayIndexMapping ->
            assertTrue(arrayIndexMapping.isTrivialIdentityMapping()));
  }

}
