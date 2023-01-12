package com.rb.nonbiz.collections;

import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import java.util.function.BiPredicate;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.nonbiz.collections.HasInstrumentIdSetTest.hasInstrumentIdSetMatcher;
import static com.rb.nonbiz.collections.HasInstrumentIdSets.emptyHasInstrumentIdSet;
import static com.rb.nonbiz.collections.HasInstrumentIdSets.hasInstrumentIdSetOf;
import static com.rb.nonbiz.collections.HasInstrumentIdSets.mergeHasInstrumentIdSetsAllowingOverlapOnSimilarItemsOnly;
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

  private final TestHasInstrumentId STOCK_A_ID1 = testHasInstrumentId(STOCK_A, 1.0);
  private final TestHasInstrumentId STOCK_A_ID2 = testHasInstrumentId(STOCK_A, 2.0);
  private final TestHasInstrumentId STOCK_B_ID2 = testHasInstrumentId(STOCK_B, 2.0);
  private final TestHasInstrumentId STOCK_C_ID3 = testHasInstrumentId(STOCK_C, 3.0);

  @Test
  public void testNewHasInstrumentIdSet() {
    // Test empty set
    assertEquals(0, newHasInstrumentIdSet(emptyIidMap()).size());

    // Test set with 2 item
    assertThat(newHasInstrumentIdSet(
            iidMapOf(
                STOCK_A, STOCK_A_ID1,
                STOCK_B, STOCK_B_ID2)),
        hasInstrumentIdSetMatcher(hasInstrumentIdSetOf(
                STOCK_A_ID1,
                STOCK_B_ID2),
            f -> testHasInstrumentIdMatcher(f)));
  }

  @Test
  public void testEmptyHasInstrumentIdSet() {
    assertEquals(0, emptyHasInstrumentIdSet().size());
    assertIllegalArgumentException( () -> emptyHasInstrumentIdSet().getOrThrow(STOCK_A));
  }

  @Test
  public void testMergeHasInstrumentIdSetsAllowingOverlapOnSimilarItemsOnly() {
    HasInstrumentIdSet<TestHasInstrumentId> setA1 = singletonHasInstrumentIdSet(STOCK_A_ID1);
    HasInstrumentIdSet<TestHasInstrumentId> setA2 = singletonHasInstrumentIdSet(STOCK_A_ID2);
    HasInstrumentIdSet<TestHasInstrumentId> setB2 = singletonHasInstrumentIdSet(STOCK_B_ID2);
    HasInstrumentIdSet<TestHasInstrumentId> setC3 = singletonHasInstrumentIdSet(STOCK_C_ID3);
    HasInstrumentIdSet<TestHasInstrumentId> setA1B2 = hasInstrumentIdSetOf(STOCK_A_ID1, STOCK_B_ID2);
    HasInstrumentIdSet<TestHasInstrumentId> setA1C3 = hasInstrumentIdSetOf(STOCK_A_ID1, STOCK_C_ID3);
    HasInstrumentIdSet<TestHasInstrumentId> setB2C3 = hasInstrumentIdSetOf(STOCK_B_ID2, STOCK_C_ID3);
    HasInstrumentIdSet<TestHasInstrumentId> setA1B2C3 = hasInstrumentIdSetOf(STOCK_A_ID1, STOCK_B_ID2, STOCK_C_ID3);
    BiPredicate<TestHasInstrumentId, TestHasInstrumentId> checker = (a, b) -> a.getNumericValue() == b.getNumericValue();
    TriConsumer<HasInstrumentIdSet<TestHasInstrumentId>, HasInstrumentIdSet<TestHasInstrumentId>, HasInstrumentIdSet<TestHasInstrumentId>> asserter =
        (set1, set2, combinedSet) ->
            assertThat(
                mergeHasInstrumentIdSetsAllowingOverlapOnSimilarItemsOnly(checker, set1, set2),
                hasInstrumentIdSetMatcher(combinedSet, f -> testHasInstrumentIdMatcher(f)));

    // Merging set with itself gives itself.
    asserter.accept(setA1, setA1, setA1);
    asserter.accept(setC3, setC3, setC3);
    asserter.accept(setA1B2, setA1B2, setA1B2);
    asserter.accept(setA1B2C3, setA1B2C3, setA1B2C3);
    // Merging set with empty gives itself.
    asserter.accept(setA1, emptyHasInstrumentIdSet(), setA1);
    asserter.accept(setA1B2, emptyHasInstrumentIdSet(), setA1B2);
    asserter.accept(emptyHasInstrumentIdSet(), setA1B2, setA1B2);

    // Clean merges...no overlap.
    asserter.accept(setA1, setB2, setA1B2);
    asserter.accept(setA1, setC3, setA1C3);
    asserter.accept(setA1B2, setC3, setA1B2C3);
    asserter.accept(setC3, setA1B2, setA1B2C3);
    // Merges with overlap, but it's OK due to equality.
    asserter.accept(setA1B2, setB2C3, setA1B2C3);
    asserter.accept(setA1B2, setB2C3, setA1B2C3);

    // Merges fail due to lack of equality.  3rd argument to the asserter doesn't matter, since it raises an exception.
    assertIllegalArgumentException( () -> asserter.accept(setA1, setA2, setA2));
    assertIllegalArgumentException( () -> asserter.accept(setA1B2, setA2, setA2));
    assertIllegalArgumentException( () -> asserter.accept(setA2, setA1B2C3, setA2));

    // Run a couple tests with 3 items.
    assertThat(
        mergeHasInstrumentIdSetsAllowingOverlapOnSimilarItemsOnly(checker, setA1, setB2, setC3),
        hasInstrumentIdSetMatcher(setA1B2C3, f -> testHasInstrumentIdMatcher(f)));
    assertIllegalArgumentException( () ->
        mergeHasInstrumentIdSetsAllowingOverlapOnSimilarItemsOnly(checker, setA1, setB2, setC3, setA2));
    assertIllegalArgumentException( () ->
        mergeHasInstrumentIdSetsAllowingOverlapOnSimilarItemsOnly(checker, setA1B2C3, setB2, setC3, setA2, setA1));
  }

  @Test
  public void testSingletonHasInstrumentIdSet() {
    HasInstrumentIdSet<TestHasInstrumentId> singletonSet = singletonHasInstrumentIdSet(testHasInstrumentId(STOCK_A, 1.0));
    assertEquals(1, singletonSet.size());
    assertThat(
        singletonSet.getOrThrow(STOCK_A),
        testHasInstrumentIdMatcher(STOCK_A_ID1));
  }

  @Test
  public void testHasInstrumentIdSetOf() {
    // Constructor with 2 items.
    HasInstrumentIdSet<TestHasInstrumentId> setAB = hasInstrumentIdSetOf(STOCK_A_ID1, STOCK_B_ID2);
    assertEquals(2, setAB.size());
    assertThat(setAB.getOrThrow(STOCK_A), testHasInstrumentIdMatcher(STOCK_A_ID1));
    assertThat(setAB.getOrThrow(STOCK_B), testHasInstrumentIdMatcher(STOCK_B_ID2));
    assertIllegalArgumentException( () -> setAB.getOrThrow(STOCK_C));

    // Constructor with 3 items.
    HasInstrumentIdSet<TestHasInstrumentId> setABC = hasInstrumentIdSetOf(STOCK_A_ID1, STOCK_B_ID2, STOCK_C_ID3);
    assertEquals(3, setABC.size());
    assertThat(setABC.getOrThrow(STOCK_A), testHasInstrumentIdMatcher(STOCK_A_ID1));
    assertThat(setABC.getOrThrow(STOCK_B), testHasInstrumentIdMatcher(STOCK_B_ID2));
    assertThat(setABC.getOrThrow(STOCK_C), testHasInstrumentIdMatcher(STOCK_C_ID3));
  }

  @Test
  public void testMergeHasInstrumentIdSetsDisallowingOverlap() {
    HasInstrumentIdSet<TestHasInstrumentId> setA = singletonHasInstrumentIdSet(STOCK_A_ID1);
    HasInstrumentIdSet<TestHasInstrumentId> setB = singletonHasInstrumentIdSet(STOCK_B_ID2);
    HasInstrumentIdSet<TestHasInstrumentId> setC = singletonHasInstrumentIdSet(STOCK_C_ID3);
    HasInstrumentIdSet<TestHasInstrumentId> setAB = hasInstrumentIdSetOf(STOCK_A_ID1, STOCK_B_ID2);
    HasInstrumentIdSet<TestHasInstrumentId> setAC = hasInstrumentIdSetOf(STOCK_A_ID1, STOCK_C_ID3);
    HasInstrumentIdSet<TestHasInstrumentId> setBC = hasInstrumentIdSetOf(STOCK_B_ID2, STOCK_C_ID3);
    HasInstrumentIdSet<TestHasInstrumentId> setABC = hasInstrumentIdSetOf(STOCK_A_ID1, STOCK_B_ID2, STOCK_C_ID3);

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