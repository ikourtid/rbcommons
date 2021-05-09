package com.rb.nonbiz.functional;

import com.rb.nonbiz.functional.AllowsMissingValues.Visitor;
import com.rb.nonbiz.functional.AllowsMissingValuesTest.TestAllowsMissingValues;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.optionalMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;

public class AllowsMissingValuesTest extends RBTestMatcher<TestAllowsMissingValues> {


  // A simple test data class that implements AllowsMissingValues so we can use it in tests.
  public static class TestAllowsMissingValues implements AllowsMissingValues<Double> {

    private final OptionalDouble rawOptional;

    // Callers will use a non-static constructor which is quite non-standard
    // in our codebase, so as to call attention to the fact that this is a test data class.
    public TestAllowsMissingValues(OptionalDouble rawOptional) {
      this.rawOptional = rawOptional;
    }

    @Override
    public <T> T visit(Visitor<T, Double> visitor) {
      return rawOptional.isPresent()
          ? visitor.visitPresentValue(rawOptional.getAsDouble())
          : visitor.visitMissingValue();
    }

  }

  @Override
  public TestAllowsMissingValues makeTrivialObject() {
    return new TestAllowsMissingValues(OptionalDouble.empty());
  }

  @Override
  public TestAllowsMissingValues makeNontrivialObject() {
    return new TestAllowsMissingValues(OptionalDouble.of(1.23));
  }

  @Override
  public TestAllowsMissingValues makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return new TestAllowsMissingValues(OptionalDouble.of(1.23 + e));
  }

  @Override
  protected boolean willMatch(TestAllowsMissingValues expected, TestAllowsMissingValues actual) {
    return allowsMissingValuesMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<AllowsMissingValues<T>> allowsMissingValuesMatcher(
      AllowsMissingValues<T> expected, MatcherGenerator<T> valueMatcherGenerator) {
    // generalVisitorMatcher is a pain normally, but it's even more of a pain here, so this is the least bad option.
    Function<AllowsMissingValues<T>, Optional<T>> toOptionalTransformer = v -> v.visit(new Visitor<Optional<T>, T>() {
      @Override
      public Optional<T> visitPresentValue(T presentValue) {
        return Optional.of(presentValue);
      }

      @Override
      public Optional<T> visitMissingValue() {
        return Optional.empty();
      }
    });

    return makeMatcher(expected, actual ->
        optionalMatcher(toOptionalTransformer.apply(expected), valueMatcherGenerator)
            .matches(toOptionalTransformer.apply(actual)));
  }


}
