package com.rb.biz.marketdata;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.types.HasLongRepresentation;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.biz.types.Symbol.symbol;

/**
 * Generates numeric keys that make it easier for an {@link InstrumentId} to be mapped to a {@link Symbol},
 * and vice versa.
 *
 * <p> Using arbitrary numeric IDs for instruments makes it painful to retrieve the symbol from them, and a
 * lookup table must be used. It would be nice to have the numeric ID give you the symbol via some calculation.
 * This does some form of uuencoding to achieve that goal. </p>
 *
 * <p> Of course, even though the instrument ID stays constant, the ticker symbol may change. However, that doesn't
 * happen all that often.
 * Also, some tickers are recycled: stock ABC changes its name to DEF, and then later some other stock
 * is given the ticker ABC. But that is a new stock, so it should have a new instrument id. </p>
 *
 *
 * <p> So this is only useful to make it easier for us to go back from instrument ID to the ORIGINAL symbol
 * that gave rise to it. It's just a convenience so that when we look at an instrument ID, we can figure out
 * what symbol it is. Again, per the previous example, if DEF were to be alive, its instrument ID would translate to the
 * string 'ABC', so slightly confusing. But most large securities (which correlates with the securities we care about)
 * don't undergo ticker changes. So this is a useful enough lookup method. Furthermore, even getting the original,
 * pre-ticker-change symbol is more useful than just looking at an arbitrary numeric long key for an {@link InstrumentId}. </p>
 *
 * <p> One limitation of this uuencoding approach is that, in order to fit a reasonably-sized ticker, we have to
 * limit the number of valid characters. This only allows letters (capitals only), numbers, and three separator
 * characters: period (.), hyphen (-), and slash (/). </p>
 *
 * <p> Although the documentation above refers to {@link InstrumentId}, this code is more general and supports
 * {@link HasLongRepresentation}, which is more general than {@link InstrumentId}. </p>
 */
public class EncodedIdGenerator {

  private static final BiMap<Character, Integer> CODES = HashBiMap.create(ImmutableMap.<Character, Integer>builder()
      .put('A', 1)
      .put('B', 2)
      .put('C', 3)
      .put('D', 4)
      .put('E', 5)
      .put('F', 6)
      .put('G', 7)
      .put('H', 8)
      .put('I', 9)
      .put('J', 10)
      .put('K', 11)
      .put('L', 12)
      .put('M', 13)
      .put('N', 14)
      .put('O', 15)
      .put('P', 16)
      .put('Q', 17)
      .put('R', 18)
      .put('S', 19)
      .put('T', 20)
      .put('U', 21)
      .put('V', 22)
      .put('W', 23)
      .put('X', 24)
      .put('Y', 25)
      .put('Z', 26)
      .put('0', 27)
      .put('1', 28)
      .put('2', 29)
      .put('3', 30)
      .put('4', 31)
      .put('5', 32)
      .put('6', 33)
      .put('7', 34)
      .put('8', 35)
      .put('9', 36)
      .put('.', 37)
      .put('_', 38)
      .put('/', 39)
      .build());

  private static final int BASE = CODES.size() + 1;

  public long generateLongId(Symbol symbol) {
    long longId = 0;

    // In the US markets, we can't have 2 symbols that differ only in the capitalization of some of their letters
    String symbolString = symbol.toString().toUpperCase();
    for (int i = 0; i < symbolString.length(); i++) {
      longId *= BASE;
      longId += getCode(symbolString.charAt(i));
    }
    return longId;
  }

  public Symbol getBestGuessSymbol(HasLongRepresentation hasLongRepresentation) {
    long longId = hasLongRepresentation.asLong();
    StringBuilder sb = new StringBuilder();
    while (longId > 0) {
      sb.append(getChar((int) (longId % BASE)));
      longId = longId / BASE;
    }
    return symbol(sb.reverse().toString());
  }

  private int getCode(char c) {
    RBPreconditions.checkArgument(
        CODES.containsKey(c),
        "Unmappable character: '%s'",
        c);
    return CODES.get(c);
  }

  private char getChar(Integer code) {
    RBPreconditions.checkArgument(
        CODES.containsValue(code),
        "Unmappable code: %s",
        code);
    return CODES.inverse().get(code);
  }

}
