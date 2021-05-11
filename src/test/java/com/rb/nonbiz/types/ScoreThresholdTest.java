package com.rb.nonbiz.types;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.types.Score.BEST_SCORE;
import static com.rb.nonbiz.types.Score.WORST_SCORE;
import static com.rb.nonbiz.types.Score.score;
import static com.rb.nonbiz.types.ScoreThreshold.alwaysExceededThreshold;
import static com.rb.nonbiz.types.ScoreThreshold.neverExceededThreshold;
import static com.rb.nonbiz.types.ScoreThreshold.scoreThreshold;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScoreThresholdTest extends RBTestMatcher<ScoreThreshold<Score>> {

  @Test
  public void satisfies() {
    ScoreThreshold<Score> mediumThreshold = scoreThreshold(0.1);
    // The behavior at exact equality is intentionally undefined; we shouldn't be relying on it.
    // It's a comparison of continuous numbers.
    assertFalse(mediumThreshold.isExceeded(score(0.2), WORST_SCORE));
    assertFalse(mediumThreshold.isExceeded(score(0.2), score(0.2)));
    assertFalse(mediumThreshold.isExceeded(score(0.2), score(0.299)));
    assertTrue(mediumThreshold.isExceeded(score(0.2), score(0.301)));
    assertTrue(mediumThreshold.isExceeded(score(0.2), BEST_SCORE));

    assertTrue(alwaysExceededThreshold().isExceeded(score(0.2), score(0.2)));
    assertTrue(alwaysExceededThreshold().isExceeded(score(1), score(0)));
    assertTrue(alwaysExceededThreshold().isExceeded(score(0), score(1)));
    assertTrue(alwaysExceededThreshold().isExceeded(score(1), score(1)));
    assertTrue(alwaysExceededThreshold().isExceeded(score(0), score(0)));

    assertFalse(neverExceededThreshold().isExceeded(score(0.2), score(0.1)));
    assertFalse(neverExceededThreshold().isExceeded(score(0.2), score(0.2)));
    assertFalse(neverExceededThreshold().isExceeded(score(1), score(0)));
    assertFalse(neverExceededThreshold().isExceeded(score(0), score(1)));
    assertFalse(neverExceededThreshold().isExceeded(score(1), score(1)));
    assertFalse(neverExceededThreshold().isExceeded(score(0), score(0)));
  }

  @Test
  public void testAlwaysPasses() {
    assertFalse(scoreThreshold(0.123).alwaysPasses());
    assertFalse(neverExceededThreshold().alwaysPasses());
    assertTrue(alwaysExceededThreshold().alwaysPasses());
  }

  @Override
  public ScoreThreshold<Score> makeTrivialObject() {
    return scoreThreshold(0);
  }

  @Override
  public ScoreThreshold<Score> makeNontrivialObject() {
    return scoreThreshold(0.12345);
  }

  @Override
  public ScoreThreshold<Score> makeMatchingNontrivialObject() {
    return scoreThreshold(0.12345000001);
  }

  @Override
  protected boolean willMatch(ScoreThreshold<Score> expected, ScoreThreshold<Score> actual) {
    return preciseValueMatcher(expected, 1e-8).matches(actual);
  }

}
