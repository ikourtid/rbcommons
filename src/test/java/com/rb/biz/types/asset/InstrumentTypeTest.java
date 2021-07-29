package com.rb.biz.types.asset;

import com.rb.biz.types.asset.InstrumentType.EtfInstrumentType;
import com.rb.biz.types.asset.InstrumentType.MutualFundInstrumentType;
import com.rb.biz.types.asset.InstrumentType.StockInstrumentType;
import com.rb.biz.types.asset.InstrumentType.StructuredProductInstrumentType;
import com.rb.biz.types.asset.InstrumentType.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.biz.types.asset.InstrumentType.EtfInstrumentType.etfInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.MutualFundInstrumentType.mutualFundInstrumentType;
import static com.rb.nonbiz.testmatchers.RBMatchers.alwaysMatchingMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;

public class InstrumentTypeTest extends RBTestMatcher<InstrumentType> {

  @Override
  public InstrumentType makeTrivialObject() {
    return etfInstrumentType();
  }

  @Override
  public InstrumentType makeNontrivialObject() {
    // This is no less 'trivial' than makeTrivialObject, but it has to be different.
    return mutualFundInstrumentType();
  }

  @Override
  public InstrumentType makeMatchingNontrivialObject() {
    // Nothing to tweak here vs. makeNontrivialObject()
    return mutualFundInstrumentType();
  }

  @Override
  protected boolean willMatch(InstrumentType expected, InstrumentType actual) {
    return instrumentTypeMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<EtfInstrumentType> etfInstrumentTypeMatcher(EtfInstrumentType etfInstrumentTypeIgnored) {
    return alwaysMatchingMatcher();
  }

  public static TypeSafeMatcher<StockInstrumentType> stockInstrumentTypeMatcher(
      StockInstrumentType stockInstrumentTypeIgnored) {
    return alwaysMatchingMatcher();
  }

  public static TypeSafeMatcher<MutualFundInstrumentType> mutualFundInstrumentTypeMatcher(
      MutualFundInstrumentType mutualFundInstrumentTypeIgnored) {
    return alwaysMatchingMatcher();
  }

  public static TypeSafeMatcher<StructuredProductInstrumentType> structuredProductInstrumentTypeMatcher(
      StructuredProductInstrumentType structuredProductInstrumentTypeIgnored) {
    return alwaysMatchingMatcher();
  }

  public static TypeSafeMatcher<InstrumentType> instrumentTypeMatcher(InstrumentType expected) {
    return generalVisitorMatcher(expected, v -> v.visit(new Visitor<VisitorMatchInfo<InstrumentType>>() {
      @Override
      public VisitorMatchInfo<InstrumentType> visitEtf(EtfInstrumentType etfInstrumentType) {
        return visitorMatchInfo(1, etfInstrumentType,
            (MatcherGenerator<EtfInstrumentType>) f -> etfInstrumentTypeMatcher(f));
      }

      @Override
      public VisitorMatchInfo<InstrumentType> visitStock(StockInstrumentType stockInstrumentType) {
        return visitorMatchInfo(2, stockInstrumentType,
            (MatcherGenerator<StockInstrumentType>) f -> stockInstrumentTypeMatcher(f));
      }

      @Override
      public VisitorMatchInfo<InstrumentType> visitMutualFund(MutualFundInstrumentType mutualFundInstrumentType) {
        return visitorMatchInfo(3, mutualFundInstrumentType,
            (MatcherGenerator<MutualFundInstrumentType>) f -> mutualFundInstrumentTypeMatcher(f));
      }
      
      @Override
      public VisitorMatchInfo<InstrumentType> visitStructuredProduct(StructuredProductInstrumentType structuredProductInstrumentType) {
        return visitorMatchInfo(4, structuredProductInstrumentType,
            (MatcherGenerator<StructuredProductInstrumentType>) f -> structuredProductInstrumentTypeMatcher(f));
      }
    }));
  }

}
