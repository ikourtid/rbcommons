package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.OneOf3.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PositiveMultiplier;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.OneOf3.only1stOf3;
import static com.rb.nonbiz.collections.OneOf3.only2ndOf3;
import static com.rb.nonbiz.collections.OneOf3.only3rdOf3;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.optionalMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class OneOf3Test extends RBTestMatcher<OneOf3<Double, PositiveMultiplier, UnitFraction>> {

  @Test
  public void implementsEquals() {
    assertEquals(only1stOf3("a"),  only1stOf3("a"));
    assertEquals(only2ndOf3("a"),  only2ndOf3("a"));
    assertEquals(only3rdOf3("a"),  only3rdOf3("a"));
  }

  @Test
  public void testVisitor() {
    OneOf3.Visitor<String, Integer, Boolean, String> visitor = new Visitor<String, Integer, Boolean, String>() {
      @Override
      public String visitOnly1stOf3(String value1) {
        return Strings.format("v1_%s", value1);
      }

      @Override
      public String visitOnly2ndOf3(Integer value2) {
        return Strings.format("v2_%s", value2);
      }

      @Override
      public String visitOnly3rdOf3(Boolean value3) {
        return Strings.format("v3_%s", value3);
      }
    };
    assertEquals(
        "v1_abc",
        OneOf3.<String, Integer, Boolean>only1stOf3("abc").visit(visitor));
    assertEquals(
        "v2_123",
        OneOf3.<String, Integer, Boolean>only2ndOf3(123).visit(visitor));
    assertEquals(
        "v3_false",
        OneOf3.<String, Integer, Boolean>only3rdOf3(false).visit(visitor));
  }

  @Test
  public void testMatcher() {
    // Normally we don't need a special method for this, but the RBTestMatcher inherited tests cannot test for
    // all scenarios, because we can't have all 3 fields existing at the same time.
    MatcherGenerator<OneOf3<Double, PositiveMultiplier, UnitFraction>> matcherGenerator =
        expected -> oneOf3Matcher(expected,
            f -> doubleAlmostEqualsMatcher(f, 1e-8),
            f -> impreciseValueMatcher(f, 1e-8),
            f -> preciseValueMatcher(f, 1e-8));
    double e = 1e-9; // epsilon
    assertThat(
        only1stOf3(0.0),
        matcherGenerator.apply(only1stOf3(e)));
    assertThat(
        only2ndOf3(positiveMultiplier(1.1)),
        matcherGenerator.apply(only2ndOf3(positiveMultiplier(1.1 + e))));
    assertThat(
        only3rdOf3(unitFraction(0.123)),
        matcherGenerator.apply(only3rdOf3(unitFraction(0.123 + e))));
  }

  @Override
  public OneOf3<Double, PositiveMultiplier, UnitFraction> makeTrivialObject() {
    return only1stOf3(0.0);
  }

  @Override
  public OneOf3<Double, PositiveMultiplier, UnitFraction> makeNontrivialObject() {
    return only2ndOf3(positiveMultiplier(1.23));
  }

  @Override
  public OneOf3<Double, PositiveMultiplier, UnitFraction> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return only2ndOf3(positiveMultiplier(1.23 + e));
  }

  @Override
  protected boolean willMatch(OneOf3<Double, PositiveMultiplier, UnitFraction> expected,
                              OneOf3<Double, PositiveMultiplier, UnitFraction> actual) {
    return oneOf3Matcher(expected,
        f -> doubleAlmostEqualsMatcher(f, 1e-8),
        f -> impreciseValueMatcher(f, 1e-8),
        f -> preciseValueMatcher(f, 1e-8))
        .matches(actual);
  }

  // This would otherwise live in RBEitherMatchers, but it uses #getValue1 etc for simplicity,
  // and we can't access those outside the package, including in RBEitherMatchers. We can access them, however,
  // in the corresponding test file (i.e. here).
  public static <T1, T2, T3> TypeSafeMatcher<OneOf3<T1, T2, T3>> oneOf3EqualityMatcher(
      OneOf3<T1, T2, T3> expected) {
    return oneOf3Matcher(expected,
        f -> typeSafeEqualTo(f),
        f -> typeSafeEqualTo(f),
        f -> typeSafeEqualTo(f));
  }

  // This would otherwise live in RBEitherMatchers, but it uses #getValue1 etc for simplicity,
  // and we can't access those outside the package, including in RBEitherMatchers. We can access them, however,
  // in the corresponding test file (i.e. here).
  public static <T1, T2, T3> TypeSafeMatcher<OneOf3<T1, T2, T3>> oneOf3Matcher(
      OneOf3<T1, T2, T3> expected,
      MatcherGenerator<T1> value1MatcherGenerator,
      MatcherGenerator<T2> value2MatcherGenerator,
      MatcherGenerator<T3> value3MatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getValue1(), f -> optionalMatcher(f, value1MatcherGenerator)),
        match(v -> v.getValue2(), f -> optionalMatcher(f, value2MatcherGenerator)),
        match(v -> v.getValue3(), f -> optionalMatcher(f, value3MatcherGenerator)));
  }

}
