package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.marketdata.FakeInstruments;
import com.rb.nonbiz.collections.IidCounter.IidCounterBuilder;
import com.rb.nonbiz.testmatchers.Match;
import com.rb.nonbiz.testmatchers.RBMatchers;
import com.rb.nonbiz.testmatchers.RBValueMatchers;
import com.rb.nonbiz.testutils.Asserters;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_E;
import static com.rb.nonbiz.collections.IidCounter.IidCounterBuilder.iidCounterBuilder;
import static com.rb.nonbiz.collections.IidCounter.iidCounterFromStream;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.emptyIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentId;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIidSetEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IidCounterTest extends RBTestMatcher<IidCounter> {

  public static IidCounter emptyIidCounter() {
    return iidCounterBuilder().build();
  }

  @Test
  public void happyPath() {
    IidCounter counter = iidCounterBuilder().build();
    assertTrue(counter.isEmpty());
    assertEquals(0, counter.getCountOrZero(STOCK_A));
    assertEquals(0, counter.getCountOrZero(STOCK_B));
    assertIidSetEquals(emptyIidSet(), counter.getItemsWithNonZeroCounts());
    assertEquals(0, counter.getSumOfCounts());

    counter = iidCounterBuilder()
        .increment(STOCK_A)
        .build();
    assertFalse(counter.isEmpty());
    assertEquals(1, counter.getCountOrZero(STOCK_A));
    assertEquals(0, counter.getCountOrZero(STOCK_B));
    assertIidSetEquals(singletonIidSet(STOCK_A), counter.getItemsWithNonZeroCounts());
    assertEquals(1, counter.getSumOfCounts());

    counter = iidCounterBuilder()
        .increment(STOCK_A)
        .increment(STOCK_B)
        .increment(STOCK_B)
        .increment(STOCK_B)
        .build();
    assertFalse(counter.isEmpty());
    assertEquals(1, counter.getCountOrZero(STOCK_A));
    assertEquals(3, counter.getCountOrZero(STOCK_B));
    assertIidSetEquals(iidSetOf(STOCK_A, STOCK_B), counter.getItemsWithNonZeroCounts());
    assertEquals(4, counter.getSumOfCounts());
  }

  @Test
  public void testGetItemsWithCountOf() {
    IidCounter counter = IidCounterBuilder.<String>iidCounterBuilder()
        .add(STOCK_A, 0)
        .add(STOCK_B, 1)
        .add(STOCK_C, 1)
        .add(STOCK_D, 2)
        .add(STOCK_E, 2)
        .add(FakeInstruments.STOCK_F, 3)
        .build();
    assertIidSetEquals(singletonIidSet(STOCK_A),   counter.getItemsWithCountOf(0));
    assertIidSetEquals(iidSetOf(STOCK_B, STOCK_C), counter.getItemsWithCountOf(1));
    assertIidSetEquals(iidSetOf(STOCK_D, STOCK_E), counter.getItemsWithCountOf(2));
    assertIidSetEquals(singletonIidSet(FakeInstruments.STOCK_F),   counter.getItemsWithCountOf(3));
    assertIidSetEquals(emptyIidSet(),              counter.getItemsWithCountOf(4));

    assertIllegalArgumentException( () -> counter.getItemsWithCountOf(-1));
    assertIllegalArgumentException( () -> counter.getItemsWithCountOf(-999));
  }

  @Test
  public void testGetItemsWithCountOfAtLeast() {
    IidCounter counter = iidCounterBuilder()
        .add(STOCK_A, 0)
        .add(STOCK_B, 1)
        .add(STOCK_C, 2)
        .add(STOCK_D, 2)
        .add(STOCK_E, 3)
        .build();
    assertIidSetEquals(iidSetOf(STOCK_A, STOCK_B, STOCK_C, STOCK_D, STOCK_E), counter.getItemsWithCountOfAtLeast(0));
    assertIidSetEquals(iidSetOf(         STOCK_B, STOCK_C, STOCK_D, STOCK_E), counter.getItemsWithCountOfAtLeast(1));
    assertIidSetEquals(iidSetOf(                  STOCK_C, STOCK_D, STOCK_E), counter.getItemsWithCountOfAtLeast(2));
    assertEquals(singletonIidSet(                                   STOCK_E), counter.getItemsWithCountOfAtLeast(3));
    assertEquals(emptyIidSet(),                                                                                                                               counter.getItemsWithCountOfAtLeast(4));

    // the following is equivalent to getItemsWithCountOfAtLeast(1):
    assertIidSetEquals(iidSetOf(         STOCK_B, STOCK_C, STOCK_D, STOCK_E), counter.getItemsWithNonZeroCounts());

    assertIllegalArgumentException( () -> counter.getItemsWithCountOf(-999));
    assertIllegalArgumentException( () -> counter.getItemsWithCountOf(-1));
  }

  @Test
  public void testAdd() {
    IidCounter counter = iidCounterBuilder()
        .add(STOCK_A, 7)
        .build();
    assertEquals(7, counter.getCountOrZero(STOCK_A));
    assertEquals(7, counter.getSumOfCounts());
    counter = iidCounterBuilder()
        .add(STOCK_A, 7)
        .add(STOCK_A, 100)
        .build();
    assertEquals(107, counter.getCountOrZero(STOCK_A));
    assertEquals(107, counter.getSumOfCounts());
  }
  
  @Test
  public void testIidCounterFromStream() {
    List<TestHasInstrumentId> items = ImmutableList.of(
        testHasInstrumentId(FakeInstruments.STOCK_A1, 0.1),
        testHasInstrumentId(FakeInstruments.STOCK_A1, 999), // excluded from count
        testHasInstrumentId(FakeInstruments.STOCK_A1, 999), // excluded from count
        testHasInstrumentId(FakeInstruments.STOCK_A2, 0.2),
        testHasInstrumentId(FakeInstruments.STOCK_A2, 0.3),
        testHasInstrumentId(FakeInstruments.STOCK_A2, 999));
    MatcherAssert.assertThat(
        "Using a predicate to exclude some items",
        iidCounterFromStream(
            items.stream(),
            v -> v.getNumericValue() < 1.0),
        iidCounterMatcher(
            iidCounterBuilder()
                .add(FakeInstruments.STOCK_A1, 1)
                .add(FakeInstruments.STOCK_A2, 2)
                .build()));
    MatcherAssert.assertThat(
        "Not using a predicate; all items are included",
        iidCounterFromStream(
            items.stream()),
        iidCounterMatcher(
            iidCounterBuilder()
                .add(FakeInstruments.STOCK_A1, 3)
                .add(FakeInstruments.STOCK_A2, 3)
                .build()));
  }

  @Override
  public IidCounter makeTrivialObject() {
    return emptyIidCounter();
  }

  @Override
  public IidCounter makeNontrivialObject() {
    return iidCounterBuilder()
        .increment(STOCK_A)
        .increment(STOCK_B)
        .add(STOCK_A, 100)
        .add(STOCK_B, 200)
        .build();
  }

  @Override
  public IidCounter makeMatchingNontrivialObject() {
    return iidCounterBuilder()
        .add(STOCK_A, 100)
        .add(STOCK_B, 200)
        .increment(STOCK_A)
        .increment(STOCK_B)
        .build();
  }

  @Override
  protected boolean willMatch(IidCounter expected, IidCounter actual) {
    return iidCounterMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<IidCounter> iidCounterMatcher(IidCounter expected) {
    return makeMatcher(expected,
        matchIidMap(v -> v.getRawMap(), f -> RBValueMatchers.typeSafeEqualTo(f)));
  }

}