package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.PartitionTest.partitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionItemsRemoverTest extends RBTest<PartitionItemsRemover> {

  private final Partition<String> STARTING_PARTITION = partition(rbMapOf(
      "a1", unitFraction(0.1),
      "a2", unitFraction(0.2),
      "a3", unitFraction(0.3),
      "a4", unitFraction(0.4)));

  @Test
  public void noKeysToRemove_throws() {
    assertIllegalArgumentException( () ->
        makeTestObject().removeItemsFromPartition(
            STARTING_PARTITION, emptyRBSet()));
  }

  @Test
  public void removesAllKeys_throws() {
    assertIllegalArgumentException( () ->
        makeTestObject().removeItemsFromPartition(
            STARTING_PARTITION, rbSetOf("a1", "a2", "a3", "a4")));
  }

  @Test
  public void notAllKeysAreInPartition_throws() {
    assertIllegalArgumentException( () ->
        makeTestObject().removeItemsFromPartition(
            STARTING_PARTITION, rbSetOf("a1", "xyz")));
  }

  @Test
  public void singleItemToRemove_returnsRemainder() {
    assertThat(
        makeTestObject().removeItemsFromPartition(STARTING_PARTITION, singletonRBSet("a1")),
        partitionMatcher(partition(rbMapOf(
            "a2", unitFraction(0.2 / doubleExplained(0.9, 0.2 + 0.3 + 0.4)),
            "a3", unitFraction(0.3 / 0.9),
            "a4", unitFraction(0.4 / 0.9)))));
    assertThat(
        makeTestObject().removeItemsFromPartition(STARTING_PARTITION, singletonRBSet("a2")),
        partitionMatcher(partition(rbMapOf(
            "a1", unitFraction(0.1 / doubleExplained(0.8, 0.1 + 0.3 + 0.4)),
            "a3", unitFraction(0.3 / 0.8),
            "a4", unitFraction(0.4 / 0.8)))));
    assertThat(
        makeTestObject().removeItemsFromPartition(STARTING_PARTITION, singletonRBSet("a3")),
        partitionMatcher(partition(rbMapOf(
            "a1", unitFraction(0.1 / doubleExplained(0.7, 0.1 + 0.2 + 0.4)),
            "a2", unitFraction(0.2 / 0.7),
            "a4", unitFraction(0.4 / 0.7)))));
    assertThat(
        makeTestObject().removeItemsFromPartition(STARTING_PARTITION, singletonRBSet("a4")),
        partitionMatcher(partition(rbMapOf(
            "a1", unitFraction(0.1 / doubleExplained(0.6, 0.1 + 0.2 + 0.3)),
            "a2", unitFraction(0.2 / 0.6),
            "a3", unitFraction(0.3 / 0.6)))));
  }

  @Test
  public void happyPath_multipleItemsToRemove_multipleRemaining_returnsRemainder() {
    assertThat(
        makeTestObject().removeItemsFromPartition(STARTING_PARTITION, rbSetOf("a1", "a2")),
        partitionMatcher(partition(rbMapOf(
            "a3", unitFraction(0.3 / doubleExplained(0.7, 0.3 + 0.4)),
            "a4", unitFraction(0.4 / 0.7)))));
  }

  @Test
  public void happyPath_multipleItemsToRemove_onlyOneRemaining_returnsRemainder() {
    assertThat(
        makeTestObject().removeItemsFromPartition(STARTING_PARTITION, rbSetOf("a1", "a2", "a3")),
        partitionMatcher(singletonPartition("a4")));
  }

  @Override
  protected PartitionItemsRemover makeTestObject() {
    return new PartitionItemsRemover();
  }

}
