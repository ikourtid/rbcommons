package com.rb.nonbiz.collections;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.rb.biz.types.Money;
import com.rb.nonbiz.math.stats.ZScore;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.Iterators.singletonIterator;
import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.collections.RBIterators.*;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.math.stats.ZScore.zScore;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.doubleIteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.bigDecimalMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_BOOLEAN;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBIteratorsTest {

  @Test
  public void consecutivePairsIterator_innerIteratorIsEmpty_returnsEmptyIterator() {
    assertFalse(consecutivePairsIterator(emptyIterator()).hasNext());
  }

  @Test
  public void consecutivePairsIterator_innerIteratorHas1Item_returnsEmptyIterator() {
    assertFalse(consecutivePairsIterator(singletonList("a").iterator()).hasNext());
  }

  @Test
  public void consecutivePairsIterator_innerIteratorHas2Items_returnsIteratorWith1Item() {
    Iterator<PairOfSameType<String>> iterator = consecutivePairsIterator(ImmutableList.of("a", "b").iterator());
    assertTrue(iterator.hasNext());
    assertEquals(pairOfSameType("a", "b"), iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void consecutivePairsIterator_innerIteratorHas4Items_returnsIteratorWith3Items() {
    Iterator<PairOfSameType<String>> iterator = consecutivePairsIterator(ImmutableList.of("a", "b", "c", "d").iterator());
    assertTrue(iterator.hasNext());
    assertEquals(pairOfSameType("a", "b"), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(pairOfSameType("b", "c"), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(pairOfSameType("c", "d"), iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void consecutiveNonOverlappingPairsIterator_innerIteratorHas2Items_returnsIteratorWith1Item() {
    Iterator<PairOfSameType<String>> iterator = consecutiveNonOverlappingPairsIterator(ImmutableList.of("a", "b").iterator());
    assertTrue(iterator.hasNext());
    assertEquals(pairOfSameType("a", "b"), iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void consecutiveNonOverlappingPairsIterator_innerIteratorHas2Items_returnsIteratorWith2Items() {
    Iterator<PairOfSameType<String>> iterator = consecutiveNonOverlappingPairsIterator(
        ImmutableList.of("a", "b", "c", "d").iterator());
    assertTrue(iterator.hasNext());
    assertEquals(pairOfSameType("a", "b"), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(pairOfSameType("c", "d"), iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testConsecutiveNonOverlappingPairsIterator_specialCases() {
    assertFalse(consecutiveNonOverlappingPairsIterator(emptyIterator()).hasNext());
    // Constructing the iterators with 1 or 3 items does not cause an exception. It cannot; we'd have to consume
    // the iterators first. However, consuming this iterator (with newArrayList) shows the problem.
    // The behavior is inconsistent with an iterator of a single item, but that's fine; we'll get an exception
    // in either case.
    Consumer<List<String>> asserter = stringList ->
        assertIllegalArgumentException( () -> newArrayList(consecutiveNonOverlappingPairsIterator(stringList.iterator())));

    asserter.accept(singletonList("a"));
    asserter.accept(ImmutableList.of("a", "b", "c"));
    asserter.accept(ImmutableList.of("a", "b", "c", "d", "e"));
  }

  @Test
  public void testConsecutivePairsForEach() {
    MutableRBSet<String> resultsSoFar = newMutableRBSet();
    Consumer<List<Character>> consumer = list -> consecutivePairsForEach(
        list.iterator(), (v1, v2) -> resultsSoFar.add(Strings.format("%s%s", v1, v2)));
    assertTrue(resultsSoFar.isEmpty());

    consumer.accept(emptyList());
    assertTrue(resultsSoFar.isEmpty());

    consumer.accept(singletonList('a'));
    assertTrue(resultsSoFar.isEmpty());

    consumer.accept(ImmutableList.of('a', 'b'));
    assertEquals(singletonRBSet("ab"), newRBSet(resultsSoFar));

    resultsSoFar.clear();
    assertTrue(resultsSoFar.isEmpty());
    consumer.accept(ImmutableList.of('a', 'b', 'c', 'd'));
    assertEquals(rbSetOf("ab", "bc", "cd"), newRBSet(resultsSoFar));
  }

  @Test
  public void testConsecutiveTuplesIterator_throwsOnInvalidSizes() {
    assertIllegalArgumentException( () -> consecutiveTuplesIterator(-1, emptyIterator()));
    assertIllegalArgumentException( () -> consecutiveTuplesIterator(0, emptyIterator()));

    assertIllegalArgumentException( () -> consecutiveTuplesIterator(-1, singletonIterator("a")));
    assertIllegalArgumentException( () -> consecutiveTuplesIterator(0, singletonIterator("a")));

    assertIllegalArgumentException( () -> consecutiveTuplesIterator(-1, ImmutableList.of("a", "b").iterator()));
    assertIllegalArgumentException( () -> consecutiveTuplesIterator(0, ImmutableList.of("a", "b").iterator()));

    assertIllegalArgumentException( () -> consecutiveTuplesIterator(-1, ImmutableList.of("a", "b", "c").iterator()));
    assertIllegalArgumentException( () -> consecutiveTuplesIterator(0, ImmutableList.of("a", "b", "c").iterator()));
  }

  @Test
  public void testConsecutiveTuplesIterator_tupleHasTrivialSize1() {
    BiConsumer<List<String>, List<List<String>>> asserter = (dataList, expectedResult) ->
        assertThat(
            newArrayList(consecutiveTuplesIterator(1, dataList.iterator())),
            orderedListEqualityMatcher(
                expectedResult));
    asserter.accept(emptyList(), emptyList());
    asserter.accept(singletonList("a"), singletonList(
        singletonList("a")));
    asserter.accept(
        ImmutableList.of("a", "b"),
        ImmutableList.of(
            singletonList("a"),
            singletonList("b")));
    assertThat(
        newArrayList(consecutiveTuplesIterator(1, ImmutableList.of("a", "b", "c").iterator())),
        orderedListEqualityMatcher(
            ImmutableList.of(
                singletonList("a"),
                singletonList("b"),
                singletonList("c"))));
  }

  @Test
  public void testConsecutiveTuplesIterator_tupleHasSize2() {
    BiConsumer<List<String>, List<List<String>>> asserter = (dataList, expectedResult) ->
        assertThat(
            newArrayList(consecutiveTuplesIterator(2, dataList.iterator())),
            orderedListEqualityMatcher(
                expectedResult));
    asserter.accept(emptyList(), emptyList());
    asserter.accept(singletonList("a"), emptyList());
    asserter.accept(
        ImmutableList.of("a", "b"),
        singletonList(ImmutableList.of("a", "b")));
    asserter.accept(
        ImmutableList.of("a", "b", "c"),
        ImmutableList.of(
            ImmutableList.of("a", "b"),
            ImmutableList.of("b", "c")));
  }

  @Test
  public void testConsecutiveTuplesIterator_tupleHasSize3() {
    BiConsumer<List<String>, List<List<String>>> asserter = (dataList, expectedResult) ->
        assertThat(
            newArrayList(consecutiveTuplesIterator(3, dataList.iterator())),
            orderedListEqualityMatcher(
                expectedResult));
    asserter.accept(emptyList(), emptyList());
    asserter.accept(singletonList("a"), emptyList());
    asserter.accept(ImmutableList.of("a", "b"), emptyList());
    asserter.accept(
        ImmutableList.of("a", "b", "c"),
        singletonList(ImmutableList.of("a", "b", "c")));
    asserter.accept(
        ImmutableList.of("a", "b", "c", "d"),
        ImmutableList.of(
            ImmutableList.of("a", "b", "c"),
            ImmutableList.of("b", "c", "d")));
  }

  @Test
  public void testConsecutiveTuplesForEach() {
    List<String> abcde = ImmutableList.of("a", "b", "c", "d", "e");
    BiConsumer<Integer, String> assertResult = (tupleSize, expectedResult) -> {
      StringBuilder sb = new StringBuilder();
      consecutiveTuplesForEach(tupleSize, abcde.iterator(), tuple -> sb.append(Joiner.on("").join(tuple)).append("_"));
      assertEquals(expectedResult, sb.toString());
    };
    assertResult.accept(6, "");
    assertResult.accept(5, "abcde_");
    assertResult.accept(4, "abcd_bcde_");
    assertResult.accept(3, "abc_bcd_cde_");
    assertResult.accept(2, "ab_bc_cd_de_");
    assertResult.accept(1, "a_b_c_d_e_");
  }

  @Test
  public void testPairConsumer() {
    BiConsumer<List<Integer>, List<Integer>> checkSum10 = (intList1, intList2) ->
        forEachPair(
            intList1.iterator(),
            intList2.iterator(),
            (element1, element2) ->
                assertEquals(10, element1 + element2));

    // for empty lists, the function is never evaluated
    checkSum10.accept(emptyList(), emptyList());

    // corresponding elements sum to 10
    checkSum10.accept(singletonList(0),          singletonList(10));
    checkSum10.accept(ImmutableList.of(1, 2),    ImmutableList.of(9, 8));
    checkSum10.accept(ImmutableList.of(1, 2, 3), ImmutableList.of(9, 8, 7));

    // wrong number of elements throws
    assertIllegalArgumentException( () -> checkSum10.accept(emptyList(), ImmutableList.of(10)));
    assertIllegalArgumentException( () -> checkSum10.accept(ImmutableList.of(10), emptyList()));
    assertIllegalArgumentException( () -> checkSum10.accept(ImmutableList.of(1, 2), ImmutableList.of(9, 8, 7)));
    assertIllegalArgumentException( () -> checkSum10.accept(ImmutableList.of(1, 2, 3), ImmutableList.of(9, 8)));
  }

  @Test
  public void testDotProduct() {
    assertIllegalArgumentException( () -> dotProduct(emptyIterator(), emptyIterator()));
    assertIllegalArgumentException( () -> dotProduct(ImmutableList.of(-1.1, 3.3).iterator(), emptyIterator()));
    assertIllegalArgumentException( () -> dotProduct(emptyIterator(), ImmutableList.of(-1.1, 3.3).iterator()));
    assertIllegalArgumentException( () -> dotProduct(singletonIterator(-1.1), emptyIterator()));
    assertIllegalArgumentException( () -> dotProduct(emptyIterator(), singletonIterator(-1.1)));
    assertIllegalArgumentException( () -> dotProduct(ImmutableList.of(-1.1, 3.3).iterator(), singletonIterator(-1.1)));
    assertEquals(
        doubleExplained(-77, -1.1 * 10 + 3.3 * (-20)),
        dotProduct(ImmutableList.of(-1.1, 3.3).iterator(), ImmutableList.of(10.0, -20.0).iterator()),
        1e-8);
    assertEquals(
        doubleExplained(-11, -1.1 * 10),
        dotProduct(singletonIterator(-1.1), singletonIterator(10.0)),
        1e-8);
  }

  @Test
  public void testCheckUnique() {
    RBPreconditions.checkUnique(emptyIterator());
    RBPreconditions.checkUnique(singletonList("a").iterator());
    RBPreconditions.checkUnique(ImmutableList.of("a", "b", "c").iterator());
    assertIllegalArgumentException( () -> RBPreconditions.checkUnique(ImmutableList.of("a", "b", "a").iterator()));
    assertIllegalArgumentException( () -> RBPreconditions.checkUnique(ImmutableList.of("a", "a").iterator()));
  }

  @Test
  public void testPasteIntoNewIterator() {
    BiFunction<Integer, Boolean, String> transformer = (i, b) -> Strings.format("%s %s", i, b ? "Y" : "N");
    assertThat(
        pasteIntoNewIterator(emptyIterator(), emptyIterator(), transformer),
        iteratorMatcher(emptyIterator(), f -> typeSafeEqualTo(f)));
    assertIllegalArgumentException( () ->
        pasteIntoNewIterator(singletonList(DUMMY_POSITIVE_INTEGER).iterator(), emptyIterator(), transformer)
            .forEachRemaining(i -> {})); // just consume every item in the iterator, to trigger the precondition
    assertIllegalArgumentException( () ->
        pasteIntoNewIterator(emptyIterator(), singletonList(DUMMY_BOOLEAN).iterator(), transformer)
            .forEachRemaining(i -> {})); // just consume every item in the iterator, to trigger the precondition
    assertThat(
        pasteIntoNewIterator(
            ImmutableList.of(10, 11).iterator(),
            ImmutableList.of(true, false).iterator(),
            transformer),
        iteratorMatcher(ImmutableList.of("10 Y", "11 N").iterator(), f -> typeSafeEqualTo(f)));
  }

  @Test
  public void testGetFirstNonUniqueIteratorItem() {
    assertOptionalEmpty(getFirstNonUniqueIteratorItem(emptyIterator()));
    assertOptionalEmpty(getFirstNonUniqueIteratorItem(singletonIterator("a")));
    assertOptionalEmpty(getFirstNonUniqueIteratorItem(ImmutableList.of("a", "b").iterator()));
    assertOptionalEmpty(getFirstNonUniqueIteratorItem(ImmutableList.of("a", "b", "c").iterator()));
    assertOptionalEquals("a", getFirstNonUniqueIteratorItem(ImmutableList.of("a", "a", "c").iterator()));
    assertOptionalEquals("a", getFirstNonUniqueIteratorItem(ImmutableList.of("a", "b", "a").iterator()));
    assertOptionalEquals("b", getFirstNonUniqueIteratorItem(ImmutableList.of("a", "b", "b").iterator()));
  }

  @Test
  public void testIteratorItemsAreUnique() {
    assertTrue(iteratorItemsAreUnique(emptyIterator()));
    assertTrue(iteratorItemsAreUnique(singletonIterator("a")));
    assertTrue(iteratorItemsAreUnique(ImmutableList.of("a", "b").iterator()));
    assertTrue(iteratorItemsAreUnique(ImmutableList.of("a", "b", "c").iterator()));
    assertFalse(iteratorItemsAreUnique(ImmutableList.of("a", "a", "c").iterator()));
    assertFalse(iteratorItemsAreUnique(ImmutableList.of("a", "b", "a").iterator()));
    assertFalse(iteratorItemsAreUnique(ImmutableList.of("a", "b", "b").iterator()));
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
  public void test_transformToDoubleIterator() {
    assertThat(
        transformToDoubleIterator(Collections.<ZScore>emptyIterator()),
        doubleIteratorMatcher(
            emptyIterator(),
            1e-8));
    assertThat(
        transformToDoubleIterator(ImmutableList.of(
            zScore(1.1),
            zScore(2.2),
            zScore(3.3))
            .iterator()),
        doubleIteratorMatcher(
            ImmutableList.of(1.1, 2.2, 3.3).iterator(),
            1e-8));
  }

  @Test
  public void testTransformToBigDecimalIterator() {
    assertThat(
        transformToBigDecimalIterator(Collections.<Money>emptyIterator()),
        iteratorMatcher(emptyIterator(), f -> typeSafeEqualTo(f)));
    assertThat(
        transformToBigDecimalIterator(ImmutableList.of(money(1.1), money(3.3)).iterator()),
        iteratorMatcher(ImmutableList.of(BigDecimal.valueOf(1.1), BigDecimal.valueOf(3.3)).iterator(),
            f -> bigDecimalMatcher(f, 1e-12)));
  }

  @Test
  public void testFromIteratorOfZeroOrOneItem() {
    assertOptionalEmpty(fromIteratorOfZeroOrOneItem(emptyIterator()));
    assertOptionalEquals(123, fromIteratorOfZeroOrOneItem(singletonIterator(123)));
    assertIllegalArgumentException( () -> fromIteratorOfZeroOrOneItem(ImmutableList.of(11, 22).iterator()));
    assertIllegalArgumentException( () -> fromIteratorOfZeroOrOneItem(ImmutableList.of(11, 22, 33).iterator()));
  }

  @Test
  public void testPaste3IntoNewIterator() {
    assertThat(
        paste3IntoNewIterator(
            ImmutableList.of("a", "b").iterator(),
            ImmutableList.of(0, 1).iterator(),
            ImmutableList.of(true, false).iterator(),
            (a, b, c) -> Strings.format("%s,%s,%s", a, b, c)),
        iteratorEqualityMatcher(
            ImmutableList.of("a,0,true", "b,1,false").iterator()));
  }

  @Test
  public void testPaste3IntoNewIterator_unequalElements_throws() {
    Function<List<String>, Iterator<String>> maker = stringList ->
        paste3IntoNewIterator(
            stringList.iterator(),
            ImmutableList.of(0, 1).iterator(),
            ImmutableList.of(true, false).iterator(),
            (a, b, c) -> Strings.format("%s,%s,%s", a, b, c));

    Iterator<String> doesNotThrow = maker.apply(ImmutableList.of("a", "b"));
    assertEquals(2, Iterators.size(doesNotThrow));

    // If the pasted Iterator is invalid because the inputs have differing lengths, we will not be
    // able to call Iterator.size(); doing so will iterate and count, discovering the mismatch.

    assertIllegalArgumentException( () -> Iterators.size(maker.apply(emptyList())));
    assertIllegalArgumentException( () -> Iterators.size(maker.apply(ImmutableList.of("a"))));
    assertIllegalArgumentException( () -> Iterators.size(maker.apply(ImmutableList.of("a", "b", "c"))));
  }

  @Test
  public void testPaste4IntoNewIterator() {
    assertThat(
        paste4IntoNewIterator(
            ImmutableList.of("a", "b").iterator(),
            ImmutableList.of(0, 1).iterator(),
            ImmutableList.of(true, false).iterator(),
            ImmutableList.of(1.0, 2.0).iterator(),
            (a, b, c, d) -> Strings.format("%s,%s,%s,%s", a, b, c, d)),
        iteratorEqualityMatcher(
            ImmutableList.of("a,0,true,1.0", "b,1,false,2.0").iterator()));
  }

  @Test
  public void testPaste4IntoNewIterator_unequalElements_throws() {
    Function<List<String>, Iterator<String>> maker = stringList ->
        paste4IntoNewIterator(
            stringList.iterator(),
            ImmutableList.of(0, 1).iterator(),
            ImmutableList.of(true, false).iterator(),
            ImmutableList.of(1.0, 2.0).iterator(),
            (a, b, c, d) -> Strings.format("%s,%s,%s,%s", a, b, c, d));

    Iterator<String> doesNotThrow = maker.apply(ImmutableList.of("a", "b"));
    assertEquals(2, Iterators.size(doesNotThrow));

    // If the pasted Iterator is invalid because the inputs have differing lengths, we will not be
    // able to call Iterator.size(); doing so will iterate and count, discovering the mismatch.

    assertIllegalArgumentException( () -> Iterators.size(maker.apply(emptyList())));
    assertIllegalArgumentException( () -> Iterators.size(maker.apply(ImmutableList.of("a"))));
    assertIllegalArgumentException( () -> Iterators.size(maker.apply(ImmutableList.of("a", "b", "c"))));
  }

  @Test
  public void testPasteMultipleIntoNewIterator() {
    assertThat(
        pasteMultipleIntoNewIterator(
            itemList -> Joiner.on(',').join(itemList),
            ImmutableList.of(
                ImmutableList.of("a0", "a1").iterator(),
                ImmutableList.of("b0", "b1").iterator(),
                ImmutableList.of("c0", "c1").iterator())),
        iteratorEqualityMatcher(
            ImmutableList.of("a0,b0,c0", "a1,b1,c1").iterator()));
  }

  @Test
  public void testPasteMultipleIntoNewIterator_unequalElements_throws() {
    Function<List<String>, Iterator<String>> maker = stringList ->
        pasteMultipleIntoNewIterator(
            itemList -> Joiner.on(',').join(itemList),
            ImmutableList.of(
                stringList.iterator(),
                ImmutableList.of("b0", "b1").iterator(),
                ImmutableList.of("c0", "c1").iterator()));

    Iterator<String> doesNotThrow = maker.apply(ImmutableList.of("a0", "a1"));
    assertEquals(2, Iterators.size(doesNotThrow));

    // If the pasted Iterator is invalid because the inputs have differing lengths, we will not be
    // able to call Iterator.size(); doing so will iterate and count, discovering the mismatch.

    assertIllegalArgumentException( () -> Iterators.size(maker.apply(emptyList())));
    assertIllegalArgumentException( () -> Iterators.size(maker.apply(ImmutableList.of("a0"))));
    assertIllegalArgumentException( () -> Iterators.size(maker.apply(ImmutableList.of("a0", "a1", "a2"))));
  }

  @Test
  public void testForEach_4argOverload() {
    BiConsumer<List<Integer>, String> asserter = (intList, expectedResult) ->
        assertEquals(
            expectedResult,
            RBIterators.forEach(
                intList.iterator(),
                "<",
                (runningString, intValue, iterationIndex, isLastItem) -> Strings.format("%s_%s%s",
                    runningString,
                    Integer.toString(intValue + iterationIndex),
                    isLastItem ? ">" : "")));

    asserter.accept(ImmutableList.of(70, 80, 90), "<_70_81_92>");
    asserter.accept(ImmutableList.of(70, 80), "<_70_81>");
    asserter.accept(singletonList(70), "<_70>");
    asserter.accept(emptyList(), "<");
  }

  @Test
  public void testForEach_3argOverload() {
    BiConsumer<List<Integer>, String> asserter = (intList, expectedResult) ->
        assertEquals(
            expectedResult,
            RBIterators.forEach(
                intList.iterator(),
                "<",
                (runningString, intValue, isLastItem) -> Strings.format("%s_%s%s",
                    runningString,
                    Integer.toString(intValue),
                    isLastItem ? ">" : "")));

    asserter.accept(ImmutableList.of(70, 80, 90), "<_70_80_90>");
    asserter.accept(ImmutableList.of(70, 80), "<_70_80>");
    asserter.accept(singletonList(70), "<_70>");
    asserter.accept(emptyList(), "<");
  }

  @Test
  public void testForEach_2argOverload() {
    BiConsumer<List<Integer>, String> asserter = (intList, expectedResult) ->
        assertEquals(
            expectedResult,
            RBIterators.forEach(
                intList.iterator(),
                "<",
                (runningString, intValue) -> Strings.format("%s_%s",
                    runningString,
                    Integer.toString(intValue))));

    asserter.accept(ImmutableList.of(70, 80, 90), "<_70_80_90");
    asserter.accept(ImmutableList.of(70, 80), "<_70_80");
    asserter.accept(singletonList(70), "<_70");
    asserter.accept(emptyList(), "<");
  }

  @Test
  public void test_getOnlyElementOrThrow() {
    assertEquals("a", getOnlyElementOrThrow(singletonIterator("a"), DUMMY_STRING));
    assertIllegalArgumentException( () -> getOnlyElementOrThrow(emptyIterator(), DUMMY_STRING));
    assertIllegalArgumentException( () -> getOnlyElementOrThrow(ImmutableList.of("a", "b").iterator(), DUMMY_STRING));
  }

  private int getOnlyIndexWithB(String...values) {
    return getOnlyIndexWhere(Arrays.asList(values).iterator(), v -> v.equals("b"));
  }

}
