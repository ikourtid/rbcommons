package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.Deviations.deviations;
import static com.rb.nonbiz.collections.Deviations.emptyDeviations;
import static com.rb.nonbiz.collections.NonZeroDeviations.emptyNonZeroDeviations;
import static com.rb.nonbiz.collections.NonZeroDeviations.nonZeroDeviations;
import static com.rb.nonbiz.collections.NonZeroDeviationsTest.nonZeroDeviationsMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This test class is not generic, but the publicly exposed test matcher is.
 */
public class DeviationsTest extends RBTestMatcher<Deviations<String>> {

  @Test
  public void testMeanAbsoluteDeviation() {
    assertEquals(BigDecimal.ZERO, emptyDeviations().getMeanAbsoluteDeviation());
    assertThat(
        deviations(rbMapOf(
            "a", signedFraction(0.1),
            "b", signedFraction(0.5),
            "c", signedFraction(-0.6)))
            .getMeanAbsoluteDeviation(),
        bigDecimalMatcher(BigDecimal.valueOf(doubleExplained(0.4, (0.1 + 0.5 + 0.6) / 3)), DEFAULT_EPSILON_1e_8));
    assertThat(
        deviations(rbMapOf(
            "a", signedFraction(0.1),
            "b", signedFraction(0.5),
            "c", signedFraction(-0.6),
            "d", SIGNED_FRACTION_0))
            .getMeanAbsoluteDeviation(),
        bigDecimalMatcher(BigDecimal.valueOf(doubleExplained(0.3, (0.1 + 0.5 + 0.6) / 4)), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testMeanSquaredDeviation() {
    assertEquals(BigDecimal.ZERO, emptyDeviations().getMeanAbsoluteDeviation());
    assertThat(
        deviations(rbMapOf(
            "a", signedFraction(0.1),
            "b", signedFraction(0.5),
            "c", signedFraction(-0.6)))
            .getMeanSquaredDeviation(),
        bigDecimalMatcher(BigDecimal.valueOf(doubleExplained(0.206666667, (0.1 * 0.1 + 0.5 * 0.5 + 0.6 * 0.6) / 3)), DEFAULT_EPSILON_1e_8));
    assertThat(
        deviations(rbMapOf(
            "a", signedFraction(0.1),
            "b", signedFraction(0.5),
            "c", signedFraction(-0.6),
            "d", SIGNED_FRACTION_0))
            .getMeanSquaredDeviation(),
        bigDecimalMatcher(BigDecimal.valueOf(doubleExplained(0.155, (0.1 * 0.1 + 0.5 * 0.5 + 0.6 * 0.6) / 4)), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void weightsDoNotSumToZero_throws() {
    assertIllegalArgumentException( () -> deviations(singletonRBMap("a", signedFraction(0.1))));
    assertIllegalArgumentException( () -> deviations(singletonRBMap("a", signedFraction(-0.1))));

    assertIllegalArgumentException( () -> deviations(rbMapOf(
        "a", signedFraction(0.1),
        "b", signedFraction(-0.11))));
    assertIllegalArgumentException( () -> deviations(rbMapOf(
        "a", signedFraction(0.11),
        "b", signedFraction(-0.1))));
    assertIllegalArgumentException( () -> deviations(rbMapOf(
        "a", signedFraction(0.11),
        "b", signedFraction(0.4),
        "c", signedFraction(-0.5))));
    assertIllegalArgumentException( () -> deviations(rbMapOf(
        "a", signedFraction(0.1),
        "b", signedFraction(0.4),
        "c", signedFraction(-0.51))));
  }

  @Test
  public void weightsSumToAlmostZero_isValid() {
    for (double epsilon : rbSetOf(1e-9, 1e-9)) {
      Deviations<String> doesNotThrow = deviations(rbMapOf(
          "a", signedFraction(0.1 + epsilon),
          "b", signedFraction(0.4),
          "c", signedFraction(-0.5)));
    }
  }

  @Test
  public void emptyDeviations_isValid() {
    Deviations<String> doesNotThrow = emptyDeviations();
    assertTrue(doesNotThrow.isEmpty());
  }

  @Test
  public void hasZeroFractions_isValid() {
    Deviations<String> onlyZero = deviations(singletonRBMap("a", SIGNED_FRACTION_0));
    assertFalse(onlyZero.isEmpty());
    Deviations<String> hasSomeZeroes = deviations(rbMapOf(
        "a", signedFraction(0.1),
        "b", SIGNED_FRACTION_0,
        "c", signedFraction(-0.1)));
    assertFalse(hasSomeZeroes.isEmpty());
  }

  @Test
  public void toNonZeroDeviationsOrThrow() {
    assertThat(
        deviations(rbMapOf(
            "a", signedFraction(0.1),
            "b", signedFraction(0.4),
            "c", signedFraction(-0.5)))
            .toNonZeroDeviationsOrThrow(),
        nonZeroDeviationsMatcher(
            nonZeroDeviations(rbMapOf(
                "a", signedFraction(0.1),
                "b", signedFraction(0.4),
                "c", signedFraction(-0.5))),
            DEFAULT_EPSILON_1e_8));
    assertThat(
        emptyDeviations().toNonZeroDeviationsOrThrow(),
        nonZeroDeviationsMatcher(emptyNonZeroDeviations(), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void toNonZeroDeviationsOrThrow_throwsIfDeviationsWithZeroExist() {
    for (Deviations<String> deviationsWithZero : ImmutableList.of(
        deviations(singletonRBMap("a", SIGNED_FRACTION_0)),
        deviations(rbMapOf(
            "a", signedFraction(0.1),
            "b", SIGNED_FRACTION_0,
            "c", signedFraction(-0.1))))) {
      assertIllegalArgumentException( () -> deviationsWithZero.toNonZeroDeviationsOrThrow());
    }

  }

  @Override
  public Deviations<String> makeTrivialObject() {
    return emptyDeviations();
  }

  @Override
  public Deviations<String> makeNontrivialObject() {
    return deviations(rbMapOf(
        "a", signedFraction(0.1),
        "b", signedFraction(0.4),
        "c", signedFraction(-0.5),
        "d", SIGNED_FRACTION_0));
  }

  @Override
  public Deviations<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    // Using -2 * e below so that all signed fractions still sum to 0
    return deviations(rbMapOf(
        "a", signedFraction(0.1 + e),
        "b", signedFraction(0.4 + e),
        "c", signedFraction(-0.5 - 2 * e),
        "d", SIGNED_FRACTION_0));
  }

  @Override
  protected boolean willMatch(Deviations<String> expected, Deviations<String> actual) {
    return deviationsMatcher(expected, DEFAULT_EPSILON_1e_8).matches(actual);
  }

  public static <K> TypeSafeMatcher<Deviations<K>> deviationsMatcher(Deviations<K> expected, Epsilon epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawSignedFractionsMap(), f -> rbMapPreciseValueMatcher(f, epsilon)));
  }

}
