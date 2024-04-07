package com.rb.biz.types.asset;

import com.rb.biz.types.asset.InstrumentTypeMap.InstrumentTypeMapBuilder;
import com.rb.nonbiz.collections.IidSet;
import com.rb.nonbiz.collections.IidSetSimpleConstructors;
import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.functional.QuadriConsumer;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.rb.biz.marketdata.FakeInstruments.*;
import static com.rb.biz.types.asset.InstrumentType.EtfInstrumentType.etfInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.MutualFundInstrumentType.mutualFundInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.StockInstrumentType.stockInstrumentType;
import static com.rb.biz.types.asset.InstrumentType.StructuredProductInstrumentType.structuredProductInstrumentType;
import static com.rb.biz.types.asset.InstrumentTypeMap.checkInstrumentTypeMapHasNoDuplicateInstruments;
import static com.rb.biz.types.asset.InstrumentTypeMap.checkInstrumentTypeMapHasNoDuplicates;
import static com.rb.biz.types.asset.InstrumentTypeMap.instrumentTypeMapWithSharedDefaults;
import static com.rb.biz.types.asset.InstrumentTypeTest.instrumentTypeMatcher;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.singletonIidSet;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;

// This test class is not generic, but the publicly exposed static matcher is.
public class InstrumentTypeMapTest extends RBTestMatcher<InstrumentTypeMap<Double>> {

  @Test
  public void testGetInstrumentTypeWhenUnique() {
    InstrumentTypeMap<IidSet> map = InstrumentTypeMapBuilder.<IidSet>instrumentTypeMapBuilder()
        .setValueForEtfs(singletonIidSet(ETF_1))
        .setValueForStocks(singletonIidSet(STOCK_A1))
        .setValueForMutualFunds(singletonIidSet(MUTUAL_FUND_1))
        .setValueForStructuredProducts(singletonIidSet(STRUCTURED_PRODUCT_1))
        .build();
    assertThat(
        map.getInstrumentTypeWhenUnique(v -> v.contains(ETF_1)),
        instrumentTypeMatcher(etfInstrumentType()));
    assertThat(
        map.getInstrumentTypeWhenUnique(v -> v.contains(STOCK_A1)),
        instrumentTypeMatcher(stockInstrumentType()));
    assertThat(
        map.getInstrumentTypeWhenUnique(v -> v.contains(MUTUAL_FUND_1)),
        instrumentTypeMatcher(mutualFundInstrumentType()));
    assertThat(
        map.getInstrumentTypeWhenUnique(v -> v.contains(STRUCTURED_PRODUCT_1)),
        instrumentTypeMatcher(structuredProductInstrumentType()));
    // These throw because the predicate is true on all 3 InstrumentTypes, 2, and 0, respectively.
    assertIllegalArgumentException( () -> map.getInstrumentTypeWhenUnique(v -> true));
    assertIllegalArgumentException( () -> map.getInstrumentTypeWhenUnique(v -> v.contains(ETF_1) || v.contains(STOCK_A1)));
    assertIllegalArgumentException( () -> map.getInstrumentTypeWhenUnique(v -> v.contains(STOCK_B)));
  }

