package com.rb.nonbiz.types;

import com.rb.biz.investing.quality.InvestedCashScore;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.biz.investing.quality.InvestedCashScore.BEST_INVESTED_CASH_SCORE;
import static com.rb.biz.investing.quality.InvestedCashScore.investedCashScore;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Score.BEST_SCORE;
import static com.rb.nonbiz.types.Score.WORST_SCORE;
import static com.rb.nonbiz.types.Score.score;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// Using InvestedCashScore even though Score is generic,
// because the test methods at the end actually have to instantiate SOME concrete score.
// But the static TypeSafeMatcher is still generic.
public class ScoreTest extends RBTestMatcher<InvestedCashScore>  {

  @Test
  public void mustBeBetween0and1inclusive_doubleConstructor() {
    assertIllegalArgumentException( () -> score(-1e-8));
    Score doesNotThrow1 = score(0);
    Score doesNotThrow2 = score(0.5);
    Score doesNotThrow3 = score(1);
    assertIllegalArgumentException( () -> score(1 + 1e-8));
  }

  @Test
  public void mustBeBetween0and1inclusive_bigDecimalConstructor() {
    assertIllegalArgumentException( () -> score(BigDecimal.valueOf(-1e-8)));
    Score doesNotThrow1 = score(BigDecimal.valueOf(0));
    Score doesNotThrow2 = score(BigDecimal.valueOf(0.5));
    Score doesNotThrow3 = score(BigDecimal.valueOf(1));
    assertIllegalArgumentException( () -> score(BigDecimal.valueOf(1 + 1e-8)));
  }

  @Test
  public void equalitiesAtMin() {
    assertEquals(WORST_SCORE, score(0));
    assertEquals(WORST_SCORE, score(BigDecimal.ZERO));
    assertEquals(WORST_SCORE, WORST_SCORE);
    assertEquals(score(0), WORST_SCORE);
    assertEquals(score(BigDecimal.ZERO), WORST_SCORE);
  }

  @Test
  public void equalitiesAtMax() {
    assertEquals(BEST_SCORE, score(1));
    assertEquals(BEST_SCORE, score(BigDecimal.ONE));
    assertEquals(BEST_SCORE, BEST_SCORE);
    assertEquals(score(1), BEST_SCORE);
    assertEquals(score(BigDecimal.ONE), BEST_SCORE);
  }

  @Test
  public void comparisonAtMin() {
    assertEquals(0, WORST_SCORE.compareTo(score(0)));
    assertEquals(0, WORST_SCORE.compareTo(score(BigDecimal.ZERO)));
    assertEquals(0, WORST_SCORE.compareTo(WORST_SCORE));
    assertEquals(0, score(0).compareTo(WORST_SCORE));
    assertEquals(0, score(BigDecimal.ZERO).compareTo(WORST_SCORE));
  }

  @Test
  public void comparisonAtMax() {
    assertEquals(0, BEST_SCORE.compareTo(score(1)));
    assertEquals(0, BEST_SCORE.compareTo(score(BigDecimal.ONE)));
    assertEquals(0, BEST_SCORE.compareTo(BEST_SCORE));
    assertEquals(0, score(1).compareTo(BEST_SCORE));
    assertEquals(0, score(BigDecimal.ONE).compareTo(BEST_SCORE));
  }

  @Test
  public void isMaximum_isMinimum() {
    assertFalse(score(1e-8).isWorst());
    assertTrue(score(0).isWorst());
    assertTrue(WORST_SCORE.isWorst());

    assertFalse(score(1 - 1e-8).isBest());
    assertTrue(score(1).isBest());
    assertTrue(BEST_SCORE.isBest());
  }

  @Test
  public void toAntiScore() {
    assertEquals(
        BigDecimal.valueOf(doubleExplained(300_000, (1 - 0.7) * 1_000_000)),
        score(0.7).toAntiScore());
  }

  @Override
  public InvestedCashScore makeTrivialObject() {
    return BEST_INVESTED_CASH_SCORE;
  }

  @Override
  public InvestedCashScore makeNontrivialObject() {
    return investedCashScore(0.12345);
  }

  @Override
  public InvestedCashScore makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return investedCashScore(0.12345 + e);
  }

  @Override
  protected boolean willMatch(InvestedCashScore expected, InvestedCashScore actual) {
    return scoreMatcher(expected).matches(actual);
  }

  public static <S extends Score> TypeSafeMatcher<S> scoreMatcher(S expected) {
    return preciseValueMatcher(expected, 1e-8);
  }

}
