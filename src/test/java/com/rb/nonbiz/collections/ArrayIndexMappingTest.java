package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.emptySimpleArrayIndexMapping;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
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
  public void testIsTrivialIdentityIntegerMapping() {
    rbSetOf(
        emptySimpleArrayIndexMapping(),    // unknown type
        simpleArrayIndexMapping("a"),      // wrong type
        simpleArrayIndexMapping("a", "b"), // wrong type
        simpleArrayIndexMapping(0L),       // wrong type (long, not int)
        simpleArrayIndexMapping(0L, 1L),   // wrong type (long, not int)
        simpleArrayIndexMapping(1),        // correct type (int), but should start with 0
        simpleArrayIndexMapping(-1),       // correct type (int), but should start with 0
        simpleArrayIndexMapping(1, 0),     // correct type (int), but should start with 0
        simpleArrayIndexMapping(-1, 0),    // correct type (int), but should start with 0
        simpleArrayIndexMapping(0, 2),     // correct type (int), but not consecutive ints starting with 0
        simpleArrayIndexMapping(0, -2),    // correct type (int), but not consecutive ints starting with 0
        simpleArrayIndexMapping(0, -1))    // correct type (int), but not consecutive ints starting with 0
        .forEach(arrayIndexMapping ->
            assertFalse(arrayIndexMapping.isTrivialIdentityIntegerMapping()));

    rbSetOf(
        simpleArrayIndexMapping(0),
        simpleArrayIndexMapping(0, 1),
        simpleArrayIndexMapping(0, 1, 2))
        .forEach(arrayIndexMapping ->
            assertTrue(arrayIndexMapping.isTrivialIdentityIntegerMapping()));
  }

}
