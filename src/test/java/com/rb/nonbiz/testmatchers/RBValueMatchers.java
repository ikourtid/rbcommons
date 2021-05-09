package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.RBNumeric;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;

import static com.rb.nonbiz.testmatchers.LambdaSwitchCase.lambdaCase;
import static com.rb.nonbiz.testmatchers.LambdaSwitchCase.lambdaSwitchMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.PreciseValue.bigDecimalsAlmostEqual;
import static junit.framework.TestCase.assertTrue;

public class RBValueMatchers {

  public static <T extends RBNumeric<? super T>> TypeSafeMatcher<T> rbNumericMatcher(T expected, double epsilon) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.doubleValue(), epsilon));
  }

  public static TypeSafeMatcher<BigDecimal> bigDecimalMatcher(BigDecimal expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return new TypeSafeMatcher<BigDecimal>() {
      @Override
      protected boolean matchesSafely(BigDecimal actual) {
        return bigDecimalsAlmostEqual(expected, actual, epsilon);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

  public static <V extends PreciseValue<? super V>> TypeSafeMatcher<V> preciseValueMatcher(V expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return new TypeSafeMatcher<V>() {
      @Override
      protected boolean matchesSafely(V actual) {
        return expected.almostEquals(actual, epsilon);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

  public static <V extends ImpreciseValue<? super V>> TypeSafeMatcher<V> impreciseValueMatcher(V expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return new TypeSafeMatcher<V>() {
      @Override
      protected boolean matchesSafely(V actual) {
        return expected.almostEquals(actual, epsilon);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

  public static <T extends RBNumeric<? super T>> TypeSafeMatcher<T> rbNumericValueMatcher(T expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return new TypeSafeMatcher<T>() {
      @Override
      protected boolean matchesSafely(T actual) {
        return Math.abs(expected.doubleValue() - actual.doubleValue()) <= epsilon;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

  public static TypeSafeMatcher<Double> doubleAlmostEqualsMatcher(double expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return makeMatcher(expected, actual -> Math.abs(expected - actual) <= epsilon);
  }

  public static TypeSafeMatcher<Float> floatAlmostEqualsMatcher(float expected, float epsilon) {
    assertValidEpsilon(epsilon);
    return makeMatcher(expected, actual -> Math.abs(expected - actual) <= epsilon);
  }

  /**
   * Matches two different Number objects. It will match if e.g. it's 11.0 (double) and 11 (int).
   * We use this with JsonObject because it's not always easy to specify the exact numeric types in our test data.
   *
   * Also see {@link #strictWithTypesNumberMatcher}
   */
  public static <T extends Number> TypeSafeMatcher<T> numberMatcher(T expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return makeMatcher(expected,
        match(v -> v.doubleValue(), f -> doubleAlmostEqualsMatcher(f, epsilon)));
  }

  /**
   * Matches two different Number objects, but will not match if e.g. it's 11.0 (double) and 11 (int).
   *
   * Also see {@link #numberMatcher}
   */
  public static <T extends Number> TypeSafeMatcher<T> strictWithTypesNumberMatcher(T expected, T epsilon) {
    return lambdaSwitchMatcher(expected,
        lambdaCase(v -> v instanceof Byte,    v -> v.byteValue(),   f -> makeMatcher(f, f2 -> Math.abs(f - f2) <= epsilon.byteValue())),
        lambdaCase(v -> v instanceof Double,  v -> v.doubleValue(), f -> makeMatcher(f, f2 -> Math.abs(f - f2) <= epsilon.doubleValue())),
        lambdaCase(v -> v instanceof Float,   v -> v.floatValue(),  f -> makeMatcher(f, f2 -> Math.abs(f - f2) <= epsilon.floatValue())),
        lambdaCase(v -> v instanceof Integer, v -> v.intValue(),    f -> makeMatcher(f, f2 -> Math.abs(f - f2) <= epsilon.intValue())),
        lambdaCase(v -> v instanceof Long,    v -> v.longValue(),   f -> makeMatcher(f, f2 -> Math.abs(f - f2) <= epsilon.longValue())),
        lambdaCase(v -> v instanceof Short,   v -> v.shortValue(),  f -> makeMatcher(f, f2 -> Math.abs(f - f2) <= epsilon.shortValue())));
  }

  /**
   * Only added for extra clarity; the same functionality could be obtained using typeSafeEqualTo().
   */
  public static <V extends Enum<? super V>> TypeSafeMatcher<V> enumMatcher(V expected) {
    return typeSafeEqualTo(expected);
  }

  /**
   * hamcrest provides an equalTo, but it's a plain Matcher, not a TypeSafeMatcher
   */
  public static <T> TypeSafeMatcher<T> typeSafeEqualTo(T expected) {
    return makeMatcher(expected, actual -> expected.equals(actual));
  }

  /**
   * hamcrest provides #same, but it's a plain Matcher, not a TypeSafeMatcher
   */
  public static <T> TypeSafeMatcher<T> typeSafeSame(T expected) {
    return makeMatcher(expected, actual -> expected == actual);
  }

  public static <T> TypeSafeMatcher<T> typeSafeAlwaysTrue() {
    return new TypeSafeMatcher<T>() {
      @Override
      protected boolean matchesSafely(T actual) {
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Always true");
      }
    };
  }

  public static <T> TypeSafeMatcher<Class<T>> classMatcher(Class<T> expected) {
    return makeMatcher(expected, actual -> expected.getClass().equals(actual.getClass()));
  }

  /**
   * For any two objects, performs an epsilon comparison if both are numeric (implement Number),
   * otherwise just tests for equality.
   */
  public static <T> TypeSafeMatcher<T> epsilonForNumericsElseEqualityMatcher(T expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return makeMatcher(expected, actual -> {
      boolean expectedIsNumber = expected instanceof Number;
      boolean actualIsNumber = actual instanceof Number;
      if (expectedIsNumber && actualIsNumber) {
        double expectedNum = ((Number) expected).doubleValue();
        double actualNum = ((Number) actual).doubleValue();
        return Math.abs(expectedNum - actualNum) <= epsilon;
      } else if (expectedIsNumber || actualIsNumber) { // can't have only one of them; that's an automatic non-match
        return false;
      }
      boolean expectedIsPreciseValue = expected instanceof PreciseValue;
      boolean actualIsPreciseValue = actual instanceof PreciseValue;
      if (expectedIsPreciseValue && actualIsPreciseValue) {
        double expectedNum = ((PreciseValue<?>) expected).doubleValue();
        double actualNum = ((PreciseValue<?>) actual).doubleValue();
        return Math.abs(expectedNum - actualNum) <= epsilon;
      } else if (expectedIsPreciseValue || actualIsPreciseValue) { // can't have only one of them; that's an automatic non-match
        return false;
      }
      boolean expectedIsImpreciseValue = expected instanceof ImpreciseValue;
      boolean actualIsImpreciseValue = actual instanceof ImpreciseValue;
      if (expectedIsImpreciseValue && actualIsImpreciseValue) {
        double expectedNum = ((ImpreciseValue<?>) expected).doubleValue();
        double actualNum = ((ImpreciseValue<?>) actual).doubleValue();
        return Math.abs(expectedNum - actualNum) <= epsilon;
      } else if (expectedIsImpreciseValue || actualIsImpreciseValue) { // can't have only one of them; that's an automatic non-match
        return false;
      }
      return expected.equals(actual);
    });
  }

  public static void assertValidEpsilon(double epsilon) {
    // This is useful to avoid typos such as 1-8 and 1e8 (instead of the usual 1e-8)
    assertTrue(
        Strings.format("It is very likely you had a typo when typing the epsilon of %s", epsilon),
        epsilon >= 0 && epsilon < 1_000);
  }

}
