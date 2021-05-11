package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.AssetId;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.biz.types.asset.CashId.CASH;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.PartitionTest.partitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionMergerTest extends RBTest<PartitionMerger> {

  @Test
  public void negativeWeight_throws() {
    assertIllegalArgumentException( () -> makeTestObject().mergePartitions(
        signedMoney(-0.01), singletonPartition(STOCK_A),
        signedMoney(0.01), singletonPartition(STOCK_B)));
  }

  @Test
  public void zeroWeightOnBoth_throws() {
    assertIllegalArgumentException( () -> makeTestObject().mergePartitions(
        ZERO_SIGNED_MONEY, singletonPartition(STOCK_A),
        ZERO_SIGNED_MONEY, singletonPartition(STOCK_B)));
  }

  @Test
  public void zeroWeightOnOne_returnsOther() {
    assertThat(
        makeTestObject().mergePartitions(
            ZERO_SIGNED_MONEY, partition(rbMapOf(
                STOCK_A, unitFraction(0.4),
                STOCK_B, unitFraction(0.6))),
            signedMoney(1.2345), partition(rbMapOf(
                CASH,    unitFraction(0.1),
                STOCK_A, unitFraction(0.9)))),
        partitionMatcher(
            partition(rbMapOf(
                CASH,    unitFraction(0.1),
                STOCK_A, unitFraction(0.9)))));
    assertThat(
        makeTestObject().mergePartitions(
            signedMoney(1.2345), partition(rbMapOf(
                CASH,    unitFraction(0.1),
                STOCK_A, unitFraction(0.9))),
            ZERO_SIGNED_MONEY, partition(rbMapOf(
                STOCK_A, unitFraction(0.4),
                STOCK_B, unitFraction(0.6)))),
        partitionMatcher(
            partition(rbMapOf(
                CASH,    unitFraction(0.1),
                STOCK_A, unitFraction(0.9)))));
  }

  @Test
  public void generalCase_nonZeroWeightOnBoth_returnsMerged() {
    Partition<AssetId> expected = partition(rbMapOf(
        CASH, unitFraction(doubleExplained(0.07, 7_000 * 0.1 / 10_000)),
        STOCK_A, unitFraction(doubleExplained(0.75, (3_000 * 0.4 + 7_000 * 0.9) / 10_000)),
        STOCK_B, unitFraction(doubleExplained(0.18, 3_000 * 0.6 / 10_000))));
    assertThat(
        makeTestObject().mergePartitions(
            money(3_000), partition(rbMapOf(
                STOCK_A, unitFraction(0.4),
                STOCK_B, unitFraction(0.6))),
            money(7_000), partition(rbMapOf(
                CASH,    unitFraction(0.1),
                STOCK_A, unitFraction(0.9)))),
        partitionMatcher(expected));
    assertThat(
        makeTestObject().mergePartitions(
            money(7_000), partition(rbMapOf(
                CASH,    unitFraction(0.1),
                STOCK_A, unitFraction(0.9))),
            money(3_000), partition(rbMapOf(
                STOCK_A, unitFraction(0.4),
                STOCK_B, unitFraction(0.6)))),
        partitionMatcher(expected));
  }

  @Override
  protected PartitionMerger makeTestObject() {
    return new PartitionMerger();
  }

}
