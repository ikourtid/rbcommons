package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.sequence.ArithmeticProgression.ArithmeticProgressionBuilder.arithmeticProgressionBuilder;
import static com.rb.nonbiz.math.sequence.ArithmeticProgression.singleValueArithmeticProgression;
import static com.rb.nonbiz.math.sequence.Sequences.transformedSequence;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.junit.Assert.assertEquals;

public class ArithmeticProgressionTest extends RBTestMatcher<ArithmeticProgression> {

  public static ArithmeticProgression allZeroesArithmeticProgression() {
    return singleValueArithmeticProgression(0);
  }

  /**
   * Returns a sequence with 0, 1, 2, etc. This is especially useful if you want to construct a Sequence of other
   * number-like items, like PreciseValue (Money etc.), external gains by term, etc.
   */
  public static ArithmeticProgression naturalNumbersAsArithmeticProgression() {
    return arithmeticProgressionBuilder()
        .setInitialValue(0)
        .setCommonDifference(1)
        .build();
  }

  /**
   * If prefix is e.g. "xyz", this returns xyz0, xyz1, xyz2, etc.
   */
  public static Sequence<String> sequenceOfPrefixedNaturalNumbers(String prefix) {
    return transformedSequence(
        naturalNumbersAsArithmeticProgression(),
        i -> Strings.format("%s%s", prefix, i.intValue()));
  }

  @Test
  public void testGet() {
    ArithmeticProgression arithmeticProgression = arithmeticProgressionBuilder()
        .setInitialValue(1.1)
        .setCommonDifference(0.5)
        .build();
    assertIllegalArgumentException( () -> arithmeticProgression.get(-999));
    assertIllegalArgumentException( () -> arithmeticProgression.get(-1));
    assertEquals(1.1, arithmeticProgression.get(0), 1e-8);
    assertEquals(1.6, arithmeticProgression.get(1), 1e-8);
    assertEquals(2.1, arithmeticProgression.get(2), 1e-8);

    assertEquals(
        doubleExplained(500_001.1, 1.1 + 0.5 * 1_000_000),
        arithmeticProgression.get(1_000_000),
        1e-8);
  }

  @Override
  public ArithmeticProgression makeTrivialObject() {
    return allZeroesArithmeticProgression();
  }

  @Override
  public ArithmeticProgression makeNontrivialObject() {
    return arithmeticProgressionBuilder()
        .setInitialValue(-1.1)
        .setCommonDifference(-3.3)
        .build();
  }

  @Override
  public ArithmeticProgression makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return arithmeticProgressionBuilder()
        .setInitialValue(-1.1 + e)
        .setCommonDifference(-3.3 + e)
        .build();
  }

  @Override
  protected boolean willMatch(ArithmeticProgression expected, ArithmeticProgression actual) {
    return arithmeticProgressionMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ArithmeticProgression> arithmeticProgressionMatcher(ArithmeticProgression expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getInitialValue(),     DEFAULT_EPSILON_1e_8),
        matchUsingDoubleAlmostEquals(v -> v.getCommonDifference(), DEFAULT_EPSILON_1e_8));
  }

}
