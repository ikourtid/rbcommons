package com.rb.nonbiz.types;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.SingleInstrumentOrderedTaxLots;
import com.rb.biz.types.SingleInstrumentOrderedTaxLotsTest;
import com.rb.biz.types.TaxLot;
import com.rb.biz.types.asset.HasList;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.types.Price.price;
import static com.rb.biz.types.SingleInstrumentOrderedTaxLots.orderedLotsSingleton;
import static com.rb.biz.types.SingleInstrumentOrderedTaxLotsTest.singleInstrumentOrderedTaxLotsMatcher;
import static com.rb.biz.types.asset.HasListTest.hasListMatcher;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.AllRawVariablesInOrderBuilder.allRawVariablesInOrderBuilder;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.WeightedList.nonEmptyWeightedList;
import static com.rb.nonbiz.types.WeightedList.possiblyEmptyWeightedList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * This test class is not generic, but the publicly exposed typesafe matcher is.
 */
public class WeightedListTest extends RBTestMatcher<WeightedList<TaxLot, SingleInstrumentOrderedTaxLots>> {

  @Test
  public void emptyAllowed_doesNotThrowIfEmpty() {
    AllRawVariablesInOrder noVars = allRawVariablesInOrderBuilder().buildWithoutPreconditions();
    WeightedList<RawVariable, AllRawVariablesInOrder> doesNotThrow = possiblyEmptyWeightedList(noVars, emptyList());
  }

  @Test
  public void emptyDisallowed_throwsIfEmpty() {
    AllRawVariablesInOrder noVars = allRawVariablesInOrderBuilder().buildWithoutPreconditions();
    assertIllegalArgumentException( () -> nonEmptyWeightedList(noVars, emptyList()));
  }

  @Test
  public void moreWeightsThanItems_throws() {
    assertIllegalArgumentException( () -> nonEmptyWeightedList(
        orderedLotsSingleton(STOCK_A, positiveQuantity(11), price(20), LocalDate.of(1974, 4, 4)),
        ImmutableList.of(1.1, 2.2)));
  }

  @Test
  public void fewerWeightsThanItems_throws() {
    assertIllegalArgumentException( () -> nonEmptyWeightedList(
        orderedLotsSingleton(STOCK_A, positiveQuantity(11), price(20), LocalDate.of(1974, 4, 4)),
        emptyList()));
  }

  @Override
  public WeightedList<TaxLot, SingleInstrumentOrderedTaxLots> makeTrivialObject() {
    return nonEmptyWeightedList(
        orderedLotsSingleton(STOCK_A, positiveQuantity(11), price(20), LocalDate.of(1974, 4, 4)),
        singletonList(1.0));
  }

  @Override
  public WeightedList<TaxLot, SingleInstrumentOrderedTaxLots> makeNontrivialObject() {
    SingleInstrumentOrderedTaxLots lots = new SingleInstrumentOrderedTaxLotsTest().makeNontrivialObject();
    assertEquals(4, lots.size());
    return nonEmptyWeightedList(lots, ImmutableList.of(-1.1, 0.0, 3.3, -7.7));
  }

  @Override
  public WeightedList<TaxLot, SingleInstrumentOrderedTaxLots> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    SingleInstrumentOrderedTaxLots lots = new SingleInstrumentOrderedTaxLotsTest().makeMatchingNontrivialObject();
    assertEquals(4, lots.size());
    return nonEmptyWeightedList(lots, ImmutableList.of(-1.1 + e, 0.0 + e, 3.3 + e, -7.7 + e));
  }

  @Override
  protected boolean willMatch(WeightedList<TaxLot, SingleInstrumentOrderedTaxLots> expected,
                              WeightedList<TaxLot, SingleInstrumentOrderedTaxLots> actual) {
    return weightedListMatcher(expected, f -> singleInstrumentOrderedTaxLotsMatcher(f)).matches(actual);
  }

  /**
   * Use this in the general case where you want to treat the 'HasList' object as its interface,
   * and therefore use the general HasList matcher which just needs a matcher for the item.
   * Otherwise, use weightedListMatcher.
   */
  public static <T, L extends HasList<T>> TypeSafeMatcher<WeightedList<T, L>> weightedListGeneralMatcher(
      WeightedList<T, L> expected, MatcherGenerator<T> itemMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getWeights(), f -> doubleListMatcher(f, 1e-8)),
        match(v -> v.getHasList(), f -> hasListMatcher(f, itemMatcherGenerator)));
  }

  /**
   * Use this in the special case where you want to treat the 'HasList' object as its specific type
   * (i.e. not generally as just the HasList interface). This means you have to pass it a MatcherGenerator
   * for the actual class of the HasList instance you're dealing with here.
   */
  public static <T, L extends HasList<T>> TypeSafeMatcher<WeightedList<T, L>> weightedListMatcher(
      WeightedList<T, L> expected, MatcherGenerator<L> listObjectMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getWeights(), f -> doubleListMatcher(f, 1e-8)),
        match(v -> v.getHasList(), listObjectMatcherGenerator));
  }

}
