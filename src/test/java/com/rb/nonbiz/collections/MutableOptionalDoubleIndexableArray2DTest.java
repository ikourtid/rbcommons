package com.rb.nonbiz.collections;

import org.junit.Test;

import static com.rb.nonbiz.collections.MutableOptionalDoubleIndexableArray2D.mutableOptionalDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MutableOptionalDoubleIndexableArray2DTest {

  @Test
  public void everythingIsPresent() {
    MutableOptionalDoubleIndexableArray2D<String, String> array2D = mutableOptionalDoubleIndexableArray2D(
        simpleArrayIndexMapping("r0", "r1"),
        simpleArrayIndexMapping("c0", "c1", "c2"));
    assertEquals(2, array2D.getNumRows());
    assertEquals(3, array2D.getNumColumns());

    array2D.setOnlyOnce("r0", "c0", 100.0);
    array2D.setOnlyOnce("r0", "c1", 100.1);
    array2D.setOnlyOnce("r0", "c2", 100.2);

    array2D.setOnlyOnce("r1", "c0", 101.0);
    array2D.setOnlyOnce("r1", "c1", 101.1);
    array2D.setOnlyOnce("r1", "c2", 101.2);

    assertEquals(100.0, array2D.get("r0", "c0").getAsDouble(), 1e-8);
    assertEquals(100.1, array2D.get("r0", "c1").getAsDouble(), 1e-8);
    assertEquals(100.2, array2D.get("r0", "c2").getAsDouble(), 1e-8);

    assertEquals(101.0, array2D.get("r1", "c0").getAsDouble(), 1e-8);
    assertEquals(101.1, array2D.get("r1", "c1").getAsDouble(), 1e-8);
    assertEquals(101.2, array2D.get("r1", "c2").getAsDouble(), 1e-8);
  }

  @Test
  public void nothingIsPresent() {
    MutableOptionalDoubleIndexableArray2D<String, String> array2D = mutableOptionalDoubleIndexableArray2D(
        simpleArrayIndexMapping("r0", "r1"),
        simpleArrayIndexMapping("c0", "c1", "c2"));
    assertFalse(array2D.get("r0", "c0").isPresent());
    assertFalse(array2D.get("r0", "c1").isPresent());
    assertFalse(array2D.get("r0", "c2").isPresent());
    assertFalse(array2D.get("r1", "c0").isPresent());
    assertFalse(array2D.get("r1", "c1").isPresent());
    assertFalse(array2D.get("r1", "c2").isPresent());
  }

  @Test
  public void setsMoreThanOnce_useSetOnlyOnceMethod_throws() {
    MutableOptionalDoubleIndexableArray2D<String, String> array2D = mutableOptionalDoubleIndexableArray2D(
        simpleArrayIndexMapping("r0", "r1"),
        simpleArrayIndexMapping("c0", "c1", "c2"));
    array2D.setOnlyOnce("r0", "c0", 123.456);
    assertIllegalArgumentException( () -> array2D.setOnlyOnce("r0", "c0", 123.456));
  }

  @Test
  public void setsMoreThanOnce_usesPlainSetMethod_doesNotThrow() {
    MutableOptionalDoubleIndexableArray2D<String, String> array2D = mutableOptionalDoubleIndexableArray2D(
        simpleArrayIndexMapping("r0", "r1"),
        simpleArrayIndexMapping("c0", "c1", "c2"));
    array2D.set("r0", "c0", 123.456);
    array2D.set("r0", "c0", 123.456);
  }

  @Test
  public void getUsingUnknownRowKey_throws() {
    MutableOptionalDoubleIndexableArray2D<String, String> array2D = mutableOptionalDoubleIndexableArray2D(
        simpleArrayIndexMapping("r0", "r1"),
        simpleArrayIndexMapping("c0", "c1", "c2"));
    assertIllegalArgumentException( () -> array2D.get("c0", "c0"));
  }

  @Test
  public void getUsingUnknownColumnKey_throws() {
    MutableOptionalDoubleIndexableArray2D<String, String> array2D = mutableOptionalDoubleIndexableArray2D(
        simpleArrayIndexMapping("r0", "r1"),
        simpleArrayIndexMapping("c0", "c1", "c2"));
    assertIllegalArgumentException( () -> array2D.get("r0", "r0"));
  }

  @Test
  public void allValuesPresent_convertToUnmodifiableDoubleIndexableArray2D_doesNotThrow() {
    MutableOptionalDoubleIndexableArray2D<String, String> array2D = mutableOptionalDoubleIndexableArray2D(
        simpleArrayIndexMapping("r0"),
        simpleArrayIndexMapping("c0"));
    array2D.set("r0", "c0", 123.456);
    ImmutableDoubleIndexableArray2D<String, String> ignored = array2D.toUnmodifiableDoubleIndexedArray2D();
  }

  @Test
  public void someValuesMissing_convertToUnmodifiableDoubleIndexableArray2D_throws() {
    MutableOptionalDoubleIndexableArray2D<String, String> array2D = mutableOptionalDoubleIndexableArray2D(
        simpleArrayIndexMapping("r0"),
        simpleArrayIndexMapping("c0"));
    assertIllegalArgumentException( () -> array2D.toUnmodifiableDoubleIndexedArray2D());
  }

}