package com.rb.nonbiz.collections;

import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.iidSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class IidSetSimpleConstructorsTest {

  @Test
  public void testNoDuplicates() {
    assertIllegalArgumentException( () -> iidSetOf(STOCK_A, STOCK_A));
    assertIllegalArgumentException( () -> iidSetOf(STOCK_A, STOCK_B, STOCK_A));
  }

}
