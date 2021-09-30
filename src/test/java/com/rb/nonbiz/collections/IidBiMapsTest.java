package com.rb.nonbiz.collections;

import junit.framework.TestCase;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_E;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_F;
import static com.rb.nonbiz.collections.IidBiMaps.emptyIidBiMap;
import static com.rb.nonbiz.collections.IidBiMaps.iidBiMapOf;
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
    IidBiMap<String> iidBiMap2 = iidBiMapOf(
        STOCK_A, "AAA",
        STOCK_B, "BBB");
    IidBiMap<String> iidBiMap3 = iidBiMapOf(
        STOCK_A, "AAA",
        STOCK_B, "BBB",
        STOCK_C, "CCC");
    IidBiMap<String> iidBiMap4 = iidBiMapOf(
        STOCK_A, "AAA",
        STOCK_B, "BBB",
        STOCK_C, "CCC",
        STOCK_D, "DDD");
    IidBiMap<String> iidBiMap5 = iidBiMapOf(
        STOCK_A, "AAA",
        STOCK_B, "BBB",
        STOCK_C, "CCC",
        STOCK_D, "DDD",
        STOCK_E, "EEE");

    assertEquals(2, iidBiMap2.size());
    assertEquals(3, iidBiMap3.size());
    assertEquals(4, iidBiMap4.size());
    assertEquals(5, iidBiMap5.size());

    assertEquals("AAA",     iidBiMap2.getItemFromInstrumentId().getOrThrow(STOCK_A));
    assertEquals("AAA",     iidBiMap3.getItemFromInstrumentId().getOrThrow(STOCK_A));
    assertEquals("AAA",     iidBiMap4.getItemFromInstrumentId().getOrThrow(STOCK_A));
    assertEquals("AAA",     iidBiMap5.getItemFromInstrumentId().getOrThrow(STOCK_A));

    assertEquals("MISSING", iidBiMap2.getItemFromInstrumentId().getOrDefault(STOCK_F, "MISSING"));
    assertEquals("MISSING", iidBiMap3.getItemFromInstrumentId().getOrDefault(STOCK_F, "MISSING"));
    assertEquals("MISSING", iidBiMap4.getItemFromInstrumentId().getOrDefault(STOCK_F, "MISSING"));
    assertEquals("MISSING", iidBiMap5.getItemFromInstrumentId().getOrDefault(STOCK_F, "MISSING"));

    assertIllegalArgumentException( () -> iidBiMap2.getItemFromInstrumentId().getOrThrow(STOCK_F));
    assertIllegalArgumentException( () -> iidBiMap3.getItemFromInstrumentId().getOrThrow(STOCK_F));
    assertIllegalArgumentException( () -> iidBiMap4.getItemFromInstrumentId().getOrThrow(STOCK_F));
    assertIllegalArgumentException( () -> iidBiMap5.getItemFromInstrumentId().getOrThrow(STOCK_F));

    assertEquals(STOCK_A, iidBiMap2.getInstrumentIdFromItem().getOrThrow("AAA"));
    assertEquals(STOCK_A, iidBiMap3.getInstrumentIdFromItem().getOrThrow("AAA"));
    assertEquals(STOCK_A, iidBiMap4.getInstrumentIdFromItem().getOrThrow("AAA"));
    assertEquals(STOCK_A, iidBiMap5.getInstrumentIdFromItem().getOrThrow("AAA"));

    assertIllegalArgumentException( () -> iidBiMap2.getInstrumentIdFromItem().getOrThrow("ZZZ"));
    assertIllegalArgumentException( () -> iidBiMap3.getInstrumentIdFromItem().getOrThrow("ZZZ"));
    assertIllegalArgumentException( () -> iidBiMap4.getInstrumentIdFromItem().getOrThrow("ZZZ"));
    assertIllegalArgumentException( () -> iidBiMap5.getInstrumentIdFromItem().getOrThrow("ZZZ"));
  }

}
