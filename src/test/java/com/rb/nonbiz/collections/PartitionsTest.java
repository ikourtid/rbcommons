package com.rb.nonbiz.collections;

import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.types.SignedFraction;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.nonbiz.collections.Deviations.deviations;
import static com.rb.nonbiz.collections.DeviationsTest.deviationsMatcher;
import static com.rb.nonbiz.collections.NonZeroDeviations.nonZeroDeviations;
import static com.rb.nonbiz.collections.NonZeroDeviationsTest.nonZeroDeviationsMatcher;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.PartitionTest.partitionMatcher;
import static com.rb.nonbiz.collections.Partitions.calculatePartitionDeviations;
import static com.rb.nonbiz.collections.Partitions.calculatePartitionNonZeroDeviations;
import static com.rb.nonbiz.collections.Partitions.partitionFromApproximateFractions;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionsTest {

  @Test
  public void isWithinEpsilon_returnsPartition() {
    assertThat(
        partitionFromApproximateFractions(singletonRBMap(STOCK_A, unitFraction(0.999)), 0.01),
        partitionMatcher(singletonPartition(STOCK_A)));
    assertThat(
        partitionFromApproximateFractions(
            rbMapOf(
                STOCK_A, unitFraction(0.4004),
                STOCK_B, unitFraction(0.6006)),
            0.01),
        partitionMatcher(partition(rbMapOf(
            STOCK_A, unitFraction(0.4),
            STOCK_B, unitFraction(0.6)))));
    assertThat(
        partitionFromApproximateFractions(
            rbMapOf(
                STOCK_A, unitFraction(doubleExplained(0.3996, 0.4 - 0.0004)),
                STOCK_B, unitFraction(doubleExplained(0.5994, 0.6 - 0.0006))),
            0.01),
        partitionMatcher(partition(rbMapOf(
            STOCK_A, unitFraction(0.4),
            STOCK_B, unitFraction(0.6)))));
  }

  @Test
  public void isNotWithinEpsilon_throws() {
    assertIllegalArgumentException( () ->
        partitionFromApproximateFractions(singletonRBMap(STOCK_A, unitFraction(0.999)), 0.0001));
    assertIllegalArgumentException( () ->
        partitionFromApproximateFractions(
            rbMapOf(
                STOCK_A, unitFraction(0.4004),
                STOCK_B, unitFraction(0.6006)),
            0.00001));
    assertIllegalArgumentException( () ->
        partitionFromApproximateFractions(
            rbMapOf(
                STOCK_A, unitFraction(doubleExplained(0.3996, 0.4 - 0.0004)),
                STOCK_B, unitFraction(doubleExplained(0.5994, 0.6 - 0.0006))),
            0.00001));
  }

  @Test
  public void testPartitionDeviations() {
    TriConsumer<RBMap<String, UnitFraction>, RBMap<String, UnitFraction>, RBMap<String, SignedFraction>> asserter =
        (partitionA, partitionB, deviationsMap) -> assertThat(
            calculatePartitionDeviations(partition(partitionA), partition(partitionB)),
            deviationsMatcher(deviations(deviationsMap), DEFAULT_EPSILON_1e_8));
    asserter.accept(
        singletonRBMap("a", UNIT_FRACTION_1),
        singletonRBMap("a", UNIT_FRACTION_1),
        singletonRBMap("a", SIGNED_FRACTION_0));
    asserter.accept(
        singletonRBMap("a", UNIT_FRACTION_1),
        singletonRBMap("b", UNIT_FRACTION_1),
        rbMapOf(
            "a", SIGNED_FRACTION_1,
            "b", signedFraction(-1)));
    asserter.accept(
        rbMapOf(
            "onlyLeft",      unitFraction(0.1),
            "biggerOnRight", unitFraction(0.2),
            "sameOnBoth",    unitFraction(0.3),
            "biggerOnLeft",  unitFraction(0.4)),
        rbMapOf(
            "onlyRight",     unitFraction(0.11),
            "biggerOnLeft",  unitFraction(0.23),
            "sameOnBoth",    unitFraction(0.30),
            "biggerOnRight", unitFraction(0.36)),
        rbMapOf(
            "onlyLeft",      signedFraction(0.1),
            "onlyRight",     signedFraction(-0.11),
            "biggerOnRight", signedFraction(doubleExplained(-0.16, 0.2 - 0.36)),
            "sameOnBoth",    SIGNED_FRACTION_0,
            "biggerOnLeft",  signedFraction(doubleExplained(0.17, 0.4 - 0.23))));
  }

  @Test
  public void testPartitionNonZeroDeviations() {
    TriConsumer<RBMap<String, UnitFraction>, RBMap<String, UnitFraction>, RBMap<String, SignedFraction>> asserter =
        (partitionA, partitionB, deviationsMap) -> assertThat(
            calculatePartitionNonZeroDeviations(partition(partitionA), partition(partitionB)),
            nonZeroDeviationsMatcher(nonZeroDeviations(deviationsMap), DEFAULT_EPSILON_1e_8));
    asserter.accept(
        singletonRBMap("a", UNIT_FRACTION_1),
        singletonRBMap("a", UNIT_FRACTION_1),
        emptyRBMap());
    asserter.accept(
        singletonRBMap("a", UNIT_FRACTION_1),
        singletonRBMap("b", UNIT_FRACTION_1),
        rbMapOf(
            "a", SIGNED_FRACTION_1,
            "b", signedFraction(-1)));
    asserter.accept(
        rbMapOf(
            "onlyLeft",      unitFraction(0.1),
            "biggerOnRight", unitFraction(0.2),
            "sameOnBoth",    unitFraction(0.3),
            "biggerOnLeft",  unitFraction(0.4)),
        rbMapOf(
            "onlyRight",     unitFraction(0.11),
            "biggerOnLeft",  unitFraction(0.23),
            "sameOnBoth",    unitFraction(0.30),
            "biggerOnRight", unitFraction(0.36)),
        rbMapOf(
            "onlyLeft",      signedFraction(0.1),
            "onlyRight",     signedFraction(-0.11),
            "biggerOnRight", signedFraction(doubleExplained(-0.16, 0.2 - 0.36)),
            // nonZeroDeviations will not store this: "sameOnBoth",    SIGNED_FRACTION_0,
            "biggerOnLeft",  signedFraction(doubleExplained(0.17, 0.4 - 0.23))));
  }

}
