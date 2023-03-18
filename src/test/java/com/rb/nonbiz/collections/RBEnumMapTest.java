package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.TestEnumXYZ;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBEnumMap.rbEnumMap;
import static com.rb.nonbiz.collections.RBSet.rbSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.enumMapEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.enumMapOf;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonEnumMap;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBEnumMapTest extends TestCase {

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

  private final RBEnumMap<TestEnumXYZ, String> EMPTY_RB_ENUM_MAP   = rbEnumMap(EMPTY_ENUM_MAP);
  private final RBEnumMap<TestEnumXYZ, String> RB_ENUM_MAP_1_ITEMS = rbEnumMap(ENUM_MAP_1_ITEM);
  private final RBEnumMap<TestEnumXYZ, String> RB_ENUM_MAP_2_ITEMS = rbEnumMap(ENUM_MAP_2_ITEMS);
  private final RBEnumMap<TestEnumXYZ, String> RB_ENUM_MAP_3_ITEMS = rbEnumMap(ENUM_MAP_3_ITEMS);

  @Test
  public void testGetCopyOfRawMap() {

    // Test round trip conversion all maps.
    for (EnumMap enum_map : ENUM_MAPS) {
      assertThat(
          rbEnumMap(enum_map).getCopyOfRawMap(),
          enumMapEqualityMatcher(enum_map));
    }
  }

  @Test
  public void testSize() {
    assertEquals(0, rbEnumMap(EMPTY_ENUM_MAP).size());
    assertEquals(1, rbEnumMap(ENUM_MAP_1_ITEM).size());
    assertEquals(2, rbEnumMap(ENUM_MAP_2_ITEMS).size());
    assertEquals(3, rbEnumMap(ENUM_MAP_3_ITEMS).size());
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
    assertEquals( RB_ENUM_MAP_1_ITEMS.getOptional(TestEnumXYZ.X), Optional.of("String_X"));
    assertOptionalEmpty(RB_ENUM_MAP_1_ITEMS.getOptional(TestEnumXYZ.Y));
    assertOptionalEmpty(RB_ENUM_MAP_1_ITEMS.getOptional(TestEnumXYZ.Z));

    // Three item map has 3 values.
    assertEquals(RB_ENUM_MAP_3_ITEMS.getOptional(TestEnumXYZ.X), Optional.of("String_X"));
    assertEquals(RB_ENUM_MAP_3_ITEMS.getOptional(TestEnumXYZ.Y), Optional.of("String_Y"));
    assertEquals(RB_ENUM_MAP_3_ITEMS.getOptional(TestEnumXYZ.Z), Optional.of("String_Z"));
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
    fail("Chris to implement");
  }

  @Test
  public void testForEachEntry() {
   fail("Chris to implement");
  }

  @Test
  public void testTestEquals() {
    fail("Chris to implement");
  }

  @Test
  public void testTestHashCode() {
    fail("Chris to implement");
  }

  @Test
  public void testTestToString() {
    fail("Chris to implement");
  }

}