package com.rb.nonbiz.math.optimization.general;

import com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpressionTest;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.general.AllArtificialObjectiveFunctionTerms.allArtificialObjectiveFunctionTerms;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpression.zeroConstantFlattenedRawVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpressionTest.flattenedRawVarExpressionMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class AllArtificialObjectiveFunctionTermsTest extends RBTestMatcher<AllArtificialObjectiveFunctionTerms> {

  public static AllArtificialObjectiveFunctionTerms emptyAllArtificialObjectiveFunctionTerms() {
    return allArtificialObjectiveFunctionTerms(zeroConstantFlattenedRawVarExpression());
  }

  @Override
  public AllArtificialObjectiveFunctionTerms makeTrivialObject() {
    return emptyAllArtificialObjectiveFunctionTerms();
  }

  @Override
  public AllArtificialObjectiveFunctionTerms makeNontrivialObject() {
    return allArtificialObjectiveFunctionTerms(new FlattenedRawVarExpressionTest().makeNontrivialObject());
  }

  @Override
  public AllArtificialObjectiveFunctionTerms makeMatchingNontrivialObject() {
    return allArtificialObjectiveFunctionTerms(new FlattenedRawVarExpressionTest().makeMatchingNontrivialObject());
  }

  @Override
  protected boolean willMatch(AllArtificialObjectiveFunctionTerms expected, AllArtificialObjectiveFunctionTerms actual) {
    return allArtificialObjectiveFunctionTermsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<AllArtificialObjectiveFunctionTerms> allArtificialObjectiveFunctionTermsMatcher(
      AllArtificialObjectiveFunctionTerms expected) {
    return makeMatcher(expected,
        match(v -> v.getFlattenedRawVarExpression(), f -> flattenedRawVarExpressionMatcher(f)));
  }

}
