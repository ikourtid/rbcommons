package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.collections.Either;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;

import java.util.stream.Stream;

import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;

/**
 * Test infrastructure for comparing {@link Stream} objects using {@link TypeSafeMatcher}s.
 *
 * <p> Of course, this requires 'consuming' the stream - but in tests, that's almost always OK. </p>
 */
public class RBStreamMatchers {

  /**
   * Obviously this 'consumes' the stream, so be mindful.
   */
  public static <T> TypeSafeMatcher<Stream<T>> streamMatcher(Stream<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected, actual ->
        iteratorMatcher(expected.iterator(), matcherGenerator).matches(actual.iterator()));
  }

  /**
   * Obviously this 'consumes' the stream, so be mindful.
   */
  public static <T> TypeSafeMatcher<Stream<T>> streamEqualityMatcher(Stream<T> expected) {
    return streamMatcher(expected, f -> typeSafeEqualTo(f));
  }

  /**
   * Obviously this 'consumes' the stream, so be mindful.
   */
  public static <T> TypeSafeMatcher<Stream<Double>> doubleStreamMatcher(Stream<Double> expected, Epsilon epsilon) {
    return streamMatcher(expected, f -> doubleAlmostEqualsMatcher(f, epsilon));
  }

}
