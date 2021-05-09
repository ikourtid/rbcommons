package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Money;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.LinearCombination.linearCombination;
import static com.rb.nonbiz.collections.LinearCombination.trivialLinearCombinationOfOneItem;
import static com.rb.nonbiz.collections.LinearCombinationTest.linearCombinationMatcher;
import static com.rb.nonbiz.collections.NodeWithWeightedChildren.nodeWithTwoWeightedLeafChildren;
import static com.rb.nonbiz.collections.NodeWithWeightedChildren.nodeWithWeightedChildren;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.WeightedByUnitFraction.weightedByUnitFraction;
import static com.rb.nonbiz.types.WeightedByUnitFractionTest.weightedByUnitFractionMatcher;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This test class is not generic, but the publicly exposed test matcher is.
 */
public class NodeWithWeightedChildrenTest extends RBTestMatcher<NodeWithWeightedChildren<String, Money>> {

  public static <N, L> NodeWithWeightedChildren<N, L> nodeWithSingleChildWithWeight1(
      N node, LinearCombination<N, L> child) {
    return nodeWithWeightedChildren(node, singletonList(weightedByUnitFraction(child, UNIT_FRACTION_1)));
  }

  public static <N, L> NodeWithWeightedChildren<N, L> nodeWithSingleLeafChildWithWeight1(
      N node, L child) {
    return nodeWithWeightedChildren(node, singletonList(weightedByUnitFraction(
        trivialLinearCombinationOfOneItem(child), UNIT_FRACTION_1)));
  }

  public static <N, L> NodeWithWeightedChildren<N, L> nodeWithTwoWeightedChildren(
      N node,
      UnitFraction weight1, LinearCombination<N, L> child1,
      UnitFraction weight2, LinearCombination<N, L> child2) {
    return nodeWithWeightedChildren(node, ImmutableList.of(
        weightedByUnitFraction(child1, weight1),
        weightedByUnitFraction(child2, weight2)));
  }

  @Test
  public void noItems_throws() {
    assertIllegalArgumentException( () -> nodeWithWeightedChildren(DUMMY_STRING, emptyList()));
  }

  @Test
  public void zeroWeightExists_throws() {
    NodeWithWeightedChildren<String, Money> doesNotThrow = nodeWithTwoWeightedLeafChildren(
        DUMMY_STRING,
        unitFraction(0.999), money(1.1),
        unitFraction(0.001), money(2.2));
    assertWeightCombinationFails(UNIT_FRACTION_0, UNIT_FRACTION_1);
    assertIllegalArgumentException( () ->
        nodeWithWeightedChildren(
            DUMMY_STRING, ImmutableList.of(
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(1.1)), UNIT_FRACTION_0),
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(2.2)), unitFraction(0.3)),
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(3.3)), unitFraction(0.7)))));
    assertIllegalArgumentException( () ->
        nodeWithWeightedChildren(
            DUMMY_STRING, singletonList(
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(1.1)), UNIT_FRACTION_0))));
  }

  @Test
  public void weightsSumToLessThan1_throws() {
    assertWeightCombinationFails(unitFraction(0.8), unitFraction(0.19));
    assertIllegalArgumentException( () ->
        nodeWithWeightedChildren(
            DUMMY_STRING, ImmutableList.of(
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(1.1)), unitFraction(0.1)),
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(2.2)), unitFraction(0.4)),
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(3.3)), unitFraction(0.49)))));
    assertIllegalArgumentException( () ->
        nodeWithWeightedChildren(
            DUMMY_STRING, singletonList(
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(1.1)), unitFraction(0.99)))));
  }

  @Test
  public void weightsSumToMoreThan1_throws() {
    assertWeightCombinationFails(unitFraction(0.8), unitFraction(0.21));
    assertIllegalArgumentException( () ->
        nodeWithWeightedChildren(
            DUMMY_STRING, ImmutableList.of(
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(1.1)), unitFraction(0.1)),
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(2.2)), unitFraction(0.4)),
                weightedByUnitFraction(trivialLinearCombinationOfOneItem(money(3.3)), unitFraction(0.51)))));
  }

  @Test
  public void valuesCanBeEqual_noWeirdReverseIndexingHappens() {
    NodeWithWeightedChildren<String, String> doesNotThrow = nodeWithTwoWeightedLeafChildren(
        "xyz",
        unitFraction(0.999), "a",
        unitFraction(0.001), "b");
  }

  private void assertWeightCombinationFails(UnitFraction weight1, UnitFraction weight2) {
    assertIllegalArgumentException( () ->
        nodeWithTwoWeightedLeafChildren(
            DUMMY_STRING,
            weight1, money(1.1),
            weight2, money(2.2)));
    assertIllegalArgumentException( () ->
        nodeWithTwoWeightedLeafChildren(
            DUMMY_STRING,
            weight1, money(1.1),
            weight2, money(2.2)));
    assertIllegalArgumentException( () ->
        nodeWithTwoWeightedChildren(
            DUMMY_STRING,
            weight1, trivialLinearCombinationOfOneItem(money(1.1)),
            weight2, trivialLinearCombinationOfOneItem(money(2.2))));
    assertIllegalArgumentException( () ->
        nodeWithTwoWeightedChildren(
            DUMMY_STRING,
            weight1, trivialLinearCombinationOfOneItem(money(1.1)),
            weight2, trivialLinearCombinationOfOneItem(money(2.2))));
  }


  @Override
  public NodeWithWeightedChildren<String, Money> makeTrivialObject() {
    return nodeWithSingleLeafChildWithWeight1("", ZERO_MONEY);
  }

  @Override
  public NodeWithWeightedChildren<String, Money> makeNontrivialObject() {
    return nodeWithWeightedChildren("abcde", ImmutableList.of(
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
            unitFraction(0.2))));
  }

  @Override
  public NodeWithWeightedChildren<String, Money> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    // the epsilons sometimes get subtracted below instead of added, so that the partition weights can still sum to 1
    return nodeWithWeightedChildren("abcde", ImmutableList.of(
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
            unitFraction(0.2 - 2 * e))));
  }

  @Override
  protected boolean willMatch(NodeWithWeightedChildren<String, Money> expected, NodeWithWeightedChildren<String, Money> actual) {
    return nodeWithWeightedChildrenMatcher(expected, n -> typeSafeEqualTo(n), l -> preciseValueMatcher(l, 1e-8))
        .matches(actual);
  }

  public static <N, L> TypeSafeMatcher<NodeWithWeightedChildren<N, L>> nodeWithWeightedChildrenMatcher(
      NodeWithWeightedChildren<N, L> expected, MatcherGenerator<N> nodeMatcherGenerator, MatcherGenerator<L> leafMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getNode(), nodeMatcherGenerator),
        matchList(v -> v.getWeightedChildren(), f -> weightedByUnitFractionMatcher(f,
            f2 -> linearCombinationMatcher(f2, nodeMatcherGenerator, leafMatcherGenerator))));
  }

}
