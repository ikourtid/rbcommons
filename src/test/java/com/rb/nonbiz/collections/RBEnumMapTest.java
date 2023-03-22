package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;
import static com.rb.nonbiz.collections.RBEnumMapTest.TestEnum.*;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.rbSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.stringMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.emptyRBEnumMap;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.rbEnumMapOf;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonRBEnumMap;
import static com.rb.nonbiz.util.RBEnumMaps.enumMapCoveringAllEnumValues;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class RBEnumMapTest {

  private final RBEnumMap<TestEnumXYZ, String> EMPTY_MAP = emptyRBEnumMap(TestEnumXYZ.class);
  private final RBEnumMap<TestEnumXYZ, String> MAP_1_ITEM =  singletonRBEnumMap(TestEnumXYZ.X, "String_X");
  private final RBEnumMap<TestEnumXYZ, String> MAP_2_ITEMS = rbEnumMapOf(
      TestEnumXYZ.X, "String_X",
      TestEnumXYZ.Y, "String_Y");
  private final RBEnumMap<TestEnumXYZ, String> MAP_3_ITEMS = rbEnumMapOf(
      TestEnumXYZ.X, "String_X",
      TestEnumXYZ.Y, "String_Y",
      TestEnumXYZ.Z, "String_Z");
  private final RBSet<RBEnumMap<TestEnumXYZ, String>> ENUM_MAPS = rbSetOf(
      EMPTY_MAP,
      MAP_1_ITEM,
      MAP_2_ITEMS,
      MAP_3_ITEMS);

  @Test
  public void testSize() {
    assertEquals(0, EMPTY_MAP.size());
    assertEquals(1, MAP_1_ITEM.size());
    assertEquals(2, MAP_2_ITEMS.size());
    assertEquals(3, MAP_3_ITEMS.size());
  }

  @Test
  public void testIsEmpty() {
    assertTrue( EMPTY_MAP.isEmpty());
    assertFalse(MAP_1_ITEM.isEmpty());
    assertFalse(MAP_2_ITEMS.isEmpty());
    assertFalse(MAP_3_ITEMS.isEmpty());
  }

  @Test
  public void testContainsKey() {
    // Empty map has no keys.
    assertFalse(EMPTY_MAP.containsKey(TestEnumXYZ.X));
    assertFalse(EMPTY_MAP.containsKey(TestEnumXYZ.Y));
    assertFalse(EMPTY_MAP.containsKey(TestEnumXYZ.Z));
    // One item map has only X.
    assertTrue( MAP_1_ITEM.containsKey(TestEnumXYZ.X));
    assertFalse(MAP_1_ITEM.containsKey(TestEnumXYZ.Y));
    assertFalse(MAP_1_ITEM.containsKey(TestEnumXYZ.Z));
    // Two item map has only X and Y.
    assertTrue( MAP_2_ITEMS.containsKey(TestEnumXYZ.X));
    assertTrue( MAP_2_ITEMS.containsKey(TestEnumXYZ.Y));
    assertFalse(MAP_2_ITEMS.containsKey(TestEnumXYZ.Z));
    // Three item map has X, Y, Z.
    assertTrue( MAP_3_ITEMS.containsKey(TestEnumXYZ.X));
    assertTrue( MAP_3_ITEMS.containsKey(TestEnumXYZ.Y));
    assertTrue( MAP_3_ITEMS.containsKey(TestEnumXYZ.Z));
  }

  @Test
  public void testContainsValue() {
    // Empty map has no values.
    assertFalse(EMPTY_MAP.containsValue("String_X"));
    assertFalse(EMPTY_MAP.containsValue("String_Y"));
    assertFalse(EMPTY_MAP.containsValue("String_Z"));
    assertFalse(EMPTY_MAP.containsValue("NO_VALUE"));

    // One item map has only X.
    assertTrue( MAP_1_ITEM.containsValue("String_X"));
    assertFalse(MAP_1_ITEM.containsValue("String_Y"));
    assertFalse(MAP_1_ITEM.containsValue("String_Z"));
    assertFalse(MAP_1_ITEM.containsValue("NO_VALUE"));

    // Three item map has X, Y, Z.
    assertTrue( MAP_3_ITEMS.containsValue("String_X"));
    assertTrue( MAP_3_ITEMS.containsValue("String_Y"));
    assertTrue( MAP_3_ITEMS.containsValue("String_Z"));
    assertFalse(MAP_3_ITEMS.containsValue("NO_VALUE"));
  }

  @Test
  public void testGetOptional() {
    // Empty map has no values.
    assertOptionalEmpty(EMPTY_MAP.getOptional(TestEnumXYZ.X));
    assertOptionalEmpty(EMPTY_MAP.getOptional(TestEnumXYZ.Y));
    assertOptionalEmpty(EMPTY_MAP.getOptional(TestEnumXYZ.Z));

    // One item map has 1 value.
    assertOptionalEquals("String_X", MAP_1_ITEM.getOptional(TestEnumXYZ.X));
    assertOptionalEmpty(             MAP_1_ITEM.getOptional(TestEnumXYZ.Y));
    assertOptionalEmpty(             MAP_1_ITEM.getOptional(TestEnumXYZ.Z));

    // Three item map has 3 values.
    assertOptionalEquals("String_X", MAP_3_ITEMS.getOptional(TestEnumXYZ.X));
    assertOptionalEquals("String_Y", MAP_3_ITEMS.getOptional(TestEnumXYZ.Y));
    assertOptionalEquals("String_Z", MAP_3_ITEMS.getOptional(TestEnumXYZ.Z));
  }

  @Test
  public void testGetOrDefault() {
    assertEquals("Default", EMPTY_MAP.getOrDefault(TestEnumXYZ.X, "Default"));
    assertEquals("Default", EMPTY_MAP.getOrDefault(TestEnumXYZ.Y, "Default"));
    assertEquals("Default", EMPTY_MAP.getOrDefault(TestEnumXYZ.Z, "Default"));

    assertEquals("String_X", MAP_1_ITEM.getOrDefault(TestEnumXYZ.X, "Default"));
    assertEquals("Default",  MAP_1_ITEM.getOrDefault(TestEnumXYZ.Y, "Default"));
    assertEquals("Default",  MAP_1_ITEM.getOrDefault(TestEnumXYZ.Z, "Default"));

    assertEquals("String_X", MAP_3_ITEMS.getOrDefault(TestEnumXYZ.X, "Default"));
    assertEquals("String_Y", MAP_3_ITEMS.getOrDefault(TestEnumXYZ.Y, "Default"));
    assertEquals("String_Z", MAP_3_ITEMS.getOrDefault(TestEnumXYZ.Z, "Default"));
  }

  @Test
  public void testGetOrThrow() {
    assertIllegalArgumentException( () -> EMPTY_MAP.getOrThrow(TestEnumXYZ.X));
    assertIllegalArgumentException( () -> EMPTY_MAP.getOrThrow(TestEnumXYZ.Y));
    assertIllegalArgumentException( () -> EMPTY_MAP.getOrThrow(TestEnumXYZ.Z));

    assertEquals("String_X",              MAP_1_ITEM.getOrThrow(TestEnumXYZ.X));
    assertIllegalArgumentException( () -> MAP_1_ITEM.getOrThrow(TestEnumXYZ.Y));
    assertIllegalArgumentException( () -> MAP_1_ITEM.getOrThrow(TestEnumXYZ.Z));

    assertEquals("String_X", MAP_3_ITEMS.getOrThrow(TestEnumXYZ.X));
    assertEquals("String_Y", MAP_3_ITEMS.getOrThrow(TestEnumXYZ.Y));
    assertEquals("String_Z", MAP_3_ITEMS.getOrThrow(TestEnumXYZ.Z));
  }

  @Test
  public void testKeySet() {
    assertEmpty(EMPTY_MAP.keySet());
    assertThat(
        rbSet(MAP_1_ITEM.keySet()),
        rbSetEqualsMatcher(singletonRBSet(TestEnumXYZ.X)));
    assertThat(
        rbSet(MAP_2_ITEMS.keySet()),
        rbSetEqualsMatcher(rbSetOf(TestEnumXYZ.X, TestEnumXYZ.Y)));
    assertThat(
        rbSet(MAP_3_ITEMS.keySet()),
        rbSetEqualsMatcher(rbSetOf(TestEnumXYZ.X, TestEnumXYZ.Y, TestEnumXYZ.Z)));
  }

  @Test
  public void testEntrySet() {
    // Test RBEnumMap's entry set is the same as the raw RBEnumMap.
    ENUM_MAPS.forEach( rbEnumMap ->
        assertThat(
            // Below we use rbSet wrappers so we can use an rbSet equality matcher.
            rbSet(rbEnumMap.entrySet()),
            rbSetEqualsMatcher(rbSet(rbEnumMap.entrySet()))));
  }

  @Test
  public void testForEachEntry() {
    StringBuilder sb = new StringBuilder();

    rbEnumMapOf(
        TestEnumXYZ.X, "x",
        TestEnumXYZ.Z, "z")
        .forEachEntryInKeyOrder( (enumKey, value) -> sb.append(Strings.format(
            "k=%s v=%s ", enumKey.toString(), value)));
    assertThat(
        sb.toString(),
        stringMatcher("k=X v=x k=Z v=z "));
  }

  @Test
  public void testTestHashCode() {
    assertNotEquals(MAP_1_ITEM.hashCode(), EMPTY_MAP.hashCode());
    assertEquals(   MAP_1_ITEM.hashCode(), MAP_1_ITEM.hashCode());
    assertNotEquals(MAP_1_ITEM.hashCode(), MAP_2_ITEMS.hashCode());
    assertNotEquals(MAP_1_ITEM.hashCode(), MAP_3_ITEMS.hashCode());
  }

  @Test
  public void testTestToString() {
    assertEquals("{}", EMPTY_MAP.toString());
    assertEquals("{X=String_X}", MAP_1_ITEM.toString());
    assertEquals("{X=String_X, Y=String_Y}", MAP_2_ITEMS.toString());
    assertEquals("{X=String_X, Y=String_Y, Z=String_Z}", MAP_3_ITEMS.toString());
  }

  // We never define enums this succinctly, but this is just to help the next test method,
  // and local enums are not supported in Java 8 apparently.
  enum TestEnum { A, B, C, D, E, F, G, H, I, J }

  @Test
  public void testAccessIsInEnumOrder() {
    RBEnumMap<TestEnum, String> rbEnumMap = newRBEnumMap(
        TestEnum.class,
        enumMapCoveringAllEnumValues(
            TestEnum.class, rbMapOf(
                A, "_A",
                C, "_C",
                E, "_E",
                G, "_G",
                I, "_I",
                B, "_B",
                D, "_D",
                F, "_F",
                H, "_H",
                J, "_J")));

    assertThat(
        newArrayList(rbEnumMap.keySet()),
        orderedListEqualityMatcher(
            ImmutableList.of(A, B, C, D, E, F, G, H, I, J)));

    assertThat(
        rbEnumMap.entrySet().stream().map(v -> v.getKey()).collect(Collectors.toList()),
        orderedListEqualityMatcher(
            ImmutableList.of(A, B, C, D, E, F, G, H, I, J)));

    assertThat(
        newArrayList(rbEnumMap.values()),
        orderedListEqualityMatcher(
            ImmutableList.of("_A", "_B", "_C", "_D", "_E", "_F", "_G", "_H", "_I", "_J")));

    assertThat(
        rbEnumMap.entrySet().stream().map(v -> v.getValue()).collect(Collectors.toList()),
        orderedListEqualityMatcher(
            ImmutableList.of("_A", "_B", "_C", "_D", "_E", "_F", "_G", "_H", "_I", "_J")));

    StringBuilder keys = new StringBuilder();
    StringBuilder values = new StringBuilder();
    rbEnumMap.forEachEntryInKeyOrder( (enumConstantKey, value) -> {
      keys.append(enumConstantKey);
      values.append(value);
    });
    assertEquals(
        keys.toString(),
        "ABCDEFGHIJ");
    assertEquals(
        values.toString(),
        "_A_B_C_D_E_F_G_H_I_J");
  }

}