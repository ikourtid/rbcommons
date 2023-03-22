package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.MutableRBEnumMap.newMutableRBEnumMap;
import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbEnumMapEqualityMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertNullPointerException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.rbEnumMapOf;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonRBEnumMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.jmock.AbstractExpectations.same;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MutableRBEnumMapTest {

  @Test
  public void putAssumingAbsent_failsIfKeyAlreadyHasAValue() {
    MutableRBEnumMap<TestEnumXYZ, Integer> map = newMutableRBEnumMap(TestEnumXYZ.class);
    map.putAssumingAbsent(TestEnumXYZ.X, 123);
    assertIllegalArgumentException( () -> map.putAssumingAbsent(TestEnumXYZ.X, 123));
    assertIllegalArgumentException( () -> map.putAssumingAbsent(TestEnumXYZ.X, 321));
  }

  @Test
  public void putAssumingPresent_failsIfKeyHasNoValue() {
    MutableRBEnumMap<TestEnumXYZ, Integer> map = newMutableRBEnumMap(TestEnumXYZ.class);
    assertIllegalArgumentException( () -> map.putAssumingPresent(TestEnumXYZ.X, 123));
    map.put(TestEnumXYZ.X, 321);
    map.putAssumingPresent(TestEnumXYZ.X, 999);
    assertEquals(999, map.getOrThrow(TestEnumXYZ.X).intValue());
  }

  @Test
  public void get_disallowsNullKeys() {
    MutableRBEnumMap<TestEnumXYZ, Integer> map = newMutableRBEnumMap(TestEnumXYZ.class);
    assertIllegalArgumentException( () -> map.getOptional(null));
    assertIllegalArgumentException( () -> map.getOrDefault(null, 1));
  }

  @Test
  public void put_disallowsNullKeys() {
    MutableRBEnumMap<TestEnumXYZ, Integer> map = newMutableRBEnumMap(TestEnumXYZ.class);
    assertIllegalArgumentException( () -> map.put(null, 1));
    assertIllegalArgumentException( () -> map.putIfAbsent(null, 1));
    assertIllegalArgumentException( () -> map.putAssumingAbsent(null, 1));
  }

  @Test
  public void put_disallowsNullValues() {
    MutableRBEnumMap<TestEnumXYZ, Integer> map = newMutableRBEnumMap(TestEnumXYZ.class);
    assertIllegalArgumentException( () -> map.put(TestEnumXYZ.X, null));
    assertNullPointerException( () -> map.putIfAbsent(TestEnumXYZ.X, () -> null));
    assertIllegalArgumentException( () -> map.putAssumingAbsent(TestEnumXYZ.X, null));
  }

  @Test
  public void testWithSuppliedKeys_valuesAreConstant() {
    Supplier<Integer> valueSupplier = () -> 123;
    assertEmpty(newMutableRBEnumMap(TestEnumXYZ.class, emptyRBSet(), valueSupplier).entrySet());
    assertEquals(
        newRBEnumMap(newMutableRBEnumMap(TestEnumXYZ.class, singletonRBSet(TestEnumXYZ.X), valueSupplier)),
        singletonRBEnumMap(TestEnumXYZ.X, 123));
    assertEquals(
        newRBEnumMap(newMutableRBEnumMap(TestEnumXYZ.class, rbSetOf(TestEnumXYZ.X, TestEnumXYZ.Y), valueSupplier)),
        rbEnumMapOf(
            TestEnumXYZ.X, 123,
            TestEnumXYZ.Y, 123));
  }

  @Test
  public void testWithSuppliedKeys_valuesAreNotConstant() {
    Supplier<List<Integer>> valueSupplier = () -> Lists.newArrayList();
    assertEmpty((newMutableRBEnumMap(TestEnumXYZ.class, emptyRBSet(), valueSupplier)).entrySet());
    assertEquals(
        newRBEnumMap(newMutableRBEnumMap(TestEnumXYZ.class, singletonRBSet(TestEnumXYZ.X), valueSupplier)),
        singletonRBEnumMap(TestEnumXYZ.X, emptyList()));
    RBEnumMap<TestEnumXYZ, List<Integer>> map = newRBEnumMap(newMutableRBEnumMap(
        TestEnumXYZ.class, rbSetOf(TestEnumXYZ.X, TestEnumXYZ.Y), valueSupplier));
    assertThat(
        map,
        rbEnumMapEqualityMatcher(
            rbEnumMapOf(
                TestEnumXYZ.X, emptyList(),
                TestEnumXYZ.Y, emptyList())));
    assertThat(
        "Since the supplier creates a new list each time, the a and b lists should not be the same object",
        map.getOrThrow(TestEnumXYZ.X),
        not(same(map.getOrThrow(TestEnumXYZ.Y))));
  }

  @Test
  public void testPossiblyInitializeAndThenUpdate() {
    TestEnumXYZ onlyKey = TestEnumXYZ.Y;
    {
      MutableRBEnumMap<TestEnumXYZ, String> mutableMap = newMutableRBEnumMap(TestEnumXYZ.class);
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> "", s -> s + "x");
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> "", s -> s + "y");
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> "", s -> s + "z");
      assertEquals(
          singletonRBEnumMap(onlyKey, "xyz"),
          newRBEnumMap(mutableMap));
    }
    {
      MutableRBEnumMap<TestEnumXYZ, Integer> mutableMap = newMutableRBEnumMap(TestEnumXYZ.class);
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> 10, s -> s + 3);
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> 10, s -> s + 4);
      mutableMap.possiblyInitializeAndThenUpdate(onlyKey, () -> 10, s -> s + 5);
      assertEquals(
          "1st call just initializes; 2nd and 3rd increment by 4 and 5, respectively",
          singletonRBEnumMap(onlyKey, intExplained(22, 10 + 3 + 4 + 5)),
          newRBEnumMap(mutableMap));
    }
  }

  @Test
  public void testEitherInitializeOrUpdate() {
    TestEnumXYZ onlyKey = TestEnumXYZ.Y;
    {
      MutableRBEnumMap<TestEnumXYZ, String> mutableMap = newMutableRBEnumMap(TestEnumXYZ.class);
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> "", s -> s + "x");
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> "", s -> s + "y");
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> "", s -> s + "z");
      assertEquals(
          "1st call just initializes; 2nd and 3rd append y and z, respectively",
          singletonRBEnumMap(onlyKey, "yz"),
          newRBEnumMap(mutableMap));
    }
    {
      MutableRBEnumMap<TestEnumXYZ, Integer> mutableMap = newMutableRBEnumMap(TestEnumXYZ.class);
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> 10, s -> s + 3);
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> 10, s -> s + 4);
      mutableMap.eitherInitializeOrUpdate(onlyKey, () -> 10, s -> s + 5);
      assertEquals(
          "1st call just initializes; 2nd and 3rd increment by 4 and 5, respectively",
          singletonRBEnumMap(onlyKey, intExplained(19, 10 + 4 + 5)),
          newRBEnumMap(mutableMap));
    }
  }

  @Test
  public void testGettersAndPutters() {
    MutableRBEnumMap<TestEnumXYZ, String> emptyMap = newMutableRBEnumMap(TestEnumXYZ.class);

    assertIllegalArgumentException( () -> emptyMap.getOrThrow(TestEnumXYZ.X));
    assertIllegalArgumentException( () -> emptyMap.getOrThrow(TestEnumXYZ.X, "template overload"));
    assertOptionalEmpty(emptyMap.getOptional(TestEnumXYZ.X));

    assertEquals("_def", emptyMap.getOrDefault(TestEnumXYZ.X, "_def"));
    assertEquals("_def", emptyMap.getOrDefault(TestEnumXYZ.X, () -> "_def"));
    // but neither of the 2 statements above put in TestEnumXYZ.X into the map
    assertIllegalArgumentException( () -> emptyMap.getOrThrow(TestEnumXYZ.X));
    assertIllegalArgumentException( () -> emptyMap.getOrThrow(TestEnumXYZ.X, "template overload"));
    assertOptionalEmpty(emptyMap.getOptional(TestEnumXYZ.X));

    MutableRBEnumMap<TestEnumXYZ, String> mutableMap = newMutableRBEnumMap(TestEnumXYZ.class);
    mutableMap.putIfAbsent(TestEnumXYZ.X, "x1");
    assertEquals("x1", mutableMap.getOrThrow(TestEnumXYZ.X));
    mutableMap.putIfAbsent(TestEnumXYZ.X, "x2");
    assertEquals("x1", mutableMap.getOrThrow(TestEnumXYZ.X));
    mutableMap.put(TestEnumXYZ.X, "x3");
    assertEquals("x3", mutableMap.getOrThrow(TestEnumXYZ.X));

    assertIllegalArgumentException( () -> mutableMap.putAssumingAbsent(TestEnumXYZ.X, DUMMY_STRING));

    mutableMap.remove(TestEnumXYZ.X);
    assertTrue(mutableMap.isEmpty());
    assertIllegalArgumentException( () -> mutableMap.getOrThrow(TestEnumXYZ.X));
    assertIllegalArgumentException( () -> mutableMap.getOrThrow(TestEnumXYZ.X, "template overload"));
    assertOptionalEmpty(mutableMap.getOptional(TestEnumXYZ.X));

    assertEquals("x4", mutableMap.getIfPresentElsePut(TestEnumXYZ.X, () -> "x4"));
    assertEquals("x4", mutableMap.getOrThrow(TestEnumXYZ.X));
  }

  @Test
  public void testPossiblyInitializeAndThenUpdateInPlace() {
    MutableRBEnumMap<TestEnumXYZ, List<String>> mutableMap = newMutableRBEnumMap(TestEnumXYZ.class);
    assertTrue(mutableMap.isEmpty());
    mutableMap.possiblyInitializeAndThenUpdateInPlace(TestEnumXYZ.X, () -> newArrayList(), list -> list.add("x"));
    assertEquals(
        singletonRBEnumMap(TestEnumXYZ.X, singletonList("x")),
        newRBEnumMap(mutableMap));
    mutableMap.possiblyInitializeAndThenUpdateInPlace(TestEnumXYZ.X, () -> newArrayList(), list -> list.add("y"));
    assertEquals(
        singletonRBEnumMap(TestEnumXYZ.X, ImmutableList.of("x", "y")),
        newRBEnumMap(mutableMap));
  }

}