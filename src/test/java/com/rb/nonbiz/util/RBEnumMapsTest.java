package com.rb.nonbiz.util;

import com.google.common.collect.ImmutableMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.junit.Test;

import java.util.EnumMap;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.enumMapEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.enumMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.util.RBEnumMaps.enumMapCoveringAllEnumValues;
import static com.rb.nonbiz.util.RBEnumMaps.transformEnumMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBEnumMapsTest {

  // A bit of a shorthand
  public static <E extends Enum<E>, V> EnumMap<E, V> newEnumMap(RBMap<E, V> rbMap) {
    return new EnumMap<E, V>(rbMap.asMap());
  }

  // It's weird that EnumMap does not allow for an empty map, but does allow for missing enums. Well, our test
  // will just expect those semantics, even if they're slightly unexpected.
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

  @Test
  public void testEnumMapCoveringAllEnumValues_fromMapOverload() {
    assertIllegalArgumentException( () -> enumMapCoveringAllEnumValues(TestEnumXYZ.class, emptyRBMap()));
    assertIllegalArgumentException( () -> enumMapCoveringAllEnumValues(TestEnumXYZ.class, singletonRBMap(
        TestEnumXYZ.X, DUMMY_STRING)));
    assertIllegalArgumentException( () -> enumMapCoveringAllEnumValues(TestEnumXYZ.class, rbMapOf(
        TestEnumXYZ.X, DUMMY_STRING,
        TestEnumXYZ.Y, DUMMY_STRING)));

    RBMap<TestEnumXYZ, String> rbMap = rbMapOf(
        TestEnumXYZ.X, "_x",
        TestEnumXYZ.Y, "_y",
        TestEnumXYZ.Z, "_z");
    // The main assertion here is that the following does not throw
    EnumMap<TestEnumXYZ, String> enumMap = enumMapCoveringAllEnumValues(TestEnumXYZ.class, rbMap);
    assertThat(
        newRBMap(enumMap),
        rbMapMatcher(rbMap, f -> typeSafeEqualTo(f)));
  }

  @Test
  public void testEnumMapCoveringAllEnumValues_fromEnumConstantOverload() {
    assertThat(
        newRBMap(enumMapCoveringAllEnumValues(TestEnumXYZ.class, v -> "_" + v.toString())),
        rbMapMatcher(
            rbMapOf(
                TestEnumXYZ.X, "_X",
                TestEnumXYZ.Y, "_Y",
                TestEnumXYZ.Z, "_Z"),
            f -> typeSafeEqualTo(f)));
  }

}
