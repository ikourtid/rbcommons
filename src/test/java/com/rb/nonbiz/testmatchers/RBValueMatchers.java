package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.IntegerValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.RBNumeric;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;

import static com.rb.nonbiz.testmatchers.LambdaSwitchCase.lambdaCase;
import static com.rb.nonbiz.testmatchers.LambdaSwitchCase.lambdaSwitchMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.PreciseValue.bigDecimalsAlmostEqual;

public class RBValueMatchers {

  public static <T extends RBNumeric<? super T>> TypeSafeMatcher<T> rbNumericMatcher(T expected, Epsilon epsilon) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.doubleValue(), epsilon));
  }

  public static TypeSafeMatcher<BigDecimal> bigDecimalMatcher(BigDecimal expected, Epsilon epsilon) {
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

  public static <V extends PreciseValue<? super V>> TypeSafeMatcher<V> preciseValueMatcher(V expected, Epsilon epsilon) {
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

  public static <V extends ImpreciseValue<? super V>> TypeSafeMatcher<V> impreciseValueMatcher(V expected, Epsilon epsilon) {
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

  public static <T extends RBNumeric<? super T>> TypeSafeMatcher<T> rbNumericValueMatcher(T expected, Epsilon epsilon) {
    return new TypeSafeMatcher<T>() {
      @Override
      protected boolean matchesSafely(T actual) {
        return epsilon.valuesAreWithin(expected.doubleValue(), actual.doubleValue());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

  public static TypeSafeMatcher<Double> doubleAlmostEqualsMatcher(double expected, Epsilon epsilon) {
    return makeMatcher(expected, actual -> epsilon.valuesAreWithin(expected, actual));
  }

  /**
   * Although by now (Dec 2022) we have a specialized {@link ImpreciseValue} subclass to denote an epsilon,
   * the case of float is special and rare enough that we don't need a float version of {@link Epsilon}.
   */
  public static TypeSafeMatcher<Float> floatAlmostEqualsMatcher(float expected, float epsilon) {
    return makeMatcher(expected, actual -> Math.abs(expected - actual) <= epsilon);
  }

  /**
   * Matches two different Number objects. It will match if e.g. it's 11.0 (double) and 11 (int).
   * We use this with JsonObject because it's not always easy to specify the exact numeric types in our test data.
   *
   * Also see {@link #strictWithTypesNumberMatcher}
   */
  public static <T extends Number> TypeSafeMatcher<T> numberMatcher(T expected, Epsilon epsilon) {
    return makeMatcher(expected,
        match(v -> v.doubleValue(), f -> doubleAlmostEqualsMatcher(f, epsilon)));
  }

  public static <T extends IntegerValue<T>> TypeSafeMatcher<T> integerValueMatcher(T expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.intValue()));
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
   * Although we can just use typeSafeEqualTo with strings, sometimes it's convenient to use this,
   * because for strings that are very long, sometimes it's easier to debug any differences by looking at the
   * index of the first difference by putting a breakpoint below.
   */
  public static TypeSafeMatcher<String> stringMatcher(String expected) {
    return makeMatcher(expected, actual -> {
      boolean stringsAreEqual = expected.equals(actual);
      if (!stringsAreEqual && expected.length() == actual.length()) {
        for (int i = 0; i < expected.length(); i++) {
          char expectedChar = expected.charAt(i);
          char actualChar = actual.charAt(i);
          if (expectedChar != actualChar) {
            String prefixOfExpected = expected.substring(0, i);
            String prefixOfActual = expected.substring(0, i);
            // This is a convenient place to put breakpoints, plus it lets you view the variables in the debugger
            int dummy = 0;
            break;
          }
        }
      }
      return stringsAreEqual;
    });
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
  public static <T> TypeSafeMatcher<T> epsilonForNumericsElseEqualityMatcher(T expected, Epsilon epsilon) {
    return makeMatcher(expected, actual -> {
      boolean expectedIsNumber = expected instanceof Number;
      boolean actualIsNumber = actual instanceof Number;
      if (expectedIsNumber && actualIsNumber) {
        double expectedNum = ((Number) expected).doubleValue();
        double actualNum = ((Number) actual).doubleValue();
        return epsilon.valuesAreWithin(expectedNum, actualNum);
      } else if (expectedIsNumber || actualIsNumber) { // can't have only one of them; that's an automatic non-match
        return false;
      }
      boolean expectedIsPreciseValue = expected instanceof PreciseValue;
      boolean actualIsPreciseValue = actual instanceof PreciseValue;
      if (expectedIsPreciseValue && actualIsPreciseValue) {
        double expectedNum = ((PreciseValue<?>) expected).doubleValue();
        double actualNum = ((PreciseValue<?>) actual).doubleValue();
        return epsilon.valuesAreWithin(expectedNum, actualNum);
      } else if (expectedIsPreciseValue || actualIsPreciseValue) { // can't have only one of them; that's an automatic non-match
        return false;
      }
      boolean expectedIsImpreciseValue = expected instanceof ImpreciseValue;
      boolean actualIsImpreciseValue = actual instanceof ImpreciseValue;
      if (expectedIsImpreciseValue && actualIsImpreciseValue) {
        double expectedNum = ((ImpreciseValue<?>) expected).doubleValue();
        double actualNum = ((ImpreciseValue<?>) actual).doubleValue();
        return epsilon.valuesAreWithin(expectedNum, actualNum);
      } else if (expectedIsImpreciseValue || actualIsImpreciseValue) { // can't have only one of them; that's an automatic non-match
        return false;
      }
      return expected.equals(actual);
    });
  }

}
