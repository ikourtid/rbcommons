package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.general.RawVariableTest.rawVariableMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.DisjointVariablesSafeguard.CHECK_FOR_DISJOINT_RAW_VARIABLES;
import static com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar.generalSuperVarWithoutArtificialTerms;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarConstraint.highLevelVarLessThanConstraint;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarConstraint.minMaxConstraints;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarWithWeight.highLevelVarWithWeight;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVariablesBuilder.highLevelVariablesBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.SuperVarTest.superVarMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_LABEL;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class HighLevelVarTest extends RBTestMatcher<HighLevelVar> {

  @Test
  public void testGetAllVariables_getNumRawVariables() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    RawVariable x = builder.addRawVariable("x");
    RawVariable y = builder.addRawVariable("y");
    RawVariable z = builder.addRawVariable("z");
    SuperVar innerVar = builder.addSuperVar(generalSuperVarWithoutArtificialTerms(
        DUMMY_LABEL,
        disjointHighLevelVarExpression(
            ImmutableList.of(
                highLevelVarWithWeight(y, 10.1),
                highLevelVarWithWeight(z, 10.2)),
            constantTerm(5.5)),
        minMaxConstraints(
            closedUnitFractionRange(unitFraction(0.11), unitFraction(0.22)),
            CHECK_FOR_DISJOINT_RAW_VARIABLES)));
    SuperVar outerVar = builder.addSuperVar(generalSuperVarWithoutArtificialTerms(
        DUMMY_LABEL,
        disjointHighLevelVarExpression(
            ImmutableList.of(
                highLevelVarWithWeight(x, 10.0),
                highLevelVarWithWeight(innerVar, 10.3)),
            constantTerm(6.6)),
        self -> ImmutableList.of(
            highLevelVarLessThanConstraint(
                DUMMY_LABEL,
                ImmutableList.of(
                    highLevelVarWithWeight(x, 10.4),
                    highLevelVarWithWeight(innerVar, 10.5)),
                constantTerm(7.7),
                CHECK_FOR_DISJOINT_RAW_VARIABLES))));
    assertThat(
        outerVar.allVariablesIterator(),
        iteratorMatcher(ImmutableList.of(x, y, z).iterator(), v -> rawVariableMatcher(v)));
    assertEquals(3, outerVar.getNumRawVariables());
  }

  @Test
  public void rawVariable_numRawVariablesIs1() {
    assertEquals(1, rawVariable("x", 0).getNumRawVariables());
  }

  @Override
  public HighLevelVar makeTrivialObject() {
    return rawVariable("x", 0);
  }

  @Override
  public HighLevelVar makeNontrivialObject() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    RawVariable x = builder.addRawVariable("x");
    RawVariable y = builder.addRawVariable("y");
    RawVariable z = builder.addRawVariable("z");
    SuperVar innerVar = builder.addSuperVar(generalSuperVarWithoutArtificialTerms(
        label("abc"),
        disjointHighLevelVarExpression(
            ImmutableList.of(
                highLevelVarWithWeight(y, 10.1),
                highLevelVarWithWeight(z, 10.2)),
            constantTerm(5.5)),
        minMaxConstraints(
            closedUnitFractionRange(unitFraction(0.11), unitFraction(0.22)),
            CHECK_FOR_DISJOINT_RAW_VARIABLES)));
    return builder.addSuperVar(generalSuperVarWithoutArtificialTerms(
        label("def"),
        disjointHighLevelVarExpression(
            ImmutableList.of(
                highLevelVarWithWeight(x, 10.0),
                highLevelVarWithWeight(innerVar, 10.3)),
            constantTerm(6.6)),
        self -> singletonList(
            highLevelVarLessThanConstraint(
                label("some constraint label"),
                ImmutableList.of(
                    highLevelVarWithWeight(x, 10.4),
                    highLevelVarWithWeight(innerVar, 10.5)),
                constantTerm(7.7),
                CHECK_FOR_DISJOINT_RAW_VARIABLES))));
  }

  @Override
  public HighLevelVar makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    RawVariable x = builder.addRawVariable("x");
    RawVariable y = builder.addRawVariable("y");
    RawVariable z = builder.addRawVariable("z");
    SuperVar innerVar = builder.addSuperVar(generalSuperVarWithoutArtificialTerms(
        label("abc"),
        disjointHighLevelVarExpression(
            ImmutableList.of(
                highLevelVarWithWeight(y, 10.1 + e),
                highLevelVarWithWeight(z, 10.2 + e)),
            constantTerm(5.5 + e)),
        minMaxConstraints(
            closedUnitFractionRange(unitFraction(0.11 + e), unitFraction(0.22 + e)),
            CHECK_FOR_DISJOINT_RAW_VARIABLES)));
    return builder.addSuperVar(generalSuperVarWithoutArtificialTerms(
        label("def"),
        disjointHighLevelVarExpression(
            ImmutableList.of(
                highLevelVarWithWeight(x, 10.0 + e),
                highLevelVarWithWeight(innerVar, 10.3 + e)),
            constantTerm(6.6 + e)),
        self -> singletonList(
            highLevelVarLessThanConstraint(
                label("some constraint label"),
                ImmutableList.of(
                    highLevelVarWithWeight(x, 10.4 + e),
                    highLevelVarWithWeight(innerVar, 10.5 + e)),
                constantTerm(7.7 + e),
                CHECK_FOR_DISJOINT_RAW_VARIABLES))));
  }

  @Override
  protected boolean willMatch(HighLevelVar expected, HighLevelVar actual) {
    return highLevelVarMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<HighLevelVar> highLevelVarMatcher(HighLevelVar expected) {
    return generalVisitorMatcher(expected, v -> v.visit(new Visitor<VisitorMatchInfo<HighLevelVar>>() {
      @Override
      public VisitorMatchInfo<HighLevelVar> visitRawVariable(RawVariable rawVariable) {
        return visitorMatchInfo(1, rawVariable, (MatcherGenerator<RawVariable>) rv -> rawVariableMatcher(rv));
      }

      @Override
      public VisitorMatchInfo<HighLevelVar> visitSuperVar(SuperVar superVar) {
        return visitorMatchInfo(2, superVar, (MatcherGenerator<SuperVar>) sv -> superVarMatcher(sv));
      }
    }));
  }

}
