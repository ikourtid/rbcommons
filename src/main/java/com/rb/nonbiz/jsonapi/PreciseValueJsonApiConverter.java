package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.rb.biz.jsonapi.JsonTicker;
import com.rb.biz.jsonapi.JsonTickerMap;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PreciseValue;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

/**
 * Reads a PreciseValue from JSON for the case where it's the value of a JsonObject that's keyed by instrument
 * (JsonTicker, to be precise).
 * In case of an exception, we will throw a detailed new exception
 * that mentions the relevant instrument - not just e.g.
 * "Attempt to construct a PositiveQuantity with -93 &le; 0"
 */
public class PreciseValueJsonApiConverter {

  public <V extends PreciseValue<? super V>> V fromJsonBigDecimal(
      JsonElement valueAsJsonElementDouble,
      JsonTicker jsonTicker, JsonTickerMap jsonTickerMap, Function<BigDecimal, V> converter) {
    Optional<InstrumentId> instrumentId = jsonTickerMap.getOptionalInstrumentId(jsonTicker);
    BigDecimal value = valueAsJsonElementDouble.getAsBigDecimal();
    if (!instrumentId.isPresent()) {
      throw new IllegalArgumentException(Strings.format("Error converting unknown ticker %s (value= %s )",
              jsonTicker, value));
    }
    try {
      return converter.apply(value);
    } catch (Exception e) {
      throw new IllegalArgumentException(Strings.format(
          "Error converting known ticker %s (instrumentId %s ): %s",
              jsonTicker, instrumentId.get().asLong(), e.getMessage()));
    }
  }

}
