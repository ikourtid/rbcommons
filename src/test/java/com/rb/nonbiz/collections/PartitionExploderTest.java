package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.biz.marketdata.FakeInstruments.ETF_1;
import static com.rb.biz.marketdata.FakeInstruments.ETF_2;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_E;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.PartitionTest.partitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class PartitionExploderTest extends RBTest<PartitionExploder> {

  @Test
  public void generalCase_twoSubPartitions_onlyPartialOverlap() {
    assertThat(
        makeTestObject().explode(
            partition(rbMapOf(
                STOCK_A, unitFractionInPct(1),
                STOCK_B, unitFractionInPct(3),
                STOCK_C, unitFractionInPct(7),
                STOCK_D, unitFractionInPct(19),
                ETF_1,   unitFraction(0.2),
                ETF_2,   unitFraction(0.5))),
            rbMapOf(
                ETF_1, partition(rbMapOf(
                    STOCK_A, unitFractionInPct(15),
                    STOCK_C, unitFractionInPct(85))),
                ETF_2, partition(rbMapOf(
                    STOCK_A, unitFractionInPct(30),
                    STOCK_B, unitFractionInPct(60),
                    STOCK_E, unitFractionInPct(10))))),
        partitionMatcher(
            partition(rbMapOf(
                // Terms arranged in columns: in top-level partition; contribution from ETF_1; and from ETF_2.
                STOCK_A, unitFractionInPct(1 + 0.2 * 15 + 0.5 * 30),
                STOCK_B, unitFractionInPct(3            + 0.5 * 60),
                STOCK_C, unitFractionInPct(7 + 0.2 * 85),
                STOCK_D, unitFractionInPct(19),
                STOCK_E, unitFractionInPct(               0.5 * 10)))));
  }

  @Override
  protected PartitionExploder makeTestObject() {
    return new PartitionExploder();
  }

}
