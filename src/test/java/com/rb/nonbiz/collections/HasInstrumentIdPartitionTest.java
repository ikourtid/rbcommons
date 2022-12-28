package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.HasInstrumentId;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_E;
import static com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster.hardCodedInstrumentMaster;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.HasInstrumentIdMapTest.hasInstrumentIdMapMatcher;
import static com.rb.nonbiz.collections.HasInstrumentIdMaps.emptyHasInstrumentIdMap;
import static com.rb.nonbiz.collections.HasInstrumentIdMaps.hasInstrumentIdMapOf;
import static com.rb.nonbiz.collections.HasInstrumentIdMaps.singletonHasInstrumentIdMap;
import static com.rb.nonbiz.collections.HasInstrumentIdPartition.hasInstrumentIdPartition;
import static com.rb.nonbiz.collections.HasInstrumentIdPartition.hasInstrumentIdPartitionFromWeights;
import static com.rb.nonbiz.collections.HasInstrumentIdPartition.singletonHasInstrumentIdPartition;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.PartitionTest.partitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentId;
import static com.rb.nonbiz.collections.TestHasInstrumentId.testHasInstrumentIdMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DATE;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HasInstrumentIdPartitionTest extends RBTestMatcher<HasInstrumentIdPartition<TestHasInstrumentId>> {

  private final TestHasInstrumentId OBJ_111 = testHasInstrumentId(instrumentId(111), 1.1);
  private final TestHasInstrumentId OBJ_444 = testHasInstrumentId(instrumentId(444), 4.4);
  private final TestHasInstrumentId OBJ_555 = testHasInstrumentId(instrumentId(555), 5.5);

  private final InstrumentMaster TEST_INSTRUMENT_MASTER = hardCodedInstrumentMaster(
      OBJ_111.getInstrumentId(), "S1",
      OBJ_444.getInstrumentId(), "S4",
      OBJ_555.getInstrumentId(), "S5");

  private final HasInstrumentIdPartition<TestHasInstrumentId> TEST_PARTITION =
      hasInstrumentIdPartition(hasInstrumentIdMapOf(
          OBJ_111, unitFraction(0.1),
          OBJ_444, unitFraction(0.4),
          OBJ_555, unitFraction(0.5)));

  @Test
  public void testGetFractionOrThrow() {
    assertEquals(unitFraction(0.1), TEST_PARTITION.getFractionOrThrow(instrumentId(111)));
    assertEquals(unitFraction(0.4), TEST_PARTITION.getFractionOrThrow(instrumentId(444)));
    assertEquals(unitFraction(0.5), TEST_PARTITION.getFractionOrThrow(instrumentId(555)));
    assertIllegalArgumentException( () -> TEST_PARTITION.getFractionOrThrow(instrumentId(987)));
  }

  @Test
  public void testInstrumentIdIterator() {
    // Using sets because ordering is not guaranteed with instrumentIdIterator.
    assertEquals(
        newRBSet(TEST_PARTITION.instrumentIdIterator()),
        rbSetOf(instrumentId(111), instrumentId(444), instrumentId(555)));
  }

  @Test
  public void testForEachEntry() {
    MutableRBSet<String> set = newMutableRBSet();
    TEST_PARTITION.forEachEntry( (testHasInstrumentId, unitFraction) -> set.add(Strings.format("%s|%s",
        testHasInstrumentId.toString(), unitFraction.toPercentString(1))));
    assertThat(
        newRBSet(set),
        rbSetEqualsMatcher(rbSetOf(
            "iid 111 1.1|10 %",
            "iid 444 4.4|40 %",
            "iid 555 5.5|50 %")));
  }

  @Test
  public void fractionsSumToLessThan1_throws() {
    assertIllegalArgumentException( () -> hasInstrumentIdPartition(hasInstrumentIdMapOf(
        OBJ_111, unitFraction(0.4),
        OBJ_444, unitFraction(0.59))));
  }

  @Test
  public void fractionsSumToMoreThan1_throws() {
    assertIllegalArgumentException( () -> hasInstrumentIdPartition(hasInstrumentIdMapOf(
        OBJ_111, unitFraction(0.4),
        OBJ_444, unitFraction(0.61))));
  }

  @Test
  public void fractionsDontHaveToAddToExactlyOneDueToFloatingPointPrecision() {
    HasInstrumentIdPartition<TestHasInstrumentId> ignored1 = hasInstrumentIdPartition(hasInstrumentIdMapOf(
        OBJ_111, unitFraction(1 / 3.0),
        OBJ_444, unitFraction(1 / 3.0),
        OBJ_555, unitFraction(1 / 3.0)));
    HasInstrumentIdPartition<TestHasInstrumentId> ignored2 = hasInstrumentIdPartition(hasInstrumentIdMapOf(
        OBJ_111, unitFraction(1, 3),
        OBJ_444, unitFraction(1, 3),
        OBJ_555, unitFraction(1, 3)));
  }

  @Test
  public void singleWholeFraction_doesNotThrow() {
    HasInstrumentIdPartition<TestHasInstrumentId> ignored1 = hasInstrumentIdPartition(singletonHasInstrumentIdMap(OBJ_111, UNIT_FRACTION_1));
    HasInstrumentIdPartition<TestHasInstrumentId> ignored2 = hasInstrumentIdPartition(singletonHasInstrumentIdMap(OBJ_111, unitFraction(1)));
    HasInstrumentIdPartition<TestHasInstrumentId> ignored3 = hasInstrumentIdPartition(singletonHasInstrumentIdMap(OBJ_111, unitFraction(1, 1)));
    HasInstrumentIdPartition<TestHasInstrumentId> ignored4 = singletonHasInstrumentIdPartition(OBJ_111);
  }

  @Test
  public void hasZeroFraction_throws() {
    assertIllegalArgumentException( () -> hasInstrumentIdPartition(hasInstrumentIdMapOf(
        OBJ_111, UNIT_FRACTION_0,
        OBJ_444, UNIT_FRACTION_1)));
  }

  @Test
  public void getsNoFractionsMap_throws() {
    assertIllegalArgumentException( () -> hasInstrumentIdPartition(emptyHasInstrumentIdMap()));
  }

  @Test
  public void testGetFractionOrZero() {
    assertEquals(unitFraction(0.1), TEST_PARTITION.getFractionOrZero(instrumentId(111)));
    assertEquals(unitFraction(0.4), TEST_PARTITION.getFractionOrZero(instrumentId(444)));
    assertEquals(unitFraction(0.5), TEST_PARTITION.getFractionOrZero(instrumentId(555)));
    assertEquals(UNIT_FRACTION_0, TEST_PARTITION.getFractionOrZero(instrumentId(987)));
  }

  @Test
  public void toString_default() {
    assertEquals(
        "100 % iid 111 1.1",
        singletonHasInstrumentIdPartition(OBJ_111).toString());
    assertEquals(
        "50 % iid 555 5.5 ; 40 % iid 444 4.4 ; 10 % iid 111 1.1",
        TEST_PARTITION.toString());
  }

  @Test
  public void toString_default_usingInstrumentMaster() {
    assertEquals(
        "100 % S1 1.1",
        singletonHasInstrumentIdPartition(OBJ_111)
            .toString(TEST_INSTRUMENT_MASTER, DUMMY_DATE));
    assertEquals(
        "50 % S5 5.5 ; 40 % S4 4.4 ; 10 % S1 1.1",
        TEST_PARTITION.toString(TEST_INSTRUMENT_MASTER, DUMMY_DATE));
  }

  @Test
  public void toString_usingPrecision() {
    assertEquals(
        "100 % iid 111 1.1",
        singletonHasInstrumentIdPartition(OBJ_111)
            .toString(2));

    HasInstrumentIdPartition<TestHasInstrumentId> nonRoundPartition =
        hasInstrumentIdPartition(hasInstrumentIdMapOf(
            OBJ_111, unitFraction(0.0998),
            OBJ_444, unitFraction(0.4001),
            OBJ_555, unitFraction(0.5001)));
    assertEquals(
        "50.01 % iid 555 5.5 ; 40.01 % iid 444 4.4 ; 9.98 % iid 111 1.1",
        nonRoundPartition.toString(2));
    assertEquals(
        "50 % iid 555 5.5 ; 40 % iid 444 4.4 ; 10 % iid 111 1.1",
        nonRoundPartition.toString(1));
    assertEquals(
        "50 % iid 555 5.5 ; 40 % iid 444 4.4 ; 10 % iid 111 1.1",
        nonRoundPartition.toString(0));
  }

  @Test
  public void toString_usingPrecision_usingInstrumentMaster() {
    assertEquals(
        "100 % S1 1.1",
        singletonHasInstrumentIdPartition(OBJ_111)
            .toStringInIncreasingInstrumentIdOrder(2, TEST_INSTRUMENT_MASTER, DUMMY_DATE));
    assertEquals(
        "100 % S1 1.1",
        singletonHasInstrumentIdPartition(OBJ_111)
            .toStringInDecreasingMembershipOrder(2, TEST_INSTRUMENT_MASTER, DUMMY_DATE));
    assertEquals(
        "100 % iid 111 1.1",
        singletonHasInstrumentIdPartition(OBJ_111)
            .toString(2));

    HasInstrumentIdPartition<TestHasInstrumentId> nonRoundPartition =
        hasInstrumentIdPartition(hasInstrumentIdMapOf(
            OBJ_111, unitFraction(0.0998),
            OBJ_444, unitFraction(0.4001),
            OBJ_555, unitFraction(0.5001)));
    assertEquals(
        "50.01 % S5 5.5 ; 40.01 % S4 4.4 ; 9.98 % S1 1.1",
        nonRoundPartition.toStringInDecreasingMembershipOrder(2, TEST_INSTRUMENT_MASTER, DUMMY_DATE));
    assertEquals(
        "50 % S5 5.5 ; 40 % S4 4.4 ; 10 % S1 1.1",
        nonRoundPartition.toStringInDecreasingMembershipOrder(1, TEST_INSTRUMENT_MASTER, DUMMY_DATE));
    assertEquals(
        "50 % S5 5.5 ; 40 % S4 4.4 ; 10 % S1 1.1",
        nonRoundPartition.toStringInDecreasingMembershipOrder(0, TEST_INSTRUMENT_MASTER, DUMMY_DATE));

    assertEquals(
        "9.98 % S1 1.1 ; 40.01 % S4 4.4 ; 50.01 % S5 5.5",
        nonRoundPartition.toStringInIncreasingInstrumentIdOrder(2, TEST_INSTRUMENT_MASTER, DUMMY_DATE));
    assertEquals(
        "10 % S1 1.1 ; 40 % S4 4.4 ; 50 % S5 5.5",
        nonRoundPartition.toStringInIncreasingInstrumentIdOrder(1, TEST_INSTRUMENT_MASTER, DUMMY_DATE));
    assertEquals(
        "10 % S1 1.1 ; 40 % S4 4.4 ; 50 % S5 5.5",
        nonRoundPartition.toStringInIncreasingInstrumentIdOrder(0, TEST_INSTRUMENT_MASTER, DUMMY_DATE));
  }

  @Test
  public void testForEachInInstrumentIdOrder() {
    StringBuilder sb = new StringBuilder();
    TEST_PARTITION.forEachInInstrumentIdOrder( (testHasInstrumentId, unitFraction) -> sb.append(Strings.format("%s %s|",
        unitFraction.toPercentString(0), testHasInstrumentId.toString())));
    assertEquals(
        "10 % iid 111 1.1|40 % iid 444 4.4|50 % iid 555 5.5|",
        sb.toString());
  }

  @Test
  public void testContainsKey() {
    assertTrue(TEST_PARTITION.containsKey(OBJ_111.getInstrumentId()));
    assertTrue(TEST_PARTITION.containsKey(OBJ_444.getInstrumentId()));
    assertTrue(TEST_PARTITION.containsKey(OBJ_555.getInstrumentId()));
    assertFalse(TEST_PARTITION.containsKey(instrumentId(999)));
  }

  // If the weights don't sum to 1.0, should still get a valid partition.
  @Test
  public void hasInstrumentIdPartitionFromWeights_doubleMap() {
    TestHasInstrumentId a = testHasInstrumentId(STOCK_A, 11);
    TestHasInstrumentId b = testHasInstrumentId(STOCK_B, 33);
    // can't renormalize an empty map
    assertIllegalArgumentException( () -> hasInstrumentIdPartitionFromWeights(emptyHasInstrumentIdMap()));

    // a single item can be renormalized to a singletonPartition
    assertResult(
        singletonHasInstrumentIdMap(a, 0.123),
        singletonHasInstrumentIdMap(a, UNIT_FRACTION_1));
    assertResult(
        singletonHasInstrumentIdMap(b, 456.7),
        singletonHasInstrumentIdMap(b, UNIT_FRACTION_1));
    // unless it's too small
    assertIllegalArgumentException( () -> hasInstrumentIdPartitionFromWeights(singletonHasInstrumentIdMap(a, 1e-13)));

    // the general case; the weights supplied are normalized to create a valid partition
    assertResult(
        hasInstrumentIdMapOf(
            a, 0.2,
            b, 0.3),
        hasInstrumentIdMapOf(
            a, unitFraction(doubleExplained(0.4, 0.2 / (0.2 + 0.3))),
            b, unitFraction(doubleExplained(0.6, 0.3 / (0.2 + 0.3)))));

    // scaling all the above input weights equally does not change the partition
    assertResult(
        hasInstrumentIdMapOf(
            a, 20.0,
            b, 30.0),
        hasInstrumentIdMapOf(
            a, unitFraction(doubleExplained(0.4, 20.0 / (20.0 + 30.0))),
            b, unitFraction(doubleExplained(0.6, 30.0 / (20.0 + 30.0)))));
  }

  @Test
  public void hasInstrumentIdPartitionFromWeights_doubleMap_tinyWeightsIgnored() {
    TestHasInstrumentId a = testHasInstrumentId(STOCK_A, 11);
    TestHasInstrumentId b = testHasInstrumentId(STOCK_B, 22);
    TestHasInstrumentId c = testHasInstrumentId(STOCK_C, 33);
    TestHasInstrumentId d = testHasInstrumentId(STOCK_D, 44);
    TestHasInstrumentId e = testHasInstrumentId(STOCK_E, 55);
    assertResult(
        hasInstrumentIdMapOf(
            a, -1.11e-13,
            b,  0.0,
            c,  2.22e-13,
            d,  0.5,
            e,  2.0),
        hasInstrumentIdMapOf(
            d, unitFraction(doubleExplained(0.2, 0.5 / (0.5 + 2.0))),
            e, unitFraction(doubleExplained(0.8, 2.0 / (0.5 + 2.0)))));
  }

  @Test
  public void testToRegularPartition() {
    assertThat(
        singletonHasInstrumentIdPartition(testHasInstrumentId(STOCK_A1, 1.1))
            .toInstrumentIdPartition(),
        partitionMatcher(
            singletonPartition(STOCK_A1)));
    assertThat(
        singletonHasInstrumentIdPartition(testHasInstrumentId(STOCK_A1, 1.1))
            .toAssetIdPartition(),
        partitionMatcher(
            singletonPartition(STOCK_A1)));

    HasInstrumentIdPartition<TestHasInstrumentId> testObject = hasInstrumentIdPartition(hasInstrumentIdMapOf(
        testHasInstrumentId(STOCK_A1, 1.1), unitFraction(0.4),
        testHasInstrumentId(STOCK_A2, 2.2), unitFraction(0.6)));
    assertThat(
        testObject.toInstrumentIdPartition(),
        partitionMatcher(
            partition(rbMapOf(
                STOCK_A1, unitFraction(0.4),
                STOCK_A2, unitFraction(0.6)))));
    assertThat(
        testObject.toAssetIdPartition(),
        partitionMatcher(
            partition(rbMapOf(
                STOCK_A1, unitFraction(0.4),
                STOCK_A2, unitFraction(0.6)))));
  }

  private void assertResult(
      HasInstrumentIdMap<TestHasInstrumentId, Double> beforeTurningIntoPartition,
      HasInstrumentIdMap<TestHasInstrumentId, UnitFraction> asPartition) {
    assertThat(
        hasInstrumentIdPartitionFromWeights(beforeTurningIntoPartition),
        hasInstrumentIdPartitionMatcher(
            hasInstrumentIdPartition(asPartition),
            f -> testHasInstrumentIdMatcher(f)));
  }

  @Override
  public HasInstrumentIdPartition<TestHasInstrumentId> makeTrivialObject() {
    return singletonHasInstrumentIdPartition(OBJ_111);
  }

  @Override
  public HasInstrumentIdPartition<TestHasInstrumentId> makeNontrivialObject() {
    return hasInstrumentIdPartition(hasInstrumentIdMapOf(
        testHasInstrumentId(instrumentId(111), 1.1), unitFraction(0.4),
        testHasInstrumentId(instrumentId(222), 2.2), unitFraction(0.6)));
  }

  @Override
  public HasInstrumentIdPartition<TestHasInstrumentId> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return hasInstrumentIdPartition(hasInstrumentIdMapOf(
        testHasInstrumentId(instrumentId(111), 1.1 + e), unitFraction(0.4 + e),
        testHasInstrumentId(instrumentId(222), 2.2 + e), unitFraction(0.6 - e)));
  }

  @Override
  protected boolean willMatch(HasInstrumentIdPartition<TestHasInstrumentId> expected,
                              HasInstrumentIdPartition<TestHasInstrumentId> actual) {
    return hasInstrumentIdPartitionMatcher(expected, f -> testHasInstrumentIdMatcher(f))
        .matches(actual);
  }

  public static <T extends HasInstrumentId> TypeSafeMatcher<HasInstrumentIdPartition<T>> hasInstrumentIdPartitionMatcher(
      HasInstrumentIdPartition<T> expected, MatcherGenerator<T> matcherGenerator) {
    return epsilonHasInstrumentIdPartitionMatcher(expected, matcherGenerator, DEFAULT_EPSILON_1e_8);
  }

  public static <T extends HasInstrumentId> TypeSafeMatcher<HasInstrumentIdPartition<T>> epsilonHasInstrumentIdPartitionMatcher(
      HasInstrumentIdPartition<T> expected, MatcherGenerator<T> matcherGenerator, Epsilon epsilon) {
    // Here, we won't use the usual makeMatcher approach, because we want to be able to print
    // the hasInstrumentIdPartition fraction at a high precision, whereas the default toString() only prints round percentages.
    return new TypeSafeMatcher<HasInstrumentIdPartition<T>>() {
      @Override
      protected boolean matchesSafely(HasInstrumentIdPartition<T> actual) {
        return hasInstrumentIdMapMatcher(
            expected.getRawFractionsMap(),
            matcherGenerator,
            f -> preciseValueMatcher(f, epsilon))
            .matches(actual.getRawFractionsMap());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected hasInstrumentIdPartition: %s", expected.toString(8)));
      }
    };
  }

}
