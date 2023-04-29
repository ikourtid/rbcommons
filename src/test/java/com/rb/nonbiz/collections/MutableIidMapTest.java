package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Price;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.types.Price.price;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.collections.IidMapTest.iidMapEqualityMatcher;
import static com.rb.nonbiz.collections.IidMapTest.iidMapMatcher;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.collections.PairOfSameTypeTest.pairOfSameTypeEqualityMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

  @Test
  public void testPutAssumingAbsentOrEqual() {
    BiPredicate<Price, Price> almostEqualityPredicate = (px1, px2) -> px1.almostEquals(px2, epsilon(0.001));
    MutableIidMap<Price> mutableIidMap = newMutableIidMap();

    mutableIidMap.putAssumingAbsentOrEqual(STOCK_A, price(10), almostEqualityPredicate);

    assertEquals(1, mutableIidMap.size());
    assertAlmostEquals(price(10), mutableIidMap.getOrThrow(STOCK_A), DEFAULT_EPSILON_1e_8);

    // Throws because the new price is off by more than the epsilon, i.e. it is not 'equal' according to the
    // almostEqualityPredicate
    assertIllegalArgumentException( () -> mutableIidMap.putAssumingAbsentOrEqual(
        STOCK_A, price(10.0011), almostEqualityPredicate));

    // Just within the epsilon, as per the almostEqualityPredicate
    mutableIidMap.putAssumingAbsentOrEqual(STOCK_A, price(10.0009), almostEqualityPredicate);
    // this should insert the new 'almost equal' value, to match the name of this method
    assertEquals(1, mutableIidMap.size());
    assertAlmostEquals(price(10.0009), mutableIidMap.getOrThrow(STOCK_A), DEFAULT_EPSILON_1e_8);
  }

  @Test
  public void testPutAssumingNoChange() {
    MutableIidMap<PairOfSameType<String>> mutableMap = newMutableIidMap();
    BiFunction<String, String, Boolean> adder = (str1, str2) -> mutableMap.putAssumingNoChange(
        STOCK_A,
        pairOfSameType(str1, str2),
        // For this test, we are intentionally only checking the left side of the pair.
        (pair1, pair2) -> pair1.getLeft().equals(pair2.getLeft()));
    BiConsumer<String, String> checker = (str1, str2) -> assertThat(
        newIidMap(mutableMap),
        iidMapMatcher(
            singletonIidMap(
                STOCK_A, pairOfSameType(str1, str2)),
            f -> pairOfSameTypeEqualityMatcher(f)));

    assertTrue(
        "true means value will be added",
        adder.apply("l1", "r1"));
    checker.accept("l1", "r1");

    // Cannot add a pair with a different left value, because the comparison predicate will count it as a change
    assertIllegalArgumentException( () -> adder.apply("l2", "r1"));
    checker.accept("l1", "r1");

    // OK to add a pair with a different *right* value, because the comparison predicate only looks at the left item
    assertFalse(
        "false means value will not be added",
        adder.apply("l1", "r2"));
    // However, per the semantics of putAssumingNoChange, the value will not be replaced.
    checker.accept("l1", "r1");
  }
  
}
