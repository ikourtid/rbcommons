package com.rb.nonbiz.collections;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.functional.TriConsumer;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.PartialComparisonResult.definedPartialComparison;
import static com.rb.nonbiz.collections.PartialComparisonResult.noOrderingDefined;
import static com.rb.nonbiz.collections.PartialComparisonResultTest.partialComparisonResultMatcher;
import static com.rb.nonbiz.collections.RBComparators.compareOptionalDoubles;
import static com.rb.nonbiz.collections.RBComparators.compareOptionalInts;
import static com.rb.nonbiz.collections.RBComparators.compareOptionals;
import static com.rb.nonbiz.collections.RBComparators.composeComparators;
import static com.rb.nonbiz.collections.RBComparators.increasingPerComparator;
import static com.rb.nonbiz.collections.RBComparators.maxFromComparator;
import static com.rb.nonbiz.collections.RBComparators.minFromComparator;
import static com.rb.nonbiz.collections.RBComparators.nonDecreasingPerComparator;
import static com.rb.nonbiz.collections.RBComparators.partiallyCompareOptionals;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBComparatorsTest {

  @Test
  public void testMinAndMaxFromComparator() {
    assertEquals("b", maxFromComparator(String::compareTo, "a", "b"));
    assertEquals("b", maxFromComparator(String::compareTo, "b", "a"));
    assertEquals("b", maxFromComparator(String::compareTo, "b", "b"));

    assertEquals("a", minFromComparator(String::compareTo, "a", "b"));
    assertEquals("a", minFromComparator(String::compareTo, "b", "a"));
    assertEquals("b", minFromComparator(String::compareTo, "b", "b"));
  }

  @Test
  public void testComposeComparators_singleComparator() {
    // Since the comparator has code in it, the best way to test it is to see how it sorts things.
    List<String> sorted = ImmutableList.of("a", "b", "c", "d");
    Collections2.permutations(sorted).forEach(unsorted -> assertEquals(
        "When using only 1 comparator, the result of the composition is just that comparator",
        sorted,
        unsorted
            .stream()
            .sorted(composeComparators(String::compareTo))
            .collect(Collectors.toList())));
  }

  @Test
  public void testComposeComparators_twoComparators() {
    // Since the comparator has code in it, the best way to test it is to see how it sorts things.
    List<String> sorted = ImmutableList.of("a", "d", "bx", "by");
    Collections2.permutations(sorted).forEach(unsorted -> assertEquals(
        "When using 2 comparators, the result of the composition is just that comparator",
        sorted,
        unsorted
            .stream()
            .sorted(composeComparators(comparing(s -> s.length()), String::compareTo))
            .collect(Collectors.toList())));
  }

  @Test
  public void testCompareOptionals() {
    // This ordering will make the tests look more legible in English
    TriConsumer<Optional<String>, PartialComparisonResult, Optional<String>> asserter = (v1, result, v2) ->
        assertThat(
            compareOptionals(String::compareTo, v1, v2),
            partialComparisonResultMatcher(result));

    asserter.accept(Optional.empty(), noOrderingDefined(), Optional.of("A"));
    asserter.accept(Optional.of("A"), noOrderingDefined(), Optional.empty());

    asserter.accept(Optional.empty(), PartialComparisonResult.equal(),       Optional.empty());
    asserter.accept(Optional.of("A"), PartialComparisonResult.equal(),       Optional.of("A"));
    asserter.accept(Optional.of("A"), PartialComparisonResult.lessThan(),    Optional.of("B"));
    asserter.accept(Optional.of("B"), PartialComparisonResult.greaterThan(), Optional.of("A"));
  }

  @Test
  public void testPartiallyCompareOptionals() {
    // This ordering will make the tests look more legible in English
    TriConsumer<Optional<String>, PartialComparisonResult, Optional<String>> asserter = (v1, result, v2) ->
        assertThat(
            // For this test, we have defined comparison against the empty string to be noOrderingDefined(),
            // so we can test what happens then.
            partiallyCompareOptionals( (x, y) ->
                    x.equals("") || y.equals("")
                        ? noOrderingDefined()
                        : definedPartialComparison(x.compareTo(y)),
                v1,
                v2),
            partialComparisonResultMatcher(result));

    asserter.accept(Optional.empty(), noOrderingDefined(), Optional.of("A"));
    asserter.accept(Optional.of("A"), noOrderingDefined(), Optional.empty());

    asserter.accept(Optional.empty(), PartialComparisonResult.equal(),       Optional.empty());
    asserter.accept(Optional.of("A"), PartialComparisonResult.equal(),       Optional.of("A"));
    asserter.accept(Optional.of("A"), PartialComparisonResult.lessThan(),    Optional.of("B"));
    asserter.accept(Optional.of("B"), PartialComparisonResult.greaterThan(), Optional.of("A"));

    asserter.accept(Optional.empty(), noOrderingDefined(), Optional.of(""));
    asserter.accept(Optional.of(""),  noOrderingDefined(), Optional.empty());
    asserter.accept(Optional.of(""),  noOrderingDefined(), Optional.of(""));
    asserter.accept(Optional.of(""),  noOrderingDefined(), Optional.of("A"));
    asserter.accept(Optional.of("A"), noOrderingDefined(), Optional.of(""));
  }

  @Test
  public void testCompareOptionalDoubles() {
    // This ordering will make the tests look more legible in English
    TriConsumer<OptionalDouble, PartialComparisonResult, OptionalDouble> asserter = (v1, result, v2) ->
        assertThat(
            compareOptionalDoubles(v1, v2),
            partialComparisonResultMatcher(result));

    asserter.accept(OptionalDouble.empty(), noOrderingDefined(), OptionalDouble.of(1.1));
    asserter.accept(OptionalDouble.of(1.1), noOrderingDefined(), OptionalDouble.empty());

    asserter.accept(OptionalDouble.empty(), PartialComparisonResult.equal(),       OptionalDouble.empty());
    asserter.accept(OptionalDouble.of(1.1), PartialComparisonResult.equal(),       OptionalDouble.of(1.1));
    asserter.accept(OptionalDouble.of(1.1), PartialComparisonResult.lessThan(),    OptionalDouble.of(2.2));
    asserter.accept(OptionalDouble.of(2.2), PartialComparisonResult.greaterThan(), OptionalDouble.of(1.1));
  }
  
  @Test
  public void testCompareOptionalInts() {
    // This ordering will make the tests look more legible in English
    TriConsumer<OptionalInt, PartialComparisonResult, OptionalInt> asserter = (v1, result, v2) ->
        assertThat(
            compareOptionalInts(v1, v2),
            partialComparisonResultMatcher(result));

    asserter.accept(OptionalInt.empty(), noOrderingDefined(), OptionalInt.of(11));
    asserter.accept(OptionalInt.of(11),  noOrderingDefined(), OptionalInt.empty());

    asserter.accept(OptionalInt.empty(), PartialComparisonResult.equal(),       OptionalInt.empty());
    asserter.accept(OptionalInt.of(11),  PartialComparisonResult.equal(),       OptionalInt.of(11));
    asserter.accept(OptionalInt.of(11),  PartialComparisonResult.lessThan(),    OptionalInt.of(22));
    asserter.accept(OptionalInt.of(22),  PartialComparisonResult.greaterThan(), OptionalInt.of(11));
  }

  @Test
  public void testNonDecreasingPerComparator() {
    assertTrue(nonDecreasingPerComparator(Double::compare, 1.1, 2.2, 3.3));
    assertTrue(nonDecreasingPerComparator(Double::compare, 1.1, 1.1, 3.3));
    assertTrue(nonDecreasingPerComparator(Double::compare, 1.1, 3.3, 3.3));
    assertTrue(nonDecreasingPerComparator(Double::compare, 1.1, 1.1, 1.1));

    assertFalse(nonDecreasingPerComparator(Double::compare, 2.3, 2.2, 3.3));
    assertFalse(nonDecreasingPerComparator(Double::compare, 3.4, 2.2, 3.3));
    assertFalse(nonDecreasingPerComparator(Double::compare, 1.1, 1.0, 3.3));
    assertFalse(nonDecreasingPerComparator(Double::compare, 1.1, 3.4, 3.3));
    assertFalse(nonDecreasingPerComparator(Double::compare, 1.1, 2.2, 1.0));
    assertFalse(nonDecreasingPerComparator(Double::compare, 1.1, 2.2, 2.1));
  }

  @Test
  public void testIncreasingPerComparator() {
    assertTrue(increasingPerComparator(Double::compare, 1.1, 2.2, 3.3));
    assertFalse(increasingPerComparator(Double::compare, 1.1, 1.1, 3.3));
    assertFalse(increasingPerComparator(Double::compare, 1.1, 3.3, 3.3));
    assertFalse(increasingPerComparator(Double::compare, 1.1, 1.1, 1.1));

    assertFalse(increasingPerComparator(Double::compare, 2.3, 2.2, 3.3));
    assertFalse(increasingPerComparator(Double::compare, 3.4, 2.2, 3.3));
    assertFalse(increasingPerComparator(Double::compare, 1.1, 1.0, 3.3));
    assertFalse(increasingPerComparator(Double::compare, 1.1, 3.4, 3.3));
    assertFalse(increasingPerComparator(Double::compare, 1.1, 2.2, 1.0));
    assertFalse(increasingPerComparator(Double::compare, 1.1, 2.2, 2.1));
  }

  @Test
  public void testComparingReversed() {
    assertEquals(
        ImmutableList.of(1, 2, 3, 4, 5),
        Stream.of(1, 3, 5, 2, 4)
            .sorted(comparing(v -> v))
            .collect(Collectors.toList()));
    assertEquals(
        ImmutableList.of(5, 4, 3, 2, 1),
        Stream.of(1, 3, 5, 2, 4)
            .sorted(comparing(v -> v, reverseOrder()))
            .collect(Collectors.toList()));
  }

  @Test
  public void makeComparator() {
    class StringAndInt {

      String string;
      Integer number;

      public StringAndInt(String string, Integer number) {
        this.string = string;
        this.number = number;
      }

    }

    StringAndInt a1 = new StringAndInt("a", 1);
    StringAndInt a2 = new StringAndInt("a", 2);
    StringAndInt b1 = new StringAndInt("b", 1);
    StringAndInt b2 = new StringAndInt("b", 2);

    Comparator<StringAndInt> comparator = RBComparators.<StringAndInt>makeComparator(
        stringAndInt -> stringAndInt.string,
        stringAndInt -> stringAndInt.number);
    List<StringAndInt> expectedSorting = ImmutableList.of(a1, a2, b1, b2);
    for (int i = 0; i < expectedSorting.size(); i++) {
      for (int j = 0; j < expectedSorting.size(); j++) {
        StringAndInt itemI = expectedSorting.get(i);
        StringAndInt itemJ = expectedSorting.get(j);
        if (i < j) {
          assertTrue(comparator.compare(itemI, itemJ) < 0);
          assertTrue(comparator.compare(itemJ, itemI) > 0);
        } else if (i > j) {
          assertTrue(comparator.compare(itemI, itemJ) > 0);
          assertTrue(comparator.compare(itemJ, itemI) < 0);
        } else {
          assertTrue(comparator.compare(itemI, itemJ) == 0);
          assertTrue(comparator.compare(itemJ, itemI) == 0);
        }
      }
    }
  }

}
