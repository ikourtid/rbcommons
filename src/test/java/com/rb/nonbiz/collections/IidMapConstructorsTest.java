package com.rb.nonbiz.collections;

import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapOfHasInstrumentId;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapTest.iidMapMatcher;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentId;
import static org.hamcrest.MatcherAssert.assertThat;

public class IidMapConstructorsTest {

  @Test
  public void testIidMapOfHasInstrumentId() {
    assertThat(
        iidMapOfHasInstrumentId(
            testHasInstrumentId(STOCK_A1, 1.1),
            testHasInstrumentId(STOCK_A2, 2.2)),
        iidMapMatcher(
            iidMapOf(
                STOCK_A1, testHasInstrumentId(STOCK_A1, 1.1),
                STOCK_A2, testHasInstrumentId(STOCK_A2, 2.2)),
            v -> TestHasInstrumentId.testHasInstrumentIdMatcher(v)));
  }

}
