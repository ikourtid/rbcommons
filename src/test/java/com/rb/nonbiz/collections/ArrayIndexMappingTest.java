package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.stream.IntStream;

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

}
