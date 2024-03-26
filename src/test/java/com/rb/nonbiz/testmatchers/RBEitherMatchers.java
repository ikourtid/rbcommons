package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.collections.Either;
import com.rb.nonbiz.collections.Either.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Test infrastructure for comparing {@link Either} objects using {@link TypeSafeMatcher}s.
 */
public class RBEitherMatchers {

  public static <L, R> TypeSafeMatcher<Either<L, R>> eitherMatcher(
      Either<L, R> expected,
      MatcherGenerator<L> leftMatcherGenerator,
      MatcherGenerator<R> rightMatcherGenerator) {
    return new TypeSafeMatcher<Either<L, R>>() {
      @Override
      protected boolean matchesSafely(Either<L, R> actual) {
        return expected.visit(new Visitor<L, R, Boolean>() {
          @Override
          public Boolean visitLeft(L expectedLeft) {
            return actual.visit(new Visitor<L, R, Boolean>() {
              @Override
              public Boolean visitLeft(L actualLeft) {
                return leftMatcherGenerator.apply(expectedLeft).matches(actualLeft);
              }

              @Override
              public Boolean visitRight(R actualRight) {
                return false; // one either has right and the other has left
              }
            });
          }

          @Override
          public Boolean visitRight(R expectedRight) {
            return actual.visit(new Visitor<L, R, Boolean>() {
              @Override
              public Boolean visitLeft(L actualLeft) {
                return false; // one either has right and the other has left
              }

              @Override
              public Boolean visitRight(R actualRight) {
                return rightMatcherGenerator.apply(expectedRight).matches(actualRight);
              }
            });
          }
        });
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected either: %s", expected));
      }
    };
  }

  public static <L, R> TypeSafeMatcher<Either<L, R>> eitherLeftOnlyMatcher(
      L expectedLeft, MatcherGenerator<L> leftMatcherGenerator) {
    return new TypeSafeMatcher<Either<L, R>>() {
      @Override
      protected boolean matchesSafely(Either<L, R> actual) {
        return actual.visit(new Visitor<L, R, Boolean>() {
          @Override
          public Boolean visitLeft(L actualLeft) {
            return leftMatcherGenerator.apply(expectedLeft).matches(actualLeft);
          }

          @Override
          public Boolean visitRight(R actualRight) {
            return false;
          }
        });
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected either/left: %s", expectedLeft));
      }
    };
  }

  public static <L, R> TypeSafeMatcher<Either<L, R>> eitherRightOnlyMatcher(
      R expectedRight, MatcherGenerator<R> rightMatcherGenerator) {
    return new TypeSafeMatcher<Either<L, R>>() {
      @Override
      protected boolean matchesSafely(Either<L, R> actual) {
        return actual.visit(new Visitor<L, R, Boolean>() {
          @Override
          public Boolean visitLeft(L actualLeft) {
            return false;
          }

          @Override
          public Boolean visitRight(R actualRight) {
            return rightMatcherGenerator.apply(expectedRight).matches(actualRight);
          }
        });
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected either/right: %s", expectedRight));
      }
    };
  }

}
