package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Optional;
import java.util.function.Consumer;

import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;
import static com.rb.nonbiz.collections.RBSet.rbSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.enumMapEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.enumMapOf;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonEnumMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RBEnumMapTest {

  private final EnumMap<TestEnumXYZ, String> EMPTY_ENUM_MAP = new EnumMap<TestEnumXYZ, String>(TestEnumXYZ.class);
  private final EnumMap<TestEnumXYZ, String> ENUM_MAP_1_ITEM =  singletonEnumMap(TestEnumXYZ.X, "String_X");
  private final EnumMap<TestEnumXYZ, String> ENUM_MAP_2_ITEMS = enumMapOf(
      TestEnumXYZ.X, "String_X",
      TestEnumXYZ.Y, "String_Y");
  private final EnumMap<TestEnumXYZ, String> ENUM_MAP_3_ITEMS = enumMapOf(
      TestEnumXYZ.X, "String_X",
      TestEnumXYZ.Y, "String_Y",
      TestEnumXYZ.Z, "String_Z");
  private final RBSet<EnumMap> ENUM_MAPS = rbSetOf(EMPTY_ENUM_MAP, ENUM_MAP_1_ITEM, ENUM_MAP_2_ITEMS, ENUM_MAP_3_ITEMS);

  private final RBEnumMap<TestEnumXYZ, String> EMPTY_RB_ENUM_MAP   = newRBEnumMap(EMPTY_ENUM_MAP);
  private final RBEnumMap<TestEnumXYZ, String> RB_ENUM_MAP_1_ITEMS = newRBEnumMap(ENUM_MAP_1_ITEM);
  private final RBEnumMap<TestEnumXYZ, String> RB_ENUM_MAP_2_ITEMS = newRBEnumMap(ENUM_MAP_2_ITEMS);
  private final RBEnumMap<TestEnumXYZ, String> RB_ENUM_MAP_3_ITEMS = newRBEnumMap(ENUM_MAP_3_ITEMS);

  @Test
  public void testGetCopyOfRawMap() {

    // Test round trip conversion all maps.
    for (EnumMap enumMap : ENUM_MAPS) {
      assertThat(
          newRBEnumMap(enumMap).getCopyOfRawMap(),
          enumMapEqualityMatcher(enumMap));
    }
  }

  @Test
  public void testImmutability() {
    EnumMap<TestEnumXYZ, String> enumMap = singletonEnumMap(TestEnumXYZ.X, "X");
    RBEnumMap<TestEnumXYZ, String> rbEnumMap = newRBEnumMap(enumMap);

    // Changing the map we're constructed with doesn't change the rbmap.
    enumMap.put(TestEnumXYZ.Z, "Z");
    enumMap.replace(TestEnumXYZ.X, "X", "A");
    // RBEnumMap should be unchanged.
    assertEquals(1, rbEnumMap.size());
    assertEquals("X", rbEnumMap.getOrThrow(TestEnumXYZ.X));

    // Now call getRawMap and change the map.
    EnumMap<TestEnumXYZ, String> retrievedMap = rbEnumMap.getCopyOfRawMap();
    retrievedMap.put(TestEnumXYZ.Z, "Z");
    retrievedMap.replace(TestEnumXYZ.X, "X", "A");
    // The RBEnumMap should be unchanged.
    assertEquals(2, retrievedMap.size());
    assertEquals(1, rbEnumMap.size());
    assertEquals("X", rbEnumMap.getOrThrow(TestEnumXYZ.X));
  }

  @Test
  public void testSize() {
    assertEquals(0, newRBEnumMap(EMPTY_ENUM_MAP).size());
    assertEquals(1, newRBEnumMap(ENUM_MAP_1_ITEM).size());
    assertEquals(2, newRBEnumMap(ENUM_MAP_2_ITEMS).size());
    assertEquals(3, newRBEnumMap(ENUM_MAP_3_ITEMS).size());
  }

  @Test
  public void testIsEmpty() {
    assertTrue( EMPTY_RB_ENUM_MAP.isEmpty());
    assertFalse(RB_ENUM_MAP_1_ITEMS.isEmpty());
    assertFalse(RB_ENUM_MAP_2_ITEMS.isEmpty());
    assertFalse(RB_ENUM_MAP_3_ITEMS.isEmpty());
  }

  @Test
  public void testContainsKey() {
    // Empty map has no keys.
    assertFalse(EMPTY_RB_ENUM_MAP.containsKey(TestEnumXYZ.X));
    assertFalse(EMPTY_RB_ENUM_MAP.containsKey(TestEnumXYZ.Y));
    assertFalse(EMPTY_RB_ENUM_MAP.containsKey(TestEnumXYZ.Z));
    // One item map has only X.
    assertTrue( RB_ENUM_MAP_1_ITEMS.containsKey(TestEnumXYZ.X));
    assertFalse(RB_ENUM_MAP_1_ITEMS.containsKey(TestEnumXYZ.Y));
    assertFalse(RB_ENUM_MAP_1_ITEMS.containsKey(TestEnumXYZ.Z));
    // Two item map has only X and Y.
    assertTrue( RB_ENUM_MAP_2_ITEMS.containsKey(TestEnumXYZ.X));
    assertTrue( RB_ENUM_MAP_2_ITEMS.containsKey(TestEnumXYZ.Y));
    assertFalse(RB_ENUM_MAP_2_ITEMS.containsKey(TestEnumXYZ.Z));
    // Three item map has X, Y, Z.
    assertTrue( RB_ENUM_MAP_3_ITEMS.containsKey(TestEnumXYZ.X));
    assertTrue( RB_ENUM_MAP_3_ITEMS.containsKey(TestEnumXYZ.Y));
    assertTrue( RB_ENUM_MAP_3_ITEMS.containsKey(TestEnumXYZ.Z));
  }

  @Test
  public void testContainsValue() {
    // Empty map has no values.
    assertFalse(EMPTY_RB_ENUM_MAP.containsValue("String_X"));
    assertFalse(EMPTY_RB_ENUM_MAP.containsValue("String_Y"));
    assertFalse(EMPTY_RB_ENUM_MAP.containsValue("String_Z"));
    assertFalse(EMPTY_RB_ENUM_MAP.containsValue("NO_VALUE"));

    // One item map has only X.
    assertTrue( RB_ENUM_MAP_1_ITEMS.containsValue("String_X"));
    assertFalse(RB_ENUM_MAP_1_ITEMS.containsValue("String_Y"));
    assertFalse(RB_ENUM_MAP_1_ITEMS.containsValue("String_Z"));
    assertFalse(RB_ENUM_MAP_1_ITEMS.containsValue("NO_VALUE"));

    // Three item map has X, Y, Z.
    assertTrue( RB_ENUM_MAP_3_ITEMS.containsValue("String_X"));
    assertTrue( RB_ENUM_MAP_3_ITEMS.containsValue("String_Y"));
    assertTrue( RB_ENUM_MAP_3_ITEMS.containsValue("String_Z"));
    assertFalse(RB_ENUM_MAP_3_ITEMS.containsValue("NO_VALUE"));
  }

  @Test
  public void testGetOptional() {
    // Empty map has no values.
    assertOptionalEmpty(EMPTY_RB_ENUM_MAP.getOptional(TestEnumXYZ.X));
    assertOptionalEmpty(EMPTY_RB_ENUM_MAP.getOptional(TestEnumXYZ.Y));
    assertOptionalEmpty(EMPTY_RB_ENUM_MAP.getOptional(TestEnumXYZ.Z));

    // One item map has 1 value.
    assertOptionalEquals(RB_ENUM_MAP_1_ITEMS.getOptional(TestEnumXYZ.X), Optional.of("String_X"));
    assertOptionalEmpty( RB_ENUM_MAP_1_ITEMS.getOptional(TestEnumXYZ.Y));
    assertOptionalEmpty( RB_ENUM_MAP_1_ITEMS.getOptional(TestEnumXYZ.Z));

    // Three item map has 3 values.
    assertOptionalEquals(RB_ENUM_MAP_3_ITEMS.getOptional(TestEnumXYZ.X), Optional.of("String_X"));
    assertOptionalEquals(RB_ENUM_MAP_3_ITEMS.getOptional(TestEnumXYZ.Y), Optional.of("String_Y"));
    assertOptionalEquals(RB_ENUM_MAP_3_ITEMS.getOptional(TestEnumXYZ.Z), Optional.of("String_Z"));
  }

  @Test
  public void testGetOrDefault() {
    assertEquals("Default", EMPTY_RB_ENUM_MAP.getOrDefault(TestEnumXYZ.X, "Default"));
    assertEquals("Default", EMPTY_RB_ENUM_MAP.getOrDefault(TestEnumXYZ.Y, "Default"));
    assertEquals("Default", EMPTY_RB_ENUM_MAP.getOrDefault(TestEnumXYZ.Z, "Default"));

    assertEquals("String_X", RB_ENUM_MAP_1_ITEMS.getOrDefault(TestEnumXYZ.X, "Default"));
    assertEquals("Default",  RB_ENUM_MAP_1_ITEMS.getOrDefault(TestEnumXYZ.Y, "Default"));
    assertEquals("Default",  RB_ENUM_MAP_1_ITEMS.getOrDefault(TestEnumXYZ.Z, "Default"));

    assertEquals("String_X", RB_ENUM_MAP_3_ITEMS.getOrDefault(TestEnumXYZ.X, "Default"));
    assertEquals("String_Y", RB_ENUM_MAP_3_ITEMS.getOrDefault(TestEnumXYZ.Y, "Default"));
    assertEquals("String_Z", RB_ENUM_MAP_3_ITEMS.getOrDefault(TestEnumXYZ.Z, "Default"));
  }

  @Test
  public void testGetOrThrow() {
    assertIllegalArgumentException( () -> EMPTY_RB_ENUM_MAP.getOrThrow(TestEnumXYZ.X));
    assertIllegalArgumentException( () -> EMPTY_RB_ENUM_MAP.getOrThrow(TestEnumXYZ.Y));
    assertIllegalArgumentException( () -> EMPTY_RB_ENUM_MAP.getOrThrow(TestEnumXYZ.Z));

    assertEquals("String_X",              RB_ENUM_MAP_1_ITEMS.getOrThrow(TestEnumXYZ.X));
    assertIllegalArgumentException( () -> RB_ENUM_MAP_1_ITEMS.getOrThrow(TestEnumXYZ.Y));
    assertIllegalArgumentException( () -> RB_ENUM_MAP_1_ITEMS.getOrThrow(TestEnumXYZ.Z));

    assertEquals("String_X", RB_ENUM_MAP_3_ITEMS.getOrThrow(TestEnumXYZ.X));
    assertEquals("String_Y", RB_ENUM_MAP_3_ITEMS.getOrThrow(TestEnumXYZ.Y));
    assertEquals("String_Z", RB_ENUM_MAP_3_ITEMS.getOrThrow(TestEnumXYZ.Z));
  }

  @Test
  public void testKeySet() {
    assertEmpty(EMPTY_RB_ENUM_MAP.keySet());
    assertThat(
        rbSet(RB_ENUM_MAP_1_ITEMS.keySet()),
        rbSetEqualsMatcher(singletonRBSet(TestEnumXYZ.X)));
    assertThat(
        rbSet(RB_ENUM_MAP_2_ITEMS.keySet()),
        rbSetEqualsMatcher(rbSetOf(TestEnumXYZ.X, TestEnumXYZ.Y)));
    assertThat(
        rbSet(RB_ENUM_MAP_3_ITEMS.keySet()),
        rbSetEqualsMatcher(rbSetOf(TestEnumXYZ.X, TestEnumXYZ.Y, TestEnumXYZ.Z)));
  }

  @Test
  public void testEntrySet() {
    // Test RBEnumMap's entry set is the same as the raw enumMap.
    for (EnumMap enumMap : ENUM_MAPS) {
      assertThat(
          // Below we use rbSet wrappers so we can use an rbSet equality matcher.
          rbSet(newRBEnumMap(enumMap).entrySet()),
          rbSetEqualsMatcher(rbSet(enumMap.entrySet())));
    }
  }

  @Test
  public void testForEachEntry() {
    Consumer<RBEnumMap<TestEnumXYZ, String>> asserter = rbEnumMap -> {
      EnumMap<TestEnumXYZ, String> enumMapFromForEachEntry = new EnumMap<TestEnumXYZ, String>(TestEnumXYZ.class);
      rbEnumMap.forEachEntry((enumKey, value) -> enumMapFromForEachEntry.put(enumKey, value));
      assertThat(
          enumMapFromForEachEntry,
          enumMapEqualityMatcher(rbEnumMap.getCopyOfRawMap()));
    };
    for( EnumMap enumMap : ENUM_MAPS ) {
      asserter.accept(newRBEnumMap(enumMap));
    }
  }

  @Test
  public void testTestHashCode() {
    assertNotEquals(RB_ENUM_MAP_1_ITEMS.hashCode(), EMPTY_RB_ENUM_MAP.hashCode());
    assertEquals(   RB_ENUM_MAP_1_ITEMS.hashCode(), RB_ENUM_MAP_1_ITEMS.hashCode());
    assertNotEquals(RB_ENUM_MAP_1_ITEMS.hashCode(), RB_ENUM_MAP_2_ITEMS.hashCode());
    assertNotEquals(RB_ENUM_MAP_1_ITEMS.hashCode(), RB_ENUM_MAP_3_ITEMS.hashCode());
  }

  @Test
  public void testTestToString() {
    assertEquals("{}", EMPTY_RB_ENUM_MAP.toString());
    assertEquals("{X=String_X}", RB_ENUM_MAP_1_ITEMS.toString());
    assertEquals("{X=String_X, Y=String_Y}", RB_ENUM_MAP_2_ITEMS.toString());
    assertEquals("{X=String_X, Y=String_Y, Z=String_Z}", RB_ENUM_MAP_3_ITEMS.toString());
  }

}