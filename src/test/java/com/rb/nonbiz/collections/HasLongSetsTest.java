package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.HasLongSets.TwoHasLongSetsVisitor;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_E;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_F;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.HasLongSets.equalAsHasLongSets;
import static com.rb.nonbiz.collections.HasLongSets.hasLongSetIsSubsetOf;
import static com.rb.nonbiz.collections.HasLongSets.hasLongSetIsSubsetOfHasLongSet;
import static com.rb.nonbiz.collections.HasLongSets.isSubsetOfHasLongSet;
import static com.rb.nonbiz.collections.HasLongSets.noSharedHasLongs;
import static com.rb.nonbiz.collections.HasLongSets.noSharedHasLongsInCollection;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HasLongSetsTest {

  @Test
  public void testNoSharedHasLongs() {
    // 3-arg version
    assertTrue( noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_E, STOCK_F)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_A), iidSetOf(STOCK_E, STOCK_F)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_A, STOCK_D), iidSetOf(STOCK_E, STOCK_F)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_B, STOCK_D), iidSetOf(STOCK_E, STOCK_F)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_C, STOCK_F)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_E, STOCK_C)));
    assertTrue( noSharedHasLongs(emptyIidSet(), emptyIidSet(), emptyIidSet()));

    // noSharedHasLongsInCollection version
    assertTrue( noSharedHasLongsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_E, STOCK_F))));
    assertFalse(noSharedHasLongsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_A), iidSetOf(STOCK_E, STOCK_F))));
    assertFalse(noSharedHasLongsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_A, STOCK_D), iidSetOf(STOCK_E, STOCK_F))));
    assertFalse(noSharedHasLongsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_B, STOCK_D), iidSetOf(STOCK_E, STOCK_F))));
    assertFalse(noSharedHasLongsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_C, STOCK_F))));
    assertFalse(noSharedHasLongsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_E, STOCK_C))));
    assertTrue( noSharedHasLongsInCollection(ImmutableList.of(
        emptyIidSet(), emptyIidSet(), emptyIidSet())));

    // 2-arg version
    assertTrue( noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_A)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_A, STOCK_D)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_B, STOCK_D)));
    assertTrue( noSharedHasLongs(emptyIidSet(), emptyIidSet()));

    // raw set versions
    assertTrue( noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), newHashSet(STOCK_C, STOCK_D)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), newHashSet(STOCK_C, STOCK_A)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), newHashSet(STOCK_A, STOCK_D)));
    assertFalse(noSharedHasLongs(iidSetOf(STOCK_A, STOCK_B), newHashSet(STOCK_B, STOCK_D)));
    assertTrue( noSharedHasLongs(emptyIidSet(), newHashSet()));

    assertTrue( noSharedHasLongs(newHashSet(STOCK_C, STOCK_D), iidSetOf(STOCK_A, STOCK_B)));
    assertFalse(noSharedHasLongs(newHashSet(STOCK_C, STOCK_A), iidSetOf(STOCK_A, STOCK_B)));
    assertFalse(noSharedHasLongs(newHashSet(STOCK_A, STOCK_D), iidSetOf(STOCK_A, STOCK_B)));
    assertFalse(noSharedHasLongs(newHashSet(STOCK_B, STOCK_D), iidSetOf(STOCK_A, STOCK_B)));
    assertTrue( noSharedHasLongs(emptyIidSet(), newHashSet()));
  }

  @Test
  public void testIsSubset_all3overloads() {
    TriConsumer<Boolean, Set<InstrumentId>, Set<InstrumentId>> asserter = (expectedResult, subset, superset) -> {
      assertEquals(expectedResult, hasLongSetIsSubsetOfHasLongSet(newIidSet(subset), newIidSet(superset)));
      assertEquals(expectedResult, isSubsetOfHasLongSet(subset, newIidSet(superset)));
      assertEquals(expectedResult, hasLongSetIsSubsetOf(newIidSet(subset), superset));
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

  @Test
  public void testTwoRBSetsVisitor() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    HasLongSets.visitHasLongsOfTwoSets(
        iidSetOf(instrumentId(10), instrumentId(11), instrumentId(12), instrumentId(13)),
        iidSetOf(instrumentId(12), instrumentId(13), instrumentId(14), instrumentId(15)),
        new TwoHasLongSetsVisitor<InstrumentId>() {
          @Override
          public void visitHasLongInLeftSetOnly(InstrumentId hasLongInLeftSetOnly) {
            mutableSet.add(Strings.format("L_%s", hasLongInLeftSetOnly));
          }

          @Override
          public void visitHasLongInRightSetOnly(InstrumentId hasLongInRightSetOnly) {
            mutableSet.add(Strings.format("R_%s", hasLongInRightSetOnly));
          }

          @Override
          public void visitHasLongInBothSets(InstrumentId hasLongInBothSets) {
            mutableSet.add(Strings.format("B_%s", hasLongInBothSets));
          }
        });
    assertEquals(
        newRBSet(mutableSet),
        rbSetOf("L_iid 10", "L_iid 11", "B_iid 12", "B_iid 13", "R_iid 14", "R_iid 15"));
  }

  @Test
  public void testequalAsHasLongSets() {
    List<InstrumentId> abcde = ImmutableList.of(STOCK_A, STOCK_B, STOCK_C, STOCK_D, STOCK_E);
    assertTrue(equalAsHasLongSets(abcde, iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D, STOCK_E)));
    assertFalse(equalAsHasLongSets(abcde, iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D)));
    assertFalse(equalAsHasLongSets(abcde, iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D, STOCK_E, STOCK_F)));

    assertTrue(equalAsHasLongSets(emptyList(), emptyIidSet()));
  }

}
