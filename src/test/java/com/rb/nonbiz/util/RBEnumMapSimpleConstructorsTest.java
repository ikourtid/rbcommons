package com.rb.nonbiz.util;

import com.rb.nonbiz.collections.RBEnumMap;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.junit.Test;

import java.util.EnumMap;

import static com.rb.nonbiz.collections.RBEnumMap.newRBEnumMap;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbEnumMapMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.stringMatcher;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.emptyRBEnumMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBEnumMapSimpleConstructorsTest {

  @Test
  public void testEmptyRbEnumMap() {
    // Empty Constructor gives zero-size map.
    assertEquals(0, emptyRBEnumMap(TestEnumXYZ.class).size());

    // Constructor from empty map gives same as the explicit empty constructor.
    // The variable below cannot be inlined.
    RBEnumMap<TestEnumXYZ, String> emptyRBEnumMap = newRBEnumMap(new EnumMap<TestEnumXYZ, String>(TestEnumXYZ.class));
    assertThat(
        emptyRBEnumMap(TestEnumXYZ.class),
        rbEnumMapMatcher(
            emptyRBEnumMap,
            f -> stringMatcher(f)));
  }
}