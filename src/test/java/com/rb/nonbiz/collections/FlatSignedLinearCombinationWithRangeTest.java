package com.rb.biz.investing.strategy.optbased.fullopt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.biz.types.asset.AssetId;
import com.rb.nonbiz.collections.FlatSignedLinearCombination;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.biz.investing.strategy.optbased.fullopt.FlatSignedLinearCombinationWithRange.flatSignedLinearCombinationWithRange;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster.hardCodedInstrumentMaster;
import static com.rb.biz.types.asset.CashId.CASH;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.FlatSignedLinearCombination.flatSignedLinearCombination;
import static com.rb.nonbiz.collections.FlatSignedLinearCombination.singletonFlatSignedLinearCombination;
import static com.rb.nonbiz.collections.FlatSignedLinearCombinationTest.flatSignedLinearCombinationMatcher;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.bigDecimalRangeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.WeightedBySignedFraction.weightedBySignedFraction;
import static org.junit.Assert.assertEquals;

public class FlatSignedLinearCombinationWithRangeTest
    extends RBTestMatcher<FlatSignedLinearCombinationWithRange<AssetId>> {

  public static <T> FlatSignedLinearCombinationWithRange<T> testFlatSignedLinearCombinationWithRangeWithSeed(
      T item1, T item2, T item3, double seed) {
    return flatSignedLinearCombinationWithRange(
        flatSignedLinearCombination(
            item1, signedFraction(0.71 + seed),
            item2, signedFraction(0.72 + seed),
            item3, signedFraction(-0.73 + seed)),
        Range.atLeast(BigDecimal.valueOf(0.74 + seed)));
  }

  @Test
  public void emptyRange_or_openEndpoint_throws() {
    FlatSignedLinearCombination<String> flatSignedLinearCombination =
        flatSignedLinearCombination(
            "dummy1", signedFraction(1.2345),
            "dummy2", signedFraction(-0.9876));

    BigDecimal min = BigDecimal.valueOf(-1.1);
    BigDecimal max = BigDecimal.valueOf(3.3);

    rbSetOf(
        Range.singleton(min),
        Range.singleton(max),
        Range.atLeast(min),
        Range.atMost(max),
        Range.closed(min, max))
        .forEach(range -> {
          FlatSignedLinearCombinationWithRange<String> doesNotThrow =
              flatSignedLinearCombinationWithRange(flatSignedLinearCombination, range);
        });

    rbSetOf(
        Range.open(min, max),
        Range.openClosed(min, max),
        Range.closedOpen(min, max),
        Range.greaterThan(min),
        Range.lessThan(max),
        Range.<BigDecimal>all())
        .forEach(range -> assertIllegalArgumentException( () ->
            flatSignedLinearCombinationWithRange(flatSignedLinearCombination, range)));
  }

  @Test
  public void testPrintsInstruments() {
    assertEquals(
        "[FSLCWR range= [-5.6…7.8] ; expression = [FSLC 2 : -120.00 % * A (iid 1 ) + 340.00 % * B (iid 2 ) FSLC] FSLCWR]",
        FlatSignedLinearCombinationWithRange.toString(
            FlatSignedLinearCombinationWithRange.flatSignedLinearCombinationWithRange(
            flatSignedLinearCombination(ImmutableList.of(
                weightedBySignedFraction(instrumentId(1), signedFraction(-1.2)),
                weightedBySignedFraction(instrumentId(2), signedFraction(3.4)))),
            Range.closed(BigDecimal.valueOf(-5.6), BigDecimal.valueOf(7.8))),
            hardCodedInstrumentMaster(
                instrumentId(1), "A",
                instrumentId(2), "B"),
            UNUSED_DATE));
  }

  @Override
  public FlatSignedLinearCombinationWithRange<AssetId> makeTrivialObject() {
    return flatSignedLinearCombinationWithRange(
        singletonFlatSignedLinearCombination(CASH),
        Range.singleton(BigDecimal.ZERO));
  }

  @Override
  public FlatSignedLinearCombinationWithRange<AssetId> makeNontrivialObject() {
    return testFlatSignedLinearCombinationWithRangeWithSeed(CASH, STOCK_A, STOCK_B, ZERO_SEED);
  }

  @Override
  public FlatSignedLinearCombinationWithRange<AssetId> makeMatchingNontrivialObject() {
    return testFlatSignedLinearCombinationWithRangeWithSeed(CASH, STOCK_A, STOCK_B, EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(FlatSignedLinearCombinationWithRange<AssetId> expected,
                              FlatSignedLinearCombinationWithRange<AssetId> actual) {
    return flatSignedLinearCombinationWithRangeMatcher(expected, f -> typeSafeEqualTo(f)).matches(actual);
  }

  public static <T> TypeSafeMatcher<FlatSignedLinearCombinationWithRange<T>> flatSignedLinearCombinationWithRangeMatcher(
      FlatSignedLinearCombinationWithRange<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getFlatSignedLinearCombination(), f -> flatSignedLinearCombinationMatcher(f, matcherGenerator)),
        match(v -> v.getRange(),                       f -> bigDecimalRangeMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

}
