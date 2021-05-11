package com.rb.nonbiz.jsonapi;

import com.rb.biz.types.trading.PositiveQuantity;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.biz.jsonapi.JsonTicker.jsonTicker;
import static com.rb.biz.jsonapi.JsonTickerMapImplTest.jsonTickerMap;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.json.RBGson.jsonBigDecimal;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsWithMessage;
import static org.junit.Assert.assertEquals;

public class PreciseValueJsonApiConverterTest extends RBTest<PreciseValueJsonApiConverter> {

  @Test
  public void validTicker_returnsValue() {
    assertEquals(
        positiveQuantity(12.34),
        makeTestObject().fromJsonBigDecimal(
            jsonBigDecimal(BigDecimal.valueOf(12.34)),
            jsonTicker("A"),
            jsonTickerMap(singletonIidMap(instrumentId(1), "A")),
            v -> positiveQuantity(v)));
  }

  @Test
  public void invalidTicker_throwsSpecificException() {
    assertThrowsWithMessage(
        "Error converting unknown ticker A (value= 12.34 )",
        () -> makeTestObject().<PositiveQuantity>fromJsonBigDecimal(
            jsonBigDecimal(BigDecimal.valueOf(12.34)),
            jsonTicker("A"),
            jsonTickerMap(singletonIidMap(instrumentId(2), "B")),
            v -> positiveQuantity(v)));
  }

  @Test
  public void validTicker_invalidValue_throwsSpecificException() {
    assertThrowsWithMessage(
        "Error converting known ticker A (instrumentId 1 ): Attempt to construct a PositiveQuantity with -43.21 <= 0",
        () -> makeTestObject().<PositiveQuantity>fromJsonBigDecimal(
            jsonBigDecimal(BigDecimal.valueOf(-43.21)),
            jsonTicker("A"),
            jsonTickerMap(singletonIidMap(instrumentId(1), "A")),
            v -> positiveQuantity(v)));
  }

  @Override
  protected PreciseValueJsonApiConverter makeTestObject() {
    return new PreciseValueJsonApiConverter();
  }

}
