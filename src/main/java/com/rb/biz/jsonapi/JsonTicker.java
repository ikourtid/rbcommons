package com.rb.biz.jsonapi;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Symbol;
import com.rb.nonbiz.util.RBPreconditions;

import java.lang.Character.UnicodeBlock;
import java.util.List;

/**
 * A {@code JsonTicker} is the representation of tickers in the JSON.
 *
 * <p> It very similar to a {@link Symbol}, except that a {@code Symbol} is more restrictive. </p>
 */
public class JsonTicker {

  private static final int MAX_JSON_TICKER_LENGTH = 256;
  private static final List<UnicodeBlock> ALLOWED_UNICODE_BLOCKS = ImmutableList.of(
      UnicodeBlock.BASIC_LATIN,        // ASCII printable characters + control characters
      UnicodeBlock.LATIN_1_SUPPLEMENT, // most common roman characters with accents
      UnicodeBlock.LATIN_EXTENDED_A,   // less common roman characters with accents
      UnicodeBlock.CURRENCY_SYMBOLS);  // currency symbols

  private final String freeFormTicker;

  private JsonTicker(String freeFormTicker) {
    this.freeFormTicker = freeFormTicker;
  }

  public static JsonTicker jsonTicker(String freeFormTicker) {
    RBPreconditions.checkArgument(
        !freeFormTicker.isEmpty(),
        "jsonTicker cannot be empty");
    RBPreconditions.checkArgument(
        freeFormTicker.length() <= MAX_JSON_TICKER_LENGTH,
        "jsonTicker has length %s > max %s : %s",
        freeFormTicker.length(), MAX_JSON_TICKER_LENGTH, freeFormTicker);
    RBPreconditions.checkArgument(
        allValidCharacters(freeFormTicker),
        "JsonTicker %s contains invalid characters",
        freeFormTicker);

    return new JsonTicker(freeFormTicker);
  }

  public String getFreeFormTicker() {
    return freeFormTicker;
  }

  public static boolean allValidCharacters(String jsonTicker) {
    // check for valid characters
    for (int i = 0; i < jsonTicker.length(); i++) {
      if (!isValidJsonTickerCharacter(jsonTicker.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean isValidJsonTickerCharacter(char c) {
    return !Character.isISOControl(c)
        && ALLOWED_UNICODE_BLOCKS.contains(UnicodeBlock.of(c));
  }

  // equals / hashCode have to be implemented, because a JsonTicker will become a key in an IidBiMap.
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JsonTicker that = (JsonTicker) o;
    return freeFormTicker.equals(that.freeFormTicker);
  }

  @Override
  public int hashCode() {
    return freeFormTicker.hashCode();
  }

  @Override
  public String toString() {
    return freeFormTicker;
  }

}
