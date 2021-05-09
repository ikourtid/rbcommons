package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Optional;

import static com.rb.nonbiz.math.optimization.general.SingleSuperVarArtificialObjectiveFunctionTermsTest.singleSuperVarArtificialObjectiveFunctionTermsMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarConstraintTest.highLevelVarConstraintMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressionTest.highLevelVarExpressionMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class SuperVarTest extends RBTestMatcher<SuperVar> {

  @Override
  public SuperVar makeTrivialObject() {
    return new GeneralSuperVarTest().makeTrivialObject();
  }

  @Override
  public SuperVar makeNontrivialObject() {
    return new GeneralSuperVarTest().makeNontrivialObject();
  }

  @Override
  public SuperVar makeMatchingNontrivialObject() {
    return new GeneralSuperVarTest().makeMatchingNontrivialObject();
  }

  @Override
  protected boolean willMatch(SuperVar expected, SuperVar actual) {
    return superVarMatcher(expected).matches(actual);
  }

  public static <T extends SuperVar> TypeSafeMatcher<T> superVarMatcher(T expected) {
    return makeMatcher(expected,
        match(v -> v.getHighLevelVarExpression(),                         f -> highLevelVarExpressionMatcher(f)),
        matchList(v -> v.getAdditionalConstraints(),                      f -> highLevelVarConstraintMatcher(f, Optional.of(expected))),
        match(v -> v.getSingleSuperVarArtificialObjectiveFunctionTerms(), f -> singleSuperVarArtificialObjectiveFunctionTermsMatcher(f)));
  }

}
