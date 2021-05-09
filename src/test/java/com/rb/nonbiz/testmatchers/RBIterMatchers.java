package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import org.hamcrest.TypeSafeMatcher;

import java.util.Iterator;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;

public class RBIterMatchers {

  /**
   * Obviously this 'consumes' the iterator, so be mindful.
   */
  public static <T> TypeSafeMatcher<Iterator<T>> iteratorMatcher(Iterator<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected, actual -> {
      while (true) {
        if (!expected.hasNext() && !actual.hasNext()) {
          return true;
        }
        if (!expected.hasNext() || !actual.hasNext()) {
          return false;
        }
        T expectedItem = expected.next();
        T actualItem = actual.next();
        if (!matcherGenerator.apply(expectedItem).matches(actualItem)) {
          return false;
        }
      }
    });
  }

  /**
   * Obviously this 'consumes' the iterator, so be mindful.
   */
  public static <T> TypeSafeMatcher<Iterator<T>> iteratorEqualityMatcher(Iterator<T> expected) {
    return iteratorMatcher(expected, f -> typeSafeEqualTo(f));
  }

  /**
   * Obviously this 'consumes' the iterator, so be mindful.
   */
  public static <T> TypeSafeMatcher<Iterator<Double>> doubleIteratorMatcher(Iterator<Double> expected, double epsilon) {
    return iteratorMatcher(expected, f -> doubleAlmostEqualsMatcher(f, epsilon));
  }

}
