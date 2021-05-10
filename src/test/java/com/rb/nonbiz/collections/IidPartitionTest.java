package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster.hardCodedInstrumentMaster;
import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.collections.IidMapTest.iidMapPreciseValueMatcher;
import static com.rb.nonbiz.collections.IidPartition.iidPartition;
import static com.rb.nonbiz.collections.IidPartition.iidPartitionFromWeights;
import static com.rb.nonbiz.collections.IidPartition.singletonIidPartition;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DATE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_SYMBOL;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class IidPartitionTest extends RBTestMatcher<IidPartition> {

  private final InstrumentId IID_111 = instrumentId(111);
  private final InstrumentId IID_222 = instrumentId(222);
  private final InstrumentId IID_333 = instrumentId(333);

  private final InstrumentMaster TEST_INSTRUMENT_MASTER = hardCodedInstrumentMaster(
      IID_111, "S1",
      IID_222, "S2",
      IID_333, "S3");

  @Test
  public void happyPath() {
    IidPartition iidPartition = iidPartition(iidMapOf(
        IID_111, unitFraction(0.4),
        IID_222, unitFraction(0.6)));
    assertEquals(unitFraction(0.4), iidPartition.getFractionOrThrow(IID_111));
    assertEquals(unitFraction(0.6), iidPartition.getFractionOrThrow(IID_222));
  }

  @Test
  public void fractionsSumToLessThan1_throws() {
    assertIllegalArgumentException( () -> iidPartition(iidMapOf(
        IID_111, unitFraction(0.4),
        IID_222, unitFraction(0.59))));
  }

  @Test
  public void fractionsSumToMoreThan1_throws() {
    assertIllegalArgumentException( () -> iidPartition(iidMapOf(
        IID_111, unitFraction(0.4),
        IID_222, unitFraction(0.61))));
  }

  @Test
  public void fractionsDontHaveToAddToExactlyOneDueToFloatingPointPrecision() {
    IidPartition ignored1 = iidPartition(iidMapOf(
        IID_111, unitFraction(1 / 3.0),
        IID_222, unitFraction(1 / 3.0),
        IID_333, unitFraction(1 / 3.0)));
    IidPartition ignored2 = iidPartition(iidMapOf(
        IID_111, unitFraction(1, 3),
        IID_222, unitFraction(1, 3),
        IID_333, unitFraction(1, 3)));
  }

  @Test
  public void singleWholeFraction_doesNotThrow() {
    IidPartition ignored1 = iidPartition(singletonIidMap(IID_111, UNIT_FRACTION_1));
    IidPartition ignored2 = iidPartition(singletonIidMap(IID_111, unitFraction(1)));
    IidPartition ignored3 = iidPartition(singletonIidMap(IID_111, unitFraction(1, 1)));
  }

  @Test
  public void hasZeroFraction_throws() {
    assertIllegalArgumentException( () -> iidPartition(iidMapOf(
        IID_111, UNIT_FRACTION_0,
        IID_222, UNIT_FRACTION_1)));
  }

  @Test
  public void getsNoFractionsMap_throws() {
    assertIllegalArgumentException( () -> iidPartition(emptyIidMap()));
  }

  @Test
  public void getUnknownKey_throws() {
    IidPartition iidPartition = iidPartition(iidMapOf(
        IID_111, unitFraction(0.4),
        IID_222, unitFraction(0.6)));
    assertIllegalArgumentException( () -> iidPartition.getFractionOrThrow(IID_333));
  }

  @Test
  public void toString_default() {
    assertEquals(
        "100 % iid 111",
        iidPartition(singletonIidMap(
            IID_111, UNIT_FRACTION_1))
            .toString());
    assertEquals(
        "60 % iid 222 ; 40 % iid 111",
        iidPartition(iidMapOf(
            IID_111, unitFraction(0.4001),
            IID_222, unitFraction(0.5999)))
            .toString());
  }

  @Test
  public void toString_default_usingInstrumentMaster() {
    assertEquals(
        "100 % S1",
        iidPartition(singletonIidMap(
            IID_111, UNIT_FRACTION_1))
            .toString(TEST_INSTRUMENT_MASTER, DUMMY_DATE));
    assertEquals(
        "60 % S2 ; 40 % S1",
        iidPartition(iidMapOf(
            IID_111, unitFraction(0.4001),
            IID_222, unitFraction(0.5999)))
            .toString(TEST_INSTRUMENT_MASTER, DUMMY_DATE));
  }

  @Test
  public void toString_twoDigitPrecision() {
    assertEquals(
        "100 % iid 111",
        iidPartition(singletonIidMap(
            IID_111, UNIT_FRACTION_1))
            .toString(2));
    assertEquals(
        "59.99 % iid 222 ; 40.01 % iid 111",
        iidPartition(iidMapOf(
            IID_111, unitFraction(0.4001),
            IID_222, unitFraction(0.5999)))
            .toString(2));

    assertEquals(
        "59.99 % iid 222 ; 40.01 % iid 111",
        iidPartition(iidMapOf(
            IID_111, unitFraction(0.4001),
            IID_222, unitFraction(0.5999)))
            .toStringInDecreasingMembershipOrder(2));
    assertEquals(
        "40.01 % iid 111 ; 59.99 % iid 222",
        iidPartition(iidMapOf(
            IID_111, unitFraction(0.4001),
            IID_222, unitFraction(0.5999)))
            .toStringInIncreasingInstrumentIdOrder(2));
  }

  @Test
  public void toString_twoDigitPrecision_usingInstrumentMaster() {
    assertEquals(
        "100 % S1",
        iidPartition(singletonIidMap(
            IID_111, UNIT_FRACTION_1))
            .toStringInIncreasingInstrumentIdOrder(2, TEST_INSTRUMENT_MASTER, DUMMY_DATE));
    assertEquals(
        "100 % S1",
        iidPartition(singletonIidMap(
            IID_111, UNIT_FRACTION_1))
            .toStringInDecreasingMembershipOrder(2, TEST_INSTRUMENT_MASTER, DUMMY_DATE));

    assertEquals(
        "40.01 % S1 ; 59.99 % S2",
        iidPartition(iidMapOf(
            IID_111, unitFraction(0.4001),
            IID_222, unitFraction(0.5999)))
            .toStringInIncreasingInstrumentIdOrder(2, TEST_INSTRUMENT_MASTER, DUMMY_DATE));
    assertEquals(
        "59.99 % S2 ; 40.01 % S1",
        iidPartition(iidMapOf(
            IID_111, unitFraction(0.4001),
            IID_222, unitFraction(0.5999)))
            .toStringInDecreasingMembershipOrder(2, TEST_INSTRUMENT_MASTER, DUMMY_DATE));
  }

  @Test
  public void toString_usesMapping() {
    Function<InstrumentId, String> symbolMapper = iid ->
        iid.asLong() == 7 ? symbol("SPY").toString() :
            iid.asLong() == 8 ? symbol("BND").toString() :
                DUMMY_SYMBOL.toString();
    assertEquals(
        "59.99 % SPY ; 40.01 % BND",
        iidPartition(iidMapOf(
            instrumentId(7), unitFraction(0.5999),
            instrumentId(8), unitFraction(0.4001)))
            .toStringInDecreasingMembershipOrder(2, symbolMapper));
    assertEquals(
        "59.99 % SPY ; 40.01 % BND",
        iidPartition(iidMapOf(
            instrumentId(7), unitFraction(0.5999),
            instrumentId(8), unitFraction(0.4001)))
            .toStringInIncreasingInstrumentIdOrder(2, symbolMapper));
  }

  @Test
  public void iidPartitionFromWeights_hasNegativeWeightSomewhere_throws() {
    assertIllegalArgumentException( () -> iidPartitionFromWeights(iidMapOf(
        STOCK_A, signedQuantity(-100),
        STOCK_B, signedQuantity(400))));
  }

  @Test
  public void iidPartitionFromWeights_oneWeightIsZero_works() {
    assertThat(
        iidPartitionFromWeights(iidMapOf(
            STOCK_A, ZERO_MONEY,
            STOCK_B, money(1))),
        iidPartitionMatcher(
            singletonIidPartition(STOCK_B)));
  }

  @Test
  public void iidPartitionFromWeights_oneWeightIsNegative_throws() {
    assertIllegalArgumentException( () -> iidPartitionFromWeights(iidMapOf(
        STOCK_A, signedMoney(-1),
        STOCK_B, signedMoney(3))));
  }

  @Test
  public void iidPartitionFromWeights_allWeightsAreZero_throws() {
    assertIllegalArgumentException( () -> iidPartitionFromWeights(iidMapOf(
        STOCK_A, ZERO_MONEY,
        STOCK_B, ZERO_MONEY)));
  }

  @Override
  public IidPartition makeTrivialObject() {
    return singletonIidPartition(IID_111);
  }

  @Override
  public IidPartition makeNontrivialObject() {
    return iidPartition(iidMapOf(
        IID_111, unitFraction(0.4),
        IID_222, unitFraction(0.6)));
  }

  @Override
  public IidPartition makeMatchingNontrivialObject() {
    return iidPartition(iidMapOf(
        IID_222, unitFraction(0.60000000001),
        IID_111, unitFraction(0.39999999999)));
  }

  @Override
  protected boolean willMatch(IidPartition expected, IidPartition actual) {
    return iidPartitionMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<IidPartition> iidPartitionMatcher(IidPartition expected) {
    return epsilonIidPartitionMatcher(expected, 1e-8);
  }

  public static TypeSafeMatcher<IidPartition> epsilonIidPartitionMatcher(IidPartition expected, double epsilon) {
    // Here, we won't use the usual makeMatcher approach, because we want to be able to print
    // the instrumentIdPartition fraction at a high precision, whereas the default toString() only prints round percentages.
    return new TypeSafeMatcher<IidPartition>() {
      @Override
      protected boolean matchesSafely(IidPartition actual) {
        return iidMapPreciseValueMatcher(expected.getRawFractionsMap(), epsilon)
            .matches(actual.getRawFractionsMap());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected instrumentIdPartition: %s", expected.toString(8)));
      }
    };
  }

}
