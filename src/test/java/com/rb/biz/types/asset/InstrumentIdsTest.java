package com.rb.biz.types.asset;

import org.junit.Test;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.biz.types.asset.InstrumentIds.parseInstrumentId;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrows;
import static org.junit.Assert.assertEquals;

public class InstrumentIdsTest {

  @Test
  public void testParseInstrumentId() {
    assertEquals(instrumentId(1), parseInstrumentId("1"));
    assertEquals(instrumentId(123_456), parseInstrumentId("123456"));

    // underscores are OK for Java identifiers, but not in the string
    assertThrows(NumberFormatException.class, () -> parseInstrumentId("123_456"));
    assertThrows(NumberFormatException.class, () -> parseInstrumentId("abc"));
    assertThrows(NumberFormatException.class, () -> parseInstrumentId("abc123"));
    assertThrows(NumberFormatException.class, () -> parseInstrumentId("123abc"));
    assertThrows(NumberFormatException.class, () -> parseInstrumentId(null));
    assertThrows(NumberFormatException.class, () -> parseInstrumentId(""));

    // too long for a java Long
    assertThrows(NumberFormatException.class, () -> parseInstrumentId("111_222_333_444_555_666"));

    // These are just IllegalArgumentException, because this is a valid long, just not a valid InstrumentId.
    assertIllegalArgumentException( () -> parseInstrumentId("0"));
    assertIllegalArgumentException( () -> parseInstrumentId("-123"));
  }

}
