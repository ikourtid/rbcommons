package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableSet;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.PartitionTest.partitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.SignedPartition.signedPartition;
import static com.rb.nonbiz.collections.SignedPartition.signedPartitionFromWeights;
import static com.rb.nonbiz.collections.SignedPartition.singletonSignedPartition;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_SYMBOL;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class SignedPartitionTest extends RBTestMatcher<SignedPartition> {

  @Test
  public void happyPath_allPositive() {
    SignedPartition<String> signedPartition = signedPartition(rbMapOf(
        "a", signedFraction(0.4),
        "b", signedFraction(0.6)));
    assertEquals(signedFraction(0.4), signedPartition.getFraction("a"));
    assertEquals(signedFraction(0.6), signedPartition.getFraction("b"));
  }

  @Test
  public void happyPath_someNegative() {
    SignedPartition<String> signedPartition = signedPartition(rbMapOf(
        "a", signedFraction(0.4),
        "b", signedFraction(0.6)));
    assertEquals(signedFraction(0.4), signedPartition.getFraction("a"));
    assertEquals(signedFraction(0.6), signedPartition.getFraction("b"));
  }

  @Test
  public void partitionFromWeights_happyPath() {
    SignedPartition<String> signedPartition = signedPartitionFromWeights(rbMapOf(
        "a", money(100),
        "b", money(400)));
    assertEquals(signedFraction(1, 5), signedPartition.getFraction("a"));
    assertEquals(signedFraction(4, 5), signedPartition.getFraction("b"));
    assertEquals(ImmutableSet.of("a", "b"), signedPartition.keySet());
  }

  @Test
  public void partitionFromWeights_oneWeightIsZero_works() {
    assertThat(
        signedPartitionFromWeights(rbMapOf(
            "a", ZERO_MONEY,
            "b", money(1))),
        signedPartitionMatcher(
            singletonSignedPartition("b")));
  }

  @Test
  public void partitionFromWeights_oneWeightIsNegative_works() {
    SignedPartition<String> signedPartition = signedPartitionFromWeights(rbMapOf(
        "a", signedMoney(-1),
        "b", signedMoney(4)));
    assertEquals(signedFraction(-100, intExplained(300, 400 - 100)), signedPartition.getFraction("a"));
    assertEquals(signedFraction(400, intExplained(300, 400 - 100)), signedPartition.getFraction("b"));
    assertEquals(ImmutableSet.of("a", "b"), signedPartition.keySet());
  }

  @Test
  public void partitionFromWeights_allWeightsAreZero_throws() {
    assertIllegalArgumentException( () -> signedPartitionFromWeights(rbMapOf(
        "a", ZERO_MONEY,
        "b", ZERO_MONEY)));
  }

  @Test
  public void fractionsSumToLessThan1_throws() {
    assertIllegalArgumentException( () -> signedPartition(rbMapOf(
        "a", signedFraction(0.4),
        "b", signedFraction(0.59))));
    assertIllegalArgumentException( () -> signedPartition(rbMapOf(
        "a", signedFraction(1.4),
        "b", signedFraction(-0.39))));
  }

  @Test
  public void fractionsSumToMoreThan1_throws() {
    assertIllegalArgumentException( () -> signedPartition(rbMapOf(
        "a", signedFraction(0.4),
        "b", signedFraction(0.61))));
    assertIllegalArgumentException( () -> signedPartition(rbMapOf(
        "a", signedFraction(-0.60),
        "b", signedFraction(1.61))));
  }

  @Test
  public void fractionsDontHaveToAddToExactlyOneDueToFloatingPointPrecision() {
    SignedPartition<String> ignored1 = signedPartition(rbMapOf(
        "a", signedFraction(1 / 3.0),
        "b", signedFraction(1 / 3.0),
        "c", signedFraction(1 / 3.0)));
    SignedPartition<String> ignored2 = signedPartition(rbMapOf(
        "a", signedFraction(1, 3),
        "b", signedFraction(1, 3),
        "c", signedFraction(1, 3)));
  }

  @Test
  public void singleWholeFraction_doesNotThrow() {
    SignedPartition<String> ignored1 = signedPartition(singletonRBMap("a", SIGNED_FRACTION_1));
    SignedPartition<String> ignored2 = signedPartition(singletonRBMap("a", signedFraction(1)));
    SignedPartition<String> ignored3 = signedPartition(singletonRBMap("a", signedFraction(1, 1)));
  }

  @Test
  public void hasZeroFraction_throws() {
    assertIllegalArgumentException( () -> signedPartition(rbMapOf(
        "a", SIGNED_FRACTION_0,
        "b", SIGNED_FRACTION_1)));
    assertIllegalArgumentException( () -> signedPartition(rbMapOf(
        "a", SIGNED_FRACTION_0,
        "b", signedFraction(1.12345),
        "c", signedFraction(-0.12345))));
  }

  @Test
  public void hasAlmostZeroFraction_throws() {
    assertIllegalArgumentException( () -> signedPartition(rbMapOf(
        "a", signedFraction(1e-9),
        "b", SIGNED_FRACTION_1)));
    assertIllegalArgumentException( () -> signedPartition(rbMapOf(
        "a", signedFraction(1e-9),
        "b", signedFraction(1.12345),
        "c", signedFraction(-0.12345))));
  }

  @Test
  public void getsNoFractionsMap_throws() {
    assertIllegalArgumentException( () -> signedPartition(emptyRBMap()));
  }

  @Test
  public void getUnknownKey_throws() {
    SignedPartition<String> signedPartition = signedPartition(rbMapOf(
        "a", signedFraction(0.4),
        "b", signedFraction(0.6)));
    assertIllegalArgumentException( () -> signedPartition.getFraction("c"));
  }

  @Test
  public void testToPartitionOfUnsigned() {
    assertThat(
        signedPartition(rbMapOf(
            "a", signedFraction(0.4),
            "b", signedFraction(0.6)))
            .toPartitionOfUnsigned(),
        partitionMatcher(
            partition(rbMapOf(
                "a", unitFraction(0.4),
                "b", unitFraction(0.6)))));
    SignedPartition<String> signedPartitionWithNegatives = signedPartition(rbMapOf(
        "a", signedFraction(1.2),
        "b", signedFraction(-0.2)));
    assertIllegalArgumentException( () -> signedPartitionWithNegatives.toPartitionOfUnsigned());
  }

  @Test
  public void toString_default() {
    assertEquals(
        "100 % a",
        signedPartition(singletonRBMap(
            "a", SIGNED_FRACTION_1))
            .toString());
    assertEquals(
        "60 % b ; 40 % a",
        signedPartition(rbMapOf(
            "a", signedFraction(0.4001),
            "b", signedFraction(0.5999)))
            .toString());
    assertEquals(
        "140 % b ; -40 % a",
        signedPartition(rbMapOf(
            "a", signedFraction(-0.4001),
            "b", signedFraction(1.4001)))
            .toString());
  }

  @Test
  public void toString_twoDigitPrecision() {
    assertEquals(
        "100.00 % a",
        signedPartition(singletonRBMap(
            "a", SIGNED_FRACTION_1))
            .toString(2));
    assertEquals(
        "59.99 % b ; 40.01 % a",
        signedPartition(rbMapOf(
            "a", signedFraction(0.4001),
            "b", signedFraction(0.5999)))
            .toString(2));
    assertEquals(
        "140.01 % b ; -40.01 % a",
        signedPartition(rbMapOf(
            "a", signedFraction(-0.4001),
            "b", signedFraction(1.4001)))
            .toString(2));
  }

  @Test
  public void toString_usesMapping() {
    Function<InstrumentId, String> symbolMapper = iid ->
        iid.asLong() == 7 ? "SPY" :
        iid.asLong() == 8 ? "BND" :
        DUMMY_SYMBOL.toString();
    assertEquals(
        "59.99 % SPY ; 40.01 % BND",
        signedPartition(rbMapOf(
            instrumentId(7), signedFraction(0.5999),
            instrumentId(8), signedFraction(0.4001)))
            .toStringInDecreasingMembershipOrder(2, symbolMapper));
    assertEquals(
        "140.01 % SPY ; -40.01 % BND",
        signedPartition(rbMapOf(
            instrumentId(7), signedFraction(1.4001),
            instrumentId(8), signedFraction(-0.4001)))
            .toStringInDecreasingMembershipOrder(2, symbolMapper));
  }

  @Override
  public SignedPartition makeTrivialObject() {
    return singletonSignedPartition("a");
  }

  @Override
  public SignedPartition makeNontrivialObject() {
    return signedPartition(rbMapOf(
        "a", signedFraction(2.4),
        "b", signedFraction(-1.4)));
  }

  @Override
  public SignedPartition makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return signedPartition(rbMapOf(
        "a", signedFraction(2.4 + e),
        "b", signedFraction(-1.4 - e)));
  }

  @Override
  protected boolean willMatch(SignedPartition expected, SignedPartition actual) {
    return signedPartitionMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<SignedPartition<T>> signedPartitionMatcher(SignedPartition<T> expected) {
    return epsilonSignedPartitionMatcher(expected, DEFAULT_EPSILON_1e_8);
  }

  public static <T> TypeSafeMatcher<SignedPartition<T>> epsilonSignedPartitionMatcher(
      SignedPartition<T> expected, Epsilon epsilon) {
    return makeMatcher(expected,
        match(v -> v.getRawFractionsMap(), f -> rbMapPreciseValueMatcher(f, epsilon)));
  }

}