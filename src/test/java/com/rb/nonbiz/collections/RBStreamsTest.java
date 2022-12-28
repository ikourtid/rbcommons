package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.collections.PairOfSameTypeTest.pairOfSameTypeMatcher;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.collections.RBStreams.*;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_BOOLEAN;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBStreamsTest {

  private final BiFunction<Integer, Boolean, String> PASTE_BI_FUNCTION = (i, b) -> Strings.format("%s%s", i, b);
  private final TriFunction<Integer, Boolean, String, String> PASTE_TRI_FUNCTION =
      (i, b, s) -> Strings.format("%s%s%s", i, b, s);

  @Test
  public void pasteIntoStream_bothEmpty_returnsEmptyStream() {
    assertEquals(0,
        pasteIntoStream(
            Collections.<Integer>emptyList().iterator(),
            Collections.<Boolean>emptyList().iterator(),
            PASTE_BI_FUNCTION).count());
    assertEquals(0,
        pasteIntoStream(
            emptyList(),
            emptyList(),
            PASTE_BI_FUNCTION).count());
    assertEquals(0,
        pasteIntoStream(
            emptySet(),
            emptySet(),
            PASTE_BI_FUNCTION).count());
  }

  @Test
  public void pasteIntoStream_oneVsZero_throws() {
    assertIllegalArgumentException( () -> pasteIntoStream(
        singletonList(DUMMY_POSITIVE_INTEGER).iterator(),
        Collections.<Boolean>emptyList().iterator(),
        PASTE_BI_FUNCTION));
    assertIllegalArgumentException( () -> pasteIntoStream(
        singletonList(DUMMY_POSITIVE_INTEGER),
        emptyList(),
        PASTE_BI_FUNCTION));
    assertIllegalArgumentException( () -> pasteIntoStream(
        singleton(DUMMY_POSITIVE_INTEGER),
        emptySet(),
        PASTE_BI_FUNCTION));
  }

  @Test
  public void pasteIntoStream_zeroVsOne_throws() {
    assertIllegalArgumentException( () -> pasteIntoStream(
        Collections.<Integer>emptyList().iterator(),
        singletonList(DUMMY_BOOLEAN).iterator(),
        PASTE_BI_FUNCTION));
    assertIllegalArgumentException( () -> pasteIntoStream(
        emptyList(),
        singletonList(DUMMY_BOOLEAN),
        PASTE_BI_FUNCTION));
    assertIllegalArgumentException( () -> pasteIntoStream(
        emptySet(),
        singleton(DUMMY_BOOLEAN),
        PASTE_BI_FUNCTION));
  }

  @Test
  public void pasteIntoStream_oneVsTwo_throws() {
    assertIllegalArgumentException( () -> pasteIntoStream(
        singletonList(DUMMY_POSITIVE_INTEGER).iterator(),
        ImmutableList.of(DUMMY_BOOLEAN, DUMMY_BOOLEAN).iterator(),
        PASTE_BI_FUNCTION));
    assertIllegalArgumentException( () -> pasteIntoStream(
        singletonList(DUMMY_POSITIVE_INTEGER),
        ImmutableList.of(DUMMY_BOOLEAN, DUMMY_BOOLEAN),
        PASTE_BI_FUNCTION));
    assertIllegalArgumentException( () -> pasteIntoStream(
        singleton(DUMMY_POSITIVE_INTEGER),
        ImmutableSet.of(true, false), // can't use DUMMY_BOOLEAN here because the set will collapse these into 1 item
        PASTE_BI_FUNCTION));
  }

  @Test
  public void pasteIntoStream_twoVsOne_throws() {
    assertIllegalArgumentException( () -> pasteIntoStream(
        ImmutableList.of(1, 2).iterator(),
        singletonList(DUMMY_BOOLEAN).iterator(),
        PASTE_BI_FUNCTION));
    assertIllegalArgumentException( () -> pasteIntoStream(
        ImmutableList.of(1, 2),
        singletonList(DUMMY_BOOLEAN),
        PASTE_BI_FUNCTION));
    assertIllegalArgumentException( () -> pasteIntoStream(
        ImmutableSet.of(1, 2),
        singleton(DUMMY_BOOLEAN),
        PASTE_BI_FUNCTION));
  }

  @Test
  public void pasteIntoStream_happyPath() {
    assertEquals(
        ImmutableList.of("1true", "2false"),
        pasteIntoStream(
            ImmutableList.of(1, 2).iterator(),
            ImmutableList.of(true, false).iterator(),
            PASTE_BI_FUNCTION).collect(Collectors.toList()));
    assertEquals(
        ImmutableList.of("1true", "2false"),
        pasteIntoStream(
            ImmutableList.of(1, 2),
            ImmutableList.of(true, false),
            PASTE_BI_FUNCTION).collect(Collectors.toList()));
    assertEquals(
        2,
        pasteIntoStream(
            ImmutableList.of(1, 2).iterator(),
            ImmutableList.of(true, false).iterator(),
            PASTE_BI_FUNCTION).count());
  }

  @Test
  public void pasteIntoStream_3itemOverload_happyPath() {
    assertEquals(
        ImmutableList.of("1trueA", "2falseB"),
        pasteIntoStream(
            ImmutableList.of(1, 2).iterator(),
            ImmutableList.of(true, false).iterator(),
            ImmutableList.of("A", "B").iterator(),
            PASTE_TRI_FUNCTION).collect(Collectors.toList()));
    assertEquals(
        ImmutableList.of("1trueA", "2falseB"),
        pasteIntoStream(
            ImmutableList.of(1, 2),
            ImmutableList.of(true, false),
            ImmutableList.of("A", "B"),
            PASTE_TRI_FUNCTION).collect(Collectors.toList()));
    assertEquals(
        2,
        pasteIntoStream(
            ImmutableList.of(1, 2).iterator(),
            ImmutableList.of(true, false).iterator(),
            ImmutableList.of("A", "B").iterator(),
            PASTE_TRI_FUNCTION).count());
  }

  @Test
  public void testSumBigDecimals() {
    assertEquals(BigDecimal.ZERO, sumBigDecimals(Stream.<BigDecimal>empty()));
    assertEquals(
        BigDecimal.valueOf(1.23),
        sumBigDecimals(singletonList(BigDecimal.valueOf(1.23)).stream()));
    assertEquals(
        BigDecimal.valueOf(11),
        sumBigDecimals(ImmutableList.of(BigDecimal.ONE, BigDecimal.TEN).stream()));
    assertEquals(
        BigDecimal.TEN,
        sumBigDecimals(ImmutableList.of(BigDecimal.ZERO, BigDecimal.TEN).stream()));
    assertEquals(
        BigDecimal.valueOf(-9),
        sumBigDecimals(ImmutableList.of(BigDecimal.ONE, BigDecimal.valueOf(-10)).stream()));
  }

  @Test
  public void testSumNonNegativeBigDecimals() {
    assertEquals(BigDecimal.ZERO, sumNonNegativeBigDecimals(Stream.<BigDecimal>empty()));
    assertEquals(
        BigDecimal.valueOf(1.23),
        sumNonNegativeBigDecimals(singletonList(BigDecimal.valueOf(1.23)).stream()));
    assertEquals(
        BigDecimal.valueOf(11),
        sumNonNegativeBigDecimals(ImmutableList.of(BigDecimal.TEN, BigDecimal.ONE).stream()));
    assertEquals(
        BigDecimal.TEN,
        sumNonNegativeBigDecimals(ImmutableList.of(BigDecimal.TEN, BigDecimal.ZERO).stream()));
    assertIllegalArgumentException( () ->
        sumNonNegativeBigDecimals(ImmutableList.of(BigDecimal.TEN, BigDecimal.valueOf(-1)).stream()));
  }

  @Test
  public void testSumNonNegativeToDouble() {
    assertEquals(
        11.22,
        sumNonNegativePreciseValuesToDouble(ImmutableList.of(money(11.00), money(0.22))),
        1e-8);
    assertEquals(
        11.22,
        sumNonNegativePreciseValuesToDouble(ImmutableList.of(money(11.00), money(0.22)).stream()),
        1e-8);
    assertIllegalArgumentException( () ->
        sumNonNegativePreciseValuesToDouble(ImmutableList.of(signedMoney(11.00), signedMoney(-0.22))));
    assertIllegalArgumentException( () ->
        sumNonNegativePreciseValuesToDouble(ImmutableList.of(signedMoney(11.00), signedMoney(-0.22)).stream()));
  }

  @Test
  public void testConsecutivePairsStream() {
    Function<List<Integer>, List<String>> toResult = list ->
        consecutivePairsStream(list, (i11, i21) -> Strings.format("%s%s", i11, i21))
            .collect(Collectors.toList());
    assertEmpty(toResult.apply(emptyList()));
    assertEmpty(toResult.apply(singletonList(1)));
    assertEquals(
        singletonList("12"),
        toResult.apply(ImmutableList.of(1, 2)));
    assertEquals(
        ImmutableList.of("12", "23", "34"),
        toResult.apply(ImmutableList.of(1, 2, 3, 4)));
  }

  @Test
  public void testConsecutiveNonOverlappingPairsStream() {
    Function<List<Integer>, List<String>> toResult = list ->
        consecutiveNonOverlappingPairsStream(list, (i11, i21) -> Strings.format("%s%s", i11, i21))
            .collect(Collectors.toList());
    assertEmpty(toResult.apply(emptyList()));
    assertEquals(
        singletonList("12"),
        toResult.apply(ImmutableList.of(1, 2)));
    assertEquals(
        ImmutableList.of("12", "34"),
        toResult.apply(ImmutableList.of(1, 2, 3, 4)));
    assertEquals(
        ImmutableList.of("12", "34", "56"),
        toResult.apply(ImmutableList.of(1, 2, 3, 4, 5, 6)));

    // Odd counts of numbers means we can't make pairs perfectly, so it will throw an exception.
    assertIllegalArgumentException( () -> toResult.apply(Collections.nCopies(1, DUMMY_POSITIVE_INTEGER)));
    assertIllegalArgumentException( () -> toResult.apply(Collections.nCopies(3, DUMMY_POSITIVE_INTEGER)));
    assertIllegalArgumentException( () -> toResult.apply(Collections.nCopies(5, DUMMY_POSITIVE_INTEGER)));
    assertIllegalArgumentException( () -> toResult.apply(Collections.nCopies(7, DUMMY_POSITIVE_INTEGER)));
  }

  @Test
  public void averageOfDoubleStream_emptyStream_throws() {
    assertIllegalArgumentException( () -> averageOfDoubleStream(DoubleStream.empty()));
  }

  @Test
  public void averageOfDoubleStream_returnsAverage() {
    assertEquals(12.34, averageOfDoubleStream(DoubleStream.of(12.34)), 1e-8);
    assertEquals(5.5, averageOfDoubleStream(DoubleStream.of(3.5, 4.5, 8.5)), 1e-8);
  }

  @Test
  public void testStreamItemsAreUnique() {
    assertTrue(streamItemsAreUnique(Stream.empty()));
    assertTrue(streamItemsAreUnique(Stream.of("a")));
    assertTrue(streamItemsAreUnique(Stream.of("a", "b")));
    assertFalse(streamItemsAreUnique(Stream.of("a", "a")));
    assertFalse(streamItemsAreUnique(Stream.of("a", "b", "a")));
  }

  @Test
  public void test_streamForEachUniqueUnorderedPairInList() {
    assertIllegalArgumentException( () -> streamForEachUniqueUnorderedPairInList(emptyList()));
    assertIllegalArgumentException( () -> streamForEachUniqueUnorderedPairInList(singletonList(1.1)));

    BiConsumer<List<Double>, List<PairOfSameType<Double>>> asserter = (items, pairs) ->
        assertThat(
            streamForEachUniqueUnorderedPairInList(items)
                .collect(Collectors.toList()),
            orderedListMatcher(
                pairs,
                f -> pairOfSameTypeMatcher(f, f2 -> doubleAlmostEqualsMatcher(f2, DEFAULT_EPSILON_1e_8))));

    asserter.accept(
        ImmutableList.of(1.1, 2.2),
        singletonList(pairOfSameType(1.1, 2.2)));
    asserter.accept(
        ImmutableList.of(1.1, 2.2, 3.3),
        ImmutableList.of(
            pairOfSameType(1.1, 2.2),
            pairOfSameType(1.1, 3.3),
            pairOfSameType(2.2, 3.3)));
    asserter.accept(
        ImmutableList.of(1.1, 2.2, 3.3, 4.4),
        ImmutableList.of(
            pairOfSameType(1.1, 2.2),
            pairOfSameType(1.1, 3.3),
            pairOfSameType(1.1, 4.4),
            pairOfSameType(2.2, 3.3),
            pairOfSameType(2.2, 4.4),
            pairOfSameType(3.3, 4.4)));
  }

  @Test
  public void test_streamForEachUniquePair() {
    assertIllegalArgumentException( () -> streamForEachUniquePair(emptyRBSet()));
    assertIllegalArgumentException( () -> streamForEachUniquePair(singletonRBSet(1.1)));

    BiConsumer<RBSet<Double>, List<PairOfSameType<Double>>> asserter = (items, pairs) ->
        assertThat(
            streamForEachUniquePair(items)
                .collect(Collectors.toList()),
            orderedListMatcher(
                pairs,
                f -> pairOfSameTypeMatcher(f, f2 -> doubleAlmostEqualsMatcher(f2, DEFAULT_EPSILON_1e_8))));

    asserter.accept(
        rbSetOf(1.1, 2.2),
        ImmutableList.of(
            pairOfSameType(1.1, 2.2),
            pairOfSameType(2.2, 1.1)));
    asserter.accept(
        rbSetOf(1.1, 2.2, 3.3),
        ImmutableList.of(
            pairOfSameType(1.1, 2.2),
            pairOfSameType(1.1, 3.3),
            pairOfSameType(2.2, 1.1),
            pairOfSameType(2.2, 3.3),
            pairOfSameType(3.3, 1.1),
            pairOfSameType(3.3, 2.2)));
    asserter.accept(
        rbSetOf(1.1, 2.2, 3.3, 4.4),
        ImmutableList.of(
            pairOfSameType(1.1, 2.2),
            pairOfSameType(1.1, 3.3),
            pairOfSameType(1.1, 4.4),
            pairOfSameType(2.2, 1.1),
            pairOfSameType(2.2, 3.3),
            pairOfSameType(2.2, 4.4),
            pairOfSameType(3.3, 1.1),
            pairOfSameType(3.3, 2.2),
            pairOfSameType(3.3, 4.4),
            pairOfSameType(4.4, 1.1),
            pairOfSameType(4.4, 2.2),
            pairOfSameType(4.4, 3.3)));
  }

  @Test
  public void testStreamIsEmpty() {
    assertTrue(streamIsEmpty(Stream.empty()));
    assertTrue(streamIsEmpty(Stream.of()));
    assertFalse(streamIsEmpty(Stream.of("a")));
    assertFalse(streamIsEmpty(Stream.of("b")));
    assertFalse(streamIsEmpty(Stream.of("c")));
  }

  @Test
  public void testVarargListConcatenation() {
    assertEquals(ImmutableList.of("A", "B"),
        concatenateFirstAndRest("A", "B").collect(Collectors.toList()));
    assertEquals(ImmutableList.of("A", "B", "C"),
        concatenateFirstAndRest("A", "B", "C").collect(Collectors.toList()));
    assertEquals(ImmutableList.of("A", "B", "C", "C", "B", "A"),
        concatenateFirstAndRest("A", "B", "C", "C", "B", "A").collect(Collectors.toList()));
    assertEquals(ImmutableList.of("A", "B", "C"),
        concatenateFirstSecondAndRest("A", "B", "C").collect(Collectors.toList()));
    assertEquals(ImmutableList.of("A", "B", "C", "D"),
        concatenateFirstSecondAndRest("A", "B", "C", "D").collect(Collectors.toList()));
    assertEquals(ImmutableList.of("A", "B", "C"),
        concatenateFirstSecondThirdAndRest("A", "B", "C").collect(Collectors.toList()));
    assertEquals(ImmutableList.of("A", "B", "C", "D"),
        concatenateFirstSecondThirdAndRest("A", "B", "C", "D").collect(Collectors.toList()));
  }

  @Test
  public void testVarargListConcatenation_specialCaseOfDoubles() {
    // Strictly speaking, this could use an epsilon comparison, but I'm guessing that whenever you use the same
    // (finite-precision) representation for a double, the compiler generates the exact same number
    // (vs. if, say, the number was the result of some arithmetic operation such as 0.7 - 0.6)
    assertEquals(ImmutableList.of(.1, .2),
        concatenateFirstAndRest(.1, .2).collect(Collectors.toList()));
    assertEquals(ImmutableList.of(.1, .2, .3),
        concatenateFirstAndRest(.1, .2, .3).collect(Collectors.toList()));
    assertEquals(ImmutableList.of(.1, .2, .3, .3, .2, .1),
        concatenateFirstAndRest(.1, .2, .3, .3, .2, .1).collect(Collectors.toList()));
    assertEquals(ImmutableList.of(.1, .2, .3),
        concatenateFirstSecondAndRest(.1, .2, .3).collect(Collectors.toList()));
    assertEquals(ImmutableList.of(.1, .2, .3, .4),
        concatenateFirstSecondAndRest(.1, .2, .3, .4).collect(Collectors.toList()));
  }

  @Test
  public void testGetOnlyElementOrDefault() {
    Function<Stream<String>, String> maker = stream -> getOnlyElementOrDefault(stream, "x", "Can't be empty");
    assertEquals("x", maker.apply(Stream.empty()));
    assertEquals("a", maker.apply(Stream.of("a")));
    assertIllegalArgumentException( () -> maker.apply(Stream.of("a", "b")));
    assertIllegalArgumentException( () -> maker.apply(Stream.of("a", "b", "c")));
  }

  @Test
  public void testGetOptionalOnlyElement() {
    Function<Stream<String>, Optional<String>> maker = stream -> getOptionalOnlyElement(stream, DUMMY_STRING);
    assertOptionalEmpty(maker.apply(Stream.empty()));
    assertOptionalEquals("a", maker.apply(Stream.of("a")));
    assertIllegalArgumentException( () -> maker.apply(Stream.of("a", "b")));
    assertIllegalArgumentException( () -> maker.apply(Stream.of("a", "b", "c")));
  }

  @Test
  public void testConcatenateStreams() {
    assertEquals(
        ImmutableList.of("a", "b", "c", "d", "e", "f"),
        concatenateStreams(
            Stream.of("a", "b"),
            Stream.of("c", "d"),
            Stream.of("e", "f"))
            .collect(Collectors.toList()));
    assertEquals(
        ImmutableList.of(8, 7, 6, 5, 1, 2, 3, 4),
        concatenateStreams(
            Stream.of(8, 7),
            Stream.of(6, 5),
            Stream.of(1, 2),
            Stream.of(3, 4))
            .collect(Collectors.toList()));
  }

}
