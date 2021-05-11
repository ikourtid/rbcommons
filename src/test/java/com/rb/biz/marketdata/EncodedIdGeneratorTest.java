package com.rb.biz.marketdata;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.StringFunctions;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import org.junit.Test;

import java.io.File;

import static com.rb.biz.types.Symbol.symbol;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static org.junit.Assert.assertEquals;

public class EncodedIdGeneratorTest extends RBTest<EncodedIdGenerator> {

  @Test
  public void testSomeKnownInstruments() {
    EncodedIdGenerator generator = makeTestObject();
    assertEquals(18_381L, generator.generateLongId(symbol("KSU")));
    assertEquals(14_493L, generator.generateLongId(symbol("IBM")));
    assertEquals(285L, generator.generateLongId(symbol("GE")));
  }

  @Test
  public void generate_simple() {
    EncodedIdGenerator generator = makeTestObject();
    assertEquals(1L, generator.generateLongId(symbol("A")));
    assertEquals(2L, generator.generateLongId(symbol("B")));
    assertEquals(25L, generator.generateLongId(symbol("Y")));
    assertEquals(26L, generator.generateLongId(symbol("Z")));
    assertEquals(27L, generator.generateLongId(symbol("0")));
    assertEquals(28L, generator.generateLongId(symbol("1")));
    assertEquals(35L, generator.generateLongId(symbol("8")));
    assertEquals(36L, generator.generateLongId(symbol("9")));
    assertEquals(41L, generator.generateLongId(symbol("AA")));
  }

  @Test
  public void getBestGuessSymbol() {
    EncodedIdGenerator generator = makeTestObject();
    assertEquals(symbol("A"), generator.getBestGuessSymbol(instrumentId(1)));
    assertEquals(symbol("B"), generator.getBestGuessSymbol(instrumentId(2)));
    assertEquals(symbol("Y"), generator.getBestGuessSymbol(instrumentId(25)));
    assertEquals(symbol("Z"), generator.getBestGuessSymbol(instrumentId(26)));
    assertEquals(symbol("0"), generator.getBestGuessSymbol(instrumentId(27)));
    assertEquals(symbol("1"), generator.getBestGuessSymbol(instrumentId(28)));
    assertEquals(symbol("8"), generator.getBestGuessSymbol(instrumentId(35)));
    assertEquals(symbol("9"), generator.getBestGuessSymbol(instrumentId(36)));
    assertEquals(symbol("AA"), generator.getBestGuessSymbol(instrumentId(41)));
  }

  @Test
  public void roundTrip() {
    EncodedIdGenerator generator = makeTestObject();
    for (String sym : ImmutableList.of("A", "B", "AA", "AB", "Z", "ABC", "XYZ", "ABC0", "ABC9", "K9K")) {
      assertEquals(symbol(sym), generator.getBestGuessSymbol(instrumentId(generator.generateLongId(symbol(sym)))));
    }
  }

  @Test
  public void roundTrip_handlesLowercase() {
    EncodedIdGenerator generator = makeTestObject();
    for (String sym : ImmutableList.of("a", "b", "aa", "ab", "z", "abc", "xyz", "abc0", "abc9", "k9k")) {
      assertEquals(symbol(sym.toUpperCase()), generator.getBestGuessSymbol(instrumentId(generator.generateLongId(symbol(sym)))));
    }
  }

  @Test
  public void roundTrip_canHandleSpecialCharacters() {
    EncodedIdGenerator generator = makeTestObject();
    for (String sym : ImmutableList.of("JW.A", "BRK/A")) {
      assertEquals(symbol(sym), generator.getBestGuessSymbol(instrumentId(generator.generateLongId(symbol(sym)))));
    }
  }

  /**
   * This sort of is a test, in that we confirm that nothing breaks when we construct instrument IDs out of
   * real tickers - but we're mostly doing it to print a mini-report of the instrument IDs we should be using.
   */
  @Test
  public void generateEtfIds() {
    generateIdMappings(String.format("%s/ssd/market_data/closing_prices/etfs", System.getenv("HOME")));
  }

  @Test
  public void generateSP1500Ids() {
    generateIdMappings(String.format("%s/ssd/market_data/closing_prices/sp1500", System.getenv("HOME")));
  }

  private void generateIdMappings(String rootDir) {
    File folder = new File(rootDir);
    RBPreconditions.checkArgument(
        folder.exists(),
        "Directory %s does not exist",
        rootDir);
    EncodedIdGenerator generator = makeTestObject();
    for (File fileEntry : folder.listFiles()) {
      if (!fileEntry.isFile()) {
        continue;
      }
      String[] filenameComponents = fileEntry.getName().split("\\.");
      assertEquals(
          Strings.format("Bad filename %s ; should have 3 components separated by dots", fileEntry.getName()),
          3, filenameComponents.length);
      assertEquals("json", filenameComponents[2]);
      Symbol sym = symbol(filenameComponents[0]);
      InstrumentId instrumentId = instrumentId(generator.generateLongId(sym));
      System.out.println(Strings.format("%s %s", sym, StringFunctions.withUnderscores(instrumentId.asLong())));
    }
  }

  @Override
  protected EncodedIdGenerator makeTestObject() {
    return new EncodedIdGenerator();
  }
  
}