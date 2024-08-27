package com.rb.biz.marketdata;

import com.rb.biz.types.asset.InstrumentId;

import static com.rb.biz.types.Symbol.symbol;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.testutils.Asserters.valueExplained;

public class FakeInstruments {

  public static InstrumentIdGenerator instrumentIdGenerator = new InstrumentIdGenerator() {{
    this.encodedIdGenerator = new EncodedIdGenerator();
  }};

  /** Use this when you just need *some* instrument in a test, and don't plan to look at it otherwise */
  public static InstrumentId DUMMY_INSTRUMENT_ID     = valueExplained(instrumentId(11_605_345L), instrumentIdGenerator.generate(symbol("DUMMY")));
  public static InstrumentId DUMMY_ETF_INSTRUMENT_ID = valueExplained(instrumentId(11_605_345L), instrumentIdGenerator.generate(symbol("DUMMY")));

  public static InstrumentId STOCK_A = valueExplained(instrumentId(79_910_611_121L), instrumentIdGenerator.generate(symbol("STOCK_A")));
  public static InstrumentId STOCK_B = valueExplained(instrumentId(79_910_611_122L), instrumentIdGenerator.generate(symbol("STOCK_B")));
  public static InstrumentId STOCK_C = valueExplained(instrumentId(79_910_611_123L), instrumentIdGenerator.generate(symbol("STOCK_C")));
  public static InstrumentId STOCK_D = valueExplained(instrumentId(79_910_611_124L), instrumentIdGenerator.generate(symbol("STOCK_D")));
  public static InstrumentId STOCK_E = valueExplained(instrumentId(79_910_611_125L), instrumentIdGenerator.generate(symbol("STOCK_E")));
  public static InstrumentId STOCK_F = valueExplained(instrumentId(79_910_611_126L), instrumentIdGenerator.generate(symbol("STOCK_F")));
  public static InstrumentId STOCK_G = valueExplained(instrumentId(79_910_611_127L), instrumentIdGenerator.generate(symbol("STOCK_G")));
  public static InstrumentId STOCK_H = valueExplained(instrumentId(79_910_611_128L), instrumentIdGenerator.generate(symbol("STOCK_H")));
  public static InstrumentId STOCK_S = valueExplained(instrumentId(79_910_611_139L), instrumentIdGenerator.generate(symbol("STOCK_S")));

  /** Use these test symbols if your tests have some 2d aspect to them.
   * E.g. all the Ai stocks are related, all the Bi stocks are related, etc.
   */
  public static InstrumentId STOCK_A1 = valueExplained(instrumentId(3_196_424_444_868L), instrumentIdGenerator.generate(symbol("STOCK_A1")));
  public static InstrumentId STOCK_A2 = valueExplained(instrumentId(3_196_424_444_869L), instrumentIdGenerator.generate(symbol("STOCK_A2")));
  public static InstrumentId STOCK_A3 = valueExplained(instrumentId(3_196_424_444_870L), instrumentIdGenerator.generate(symbol("STOCK_A3")));
  public static InstrumentId STOCK_A4 = valueExplained(instrumentId(3_196_424_444_871L), instrumentIdGenerator.generate(symbol("STOCK_A4")));
  public static InstrumentId STOCK_A5 = valueExplained(instrumentId(3_196_424_444_872L), instrumentIdGenerator.generate(symbol("STOCK_A5")));
  public static InstrumentId STOCK_A6 = valueExplained(instrumentId(3_196_424_444_873L), instrumentIdGenerator.generate(symbol("STOCK_A6")));
  public static InstrumentId STOCK_A7 = valueExplained(instrumentId(3_196_424_444_874L), instrumentIdGenerator.generate(symbol("STOCK_A7")));
  public static InstrumentId STOCK_A8 = valueExplained(instrumentId(3_196_424_444_875L), instrumentIdGenerator.generate(symbol("STOCK_A8")));
  public static InstrumentId STOCK_A9 = valueExplained(instrumentId(3_196_424_444_876L), instrumentIdGenerator.generate(symbol("STOCK_A9")));

