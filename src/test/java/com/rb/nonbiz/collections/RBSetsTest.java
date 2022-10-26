package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.RBSets.TwoRBSetsVisitor;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_E;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.collections.RBSets.difference;
import static com.rb.nonbiz.collections.RBSets.equalAsSets;
import static com.rb.nonbiz.collections.RBSets.intersection;
import static com.rb.nonbiz.collections.RBSets.isSubsetOf;
import static com.rb.nonbiz.collections.RBSets.noSharedItems;
import static com.rb.nonbiz.collections.RBSets.setUnionOfFirstAndRest;
import static com.rb.nonbiz.collections.RBSets.setUnionOfFirstSecondAndRest;
import static com.rb.nonbiz.collections.RBSets.toRBSetIfUnique;
import static com.rb.nonbiz.collections.RBSets.union;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBSetsTest {

  @Test
  public void testNoSharedItems() {
    assertTrue( noSharedItems(rbSetOf(1, 2), rbSetOf(3, 4), rbSetOf(5, 6)));
    assertFalse(noSharedItems(rbSetOf(1, 2), rbSetOf(3, 1), rbSetOf(5, 6)));
    assertFalse(noSharedItems(rbSetOf(1, 2), rbSetOf(1, 4), rbSetOf(5, 6)));
    assertFalse(noSharedItems(rbSetOf(1, 2), rbSetOf(2, 4), rbSetOf(5, 6)));
    assertFalse(noSharedItems(rbSetOf(1, 2), rbSetOf(3, 4), rbSetOf(3, 6)));
    assertFalse(noSharedItems(rbSetOf(1, 2), rbSetOf(3, 4), rbSetOf(5, 3)));

    assertTrue(noSharedItems(emptyRBSet()));
    assertTrue(noSharedItems(emptyRBSet(), emptyRBSet()));
    assertTrue(noSharedItems(emptyRBSet(), emptyRBSet(), emptyRBSet()));
    assertTrue(noSharedItems(singletonRBSet(1)));
    assertTrue(noSharedItems(rbSetOf(1, 2)));

    // can also compare RBSet and Set:
    assertTrue( noSharedItems(rbSetOf(1, 2), ImmutableSet.of(5, 6)));
    assertFalse(noSharedItems(rbSetOf(1, 2), ImmutableSet.of(3, 1)));

    assertTrue( noSharedItems(ImmutableSet.of(5, 6), rbSetOf(1, 2)));
    assertFalse(noSharedItems(ImmutableSet.of(3, 1), rbSetOf(1, 2)));

  }

  @Test
  public void testIsSubset() {
    expectIsSubset(true, emptyRBSet(), emptyRBSet());
    expectIsSubset(true, emptyRBSet(), singletonRBSet(1));
    expectIsSubset(false, singletonRBSet(1), emptyRBSet());

    expectIsSubset(true, rbSetOf(1, 2), rbSetOf(1, 2));
    expectIsSubset(true, singletonRBSet(1), rbSetOf(1, 2));
    expectIsSubset(true, singletonRBSet(2), rbSetOf(1, 2));
    expectIsSubset(true, emptyRBSet(), rbSetOf(1, 2));
    expectIsSubset(false, rbSetOf(1, 2, 3), rbSetOf(1, 2));
    expectIsSubset(false, rbSetOf(1, 3), rbSetOf(1, 2));
    expectIsSubset(false, singletonRBSet(3), rbSetOf(1, 2));
  }

  @Test
  public void testIntersection() {
    assertEquals(emptyRBSet(),      intersection(rbSetOf(1, 2), rbSetOf(3, 4)));
    assertEquals(singletonRBSet(2), intersection(rbSetOf(1, 2), rbSetOf(2, 3)));
    assertEquals(rbSetOf(1, 2),     intersection(rbSetOf(1, 2), rbSetOf(1, 2)));

    // can also take the intersection between an RBSet and a Set:
    assertEquals(emptyRBSet(),      intersection(rbSetOf(1, 2), ImmutableSet.of(3, 4)));
    assertEquals(singletonRBSet(2), intersection(rbSetOf(1, 2), ImmutableSet.of(2, 3)));
    assertEquals(rbSetOf(1, 2),     intersection(rbSetOf(1, 2), ImmutableSet.of(1, 2)));

    // or between a Set and an RBSet:
    assertEquals(emptyRBSet(),      intersection(ImmutableSet.of(3, 4), rbSetOf(1, 2)));
    assertEquals(singletonRBSet(2), intersection(ImmutableSet.of(2, 3), rbSetOf(1, 2)));
    assertEquals(rbSetOf(1, 2),     intersection(ImmutableSet.of(1, 2), rbSetOf(1, 2)));
  }

  @Test
  public void testUnion() {
    Set<String> empty = emptySet();
    Set<String> a = singleton("A");
    Set<String> b = singleton("B");
    Set<String> c = singleton("C");
    Set<String> ab = ImmutableSet.of("A", "B");
    Set<String> bc = ImmutableSet.of("B", "C");
    Set<String> ac = ImmutableSet.of("A", "C");
    Set<String> abc = ImmutableSet.of("A", "B", "C");

    rbSetOf(empty, a, b, c, ab, bc, ac, abc)
        .forEach(iidSet -> {
          // empty U x = x
          assertUnion(empty, iidSet, iidSet);
          // x U x = x
          assertUnion(iidSet, iidSet, iidSet);
        });
    assertUnion(a, b, ab);
    assertUnion(a, c, ac);
    assertUnion(b, c, bc);
    assertUnion(ab, a, ab);
    assertUnion(ab, b, ab);
    assertUnion(ab, c, abc);
    assertUnion(ac, a, ac);
    assertUnion(ac, b, abc);
    assertUnion(ac, c, ac);
    assertUnion(bc, a, abc);
    assertUnion(bc, b, bc);
    assertUnion(bc, c, bc);
  }

  @Test
  public void setUnionInEmptyCases() {
    Set<String> empty = emptySet();
    Set<String> a = singleton("A");
    assertUnion(empty, empty, empty);
    assertUnion(empty, a, a);
    assertUnion(a, empty, a);
  }

  private void assertUnion(Set<String> set1, Set<String> set2, Set<String> expectedUnionAsSet) {
    RBSet<String> expectedUnion = newRBSet(expectedUnionAsSet);
    assertThat(RBSets.union(newRBSet(set1), set2),           rbSetEqualsMatcher(expectedUnion));
    assertThat(RBSets.union(set1,           newRBSet(set2)), rbSetEqualsMatcher(expectedUnion));
    assertThat(RBSets.union(newRBSet(set1), newRBSet(set2)), rbSetEqualsMatcher(expectedUnion));

    assertThat(RBSets.union(newRBSet(set2), set1),           rbSetEqualsMatcher(expectedUnion));
    assertThat(RBSets.union(set2,           newRBSet(set1)), rbSetEqualsMatcher(expectedUnion));
    assertThat(RBSets.union(newRBSet(set2), newRBSet(set1)), rbSetEqualsMatcher(expectedUnion));
  }

  @Test
  public void testUnionOfMultiple() {
    RBSet<Character> empty = emptyRBSet();
    RBSet<Character> a = singletonRBSet('a');
    for (List<RBSet<Character>> listOfSets : rbSetOf(
        Collections.<RBSet<Character>>emptyList(),
        singletonList(empty),
        ImmutableList.of(empty, empty),
        ImmutableList.of(empty, empty, empty))) {
      assertEquals(empty, RBSets.union(listOfSets.iterator()));
      assertEquals(empty, RBSets.unionOfPlainSets(Iterators.transform(listOfSets.iterator(), v -> v.asSet())));
    }

    for (List<RBSet<Character>> listOfSets : rbSetOf(
        ImmutableList.of(empty, a),
        ImmutableList.of(a, empty),
        ImmutableList.of(a, a),
        ImmutableList.of(a, a, a),
        ImmutableList.of(empty, a, a),
        ImmutableList.of(a, a, empty))) {
      assertEquals(a, RBSets.union(listOfSets.iterator()));
      assertEquals(a, RBSets.unionOfPlainSets(Iterators.transform(listOfSets.iterator(), v -> v.asSet())));
    }

    for (List<RBSet<Character>> listOfSets : rbSetOf(
        ImmutableList.of(
            singletonRBSet('a'),
            singletonRBSet('b'),
            singletonRBSet('c'),
            singletonRBSet('d')),
        ImmutableList.of(
            rbSetOf('a', 'b'),
            rbSetOf('b', 'c'),
            rbSetOf('c', 'd'),
            rbSetOf('d', 'a')),
        ImmutableList.of(
            rbSetOf('a', 'b', 'c', 'd'),
            rbSetOf('a', 'b', 'c', 'd'),
            rbSetOf('a', 'b', 'c', 'd'),
            rbSetOf('a', 'b', 'c', 'd')))) {
      assertEquals(
          rbSetOf('a', 'b', 'c', 'd'),
          RBSets.union(listOfSets.iterator()));
    }
  }

  @Test
  public void testUnionWithItem() {
     assertThat(union(emptyRBSet(),        'a'), rbSetEqualsMatcher(singletonRBSet( 'a')));
     assertThat(union(singletonRBSet('a'), 'a'), rbSetEqualsMatcher(singletonRBSet( 'a')));
     assertThat(union(singletonRBSet('a'), 'b'), rbSetEqualsMatcher(rbSetOf('a', 'b')));
     assertThat(union(rbSetOf('a', 'b'),   'c'), rbSetEqualsMatcher(rbSetOf('a', 'b', 'c')));
  }

  // Handy because it tries out all 4 overloads
  private void expectIsSubset(boolean expectedResult, RBSet<Integer> set1, RBSet<Integer> set2) {
    assertEquals(expectedResult, isSubsetOf(set1, set2));
    assertEquals(expectedResult, isSubsetOf(set1, set2.asSet()));
    assertEquals(expectedResult, isSubsetOf(set1.asSet(), set2));
    assertEquals(expectedResult, isSubsetOf(set1.asSet(), set2.asSet()));
  }

  @Test
  public void testToRBSetIfUnique() {
    assertOptionalEquals(emptyRBSet(), toRBSetIfUnique(emptyList()));
    assertOptionalEquals(singletonRBSet("a"), toRBSetIfUnique(singletonList("a")));
    assertOptionalEquals(rbSetOf("a", "b"), toRBSetIfUnique(ImmutableList.of("a", "b")));
    assertOptionalEmpty(toRBSetIfUnique(ImmutableList.of("a", "a")));
    assertOptionalEmpty(toRBSetIfUnique(ImmutableList.of("a", "b", "a")));
  }

  @Test
  public void testTwoRBSetsVisitor() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    RBSets.visitItemsOfTwoSets(
        rbSetOf(10, 11, 12, 13),
        rbSetOf(12, 13, 14, 15),
        new TwoRBSetsVisitor<Integer>() {
          @Override
          public void visitItemInLeftSetOnly(Integer itemInLeftSetOnly) {
            mutableSet.add(Strings.format("L%s", itemInLeftSetOnly));
          }

          @Override
          public void visitItemInRightSetOnly(Integer itemInRightSetOnly) {
            mutableSet.add(Strings.format("R%s", itemInRightSetOnly));
          }

          @Override
          public void visitItemInBothSets(Integer itemInBothSets) {
            mutableSet.add(Strings.format("B%s", itemInBothSets));
          }
        });
    assertEquals(
        newRBSet(mutableSet),
        rbSetOf("L10", "L11", "B12", "B13", "R14", "R15"));
  }

  @Test
  public void testEqualAsSets() {
    List<String> abcde = ImmutableList.of("a", "b", "c", "d", "e");
    assertTrue(equalAsSets(abcde, rbSetOf("a", "b", "c", "d", "e")));
    assertFalse(equalAsSets(abcde, rbSetOf("a", "b", "c", "d")));
    assertFalse(equalAsSets(abcde, rbSetOf("a", "b", "c", "d", "e", "f")));

    assertTrue(equalAsSets(abcde, ImmutableSet.of("a", "b", "c", "d", "e")));
    assertFalse(equalAsSets(abcde, ImmutableSet.of("a", "b", "c", "d")));
    assertFalse(equalAsSets(abcde, ImmutableSet.of("a", "b", "c", "d", "e", "f")));

    assertTrue(equalAsSets(emptyList(), emptyRBSet()));
    assertTrue(equalAsSets(emptyList(), emptySet()));
  }

  @Test
  public void testVarargSetUnion() {
    assertEquals(rbSetOf("A", "B"), setUnionOfFirstAndRest("A", "B"));
    assertEquals(rbSetOf("A", "B", "C"), setUnionOfFirstAndRest("A", "B", "C"));
    assertEquals(rbSetOf("A", "B", "C"), setUnionOfFirstAndRest("A", "B", "C", "C", "B", "A"));
    assertEquals(rbSetOf("A", "B", "C"), setUnionOfFirstSecondAndRest("A", "B", "C"));
    assertEquals(rbSetOf("A", "B", "C", "D"), setUnionOfFirstSecondAndRest("A", "B", "C", "D"));
  }

  @Test
  public void testRBSetDifference() {
    RBSet<InstrumentId> abcde = rbSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D, STOCK_E);
    RBSet<InstrumentId> abc   = rbSetOf(STOCK_A, STOCK_B, STOCK_C);
    RBSet<InstrumentId> de    = rbSetOf(STOCK_D, STOCK_E);

    assertEquals(abc, difference(abcde, de));
    assertEquals(de,  difference(abcde, abc));

    assertEquals(abcde, difference(abcde, emptyRBSet()));

    assertEquals(emptyRBSet(), difference(emptyRBSet(), abcde));
    assertEquals(emptyRBSet(), difference(abcde, abcde));
    assertEquals(emptyRBSet(), difference(emptyRBSet(), emptyRBSet()));

    assertEquals(abc, difference(abc, de));
    assertEquals(de,  difference(de, abc));
  }

  @Test
  public void testRBSetDifferenceOverloads() {
    RBSet<InstrumentId> abcde = rbSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D, STOCK_E);
    RBSet<InstrumentId> abc   = rbSetOf(STOCK_A, STOCK_B, STOCK_C);
    RBSet<InstrumentId> de    = rbSetOf(STOCK_D, STOCK_E);

    Set<InstrumentId> abcdeSet = ImmutableSet.of(STOCK_A, STOCK_B, STOCK_C, STOCK_D, STOCK_E);
    Set<InstrumentId> abcSet   = ImmutableSet.of(STOCK_A, STOCK_B, STOCK_C);
    Set<InstrumentId> deSet    = ImmutableSet.of(STOCK_D, STOCK_E);

    assertEquals(abc, difference(abcde,    deSet));
    assertEquals(abc, difference(abcdeSet, de));
    assertEquals(de,  difference(abcde,    abcSet));
    assertEquals(de,  difference(abcdeSet, abc));

    assertEquals(abcde, difference(abcde,    emptySet()));
    assertEquals(abcde, difference(abcdeSet, emptyRBSet()));

    assertEquals(emptyRBSet(), difference(emptyRBSet(), abcdeSet));
    assertEquals(emptyRBSet(), difference(emptySet(),   abcde));
    assertEquals(emptyRBSet(), difference(abcde,        abcdeSet));
    assertEquals(emptyRBSet(), difference(abcdeSet,     abcde));
    assertEquals(emptyRBSet(), difference(emptyRBSet(), emptySet()));
    assertEquals(emptyRBSet(), difference(emptySet(),   emptyRBSet()));

    assertEquals(abc, difference(abc,    deSet));
    assertEquals(abc, difference(abcSet, de));
    assertEquals(de,  difference(de,     abcSet));
    assertEquals(de,  difference(deSet,  abc));
  }

  @Test
  public void testContainsAll() {
    assertTrue(rbSetOf(1, 2, 3).containsAll(ImmutableSet.of(1)));
    assertTrue(rbSetOf(1, 2, 3).containsAll(ImmutableSet.of(1, 2)));
    assertTrue(rbSetOf(1, 2, 3).containsAll(ImmutableSet.of(1, 2, 3)));

    assertFalse(rbSetOf(1, 2, 3).containsAll(ImmutableSet.of(4)));
    assertFalse(rbSetOf(1, 2, 3).containsAll(ImmutableSet.of(1, 4)));
    assertFalse(rbSetOf(1, 2, 3).containsAll(ImmutableSet.of(1, 2, 3, 4)));
  }

}
