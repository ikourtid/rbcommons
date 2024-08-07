package com.rb.nonbiz.testmatchers;

import com.google.common.base.Joiner;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.stream.IntStream;

import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;

/**
 * Test infrastructure for comparing arrays with {@link TypeSafeMatcher}s.
 */
public class RBArrayMatchers {

  public static <T> TypeSafeMatcher<T[]> arrayMatcher(T[] expected, MatcherGenerator<T> itemMatcherGenerator) {
    return new TypeSafeMatcher<T[]>() {
      @Override
      protected boolean matchesSafely(T[] actual) {
        if (expected.length != actual.length) {
          return false;
        }
        return IntStream
            .range(0, expected.length)
            .allMatch(i -> {
              // The following is un-inlined to make it easier to place breakpoints during debugging, when needed.
              boolean matches = itemMatcherGenerator.apply(expected[i]).matches(actual[i]);
              return matches;
            });
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Joiner.on(' ').join(expected));
      }
    };
  }

  public static <T> TypeSafeMatcher<T[]> arrayEqualityMatcher(T[] expected) {
    return arrayMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static TypeSafeMatcher<double[]> doubleArrayMatcher(double[] expected, Epsilon epsilon) {
    return new TypeSafeMatcher<double[]>() {
      @Override
      protected boolean matchesSafely(double[] actual) {
        if (expected.length != actual.length) {
          return false;
        }
        return IntStream
            .range(0, expected.length)
            .allMatch(i -> epsilon.valuesAreWithin(expected[i], actual[i]));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format(
            "double[] expected: %s", Strings.formatDoubleArray(expected)));
      }
    };
  }

  public static TypeSafeMatcher<int[]> intArrayMatcher(int[] expected) {
    return new TypeSafeMatcher<int[]>() {
      @Override
      protected boolean matchesSafely(int[] actual) {
        if (expected.length != actual.length) {
          return false;
        }
        return IntStream
            .range(0, expected.length)
            .allMatch(i -> expected[i] == actual[i]);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format(
            "int[] expected: %s", expected));
      }
    };
  }

  public static TypeSafeMatcher<long[]> longArrayMatcher(long[] expected) {
    return new TypeSafeMatcher<long[]>() {
      @Override
      protected boolean matchesSafely(long[] actual) {
        if (expected.length != actual.length) {
          return false;
        }
        return IntStream
            .range(0, expected.length)
            .allMatch(i -> expected[i] == actual[i]);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format(
            "long[] expected: %s", expected));
      }
    };
  }

  public static <V> TypeSafeMatcher<V[][]> array2DMatcher(V[][] expected, MatcherGenerator<V> matcherGenerator) {
    return new TypeSafeMatcher<V[][]>() {
      @Override
      protected boolean matchesSafely(V[][] actual) {
        if (expected.length != actual.length) {
          return false;
        }
        if (expected.length == 0) {
          return true;
        }
        for (int i = 0; i < expected.length; i++) {
          if (expected[i].length != actual[i].length) {
            return false;
          }
          for (int j = 0; j < expected[i].length; j++) {
            if (!matcherGenerator.apply(expected[i][j]).matches(actual[i][j])) {
              return false;
            }
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected.toString()));
      }
    };
  }

  public static <V> TypeSafeMatcher<V[][][]> array3DMatcher(V[][][] expected, MatcherGenerator<V> matcherGenerator) {
    return new TypeSafeMatcher<V[][][]>() {
      @Override
      protected boolean matchesSafely(V[][][] actual) {
        if (expected.length != actual.length) {
          return false;
        }
        for (int i = 0; i < expected.length; i++) {
          if (expected[i].length != actual[i].length) {
            return false;
          }
          for (int j = 0; j < expected[i].length; j++) {
            if (expected[i][j].length != actual[i][j].length) {
              return false;
            }
            for (int k = 0; k < expected[i][j].length; k++) {
              if (!matcherGenerator.apply(expected[i][j][k]).matches(actual[i][j][k])) {
                return false;
              }
            }
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected.toString()));
      }
    };
  }

  public static TypeSafeMatcher<double[][]> doubleArray2DMatcher(double[][] expected, Epsilon epsilon) {
    return new TypeSafeMatcher<double[][]>() {
      @Override
      protected boolean matchesSafely(double[][] actual) {
        if (expected.length != actual.length) {
          return false;
        }
        for (int i = 0; i < expected.length; i++) {
          if (expected[i].length != actual[i].length) {
            return false;
          }
          for (int j = 0; j < expected[i].length; j++) {
            if (!epsilon.valuesAreWithin(expected[i][j], actual[i][j])) {
              return false;
            }
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s",
            Arrays.deepToString(expected)
                // The following allows printing each row in a separate line
                .replace("], ", "]\n")
                .replace("[[", "[")
                .replace("]]", "]")));
      }
    };
  }

  public static TypeSafeMatcher<int[][]> intArray2DMatcher(int[][] expected) {
    return new TypeSafeMatcher<int[][]>() {
      @Override
      protected boolean matchesSafely(int[][] actual) {
        if (expected.length != actual.length) {
          return false;
        }
        for (int i = 0; i < expected.length; i++) {
          if (expected[i].length != actual[i].length) {
            return false;
          }
          for (int j = 0; j < expected[i].length; j++) {
            if (expected[i][j] != actual[i][j]) {
              return false;
            }
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s",
            Arrays.deepToString(expected)
                // The following allows printing each row in a separate line
                .replace("], ", "]\n")
                .replace("[[", "[")
                .replace("]]", "]")));
      }
    };
  }

}
