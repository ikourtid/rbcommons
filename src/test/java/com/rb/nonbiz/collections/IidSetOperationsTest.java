package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.functional.TriConsumer;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_E;
import static com.rb.nonbiz.collections.IidSetOperations.differenceOfIidSets;
import static com.rb.nonbiz.collections.IidSetOperations.intersectionOfIidSets;
import static com.rb.nonbiz.collections.IidSetOperations.isSubsetOf;
import static com.rb.nonbiz.collections.IidSetOperations.setUnionOfFirstAndRestInstrumentIds;
import static com.rb.nonbiz.collections.IidSetOperations.setUnionOfFirstSecondAndRestInstrumentIds;
import static com.rb.nonbiz.collections.IidSetOperations.unionOfIidSets;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIidSetEquals;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class IidSetOperationsTest {

  @Test
  public void testUnion() {
    Set<InstrumentId> empty = emptySet();
    Set<InstrumentId> a = singleton(STOCK_A);
    Set<InstrumentId> b = singleton(STOCK_B);
    Set<InstrumentId> c = singleton(STOCK_C);
    Set<InstrumentId> ab = ImmutableSet.of(STOCK_A, STOCK_B);
    Set<InstrumentId> bc = ImmutableSet.of(STOCK_B, STOCK_C);
    Set<InstrumentId> ac = ImmutableSet.of(STOCK_A, STOCK_C);
    Set<InstrumentId> abc = ImmutableSet.of(STOCK_A, STOCK_B, STOCK_C);

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

  private void assertUnion(Set<InstrumentId> set1, Set<InstrumentId> set2, Set<InstrumentId> expectedUnionAsSet) {
    IidSet expectedUnion = newIidSet(expectedUnionAsSet);
    assertIidSetEquals(expectedUnion, unionOfIidSets(newIidSet(set1), set2));
    assertIidSetEquals(expectedUnion, unionOfIidSets(set1,            newIidSet(set2)));
    assertIidSetEquals(expectedUnion, unionOfIidSets(newIidSet(set1), newIidSet(set2)));

    assertIidSetEquals(expectedUnion, unionOfIidSets(newIidSet(set2), set1));
    assertIidSetEquals(expectedUnion, unionOfIidSets(set2,            newIidSet(set1)));
    assertIidSetEquals(expectedUnion, unionOfIidSets(newIidSet(set2), newIidSet(set1)));
  }

  @Test
  public void testUnionOfMultiple() {
    IidSet empty = emptyIidSet();
    IidSet a = singletonIidSet(STOCK_A);
    for (List<IidSet> listOfSets : rbSetOf(
        Collections.<IidSet>emptyList(),
        singletonList(empty),
        ImmutableList.of(empty, empty),
        ImmutableList.of(empty, empty, empty))) {
      assertIidSetEquals(empty, unionOfIidSets(listOfSets));
    }

    for (List<IidSet> listOfSets : rbSetOf(
        ImmutableList.of(empty, a),
        ImmutableList.of(a, empty),
        ImmutableList.of(a, a),
        ImmutableList.of(a, a, a),
        ImmutableList.of(empty, a, a),
        ImmutableList.of(a, a, empty))) {
      assertIidSetEquals(a, unionOfIidSets(listOfSets));
    }

    for (List<IidSet> listOfSets : rbSetOf(
        ImmutableList.of(
            singletonIidSet(STOCK_A),
            singletonIidSet(STOCK_B),
            singletonIidSet(STOCK_C),
            singletonIidSet(STOCK_D)),
        ImmutableList.of(
            iidSetOf(STOCK_A, STOCK_B),
            iidSetOf(STOCK_B, STOCK_C),
            iidSetOf(STOCK_C, STOCK_D),
            iidSetOf(STOCK_D, STOCK_A)),
        ImmutableList.of(
            iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D),
            iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D),
            iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D),
            iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D)))) {
      assertIidSetEquals(
          iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D),
          unionOfIidSets(listOfSets));
    }
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D),
        unionOfIidSets(
            iidSetOf(STOCK_A, STOCK_B),
            iidSetOf(STOCK_B, STOCK_C),
            iidSetOf(STOCK_C, STOCK_D)));
  }

  @Test
  public void testSetUnionOfFirstAndRestInstrumentIds() {
    assertIidSetEquals(
        singletonIidSet(STOCK_A),
        setUnionOfFirstAndRestInstrumentIds(STOCK_A));
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B),
        setUnionOfFirstAndRestInstrumentIds(STOCK_A, STOCK_B));
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B, STOCK_C),
        setUnionOfFirstAndRestInstrumentIds(STOCK_A, STOCK_B, STOCK_C));
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B, STOCK_C),
        setUnionOfFirstAndRestInstrumentIds(STOCK_A, STOCK_B, STOCK_C, STOCK_C, STOCK_B, STOCK_A));
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B, STOCK_C),
        setUnionOfFirstAndRestInstrumentIds(STOCK_A, STOCK_B, STOCK_C));
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D),
        setUnionOfFirstAndRestInstrumentIds(STOCK_A, STOCK_B, STOCK_C, STOCK_D));
  }

  @Test
  public void testSetUnionOfFirstSecondAndRestInstrumentIds() {
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B),
        setUnionOfFirstSecondAndRestInstrumentIds(STOCK_A, STOCK_B));
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B, STOCK_C),
        setUnionOfFirstSecondAndRestInstrumentIds(STOCK_A, STOCK_B, STOCK_C));
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B, STOCK_C),
        setUnionOfFirstSecondAndRestInstrumentIds(STOCK_A, STOCK_B, STOCK_C, STOCK_C, STOCK_B, STOCK_A));
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B, STOCK_C),
        setUnionOfFirstSecondAndRestInstrumentIds(STOCK_A, STOCK_B, STOCK_C));
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D),
        setUnionOfFirstSecondAndRestInstrumentIds(STOCK_A, STOCK_B, STOCK_C, STOCK_D));
  }

  @Test
  public void testSetIntersection_all3overloads() {
    assertIidSetEquals(emptyIidSet(), intersectionOfIidSets(emptyIidSet(), emptySet()));
    assertIidSetEquals(emptyIidSet(), intersectionOfIidSets(emptySet(), emptyIidSet()));
    assertIidSetEquals(emptyIidSet(), intersectionOfIidSets(emptyIidSet(), emptyIidSet()));

    assertIidSetEquals(
        iidSetOf(STOCK_B, STOCK_C),
        intersectionOfIidSets(
            iidSetOf(STOCK_A, STOCK_B, STOCK_C),
            iidSetOf(STOCK_B, STOCK_C, STOCK_D)));

    assertIidSetEquals(
        iidSetOf(STOCK_B, STOCK_C),
        intersectionOfIidSets(
            iidSetOf(STOCK_A, STOCK_B, STOCK_C),
            ImmutableSet.of(STOCK_B, STOCK_C, STOCK_D)));

    assertIidSetEquals(
        iidSetOf(STOCK_B, STOCK_C),
        intersectionOfIidSets(
            ImmutableSet.of(STOCK_A, STOCK_B, STOCK_C),
            iidSetOf(STOCK_B, STOCK_C, STOCK_D)));
  }

  @Test
  public void testIidSetDifference() {
    IidSet abcde = iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D, STOCK_E);
    IidSet abc   = iidSetOf(STOCK_A, STOCK_B, STOCK_C);
    IidSet de    = iidSetOf(STOCK_D, STOCK_E);

    assertIidSetEquals(abc, differenceOfIidSets(abcde, de));
    assertIidSetEquals(de,  differenceOfIidSets(abcde, abc));

    assertIidSetEquals(abcde, differenceOfIidSets(abcde, emptyIidSet()));

    assertIidSetEquals(emptyIidSet(), differenceOfIidSets(emptyIidSet(), abcde));
    assertIidSetEquals(emptyIidSet(), differenceOfIidSets(abcde, abcde));
    assertIidSetEquals(emptyIidSet(), differenceOfIidSets(emptyIidSet(), emptyIidSet()));

    assertIidSetEquals(abc, differenceOfIidSets(abc, de));
    assertIidSetEquals(de,  differenceOfIidSets(de, abc));
  }

  @Test
  public void testIidSetDifferenceOverloads() {
    IidSet abcde = iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D, STOCK_E);
    IidSet abc   = iidSetOf(STOCK_A, STOCK_B, STOCK_C);
    IidSet de    = iidSetOf(STOCK_D, STOCK_E);

    Set<InstrumentId> abcdeSet = ImmutableSet.of(STOCK_A, STOCK_B, STOCK_C, STOCK_D, STOCK_E);
    Set<InstrumentId> abcSet   = ImmutableSet.of(STOCK_A, STOCK_B, STOCK_C);
    Set<InstrumentId> deSet    = ImmutableSet.of(STOCK_D, STOCK_E);

    assertIidSetEquals(abc, differenceOfIidSets(abcde,    deSet));
    assertIidSetEquals(abc, differenceOfIidSets(abcdeSet, de));
    assertIidSetEquals(de,  differenceOfIidSets(abcde,    abcSet));
    assertIidSetEquals(de,  differenceOfIidSets(abcdeSet, abc));

    assertIidSetEquals(abcde, differenceOfIidSets(abcde,    emptySet()));
    assertIidSetEquals(abcde, differenceOfIidSets(abcdeSet, emptyIidSet()));

    assertIidSetEquals(emptyIidSet(), differenceOfIidSets(emptyIidSet(), abcdeSet));
    assertIidSetEquals(emptyIidSet(), differenceOfIidSets(emptySet(),    abcde));
    assertIidSetEquals(emptyIidSet(), differenceOfIidSets(abcde,         abcdeSet));
    assertIidSetEquals(emptyIidSet(), differenceOfIidSets(abcdeSet,      abcde));
    assertIidSetEquals(emptyIidSet(), differenceOfIidSets(emptyIidSet(), emptySet()));
    assertIidSetEquals(emptyIidSet(), differenceOfIidSets(emptySet(),    emptyIidSet()));

    assertIidSetEquals(abc, differenceOfIidSets(abc,    deSet));
    assertIidSetEquals(abc, differenceOfIidSets(abcSet, de));
    assertIidSetEquals(de,  differenceOfIidSets(de,     abcSet));
    assertIidSetEquals(de,  differenceOfIidSets(deSet,  abc));
  }


  @Test
  public void testIsSubset_all3overloads() {
    TriConsumer<Boolean, Set<InstrumentId>, Set<InstrumentId>> asserter = (expectedResult, subset, superset) -> {
      // this exercise all 3 overloads
      assertEquals(expectedResult, isSubsetOf(newIidSet(subset), newIidSet(superset)));
      assertEquals(expectedResult, isSubsetOf(subset, newIidSet(superset)));
      assertEquals(expectedResult, isSubsetOf(newIidSet(subset), superset));
    };
    asserter.accept(true,  emptySet(),                                 emptySet());
    asserter.accept(true,  emptySet(),                                 singleton(STOCK_A));
    asserter.accept(false, singleton(STOCK_A),                         emptySet());
    asserter.accept(true,  ImmutableSet.of(STOCK_A, STOCK_B),          ImmutableSet.of(STOCK_A, STOCK_B));
    asserter.accept(true,  singleton(STOCK_A),                         ImmutableSet.of(STOCK_A, STOCK_B));
    asserter.accept(true,  singleton(STOCK_B),                         ImmutableSet.of(STOCK_A, STOCK_B));
    asserter.accept(true,  emptySet(),                                 ImmutableSet.of(STOCK_A, STOCK_B));
    asserter.accept(false, ImmutableSet.of(STOCK_A, STOCK_B, STOCK_C), ImmutableSet.of(STOCK_A, STOCK_B));
    asserter.accept(false, ImmutableSet.of(STOCK_A, STOCK_C),          ImmutableSet.of(STOCK_A, STOCK_B));
    asserter.accept(false, singleton(STOCK_C),                         ImmutableSet.of(STOCK_A, STOCK_B));
  }

}
