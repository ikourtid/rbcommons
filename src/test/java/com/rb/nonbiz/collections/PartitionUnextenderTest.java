package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.function.DoubleFunction;

import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.PartitionTest.epsilonPartitionMatcher;
import static com.rb.nonbiz.collections.PartitionTest.partitionMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartitionUnextenderTest extends RBTest<PartitionUnextender> {

  @Test
  public void itemToReduceDoesNotExist_throws() {
    assertIllegalArgumentException( () -> makeTestObject().unextend(
        partition(rbMapOf(
            "a", unitFraction(0.15),
            "b", unitFraction(0.85))),
        "c",
        unitFraction(0.01)));
  }

  @Test
  public void cannotUnextendSingletonPartition() {
    assertIllegalArgumentException( () -> makeTestObject().unextend(singletonPartition("a"), "a", UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> makeTestObject().unextend(singletonPartition("a"), "a", unitFraction(0.12345)));
    assertIllegalArgumentException( () -> makeTestObject().unextend(singletonPartition("a"), "a", UNIT_FRACTION_1));
  }

  @Test
  public void cannotUnextendByZero() {
    DoubleFunction<Partition<String>> unextend = unitFractionOfOldTotal ->
        makeTestObject().unextend(
            partition(rbMapOf(
                "a", unitFraction(0.15),
                "b", unitFraction(0.85))),
            "b",
            unitFraction(unitFractionOfOldTotal));
    assertIllegalArgumentException( () -> unextend.apply(0));
    Partition<String> doesNotThrow = unextend.apply(1e-4);
  }

  @Test
  public void itemToReduceIsBeingReducedTooMuch_throws() {
    DoubleFunction<Partition<String>> unextend = unitFractionOfOldTotal ->
        makeTestObject().unextend(
            partition(rbMapOf(
                "a", unitFraction(0.15),
                "b", unitFraction(0.85))),
            "b",
            unitFraction(unitFractionOfOldTotal));
    Partition<String> doesNotThrow;
    doesNotThrow = unextend.apply(0.84);
    doesNotThrow = unextend.apply(0.85); // also tested in unextendsEntireItem_doesNotThrow_resultExcludesItem
    assertIllegalArgumentException( () -> unextend.apply(0.86));
    assertIllegalArgumentException( () -> unextend.apply(1.00));
  }

  @Test
  public void unextendsEntireItem_doesNotThrow_resultExcludesItem() {
    assertThat(
        makeTestObject().unextend(
            partition(rbMapOf(
                "a", unitFraction(0.15),
                "b", unitFraction(0.85))),
            "b",
            unitFraction(0.85)),
        partitionMatcher(singletonPartition("a")));
  }

  @Test
  public void generalCase_subtracting40pctFromOldTotal() {
    assertThat(
        makeTestObject().unextend(
            partition(rbMapOf(
                "a", unitFraction(0.15),
                "b", unitFraction(0.85))),
            "b",
            unitFraction(0.4)),
        epsilonPartitionMatcher(
            partition(rbMapOf(
                "a", unitFraction(doubleExplained(0.25, 0.15 / (0.15 + doubleExplained(0.45, (0.85 - 0.4))))),
                "b", unitFraction(doubleExplained(0.75, 0.45 / (0.15 + 0.45))))),
            1e-8));
  }

  @Test
  public void partitionExtendingAndUnextendingAreInverse() {
    Partition<String> partition = partition(rbMapOf(
        "a", unitFraction(0.15),
        "b", unitFraction(0.85)));
    assertThat(
        makeRealObject(PartitionExtender.class).extend(
            makeTestObject().unextend(partition, "b", unitFraction(0.2)),
            "b",
            unitFraction(0.2)),
        epsilonPartitionMatcher(partition, 1e-8));
    assertThat(
        makeTestObject().unextend(
            makeRealObject(PartitionExtender.class).extend(partition, "b", unitFraction(0.2)),
            "b",
            unitFraction(0.2)),
        epsilonPartitionMatcher(partition, 1e-8));
  }

  @Test
  public void testCombinedUseOfExtenderAndUnextenderToTweakOnlyTwoItems() {
    Partition<String> original = partition(rbMapOf(
        "a", unitFraction(0.1),
        "b", unitFraction(0.2),
        "c", unitFraction(0.3),
        "d", unitFraction(0.4)));
    Partition<String> mutuallyTweaked = partition(rbMapOf(
        "a", unitFraction(0.1),
        "b", unitFraction(0.2),
        "c", unitFraction(0.29),
        "d", unitFraction(0.41)));

    // If we subtract 1% from C and then add 1% to D, it's as if we did both steps at the same time.
    assertThat(
        makeRealObject(PartitionExtender.class).extend(
            makeTestObject().unextend(original, "c", unitFraction(0.01)),
            "d", unitFraction(0.01)),
        epsilonPartitionMatcher(mutuallyTweaked, 1e-8));
    // If we add 1% to D and then subtract 1% from C, it's NOT the same as if we did both steps at the same time,
    // and it is therefore not the same as the previous. This is non-obvious but makes sense; if we subtract 1% from C
    // FIRST, that removes e.g. $10k from a $1m partition (using some sample amount). However, if we first add 1% of D
    // (i.e. $10k D to a $1m partition), then subtracting 1% C of the new total would be subtracting $10,100 of C.
    assertThat(
        makeTestObject().unextend(
            makeRealObject(PartitionExtender.class).extend(original, "d", unitFraction(0.01)),
            "c", unitFraction(0.01)),
        not(epsilonPartitionMatcher(mutuallyTweaked, 1e-8)));
  }

  @Override
  protected PartitionUnextender makeTestObject() {
    return new PartitionUnextender();
  }

}
