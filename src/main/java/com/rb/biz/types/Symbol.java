package com.rb.biz.types;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * This is a thin wrapper around a regular String, but it gives us type safety
 * whenever we want to represent a ticker symbol for an instrument (stock or ETF).
 *
 * <p> The string has limitations on its length and content, but that covers every instance of security
 * symbol we have encountered. </p>
 *
 * <p> Stocks sometimes have ticker changes, where the symbol can change,
 * even though the underlying traded entity is fundamentally the same.
 * One such example was QQQ {@code ->} QQQQ {@code ->} QQQ. A {@link Symbol} / date combination is unique,
 * and an {@link InstrumentId} always has a single {@link Symbol} on a given day; ticker changes cannot happen
 * mid-day. </p>
 *
 * <p> Note that, in general, symbols are mostly used for human-facing outputs, like log files that describe
 * what happened, HTML pages that summarize trading activity, etc. It is <strong>very</strong> important that
 * {@link InstrumentId} are used whenever possible. </p>
 */
public class Symbol {

  // 40 ^ 11 can fit under a long (using our uuencoding for symbols), so we can have up to 11 chars,
  // but let's be safe; there should never be any symbols this long
  private static final int MAX_SYMBOL_LENGTH = 8;

  private final String tickerSymbol;

  private Symbol(String tickerSymbol) {
    this.tickerSymbol = tickerSymbol;
  }

  public static Symbol symbol(String tickerSymbol) {
    RBPreconditions.checkArgument(
        isValidSymbol(tickerSymbol),
        "%s is not a valid symbol",
        tickerSymbol);
    return new Symbol(tickerSymbol);
  }

  public static Symbol symbolAllowingInvalid(String tickerSymbol) {
    return new Symbol(tickerSymbol);
  }

  public static boolean isValidSymbol(String tickerSymbol) {
    if (tickerSymbol.isEmpty()) {
      return false;
    }
    if (tickerSymbol.length() > MAX_SYMBOL_LENGTH) {
      return false;
    }
    if (tickerSymbol.equals("$")) { // this is a hack to allow for cashSymbol() to exist
      return true;
    }
    // can only be characters, digits, symbols, dot, underscore, slash
    for (int i = 0; i < tickerSymbol.length(); i++) {
      if (!isValidSymbolCharacter(tickerSymbol.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static String getDisplaySymbol(Symbol symbol, InstrumentId instrumentId) {
    return Strings.format("%s ( %s )", symbol, instrumentId);
  }

  public static Symbol instrumentIdAsSymbol(InstrumentId instrumentId) {
    return symbolAllowingInvalid(Strings.format("iid %s", instrumentId.asLong()));
  }

  private static boolean isValidSymbolCharacter(char c) {
    // Don't just add stuff below; it will affect that uuencoding trick that EncodedIdGenerator uses.
    // Unfortunately this set of characters mapped to what our older datasource showed. Our new datasource (SEP,
    // as of Feb 2022) has a hyphen for some tickers, and also the caret for some stock indexes (e.g. ^DJI).
    // We *could* start supporting these in the EncodedIdGenerator, but it will change our instrument IDs,
    // which means that many tests will need to be upgraded, plus any definition of I_* that has a numeric ID
    // such as in KnownInstrumentIds.java. One thing we could do to support this is to map any new character to
    // behave e.g. like the slash character. This means that if we ever had X/Y and X^Y, we'd have a bug.
    // In practice, that's unlikely to happen. Anyway, let's wait until this ever becomes a problem. In the end,
    // when this gets used in production, the callers will probably have their own instrument IDs anyway.
    // Our EncodedIdGenerator is only used to create an instrument master for our internal testing and backtests.
    return Character.isAlphabetic(c)
        || Character.isDigit(c)
        || c == '.'
        || c == '/'
        || c == '_';
  }

  @Override
  public String toString() {
    return tickerSymbol;
  }

  // If you care about the ticker (e.g. "IBM") for any purposes other than displaying it to a human, use this method.
  // In general, #toString is not guaranteed to be anything other than human-readable.
  public String asString() {
    return tickerSymbol;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Symbol symbol = (Symbol) o;

    return tickerSymbol.equals(symbol.tickerSymbol);

  }

  @Override
  public int hashCode() {
    return tickerSymbol.hashCode();
  }

}
