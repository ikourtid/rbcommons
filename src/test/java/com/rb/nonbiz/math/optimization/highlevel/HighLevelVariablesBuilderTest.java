package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrderBuilderTest.allRawVariablesInOrderBuilderMatcher;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.general.RawVariableTest.rawVariableMatcher;
import static com.rb.nonbiz.math.optimization.general.RawVariableWithRange.rawVariableWithRange;
import static com.rb.nonbiz.math.optimization.general.RawVariablesWithRange.rawVariablesWithRange;
import static com.rb.nonbiz.math.optimization.general.RawVariablesWithRangeTest.rawVariablesWithRangeMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.generalSuperVarWithoutAddedConstraintsOrArtificialTerms;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.differenceOf2DisjointHighLevelVars;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVariablesBuilder.highLevelVariablesBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.SuperVarTest.superVarMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rbMapToDoubleRangeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_LABEL;

public class HighLevelVariablesBuilderTest extends RBTestMatcher<HighLevelVariablesBuilder> {

  @Test
  public void happyPath() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    RawVariable v1 = builder.addRawVariable("v1");
    RawVariable v2 = builder.addRawVariable("v2");
    RawVariable v3 = builder.addRawVariable("v3");
    RawVariable v4 = builder.addConstrainedRawVariable("v4", Range.atLeast(4.4));
    RawVariable v5 = builder.addConstrainedRawVariable("v5", Range.atMost(5.5));
    SuperVar v1_2 = builder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, differenceOf2DisjointHighLevelVars(v1, v2)));
    SuperVar v3_4 = builder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, differenceOf2DisjointHighLevelVars(v3, v4)));
    assertThat(
        builder.getSuperVars(),
        orderedListMatcher(ImmutableList.of(v1_2, v3_4), v -> superVarMatcher(v)));
    assertThat(
        builder.build().getRawVariablesInOrder(),
        orderedListMatcher(ImmutableList.of(v1, v2, v3, v4, v5), v -> rawVariableMatcher(v)));
    assertThat(
        builder.getRawVariablesWithRanges(),
        rawVariablesWithRangeMatcher(rawVariablesWithRange(ImmutableList.of(
            rawVariableWithRange(v4, Range.atLeast(4.4)),
            rawVariableWithRange(v5, Range.atMost(5.5))))));
  }

  @Test
  public void addsSuperVarWithWrongVars_throws() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    builder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, singleVarExpression(rawVariable("x", 0))));
    assertIllegalArgumentException( () -> builder.build());
  }


  @Override
  public HighLevelVariablesBuilder makeTrivialObject() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    RawVariable v1 = builder.addRawVariable("x");
    return builder;
  }

  @Override
  public HighLevelVariablesBuilder makeNontrivialObject() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    RawVariable v1 = builder.addRawVariable("v1");
    RawVariable v2 = builder.addRawVariable("v2");
    RawVariable v3 = builder.addRawVariable("v3");
    RawVariable v4 = builder.addConstrainedRawVariable("v4", Range.atLeast(4.4));
    RawVariable v5 = builder.addConstrainedRawVariable("v5", Range.atMost(5.5));
    SuperVar v1_2 = builder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, differenceOf2DisjointHighLevelVars(v1, v2)));
    SuperVar v3_4 = builder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, differenceOf2DisjointHighLevelVars(v3, v4)));
    return builder;
  }

  @Override
  public HighLevelVariablesBuilder makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    RawVariable v1 = builder.addRawVariable("v1");
    RawVariable v2 = builder.addRawVariable("v2");
    RawVariable v3 = builder.addRawVariable("v3");
    RawVariable v4 = builder.addConstrainedRawVariable("v4", Range.atLeast(4.4 + e));
    RawVariable v5 = builder.addConstrainedRawVariable("v5", Range.atMost(5.5 + e));
    SuperVar v1_2 = builder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, differenceOf2DisjointHighLevelVars(v1, v2)));
    SuperVar v3_4 = builder.addSuperVar(generalSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, differenceOf2DisjointHighLevelVars(v3, v4)));
    return builder;
  }

  @Override
  protected boolean willMatch(HighLevelVariablesBuilder expected, HighLevelVariablesBuilder actual) {
    return highLevelVariablesBuilderMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<HighLevelVariablesBuilder> highLevelVariablesBuilderMatcher(
      HighLevelVariablesBuilder expected) {
    return makeMatcher(expected,
        match(    v -> v.getAllRawVariablesInOrderBuilder(),   f -> allRawVariablesInOrderBuilderMatcher(f)),
        matchList(v -> v.getSuperVars(),                       f -> superVarMatcher(f)),
        match(    v -> newRBMap(v.getRangesForRawVariables()), f -> rbMapToDoubleRangeMatcher(f, 1e-8)));
  }

}
