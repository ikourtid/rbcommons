package com.rb.nonbiz.collections;

import org.junit.Test;

import static com.rb.nonbiz.collections.MutableOptionalDoubleRawArray2D.mutableOptionalDoubleRawArray2D;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MutableOptionalDoubleRawArray2DTest {

  @Test
  public void everythingIsPresent() {
    MutableOptionalDoubleRawArray2D array2D = mutableOptionalDoubleRawArray2D(2, 3);
    assertEquals(2, array2D.getNumRows());
    assertEquals(3, array2D.getNumColumns());

    array2D.setOnlyOnce(0, 0, 100.0);
    array2D.setOnlyOnce(0, 1, 100.1);
    array2D.setOnlyOnce(0, 2, 100.2);

    array2D.setOnlyOnce(1, 0, 101.0);
    array2D.setOnlyOnce(1, 1, 101.1);
    array2D.setOnlyOnce(1, 2, 101.2);

    assertEquals(100.0, array2D.get(0, 0).getAsDouble(), 1e-8);
    assertEquals(100.1, array2D.get(0, 1).getAsDouble(), 1e-8);
    assertEquals(100.2, array2D.get(0, 2).getAsDouble(), 1e-8);

    assertEquals(101.0, array2D.get(1, 0).getAsDouble(), 1e-8);
    assertEquals(101.1, array2D.get(1, 1).getAsDouble(), 1e-8);
    assertEquals(101.2, array2D.get(1, 2).getAsDouble(), 1e-8);

    assertEquals(6, array2D.getNumValuesPresent());
    assertEquals(UNIT_FRACTION_1, array2D.getFractionPresent());
  }

  @Test
  public void nothingIsPresent() {
    MutableOptionalDoubleRawArray2D array2D = mutableOptionalDoubleRawArray2D(2, 3);
    assertFalse(array2D.get(0, 0).isPresent());
    assertFalse(array2D.get(0, 1).isPresent());
    assertFalse(array2D.get(0, 2).isPresent());
    assertFalse(array2D.get(1, 0).isPresent());
    assertFalse(array2D.get(1, 1).isPresent());
    assertFalse(array2D.get(1, 2).isPresent());
    assertEquals(0, array2D.getNumValuesPresent());
    assertEquals(UNIT_FRACTION_0, array2D.getFractionPresent());
  }

  @Test
  public void setsMoreThanOnce_useSetOnlyOnceMethod_throws() {
    MutableOptionalDoubleRawArray2D array2D = mutableOptionalDoubleRawArray2D(2, 3);
    array2D.setOnlyOnce(0, 0, 123.456);
    assertIllegalArgumentException( () -> array2D.setOnlyOnce(0, 0, 123.456));
  }

  @Test
  public void setsMoreThanOnce_usesPlainSetMethod_doesNotThrow() {
    MutableOptionalDoubleRawArray2D array2D = mutableOptionalDoubleRawArray2D(2, 3);
    array2D.set(0, 0, 123.456);
    array2D.set(0, 0, 123.456);
  }


}