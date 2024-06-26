package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableSet;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.Symbol.symbol;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.biz.types.trading.SignedQuantity.signedQuantity;
import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.DoubleMap.emptyDoubleMap;
import static com.rb.nonbiz.collections.DoubleMap.singletonDoubleMap;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.partitionFromPositiveWeightsWhichMayNotSumTo1;
import static com.rb.nonbiz.collections.Partition.partitionFromPositiveWeightsWhichMaySumToBelow1;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.SignedPartition.signedPartition;
import static com.rb.nonbiz.collections.SignedPartitionTest.signedPartitionMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_SYMBOL;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.UnitFraction.DUMMY_UNIT_FRACTION;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PartitionTest extends RBTestMatcher<Partition<String>> {

  @Test
  public void testContainsOnlyKey() {
    assertTrue(singletonPartition("a").containsOnlyKey("a"));
    assertFalse(singletonPartition("b").containsOnlyKey("a"));
    assertFalse(
        partition(rbMapOf(
            "a", DUMMY_UNIT_FRACTION,
            "b", DUMMY_UNIT_FRACTION.complement()))
            .containsOnlyKey("a"));
    assertFalse(
        partition(rbMapOf(
            "c", DUMMY_UNIT_FRACTION,
            "b", DUMMY_UNIT_FRACTION.complement()))
            .containsOnlyKey("a"));
  }

  @Test
  public void getters() {
    Partition<String> partition = partition(rbMapOf(
        "a", unitFraction(0.4),
        "b", unitFraction(0.6)));
    assertEquals(2, partition.size());
    assertEquals(
        ImmutableSet.of("a", "b"),
        partition.keySet());

    assertTrue( partition.containsKey("a"));
    assertTrue( partition.containsKey("b"));
    assertFalse(partition.containsKey("missingKey"));

    assertEquals(
        unitFraction(0.4),
        partition.getOrZero("a"));
    assertIllegalArgumentException( () -> partition.getOrThrow("missingKey"));

    assertEquals(
        unitFraction(0.6),
        partition.getOrZero("b"));
    assertEquals(
        UNIT_FRACTION_0,
        partition.getOrZero("missingKey"));
  }

  @Test
  public void testEntrySet() {
    assertAlmostEquals(
        unitFraction(0.6),
        UnitFraction.sum(
            partition(rbMapOf(
                "skip", unitFraction(0.4),
                "keep", unitFraction(0.6)))
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals("keep"))
                .map(entry -> entry.getValue())),
        DEFAULT_EPSILON_1e_8);
  }

  @Test
  public void happyPath() {
    Partition<String> partition = partition(rbMapOf(
        "a", unitFraction(0.4),
        "b", unitFraction(0.6)));
    assertEquals(unitFraction(0.4), partition.getFraction("a"));
    assertEquals(unitFraction(0.6), partition.getFraction("b"));
  }

  @Test
  public void partitionFromPositiveWeightsWhichMayNotSumTo1_preciseValueOverload_happyPath() {
    Partition<String> partition = partitionFromPositiveWeightsWhichMayNotSumTo1(rbMapOf(
        "a", money(100),
        "b", money(400)));
    assertEquals(unitFraction(1, 5), partition.getFraction("a"));
    assertEquals(unitFraction(4, 5), partition.getFraction("b"));
    assertEquals(ImmutableSet.of("a", "b"), partition.keySet());
  }

  @Test
  public void partitionFromPositiveWeightsWhichSumToBelow1_preciseValueOverload_happyPath() {
    Partition<String> partition = partitionFromPositiveWeightsWhichMayNotSumTo1(rbMapOf(
        "a", money(100),
        "b", money(400)));
    assertEquals(unitFraction(1, 5), partition.getFraction("a"));
    assertEquals(unitFraction(4, 5), partition.getFraction("b"));
    assertEquals(ImmutableSet.of("a", "b"), partition.keySet());
  }

  @Test
  public void partitionFromPositiveWeightsWhichMayNotSumTo1_doubleMap_negativeWeight_throws() {
    // no negative weights allowed
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMayNotSumTo1(singletonDoubleMap("a", -0.123)));
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMayNotSumTo1(doubleMap(rbMapOf(
        "a",  0.5,
        "b", -0.1,
        "c",  0.9))));

    // except for extremely small negative weights (max magnitude -1e-12), which are skipped
    assertThat(
        partitionFromPositiveWeightsWhichMayNotSumTo1(doubleMap(rbMapOf(
            "a", 4.0,
            "b", -1e-13,
            "c", 6.0))),
        partitionMatcher(partition(rbMapOf(
            "a", unitFraction(0.4),
            "c", unitFraction(0.6)))));
  }

  @Test
  public void partitionFromPositiveWeightsWhichMaySumToBelow1_doubleMap_negativeWeight_throws() {
    // no negative weights allowed
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMaySumToBelow1(singletonDoubleMap("a", -0.123)));
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMaySumToBelow1(doubleMap(rbMapOf(
        "a",  0.5,
        "b", -0.1,
        "c",  0.9))));

    // except for extremely small negative weights (max magnitude -1e-12), which are skipped
    assertThat(
        partitionFromPositiveWeightsWhichMaySumToBelow1(doubleMap(rbMapOf(
            "a", 0.04,
            "b", -1e-13,
            "c", +2e-13,
            "d", 0.06))),
        partitionMatcher(partition(rbMapOf(
            "a", unitFraction(0.4),
            "d", unitFraction(0.6)))));
  }

  // If the weights don't sum to 1.0, should still get a valid partition.
  @Test
  public void partitionFromPositiveWeightsWhichMayNotSumTo1_doubleMap() {
    // can't renormalize an empty map
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMayNotSumTo1(emptyDoubleMap()));

    // a single item can be renormalized to a singletonPartition
    assertThat(
        partitionFromPositiveWeightsWhichMayNotSumTo1(singletonDoubleMap("a", 0.123)),
        partitionMatcher(singletonPartition("a")));
    assertThat(
        partitionFromPositiveWeightsWhichMayNotSumTo1(singletonDoubleMap("b", 456.7)),
        partitionMatcher(singletonPartition("b")));
    // unless it's too small
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMayNotSumTo1(singletonDoubleMap("a", 1e-13)));

    // the general case; the weights supplied are normalized to create a valid partition
    assertThat(
        partitionFromPositiveWeightsWhichMayNotSumTo1(doubleMap(rbMapOf(
            "a", 0.2,
            "b", 0.3))),
        partitionMatcher(partition(rbMapOf(
            "a", unitFraction(doubleExplained(0.4, 0.2 / (0.2 + 0.3))),
            "b", unitFraction(doubleExplained(0.6, 0.3 / (0.2 + 0.3)))))));

    // scaling all the above input weights equally does not change the partition
    assertThat(
        partitionFromPositiveWeightsWhichMayNotSumTo1(doubleMap(rbMapOf(
            "a", 20.0,
            "b", 30.0))),
        partitionMatcher(partition(rbMapOf(
            "a", unitFraction(doubleExplained(0.4, 20.0 / (20.0 + 30.0))),
            "b", unitFraction(doubleExplained(0.6, 30.0 / (20.0 + 30.0)))))));
  }

  // If the weights don't sum to 1.0, should still get a valid partition,
  // but we should get an exception if they sum to above 1.
  @Test
  public void partitionFromPositiveWeightsWhichMaySumToBelow1_doubleMap() {
    // can't renormalize an empty map
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMaySumToBelow1(emptyDoubleMap()));

    // a single item can be renormalized to a singletonPartition...
    assertThat(
        partitionFromPositiveWeightsWhichMaySumToBelow1(singletonDoubleMap("a", 0.123)),
        partitionMatcher(singletonPartition("a")));
    // ... unless it's too small
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMaySumToBelow1(singletonDoubleMap("a", 1e-13)));
    // Weight (and therefore weight sum) is > 1
    assertIllegalArgumentException( () ->
        partitionFromPositiveWeightsWhichMaySumToBelow1(singletonDoubleMap("b", 456.7)));

    // the general case; the weights supplied are normalized to create a valid partition
    assertThat(
        partitionFromPositiveWeightsWhichMaySumToBelow1(doubleMap(rbMapOf(
            "a", 0.2,
            "b", 0.3))),
        partitionMatcher(partition(rbMapOf(
            "a", unitFraction(doubleExplained(0.4, 0.2 / (0.2 + 0.3))),
            "b", unitFraction(doubleExplained(0.6, 0.3 / (0.2 + 0.3)))))));

    // Weight sum is > 1 - exception
    assertIllegalArgumentException( () ->
        partitionFromPositiveWeightsWhichMaySumToBelow1(doubleMap(rbMapOf(
            "a", 20.0,
            "b", 30.0))));
  }

  @Test
  public void partitionFromPositiveWeightsWhichMayNotSumTo1_doubleMap_tinyWeightsIgnored() {
    assertThat(
        partitionFromPositiveWeightsWhichMayNotSumTo1(doubleMap(rbMapOf(
            "a", -1.11e-13,
            "b",  0.0,
            "c",  2.22e-13,
            "d",  0.5,
            "e",  2.0))),
        partitionMatcher(partition(rbMapOf(
            "d", unitFraction(doubleExplained(0.2, 0.5 / (0.5 + 2.0))),
            "e", unitFraction(doubleExplained(0.8, 2.0 / (0.5 + 2.0)))))));
  }

  @Test
  public void partitionFromPositiveWeightsWhichMaySumToBelow1_doubleMap_tinyWeightsIgnored() {
    assertThat(
        partitionFromPositiveWeightsWhichMaySumToBelow1(doubleMap(rbMapOf(
            "a", -1.11e-13,
            "b",  0.0,
            "c",  2.22e-13,
            "d",  0.05,
            "e",  0.2))),
        partitionMatcher(partition(rbMapOf(
            "d", unitFraction(doubleExplained(0.2, 0.05 / (0.05 + 0.2))),
            "e", unitFraction(doubleExplained(0.8, 0.2  / (0.05 + 0.2)))))));

    // Weights sum to > 1
    assertIllegalArgumentException( () ->
        partitionFromPositiveWeightsWhichMaySumToBelow1(doubleMap(rbMapOf(
            "a", -1.11e-13,
            "b",  0.0,
            "c",  2.22e-13,
            "d",  0.5,
            "e",  2.0))));
  }

  @Test
  public void partitionFromPositiveWeights_hasNegativeWeightSomewhere_throws() {
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMayNotSumTo1(rbMapOf(
        "a", signedQuantity(-100),
        "b", signedQuantity(400))));
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMaySumToBelow1(rbMapOf(
        "a", signedQuantity(-100),
        "b", signedQuantity(400))));
  }

  @Test
  public void partitionFromPositiveWeights_oneWeightIsZero_works() {
    assertThat(
        partitionFromPositiveWeightsWhichMayNotSumTo1(rbMapOf(
            "a", ZERO_MONEY,
            "b", money(1))),
        partitionMatcher(
            singletonPartition("b")));
    assertThat(
        partitionFromPositiveWeightsWhichMaySumToBelow1(rbMapOf(
            "a", ZERO_MONEY,
            "b", money(1))),
        partitionMatcher(
            singletonPartition("b")));
  }

  @Test
  public void partitionFromPositiveWeights_oneWeightIsNegative_throws() {
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMayNotSumTo1(rbMapOf(
        "a", signedMoney(-1),
        "b", signedMoney(3))));
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMaySumToBelow1(rbMapOf(
        "a", signedMoney(-1),
        "b", signedMoney(3))));

    // Weights sum to < 1, but there's a negative value, so exception
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMaySumToBelow1(rbMapOf(
        "a", signedMoney(-1),
        "b", signedMoney(1.1))));
    // Weights sum to < 0, but there's a negative value, so exception either way
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMaySumToBelow1(rbMapOf(
        "a", signedMoney(-1),
        "b", signedMoney(0.5))));
  }

  @Test
  public void partitionFromPositiveWeights_allWeightsAreZero_throws() {
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMayNotSumTo1(rbMapOf(
        "a", ZERO_MONEY,
        "b", ZERO_MONEY)));
    assertIllegalArgumentException( () -> partitionFromPositiveWeightsWhichMaySumToBelow1(rbMapOf(
        "a", ZERO_MONEY,
        "b", ZERO_MONEY)));
  }

  @Test
  public void fractionsSumToLessThan1_throws() {
    assertIllegalArgumentException( () -> partition(rbMapOf(
        "a", unitFraction(0.4),
        "b", unitFraction(0.59))));
  }

  @Test
  public void fractionsSumToMoreThan1_throws() {
    assertIllegalArgumentException( () -> partition(rbMapOf(
        "a", unitFraction(0.4),
        "b", unitFraction(0.61))));
  }

  @Test
  public void fractionsDontHaveToAddToExactlyOneDueToFloatingPointPrecision() {
    Partition<String> ignored1 = partition(rbMapOf(
        "a", unitFraction(1 / 3.0),
        "b", unitFraction(1 / 3.0),
        "c", unitFraction(1 / 3.0)));
    Partition<String> ignored2 = partition(rbMapOf(
        "a", unitFraction(1, 3),
        "b", unitFraction(1, 3),
        "c", unitFraction(1, 3)));
  }

  @Test
  public void singleWholeFraction_doesNotThrow() {
    Partition<String> ignored1 = partition(singletonRBMap("a", UNIT_FRACTION_1));
    Partition<String> ignored2 = partition(singletonRBMap("a", unitFraction(1)));
    Partition<String> ignored3 = partition(singletonRBMap("a", unitFraction(1, 1)));
  }

  @Test
  public void hasZeroFraction_throws() {
    assertIllegalArgumentException( () -> partition(rbMapOf(
        "a", UNIT_FRACTION_0,
        "b", UNIT_FRACTION_1)));
  }

  @Test
  public void getsNoFractionsMap_throws() {
    assertIllegalArgumentException( () -> partition(emptyRBMap()));
  }

  @Test
  public void getUnknownKey_throws() {
    Partition<String> partition = partition(rbMapOf(
        "a", unitFraction(0.4),
        "b", unitFraction(0.6)));
    assertIllegalArgumentException( () -> partition.getFraction("c"));
  }

  @Test
  public void toString_default() {
    assertEquals(
        "100 % a",
        partition(singletonRBMap(
            "a", UNIT_FRACTION_1))
            .toString());
    assertEquals(
        "59.99 % b ; 40.01 % a",
        partition(rbMapOf(
            "a", unitFraction(0.4001),
            "b", unitFraction(0.5999)))
            .toString());
    assertEquals(
        "60 % b ; 40 % a",
        partition(rbMapOf(
            "a", unitFraction(0.40001),
            "b", unitFraction(0.59999)))
            .toString());
  }

  @Test
  public void toString_twoDigitPrecision() {
    assertEquals(
        "100 % a",
        partition(singletonRBMap(
            "a", UNIT_FRACTION_1))
            .toString(2));
    assertEquals(
        "59.99 % b ; 40.01 % a",
        partition(rbMapOf(
            "a", unitFraction(0.4001),
            "b", unitFraction(0.5999)))
            .toString(2));
  }

  @Test
  public void toString_usesMapping() {
    Function<InstrumentId, String> symbolMapper = iid ->
        iid.asLong() == 7 ? symbol("SPY").toString() :
            iid.asLong() == 8 ? symbol("BND").toString() :
                DUMMY_SYMBOL.toString();
    Partition<InstrumentId> partition = partition(rbMapOf(
        instrumentId(7), unitFraction(0.5999),
        instrumentId(8), unitFraction(0.4001)));
    // print in decreasing membership order
    assertEquals(
        "59.99 % SPY ; 40.01 % BND",
        partition.toStringInDecreasingMembershipOrder(2, symbolMapper));

    // print in InstrumentId order
    assertEquals(
        "59.99 % SPY ; 40.01 % BND",
        partition.toStringInIncreasingKeyOrder(2, InstrumentId::compareTo, symbolMapper));
  }

  @Test
  public void testToSignedPartition() {
    assertThat(
        partition(rbMapOf(
            "a", unitFraction(0.4),
            "b", unitFraction(0.6)))
            .toSignedPartition(),
        signedPartitionMatcher(
            signedPartition(rbMapOf(
                "a", signedFraction(0.4),
                "b", signedFraction(0.6)))));
  }

  @Override
  public Partition<String> makeTrivialObject() {
    return singletonPartition("a");
  }

  @Override
  public Partition<String> makeNontrivialObject() {
    return partition(rbMapOf(
        "a", unitFraction(0.4),
        "b", unitFraction(0.6)));
  }

  @Override
  public Partition<String> makeMatchingNontrivialObject() {
    return partition(rbMapOf(
        "b", unitFraction(0.60000000001),
        "a", unitFraction(0.39999999999)));
  }

  @Override
  protected boolean willMatch(Partition<String> expected, Partition<String> actual) {
    return partitionMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<Partition<T>> partitionMatcher(Partition<T> expected) {
    return epsilonPartitionMatcher(expected, DEFAULT_EPSILON_1e_8);
  }

  public static <T> TypeSafeMatcher<Partition<T>> epsilonPartitionMatcher(Partition<T> expected, Epsilon epsilon) {
    // Here, we won't use the usual makeMatcher approach, because we want to be able to print
    // the partition fraction at a high precision, whereas the default toString() only prints round percentages.
    return new TypeSafeMatcher<Partition<T>>() {
      @Override
      protected boolean matchesSafely(Partition<T> actual) {
        return rbMapPreciseValueMatcher(expected.getRawFractionsMap(), epsilon)
            .matches(actual.getRawFractionsMap());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected partition: %s", expected.toString(8)));
      }
    };
  }

}
