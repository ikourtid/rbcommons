package com.rb.nonbiz.math.optimization.general;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpression;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.general.WeightedRawVariables.weightedRawVariables;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpression.constantFlattenedRawVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpression.flattenedRawVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpression.zeroConstantFlattenedRawVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.FlattenedRawVarExpressionTest.flattenedRawVarExpressionMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;

public class FlattenedRawVarExpressionMergerTest extends RBTest<FlattenedRawVarExpressionMerger> {

  @Test
  public void nothingPassed_returnsEmptyExpression() {
    assertThat(
        makeTestObject().merge(emptyIterator()),
        flattenedRawVarExpressionMatcher(zeroConstantFlattenedRawVarExpression()));
    assertThat(
        makeTestObject().mergeDisjoint(emptyIterator()),
        flattenedRawVarExpressionMatcher(zeroConstantFlattenedRawVarExpression()));
  }

  @Test
  public void oneItemPassed_returnsItem() {
    FlattenedRawVarExpression expression = flattenedRawVarExpression(
        weightedRawVariables(doubleMap(rbMapOf(
            rawVariable("x0", 0), 1.1,
            rawVariable("x1", 1), 2.2))),
        constantTerm(3.3));
    assertThat(
        makeTestObject().merge(singleton(expression).iterator()),
        flattenedRawVarExpressionMatcher(expression));
  }

  @Test
  public void bothZero_returnsZero() {
    assertMergeResult(zeroConstantFlattenedRawVarExpression(),
        zeroConstantFlattenedRawVarExpression(), zeroConstantFlattenedRawVarExpression());
  }

  @Test
  public void bothNonZeroConstants_returnsSum() {
    assertMergeResult(constantFlattenedRawVarExpression(3.3),
        constantFlattenedRawVarExpression(1.1), constantFlattenedRawVarExpression(2.2));
  }

  @Test
  public void oneIsEmpty_returnsOtherOne() {
    assertMergeResult(
        flattenedRawVarExpression(
            weightedRawVariables(doubleMap(rbMapOf(
                rawVariable("x0", 0), 1.1,
                rawVariable("x1", 1), 2.2))),
            constantTerm(3.3)),
        flattenedRawVarExpression(
            weightedRawVariables(doubleMap(rbMapOf(
                rawVariable("x0", 0), 1.1,
                rawVariable("x1", 1), 2.2))),
            constantTerm(3.3)),
        zeroConstantFlattenedRawVarExpression());
  }

  @Test
  public void bothNonEmpty_noOverlap() {
    assertMergeResult(
        flattenedRawVarExpression(
            weightedRawVariables(doubleMap(rbMapOf(
                rawVariable("x0", 0), 1.1,
                rawVariable("x1", 1), 2.2,
                rawVariable("x2", 2), 4.4,
                rawVariable("x3", 3), 5.5))),
            constantTerm(doubleExplained(9.9, 3.3 + 6.6))),
        flattenedRawVarExpression(
            weightedRawVariables(doubleMap(rbMapOf(
                rawVariable("x0", 0), 1.1,
                rawVariable("x1", 1), 2.2))),
            constantTerm(3.3)),
        flattenedRawVarExpression(
            weightedRawVariables(doubleMap(rbMapOf(
                rawVariable("x2", 2), 4.4,
                rawVariable("x3", 3), 5.5))),
            constantTerm(6.6)));
  }

  @Test
  public void bothNonEmpty_hasOverlap_throwsWhenAssumingDisjoint_returnsMergedResultWhenNotAssumingDisjoint() {
    FlattenedRawVarExpression expr1 = flattenedRawVarExpression(
        weightedRawVariables(doubleMap(rbMapOf(
            rawVariable("x0", 0), 1.1,
            rawVariable("x1", 1), 2.2))),
        constantTerm(3.3));
    FlattenedRawVarExpression expr2 = flattenedRawVarExpression(
            weightedRawVariables(doubleMap(rbMapOf(
                rawVariable("x1", 1), 4.4,
                rawVariable("x2", 2), 5.5))),
            constantTerm(6.6));
    assertIllegalArgumentException( () -> makeTestObject().mergeDisjoint(ImmutableList.of(expr1, expr2).iterator()));
    assertIllegalArgumentException( () -> makeTestObject().mergeDisjoint(ImmutableList.of(expr2, expr1).iterator()));
    FlattenedRawVarExpression expected = flattenedRawVarExpression(
        weightedRawVariables(doubleMap(rbMapOf(
            rawVariable("x0", 0), 1.1,
            rawVariable("x1", 1), doubleExplained(6.6, 2.2 + 4.4),
            rawVariable("x2", 2), 5.5))),
        constantTerm(doubleExplained(9.9, 3.3 + 6.6)));
    assertThat(
        makeTestObject().merge(ImmutableList.of(expr1, expr2).iterator()),
        flattenedRawVarExpressionMatcher(expected));
    assertThat(
        makeTestObject().merge(ImmutableList.of(expr2, expr1).iterator()),
        flattenedRawVarExpressionMatcher(expected));
  }

  @Test
  public void bothNonEmpty_hasOverlapResultingInZeroWeight_dropsZeroWeightTerm_returnsMergedResultWhenNotAssumingDisjoint() {
    FlattenedRawVarExpression expr1 = flattenedRawVarExpression(
        weightedRawVariables(doubleMap(rbMapOf(
            rawVariable("x0", 0), 1.1,
            rawVariable("x1", 1), 2.2))),
        constantTerm(3.3));
    FlattenedRawVarExpression expr2 = flattenedRawVarExpression(
        weightedRawVariables(doubleMap(rbMapOf(
            rawVariable("x1", 1), -2.2,
            rawVariable("x2", 2), 5.5))),
        constantTerm(6.6));
    FlattenedRawVarExpression expected = flattenedRawVarExpression(
        weightedRawVariables(doubleMap(rbMapOf(
            rawVariable("x0", 0), 1.1,
            // no x1: 2.2 and -2.2 cancel out
            rawVariable("x2", 2), 5.5))),
        constantTerm(doubleExplained(9.9, 3.3 + 6.6)));
    assertThat(
        makeTestObject().merge(ImmutableList.of(expr1, expr2).iterator()),
        flattenedRawVarExpressionMatcher(expected));
    assertThat(
        makeTestObject().merge(ImmutableList.of(expr2, expr1).iterator()),
        flattenedRawVarExpressionMatcher(expected));
  }

  private void assertMergeResult(FlattenedRawVarExpression expected,
                                 FlattenedRawVarExpression expr1, FlattenedRawVarExpression expr2) {
    assertThat(
        makeTestObject().merge(ImmutableList.of(expr1, expr2).iterator()),
        flattenedRawVarExpressionMatcher(expected));
    assertThat(
        makeTestObject().merge(ImmutableList.of(expr2, expr1).iterator()),
        flattenedRawVarExpressionMatcher(expected));
    assertThat(
        makeTestObject().mergeDisjoint(ImmutableList.of(expr1, expr2).iterator()),
        flattenedRawVarExpressionMatcher(expected));
    assertThat(
        makeTestObject().mergeDisjoint(ImmutableList.of(expr2, expr1).iterator()),
        flattenedRawVarExpressionMatcher(expected));
  }

  @Override
  protected FlattenedRawVarExpressionMerger makeTestObject() {
    return new FlattenedRawVarExpressionMerger();
  }

}
