package com.rb.biz.marketdata;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.StringFunctions;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import org.junit.Test;

import java.io.File;

import static com.rb.biz.types.Symbol.symbol;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static org.junit.Assert.assertEquals;

public class InstrumentIdGeneratorTest extends RBCommonsIntegrationTest<InstrumentIdGenerator> {

  @Test
  public void testSomeKnownInstruments() {
    InstrumentIdGenerator generator = makeRealObject();
    assertEquals(instrumentId(18_381), generator.generate(symbol("KSU")));
    assertEquals(instrumentId(14_493), generator.generate(symbol("IBM")));
    assertEquals(instrumentId(285), generator.generate(symbol("GE")));
  }

  @Test
  public void generate_simple() {
    InstrumentIdGenerator generator = makeRealObject();
    assertEquals(instrumentId(1), generator.generate(symbol("A")));
    assertEquals(instrumentId(2), generator.generate(symbol("B")));
    assertEquals(instrumentId(25), generator.generate(symbol("Y")));
    assertEquals(instrumentId(26), generator.generate(symbol("Z")));
    assertEquals(instrumentId(27), generator.generate(symbol("0")));
    assertEquals(instrumentId(28), generator.generate(symbol("1")));
    assertEquals(instrumentId(35), generator.generate(symbol("8")));
    assertEquals(instrumentId(36), generator.generate(symbol("9")));
    assertEquals(instrumentId(41), generator.generate(symbol("AA")));
  }

  @Test
  public void getBestGuessSymbol() {
    InstrumentIdGenerator generator = makeRealObject();
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
    InstrumentIdGenerator generator = makeRealObject();
    for (String sym : ImmutableList.of("A", "B", "AA", "AB", "Z", "ABC", "XYZ", "ABC0", "ABC9", "K9K")) {
      assertEquals(symbol(sym), generator.getBestGuessSymbol(generator.generate(symbol(sym))));
    }
  }

  @Test
  public void roundTrip_handlesLowercase() {
    InstrumentIdGenerator generator = makeRealObject();
    for (String sym : ImmutableList.of("a", "b", "aa", "ab", "z", "abc", "xyz", "abc0", "abc9", "k9k")) {
      assertEquals(symbol(sym.toUpperCase()), generator.getBestGuessSymbol(generator.generate(symbol(sym))));
    }
  }

  @Test
  public void roundTrip_canHandleSpecialCharacters() {
    InstrumentIdGenerator generator = makeRealObject();
    for (String sym : ImmutableList.of("JW.A", "BRK/A")) {
      assertEquals(symbol(sym), generator.getBestGuessSymbol(generator.generate(symbol(sym))));
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
    InstrumentIdGenerator generator = makeRealObject();
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
      InstrumentId instrumentId = generator.generate(sym);
      System.out.println(Strings.format("%s %s", sym, StringFunctions.withUnderscores(instrumentId.asLong())));
    }
  }

  @Override
  protected Class<InstrumentIdGenerator> getClassBeingTested() {
    return InstrumentIdGenerator.class;
  }

}