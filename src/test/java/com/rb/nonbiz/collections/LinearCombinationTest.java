package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Money;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.LinearCombination.linearCombination;
import static com.rb.nonbiz.collections.LinearCombination.trivialLinearCombinationOfOneItem;
import static com.rb.nonbiz.collections.NodeWithWeightedChildren.nodeWithTwoWeightedLeafChildren;
import static com.rb.nonbiz.collections.NodeWithWeightedChildren.nodeWithWeightedChildren;
import static com.rb.nonbiz.collections.NodeWithWeightedChildrenTest.nodeWithWeightedChildrenMatcher;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBEitherMatchers.eitherMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.WeightedByUnitFraction.weightedByUnitFraction;
import static org.junit.Assert.assertEquals;

/**
 * This test class is not generic, but the publicly exposed static matcher is.
 */
public class LinearCombinationTest extends RBTestMatcher<LinearCombination<String, Money>> {

  @Test
  public void getLeaves() {
    assertEquals(
        singletonRBSet("a"),
        trivialLinearCombinationOfOneItem("a")
        .getLeaves());
    assertEquals(
        rbSetOf("a", "b", "c", "d", "e"),
        linearCombination(nodeWithWeightedChildren("abcde", ImmutableList.of(
            weightedByUnitFraction(
                linearCombination(nodeWithTwoWeightedLeafChildren("ab",
                    unitFraction(0.9), "a",
                    unitFraction(0.1), "b")),
                unitFraction(0.3)),
            weightedByUnitFraction(
                linearCombination(nodeWithTwoWeightedLeafChildren("cd",
                    unitFraction(0.6), "c",
                    unitFraction(0.4), "d")),
                unitFraction(0.5)),
            weightedByUnitFraction(
                trivialLinearCombinationOfOneItem("e"),
                unitFraction(0.2)))))
            .getLeaves());
  }

  @Override
  public LinearCombination<String, Money> makeTrivialObject() {
    return trivialLinearCombinationOfOneItem(ZERO_MONEY);
  }

  @Override
  public LinearCombination<String, Money> makeNontrivialObject() {
    return linearCombination(nodeWithWeightedChildren("abcde", ImmutableList.of(
        weightedByUnitFraction(
            linearCombination(nodeWithTwoWeightedLeafChildren("ab",
                unitFraction(0.9), money(99),
                unitFraction(0.1), money(11))),
            unitFraction(0.3)),
        weightedByUnitFraction(
            linearCombination(nodeWithTwoWeightedLeafChildren("cd",
                unitFraction(0.6), money(66),
                unitFraction(0.4), money(44))),
            unitFraction(0.5)),
        weightedByUnitFraction(
            trivialLinearCombinationOfOneItem(money(22)),
            unitFraction(0.2)))));
  }

  @Override
  public LinearCombination<String, Money> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    // the epsilons sometimes get subtracted below instead of added, so that the partition weights can still sum to 1
    return linearCombination(nodeWithWeightedChildren("abcde", ImmutableList.of(
        weightedByUnitFraction(
            linearCombination(nodeWithTwoWeightedLeafChildren("ab",
                unitFraction(0.9 + e), money(99 + e),
                unitFraction(0.1 - e), money(11 + e))),
            unitFraction(0.3 + e)),
        weightedByUnitFraction(
            linearCombination(nodeWithTwoWeightedLeafChildren("cd",
                unitFraction(0.6 + e), money(66 + e),
                unitFraction(0.4 - e), money(44 + e))),
            unitFraction(0.5 + e)),
        weightedByUnitFraction(
            trivialLinearCombinationOfOneItem(money(22 + e)),
            unitFraction(0.2 - 2 * e)))));
  }

  @Override
  protected boolean willMatch(LinearCombination<String, Money> expected, LinearCombination<String, Money> actual) {
    return linearCombinationMatcher(expected, n -> typeSafeEqualTo(n), l -> preciseValueMatcher(l, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <N, L> TypeSafeMatcher<LinearCombination<N, L>> linearCombinationMatcher(
      LinearCombination<N, L> expected, MatcherGenerator<N> nodeMatcherGenerator, MatcherGenerator<L> leafMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getNodeWithChildrenOrLeaf(), f -> eitherMatcher(
            f,
            f2 -> nodeWithWeightedChildrenMatcher(f2, nodeMatcherGenerator, leafMatcherGenerator),
            leafMatcherGenerator)));
  }

}
