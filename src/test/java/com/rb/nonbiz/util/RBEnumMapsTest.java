package com.rb.nonbiz.util;

import com.google.common.collect.ImmutableMap;
import com.rb.nonbiz.collections.RBEnumMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.junit.Test;

import java.util.EnumMap;

import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbEnumMapEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbEnumMapMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.rbEnumMapOf;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonRBEnumMap;
import static com.rb.nonbiz.util.RBEnumMaps.rbEnumMapCoveringAllEnumValues;
import static com.rb.nonbiz.util.RBEnumMaps.transformRBEnumMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBEnumMapsTest {

  // It's weird that EnumMap does not allow for an empty map, but does allow for missing enums. Well, our test
  // will just expect those semantics, even if they're slightly unexpected.
  // However, the weirdness is justified if one looks at the empty EnumMap constructor: if there are no items in it,
  // it has no way of figuring out the Class object for the enum. This is not a constraint in our own RBEnumMap
  // and MutableRBEnumMap classes, because in the cases of empty data, we always specify the class object upon
  // construction.
  @Test
  public void enumMap_doesNotNeedAllKeys_butCannotBeEmpty() {
    assertEquals(
        "This test relies on the TestEnumXYZ enum having 3 values",
        3,
        TestEnumXYZ.values().length);
    assertIllegalArgumentException( () -> new EnumMap<TestEnumXYZ, String>(ImmutableMap.of()));
    RBEnumMap<TestEnumXYZ, String> doesNotThrow;
    doesNotThrow = singletonRBEnumMap(TestEnumXYZ.X, "_x");
    doesNotThrow = newRBEnumMap(TestEnumXYZ.class, rbMapOf(
        TestEnumXYZ.X, "_x",
        TestEnumXYZ.Y, "_y"));
    doesNotThrow = newRBEnumMap(TestEnumXYZ.class, rbMapOf(
        TestEnumXYZ.X, "_x",
        TestEnumXYZ.Y, "_y",
        TestEnumXYZ.Z, "_z"));
  }

  @Test
  public void testTransformEnumMap() {
    RBEnumMap<TestEnumXYZ, Integer> original = rbEnumMapOf(
        TestEnumXYZ.X, 11,
        TestEnumXYZ.Y, 22);

    assertThat(
        transformRBEnumMap(original, v -> v + 0.07),
        rbEnumMapMatcher(
            rbEnumMapOf(
                TestEnumXYZ.X, 11.07,
                TestEnumXYZ.Y, 22.07),
            f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8)));
    assertThat(
        transformRBEnumMap(original, (enumKey, value) -> enumKey + "_" + value),
        rbEnumMapEqualityMatcher(
            rbEnumMapOf(
                TestEnumXYZ.X, "X_11",
                TestEnumXYZ.Y, "Y_22")));
  }

  @Test
  public void testEnumMapCoveringAllEnumValues_fromMapOverload() {
    assertIllegalArgumentException( () -> rbEnumMapCoveringAllEnumValues(TestEnumXYZ.class, emptyRBMap()));
    assertIllegalArgumentException( () -> rbEnumMapCoveringAllEnumValues(TestEnumXYZ.class, singletonRBMap(
        TestEnumXYZ.X, DUMMY_STRING)));
    assertIllegalArgumentException( () -> rbEnumMapCoveringAllEnumValues(TestEnumXYZ.class, rbMapOf(
        TestEnumXYZ.X, DUMMY_STRING,
        TestEnumXYZ.Y, DUMMY_STRING)));

    RBMap<TestEnumXYZ, String> rbMap = rbMapOf(
        TestEnumXYZ.X, "_x",
        TestEnumXYZ.Y, "_y",
        TestEnumXYZ.Z, "_z");
    // The main assertion here is that the following does not throw
    RBEnumMap<TestEnumXYZ, String> enumMap = rbEnumMapCoveringAllEnumValues(TestEnumXYZ.class, rbMap);
  }

  @Test
  public void testEnumMapCoveringAllEnumValues_fromEnumConstantOverload() {
    assertThat(
        rbEnumMapCoveringAllEnumValues(TestEnumXYZ.class, v -> "_" + v.toString()),
        rbEnumMapMatcher(
            rbEnumMapOf(
                TestEnumXYZ.X, "_X",
                TestEnumXYZ.Y, "_Y",
                TestEnumXYZ.Z, "_Z"),
            f -> typeSafeEqualTo(f)));
  }

}
