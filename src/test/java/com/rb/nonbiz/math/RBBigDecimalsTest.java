package com.rb.nonbiz.math;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.math.RBBigDecimals.bigDecimalAverage;
import static com.rb.nonbiz.math.RBBigDecimals.bigDecimalInvert;
import static com.rb.nonbiz.math.RBBigDecimals.bigDecimalMax;
import static com.rb.nonbiz.math.RBBigDecimals.bigDecimalMin;
import static com.rb.nonbiz.math.RBBigDecimals.epsilonCompareBigDecimals;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_BIG_DECIMAL;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.RBDoublesTest.comparisonSignVisitor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBBigDecimalsTest {

  @Test
  public void testMinAndMax() {
    ImmutableList.of(
        pair(BigDecimal.valueOf(-3.3), BigDecimal.valueOf(-3.3)),
        pair(BigDecimal.valueOf(-3.3), BigDecimal.valueOf(-2.2)),
        pair(BigDecimal.valueOf(-3.3), BigDecimal.ZERO),
        pair(BigDecimal.valueOf(-3.3), BigDecimal.ONE),
        pair(BigDecimal.valueOf(-2.2), BigDecimal.valueOf(-2.2)),
        pair(BigDecimal.valueOf(-2.2), BigDecimal.ZERO),
        pair(BigDecimal.valueOf(-2.2), BigDecimal.ONE),
        pair(BigDecimal.ZERO, BigDecimal.ZERO),
        pair(BigDecimal.ZERO, BigDecimal.ONE),
        pair(BigDecimal.ONE, BigDecimal.ONE))
        .forEach(pair -> {
          BigDecimal smaller = pair.getLeft();
          BigDecimal bigger = pair.getRight();
          assertEquals(smaller, bigDecimalMin(smaller, bigger));
          assertEquals(smaller, bigDecimalMin(bigger, smaller));
          assertEquals(bigger, bigDecimalMax(smaller, bigger));
          assertEquals(bigger, bigDecimalMax(bigger, smaller));
        });
  }

  @Test
  public void testInvert() {
    assertEquals(BigDecimal.valueOf(1.25), bigDecimalInvert(BigDecimal.valueOf(0.8)));
    assertEquals(BigDecimal.valueOf(0.8), bigDecimalInvert(BigDecimal.valueOf(1.25)));
    assertEquals(BigDecimal.ONE, bigDecimalInvert(BigDecimal.ONE));
  }

  @Test
  public void testEpsilonCompare_epsilonMustBePositiveAndSmall() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    assertIllegalArgumentException( () -> epsilonCompareBigDecimals(DUMMY_BIG_DECIMAL, DUMMY_BIG_DECIMAL, epsilon(-999), visitor));
    assertIllegalArgumentException( () -> epsilonCompareBigDecimals(DUMMY_BIG_DECIMAL, DUMMY_BIG_DECIMAL, epsilon(-1), visitor));
    assertIllegalArgumentException( () -> epsilonCompareBigDecimals(DUMMY_BIG_DECIMAL, DUMMY_BIG_DECIMAL, epsilon(-1e-9), visitor));
    assertIllegalArgumentException( () -> epsilonCompareBigDecimals(DUMMY_BIG_DECIMAL, DUMMY_BIG_DECIMAL, epsilon(0), visitor));
    assertIllegalArgumentException( () -> epsilonCompareBigDecimals(DUMMY_BIG_DECIMAL, DUMMY_BIG_DECIMAL, epsilon(10_000), visitor));
    assertIllegalArgumentException( () -> epsilonCompareBigDecimals(DUMMY_BIG_DECIMAL, DUMMY_BIG_DECIMAL, epsilon(1e9), visitor));

    assertEquals("==", epsilonCompareBigDecimals(DUMMY_BIG_DECIMAL, DUMMY_BIG_DECIMAL, epsilon(1e-8), visitor));
  }

  @Test
  public void testEpsilonCompare_generalCase() {
    EpsilonComparisonVisitor<String> visitor = comparisonSignVisitor();
    BigDecimal slightlyMoreThan10 = BigDecimal.valueOf(10 + 1e-9);
    assertEquals("<",  epsilonCompareBigDecimals(BigDecimal.ONE,     BigDecimal.TEN,     epsilon(1e-8), visitor));
    assertEquals(">",  epsilonCompareBigDecimals(BigDecimal.TEN,     BigDecimal.ONE,     epsilon(1e-8), visitor));
    assertEquals("==", epsilonCompareBigDecimals(BigDecimal.TEN,     BigDecimal.TEN,     epsilon(1e-8), visitor));
    assertEquals("==", epsilonCompareBigDecimals(slightlyMoreThan10, BigDecimal.TEN,     epsilon(1e-8), visitor));
    assertEquals("==", epsilonCompareBigDecimals(BigDecimal.TEN,     slightlyMoreThan10, epsilon(1e-8), visitor));
    assertEquals(">",  epsilonCompareBigDecimals(slightlyMoreThan10, BigDecimal.TEN,     epsilon(1e-10), visitor));
    assertEquals("<",  epsilonCompareBigDecimals(BigDecimal.TEN,     slightlyMoreThan10, epsilon(1e-10), visitor));
  }

  @Test
  public void testBigDecimalAverage() {
    assertThat(
        bigDecimalAverage(BigDecimal.ZERO, BigDecimal.ZERO),
        bigDecimalMatcher(BigDecimal.ZERO, epsilon(1e-14)));
    assertThat(
        bigDecimalAverage(BigDecimal.TEN, BigDecimal.TEN),
        bigDecimalMatcher(BigDecimal.TEN, epsilon(1e-14)));
    assertThat(
        bigDecimalAverage(BigDecimal.ZERO, BigDecimal.TEN),
        bigDecimalMatcher(BigDecimal.valueOf(5), epsilon(1e-14)));
  }

}
