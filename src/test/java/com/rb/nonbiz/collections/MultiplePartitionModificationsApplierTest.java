package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.jmock.Expectations;
import org.junit.Test;

import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.PartitionModificationTest.testStringPartitionModificationWithSeed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jmock.AbstractExpectations.same;

public class MultiplePartitionModificationsApplierTest extends RBTest<MultiplePartitionModificationsApplier> {

  SinglePartitionModificationApplier singlePartitionModificationApplier =
      mockery.mock(SinglePartitionModificationApplier.class);

  @Test
  public void generalCase_makesConsecutivePartitionModifications() {
    PartitionModification<String> someModification1 = testStringPartitionModificationWithSeed(0.001);
    PartitionModification<String> someModification2 = testStringPartitionModificationWithSeed(0.002);
    PartitionModification<String> someModification3 = testStringPartitionModificationWithSeed(0.003);

    Partition<String> someOriginalPartition = singletonPartition("o");
    Partition<String> somePartitionAfterModification1 = singletonPartition("p1");
    Partition<String> somePartitionAfterModification2 = singletonPartition("p2");
    Partition<String> somePartitionAfterModification3 = singletonPartition("p3");

    expectSingleApplication(someOriginalPartition,           someModification1, somePartitionAfterModification1);
    expectSingleApplication(somePartitionAfterModification1, someModification2, somePartitionAfterModification2);
    expectSingleApplication(somePartitionAfterModification2, someModification3, somePartitionAfterModification3);

    assertThat(
        makeTestObject().apply(
            someOriginalPartition,
            someModification1,
            someModification2,
            someModification3),
        same(somePartitionAfterModification3));
  }

  private void expectSingleApplication(
      Partition<String> someOriginalPartition,
      PartitionModification<String> somePartitionModification,
      Partition<String> someExpectedPartition) {
    mockery.checking(new Expectations() {{
      oneOf(singlePartitionModificationApplier).apply(
          with(same(someOriginalPartition)),
          with(same(somePartitionModification)));
      will(returnValue(someExpectedPartition));
    }});
  }

  @Override
  protected MultiplePartitionModificationsApplier makeTestObject() {
    MultiplePartitionModificationsApplier testObject = new MultiplePartitionModificationsApplier();
    testObject.singlePartitionModificationApplier = singlePartitionModificationApplier;
    return testObject;
  }

}
