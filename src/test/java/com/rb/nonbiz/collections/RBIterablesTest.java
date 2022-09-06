package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.collections.RBIterables.allPairsMatch;
import static com.rb.nonbiz.collections.RBIterables.consecutiveNonOverlappingPairs;
import static com.rb.nonbiz.collections.RBIterables.consecutivePairs;
import static com.rb.nonbiz.collections.RBIterables.dotProduct;
import static com.rb.nonbiz.collections.RBIterables.forEachUnequalPairInList;
import static com.rb.nonbiz.collections.RBIterables.forEachUniquePair;
import static com.rb.nonbiz.collections.RBIterables.getOnlyIndexWhere;
import static com.rb.nonbiz.collections.RBIterables.sumDoubles;
import static com.rb.nonbiz.collections.RBIterables.weightedAverage;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RBIterablesTest {

  @Test
  public void consecutivePairs_innerIteratorIsEmpty_returnsEmptyIterator() {
    assertEmpty(consecutivePairs(emptyList()));
  }

  @Test
  public void consecutivePairs_innerIteratorHas1Item_returnsEmptyIterator() {
    assertEmpty(consecutivePairs(singletonList("a")));
  }

  @Test
  public void consecutivePairs_innerIteratorHas2Items_returnsIteratorWith1Item() {
    assertEquals(
        singletonList(pairOfSameType("a", "b")),
        consecutivePairs(ImmutableList.of("a", "b")));
  }

  @Test
  public void consecutivePairs_innerIteratorHas4Items_returnsIteratorWith3Items() {
    assertEquals(
        ImmutableList.of(pairOfSameType("a", "b"), pairOfSameType("b", "c"), pairOfSameType("c", "d")),
        consecutivePairs(ImmutableList.of("a", "b", "c", "d")));
  }

  @Test
  public void testConsecutiveNonOverlappingPairs() {
    IntFunction<List<PairOfSameType<String>>> pairsCreator = count -> consecutiveNonOverlappingPairs(IntStream
        .range(0, count)
        .mapToObj(i -> "_" + i).collect(Collectors.toList()));
    assertEquals(emptyList(), pairsCreator.apply(0));
    assertEquals(
        singletonList(pairOfSameType("_0", "_1")),
        pairsCreator.apply(2));
    assertEquals(
        ImmutableList.of(pairOfSameType("_0", "_1"), pairOfSameType("_2", "_3")),
        pairsCreator.apply(4));
    assertEquals(
        ImmutableList.of(pairOfSameType("_0", "_1"), pairOfSameType("_2", "_3"), pairOfSameType("_4", "_5")),
        pairsCreator.apply(6));
    // Odd means we can't make full pairs
    assertIllegalArgumentException( () -> pairsCreator.apply(1));
    assertIllegalArgumentException( () -> pairsCreator.apply(3));
    assertIllegalArgumentException( () -> pairsCreator.apply(5));
  }

  @Test
  public void testSumDoubles() {
    assertEquals(0, sumDoubles(emptyList()), 1e-8);
    assertEquals(1.11, sumDoubles(singletonList(1.11)), 1e-8);
    assertEquals(7.77, sumDoubles(ImmutableList.of(1.11, 2.22, 4.44)), 1e-8);
  }

  @Test
  public void testWeightedAverage() {
    assertEquals(0.0, weightedAverage(emptyList(), emptyList()), 1e-8);
    assertEquals(7.0, weightedAverage(singletonList(7.0), singletonList(1.2345)), 1e-8);
    assertEquals(50.0,
        weightedAverage(
            ImmutableList.of(40.0, 60.0), ImmutableList.of(1.0, 1.0)), 1e-8);
    assertEquals(50.0,
        weightedAverage(
            ImmutableList.of(40.0, 60.0), ImmutableList.of(1.2345, 1.2345)), 1e-8);
    assertEquals(doubleExplained(55.0, 40 * 0.25 + 60 * 0.75),
        weightedAverage(
            ImmutableList.of(40.0, 60.0), ImmutableList.of(1.0, 3.0)), 1e-8);
    assertEquals(40.0,
        weightedAverage(
            ImmutableList.of(40.0, 60.0), ImmutableList.of(1.2345, 0.0)), 1e-8);
    assertEquals(60.0,
        weightedAverage(
            ImmutableList.of(40.0, 60.0), ImmutableList.of(0.0, 1.2345)), 1e-8);
  }

  @Test
  public void testIllegalWeightedAverageCases() {
    assertIllegalArgumentException( () -> weightedAverage(ImmutableList.of(1.1, 2.2), ImmutableList.of(3.3)));
    assertIllegalArgumentException( () -> weightedAverage(ImmutableList.of(1.1, 2.2), ImmutableList.of(3.3, 4.4, 5.5)));
    assertIllegalArgumentException( () -> weightedAverage(ImmutableList.of(1.1, 2.2), ImmutableList.of(0.0, 0.0)));
    assertIllegalArgumentException( () -> weightedAverage(ImmutableList.of(1.1, 2.2), ImmutableList.of(-1.0, 5.0)));
  }

  @Test
  public void forEachPair_bothEmpty_doesNothing() {
    assertEmpty(runForEach(emptyList(), emptyList()));
  }

  @Test
  public void forEachPair_bothEqualSize_works() {
    assertEquals(
        ImmutableList.of("1_true", "2_false"),
        runForEach(ImmutableList.of(1, 2), ImmutableList.of(true, false)));
  }

  @Test
  public void forEachPair_sizesAreUnequal_throws() {
    assertIllegalArgumentException( () -> runForEach(ImmutableList.of(1, 2), ImmutableList.of(true)));
    assertIllegalArgumentException( () -> runForEach(ImmutableList.of(1, 2), emptyList()));
    assertIllegalArgumentException( () -> runForEach(singletonList(1), ImmutableList.of(true, false)));
    assertIllegalArgumentException( () -> runForEach(emptyList(), ImmutableList.of(true, false)));
  }

  @Test
  public void testDotProduct() {
    assertIllegalArgumentException( () -> dotProduct(emptyList(), emptyList()));
    assertIllegalArgumentException( () -> dotProduct(ImmutableList.of(-1.1, 3.3), emptyList()));
    assertIllegalArgumentException( () -> dotProduct(emptyList(), ImmutableList.of(-1.1, 3.3)));
    assertIllegalArgumentException( () -> dotProduct(singletonList(-1.1), emptyList()));
    assertIllegalArgumentException( () -> dotProduct(emptyList(), singletonList(-1.1)));
    assertIllegalArgumentException( () -> dotProduct(ImmutableList.of(-1.1, 3.3), singletonList(-1.1)));
    assertEquals(
        doubleExplained(-77, -1.1 * 10 + 3.3 * (-20)),
        dotProduct(ImmutableList.of(-1.1, 3.3), ImmutableList.of(10.0, -20.0)),
        1e-8);
    assertEquals(
        doubleExplained(-11, -1.1 * 10),
        dotProduct(singletonList(-1.1), singletonList(10.0)),
        1e-8);
  }

  @Test
  public void testGetOnlyIndexWhere() {
    assertThrowsAnyException( () -> getOnlyIndexWithB());
    assertThrowsAnyException( () -> getOnlyIndexWithB("a"));
    assertThrowsAnyException( () -> getOnlyIndexWithB("a", "b", "b"));
    assertThrowsAnyException( () -> getOnlyIndexWithB("b", "a", "b"));
    assertThrowsAnyException( () -> getOnlyIndexWithB("b", "b", "a"));
    assertThrowsAnyException( () -> getOnlyIndexWithB("b", "b", "b"));
    assertEquals(0, getOnlyIndexWithB("b"));
    assertEquals(0, getOnlyIndexWithB("b", "a"));
    assertEquals(1, getOnlyIndexWithB("a", "b"));
    assertEquals(0, getOnlyIndexWithB("b", "a", "a"));
    assertEquals(1, getOnlyIndexWithB("a", "b", "a"));
    assertEquals(2, getOnlyIndexWithB("a", "a", "b"));
  }

  @Test
  public void testForEachUniqueUnorderedPairList() {
    List<Integer> result = newArrayList();
    forEachUnequalPairInList(ImmutableList.of(2, 3, 4), (i1, i2) -> result.add(i1 * i2));
    assertEquals(
        ImmutableList.of(
            intExplained(6, 2 * 3),
            intExplained(8, 2 * 4),
            intExplained(12, 3 * 4)),
        result);
  }

  @Test
  public void testForEachUniquePair() {
    List<String> result = newArrayList();
    forEachUniquePair(ImmutableList.of("a", "b", "c"), (s1, s2) -> result.add(Strings.format("%s%s", s1, s2)));
    assertEquals(
        ImmutableList.of("ab", "ac", "ba", "bc", "ca", "cb"),
        result);
  }

  @Test
  public void test_allPairsMatch() {
    BiFunction<List<String>, List<String>, Boolean> matchChecker = (list1, list2) ->
        allPairsMatch(
            list1,
            list2,
            // True if both strings start with the same character.
            (str1, str2) -> str1.substring(0, 1).equals(str2.substring(0, 1)));

    assertTrue(matchChecker.apply(emptyList(), emptyList()));
    assertTrue(matchChecker.apply(singletonList("a"), singletonList("a")));
    assertTrue(matchChecker.apply(singletonList("ax"), singletonList("ay"))); // in this test, we only check the first letter
    assertTrue(matchChecker.apply(
        ImmutableList.of("ax", "bx"),
        ImmutableList.of("ay", "by")));

    assertIllegalArgumentException( () -> matchChecker.apply(
        singletonList(DUMMY_STRING),
        emptyList()));
    assertIllegalArgumentException( () -> matchChecker.apply(
        singletonList(DUMMY_STRING),
        ImmutableList.of(DUMMY_STRING, DUMMY_STRING)));
  }

  private int getOnlyIndexWithB(String...values) {
    return getOnlyIndexWhere(Arrays.asList(values), s -> s.equals("b"));
  }

  private List<String> runForEach(List<Integer> left, List<Boolean> right) {
    List<String> results = newArrayList();
    RBIterables.forEachPair(left, right, (i, b) -> results.add(Strings.format("%s_%s", i, b)));
    return results;
  }

}