  @Test
  public void testCheckInstrumentTypeMapHasNoDuplicates() {
    QuadriConsumer<
            RBSet<Pair<InstrumentId, String>>,
            RBSet<Pair<InstrumentId, String>>,
            RBSet<Pair<InstrumentId, String>>,
            RBSet<Pair<InstrumentId, String>>> checker =
        (forEtfs, forStocks, forMutualFunds, forStructuredProducts) ->
            checkInstrumentTypeMapHasNoDuplicates(
            InstrumentTypeMapBuilder.<RBSet<Pair<InstrumentId, String>>>instrumentTypeMapBuilder()
                .setValueForEtfs(forEtfs)
                .setValueForStocks(forStocks)
                .setValueForMutualFunds(forMutualFunds)
                .setValueForStructuredProducts(forStructuredProducts)
                .build(),
            v -> newRBSet(v.stream().map(v1 -> v1.getLeft())));

    // does not throw
    checker.accept(
        rbSetOf(pair(STOCK_A1, DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_B1, DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_C1, DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_D1, DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING)));

    checker.accept(
        singletonRBSet(pair(STOCK_A1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_B1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_C1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_D1, DUMMY_STRING)));

    checker.accept(
        emptyRBSet(),
        emptyRBSet(),
        emptyRBSet(),
        emptyRBSet());

    // STOCK_E appears in two places below. The 2nd pair is immaterial
    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_C1, DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_D1, DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_B1, DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_D1, DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_B1, DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_C1, DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_A1, DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_D1, DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_A1, DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_C1, DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_A1, DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_B1, DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));
    
    // Same as above, but we removed the 2nd pair which was an attempt to keep the test general
    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_C1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_D1, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_B1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_D1, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_B1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_C1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_A1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_D1, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_A1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_C1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_A1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_B1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING))));
  }

  @Test
  public void testCheckInstrumentTypeMapHasNoDuplicateInstruments() {
    QuadriConsumer<
        RBSet<Pair<InstrumentId, String>>,
        RBSet<Pair<InstrumentId, String>>,
        RBSet<Pair<InstrumentId, String>>,
        RBSet<Pair<InstrumentId, String>>> checker =
        (forEtfs, forStocks, forMutualFunds, forStructuredProducts) ->
            checkInstrumentTypeMapHasNoDuplicateInstruments(
                InstrumentTypeMapBuilder.<RBSet<Pair<InstrumentId, String>>>instrumentTypeMapBuilder()
                    .setValueForEtfs(forEtfs)
                    .setValueForStocks(forStocks)
                    .setValueForMutualFunds(forMutualFunds)
                    .setValueForStructuredProducts(forStructuredProducts)
                    .build(),
                v -> newIidSet(v.stream()
                    .map(v1 -> v1.getLeft())
                    .collect(Collectors.toList())));

    // does not throw
    checker.accept(
        rbSetOf(pair(STOCK_A1, DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_B1, DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_C1, DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_D1, DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING)));

    checker.accept(
        singletonRBSet(pair(STOCK_A1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_B1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_C1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_D1, DUMMY_STRING)));

    checker.accept(
        emptyRBSet(),
        emptyRBSet(),
        emptyRBSet(),
        emptyRBSet());

    // STOCK_E appears in two places below. The 2nd pair is immaterial
    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_C1, DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_D1, DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_B1, DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_D1, DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_B1, DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_C1, DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_A1, DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_D1, DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_A1, DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_C1, DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        rbSetOf(pair(STOCK_A1, DUMMY_STRING), pair(STOCK_A2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_B1, DUMMY_STRING), pair(STOCK_B2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_C2, DUMMY_STRING)),
        rbSetOf(pair(STOCK_E,  DUMMY_STRING), pair(STOCK_D2, DUMMY_STRING))));

    // Same as above, but we removed the 2nd pair which was an attempt to keep the test general
    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_C1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_D1, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_B1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_D1, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_B1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_C1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_A1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_D1, DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_A1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_C1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING))));

    assertIllegalArgumentException( () -> checker.accept(
        singletonRBSet(pair(STOCK_A1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_B1, DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING)),
        singletonRBSet(pair(STOCK_E,  DUMMY_STRING))));
  }

  @Override
  public InstrumentTypeMap<Double> makeTrivialObject() {
    return instrumentTypeMapWithSharedDefaults(0.0);
  }

  @Override
  public InstrumentTypeMap<Double> makeNontrivialObject() {
    return InstrumentTypeMapBuilder.<Double>instrumentTypeMapBuilder()
        .setValueForEtfs(1.1)
        .setValueForStocks(3.3)
        .setValueForMutualFunds(-7.7)
        .setValueForStructuredProducts(15.15)
        .build();
  }

  @Override
  public InstrumentTypeMap<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return InstrumentTypeMapBuilder.<Double>instrumentTypeMapBuilder()
        .setValueForEtfs(1.1 + e)
        .setValueForStocks(3.3 + e)
        .setValueForMutualFunds(-7.7 + e)
        .setValueForStructuredProducts(15.15 + e)
        .build();
  }

  @Override
  protected boolean willMatch(InstrumentTypeMap<Double> expected, InstrumentTypeMap<Double> actual) {
    return instrumentTypeMapMatcher(expected, f -> doubleAlmostEqualsMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<InstrumentTypeMap<T>> instrumentTypeMapEqualityMatcher(
      InstrumentTypeMap<T> expected) {
    return instrumentTypeMapMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <T> TypeSafeMatcher<InstrumentTypeMap<T>> instrumentTypeMapMatcher(
      InstrumentTypeMap<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getValueForEtfs(),               matcherGenerator),
        match(v -> v.getValueForStocks(),             matcherGenerator),
        match(v -> v.getValueForMutualFunds(),        matcherGenerator),
        match(v -> v.getValueForStructuredProducts(), matcherGenerator));
  }

}
