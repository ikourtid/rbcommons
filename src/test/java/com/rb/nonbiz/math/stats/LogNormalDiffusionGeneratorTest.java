package com.rb.nonbiz.math.stats;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Money;
import com.rb.biz.types.Price;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;

import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.Price.price;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.math.stats.MutableNormalDistributionGenerator.mutableNormalDistributionGeneratorWithSeed;
import static com.rb.nonbiz.math.stats.NormalDistribution.NormalDistributionBuilder.normalDistributionBuilder;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.preciseValueListMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.RandomNumberGeneratorSeed.randomNumberGeneratorSeed;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

public class LogNormalDiffusionGeneratorTest extends RBCommonsIntegrationTest<LogNormalDiffusionGenerator> {

  @Test
  public void generalCase_generatesSimplePriceDiffusion_usingPrices() {
    BiConsumer<Integer, List<Price>> asserter = (seed, expectedPrices) ->
        assertThat(
            makeRealObject().generate(
                v -> price(v),
                price(100.0),
                makeMutableNormalDistributionGenerator(seed),
                5),
            preciseValueListMatcher(
                expectedPrices,
                DEFAULT_EPSILON_1e_8));

    asserter.accept(
        321,  // just a seed, so that this test is deterministic.
        ImmutableList.of(
            price(100),
            // Not a proof, but these look like reasonable stock price movements.
            price(100.30081904478621),
            price(100.54029563633472),
            price(101.45014466547623),
            price(101.1266545357682)));

    // The above may make it look like prices are always going up - even though the last price is a down move.
    // So let's just try a different RNG seed of 321 to look at a different result.
    asserter.accept(
        123,
        ImmutableList.of(
            price(100),
            // Not a proof, but these look like reasonable stock price movements.
            price(98.49395023822619),
            price(98.67608940729455),
            price(97.63735792789385),
            price(98.15142417408387)));
  }

  // Mostly same as previous test, but using Money instead of Price
  @Test
  public void generalCase_generatesSimplePriceDiffusion_usingMoney() {
    BiConsumer<Integer, List<Money>> asserter = (seed, expectedAmounts) ->
        assertThat(
            makeRealObject().generate(
                v -> money(v),
                money(100.0),
                makeMutableNormalDistributionGenerator(seed),
                5),
            preciseValueListMatcher(
                expectedAmounts,
                DEFAULT_EPSILON_1e_8));

    asserter.accept(
        321,  // just a seed, so that this test is deterministic.
        ImmutableList.of(
            money(100),
            money(100.30081904478621),
            money(100.54029563633472),
            money(101.45014466547623),
            money(101.1266545357682)));
  }

  @Test
  public void simplestCase_size1() {
    rbSetOf(-123, 0, 123, 321).forEach(seed ->
        assertThat(
            makeRealObject().generate(
                v -> price(v),
                price(100.0),
                makeMutableNormalDistributionGenerator(seed),
                1),
            preciseValueListMatcher(
                singletonList(price(100.0)),
                DEFAULT_EPSILON_1e_8)));
  }

  @Test
  public void zeroOrNegativeSize_throws() {
    rbSetOf(0, -1, -2).forEach(
        badSize -> assertIllegalArgumentException( () -> makeRealObject().generate(
            v -> price(v),
            DUMMY_PRICE,
            makeMutableNormalDistributionGenerator(DUMMY_POSITIVE_INTEGER), // dummy seed
            badSize)));
  }

  private MutableNormalDistributionGenerator makeMutableNormalDistributionGenerator(int seed) {
    return mutableNormalDistributionGeneratorWithSeed(
        normalDistributionBuilder()
            // 0 = flat
            .setMean(0)
            // 16% annualized volatility is fairly standard / reasonable.
            // We divide by sqrt(252) (approximately 16 - standard trick in the options world)
            // to get to a daily number.
            .setStandardDeviation(doubleExplained(0.01007905261, 0.16 / Math.sqrt(252)))
            .build(),
        randomNumberGeneratorSeed(seed));
  }

  @Override
  protected Class<LogNormalDiffusionGenerator> getClassBeingTested() {
    return LogNormalDiffusionGenerator.class;
  }

}
