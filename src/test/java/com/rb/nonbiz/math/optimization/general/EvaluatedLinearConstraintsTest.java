package com.rb.nonbiz.math.optimization.general;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.general.EvaluatedLinearConstraintTest.evaluatedLinearConstraintMatcher;
import static com.rb.nonbiz.math.optimization.general.EvaluatedLinearConstraints.emptyEvaluatedLinearConstraints;
import static com.rb.nonbiz.math.optimization.general.EvaluatedLinearConstraints.evaluatedLinearConstraints;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class EvaluatedLinearConstraintsTest extends RBTestMatcher<EvaluatedLinearConstraints> {

  @Override
  public EvaluatedLinearConstraints makeTrivialObject() {
    return emptyEvaluatedLinearConstraints();
  }

  @Override
  public EvaluatedLinearConstraints makeNontrivialObject() {
    return evaluatedLinearConstraints(ImmutableList.of(
        new EvaluatedLinearConstraintTest().makeTrivialObject(),
        new EvaluatedLinearConstraintTest().makeNontrivialObject()));
  }

  @Override
  public EvaluatedLinearConstraints makeMatchingNontrivialObject() {
    return evaluatedLinearConstraints(ImmutableList.of(
        new EvaluatedLinearConstraintTest().makeTrivialObject(),
        new EvaluatedLinearConstraintTest().makeMatchingNontrivialObject()));
  }

  @Override
  protected boolean willMatch(EvaluatedLinearConstraints expected, EvaluatedLinearConstraints actual) {
    return evaluatedLinearConstraintsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<EvaluatedLinearConstraints> evaluatedLinearConstraintsMatcher(
      EvaluatedLinearConstraints expected) {
    return makeMatcher(expected,
        matchList(v -> v.getRawEvaluatedLinearConstraints(), f -> evaluatedLinearConstraintMatcher(f)));
  }

}
