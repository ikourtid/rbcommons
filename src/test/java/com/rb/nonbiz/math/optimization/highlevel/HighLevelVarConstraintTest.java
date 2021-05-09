package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.zeroConstantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.DisjointVariablesSafeguard.CHECK_FOR_DISJOINT_RAW_VARIABLES;
import static com.rb.nonbiz.math.optimization.highlevel.DisjointVariablesSafeguard.DO_NOT_CHECK_FOR_DISJOINT_RAW_VARIABLES;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarConstraint.highLevelVarEqualityConstraint;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressionTest.highLevelVarExpressionMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarWithWeight.highLevelVarWithWeight;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_LABEL;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class HighLevelVarConstraintTest extends RBTestMatcher<HighLevelVarConstraint> {

  public static HighLevelVarConstraint testHighLevelVarConstraintWithSeed(double seed) {
    return highLevelVarEqualityConstraint(
        DUMMY_LABEL,
        ImmutableList.of(
            highLevelVarWithWeight(
                new TestSuperVar(
                    2.0 + seed,
                    rawVariable("x", 0),
                    3.0 + seed,
                    rawVariable("y", 1),
                    4.0 + seed),
                5.0 + seed),
            highLevelVarWithWeight(
                new TestSuperVar(
                    6.0 + seed,
                    rawVariable("z", 2),
                    7.0 + seed,
                    rawVariable("w", 3),
                    8.0 + seed),
                9.0 + seed)),
        constantTerm(10.0 + seed),
        CHECK_FOR_DISJOINT_RAW_VARIABLES);
  }

  @Test
  public void noVariables_throws() {
    rbSetOf(zeroConstantTerm(), constantTerm(DUMMY_DOUBLE))
        .forEach(constantTerm -> assertIllegalArgumentException( () -> highLevelVarEqualityConstraint(
            DUMMY_LABEL, emptyList(), constantTerm, DO_NOT_CHECK_FOR_DISJOINT_RAW_VARIABLES)));
  }

  @Override
  public HighLevelVarConstraint makeTrivialObject() {
    return highLevelVarEqualityConstraint(
        DUMMY_LABEL,
        singletonList(highLevelVarWithWeight(new TestSuperVar(1.0, 1.0, 1.0), 1.0)),
        zeroConstantTerm(),
        CHECK_FOR_DISJOINT_RAW_VARIABLES);
  }

  @Override
  public HighLevelVarConstraint makeNontrivialObject() {
    return testHighLevelVarConstraintWithSeed(ZERO_SEED);
  }

  @Override
  public HighLevelVarConstraint makeMatchingNontrivialObject() {
    return testHighLevelVarConstraintWithSeed(EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(HighLevelVarConstraint expected, HighLevelVarConstraint actual) {
    return highLevelVarConstraintMatcher(expected, Optional.empty()).matches(actual);
  }

  // We need the 2nd arg to avoid infinite recursion, as an HighLevelVar's constraints may contain itself.
  public static TypeSafeMatcher<HighLevelVarConstraint> highLevelVarConstraintMatcher(
      HighLevelVarConstraint expected, Optional<HighLevelVar> doNotMatchThisVar) {
    return makeMatcher(expected, actual ->
        highLevelVarExpressionMatcher(expected.getExpression(), doNotMatchThisVar).matches(actual.getExpression())
            && expected.getConstraintDirection().equals(actual.getConstraintDirection()));
  }

}
