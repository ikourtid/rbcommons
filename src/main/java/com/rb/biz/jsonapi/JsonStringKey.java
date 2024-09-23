package com.rb.biz.jsonapi;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.util.RBPreconditions;

import java.lang.Character.UnicodeBlock;
import java.util.List;

/**
 * A string representation inside JSON, which gets mapped to a numeric ID internally.
 *
 * <p> The most widely used subclass is {@link JsonTicker}, which internally gets mapped to an {@link InstrumentId}
 * (which is effectively a number), but this is a general concept that can be used e.g. for account IDs. </p>
 */
public abstract class JsonStringKey {

  private static final int MAX_JSON_STRING_KEY_LENGTH = 256;
  private static final List<UnicodeBlock> ALLOWED_UNICODE_BLOCKS = ImmutableList.of(
      UnicodeBlock.BASIC_LATIN,        // ASCII printable characters + control characters
      UnicodeBlock.LATIN_1_SUPPLEMENT, // most common roman characters with accents
      UnicodeBlock.LATIN_EXTENDED_A,   // less common roman characters with accents
      UnicodeBlock.CURRENCY_SYMBOLS);  // currency symbols

  private final String freeFormString;

  protected JsonStringKey(String freeFormString) {
    this.freeFormString = freeFormString;
  }

  protected static void sanityCheckJsonStringKey(String freeFormString) {
    RBPreconditions.checkArgument(
        !freeFormString.isEmpty(),
        "jsonStringKey cannot be empty");
    RBPreconditions.checkArgument(
        freeFormString.length() <= MAX_JSON_STRING_KEY_LENGTH,
        "jsonStringKey has length %s > max %s : %s",
        freeFormString.length(), MAX_JSON_STRING_KEY_LENGTH, freeFormString);
    RBPreconditions.checkArgument(
        allValidCharacters(freeFormString),
        "jsonStringKey %s contains invalid characters",
        freeFormString);
  }

  public String getFreeFormString() {
    return freeFormString;
  }

  public static boolean allValidCharacters(String freeFormString) {
    // check for valid characters
    for (int i = 0; i < freeFormString.length(); i++) {
      if (!isValidJsonStringKeyCharacter(freeFormString.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean isValidJsonStringKeyCharacter(char c) {
    return !Character.isISOControl(c)
        && ALLOWED_UNICODE_BLOCKS.contains(UnicodeBlock.of(c));
  }

  // equals / hashCode have to be implemented, because this will become a key in an IidBiMap.
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JsonStringKey that = (JsonStringKey) o;
    return freeFormString.equals(that.freeFormString);
  }

  @Override
  public int hashCode() {
    return freeFormString.hashCode();
  }

  @Override
  public String toString() {
    return freeFormString;
  }

}
