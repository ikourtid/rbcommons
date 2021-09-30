package com.rb.nonbiz.collections;

import junit.framework.TestCase;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_E;
import static com.rb.nonbiz.collections.IidBiMaps.emptyIidBiMap;
import static com.rb.nonbiz.collections.IidBiMaps.singletonIidBiMap;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class IidBiMapsTest extends TestCase {

  @Test
  public void testEmptyBiMap() {
    IidBiMap<String> emptyIidBiMap = emptyIidBiMap();
    assertEquals(0, emptyIidBiMap.size());
    assertTrue(emptyIidBiMap.isEmpty());
  }

  @Test
  public void testSingletonIidBiMap() {
    IidBiMap<String> singletonIidBiMap = singletonIidBiMap(STOCK_A, "ABC");

    assertEquals(1, singletonIidBiMap.size());
    assertFalse(singletonIidBiMap.isEmpty());
    assertEquals("ABC",     singletonIidBiMap.getItemFromInstrumentId().getOrThrow(STOCK_A));
    assertEquals("MISSING", singletonIidBiMap.getItemFromInstrumentId().getOrDefault(STOCK_E, "MISSING"));

    assertIllegalArgumentException( () -> singletonIidBiMap.getItemFromInstrumentId().getOrThrow(STOCK_E));
  }

  @Test
  public void testOtherConstructors() {
    IidBiMap<String> iidBiMap = iidBiMapOf(
        STOCK_A, "ABC",
        STOCK_B, "DEF");

    assertEquals(1, singletonIidBiMap.size());
    assertFalse(singletonIidBiMap.isEmpty());
    assertEquals("ABC",     singletonIidBiMap.getItemFromInstrumentId().getOrThrow(STOCK_A));
    assertEquals("MISSING", singletonIidBiMap.getItemFromInstrumentId().getOrDefault(STOCK_E, "MISSING"));

    assertIllegalArgumentException( () -> singletonIidBiMap.getItemFromInstrumentId().getOrThrow(STOCK_E));
  }

}