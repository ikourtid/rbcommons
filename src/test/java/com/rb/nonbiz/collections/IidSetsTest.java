package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.rb.biz.marketdata.FakeInstruments.DUMMY_INSTRUMENT_ID;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_E;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_F;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSetInOrder;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.IidSetTest.iidSetMatcher;
import static com.rb.nonbiz.collections.IidSets.TwoIidSetsVisitor;
import static com.rb.nonbiz.collections.IidSets.noSharedIids;
import static com.rb.nonbiz.collections.IidSets.noSharedIidsInCollection;
import static com.rb.nonbiz.collections.IidSets.toIidSetIfUnique;
import static com.rb.nonbiz.collections.IidSets.visitInstrumentsOfTwoSets;
import static com.rb.nonbiz.collections.MutableIidSet.newMutableIidSetWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.nonEmptyOptionalMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIidSetEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IidSetsTest {

  @Test
  public void testNoSharedIids() {
    // 3-arg version
    assertTrue( noSharedIids(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_E, STOCK_F)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_A), iidSetOf(STOCK_E, STOCK_F)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_A, STOCK_D), iidSetOf(STOCK_E, STOCK_F)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_B, STOCK_D), iidSetOf(STOCK_E, STOCK_F)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_C, STOCK_F)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_E, STOCK_C)));
    assertTrue( noSharedIids(emptyIidSet(), emptyIidSet(), emptyIidSet()));

    // noSharedIidsInCollection version
    assertTrue( noSharedIidsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_E, STOCK_F))));
    assertFalse(noSharedIidsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_A), iidSetOf(STOCK_E, STOCK_F))));
    assertFalse(noSharedIidsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_A, STOCK_D), iidSetOf(STOCK_E, STOCK_F))));
    assertFalse(noSharedIidsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_B, STOCK_D), iidSetOf(STOCK_E, STOCK_F))));
    assertFalse(noSharedIidsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_C, STOCK_F))));
    assertFalse(noSharedIidsInCollection(ImmutableList.of(
        iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D), iidSetOf(STOCK_E, STOCK_C))));
    assertTrue( noSharedIidsInCollection(ImmutableList.of(
        emptyIidSet(), emptyIidSet(), emptyIidSet())));

    // 2-arg version
    assertTrue( noSharedIids(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_D)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_C, STOCK_A)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_A, STOCK_D)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), iidSetOf(STOCK_B, STOCK_D)));
    assertTrue( noSharedIids(emptyIidSet(), emptyIidSet()));

    // raw set versions
    assertTrue( noSharedIids(iidSetOf(STOCK_A, STOCK_B), newHashSet(STOCK_C, STOCK_D)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), newHashSet(STOCK_C, STOCK_A)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), newHashSet(STOCK_A, STOCK_D)));
    assertFalse(noSharedIids(iidSetOf(STOCK_A, STOCK_B), newHashSet(STOCK_B, STOCK_D)));
    assertTrue( noSharedIids(emptyIidSet(), newHashSet()));

    assertTrue( noSharedIids(newHashSet(STOCK_C, STOCK_D), iidSetOf(STOCK_A, STOCK_B)));
    assertFalse(noSharedIids(newHashSet(STOCK_C, STOCK_A), iidSetOf(STOCK_A, STOCK_B)));
    assertFalse(noSharedIids(newHashSet(STOCK_A, STOCK_D), iidSetOf(STOCK_A, STOCK_B)));
    assertFalse(noSharedIids(newHashSet(STOCK_B, STOCK_D), iidSetOf(STOCK_A, STOCK_B)));
    assertTrue( noSharedIids(emptyIidSet(), newHashSet()));
  }

  @Test
  public void testToIidSetIfUnique() {
    assertThat(
        toIidSetIfUnique(emptyList()),
        nonEmptyOptionalMatcher(
            iidSetMatcher(emptyIidSet())));
    assertThat(
        toIidSetIfUnique(singletonList(STOCK_A)),
        nonEmptyOptionalMatcher(
            iidSetMatcher(singletonIidSet(STOCK_A))));
    assertThat(
        toIidSetIfUnique(ImmutableList.of(STOCK_A, STOCK_B)),
        nonEmptyOptionalMatcher(
            iidSetMatcher(iidSetOf(STOCK_A, STOCK_B))));
    assertOptionalEmpty(toIidSetIfUnique(ImmutableList.of(STOCK_A, STOCK_A)));
    assertOptionalEmpty(toIidSetIfUnique(ImmutableList.of(STOCK_A, STOCK_B, STOCK_A)));
  }

  @Test
  public void sizeHintIsJustAHint_ifSmallThenThingsStillWork() {
    MutableIidSet mutableSet = newMutableIidSetWithExpectedSize(1);
    mutableSet.add(STOCK_A);
    mutableSet.add(STOCK_B);
    mutableSet.add(STOCK_C);
    assertIidSetEquals(
        iidSetOf(STOCK_A, STOCK_B, STOCK_C),
        newIidSet(mutableSet));
  }

  @Test
  public void testConstructorWithListInOrder() {
    List<InstrumentId> listInOrder = ImmutableList.of(
        instrumentId(1), instrumentId(2), instrumentId(3), instrumentId(4), instrumentId(5));
    IidSet iidSet = newIidSetInOrder(listInOrder);
    assertThat(
        iidSet.toSortedList(),
        orderedListMatcher(listInOrder, f -> typeSafeEqualTo(f)));
    // Checking the 2nd time as well, since the 1st time around we may compute & the 2nd return the cached value
    assertThat(
        iidSet.toSortedList(),
        orderedListMatcher(listInOrder, f -> typeSafeEqualTo(f)));
  }

  @Test
  public void constructorWithListInOrder_notInOrder_throws() {
    TriFunction<Integer, Integer, Integer, IidSet> maker = (i1, i2, i3) ->
        newIidSetInOrder(ImmutableList.of(instrumentId(i1), instrumentId(i2), instrumentId(i3)));

    IidSet doesNotThrow = maker.apply(1, 2, 3);
    assertIllegalArgumentException( () -> maker.apply(1, 1, 3));
    assertIllegalArgumentException( () -> maker.apply(1, 2, 2));
    assertIllegalArgumentException( () -> maker.apply(1, 3, 2));
    assertIllegalArgumentException( () -> maker.apply(2, 1, 3));

    doesNotThrow = singletonIidSet(DUMMY_INSTRUMENT_ID); // nothing to be out of order here
  }

  @Test
  public void testTwoIidSetsVisitor() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    visitInstrumentsOfTwoSets(
        iidSetOf(instrumentId(10), instrumentId(11), instrumentId(12), instrumentId(13)),
        iidSetOf(instrumentId(12), instrumentId(13), instrumentId(14), instrumentId(15)),
        new TwoIidSetsVisitor() {
          @Override
          public void visitInstrumentInLeftSetOnly(InstrumentId instrumentInLeftSetOnly) {
            mutableSet.add(Strings.format("L%s", instrumentInLeftSetOnly.asLong()));
          }

          @Override
          public void visitInstrumentInRightSetOnly(InstrumentId instrumentInRightSetOnly) {
            mutableSet.add(Strings.format("R%s", instrumentInRightSetOnly.asLong()));
          }

          @Override
          public void visitInstrumentInBothSets(InstrumentId instrumentInBothSets) {
            mutableSet.add(Strings.format("B%s", instrumentInBothSets.asLong()));
          }
        });
    assertEquals(
        newRBSet(mutableSet),
        rbSetOf("L10", "L11", "B12", "B13", "R14", "R15"));
  }

}
