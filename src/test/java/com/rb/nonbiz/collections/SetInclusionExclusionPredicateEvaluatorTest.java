package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.SetInclusionExclusionPredicateEvaluator.SetInclusionExclusionInstructions;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.collections.SetInclusionExclusionPredicateEvaluator.SetInclusionExclusionInstructions.*;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;

public class SetInclusionExclusionPredicateEvaluatorTest extends RBTest<SetInclusionExclusionPredicateEvaluator> {

  @Test
  public void testIncludeEverything() {
    assertOptionalEquals(true, makeTestObject().mustBeIncluded('a', includeEverything()));
  }

  @Test
  public void testExcludeEverything() {
    assertOptionalEquals(false, makeTestObject().mustBeIncluded('a', excludeEverything()));
  }

  @Test
  public void testUseRulesForEverything() {
    assertOptionalEmpty(makeTestObject().mustBeIncluded('a', useRulesForEverything()));
  }

  @Test
  public void testUseRulesForTheseExcludeRest() {
    assertOptionalEmpty(makeTestObject().mustBeIncluded('a', useRulesForTheseExcludeRest(singletonRBSet('a'))));
    assertOptionalEquals(false, makeTestObject().mustBeIncluded('b', useRulesForTheseExcludeRest(singletonRBSet('a'))));
  }

  @Test
  public void testUseRulesForTheseIncludeRest() {
    assertOptionalEmpty(makeTestObject().mustBeIncluded('a', useRulesForTheseIncludeRest(singletonRBSet('a'))));
    assertOptionalEquals(true, makeTestObject().mustBeIncluded('b', useRulesForTheseIncludeRest(singletonRBSet('a'))));
  }

  @Test
  public void testIncludeTheseExcludeRest() {
    assertOptionalEquals(true, makeTestObject().mustBeIncluded('a', includeTheseExcludeRest(singletonRBSet('a'))));
    assertOptionalEquals(false, makeTestObject().mustBeIncluded('b', includeTheseExcludeRest(singletonRBSet('a'))));
  }

  @Test
  public void testIncludeTheseUseRulesForRest() {
    assertOptionalEquals(true, makeTestObject().mustBeIncluded('a', includeTheseUseRulesForRest(singletonRBSet('a'))));
    assertOptionalEmpty(makeTestObject().mustBeIncluded('b', includeTheseUseRulesForRest(singletonRBSet('a'))));
  }

  @Test
  public void testExcludeTheseIncludeRest() {
    assertOptionalEquals(false, makeTestObject().mustBeIncluded('a', excludeTheseIncludeRest(singletonRBSet('a'))));
    assertOptionalEquals(true, makeTestObject().mustBeIncluded('b', excludeTheseIncludeRest(singletonRBSet('a'))));
  }

  @Test
  public void testExcludeTheseUseRulesForRest() {
    assertOptionalEquals(false, makeTestObject().mustBeIncluded('a', excludeTheseUseRulesForRest(singletonRBSet('a'))));
    assertOptionalEmpty(makeTestObject().mustBeIncluded('b', excludeTheseUseRulesForRest(singletonRBSet('a'))));
  }

  @Test
  public void testIncludeSomeExcludeSomeUseRulesForRest() {
    // i = included, e = excluded
    SetInclusionExclusionInstructions<Character> instructions =
        includeTheseExcludeTheseUseRulesForRest(singletonRBSet('i'), singletonRBSet('e'));
    assertOptionalEquals(true, makeTestObject().mustBeIncluded('i', instructions));
    assertOptionalEquals(false, makeTestObject().mustBeIncluded('e', instructions));
    assertOptionalEmpty(makeTestObject().mustBeIncluded('a', instructions));
  }

  @Override
  protected SetInclusionExclusionPredicateEvaluator makeTestObject() {
    return new SetInclusionExclusionPredicateEvaluator();
  }

}
