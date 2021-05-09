package com.rb.nonbiz.math.optimization.general;

import com.rb.biz.investing.strategy.optbased.NaiveSubObjective;
import com.rb.biz.investing.strategy.optbased.NaiveSubObjectiveTest;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.biz.investing.strategy.optbased.NaiveSubObjectiveTest.naiveSubObjectiveMatcher;
import static com.rb.nonbiz.math.optimization.general.NormalizedLeafSubObjective.normalizedNaiveSubObjective;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplier.naiveSubObjectiveNormalizationMultiplier;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplierTest.objectiveValueNormalizationMultiplierMatcher;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplierTest.objectiveValueNormalizationMultiplierOfSameTypeMatcher;
import static com.rb.nonbiz.math.optimization.general.ObjectiveValueNormalizationMultiplierTest.unitNaiveSubObjectiveNormalization;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class NormalizedLeafSubObjectiveTest extends RBTestMatcher<NormalizedLeafSubObjective<NaiveSubObjective>> {

  @Override
  public NormalizedLeafSubObjective<NaiveSubObjective> makeTrivialObject() {
    return normalizedNaiveSubObjective(new NaiveSubObjectiveTest().makeTrivialObject(), unitNaiveSubObjectiveNormalization());
  }

  @Override
  public NormalizedLeafSubObjective<NaiveSubObjective> makeNontrivialObject() {
    return normalizedNaiveSubObjective(
        new NaiveSubObjectiveTest().makeNontrivialObject(),
        naiveSubObjectiveNormalizationMultiplier(0.12345));
  }

  @Override
  public NormalizedLeafSubObjective<NaiveSubObjective> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return normalizedNaiveSubObjective(
        new NaiveSubObjectiveTest().makeNontrivialObject(),
        naiveSubObjectiveNormalizationMultiplier(0.12345 + e));
  }

  @Override
  protected boolean willMatch(NormalizedLeafSubObjective<NaiveSubObjective> expected,
                              NormalizedLeafSubObjective<NaiveSubObjective> actual) {
    return normalizedLeafSubObjectiveOfSameTypeMatcher(expected, f -> naiveSubObjectiveMatcher(f)).matches(actual);
  }

  public static <T extends LinearSubObjectiveFunction> TypeSafeMatcher<NormalizedLeafSubObjective<T>> normalizedLeafSubObjectiveOfSameTypeMatcher(
      NormalizedLeafSubObjective<T> expected, MatcherGenerator<T> leafsubObjectiveMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getNormalizationMultiplier(), f -> objectiveValueNormalizationMultiplierOfSameTypeMatcher(f)),
        match(v -> v.getLeafSubObjective(), leafsubObjectiveMatcherGenerator));
  }

  public static TypeSafeMatcher<NormalizedLeafSubObjective<? extends LinearSubObjectiveFunction>> normalizedLeafSubObjectiveMatcher(
      NormalizedLeafSubObjective<? extends LinearSubObjectiveFunction> expected,
      MatcherGenerator<LinearSubObjectiveFunction> leafsubObjectiveMatcherGenerator) {
    // I can't get the newer-style matcher to work due to some weird generics problem.
    return makeMatcher(expected, actual ->
        objectiveValueNormalizationMultiplierMatcher(expected.getNormalizationMultiplier())
            .matches(actual.getNormalizationMultiplier())
          && leafsubObjectiveMatcherGenerator.apply(expected.getLeafSubObjective())
            .matches(actual.getLeafSubObjective()));
  }

}
