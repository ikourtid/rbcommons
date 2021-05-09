package com.rb.nonbiz.testmatchers;

import com.google.common.collect.Range;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.RBNumeric;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.number.IsCloseTo;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.ClosedRangeTest.closedRangeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.rbNumericValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;

public class RBRangeMatchers {

  public static TypeSafeMatcher<Range<Double>> doubleRangeMatcher(Range<Double> expected, double epsilon) {
    return rangeMatcher(expected, v -> new IsCloseTo(v, epsilon));
  }

  public static TypeSafeMatcher<ClosedRange<Double>> doubleClosedRangeMatcher(ClosedRange<Double> expected, double epsilon) {
    return closedRangeMatcher(expected, v -> new IsCloseTo(v, epsilon));
  }

  public static <T extends Comparable<? super T>> TypeSafeMatcher<Range<T>> rangeMatcher(Range<T> expected,
                                                                              MatcherGenerator<T> itemMatcherGenerator) {
    return new TypeSafeMatcher<Range<T>>() {
      @Override
      protected boolean matchesSafely(Range<T> actual) {
        if ((expected.hasLowerBound() && !actual.hasLowerBound()) ||
            (!expected.hasLowerBound() && actual.hasLowerBound())) {
          return false;
        }
        if (expected.hasLowerBound() && actual.hasLowerBound()) {
          if (expected.lowerBoundType() != actual.lowerBoundType()) {
            return false;
          }
          if (!itemMatcherGenerator.apply(expected.lowerEndpoint()).matches(actual.lowerEndpoint())) {
            return false;
          }
        }

        if ((expected.hasUpperBound() && !actual.hasUpperBound()) ||
            (!expected.hasUpperBound() && actual.hasUpperBound())) {
          return false;
        }
        if (expected.hasUpperBound() && actual.hasUpperBound()) {
          if (expected.upperBoundType() != actual.upperBoundType()) {
            return false;
          }
          if (!itemMatcherGenerator.apply(expected.upperEndpoint()).matches(actual.upperEndpoint())) {
            return false;
          }
        }

        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected range %s", expected));
      }
    };
  }

  public static <T extends Comparable<? super T>> TypeSafeMatcher<Range<T>> rangeEqualityMatcher(Range<T> expected) {
    return rangeMatcher(expected, v -> typeSafeEqualTo(v));
  }

  public static <T extends PreciseValue<? super T>> TypeSafeMatcher<Range<T>> preciseValueRangeMatcher(
      Range<T> expected, double epsilon) {
    return rangeMatcher(expected, v -> preciseValueMatcher(v, epsilon));
  }

  public static <T extends PreciseValue<T>> TypeSafeMatcher<ClosedRange<T>> preciseValueClosedRangeMatcher(
      ClosedRange<T> expected, double epsilon) {
    return closedRangeMatcher(expected, v -> preciseValueMatcher(v, epsilon));
  }

  public static <T extends ImpreciseValue<? super T>> TypeSafeMatcher<Range<T>> impreciseValueRangeMatcher(
      Range<T> expected, double epsilon) {
    return rangeMatcher(expected, v -> impreciseValueMatcher(v, epsilon));
  }

  public static <T extends RBNumeric<? super T>> TypeSafeMatcher<Range<T>> rbNumericRangeMatcher(
      Range<T> expected, double epsilon) {
    return rangeMatcher(expected, v -> rbNumericValueMatcher(v, epsilon));
  }

  public static TypeSafeMatcher<Range<BigDecimal>> bigDecimalRangeMatcher(
      Range<BigDecimal> expected, double epsilon) {
    return rangeMatcher(expected, v -> bigDecimalMatcher(v, epsilon));
  }

  public static <K, V extends PreciseValue<? super V>> TypeSafeMatcher<RBMap<K, Range<V>>> rbMapPreciseValueRangeMatcher(
      RBMap<K, Range<V>> expected, double epsilon) {
    return new TypeSafeMatcher<RBMap<K, Range<V>>>() {
      @Override
      protected boolean matchesSafely(RBMap<K, Range<V>> actual) {
        if (!expected.keySet().equals(actual.keySet())) {
          return false;
        }
        for (K key : expected.keySet()) {
          Range<V> expectedRange = expected.getOrThrow(key);
          Range<V> actualRange = actual.getOrThrow(key);
          if (!preciseValueRangeMatcher(expectedRange, epsilon).matches(actualRange)) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

  public static <K> TypeSafeMatcher<RBMap<K, Range<Double>>> rbMapToDoubleRangeMatcher(
      RBMap<K, Range<Double>> expected, double epsilon) {
    return new TypeSafeMatcher<RBMap<K, Range<Double>>>() {
      @Override
      protected boolean matchesSafely(RBMap<K, Range<Double>> actual) {
        if (!expected.keySet().equals(actual.keySet())) {
          return false;
        }
        for (K key : expected.keySet()) {
          Range<Double> expectedRange = expected.getOrThrow(key);
          Range<Double> actualRange = actual.getOrThrow(key);
          if (!doubleRangeMatcher(expectedRange, epsilon).matches(actualRange)) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

}
