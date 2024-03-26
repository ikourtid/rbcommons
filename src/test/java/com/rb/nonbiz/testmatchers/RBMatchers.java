package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.text.Strings;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;

/**
 * Test infrastructure for comparing objects using {@link TypeSafeMatcher}s.
 */
public class RBMatchers {

  /** This is to allow the possibility of avoiding the annoying describeTo implementation
   * which clutters up the test code.
   */
  public abstract static class RBTypeSafeMatcher<T> extends TypeSafeMatcher<T> {

    @Override
    public void describeTo(Description description) {
      description.appendText("ignoring");
    }

  }

  public static <T> TypeSafeMatcher<T> makeMatcher(T expected, Predicate<T> predicate) {
    return new TypeSafeMatcher<T>() {
      @Override
      protected boolean matchesSafely(T actual) {
        return predicate.test(actual);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected: %s", expected));
      }
    };
  }

  /**
   * You must have at least 1 Match below; see alwaysMatchingMatcher (the logical equivalent of 0 Match objects)
   */
  @SafeVarargs
  public static <T> TypeSafeMatcher<T> makeMatcher(T expected, Match<T, ?> first, Match<T, ?>...rest) {
    return new TypeSafeMatcher<T>() {
      @Override
      protected boolean matchesSafely(T actual) {
        // This is less compact, but don't refactor it; it's easier to add breakpoints this way.
        // Also, we could have used a foreach loop, but you can't view the Match objects in the
        // debugger easily, because they are lambdas, so we will use the int-index, old style for loop variety.
        // This way you can see that e.g. it is the 3rd match that fails.
        List<Match<T, ?>> allMatches = concatenateFirstAndRest(first, rest);
        for (int i = 0; i < allMatches.size(); i++) {
          if (!allMatches.get(i).matches(expected, actual)) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected: %s", expected));
      }
    };
  }

  public static <T> TypeSafeMatcher<T> alwaysMatchingMatcher() {
    return makeSimpleMatcher(x -> true);
  }

  public static <T> TypeSafeMatcher<T> makeSimpleMatcher(Predicate<T> predicate) {
    return new RBTypeSafeMatcher<T>() {
      @Override
      protected boolean matchesSafely(T actual) {
        return predicate.test(actual);
      }
    };
  }

  @FunctionalInterface
  public interface MatcherGenerator<T> extends Function<T, TypeSafeMatcher<T>> {}

}
