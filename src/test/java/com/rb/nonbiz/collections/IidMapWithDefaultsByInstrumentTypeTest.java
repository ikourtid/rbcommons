package com.rb.nonbiz.collections;

import com.rb.biz.types.Price;
import com.rb.biz.types.asset.InstrumentTypeMap.InstrumentTypeMapBuilder;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.ETF_1;
import static com.rb.biz.marketdata.FakeInstruments.MUTUAL_FUND_1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.biz.marketdata.FakeInstruments.STRUCTURED_PRODUCT_1;
import static com.rb.biz.types.Price.price;
import static com.rb.biz.types.asset.InstrumentType.EtfInstrumentType.etfInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.MutualFundInstrumentType.mutualFundInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.StockInstrumentType.stockInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.StructuredProductInstrumentType.structuredProductInstrumentType;
import static com.rb.biz.types.asset.InstrumentTypeMap.instrumentTypeMapWithSharedDefaults;
import static com.rb.biz.types.asset.InstrumentTypeMapTest.instrumentTypeMapMatcher;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapWithDefaultsByInstrumentType.emptyIidMapByInstrumentTypeWithSharedDefaults;
import static com.rb.nonbiz.collections.IidMapWithDefaultsByInstrumentType.iidMapWithDefaultsByInstrumentType;
import static com.rb.nonbiz.collections.IidMapWithDefaultsByInstrumentType.iidMapWithOnlyDefaultsByInstrumentType;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// This test class is not generic, but the publicly exposed static matcher is.
public class IidMapWithDefaultsByInstrumentTypeTest extends RBTestMatcher<IidMapWithDefaultsByInstrumentType<Double>> {

  public static <V> IidMapWithDefaultsByInstrumentType<V> iidMapWithSharedDefaultByInstrumentType(
      V sharedDefaultValueForAllInstrumentTypes, IidMap<V> rawIidMap) {
    return iidMapWithDefaultsByInstrumentType(
        instrumentTypeMapWithSharedDefaults(sharedDefaultValueForAllInstrumentTypes), rawIidMap);
  }

  @Test
  public void testDefaultsOnly() {
    IidMapWithDefaultsByInstrumentType<Price> minPriceByTypeMap = iidMapWithOnlyDefaultsByInstrumentType(
        InstrumentTypeMapBuilder.<Price>instrumentTypeMapBuilder()
            .setValueForStocks(            price(1.0))
            .setValueForEtfs(              price(2.0))
            .setValueForMutualFunds(       price(3.0))
            .setValueForStructuredProducts(price(4.0))
            .build());

    // note: getMapSize() only counts the instrument-specific entries, not the defaults
    assertEquals(0, minPriceByTypeMap.getMapSize());

    // none of the following instruments has a specific entry in 'minPriceByTypeMap', so they get the
    // instrument type-specific default
    assertEquals(price(1.0), minPriceByTypeMap.getOrDefault(STOCK_A,              stockInstrumentType()));
    assertEquals(price(2.0), minPriceByTypeMap.getOrDefault(ETF_1,                etfInstrumentType()));
    assertEquals(price(3.0), minPriceByTypeMap.getOrDefault(MUTUAL_FUND_1,        mutualFundInstrumentType()));
    assertEquals(price(4.0), minPriceByTypeMap.getOrDefault(STRUCTURED_PRODUCT_1, structuredProductInstrumentType()));
  }

  @Test
  public void testAllMatch() {
    IidMapWithDefaultsByInstrumentType<Price> priceMap =
        iidMapWithDefaultsByInstrumentType(
            InstrumentTypeMapBuilder.<Price>instrumentTypeMapBuilder()
                .setValueForStocks(            price(1.0))
                .setValueForEtfs(              price(2.0))
                .setValueForMutualFunds(       price(3.0))
                .setValueForStructuredProducts(price(4.0))
                .build(),
            iidMapOf(
                STOCK_A, price(5.0),
                ETF_1,   price(6.0)));

    // note: getMapSize() only counts the instrument-specific entries, not the defaults
    assertEquals(2, priceMap.getMapSize());

    assertFalse(priceMap.allMatch(price -> price.isLessThan(price(5.0))));
    assertTrue( priceMap.allMatch(price -> price.isLessThan(price(7.0))));

    assertTrue( priceMap.allMatch(price -> price.isExactlyRound()));
  }

  @Override
  public IidMapWithDefaultsByInstrumentType<Double> makeTrivialObject() {
    return emptyIidMapByInstrumentTypeWithSharedDefaults(0.0);
  }

  @Override
  public IidMapWithDefaultsByInstrumentType<Double> makeNontrivialObject() {
    return iidMapWithDefaultsByInstrumentType(
        InstrumentTypeMapBuilder.<Double>instrumentTypeMapBuilder()
            .setValueForEtfs(1.1)
            .setValueForStocks(-3.3)
            .setValueForMutualFunds(7.7)
            .setValueForStructuredProducts(15.15)
            .build(),
        iidMapOf(
            STOCK_A1, 31.31,
            STOCK_A2, 63.63));
  }

  @Override
  public IidMapWithDefaultsByInstrumentType<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return iidMapWithDefaultsByInstrumentType(
        InstrumentTypeMapBuilder.<Double>instrumentTypeMapBuilder()
            .setValueForEtfs(1.1 + e)
            .setValueForStocks(-3.3 + e)
            .setValueForMutualFunds(7.7 + e)
            .setValueForStructuredProducts(15.15 + e)
            .build(),
        iidMapOf(
            STOCK_A1, 31.31 + e,
            STOCK_A2, 63.63 + e));
  }

  @Override
  protected boolean willMatch(IidMapWithDefaultsByInstrumentType<Double> expected,
                              IidMapWithDefaultsByInstrumentType<Double> actual) {
    return iidMapWithDefaultsByInstrumentTypeMatcher(expected, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <V> TypeSafeMatcher<IidMapWithDefaultsByInstrumentType<V>> iidMapWithDefaultsByInstrumentTypeEqualityMatcher(
      IidMapWithDefaultsByInstrumentType<V> expected) {
    return iidMapWithDefaultsByInstrumentTypeMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <V> TypeSafeMatcher<IidMapWithDefaultsByInstrumentType<V>> iidMapWithDefaultsByInstrumentTypeMatcher(
      IidMapWithDefaultsByInstrumentType<V> expected, MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected,
        match(      v -> v.getDefaultValues(), f -> instrumentTypeMapMatcher(f, matcherGenerator)),
        matchIidMap(v -> v.getRawIidMap(),     matcherGenerator));
  }

}
