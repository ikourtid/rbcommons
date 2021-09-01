package com.rb.nonbiz.util;

import com.google.common.collect.ImmutableMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testmatchers.RBValueMatchers;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.junit.Test;

import java.util.EnumMap;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.enumMapEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.enumMapMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.util.RBEnumMaps.transformEnumMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBEnumMapsTest {

  // A bit of a shorthand
  public static <E extends Enum<E>, V> EnumMap<E, V> newEnumMap(RBMap<E, V> rbMap) {
    return new EnumMap<E, V>(rbMap.asMap());
  }

  // It's weird that EnumMap does not allow for an empty map, but does allow for missing enums. Well,
  @Test
  public void enumMap_doesNotNeedAllKeys_butCannotBeEmpty() {
    assertEquals(
        "This test relies on the TestEnumXYZ enum having 3 values",
        3,
        TestEnumXYZ.values().length);
    assertIllegalArgumentException( () -> new EnumMap<TestEnumXYZ, String>(ImmutableMap.of()));
    EnumMap<TestEnumXYZ, String> doesNotThrow;
    doesNotThrow = newEnumMap(singletonRBMap(TestEnumXYZ.X, "_x"));
    doesNotThrow = newEnumMap(rbMapOf(
        TestEnumXYZ.X, "_x",
        TestEnumXYZ.Y, "_y"));
    doesNotThrow = newEnumMap(rbMapOf(
        TestEnumXYZ.X, "_x",
        TestEnumXYZ.Y, "_y",
        TestEnumXYZ.Z, "_z"));
  }

  @Test
  public void testTransformEnumMap() {
    EnumMap<TestEnumXYZ, Integer> original = newEnumMap(rbMapOf(
        TestEnumXYZ.X, 11,
        TestEnumXYZ.Y, 22));

    assertThat(
        transformEnumMap(original, v -> v + 0.07),
        enumMapMatcher(
            newEnumMap(rbMapOf(
                TestEnumXYZ.X, 11.07,
                TestEnumXYZ.Y, 22.07)),
            f -> doubleAlmostEqualsMatcher(f, 1e-8)));
    assertThat(
        transformEnumMap(original, (enumKey, value) -> enumKey + "_" + value),
        enumMapEqualityMatcher(
            newEnumMap(rbMapOf(
                TestEnumXYZ.X, "X_11",
                TestEnumXYZ.Y, "Y_22"))));
  }

}
