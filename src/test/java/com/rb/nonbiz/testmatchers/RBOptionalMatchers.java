package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PreciseValue;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeSame;
import static junit.framework.TestCase.assertTrue;

public class RBOptionalMatchers {

  public static <T> TypeSafeMatcher<Optional<T>> emptyOptionalMatcher() {
    return new TypeSafeMatcher<Optional<T>>() {
      @Override
      protected boolean matchesSafely(Optional<T> actual) {
        return !actual.isPresent();
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Expected empty optional");
      }
    };
  }

  public static <T> TypeSafeMatcher<Optional<T>> nonEmptyOptionalMatcher(TypeSafeMatcher<T> itemMatcher) {
    return new TypeSafeMatcher<Optional<T>>() {
      @Override
      protected boolean matchesSafely(Optional<T> actual) {
        return actual.isPresent() && itemMatcher.matches(actual.get());
      }

      @Override
      public void describeTo(Description description) {
        itemMatcher.describeTo(description);
      }
    };
  }

  public static <T> TypeSafeMatcher<Optional<T>> optionalMatcher(Optional<T> expected,
                                                                 MatcherGenerator<T> itemMatcherGenerator) {
    return new TypeSafeMatcher<Optional<T>>() {
      @Override
      protected boolean matchesSafely(Optional<T> actual) {
        if (!expected.isPresent() && !actual.isPresent()) {
          return true; // both are Optional.empty()
        }
        if (!expected.isPresent() || !actual.isPresent()) {
          return false; // only 1 is Optional.empty()
        }
        return itemMatcherGenerator.apply(expected.get()).matches(actual.get());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Optional expected: %s", expected));
      }
    };
  }

  /**
   * This is like the same() matcher (i.e. effectively a pointer comparison),
   * except that it compares the pointers inside non-empty optionals.
   * If both optionals are empty, they will match of course.
   */
  public static <T> TypeSafeMatcher<Optional<T>> sameOptional(Optional<T> expected) {
    return optionalMatcher(expected, f -> typeSafeSame(f));
  }

  public static <T> TypeSafeMatcher<Optional<T>> sameNonEmptyOptional(T expected) {
    return sameOptional(Optional.of(expected));
  }

  public static TypeSafeMatcher<OptionalDouble> optionalDoubleMatcher(OptionalDouble expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return new TypeSafeMatcher<OptionalDouble>() {
      @Override
      protected boolean matchesSafely(OptionalDouble actual) {
        if (!expected.isPresent() && !actual.isPresent()) {
          return true; // both are Optional.empty()
        }
        if (!expected.isPresent() || !actual.isPresent()) {
          return false; // only 1 is Optional.empty()
        }
        return Math.abs(expected.getAsDouble() - actual.getAsDouble()) < epsilon;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("OptionalDouble expected: %s", expected));
      }
    };
  }

  public static TypeSafeMatcher<OptionalInt> optionalIntMatcher(OptionalInt expected) {
    return new TypeSafeMatcher<OptionalInt>() {
      @Override
      protected boolean matchesSafely(OptionalInt actual) {
        if (!expected.isPresent() && !actual.isPresent()) {
          return true; // both are Optional.empty()
        }
        if (!expected.isPresent() || !actual.isPresent()) {
          return false; // only 1 is Optional.empty()
        }
        return expected.getAsInt() == actual.getAsInt();
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("OptionalInt expected: %s", expected));
      }
    };
  }

  public static TypeSafeMatcher<OptionalInt> emptyOptionalIntMatcher() {
    return optionalIntMatcher(OptionalInt.empty());
  }

  public static <T extends PreciseValue<T>> TypeSafeMatcher<Optional<T>> optionalPreciseValueMatcher(
      Optional<T> expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return optionalMatcher(expected, v -> preciseValueMatcher(v, epsilon));
  }

  public static <T extends PreciseValue<T>> TypeSafeMatcher<Optional<T>> nonEmptyOptionalPreciseValueMatcher(T expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return new TypeSafeMatcher<Optional<T>>() {
      @Override
      protected boolean matchesSafely(Optional<T> actual) {
        return actual.isPresent() && preciseValueMatcher(expected, epsilon).matches(actual.get());
      }

      @Override
      public void describeTo(Description description) {
        preciseValueMatcher(expected, epsilon).describeTo(description);
      }
    };
  }

  public static TypeSafeMatcher<OptionalDouble> nonEmptyOptionalDoubleMatcher(double expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return new TypeSafeMatcher<OptionalDouble>() {
      @Override
      protected boolean matchesSafely(OptionalDouble actual) {
        return actual.isPresent() && doubleAlmostEqualsMatcher(expected, epsilon).matches(actual.getAsDouble());
      }

      @Override
      public void describeTo(Description description) {
        doubleAlmostEqualsMatcher(expected, epsilon).describeTo(description);
      }
    };
  }

  private static final void assertValidEpsilon(double epsilon) {
    // This is useful to avoid typos such as 1-8 and 1e8 (instead of the usual 1e-8)
    assertTrue(
        Strings.format("It is very likely you had a typo when typing the epsilon of %s", epsilon),
        epsilon >= 0 && epsilon < 1_000);
  }

}
