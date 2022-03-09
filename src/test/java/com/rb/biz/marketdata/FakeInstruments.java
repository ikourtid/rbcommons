package com.rb.biz.marketdata;

import com.rb.biz.types.asset.InstrumentId;

import static com.rb.biz.types.Symbol.symbol;

public class FakeInstruments {

  public static InstrumentIdGenerator instrumentIdGenerator = new InstrumentIdGenerator() {{
    this.encodedIdGenerator = new EncodedIdGenerator();
  }};

  /** Use this when you just need *some* instrument in a test, and don't plan to look at it otherwise */
  public static InstrumentId DUMMY_INSTRUMENT_ID     = instrumentIdGenerator.generate(symbol("DUMMY"));
  public static InstrumentId DUMMY_ETF_INSTRUMENT_ID = instrumentIdGenerator.generate(symbol("DUMMY"));

  public static InstrumentId STOCK_A = instrumentIdGenerator.generate(symbol("STOCK_A"));
  public static InstrumentId STOCK_B = instrumentIdGenerator.generate(symbol("STOCK_B"));
  public static InstrumentId STOCK_C = instrumentIdGenerator.generate(symbol("STOCK_C"));
  public static InstrumentId STOCK_D = instrumentIdGenerator.generate(symbol("STOCK_D"));
  public static InstrumentId STOCK_E = instrumentIdGenerator.generate(symbol("STOCK_E"));
  public static InstrumentId STOCK_F = instrumentIdGenerator.generate(symbol("STOCK_F"));
  public static InstrumentId STOCK_G = instrumentIdGenerator.generate(symbol("STOCK_G"));
  public static InstrumentId STOCK_H = instrumentIdGenerator.generate(symbol("STOCK_H"));
  public static InstrumentId STOCK_S = instrumentIdGenerator.generate(symbol("STOCK_S"));

  /** Use these test symbols if your tests have some 2d aspect to them.
   * E.g. all the Ai stocks are related, all the Bi stocks are related, etc.
   */
  public static InstrumentId STOCK_A1 = instrumentIdGenerator.generate(symbol("STOCK_A1"));
  public static InstrumentId STOCK_A2 = instrumentIdGenerator.generate(symbol("STOCK_A2"));
  public static InstrumentId STOCK_A3 = instrumentIdGenerator.generate(symbol("STOCK_A3"));
  public static InstrumentId STOCK_A4 = instrumentIdGenerator.generate(symbol("STOCK_A4"));
  public static InstrumentId STOCK_A5 = instrumentIdGenerator.generate(symbol("STOCK_A5"));
  public static InstrumentId STOCK_A6 = instrumentIdGenerator.generate(symbol("STOCK_A6"));
  public static InstrumentId STOCK_A7 = instrumentIdGenerator.generate(symbol("STOCK_A7"));
  public static InstrumentId STOCK_A8 = instrumentIdGenerator.generate(symbol("STOCK_A8"));
  public static InstrumentId STOCK_A9 = instrumentIdGenerator.generate(symbol("STOCK_A9"));

  public static InstrumentId STOCK_B1 = instrumentIdGenerator.generate(symbol("STOCK_B1"));
  public static InstrumentId STOCK_B2 = instrumentIdGenerator.generate(symbol("STOCK_B2"));
  public static InstrumentId STOCK_B3 = instrumentIdGenerator.generate(symbol("STOCK_B3"));
  public static InstrumentId STOCK_B4 = instrumentIdGenerator.generate(symbol("STOCK_B4"));
  public static InstrumentId STOCK_B5 = instrumentIdGenerator.generate(symbol("STOCK_B5"));
  public static InstrumentId STOCK_B6 = instrumentIdGenerator.generate(symbol("STOCK_B6"));
  public static InstrumentId STOCK_B7 = instrumentIdGenerator.generate(symbol("STOCK_B7"));
  public static InstrumentId STOCK_B8 = instrumentIdGenerator.generate(symbol("STOCK_B8"));

  public static InstrumentId STOCK_C1 = instrumentIdGenerator.generate(symbol("STOCK_C1"));
  public static InstrumentId STOCK_C2 = instrumentIdGenerator.generate(symbol("STOCK_C2"));
  public static InstrumentId STOCK_C3 = instrumentIdGenerator.generate(symbol("STOCK_C3"));
  public static InstrumentId STOCK_C4 = instrumentIdGenerator.generate(symbol("STOCK_C4"));
  public static InstrumentId STOCK_C5 = instrumentIdGenerator.generate(symbol("STOCK_C5"));

