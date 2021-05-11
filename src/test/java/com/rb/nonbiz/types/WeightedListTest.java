package com.rb.nonbiz.types;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.HasList;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.WeightedListTest.TestHasStringList;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.types.Price.price;
import static com.rb.biz.types.asset.HasListTest.hasListMatcher;
import static com.rb.biz.types.trading.PositiveQuantity.positiveQuantity;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.WeightedList.nonEmptyWeightedList;
import static com.rb.nonbiz.types.WeightedList.possiblyEmptyWeightedList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This test class is not generic, but the publicly exposed typesafe matcher is.
 */
public class WeightedListTest extends RBTestMatcher<WeightedList<String, TestHasStringList>> {
  
  @Test
  public void emptyAllowed_doesNotThrowIfEmpty() {
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
  public WeightedList<String, TestHasStringList> makeTrivialObject() {
    return nonEmptyWeightedList(
        new TestHasStringList(singletonList("")),
        singletonList(1.0));
  }

  @Override
  public WeightedList<String, TestHasStringList> makeNontrivialObject() {
    return nonEmptyWeightedList(
        new TestHasStringList(ImmutableList.of("a", "b", "c", "d")),
        ImmutableList.of(-1.1, 0.0, 3.3, -7.7));
  }

  @Override
  public WeightedList<String, TestHasStringList> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return nonEmptyWeightedList(
        new TestHasStringList(ImmutableList.of("a", "b", "c", "d")),
        ImmutableList.of(-1.1 + e, 0.0 + e, 3.3 + e, -7.7 + e));
  }

  @Override
  protected boolean willMatch(WeightedList<String, TestHasStringList> expected,
                              WeightedList<String, TestHasStringList> actual) {
    return weightedListGeneralMatcher(expected, f -> typeSafeEqualTo(f)).matches(actual);
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
