package com.rb.biz.jsonapi;

import com.rb.biz.types.Symbol;

/**
 * A {@code JsonTicker} is the representation of tickers in the JSON.
 *
 * <p> It very similar to a {@link Symbol}, except that a {@code Symbol} is more restrictive. </p>
 */
public class JsonTicker extends JsonStringKey {

  private JsonTicker(String freeFormTicker) {
    super(freeFormTicker);
  }

  public static JsonTicker jsonTicker(String freeFormTicker) {
    sanityCheckJsonStringKey(freeFormTicker);
    return new JsonTicker(freeFormTicker);
  }

}
