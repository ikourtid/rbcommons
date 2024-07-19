package com.rb.nonbiz.collections;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rb.nonbiz.text.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.PairTest.pairEqualityMatcher;
import static com.rb.nonbiz.collections.RBLists.*;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalIntEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalIntEquals;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyListIterator;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RBListsTest {

  @Test
  public void testListConcatenation_varargs() {
    assertEmpty(listConcatenation());
    assertEmpty(listConcatenation(emptyList()));
    assertEmpty(listConcatenation(emptyList(), emptyList()));
    assertEquals(singletonList("a"), listConcatenation(singletonList("a"), emptyList()));
    assertEquals(singletonList("a"), listConcatenation(emptyList(), singletonList("a")));
    assertEquals(
        ImmutableList.of("a", "b", "x", "y", "1", "2"),
        listConcatenation(ImmutableList.of("a", "b"), ImmutableList.of("x", "y"), ImmutableList.of("1", "2")));
  }

  @Test
  public void testListConcatenation_iterator() {
    List<String> emptyList = emptyList(); // avoids type problems below
    assertEmpty(listConcatenation());
    assertEmpty(listConcatenation(emptyListIterator()));
    assertEmpty(listConcatenation(ImmutableList.of(emptyList, emptyList).iterator()));
    assertEquals(singletonList("a"), listConcatenation(ImmutableList.of(singletonList("a"), emptyList).iterator()));
    assertEquals(singletonList("a"), listConcatenation(ImmutableList.of(emptyList, singletonList("a")).iterator()));
    assertEquals(
        ImmutableList.of("a", "b", "x", "y", "1", "2"),
        listConcatenation(ImmutableList.<List<String>>of(
                ImmutableList.of("a", "b"),
                ImmutableList.of("x", "y"),
                ImmutableList.of("1", "2"))
            .iterator()));
  }

  @Test
  public void testListConcatenationWithoutDuplicates() {
    assertEmpty(listConcatenationWithoutDuplicates());
    assertEmpty(listConcatenationWithoutDuplicates(emptyList()));
    assertEmpty(listConcatenationWithoutDuplicates(emptyList(), emptyList()));
    assertEquals(singletonList("a"), listConcatenationWithoutDuplicates(singletonList("a"), emptyList()));
    assertEquals(singletonList("a"), listConcatenationWithoutDuplicates(emptyList(), singletonList("a")));
    assertEquals(
        ImmutableList.of("a", "b", "x", "y", "1", "2"),
        listConcatenationWithoutDuplicates(ImmutableList.of("a", "b"), ImmutableList.of("x", "y"), ImmutableList.of("1", "2")));

    assertEquals(
        ImmutableList.of("a", "b", "x", "2"),
        listConcatenationWithoutDuplicates(ImmutableList.of("a", "b"), ImmutableList.of("x", "a"), ImmutableList.of("a", "2")));
    assertEquals(
        singletonList("c"),
        listConcatenationWithoutDuplicates(ImmutableList.of("c", "c"), ImmutableList.of("c", "c", "c")));
  }

  @Test
  public void listIntersection() {
    assertEmpty(RBLists.intersection(emptyList(), emptyList()));
    assertEmpty(RBLists.intersection(singletonList("a"), emptyList()));
    assertEmpty(RBLists.intersection(emptyList(), singletonList("a")));
    assertEmpty(RBLists.intersection(emptyList(), singletonList("a")));
    assertEquals(
        ImmutableList.of(    "a",           "d",           "g",           "j",           "m",           "p"),
        RBLists.intersection(
            ImmutableList.of("a", "b",      "d", "e",      "g", "h",      "j", "k",      "m", "n",      "p", "q"),
            ImmutableList.of("a",      "c", "d",      "f", "g",      "i", "j",      "l", "m",      "o", "p"     )));
  }

  @Test
  public void testListComparator() {
    Comparator<List<Character>> comparator = RBLists.deterministicOrderedListComparator();
    for (List<List<Character>> orderedList : Collections2.permutations(ImmutableSet.of(
        Collections.<Character>emptyList(),
        singletonList('a'),
        ImmutableList.of('a', 'b'),
        ImmutableList.of('a', 'c'),
        ImmutableList.of('a', 'c', 'd'),
        ImmutableList.of('c', 'b', 'a')))) {
      assertEquals(
          ImmutableList.of(
              Collections.<Character>emptyList(),
              singletonList('a'),
              ImmutableList.of('a', 'b'),
              ImmutableList.of('a', 'c'),
              ImmutableList.of('a', 'c', 'd'),
              ImmutableList.of('c', 'b', 'a')),
          orderedList
              .stream()
              .sorted(comparator)
              .collect(Collectors.toList()));
    }
  }

  @Test
  public void testItemsAreUnique() {
    assertTrue(listItemsAreUnique(emptyList()));
    assertTrue(listItemsAreUnique(singletonList("a")));
    assertTrue(listItemsAreUnique(ImmutableList.of("a", "b")));
    assertFalse(listItemsAreUnique(ImmutableList.of("a", "a")));
    assertFalse(listItemsAreUnique(ImmutableList.of("a", "b", "a")));
  }

  @Test
  public void testSortEfficientlyToList_noSecondarySort() {
    List<String> strings = ImmutableList.of("a", "dddd", "ccc", "bb");
    assertEquals(
        ImmutableList.of("a", "bb", "ccc", "dddd"),
        sortEfficientlyToList(strings.stream(), s -> s.length()));
  }

  @Test
  public void testSortEfficientlyToList_hasSecondarySort() {
    List<String> strings = ImmutableList.of("a", "d", "c", "b");
    assertEquals(
        ImmutableList.of("a", "b", "c", "d"),
        sortEfficientlyToList(
            strings.stream(),
            s -> s.length(), // length is 1 everywhere, so that doesn't uniquely specify a sort
            String::compareTo)); // however, string comparison does
  }

  @Test
  public void testGetFirstNonUniqueListItem() {
    assertOptionalEmpty(getFirstNonUniqueListItem(emptyList()));
    assertOptionalEmpty(getFirstNonUniqueListItem(singletonList("a")));
    assertOptionalEmpty(getFirstNonUniqueListItem(ImmutableList.of("a", "b")));
    assertOptionalEmpty(getFirstNonUniqueListItem(ImmutableList.of("a", "b", "c")));
    assertOptionalEquals("a", getFirstNonUniqueListItem(ImmutableList.of("a", "a", "c")));
    assertOptionalEquals("a", getFirstNonUniqueListItem(ImmutableList.of("a", "b", "a")));
    assertOptionalEquals("b", getFirstNonUniqueListItem(ImmutableList.of("a", "b", "b")));
  }

  @Test
  public void testListItemsAreUnique() {
    Assert.assertTrue(listItemsAreUnique(emptyList()));
    Assert.assertTrue(listItemsAreUnique(singletonList("a")));
    Assert.assertTrue(listItemsAreUnique(ImmutableList.of("a", "b")));
    Assert.assertTrue(listItemsAreUnique(ImmutableList.of("a", "b", "c")));
    assertFalse(listItemsAreUnique(ImmutableList.of("a", "a", "c")));
    assertFalse(listItemsAreUnique(ImmutableList.of("a", "b", "a")));
    assertFalse(listItemsAreUnique(ImmutableList.of("a", "b", "b")));
  }

  @Test
  public void testVarargListConcatenation() {
    assertEquals(ImmutableList.of("A", "B"), concatenateFirstAndRest("A", "B"));
    assertEquals(ImmutableList.of("A", "B", "C"), concatenateFirstAndRest("A", "B", "C"));
    assertEquals(ImmutableList.of("A", "B", "C", "C", "B", "A"), concatenateFirstAndRest("A", "B", "C", "C", "B", "A"));
    assertEquals(ImmutableList.of("A", "B", "C"), concatenateFirstSecondAndRest("A", "B", "C"));
    assertEquals(ImmutableList.of("A", "B", "C", "D"), concatenateFirstSecondAndRest("A", "B", "C", "D"));
    assertEquals(ImmutableList.of("A", "B", "C"), concatenateFirstSecondThirdAndRest("A", "B", "C"));
    assertEquals(ImmutableList.of("A", "B", "C", "D"), concatenateFirstSecondThirdAndRest("A", "B", "C", "D"));
  }

  @Test
  public void testVarargListConcatenation_specialCaseOfDoubles() {
    // Strictly speaking, this could use an epsilon comparison, but I'm guessing that whenever you use the same
    // (finite-precision) representation for a double, the compiler generates the exact same number
    // (vs. if, say, the number was the result of some arithmetic operation such as 0.7 - 0.6)
    assertEquals(ImmutableList.of(.1, .2), concatenateFirstAndRest(.1, .2));
    assertEquals(ImmutableList.of(.1, .2, .3), concatenateFirstAndRest(.1, .2, .3));
    assertEquals(ImmutableList.of(.1, .2, .3, .3, .2, .1), concatenateFirstAndRest(.1, .2, .3, .3, .2, .1));
    assertEquals(ImmutableList.of(.1, .2, .3), concatenateFirstSecondAndRest(.1, .2, .3));
    assertEquals(ImmutableList.of(.1, .2, .3, .4), concatenateFirstSecondAndRest(.1, .2, .3, .4));
  }

  @Test
  public void testListConcatenationFromEach() {
    Function<Integer, Stream<String>> transformer = intValue -> Stream.of(
        Strings.format("%s_A", intValue),
        Strings.format("%s_B", intValue));
    assertEmpty(listConcatenationFromEach(emptyList(),            transformer));
    assertEquals(
        listConcatenationFromEach(singletonList(1), transformer),
        ImmutableList.of(
            "1_A",
            "1_B"));
    assertEquals(
        listConcatenationFromEach(ImmutableList.of(1, 2), transformer),
        ImmutableList.of(
            "1_A",
            "1_B",
            "2_A",
            "2_B"));
  }

  @Test
  public void testListConcatenationFromEachPair() {
    BiFunction<Integer, Boolean, List<String>> transformer = (intValue, boolValue) -> ImmutableList.of(
        Strings.format("%s_%s_A", intValue, boolValue),
        Strings.format("%s_%s_B", intValue, boolValue));
    assertEmpty(listConcatenationFromEachPair(emptyList(),            emptyList(),                   transformer));
    assertEmpty(listConcatenationFromEachPair(singletonList(1),       emptyList(),                   transformer));
    assertEmpty(listConcatenationFromEachPair(ImmutableList.of(1, 2), emptyList(),                   transformer));

    assertEmpty(listConcatenationFromEachPair(emptyList(),            singletonList(false),          transformer));
    assertEmpty(listConcatenationFromEachPair(emptyList(),            ImmutableList.of(false, true), transformer));

    assertEquals(
        listConcatenationFromEachPair(singletonList(1), singletonList(false), transformer),
        ImmutableList.of(
            "1_false_A",
            "1_false_B"));
    assertEquals(
        listConcatenationFromEachPair(ImmutableList.of(1, 2), ImmutableList.of(false, true), transformer),
        ImmutableList.of(
            "1_false_A",
            "1_false_B",
            "1_true_A",
            "1_true_B",
            "2_false_A",
            "2_false_B",
            "2_true_A",
            "2_true_B"));
  }

  @Test
  public void testNewRBListWithExpectedSize() {
    assertTrue(newRBListWithExpectedSize(0, emptyIterator()).isEmpty());
    assertTrue(newRBListWithExpectedSize(123, emptyIterator()).isEmpty());

    assertEquals(
        ImmutableList.of("a", "b", "c"),
        newRBListWithExpectedSize(3, ImmutableList.of("a", "b", "c").iterator()));
  }

  @Test
  public void testPasteLists() {
    Function<List<Integer>, List<String>> maker = intList -> pasteLists(
        intList,
        ImmutableList.of(true, false),
        (intValue, booleanValue) -> Strings.format("%s_%s", intValue, booleanValue));
    assertEquals(
        ImmutableList.of("1_true", "2_false"),
        maker.apply(ImmutableList.of(1, 2)));

    // too few / many items in list => sizes don't match
    assertIllegalArgumentException( () -> maker.apply(singletonList(DUMMY_POSITIVE_INTEGER)));
    assertIllegalArgumentException( () -> maker.apply(Collections.nCopies(3, DUMMY_POSITIVE_INTEGER)));
  }

  @Test
  public void testPasteLists_worksOnEmpty() {
    assertEquals(
        emptyList(),
        pasteLists(
            emptyList(),
            emptyList(),
            (intValue, booleanValue) -> Strings.format("%s_%s", intValue, booleanValue)));
  }

  @Test
  public void testFindIndexOfFirstConsecutivePair() {
    Function<List<Double>, OptionalInt> findFirstDecrease = list ->
        findIndexOfFirstConsecutivePair(list, (v1, v2) -> v1 > v2);

    assertOptionalIntEmpty(findFirstDecrease.apply(emptyList()));
    assertOptionalIntEmpty(findFirstDecrease.apply(singletonList(7.7)));
    assertOptionalIntEmpty(findFirstDecrease.apply(ImmutableList.of(7.7, 8.8)));
    assertOptionalIntEmpty(findFirstDecrease.apply(ImmutableList.of(7.7, 8.8, 9.9)));

    assertOptionalIntEquals(0, findFirstDecrease.apply(ImmutableList.of(7.7, 6.6, 9.9)));
    assertOptionalIntEquals(0, findFirstDecrease.apply(ImmutableList.of(7.7, 6.6, 5.5)));

    assertOptionalIntEquals(1, findFirstDecrease.apply(ImmutableList.of(7.7, 9.9, 8.8)));
  }

  @Test
  public void testPossiblyReduceConsecutiveItems() {
    BiConsumer<List<Pair<String, Integer>>, List<Pair<String, Integer>>> asserter = (inputList, expectedReducedList) ->
        assertThat(
            possiblyReduceConsecutiveItems(
                inputList,
                (v1, v2) -> v1.getLeft().equals(v2.getLeft()),
                (v1, v2) -> pair(v1.getLeft(), v1.getRight() + v2.getRight())),
            orderedListMatcher(
                expectedReducedList,
                f -> pairEqualityMatcher(f)));

    // Simple cases of no reduction
    rbSetOf(
        Collections.<Pair<String, Integer>>emptyList(),
        ImmutableList.of(pair("a", 100)),
        ImmutableList.of(pair("a", 100), pair("b", 101)),
        ImmutableList.of(pair("a", 100), pair("b", 100)),
        ImmutableList.of(pair("a", 100), pair("b", 101), pair("c", 102)),
        ImmutableList.of(pair("a", 100), pair("b", 100), pair("c", 102)),
        ImmutableList.of(pair("a", 100), pair("b", 101), pair("c", 101)),
        ImmutableList.of(pair("a", 100), pair("b", 100), pair("c", 100)))
        .forEach(unchangedList ->
            asserter.accept(unchangedList, unchangedList));

    // Single reduction on first item only
    asserter.accept(
        ImmutableList.of(
            pair("a", 100),
            pair("a", 101)),
        singletonList(
            pair("a", intExplained(201, 100 + 101))));
    asserter.accept(
        ImmutableList.of(
            pair("a", 100),
            pair("a", 101),
            pair("b", 777)),
        ImmutableList.of(
            pair("a", intExplained(201, 100 + 101)),
            pair("b", 777)));

    // Single reduction on second first item only
    asserter.accept(
        ImmutableList.of(
            pair("x", 777),
            pair("a", 100),
            pair("a", 101)),
        ImmutableList.of(
            pair("x", 777),
            pair("a", intExplained(201, 100 + 101))));
    asserter.accept(
        ImmutableList.of(
            pair("x", 777),
            pair("a", 100),
            pair("a", 101),
            pair("y", 888)),
        ImmutableList.of(
            pair("x", 777),
            pair("a", intExplained(201, 100 + 101)),
            pair("y", 888)));

    // Multiple reductions on first item
    asserter.accept(
        ImmutableList.of(
            pair("a", 100),
            pair("a", 101),
            pair("a", 102)),
        singletonList(
            pair("a", intExplained(303, 100 + 101 + 102))));
    asserter.accept(
        ImmutableList.of(
            pair("a", 100),
            pair("a", 101),
            pair("a", 102),
            pair("y", 888)),
        ImmutableList.of(
            pair("a", intExplained(303, 100 + 101 + 102)),
            pair("y", 888)));

    // Multiple reductions on second item
    asserter.accept(
        ImmutableList.of(
            pair("x", 777),
            pair("a", 100),
            pair("a", 101),
            pair("a", 102)),
        ImmutableList.of(
            pair("x", 777),
            pair("a", intExplained(303, 100 + 101 + 102))));
    asserter.accept(
        ImmutableList.of(
            pair("x", 777),
            pair("a", 100),
            pair("a", 101),
            pair("a", 102),
            pair("y", 888)),
        ImmutableList.of(
            pair("x", 777),
            pair("a", intExplained(303, 100 + 101 + 102)),
            pair("y", 888)));

    // Most general case: two sets of multiple reductions in the middle
    asserter.accept(
        ImmutableList.of(
            pair("x", 777),
            pair("a", 100),
            pair("a", 101),
            pair("a", 102),
            pair("y", 888),
            pair("a", 200),
            pair("a", 201),
            pair("a", 202)),
        ImmutableList.of(
            pair("x", 777),
            pair("a", intExplained(303, 100 + 101 + 102)),
            pair("y", 888),
            pair("a", intExplained(603, 200 + 201 + 202))));
    asserter.accept(
        ImmutableList.of(
            pair("x", 777),
            pair("a", 100),
            pair("a", 101),
            pair("a", 102),
            pair("y", 888),
            pair("a", 200),
            pair("a", 201),
            pair("a", 202),
            pair("z", 999)),
        ImmutableList.of(
            pair("x", 777),
            pair("a", intExplained(303, 100 + 101 + 102)),
            pair("y", 888),
            pair("a", intExplained(603, 200 + 201 + 202)),
            pair("z", 999)));
    asserter.accept(
        ImmutableList.of(
            pair("x", 777),
            pair("a", 100),
            pair("a", 101),
            pair("a", 102),
            pair("y", 888),
            pair("q", 555),
            pair("a", 200),
            pair("a", 201),
            pair("a", 202),
            pair("z", 999)),
        ImmutableList.of(
            pair("x", 777),
            pair("a", intExplained(303, 100 + 101 + 102)),
            pair("y", 888),
            pair("q", 555),
            pair("a", intExplained(603, 200 + 201 + 202)),
            pair("z", 999)));
  }

  @Test
  public void testListsAreSimilar() {
    Predicate<List<Double>> predicate = list ->
        listsAreSimilar(ImmutableList.of(1.1, 3.3), list, (v1, v2) -> DEFAULT_EPSILON_1e_8.valuesAreWithin(v1, v2));

    rbSetOf(
        ImmutableList.of(1.1, 3.3),
        ImmutableList.of(1.1 - 1e-9, 3.3 - 1e-9),
        ImmutableList.of(1.1 + 1e-9, 3.3 + 1e-9))
        .forEach(list -> assertTrue(predicate.test(list)));

    rbSetOf(
        ImmutableList.of(1.1 - 1e-7, 3.3 - 1e-7),
        ImmutableList.of(1.1 + 1e-7, 3.3 + 1e-7),
        ImmutableList.of(1.1,        3.3 + 1e-7),
        ImmutableList.of(1.1 + 1e-7, 3.3),
        Collections.<Double>emptyList(),
        singletonList(1.1),
        ImmutableList.of(1.1, 3.3, DUMMY_DOUBLE))
        .forEach(list -> assertFalse(predicate.test(list)));
  }

  @Test
  public void testTransformUsingBothIndexAndValue() {
    BiConsumer<List<Boolean>, List<String>> asserter = (starting, transformed) ->
        assertThat(
            transformUsingBothIndexAndValue(
                starting,
                (i, str) -> Strings.format("%s_%s", i, str)),
            orderedListEqualityMatcher(
                transformed));

    asserter.accept(
        ImmutableList.of(true, false),
        ImmutableList.of("0_true", "1_false"));
    asserter.accept(
        emptyList(),
        emptyList());
  }

  @Test
  public void testCopyWithModifiedElement() {
    assertIllegalArgumentException( () -> copyWithModifiedElement(emptyList(), 0, v -> v + "_"));

    assertIllegalArgumentException( () -> copyWithModifiedElement(singletonList("a"), -1, v -> v + "_"));
    assertEquals(singletonList("a_"),     copyWithModifiedElement(singletonList("a"),  0, v -> v + "_"));
    assertIllegalArgumentException( () -> copyWithModifiedElement(singletonList("a"),  1, v -> v + "_"));

    assertIllegalArgumentException( () ->           copyWithModifiedElement(ImmutableList.of("a", "b", "c"), -1, v -> v + "_"));
    assertEquals(ImmutableList.of("a_", "b", "c"),  copyWithModifiedElement(ImmutableList.of("a", "b", "c"),  0, v -> v + "_"));
    assertEquals(ImmutableList.of("a",  "b_", "c"), copyWithModifiedElement(ImmutableList.of("a", "b", "c"),  1, v -> v + "_"));
    assertEquals(ImmutableList.of("a",  "b", "c_"), copyWithModifiedElement(ImmutableList.of("a", "b", "c"),  2, v -> v + "_"));
    assertIllegalArgumentException( () ->           copyWithModifiedElement(ImmutableList.of("a", "b", "c"),  3, v -> v + "_"));
  }

  @Test
  public void testGetListPrefixWherePredicateHoldsContiguously() {
    BiConsumer<List<String>, List<String>> asserter = (original, expectedResult) ->
        assertThat(
            // This will only keep the first contiguous N strings that have a length of 1 or 0.
            getListPrefixWherePredicateHoldsContiguously(original, v -> v.length() <= 1),
            orderedListEqualityMatcher(expectedResult));

    asserter.accept(ImmutableList.of("a",  "b",  "c",  "d"), ImmutableList.of("a", "b", "c", "d"));
    asserter.accept(ImmutableList.of("aa", "b",  "c",  "d"), ImmutableList.of(                  ));
    asserter.accept(ImmutableList.of("a",  "bb", "c",  "d"), ImmutableList.of("a"               ));
    asserter.accept(ImmutableList.of("a",  "b", "cc",  "d"), ImmutableList.of("a", "b"          ));
    asserter.accept(ImmutableList.of("a",  "b",  "c", "dd"), ImmutableList.of("a", "b", "c"     ));

    asserter.accept(ImmutableList.of("a",  "b",  "c"), ImmutableList.of("a", "b", "c"));
    asserter.accept(ImmutableList.of("aa", "b",  "c"), ImmutableList.of(             ));
    asserter.accept(ImmutableList.of("a",  "bb", "c"), ImmutableList.of("a"          ));
    asserter.accept(ImmutableList.of("a",  "b", "cc"), ImmutableList.of("a", "b"     ));

    asserter.accept(ImmutableList.of("a",  "b"),  ImmutableList.of("a", "b"));
    asserter.accept(ImmutableList.of("aa", "b"),  ImmutableList.of(        ));
    asserter.accept(ImmutableList.of("a",  "bb"), ImmutableList.of("a"     ));

    asserter.accept(ImmutableList.of("a"),  ImmutableList.of("a"));
    asserter.accept(ImmutableList.of("aa"), ImmutableList.of(  ));

    asserter.accept(emptyList(), emptyList());
  }

  @Test
  public void testGetListSuffixWherePredicateHoldsContiguously() {
    BiConsumer<List<String>, List<String>> asserter = (original, expectedResult) ->
        assertThat(
            // This will only keep the last contiguous N strings that have a length of 1 or 0.
            getListSuffixWherePredicateHoldsContiguously(original, v -> v.length() <= 1),
            orderedListEqualityMatcher(expectedResult));

    asserter.accept(ImmutableList.of("a",  "b",  "c",  "d"), ImmutableList.of("a", "b", "c", "d"));
    asserter.accept(ImmutableList.of("aa", "b",  "c",  "d"), ImmutableList.of(     "b", "c", "d"));
    asserter.accept(ImmutableList.of("a",  "bb", "c",  "d"), ImmutableList.of(          "c", "d"));
    asserter.accept(ImmutableList.of("a",  "b", "cc",  "d"), ImmutableList.of(               "d"));
    asserter.accept(ImmutableList.of("a",  "b",  "c", "dd"), ImmutableList.of(                  ));

    asserter.accept(ImmutableList.of("a",  "b",  "c"), ImmutableList.of("a", "b", "c"));
    asserter.accept(ImmutableList.of("aa", "b",  "c"), ImmutableList.of(     "b", "c"));
    asserter.accept(ImmutableList.of("a",  "bb", "c"), ImmutableList.of(          "c"));
    asserter.accept(ImmutableList.of("a",  "b", "cc"), ImmutableList.of(             ));

    asserter.accept(ImmutableList.of("a",  "b"),  ImmutableList.of("a", "b"));
    asserter.accept(ImmutableList.of("aa", "b"),  ImmutableList.of(     "b"));
    asserter.accept(ImmutableList.of("a",  "bb"), ImmutableList.of(        ));

    asserter.accept(ImmutableList.of("a"),  ImmutableList.of("a"));
    asserter.accept(ImmutableList.of("aa"), ImmutableList.of(  ));

    asserter.accept(emptyList(), emptyList());
  }

}