  public static InstrumentId STOCK_B1 = valueExplained(instrumentId(3_196_424_444_908L), instrumentIdGenerator.generate(symbol("STOCK_B1")));
  public static InstrumentId STOCK_B2 = valueExplained(instrumentId(3_196_424_444_909L), instrumentIdGenerator.generate(symbol("STOCK_B2")));
  public static InstrumentId STOCK_B3 = valueExplained(instrumentId(3_196_424_444_910L), instrumentIdGenerator.generate(symbol("STOCK_B3")));
  public static InstrumentId STOCK_B4 = valueExplained(instrumentId(3_196_424_444_911L), instrumentIdGenerator.generate(symbol("STOCK_B4")));
  public static InstrumentId STOCK_B5 = valueExplained(instrumentId(3_196_424_444_912L), instrumentIdGenerator.generate(symbol("STOCK_B5")));
  public static InstrumentId STOCK_B6 = valueExplained(instrumentId(3_196_424_444_913L), instrumentIdGenerator.generate(symbol("STOCK_B6")));
  public static InstrumentId STOCK_B7 = valueExplained(instrumentId(3_196_424_444_914L), instrumentIdGenerator.generate(symbol("STOCK_B7")));
  public static InstrumentId STOCK_B8 = valueExplained(instrumentId(3_196_424_444_915L), instrumentIdGenerator.generate(symbol("STOCK_B8")));

  public static InstrumentId STOCK_C1 = valueExplained(instrumentId(3_196_424_444_948L), instrumentIdGenerator.generate(symbol("STOCK_C1")));
  public static InstrumentId STOCK_C2 = valueExplained(instrumentId(3_196_424_444_949L), instrumentIdGenerator.generate(symbol("STOCK_C2")));
  public static InstrumentId STOCK_C3 = valueExplained(instrumentId(3_196_424_444_950L), instrumentIdGenerator.generate(symbol("STOCK_C3")));
  public static InstrumentId STOCK_C4 = valueExplained(instrumentId(3_196_424_444_951L), instrumentIdGenerator.generate(symbol("STOCK_C4")));
  public static InstrumentId STOCK_C5 = valueExplained(instrumentId(3_196_424_444_952L), instrumentIdGenerator.generate(symbol("STOCK_C5")));

  public static InstrumentId STOCK_D1 = valueExplained(instrumentId(3_196_424_444_988L), instrumentIdGenerator.generate(symbol("STOCK_D1")));
  public static InstrumentId STOCK_D2 = valueExplained(instrumentId(3_196_424_444_989L), instrumentIdGenerator.generate(symbol("STOCK_D2")));
  public static InstrumentId STOCK_D3 = valueExplained(instrumentId(3_196_424_444_990L), instrumentIdGenerator.generate(symbol("STOCK_D3")));
  public static InstrumentId STOCK_D4 = valueExplained(instrumentId(3_196_424_444_991L), instrumentIdGenerator.generate(symbol("STOCK_D4")));
  public static InstrumentId STOCK_D5 = valueExplained(instrumentId(3_196_424_444_992L), instrumentIdGenerator.generate(symbol("STOCK_D5")));

  public static InstrumentId STOCK_E1 = valueExplained(instrumentId(3_196_424_445_028L), instrumentIdGenerator.generate(symbol("STOCK_E1")));
  public static InstrumentId STOCK_E2 = valueExplained(instrumentId(3_196_424_445_029L), instrumentIdGenerator.generate(symbol("STOCK_E2")));
  public static InstrumentId STOCK_E3 = valueExplained(instrumentId(3_196_424_445_030L), instrumentIdGenerator.generate(symbol("STOCK_E3")));
  public static InstrumentId STOCK_E4 = valueExplained(instrumentId(3_196_424_445_031L), instrumentIdGenerator.generate(symbol("STOCK_E4")));
  public static InstrumentId STOCK_E5 = valueExplained(instrumentId(3_196_424_445_032L), instrumentIdGenerator.generate(symbol("STOCK_E5")));

  /** You can use these for stocks where you want to specify that their lots get long-term tax treatment (hence L) */
  public static InstrumentId STOCK_L1 = valueExplained(instrumentId(3_196_424_445_308L), instrumentIdGenerator.generate(symbol("STOCK_L1")));
  public static InstrumentId STOCK_L2 = valueExplained(instrumentId(3_196_424_445_309L), instrumentIdGenerator.generate(symbol("STOCK_L2")));
  public static InstrumentId STOCK_L3 = valueExplained(instrumentId(3_196_424_445_310L), instrumentIdGenerator.generate(symbol("STOCK_L3")));
  public static InstrumentId STOCK_L4 = valueExplained(instrumentId(3_196_424_445_311L), instrumentIdGenerator.generate(symbol("STOCK_L4")));
  public static InstrumentId STOCK_L5 = valueExplained(instrumentId(3_196_424_445_312L), instrumentIdGenerator.generate(symbol("STOCK_L5")));

