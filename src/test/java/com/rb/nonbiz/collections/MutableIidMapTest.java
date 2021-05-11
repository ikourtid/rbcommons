package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.collections.IidMapTest.iidMapEqualityMatcher;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MutableIidMapTest {

  @Test
  public void testGettersAndPutters() {
    MutableIidMap<String> emptyMap = newMutableIidMap();

    assertIllegalArgumentException( () -> emptyMap.getOrThrow(STOCK_A));
    assertIllegalArgumentException( () -> emptyMap.getOrThrow(STOCK_A, "template overload"));
    assertOptionalEmpty(emptyMap.getOptional(STOCK_A));
    assertNull(emptyMap.getOrNull(STOCK_A));

    assertEquals("a", emptyMap.getOrDefault(STOCK_A, "a"));
    assertEquals("a", emptyMap.getOrDefault(STOCK_A, () -> "a"));
    // but neither of the 2 statements above put in "a" into the map
    assertIllegalArgumentException( () -> emptyMap.getOrThrow(STOCK_A));
    assertIllegalArgumentException( () -> emptyMap.getOrThrow(STOCK_A, "template overload"));
    assertOptionalEmpty(emptyMap.getOptional(STOCK_A));
    assertNull(emptyMap.getOrNull(STOCK_A));

    MutableIidMap<String> mutableMap = newMutableIidMap();
    mutableMap.putIfAbsent(STOCK_A, "_a");
    assertEquals("_a", mutableMap.getOrThrow(STOCK_A));
    assertEquals("_a", mutableMap.getOrNull(STOCK_A));
    mutableMap.putIfAbsent(STOCK_A, "b");
    assertEquals("_a", mutableMap.getOrThrow(STOCK_A));
    assertEquals("_a", mutableMap.getOrNull(STOCK_A));
    mutableMap.put(STOCK_A, "a");
    assertEquals("a", mutableMap.getOrThrow(STOCK_A));
    assertEquals("a", mutableMap.getOrNull(STOCK_A));

    assertIllegalArgumentException( () -> mutableMap.putAssumingAbsent(STOCK_A, DUMMY_STRING));

    mutableMap.remove(STOCK_A);
    assertTrue(mutableMap.isEmpty());
    assertIllegalArgumentException( () -> mutableMap.getOrThrow(STOCK_A));
    assertIllegalArgumentException( () -> mutableMap.getOrThrow(STOCK_A, "template overload"));
    assertOptionalEmpty(mutableMap.getOptional(STOCK_A));
    assertNull(mutableMap.getOrNull(STOCK_A));

    assertEquals("a", mutableMap.getIfPresentElsePut(STOCK_A, () -> "a"));
    assertEquals("a", mutableMap.getOrThrow(STOCK_A));
    assertEquals("a", mutableMap.getOrNull(STOCK_A));
  }

  @Test
  public void initialSizeIsJustAHint_canGrowBiggerIfNeeded() {
    rbSetOf(
        newMutableIidMap(),
        newMutableIidMapWithExpectedSize(0),
        newMutableIidMapWithExpectedSize(1),
        newMutableIidMapWithExpectedSize(77))
        .forEach(mutableMap -> {
          mutableMap.put(STOCK_A, "A");
          mutableMap.put(STOCK_B, "B");
          assertEquals(2, mutableMap.size());
          assertEquals("A", mutableMap.getOrThrow(STOCK_A));
          assertEquals("B", mutableMap.getOrThrow(STOCK_B));
        });
  }

  @Test
  public void testPossiblyInitializeAndThenUpdateInPlace() {
    MutableIidMap<List<String>> mutableMap = newMutableIidMap();
    assertTrue(mutableMap.isEmpty());
    mutableMap.possiblyInitializeAndThenUpdateInPlace(STOCK_A, () -> newArrayList(), list -> list.add("x"));
    assertThat(
        newIidMap(mutableMap),
        iidMapEqualityMatcher(
            singletonIidMap(STOCK_A, singletonList("x"))));
    mutableMap.possiblyInitializeAndThenUpdateInPlace(STOCK_A, () -> newArrayList(), list -> list.add("y"));
    assertThat(
        newIidMap(mutableMap),
        iidMapEqualityMatcher(
            singletonIidMap(STOCK_A, ImmutableList.of("x", "y"))));
  }

}
