package com.rb.nonbiz.collections;

import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.nonbiz.collections.HasInstrumentIdSetTest.hasInstrumentIdSetMatcher;
import static com.rb.nonbiz.collections.HasInstrumentIdSets.emptyHasInstrumentIdSet;
import static com.rb.nonbiz.collections.HasInstrumentIdSets.hasInstrumentIdSetOf;
import static com.rb.nonbiz.collections.HasInstrumentIdSets.mergeHasInstrumentIdSetsDisallowingOverlap;
import static com.rb.nonbiz.collections.HasInstrumentIdSets.newHasInstrumentIdSet;
import static com.rb.nonbiz.collections.HasInstrumentIdSets.singletonHasInstrumentIdSet;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentId;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentIdMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class HasInstrumentIdSetsTest
    extends RBCommonsIntegrationTest<HasInstrumentIdSets> {

  private final TestHasInstrumentId STOCK_A_ID = testHasInstrumentId(STOCK_A, 1.0);
  private final TestHasInstrumentId STOCK_B_ID = testHasInstrumentId(STOCK_B, 2.0);
  private final TestHasInstrumentId STOCK_C_ID = testHasInstrumentId(STOCK_C, 3.0);

  @Test
  public void testNewHasInstrumentIdSet() {
    // Test empty set
    assertEquals(0, newHasInstrumentIdSet(emptyIidMap()).size());

    // Test set with 2 item
    assertThat(newHasInstrumentIdSet(
            iidMapOf(
                STOCK_A, STOCK_A_ID,
                STOCK_B, STOCK_B_ID)),
        hasInstrumentIdSetMatcher(hasInstrumentIdSetOf(
                STOCK_A_ID,
                STOCK_B_ID),
            f -> testHasInstrumentIdMatcher(f)));
  }

  @Test
  public void testEmptyHasInstrumentIdSet() {
    assertEquals(0, emptyHasInstrumentIdSet().size());
    assertIllegalArgumentException( () -> emptyHasInstrumentIdSet().getOrThrow(STOCK_A));
  }

  @Test
  public void testSingletonHasInstrumentIdSet() {
    HasInstrumentIdSet<TestHasInstrumentId> singletonSet = singletonHasInstrumentIdSet(testHasInstrumentId(STOCK_A, 1.0));
    assertEquals(1, singletonSet.size());
    assertThat(
        singletonSet.getOrThrow(STOCK_A),
        testHasInstrumentIdMatcher(STOCK_A_ID));
  }

  @Test
  public void testHasInstrumentIdSetOf() {
    // Constructor with 2 items.
    HasInstrumentIdSet<TestHasInstrumentId> setAB = hasInstrumentIdSetOf(STOCK_A_ID, STOCK_B_ID);
    assertEquals(2, setAB.size());
    assertThat(setAB.getOrThrow(STOCK_A), testHasInstrumentIdMatcher(STOCK_A_ID));
    assertThat(setAB.getOrThrow(STOCK_B), testHasInstrumentIdMatcher(STOCK_B_ID));
    assertIllegalArgumentException( () -> setAB.getOrThrow(STOCK_C));

    // Constructor with 3 items.
    HasInstrumentIdSet<TestHasInstrumentId> setABC = hasInstrumentIdSetOf(STOCK_A_ID, STOCK_B_ID, STOCK_C_ID);
    assertEquals(3, setABC.size());
    assertThat(setABC.getOrThrow(STOCK_A), testHasInstrumentIdMatcher(STOCK_A_ID));
    assertThat(setABC.getOrThrow(STOCK_B), testHasInstrumentIdMatcher(STOCK_B_ID));
    assertThat(setABC.getOrThrow(STOCK_C), testHasInstrumentIdMatcher(STOCK_C_ID));
  }

  @Test
  public void testMergeHasInstrumentIdSetsDisallowingOverlap() {
    HasInstrumentIdSet<TestHasInstrumentId> setA = singletonHasInstrumentIdSet(STOCK_A_ID);
    HasInstrumentIdSet<TestHasInstrumentId> setB = singletonHasInstrumentIdSet(STOCK_B_ID);
    HasInstrumentIdSet<TestHasInstrumentId> setC = singletonHasInstrumentIdSet(STOCK_C_ID);
    HasInstrumentIdSet<TestHasInstrumentId> setAB = hasInstrumentIdSetOf(STOCK_A_ID, STOCK_B_ID);
    HasInstrumentIdSet<TestHasInstrumentId> setAC = hasInstrumentIdSetOf(STOCK_A_ID, STOCK_C_ID);
    HasInstrumentIdSet<TestHasInstrumentId> setBC = hasInstrumentIdSetOf(STOCK_B_ID, STOCK_C_ID);
    HasInstrumentIdSet<TestHasInstrumentId> setABC = hasInstrumentIdSetOf(STOCK_A_ID, STOCK_B_ID, STOCK_C_ID);

    TriConsumer<HasInstrumentIdSet<TestHasInstrumentId>, HasInstrumentIdSet<TestHasInstrumentId>, HasInstrumentIdSet<TestHasInstrumentId>> asserter =
        (set1, set2, combinedSet) ->
            assertThat(
                mergeHasInstrumentIdSetsDisallowingOverlap(set1, set2),
                hasInstrumentIdSetMatcher(combinedSet, f -> testHasInstrumentIdMatcher(f)));

    asserter.accept(setA, setB, setAB);
    asserter.accept(emptyHasInstrumentIdSet(), setA, setA);
    asserter.accept(emptyHasInstrumentIdSet(), setB, setB);
    asserter.accept(emptyHasInstrumentIdSet(), setABC, setABC);
    asserter.accept(setA, setC, setAC);
    asserter.accept(setA, setBC, setABC);
    asserter.accept(setAC, setB, setABC);

    // Check a few with 3 inputs
    assertThat(
        mergeHasInstrumentIdSetsDisallowingOverlap(setA, setB, setC),
        hasInstrumentIdSetMatcher(setABC, f -> testHasInstrumentIdMatcher(f)));
    assertThat(
        mergeHasInstrumentIdSetsDisallowingOverlap(emptyHasInstrumentIdSet(), setB, emptyHasInstrumentIdSet(), setC),
        hasInstrumentIdSetMatcher(setBC, f -> testHasInstrumentIdMatcher(f)));

    // Check overlaps.
    assertIllegalArgumentException( () -> mergeHasInstrumentIdSetsDisallowingOverlap(setA,  setA));
    assertIllegalArgumentException( () -> mergeHasInstrumentIdSetsDisallowingOverlap(setA,  setAB));
    assertIllegalArgumentException( () -> mergeHasInstrumentIdSetsDisallowingOverlap(setAC, setC));

    // Tests with 3 and 4 items.
    assertIllegalArgumentException( () -> mergeHasInstrumentIdSetsDisallowingOverlap(setA,  setB, setA));
    assertIllegalArgumentException( () -> mergeHasInstrumentIdSetsDisallowingOverlap(setA,  setB, setC, setA));
    assertIllegalArgumentException( () -> mergeHasInstrumentIdSetsDisallowingOverlap(setA,  emptyHasInstrumentIdSet(), setC, setA));
  }

  @Override
  protected Class<HasInstrumentIdSets> getClassBeingTested() {
    return HasInstrumentIdSets.class;
  }

}