  /**
   * You can use these for stocks where you want to specify that their lots get short-term tax treatment (hence S).
   * Additionally, if you want to make a distinction between stocks we are buying (or expecting to buy) and selling,
   * you can use STOCK_B* for buys and the STOCK_S* below for sells.
   */
  public static InstrumentId STOCK_S1 = valueExplained(instrumentId(3_196_424_445_588L), instrumentIdGenerator.generate(symbol("STOCK_S1")));
  public static InstrumentId STOCK_S2 = valueExplained(instrumentId(3_196_424_445_589L), instrumentIdGenerator.generate(symbol("STOCK_S2")));
  public static InstrumentId STOCK_S3 = valueExplained(instrumentId(3_196_424_445_590L), instrumentIdGenerator.generate(symbol("STOCK_S3")));
  public static InstrumentId STOCK_S4 = valueExplained(instrumentId(3_196_424_445_591L), instrumentIdGenerator.generate(symbol("STOCK_S4")));
  public static InstrumentId STOCK_S5 = valueExplained(instrumentId(3_196_424_445_592L), instrumentIdGenerator.generate(symbol("STOCK_S5")));

  public static InstrumentId ETF_1 = valueExplained(instrumentId(14_091_148L), instrumentIdGenerator.generate(symbol("ETF_1")));
  public static InstrumentId ETF_2 = valueExplained(instrumentId(14_091_149L), instrumentIdGenerator.generate(symbol("ETF_2")));
  public static InstrumentId ETF_3 = valueExplained(instrumentId(14_091_150L), instrumentIdGenerator.generate(symbol("ETF_3")));
  public static InstrumentId ETF_4 = valueExplained(instrumentId(14_091_151L), instrumentIdGenerator.generate(symbol("ETF_4")));
  public static InstrumentId ETF_5 = valueExplained(instrumentId(14_091_152L), instrumentIdGenerator.generate(symbol("ETF_5")));
  public static InstrumentId ETF_6 = valueExplained(instrumentId(14_091_153L), instrumentIdGenerator.generate(symbol("ETF_6")));

  public static InstrumentId MUTUAL_FUND_1 = valueExplained(instrumentId(843_148L), instrumentIdGenerator.generate(symbol("MF_1")));
  public static InstrumentId MUTUAL_FUND_2 = valueExplained(instrumentId(843_149L), instrumentIdGenerator.generate(symbol("MF_2")));
  public static InstrumentId MUTUAL_FUND_3 = valueExplained(instrumentId(843_150L), instrumentIdGenerator.generate(symbol("MF_3")));
  public static InstrumentId MUTUAL_FUND_4 = valueExplained(instrumentId(843_151L), instrumentIdGenerator.generate(symbol("MF_4")));

  public static InstrumentId STRUCTURED_PRODUCT_1 = valueExplained(instrumentId(1_243_148L), instrumentIdGenerator.generate(symbol("SP_1")));
  public static InstrumentId STRUCTURED_PRODUCT_2 = valueExplained(instrumentId(1_243_149L), instrumentIdGenerator.generate(symbol("SP_2")));
  public static InstrumentId STRUCTURED_PRODUCT_3 = valueExplained(instrumentId(1_243_150L), instrumentIdGenerator.generate(symbol("SP_3")));
  public static InstrumentId STRUCTURED_PRODUCT_4 = valueExplained(instrumentId(1_243_151L), instrumentIdGenerator.generate(symbol("SP_4")));
  public static InstrumentId STRUCTURED_PRODUCT_5 = valueExplained(instrumentId(1_243_152L), instrumentIdGenerator.generate(symbol("SP_5")));
  public static InstrumentId STRUCTURED_PRODUCT_6 = valueExplained(instrumentId(1_243_153L), instrumentIdGenerator.generate(symbol("SP_6")));
  public static InstrumentId STRUCTURED_PRODUCT_7 = valueExplained(instrumentId(1_243_154L), instrumentIdGenerator.generate(symbol("SP_7")));
  public static InstrumentId STRUCTURED_PRODUCT_8 = valueExplained(instrumentId(1_243_155L), instrumentIdGenerator.generate(symbol("SP_8")));

}
