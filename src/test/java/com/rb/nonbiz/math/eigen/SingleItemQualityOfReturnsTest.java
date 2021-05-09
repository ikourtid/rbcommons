package com.rb.nonbiz.math.eigen;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.DUMMY_INSTRUMENT_ID;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.nonbiz.math.eigen.SingleItemQualityOfReturns.SingleItemQualityOfReturnsBuilder.singleItemQualityOfReturnsBuilder;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class SingleItemQualityOfReturnsTest extends RBTestMatcher<SingleItemQualityOfReturns<InstrumentId>> {

  public static <T extends Investable> SingleItemQualityOfReturns<T> onlyNumActualReturns(T key, int numActualReturns) {
    return singleItemQualityOfReturnsBuilder(key)
        .setNumActual(numActualReturns)
        .setNumGapFilled(0)
        .setNumBackFilled(0)
        .build();
  }

  @Test
  public void zeroNumActualIsAllowed_zeroTotalActualPlusGapFilledIsNot() {
    // In theory, a stock that's all halts would all show with gap fills
    SingleItemQualityOfReturns<InstrumentId> doesNotThrow = singleItemQualityOfReturnsBuilder(DUMMY_INSTRUMENT_ID)
        .setNumActual(0)
        .setNumGapFilled(1)
        .setNumBackFilled(0)
        .build();
    assertIllegalArgumentException( () -> singleItemQualityOfReturnsBuilder(DUMMY_INSTRUMENT_ID)
        .setNumActual(0)
        .setNumGapFilled(0)
        .setNumBackFilled(1)
        .build());
    assertIllegalArgumentException( () -> singleItemQualityOfReturnsBuilder(DUMMY_INSTRUMENT_ID)
        .setNumActual(0)
        .setNumGapFilled(0)
        .setNumBackFilled(0)
        .build());
  }

  @Override
  public SingleItemQualityOfReturns<InstrumentId> makeTrivialObject() {
    return singleItemQualityOfReturnsBuilder(STOCK_A)
        .setNumActual(1)
        .setNumGapFilled(0)
        .setNumBackFilled(0)
        .build();
  }

  @Override
  public SingleItemQualityOfReturns<InstrumentId> makeNontrivialObject() {
    return singleItemQualityOfReturnsBuilder(STOCK_A)
        .setNumActual(8)
        .setNumGapFilled(7)
        .setNumBackFilled(6)
        .build();
  }

  @Override
  public SingleItemQualityOfReturns<InstrumentId> makeMatchingNontrivialObject() {
    return singleItemQualityOfReturnsBuilder(STOCK_A)
        .setNumActual(8)
        .setNumGapFilled(7)
        .setNumBackFilled(6)
        .build();
  }

  @Override
  protected boolean willMatch(SingleItemQualityOfReturns<InstrumentId> expected, SingleItemQualityOfReturns<InstrumentId> actual) {
    return singleItemQualityOfReturnsMatcher(expected).matches(actual);
  }

  public static <T extends Investable> TypeSafeMatcher<SingleItemQualityOfReturns<T>> singleItemQualityOfReturnsMatcher(
      SingleItemQualityOfReturns<T> expected) {
    return makeMatcher(expected, actual ->
        expected.getKey().equals(actual.getKey())
        && expected.getNumActual() == actual.getNumActual()
        && expected.getNumGapFilled() == actual.getNumGapFilled()
        && expected.getNumBackFilled() == actual.getNumBackFilled());
  }

}
