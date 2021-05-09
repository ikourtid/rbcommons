package com.rb.nonbiz.math.eigen;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.math.eigen.MultiItemQualityOfReturns.multiItemQualityOfReturns;
import static com.rb.nonbiz.math.eigen.SingleItemQualityOfReturns.SingleItemQualityOfReturnsBuilder.singleItemQualityOfReturnsBuilder;
import static com.rb.nonbiz.math.eigen.SingleItemQualityOfReturnsTest.onlyNumActualReturns;
import static com.rb.nonbiz.math.eigen.SingleItemQualityOfReturnsTest.singleItemQualityOfReturnsMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_POSITIVE_INTEGER;

public class MultiItemQualityOfReturnsTest extends RBTestMatcher<MultiItemQualityOfReturns<InstrumentId>> {

  public static <T extends Investable> MultiItemQualityOfReturns<T> dummyMultiItemQualityOfReturns(T...items) {
    return multiItemQualityOfReturns(newRBSet(Arrays.asList(items))
        .toRBMap(item -> onlyNumActualReturns(item, DUMMY_POSITIVE_INTEGER)));
  }

  @Test
  public void isEmpty_throws() {
    assertIllegalArgumentException( () -> multiItemQualityOfReturns(emptyRBMap()));
  }

  @Test
  public void instrumentIdsDoNotMatch_throws() {
    assertIllegalArgumentException( () -> multiItemQualityOfReturns(singletonRBMap(
        STOCK_A, singleItemQualityOfReturnsBuilder(STOCK_B)
            .setNumActual(1)
            .setNumBackFilled(0)
            .setNumGapFilled(0)
            .build())));
  }

  @Override
  public MultiItemQualityOfReturns<InstrumentId> makeTrivialObject() {
    return multiItemQualityOfReturns(singletonRBMap(
        STOCK_A, singleItemQualityOfReturnsBuilder(STOCK_A)
            .setNumActual(1)
            .setNumBackFilled(0)
            .setNumGapFilled(0)
            .build()));
  }

  @Override
  public MultiItemQualityOfReturns<InstrumentId> makeNontrivialObject() {
    return multiItemQualityOfReturns(rbMapOf(
        STOCK_A, singleItemQualityOfReturnsBuilder(STOCK_A)
            .setNumActual(1)
            .setNumBackFilled(2)
            .setNumGapFilled(3)
            .build(),
        STOCK_B, singleItemQualityOfReturnsBuilder(STOCK_B)
            .setNumActual(4)
            .setNumBackFilled(5)
            .setNumGapFilled(6)
            .build()));
  }

  @Override
  public MultiItemQualityOfReturns<InstrumentId> makeMatchingNontrivialObject() {
    return multiItemQualityOfReturns(rbMapOf(
        STOCK_A, singleItemQualityOfReturnsBuilder(STOCK_A)
            .setNumActual(1)
            .setNumBackFilled(2)
            .setNumGapFilled(3)
            .build(),
        STOCK_B, singleItemQualityOfReturnsBuilder(STOCK_B)
            .setNumActual(4)
            .setNumBackFilled(5)
            .setNumGapFilled(6)
            .build()));
  }

  @Override
  protected boolean willMatch(MultiItemQualityOfReturns<InstrumentId> expected,
                              MultiItemQualityOfReturns<InstrumentId> actual) {
    return multiItemQualityOfReturnsMatcher(expected).matches(actual);
  }

  public static <T extends Investable> TypeSafeMatcher<MultiItemQualityOfReturns<T>> multiItemQualityOfReturnsMatcher(
      MultiItemQualityOfReturns<T> expected) {
    return makeMatcher(expected, actual ->
        rbMapMatcher(
            expected.getQualityOfReturnsMap(),
            siqor -> singleItemQualityOfReturnsMatcher(siqor))
            .matches(actual.getQualityOfReturnsMap()));
  }

}
