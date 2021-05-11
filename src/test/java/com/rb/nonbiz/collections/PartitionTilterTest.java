package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.PositiveMultiplier;
import com.rb.nonbiz.types.PositiveMultipliersMap;
import org.junit.Test;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.PartitionTest.partitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static com.rb.nonbiz.types.PositiveMultipliersMap.emptyPositiveMultipliersMap;
import static com.rb.nonbiz.types.PositiveMultipliersMap.positiveMultipliersMap;
import static com.rb.nonbiz.types.PositiveMultipliersMap.singletonPositiveMultipliersMap;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionTilterTest extends RBTest<PartitionTilter> {

  @Test
  public void emptyMultipliers_partitionUnchanged() {
    Partition<String> originalPartition = partition(rbMapOf(
        "A", unitFraction(0.6),
        "B", unitFraction(0.4)));
    assertThat(
        makeTestObject().tiltPartition(originalPartition, emptyPositiveMultipliersMap()),
        partitionMatcher(originalPartition));   // original partition unchanged
  }

  @Test
  public void multipliersKeys_doNotMatchPartitionKeys_partitionUnchanged() {
    Partition<String> originalPartition = partition(rbMapOf(
        "A", unitFraction(0.6),
        "B", unitFraction(0.4)));
    assertThat(
        makeTestObject().tiltPartition(
            originalPartition,
            positiveMultipliersMap(rbMapOf(
                "X", positiveMultiplier(0.123), // no overlap between tilt map and partition
                "Y", positiveMultiplier(4.567),
                "Z", positiveMultiplier(9.876)))),
        partitionMatcher(originalPartition));   // original partition unchanged
  }

  // A partition with a single item with 100% weight will not change when "tilted"
  // by any amount; after normalization the tilted value remains 100%.
  @Test
  public void singlePartitionEntryTilted_unchanged() {
    Partition<String> singleEntryPartition = singletonPartition("A");  // weight "A" =  100%
    for (double multiplier : rbSetOf(
        1e-9,
        0.001,
        0.1,
        1.0,
        10.0,
        1_000.0,
        1e9)) {
      assertThat(
          makeTestObject().tiltPartition(
              singleEntryPartition,
              singletonPositiveMultipliersMap("A", positiveMultiplier(multiplier))),
          partitionMatcher(singleEntryPartition));   // single entry partition unchanged
    }
  }

  // If all entries in a partition are multiplied by the same amount,
  // there is no tilting effect after normalization.
  @Test
  public void allMultipliersEqual_noTilt() {
    Partition<String> originalPartition = partition(rbMapOf(
        "A", unitFraction(0.1),
        "B", unitFraction(0.2),
        "C", unitFraction(0.3),
        "D", unitFraction(0.4)));
    for (double multiplier : rbSetOf(
        1e-9,
        0.001,
        0.1,
        1.0,
        10.0,
        1_000.0,
        1e9)) {
      PositiveMultiplier uniformMultiplier = positiveMultiplier(multiplier);
      assertThat(
          makeTestObject().tiltPartition(
              originalPartition,
              positiveMultipliersMap(rbMapOf(
                  "A", uniformMultiplier,
                  "B", uniformMultiplier,
                  "C", uniformMultiplier,
                  "D", uniformMultiplier))),
          partitionMatcher(originalPartition));  // original partition unchanged
    }
  }

  // Multiple tilts can be applied to a single partition. If they are,
  // the order in which they are applied does not matter.
  @Test
  public void multipleTilts_areCommutative() {
    Partition<String> originalPartition = partition(rbMapOf(
        "A", unitFraction(0.1),
        "B", unitFraction(0.2),
        "C", unitFraction(0.3),
        "D", unitFraction(0.4)));
    PositiveMultipliersMap<String> tilt1 = positiveMultipliersMap(rbMapOf(
        "A", positiveMultiplier(0.123),
        "B", positiveMultiplier(0.456),
        "C", positiveMultiplier(7.890),
        "D", positiveMultiplier(12.34)));
    PositiveMultipliersMap<String> tilt2 = positiveMultipliersMap(rbMapOf(
        "A", positiveMultiplier(1.111),
        "B", positiveMultiplier(2.222),
        "C", positiveMultiplier(3.333),
        "D", positiveMultiplier(4.444)));

    assertThat(
        makeTestObject().tiltPartition(                      // apply tilt1, then tilt2
            makeTestObject().tiltPartition(originalPartition, tilt1),
            tilt2),
        partitionMatcher(makeTestObject().tiltPartition(     // apply tilt2, then tilt1
            makeTestObject().tiltPartition(originalPartition, tilt2),
            tilt1)));
  }

  @Test
  public void generalCase() {
    Partition<String> tiltedPartition = makeTestObject().tiltPartition(
        partition(rbMapOf(
            "A", unitFraction(0.1),
            "B", unitFraction(0.2),
            "C", unitFraction(0.3),
            "D", unitFraction(0.4))),
        positiveMultipliersMap(rbMapOf(
            "A", positiveMultiplier(3.2),
            "B", positiveMultiplier(2.0),
            "C", positiveMultiplier(1.6),      // no entry for "D"; use default 1.0
            "Z", positiveMultiplier(999.0)))); // not used; no partition entry "Z"

    // after the tilts, the new denominator for calculating the new partition unitFractions
    double denominator = doubleExplained(1.6, 0.1 * 3.2 + 0.2 * 2.0 + 0.3 * 1.6 + 0.4 * 1.0);

    assertThat(
        tiltedPartition,
        partitionMatcher(partition(rbMapOf(
            "A", unitFraction(doubleExplained(0.20, 0.1 * 3.2 / denominator)),
            "B", unitFraction(doubleExplained(0.25, 0.2 * 2.0 / denominator)),
            "C", unitFraction(doubleExplained(0.30, 0.3 * 1.6 / denominator)),
            "D", unitFraction(doubleExplained(0.25, 0.4 * 1.0 / denominator))))));

    // An entry in the multiplier map that does not match an entry in the original partition
    // does not create an entry in the "tilted" partition.
    assertIllegalArgumentException( () -> tiltedPartition.getFraction("Z"));
  }

  @Test
  public void presentableTest() {
    InstrumentId A = instrumentId(1);
    InstrumentId B = instrumentId(2);
    InstrumentId C = instrumentId(3);
    InstrumentId D = instrumentId(4);

    Partition<InstrumentId> tiltedPartition = makeTestObject().tiltPartition(
        // The original partition (a generalization of a stock index) is 10% A, 20% B, 30% C, 40% D.
        partition(rbMapOf(
            A, unitFraction(0.1),
            B, unitFraction(0.2),
            C, unitFraction(0.3),
            D, unitFraction(0.4))),

        // ... however, we want to tilt things a bit to reflect values-based preferences.
        positiveMultipliersMap(rbMapOf(
            A, positiveMultiplier(1.6),    // We'll use 60% more of stock A than we otherwise would. We like A.
            B, positiveMultiplier(0.3),    // We'll only use 30% of the normal amount of B, because we hate B.
                                           // No tilt specified for C, so we will use the default value of 1.0.
            D, positiveMultiplier(0.7)))); // We'll only use 70% of the normal amount of D, because we dislike D
                                           // (though not as much as we dislike B).

    // after the tilts, the percentages sum to 80%, but we have to normalize the result so that all % sum to 100%.
    double denominator = doubleExplained(0.8,
        0.1 * 1.6 +
        0.2 * 0.3 +
        0.3 +        // no tilt for C, i.e. using default tilt of 1
        0.4 * 0.7);

    assertThat(
        tiltedPartition,
        // After tilting, the index will be 20% A, 7.5% B, 37.5% C, 35% D.
        partitionMatcher(partition(rbMapOf(
            A, unitFraction(doubleExplained(0.20,  0.1 * 1.6 / denominator)),
            B, unitFraction(doubleExplained(0.075, 0.2 * 0.3 / denominator)),
            C, unitFraction(doubleExplained(0.375, 0.3       / denominator)),
            D, unitFraction(doubleExplained(0.35,  0.4 * 0.7 / denominator))))));
  }

  @Override
  protected PartitionTilter makeTestObject() {
    return new PartitionTilter();
  }

}
