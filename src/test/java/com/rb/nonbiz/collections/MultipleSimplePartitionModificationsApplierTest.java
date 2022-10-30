package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.jmock.Expectations;
import org.junit.Test;

import static com.rb.nonbiz.collections.SimplePartitionModificationTest.testSimpleStringPartitionModificationWithSeed;
import static com.rb.nonbiz.collections.Partition.singletonPartition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jmock.AbstractExpectations.same;

public class MultipleSimplePartitionModificationsApplierTest
    extends RBTest<MultipleSimplePartitionModificationsApplier> {

  SingleSimplePartitionModificationApplier singleSimplePartitionModificationApplier =
      mockery.mock(SingleSimplePartitionModificationApplier.class);

  @Test
  public void generalCase_makesConsecutivePartitionModifications() {
    SimplePartitionModification<String> someModification1 = testSimpleStringPartitionModificationWithSeed(0.001);
    SimplePartitionModification<String> someModification2 = testSimpleStringPartitionModificationWithSeed(0.002);
    SimplePartitionModification<String> someModification3 = testSimpleStringPartitionModificationWithSeed(0.003);

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
      SimplePartitionModification<String> someSimplePartitionModification,
      Partition<String> someExpectedPartition) {
    mockery.checking(new Expectations() {{
      oneOf(singleSimplePartitionModificationApplier).apply(
          with(same(someOriginalPartition)),
          with(same(someSimplePartitionModification)));
      will(returnValue(someExpectedPartition));
    }});
  }

  @Override
  protected MultipleSimplePartitionModificationsApplier makeTestObject() {
    MultipleSimplePartitionModificationsApplier testObject = new MultipleSimplePartitionModificationsApplier();
    testObject.singleSimplePartitionModificationApplier = singleSimplePartitionModificationApplier;
    return testObject;
  }

}
