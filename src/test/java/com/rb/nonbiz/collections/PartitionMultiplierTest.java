package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertEquals;

public class PartitionMultiplierTest extends RBTest<PartitionMultiplier> {

  @Test
  public void happyPath() {
    assertEquals(
        rbMapOf(
            "a", money(100),
            "b", money(400)),
        makeTestObject().multiplyPartitionBy(
            partition(rbMapOf(
                "a", unitFraction(0.2),
                "b", unitFraction(0.8))),
            money(500),
            bd -> money(bd)));
  }

  @Test
  public void allocatesFullyForSingletonPartitions() {
    assertEquals(
        singletonRBMap(
            "a", money(1_000)),
        makeTestObject().multiplyPartitionBy(
            singletonPartition("a"),
            money(1_000),
            bd -> money(bd)));
  }

  @Test
  public void valueIsZero_works() {
    assertEquals(
        singletonRBMap(
            "a", ZERO_MONEY),
        makeTestObject().multiplyPartitionBy(
            singletonPartition("a"),
            ZERO_MONEY,
            bd -> money(bd)));
  }

  @Override
  protected PartitionMultiplier makeTestObject() {
    return new PartitionMultiplier();
  }
}