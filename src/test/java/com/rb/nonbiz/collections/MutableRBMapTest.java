package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertNullPointerException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.IsNot.not;
import static org.jmock.AbstractExpectations.same;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class MutableRBMapTest {

  @Test
  public void putAssumingAbsent_failsIfKeyAlreadyHasAValue() {
    MutableRBMap<String, Integer> map = newMutableRBMap();
    map.putAssumingAbsent("a", 123);
    assertIllegalArgumentException( () -> map.putAssumingAbsent("a", 123));
    assertIllegalArgumentException( () -> map.putAssumingAbsent("a", 321));
  }

  @Test
  public void putAssumingPresent_failsIfKeyHasNoValue() {
    MutableRBMap<String, Integer> map = newMutableRBMap();
    assertIllegalArgumentException( () -> map.putAssumingPresent("a", 123));
    map.put("a", 321);
    map.putAssumingPresent("a", 999);
    assertEquals(999, map.getOrThrow("a").intValue());
  }

  @Test
  public void get_disallowsNullKeys() {
    MutableRBMap<String, Integer> map = newMutableRBMap();
    assertIllegalArgumentException( () -> map.getOptional(null));
    assertIllegalArgumentException( () -> map.getOrDefault(null, 1));
  }

  @Test
  public void put_disallowsNullKeys() {
    MutableRBMap<String, Integer> map = newMutableRBMap();
    assertIllegalArgumentException( () -> map.put(null, 1));
    assertIllegalArgumentException( () -> map.putIfAbsent(null, 1));
    assertIllegalArgumentException( () -> map.putAssumingAbsent(null, 1));
  }

  @Test
  public void put_disallowsNullValues() {
    MutableRBMap<String, Integer> map = newMutableRBMap();
    assertIllegalArgumentException( () -> map.put("a", null));
    assertNullPointerException( () -> map.putIfAbsent("a", () -> null));
    assertIllegalArgumentException( () -> map.putAssumingAbsent("a", null));
  }

  @Test
  public void testWithSuppliedKeys_valuesAreConstant() {
    Supplier<Integer> valueSupplier = () -> 123;
    assertEmpty(newMutableRBMap(emptyRBSet(), valueSupplier).entrySet());
    assertEquals(
        newRBMap(newMutableRBMap(singletonRBSet("a"), valueSupplier)),
        singletonRBMap("a", 123));
    assertEquals(
        newRBMap(newMutableRBMap(rbSetOf("a", "b"), valueSupplier)),
        rbMapOf(
            "a", 123,
            "b", 123));
  }

  @Test
  public void testWithSuppliedKeys_valuesAreNotConstant() {
    Supplier<List<Integer>> valueSupplier = () -> Lists.newArrayList();
    assertEmpty((newMutableRBMap(emptyRBSet(), valueSupplier)).entrySet());
    assertEquals(
        newRBMap(newMutableRBMap(singletonRBSet("a"), valueSupplier)),
        singletonRBMap("a", emptyList()));
    RBMap<String, List<Integer>> map = newRBMap(newMutableRBMap(rbSetOf("a", "b"), valueSupplier));
    assertEquals(map, rbMapOf(
        "a", emptyList(),
        "b", emptyList()));
    assertThat(
        "Since the supplier creates a new list each time, the a and b lists should not be the same object",
        map.getOrThrow("a"),
        not(same(map.getOrThrow("b"))));
  }

  @Test
  public void testPossiblyInitializeAndThenUpdate() {
    int onlyKey = 123;
    {
      MutableRBMap<Integer, String> mutableMap = newMutableRBMap();
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> "", s -> s + "x");
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> "", s -> s + "y");
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> "", s -> s + "z");
      assertEquals(
          singletonRBMap(onlyKey, "xyz"),
          newRBMap(mutableMap));
    }
    {
      MutableRBMap<Integer, Integer> mutableMap = newMutableRBMap();
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> 10, s -> s + 3);
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> 10, s -> s + 4);
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> 10, s -> s + 5);
      assertEquals(
          "1st call just initializes; 2nd and 3rd increment by 4 and 5, respectively",
          singletonRBMap(onlyKey, intExplained(22, 10 + 3 + 4 + 5)),
          newRBMap(mutableMap));
    }
  }

  @Test
  public void testEitherInitializeOrUpdate() {
    int onlyKey = 123;
    {
      MutableRBMap<Integer, String> mutableMap = newMutableRBMap();
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> "", s -> s + "x");
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> "", s -> s + "y");
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> "", s -> s + "z");
      assertEquals(
          "1st call just initializes; 2nd and 3rd append y and z, respectively",
          singletonRBMap(onlyKey, "yz"),
          newRBMap(mutableMap));
    }
    {
      MutableRBMap<Integer, Integer> mutableMap = newMutableRBMap();
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> 10, s -> s + 3);
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> 10, s -> s + 4);
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> 10, s -> s + 5);
      assertEquals(
          "1st call just initializes; 2nd and 3rd increment by 4 and 5, respectively",
          singletonRBMap(onlyKey, intExplained(19, 10 + 4 + 5)),
          newRBMap(mutableMap));
    }
  }

  @Test
  public void testGettersAndPutters() {
    MutableRBMap<Integer, String> emptyMap = newMutableRBMap();

    assertIllegalArgumentException( () -> emptyMap.getOrThrow(1));
    assertIllegalArgumentException( () -> emptyMap.getOrThrow(1, "template overload"));
    assertOptionalEmpty(emptyMap.getOptional(1));

    assertEquals("a", emptyMap.getOrDefault(1, "a"));
    assertEquals("a", emptyMap.getOrDefault(1, () -> "a"));
    // but neither of the 2 statements above put in "a" into the map
    assertIllegalArgumentException( () -> emptyMap.getOrThrow(1));
    assertIllegalArgumentException( () -> emptyMap.getOrThrow(1, "template overload"));
    assertOptionalEmpty(emptyMap.getOptional(1));

    MutableRBMap<Integer, String> mutableMap = newMutableRBMap();
    mutableMap.putIfAbsent(1, "_a");
    assertEquals("_a", mutableMap.getOrThrow(1));
    mutableMap.putIfAbsent(1, "b");
    assertEquals("_a", mutableMap.getOrThrow(1));
    mutableMap.put(1, "a");
    assertEquals("a", mutableMap.getOrThrow(1));

    assertIllegalArgumentException( () -> mutableMap.putAssumingAbsent(1, DUMMY_STRING));

    mutableMap.remove(1);
    assertTrue(mutableMap.isEmpty());
    assertIllegalArgumentException( () -> mutableMap.getOrThrow(1));
    assertIllegalArgumentException( () -> mutableMap.getOrThrow(1, "template overload"));
    assertOptionalEmpty(mutableMap.getOptional(1));

    assertEquals("a", mutableMap.getIfPresentElsePut(1, () -> "a"));
    assertEquals("a", mutableMap.getOrThrow(1));
  }

  @Test
  public void testPossiblyInitializeAndThenUpdateInPlace() {
    MutableRBMap<Integer, List<String>> mutableMap = newMutableRBMap();
    assertTrue(mutableMap.isEmpty());
    mutableMap.possiblyInitializeAndThenUpdateInPlace(123, () -> newArrayList(), list -> list.add("x"));
    assertEquals(
        singletonRBMap(123, singletonList("x")),
        newRBMap(mutableMap));
    mutableMap.possiblyInitializeAndThenUpdateInPlace(123, () -> newArrayList(), list -> list.add("y"));
    assertEquals(
        singletonRBMap(123, ImmutableList.of("x", "y")),
        newRBMap(mutableMap));
  }

}