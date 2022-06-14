package com.rb.nonbiz.text;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.text.RBSetOfHasUniqueId.rbSetOfHasUniqueId;
import static com.rb.nonbiz.text.RBSetsOfHasUniqueId.rbSetOfHasUniqueIdToRBMap;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueId;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueIdMatcher;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBSetsOfHasUniqueIdTest extends RBTest<RBSetOfHasUniqueId<TestHasUniqueId>> {

  @Test
  public void testRBSetOfHasUniqueIdToRBMap() {
    UniqueId<TestHasUniqueId> id1 = uniqueId("id1");
    UniqueId<TestHasUniqueId> id2 = uniqueId("id2");

    TestHasUniqueId testHasUniqueId1 = testHasUniqueId(id1, unitFraction(0.111));
    TestHasUniqueId testHasUniqueId2 = testHasUniqueId(id2, unitFraction(0.222));

    RBSetOfHasUniqueId<TestHasUniqueId> setOfTests = rbSetOfHasUniqueId(
        ImmutableList.of(
            testHasUniqueId1,
            testHasUniqueId2));

    assertThat(
        rbSetOfHasUniqueIdToRBMap(setOfTests),
        rbMapMatcher(rbMapOf(
                id1, testHasUniqueId1,
                id2, testHasUniqueId2),
            f -> testHasUniqueIdMatcher(f)));
  }

  @Override
  protected RBSetOfHasUniqueId<TestHasUniqueId> makeTestObject() {
    return makeRealObject(RBSetOfHasUniqueId.class);
  }

}