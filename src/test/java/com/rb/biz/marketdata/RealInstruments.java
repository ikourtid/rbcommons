package com.rb.biz.marketdata;

import com.rb.biz.types.asset.InstrumentId;

import static com.rb.biz.types.Symbol.symbol;

public class RealInstruments {

  public static InstrumentIdGenerator instrumentIdGenerator = new InstrumentIdGenerator() {{
    this.encodedIdGenerator = new EncodedIdGenerator();
  }};

  // These instruments are intentionally in A, B, C, D, E, F, G, H, I, J order,
  // but using real tickers makes this a bit more realistic.
  public static final InstrumentId I_AAPL = instrumentIdGenerator.generate(symbol("AAPL"));
  public static final InstrumentId I_BAC  = instrumentIdGenerator.generate(symbol("BAC"));
  public static final InstrumentId I_C    = instrumentIdGenerator.generate(symbol("C"));
  public static final InstrumentId I_DIS  = instrumentIdGenerator.generate(symbol("DIS"));
  public static final InstrumentId I_EBAY = instrumentIdGenerator.generate(symbol("EBAY"));

  public static final InstrumentId I_F    = instrumentIdGenerator.generate(symbol("F"));
  public static final InstrumentId I_GE   = instrumentIdGenerator.generate(symbol("GE"));
  public static final InstrumentId I_HD   = instrumentIdGenerator.generate(symbol("HD"));
  public static final InstrumentId I_IBM  = instrumentIdGenerator.generate(symbol("IBM"));
  public static final InstrumentId I_JPM  = instrumentIdGenerator.generate(symbol("JPM"));

}
