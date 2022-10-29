package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.jmock.Expectations;
import org.junit.Test;

import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static com.rb.nonbiz.collections.DetailedPartitionModificationTest.testDetailedStringPartitionModificationWithSeed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jmock.AbstractExpectations.same;

public class MultipleDetailedPartitionModificationsApplierTest extends RBTest<MultiplePartitionModificationsApplier> {

  SinglePartitionModificationApplier singlePartitionModificationApplier =
      mockery.mock(SinglePartitionModificationApplier.class);

  @Test
  public void generalCase_makesConsecutivePartitionModifications() {
    DetailedPartitionModification<String> someModification1 = testDetailedStringPartitionModificationWithSeed(0.001);
    DetailedPartitionModification<String> someModification2 = testDetailedStringPartitionModificationWithSeed(0.002);
    DetailedPartitionModification<String> someModification3 = testDetailedStringPartitionModificationWithSeed(0.003);

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
      DetailedPartitionModification<String> someDetailedPartitionModification,
      Partition<String> someExpectedPartition) {
    mockery.checking(new Expectations() {{
      oneOf(singlePartitionModificationApplier).apply(
          with(same(someOriginalPartition)),
          with(same(someDetailedPartitionModification)));
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
