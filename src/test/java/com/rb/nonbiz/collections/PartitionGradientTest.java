package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.DoubleFunction;

import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.PartitionGradient.partitionGradient;
import static com.rb.nonbiz.collections.PartitionTest.epsilonPartitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class PartitionGradientTest extends RBTestMatcher<PartitionGradient<String>> {

  @Test
  public void testKeysForBumpedPartitions() {
    Partition<String> ab = partition(rbMapOf(
        "a", unitFraction(0.6),
        "b", unitFraction(0.4)));
    Partition<String> aUpBDown = partition(rbMapOf(
        "a", unitFraction(0.61),
        "b", unitFraction(0.39)));
    Partition<String> aDownBUp = partition(rbMapOf(
        "a", unitFraction(0.59),
        "b", unitFraction(0.41)));
    PartitionGradient<String> doesNotThrow;
    // Bumped up partitions keysets must match
    doesNotThrow = partitionGradient(
        ab,
        unitFraction(0.01),
        rbMapOf("a", aUpBDown, "b", aDownBUp),
        rbMapOf("a", aDownBUp, "b", aUpBDown));
    assertIllegalArgumentException( () -> partitionGradient(
        ab,
        unitFraction(0.01),
        singletonRBMap("a", aUpBDown),
        rbMapOf("a", aDownBUp, "b", aUpBDown)));
    assertIllegalArgumentException( () -> partitionGradient(
        ab,
        unitFraction(0.01),
        rbMapOf("a", aUpBDown, "b", aDownBUp, "c", aDownBUp),
        rbMapOf("a", aDownBUp, "b", aUpBDown)));

    // bumped down partitions won't exist for cases where there isn't enough of something to bump it down,
    // so that keyset just needs to be a subset (not necessarily a proper one) of all keys.
    doesNotThrow = partitionGradient(
        ab,
        unitFraction(0.01),
        rbMapOf("a", aUpBDown, "b", aDownBUp),
        singletonRBMap("a", aDownBUp));
    doesNotThrow = partitionGradient(
        ab,
        unitFraction(0.01),
        rbMapOf("a", aUpBDown, "b", aDownBUp),
        emptyRBMap());
    assertIllegalArgumentException( () -> partitionGradient(
        ab,
        unitFraction(0.01),
        rbMapOf("a", aUpBDown, "b", aDownBUp),
        rbMapOf("a", aDownBUp, "b", aUpBDown, "c", aDownBUp)));
  }

  @Test
  public void allPartitionsMustHaveSameKeySet() {
    Partition<String> partitionA = singletonPartition("a");
    Partition<String> partitionAB = partition(rbMapOf(
        "a", unitFraction(0.6),
        "b", unitFraction(0.4)));
    Partition<String> abc = partition(rbMapOf(
        "a", unitFraction(0.6),
        "b", unitFraction(0.3),
        "c", unitFraction(0.1)));
    // We could assert in the constructor that the bumped up partitions actually are bumped up on the right key,
    // but since we don't, this test can be simpler.
    PartitionGradient<String> doesNotThrow = partitionGradient(
        partitionAB,
        unitFraction(0.01),
        rbMapOf("a", partitionAB, "b", partitionAB),
        rbMapOf("a", partitionAB, "b", partitionAB));
    for (Partition<String> badPartition : rbSetOf(partitionA, abc)) {
      assertIllegalArgumentException( () -> partitionGradient(
          badPartition,
          unitFraction(0.01),
          rbMapOf("a", partitionAB, "b", partitionAB),
          rbMapOf("a", partitionAB, "b", partitionAB)));
      assertIllegalArgumentException( () -> partitionGradient(
          partitionAB,
          unitFraction(0.01),
          rbMapOf("a", badPartition, "b", partitionAB),
          rbMapOf("a", partitionAB, "b", partitionAB)));
      assertIllegalArgumentException( () -> partitionGradient(
          partitionAB,
          unitFraction(0.01),
          rbMapOf("a", partitionAB, "b", partitionAB),
          rbMapOf("a", badPartition, "b", partitionAB)));
    }
  }

  @Test
  public void bumpAmountCannotBeAlmostZeroOrAlmostOne() {
    Partition<String> partitionAB = partition(rbMapOf(
        "a", unitFraction(0.6),
        "b", unitFraction(0.4)));
    DoubleFunction<PartitionGradient<String>> maker = bumpAmount -> partitionGradient(
        partitionAB,
        unitFraction(bumpAmount),
        rbMapOf("a", partitionAB, "b", partitionAB),
        rbMapOf("a", partitionAB, "b", partitionAB));
    assertIllegalArgumentException( () -> maker.apply(0));
    assertIllegalArgumentException( () -> maker.apply(1e-9));
    PartitionGradient<String> doesNotThrow;
    doesNotThrow = maker.apply(0.1);
    doesNotThrow = maker.apply(0.9);
    assertIllegalArgumentException( () -> maker.apply(1 - 1e-9));
    assertIllegalArgumentException( () -> maker.apply(1));
  }

  @Override
  public PartitionGradient<String> makeTrivialObject() {
    // This isn't quite how the gradients would be generated using 0.01, but these numbers are easier to read.
    // PartitionGradient#makeNontrivialObject does it right though.
    return partitionGradient(
        partition(rbMapOf(
            "a", unitFraction(0.6),
            "b", unitFraction(0.4))),
        unitFraction(0.01),
        rbMapOf(
            "a", partition(rbMapOf(
                "a", unitFraction(0.61),
                "b", unitFraction(0.39))),
            "b", partition(rbMapOf(
                "a", unitFraction(0.59),
                "b", unitFraction(0.41)))),
        rbMapOf(
            "a", partition(rbMapOf(
                "a", unitFraction(0.59),
                "b", unitFraction(0.41))),
            "b", partition(rbMapOf(
                "a", unitFraction(0.61),
                "b", unitFraction(0.39)))));
  }

  @Override
  public PartitionGradient<String> makeNontrivialObject() {
    doubleExplained(0.99, 1 - 0.01);
    return partitionGradient(
        partition(rbMapOf(
            "a", unitFraction(0.2),
            "b", unitFraction(0.3),
            "c", unitFraction(0.5))),
        unitFraction(0.01),
        // bumping up the keys; we are not asserting this level of realism in the static constructor,
        // but I might as well make this realistic.
        rbMapOf(
            "a", partition(rbMapOf(
                "a", unitFraction(doubleExplained(0.208, 0.2 * 0.99 + 0.01)),
                "b", unitFraction(doubleExplained(0.297, 0.3 / (0.3 + 0.5) * (1 - 0.208))),
                "c", unitFraction(doubleExplained(0.495, 0.5 / (0.3 + 0.5) * (1 - 0.208))))),
            "b", partition(rbMapOf(
                "a", unitFraction(doubleExplained(0.198, 0.2 / (0.2 + 0.5) * (1 - 0.307))),
                "b", unitFraction(doubleExplained(0.307, 0.3 * 0.99 + 0.01)),
                "c", unitFraction(doubleExplained(0.495, 0.5 / (0.2 + 0.5) * (1 - 0.307))))),
            "c", partition(rbMapOf(
                "a", unitFraction(doubleExplained(0.198, 0.2 / (0.2 + 0.3) * (1 - 0.505))),
                "b", unitFraction(doubleExplained(0.297, 0.3 / (0.2 + 0.3) * (1 - 0.505))),
                "c", unitFraction(doubleExplained(0.505, 0.5 * 0.99 + 0.01))))),
        // bumping down the keys
        rbMapOf(
            "a", partition(rbMapOf(
                "a", unitFraction(doubleExplained(0.191919191, (0.2 - 0.01) / 0.99)),
                "b", unitFraction(doubleExplained(0.303030303, 0.3 / (0.3 + 0.5) * (1 - 0.1919191919))),
                "c", unitFraction(doubleExplained(0.505050505, 0.5 / (0.3 + 0.5) * (1 - 0.1919191919))))),
            "b", partition(rbMapOf(
                "a", unitFraction(doubleExplained(0.202020202, 0.2 / (0.2 + 0.5) * (1 - 0.292929293))),
                "b", unitFraction(doubleExplained(0.292929293, (0.3 - 0.01) / 0.99)),
                "c", unitFraction(doubleExplained(0.505050505, 0.5 / (0.2 + 0.5) * (1 - 0.292929293))))),
            "c", partition(rbMapOf(
                "a", unitFraction(doubleExplained(0.202020202, 0.2 / (0.2 + 0.3) * (1 - 0.494949495))),
                "b", unitFraction(doubleExplained(0.303030303, 0.3 / (0.2 + 0.3) * (1 - 0.494949495))),
                "c", unitFraction(doubleExplained(0.494949495, (0.5 - 0.01) / 0.99))))));
  }

  @Override
  public PartitionGradient<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    doubleExplained(0.99, 1 - 0.01);
    return partitionGradient(
        partition(rbMapOf(
            "a", unitFraction(0.2 + e),
            "b", unitFraction(0.3 + e),
            "c", unitFraction(0.5 + e))),
        unitFraction(0.01 + e),
        rbMapOf(
            "a", partition(rbMapOf(
                "a", unitFraction(0.208 + e),
                "b", unitFraction(0.297 + e),
                "c", unitFraction(0.495 + e))),
            "b", partition(rbMapOf(
                "a", unitFraction(0.198 + e),
                "b", unitFraction(0.307 + e),
                "c", unitFraction(0.495 + e))),
            "c", partition(rbMapOf(
                "a", unitFraction(0.198 + e),
                "b", unitFraction(0.297 + e),
                "c", unitFraction(0.505 + e)))),
        // bumping down the keys
        rbMapOf(
            "a", partition(rbMapOf(
                "a", unitFraction(0.191919191 + e),
                "b", unitFraction(0.303030303 + e),
                "c", unitFraction(0.505050505 + e))),
            "b", partition(rbMapOf(
                "a", unitFraction(0.202020202 + e),
                "b", unitFraction(0.292929293 + e),
                "c", unitFraction(0.505050505 + e))),
            "c", partition(rbMapOf(
                "a", unitFraction(0.202020202 + e),
                "b", unitFraction(0.303030303 + e),
                "c", unitFraction(0.494949495 + e)))));
  }

  @Override
  protected boolean willMatch(PartitionGradient<String> expected, PartitionGradient<String> actual) {
    return partitionGradientMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<PartitionGradient<T>> partitionGradientMatcher(PartitionGradient<T> expected) {
    return makeMatcher(expected,
        match(                 v -> v.getOriginalPartition(),         f -> epsilonPartitionMatcher(f, DEFAULT_EPSILON_1e_8)),
        matchUsingAlmostEquals(v -> v.getBumpAmount(), DEFAULT_EPSILON_1e_8),
        matchRBMap(            v -> v.getPartitionsWhenBumpingUp(),   f -> epsilonPartitionMatcher(f, DEFAULT_EPSILON_1e_8)),
        matchRBMap(            v -> v.getPartitionsWhenBumpingDown(), f -> epsilonPartitionMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

}
