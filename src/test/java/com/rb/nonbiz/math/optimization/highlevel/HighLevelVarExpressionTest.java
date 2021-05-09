package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.zeroConstantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.constantHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.highLevelVarExpressionWithoutConstantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.zeroConstantHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.disjointHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.highLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.singleVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.sumOfHighLevelVars;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarWithWeight.highLevelVarWithWeight;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarWithWeightTest.highLevelVarWithWeightMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.RBPublicTestOnlySuperVarConstructors.testConstantSuperVar;
import static com.rb.nonbiz.math.optimization.highlevel.RBPublicTestOnlySuperVarConstructors.testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingImpreciseAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_LABEL;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class HighLevelVarExpressionTest extends RBTestMatcher<HighLevelVarExpression> {

  public static HighLevelVarExpression testHighLevelVarExpressionWithSeed(double seed) {
    return testHighLevelVarExpressionWithSeed(seed, 0);
  }

  public static HighLevelVarExpression testHighLevelVarExpressionWithSeed(double seed, int varSeed) {
    return disjointHighLevelVarExpression(
        ImmutableList.of(
            highLevelVarWithWeight(new TestSuperVar(
                2.0 + seed, rawVariable("x", 0 + varSeed), 3.0 + seed, rawVariable("y", 1 + varSeed), 4.0 + seed), 100 + seed),
            highLevelVarWithWeight(new TestSuperVar(
                5.0 + seed, rawVariable("z", 2 + varSeed), 6.0 + seed, rawVariable("w", 3 + varSeed), 7.0 + seed), 200 + seed)),
        constantTerm(8.0 + seed));
  }

  @Test
  public void someVariablesAreShared_throws() {
    assertIllegalArgumentException( () ->
        disjointHighLevelVarExpression(
            ImmutableList.of(
                highLevelVarWithWeight(new TestSuperVar(DUMMY_DOUBLE, rawVariable("x", 0), DUMMY_DOUBLE, rawVariable("y", 1), DUMMY_DOUBLE), DUMMY_DOUBLE),
                highLevelVarWithWeight(new TestSuperVar(DUMMY_DOUBLE, rawVariable("z", 2), DUMMY_DOUBLE, rawVariable("x", 0), DUMMY_DOUBLE), DUMMY_DOUBLE)),
            constantTerm(DUMMY_DOUBLE)));
  }

  @Test
  public void testIsConstant_simple() {
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    assertFalse(disjointHighLevelVarExpression(2, x, 3, y, 5.5).isConstant());
    assertFalse(disjointHighLevelVarExpression(2, x, 3, y).isConstant());
    assertTrue(constantHighLevelVarExpression(constantTerm(0.123)).isConstant());
    assertTrue(zeroConstantHighLevelVarExpression().isConstant());
  }

  @Test
  public void testIsConstant_holdsExpressionsThatAreThemselvesConstant() {
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    SuperVar xPlusY = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, sumOfHighLevelVars(x, y));
    assertFalse(singleVarExpression(xPlusY).isConstant());
    SuperVar tenPlusFive = testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, zeroConstantHighLevelVarExpression());
    assertTrue(singleVarExpression(tenPlusFive).isConstant());
  }

  @Test
  public void testSubtractConstant() {
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    assertThat(
        disjointHighLevelVarExpression(2, x, 3, y, 5.5).subtractConstant(constantTerm(4.4)),
        highLevelVarExpressionMatcher(disjointHighLevelVarExpression(2, x, 3, y, doubleExplained(1.1, 5.5 - 4.4))));
    assertThat(
        constantHighLevelVarExpression(constantTerm(-6.6)).subtractConstant(constantTerm(-7.7)),
        highLevelVarExpressionMatcher(constantHighLevelVarExpression(constantTerm(doubleExplained(1.1, -6.6 - (-7.7))))));
    assertThat(
        disjointHighLevelVarExpression(2, x, 3, y, 5.5).subtractConstant(zeroConstantTerm()),
        highLevelVarExpressionMatcher(disjointHighLevelVarExpression(2, x, 3, y, 5.5)));
  }

  @Test
  public void testWithoutConstantTerm() {
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    assertThat(
        disjointHighLevelVarExpression(2, x, 3, y, 5.5).withoutConstantTerm(),
        highLevelVarExpressionMatcher(disjointHighLevelVarExpression(2, x, 3, y)));
  }

  @Test
  public void testAddAndAddDisjoint() {
    assertThat(
        constantHighLevelVarExpression(constantTerm(-1.1))
            .add(constantHighLevelVarExpression(constantTerm(3.3))),
        highLevelVarExpressionMatcher(
            constantHighLevelVarExpression(constantTerm(doubleExplained(2.2, -1.1 + 3.3)))));
    assertThat(
        constantHighLevelVarExpression(constantTerm(-1.1))
            .addDisjoint(constantHighLevelVarExpression(constantTerm(3.3))),
        highLevelVarExpressionMatcher(
            constantHighLevelVarExpression(constantTerm(doubleExplained(2.2, -1.1 + 3.3)))));
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    RawVariable z = rawVariable("z", 2);
    assertThat(
        disjointHighLevelVarExpression(5, x, 6, y, 1.1)
            .add(highLevelVarExpression(7, z, 2.2)),
        highLevelVarExpressionMatcher(
            disjointHighLevelVarExpression(5, x, 6, y, 7, z, doubleExplained(3.3, 1.1 + 2.2))));
    assertThat(
        disjointHighLevelVarExpression(5, x, 6, y, 1.1)
            .addDisjoint(highLevelVarExpression(7, z, 2.2)),
        highLevelVarExpressionMatcher(
            disjointHighLevelVarExpression(5, x, 6, y, 7, z, doubleExplained(3.3, 1.1 + 2.2))));
    assertIllegalArgumentException( () -> highLevelVarExpression(1, x, 0)
        .addDisjoint(highLevelVarExpression(1, x, 0)));
  }

  @Test
  public void testSubtractAndSubtractDisjoint() {
    assertThat(
        constantHighLevelVarExpression(constantTerm(-1.1))
            .subtract(constantHighLevelVarExpression(constantTerm(3.3))),
        highLevelVarExpressionMatcher(
            constantHighLevelVarExpression(constantTerm(doubleExplained(-4.4, -1.1 - 3.3)))));
    assertThat(
        constantHighLevelVarExpression(constantTerm(-1.1))
            .subtractDisjoint(constantHighLevelVarExpression(constantTerm(3.3))),
        highLevelVarExpressionMatcher(
            constantHighLevelVarExpression(constantTerm(doubleExplained(-4.4, -1.1 - 3.3)))));
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    RawVariable z = rawVariable("z", 2);
    assertThat(
        disjointHighLevelVarExpression(5, x, 6, y, 1.1)
            .subtract(highLevelVarExpression(7, z, 3.3)),
        highLevelVarExpressionMatcher(
            disjointHighLevelVarExpression(5, x, 6, y, -7, z, doubleExplained(-2.2, 1.1 - 3.3))));
    assertThat(
        disjointHighLevelVarExpression(5, x, 6, y, 1.1)
            .subtractDisjoint(highLevelVarExpression(7, z, 3.3)),
        highLevelVarExpressionMatcher(
            disjointHighLevelVarExpression(5, x, 6, y, -7, z, doubleExplained(-2.2, 1.1 - 3.3))));
    assertIllegalArgumentException( () -> highLevelVarExpression(1, x, 0)
        .subtractDisjoint(highLevelVarExpression(1, x, 0)));
  }

  @Test
  public void testNegate() {
    // 2x -3y + 5 becomes -ax + by -c
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 1);
    assertThat(
        constantHighLevelVarExpression(constantTerm(-1.1)).negate(),
        highLevelVarExpressionMatcher(constantHighLevelVarExpression(constantTerm(1.1))));
    assertThat(
        zeroConstantHighLevelVarExpression().negate(),
        highLevelVarExpressionMatcher(zeroConstantHighLevelVarExpression()));
    assertThat(
        constantHighLevelVarExpression(constantTerm(1.1)).negate(),
        highLevelVarExpressionMatcher(constantHighLevelVarExpression(constantTerm(-1.1))));
    assertThat(
        disjointHighLevelVarExpression(2, x, -3, y, 5).negate(),
        highLevelVarExpressionMatcher(disjointHighLevelVarExpression(-2, x, 3, y, -5)));
    assertThat(
        disjointHighLevelVarExpression(-2, x, 3, y, -5).negate(),
        highLevelVarExpressionMatcher(disjointHighLevelVarExpression(2, x, -3, y, 5)));
  }

  @Test
  public void testGetRecursivelyExpandedConstantTerm() {
    // 5x + 7, where x = 3y + 2
    HighLevelVarExpression fiveXplus7 = highLevelVarExpression(
        5,
        testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(DUMMY_LABEL, highLevelVarExpression(3.0, rawVariable("y", 0), 2)),
        7);
    assertThat(
        fiveXplus7.getRecursivelyExpandedConstantTerm(),
        impreciseValueMatcher(constantTerm(doubleExplained(17, 7 + 5 * 2)), 1e-8));
    assertThat(
        fiveXplus7.getConstantTerm(),
        impreciseValueMatcher(constantTerm(7), 1e-8));
  }

  @Test
  public void testIsConstantGetsProperlyPreservedDuringOperations() {
    RawVariable var0 = rawVariable("x0", 0);
    RawVariable var1 = rawVariable("x1", 1);
    RawVariable var2 = rawVariable("x2", 2);
    RawVariable var3 = rawVariable("x3", 3);
    RawVariable var4 = rawVariable("x4", 4);
    RawVariable var5 = rawVariable("x5", 5);
    HighLevelVarExpression shallowConstant0 = constantHighLevelVarExpression(constantTerm(1.2345));
    HighLevelVarExpression shallowConstant1 = constantHighLevelVarExpression(constantTerm(6.7890));
    HighLevelVarExpression shallowConstant2 = zeroConstantHighLevelVarExpression();
    // This is called 'deep' because the expression doesn't only have a 'top-level' constant term of 5.5,
    // but 2 supervars which themselves are constant. This is to make sure that the code doesn't erroneously
    // have the logic of "is constant" == "only has constantTerm".
    // treat that as a non-constant just because it has
    HighLevelVarExpression deepConstant0 = highLevelVarExpression(
        1.1, testConstantSuperVar(DUMMY_LABEL, constantTerm(2.2)),
        3.3, testConstantSuperVar(DUMMY_LABEL, constantTerm(4.4)),
        5.5);
    HighLevelVarExpression deepConstant1 = highLevelVarExpression(
        6.6, testConstantSuperVar(DUMMY_LABEL, constantTerm(7.7)),
        8.8, testConstantSuperVar(DUMMY_LABEL, constantTerm(9.9)),
        10.10);
    HighLevelVarExpression nonConstant0 = singleVarExpression(var0);
    HighLevelVarExpression nonConstant1 = singleVarExpression(var1);
    HighLevelVarExpression nonConstant2 = highLevelVarExpression(11.11, var2, 12.12);
    HighLevelVarExpression nonConstant3 = highLevelVarExpressionWithoutConstantTerm(
        singletonList(highLevelVarWithWeight(var3, 13.13)));
    HighLevelVarExpression nonConstant4 = disjointHighLevelVarExpression(14.14, var4, 15.15, var5);

    List<HighLevelVarExpression> constants =
        ImmutableList.of(shallowConstant0, shallowConstant1, shallowConstant2, deepConstant0, deepConstant1);
    List<HighLevelVarExpression> nonConstants =
        ImmutableList.of(nonConstant0, nonConstant1, nonConstant2, nonConstant3, nonConstant4);

    constants.forEach(expr -> assertTrue(expr.isConstant()));
    constants.forEach(expr -> assertTrue(expr.negate().isConstant()));
    nonConstants.forEach(expr -> assertFalse(expr.isConstant()));
    nonConstants.forEach(expr -> assertFalse(expr.negate().isConstant()));

    for (HighLevelVarExpression constantA : constants) {
      for (HighLevelVarExpression constantB : constants) {
        assertTrue(constantA.add(constantB).isConstant());
        assertTrue(constantA.addDisjoint(constantB).isConstant());
        assertTrue(constantA.subtract(constantB).isConstant());
        assertTrue(constantA.subtractDisjoint(constantB).isConstant());
        assertTrue(constantA.subtractConstant(constantTerm(16.16)).isConstant());
      }
    }
    for (HighLevelVarExpression nonConstantA : nonConstants) {
      for (HighLevelVarExpression nonConstantB : nonConstants) {
        assertFalse(nonConstantA.add(nonConstantB).isConstant());
        assertFalse(nonConstantA.subtract(nonConstantB).isConstant());
        assertFalse(nonConstantA.subtractConstant(constantTerm(17.17)).isConstant());
        if (nonConstantA != nonConstantB) {
          assertFalse(nonConstantA.addDisjoint(nonConstantB).isConstant());
          assertFalse(nonConstantA.subtractDisjoint(nonConstantB).isConstant());
        }
      }
    }
    for (HighLevelVarExpression nonConstant : nonConstants) {
      for (HighLevelVarExpression constant : constants) {
        assertFalse(nonConstant.add(constant).isConstant());
        assertFalse(nonConstant.addDisjoint(constant).isConstant());
        assertFalse(nonConstant.subtract(constant).isConstant());
        assertFalse(nonConstant.subtractDisjoint(constant).isConstant());
        assertFalse(nonConstant.subtractConstant(constantTerm(18.18)).isConstant());

        assertFalse(constant.add(nonConstant).isConstant());
        assertFalse(constant.addDisjoint(nonConstant).isConstant());
        assertFalse(constant.subtract(nonConstant).isConstant());
        assertFalse(constant.subtractDisjoint(nonConstant).isConstant());
      }
    }
  }

  @Override
  public HighLevelVarExpression makeTrivialObject() {
    return zeroConstantHighLevelVarExpression();
  }

  @Override
  public HighLevelVarExpression makeNontrivialObject() {
    return disjointHighLevelVarExpression(
        ImmutableList.of(
            highLevelVarWithWeight(new TestSuperVar(2.0, rawVariable("x", 0), 3.0, rawVariable("y", 1), 4.0), 100),
            highLevelVarWithWeight(new TestSuperVar(5.0, rawVariable("z", 2), 6.0, rawVariable("w", 3), 7.0), 200)),
        constantTerm(8.0));
  }

  @Override
  public HighLevelVarExpression makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return disjointHighLevelVarExpression(
        ImmutableList.of(
            highLevelVarWithWeight(new TestSuperVar(2.0 + e, rawVariable("x", 0), 3.0 + e, rawVariable("y", 1), 4.0 + e), 100 + e),
            highLevelVarWithWeight(new TestSuperVar(5.0 + e, rawVariable("z", 2), 6.0 + e, rawVariable("w", 3), 7.0 + e), 200 + e)),
        constantTerm(8.0 + e));
  }

  @Override
  protected boolean willMatch(HighLevelVarExpression expected, HighLevelVarExpression actual) {
    return highLevelVarExpressionMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<HighLevelVarExpression> highLevelVarExpressionMatcher(HighLevelVarExpression expected) {
    return highLevelVarExpressionMatcher(expected, Optional.empty());
  }

  // We need the 2nd arg to avoid infinite recursion, as an HighLevelVar's constraints may contain itself.
  public static TypeSafeMatcher<HighLevelVarExpression> highLevelVarExpressionMatcher(
      HighLevelVarExpression expected, Optional<HighLevelVar> doNotMatchThisVar) {
    return makeMatcher(expected,
        // There's no ordered semantics here, but it just makes testing easier.
        matchList(v -> v.getHighLevelVarsWithWeights(), f -> highLevelVarWithWeightMatcher(f, doNotMatchThisVar)),
        matchUsingImpreciseAlmostEquals(v -> v.getConstantTerm(), 1e-8),
        matchUsingEquals(v -> v.isConstant()));
    // We would normally also add:
    // , matchEnum(v -> v.getDisjointVariablesSafeguard()
    // However, this breaks a lot of existing tests which generate test-only expressions.
    // Since this field is not really data, but just an instruction to turn on a precondition, it's relatively OK
    // that we skip it in the matcher. Plus, the matcher is for tests only anyway. It's not great, but it's hard otherwise.
  }

}
