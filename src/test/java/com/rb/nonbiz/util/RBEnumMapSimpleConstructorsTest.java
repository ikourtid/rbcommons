package com.rb.nonbiz.util;

import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.junit.Test;

import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.emptyRBEnumMap;
import static org.junit.Assert.assertEquals;

public class RBEnumMapSimpleConstructorsTest {

  @Test
  public void testEmptyRbEnumMap() {
    // Empty Constructor does not throw, and gives a zero-size map.
    assertEquals(0, emptyRBEnumMap(TestEnumXYZ.class).size());
  }
  
}