  public static InstrumentId STOCK_D1 = instrumentIdGenerator.generate(symbol("STOCK_D1"));
  public static InstrumentId STOCK_D2 = instrumentIdGenerator.generate(symbol("STOCK_D2"));
  public static InstrumentId STOCK_D3 = instrumentIdGenerator.generate(symbol("STOCK_D3"));
  public static InstrumentId STOCK_D4 = instrumentIdGenerator.generate(symbol("STOCK_D4"));
  public static InstrumentId STOCK_D5 = instrumentIdGenerator.generate(symbol("STOCK_D5"));

  public static InstrumentId STOCK_E1 = instrumentIdGenerator.generate(symbol("STOCK_E1"));
  public static InstrumentId STOCK_E2 = instrumentIdGenerator.generate(symbol("STOCK_E2"));
  public static InstrumentId STOCK_E3 = instrumentIdGenerator.generate(symbol("STOCK_E3"));
  public static InstrumentId STOCK_E4 = instrumentIdGenerator.generate(symbol("STOCK_E4"));
  public static InstrumentId STOCK_E5 = instrumentIdGenerator.generate(symbol("STOCK_E5"));

  /** You can use these for stocks where you want to specify that their lots get long-term tax treatment (hence L) */
  public static InstrumentId STOCK_L1 = instrumentIdGenerator.generate(symbol("STOCK_L1"));
  public static InstrumentId STOCK_L2 = instrumentIdGenerator.generate(symbol("STOCK_L2"));
  public static InstrumentId STOCK_L3 = instrumentIdGenerator.generate(symbol("STOCK_L3"));
  public static InstrumentId STOCK_L4 = instrumentIdGenerator.generate(symbol("STOCK_L4"));
  public static InstrumentId STOCK_L5 = instrumentIdGenerator.generate(symbol("STOCK_L5"));

  /** You can use these for stocks where you want to specify that their lots get short-term tax treatment (hence S) */
  public static InstrumentId STOCK_S1 = instrumentIdGenerator.generate(symbol("STOCK_S1"));
  public static InstrumentId STOCK_S2 = instrumentIdGenerator.generate(symbol("STOCK_S2"));
  public static InstrumentId STOCK_S3 = instrumentIdGenerator.generate(symbol("STOCK_S3"));
  public static InstrumentId STOCK_S4 = instrumentIdGenerator.generate(symbol("STOCK_S4"));
  public static InstrumentId STOCK_S5 = instrumentIdGenerator.generate(symbol("STOCK_S5"));

  public static InstrumentId ETF_1 = instrumentIdGenerator.generate(symbol("ETF_1"));
  public static InstrumentId ETF_2 = instrumentIdGenerator.generate(symbol("ETF_2"));
  public static InstrumentId ETF_3 = instrumentIdGenerator.generate(symbol("ETF_3"));
  public static InstrumentId ETF_4 = instrumentIdGenerator.generate(symbol("ETF_4"));
  public static InstrumentId ETF_5 = instrumentIdGenerator.generate(symbol("ETF_5"));
  public static InstrumentId ETF_6 = instrumentIdGenerator.generate(symbol("ETF_6"));

  public static InstrumentId MUTUAL_FUND_1 = instrumentIdGenerator.generate(symbol("MF_1"));
  public static InstrumentId MUTUAL_FUND_2 = instrumentIdGenerator.generate(symbol("MF_2"));
  public static InstrumentId MUTUAL_FUND_3 = instrumentIdGenerator.generate(symbol("MF_3"));
  public static InstrumentId MUTUAL_FUND_4 = instrumentIdGenerator.generate(symbol("MF_4"));

  public static InstrumentId STRUCTURED_PRODUCT_1 = instrumentIdGenerator.generate(symbol("SP_1"));
  public static InstrumentId STRUCTURED_PRODUCT_2 = instrumentIdGenerator.generate(symbol("SP_2"));
  public static InstrumentId STRUCTURED_PRODUCT_3 = instrumentIdGenerator.generate(symbol("SP_3"));
  public static InstrumentId STRUCTURED_PRODUCT_4 = instrumentIdGenerator.generate(symbol("SP_4"));
  public static InstrumentId STRUCTURED_PRODUCT_5 = instrumentIdGenerator.generate(symbol("SP_5"));
  public static InstrumentId STRUCTURED_PRODUCT_6 = instrumentIdGenerator.generate(symbol("SP_6"));
  public static InstrumentId STRUCTURED_PRODUCT_7 = instrumentIdGenerator.generate(symbol("SP_7"));
  public static InstrumentId STRUCTURED_PRODUCT_8 = instrumentIdGenerator.generate(symbol("SP_8"));

}
