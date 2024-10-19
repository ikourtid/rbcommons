package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.Deviations.deviations;
import static com.rb.nonbiz.collections.Deviations.emptyDeviations;
import static com.rb.nonbiz.collections.DeviationsTest.deviationsMatcher;
import static com.rb.nonbiz.collections.NonZeroDeviations.emptyNonZeroDeviations;
import static com.rb.nonbiz.collections.NonZeroDeviations.nonZeroDeviations;
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
import static org.junit.Assert.assertTrue;

/**
 * This test class is not generic, but the publicly exposed test matcher is.
 */
public class NonZeroDeviationsTest extends RBTestMatcher<NonZeroDeviations<String>> {

  @Test
  public void testMeanAbsoluteDeviation() {
    assertEquals(BigDecimal.ZERO, emptyDeviations().getMeanAbsoluteDeviation());
    assertThat(
        nonZeroDeviations(rbMapOf(
            "a", signedFraction(0.1),
            "b", signedFraction(0.5),
            "c", signedFraction(-0.6)))
            .getMeanAbsoluteDeviation(),
        bigDecimalMatcher(BigDecimal.valueOf(doubleExplained(0.4, (0.1 + 0.5 + 0.6) / 3)), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testTotalAbsoluteDeviation() {
    assertEquals(BigDecimal.ZERO, emptyDeviations().getMeanAbsoluteDeviation());
    assertThat(
        nonZeroDeviations(rbMapOf(
            "a", signedFraction(0.1),
            "b", signedFraction(0.5),
            "c", signedFraction(-0.6)))
            .getTotalAbsoluteDeviation(),
        bigDecimalMatcher(BigDecimal.valueOf(doubleExplained(1.2, 0.1 + 0.5 + 0.6)), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testMeanSquaredDeviation() {
    assertEquals(BigDecimal.ZERO, emptyDeviations().getMeanAbsoluteDeviation());
    assertThat(
        nonZeroDeviations(rbMapOf(
            "a", signedFraction(0.1),
            "b", signedFraction(0.5),
            "c", signedFraction(-0.6)))
            .getMeanSquaredDeviation(),
        bigDecimalMatcher(BigDecimal.valueOf(doubleExplained(0.206666667, (0.1 * 0.1 + 0.5 * 0.5 + 0.6 * 0.6) / 3)), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void weightsDoNotSumToZero_throws() {
    assertIllegalArgumentException( () -> nonZeroDeviations(singletonRBMap("a", signedFraction(0.1))));
    assertIllegalArgumentException( () -> nonZeroDeviations(singletonRBMap("a", signedFraction(-0.1))));

    assertIllegalArgumentException( () -> nonZeroDeviations(rbMapOf(
        "a", signedFraction(0.1),
        "b", signedFraction(-0.11))));
    assertIllegalArgumentException( () -> nonZeroDeviations(rbMapOf(
        "a", signedFraction(0.11),
        "b", signedFraction(-0.1))));
    assertIllegalArgumentException( () -> nonZeroDeviations(rbMapOf(
        "a", signedFraction(0.11),
        "b", signedFraction(0.4),
        "c", signedFraction(-0.5))));
    assertIllegalArgumentException( () -> nonZeroDeviations(rbMapOf(
        "a", signedFraction(0.1),
        "b", signedFraction(0.4),
        "c", signedFraction(-0.51))));
  }

  @Test
  public void weightsSumToAlmostZero_isValid() {
    for (double epsilon : rbSetOf(-1e-9, 1e-9)) {
      NonZeroDeviations<String> doesNotThrow = nonZeroDeviations(rbMapOf(
          "a", signedFraction(0.1 + epsilon),
          "b", signedFraction(0.4),
          "c", signedFraction(-0.5)));
    }
  }

  @Test
  public void emptyNonZeroDeviations_isValid() {
    NonZeroDeviations<String> doesNotThrow = emptyNonZeroDeviations();
    assertTrue(doesNotThrow.isEmpty());
  }

  @Test
  public void hasZeroFractions_throws() {
    assertIllegalArgumentException( () -> nonZeroDeviations(singletonRBMap("a", SIGNED_FRACTION_0)));
    assertIllegalArgumentException( () -> nonZeroDeviations(rbMapOf(
        "a", signedFraction(0.1),
        "b", SIGNED_FRACTION_0,
        "c", signedFraction(-0.1))));
  }

  @Test
  public void testToDeviations() {
    assertThat(
        nonZeroDeviations(rbMapOf(
            "a", signedFraction(0.1),
            "b", signedFraction(0.4),
            "c", signedFraction(-0.5)))
        .toDeviations(),
        deviationsMatcher(
            deviations(rbMapOf(
                "a", signedFraction(0.1),
                "b", signedFraction(0.4),
                "c", signedFraction(-0.5))),
            DEFAULT_EPSILON_1e_8));
    assertThat(
        emptyNonZeroDeviations().toDeviations(),
        deviationsMatcher(emptyDeviations(), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testToDeviationsOrThrow_throwsIfZeroesExist() {

  }

  @Override
  public NonZeroDeviations<String> makeTrivialObject() {
    return nonZeroDeviations(rbMapOf(
        "a", signedFraction(1.0),
        "b", signedFraction(-1.0)));
  }

  @Override
  public NonZeroDeviations<String> makeNontrivialObject() {
    return nonZeroDeviations(rbMapOf(
        "a", signedFraction(0.1),
        "b", signedFraction(0.4),
        "c", signedFraction(-0.5)));
  }

  @Override
  public NonZeroDeviations<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    // Using -2 * e below so that all signed fractions still sum to 0
    return nonZeroDeviations(rbMapOf(
        "a", signedFraction(0.1 + e),
        "b", signedFraction(0.4 + e),
        "c", signedFraction(-0.5 - 2 * e)));
  }

  @Override
  protected boolean willMatch(NonZeroDeviations<String> expected, NonZeroDeviations<String> actual) {
    return nonZeroDeviationsMatcher(expected, DEFAULT_EPSILON_1e_8).matches(actual);
  }

  public static <K> TypeSafeMatcher<NonZeroDeviations<K>> nonZeroDeviationsMatcher(
      NonZeroDeviations<K> expected, Epsilon epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawSignedFractionsMap(), f -> rbMapPreciseValueMatcher(f, epsilon)));
  }

}
