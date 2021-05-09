package com.rb.nonbiz.text;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.RBSetOfHasUniqueId.emptyRBSetOfHasUniqueId;
import static com.rb.nonbiz.text.RBSetOfHasUniqueId.rbSetOfHasUniqueId;
import static com.rb.nonbiz.text.RBSetsOfHasUniqueId.rbSetOfHasUniqueIdOf;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueId;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueIdMatcher;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static com.rb.nonbiz.types.UnitFraction.DUMMY_UNIT_FRACTION;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RBSetOfHasUniqueIdTest extends RBTestMatcher<RBSetOfHasUniqueId<TestHasUniqueId>> {

  @Test
  public void keysDoNotMatchValues_throws() {
    assertIllegalArgumentException( () -> rbSetOfHasUniqueId(singletonRBMap(
        uniqueId("idB"), testHasUniqueId(uniqueId("idA"), DUMMY_UNIT_FRACTION))));
    assertIllegalArgumentException( () -> rbSetOfHasUniqueId(rbMapOf(
        uniqueId("idB"), testHasUniqueId(uniqueId("idA"), DUMMY_UNIT_FRACTION),
        uniqueId("idA"), testHasUniqueId(uniqueId("idB"), DUMMY_UNIT_FRACTION))));
  }

  @Test
  public void testIsEmptyAndSize() {
    assertEquals(0, emptyRBSetOfHasUniqueId().size());
    assertTrue(emptyRBSetOfHasUniqueId().isEmpty());

    RBSetOfHasUniqueId<TestHasUniqueId> size2 = rbSetOfHasUniqueIdOf(
        testHasUniqueId(uniqueId("idA"), DUMMY_UNIT_FRACTION),
        testHasUniqueId(uniqueId("idB"), DUMMY_UNIT_FRACTION));
    assertEquals(2, size2.size());
    assertFalse(size2.isEmpty());
  }

  @Test
  public void testOrderedStream() {
    Collections2.permutations(ImmutableList.<UniqueId<TestHasUniqueId>>of(
        uniqueId("idA"), uniqueId("idB"), uniqueId("idC"), uniqueId("idD")))
        .forEach(list ->
            assertThat(
                "Whichever way we permute the unique IDs, the ordered stream should come out the same",
                rbSetOfHasUniqueId(ImmutableList.of(
                    testHasUniqueId(list.get(0), unitFraction(0.123)),
                    testHasUniqueId(list.get(1), unitFraction(0.123)),
                    testHasUniqueId(list.get(2), unitFraction(0.123)),
                    testHasUniqueId(list.get(3), unitFraction(0.123))))
                    .orderedStream()
                    .collect(Collectors.toList()),
                orderedListMatcher(
                    ImmutableList.of(
                        testHasUniqueId(uniqueId("idA"), unitFraction(0.123)),
                        testHasUniqueId(uniqueId("idB"), unitFraction(0.123)),
                        testHasUniqueId(uniqueId("idC"), unitFraction(0.123)),
                        testHasUniqueId(uniqueId("idD"), unitFraction(0.123))),
                    f -> testHasUniqueIdMatcher(f))));
  }

  @Override
  public RBSetOfHasUniqueId<TestHasUniqueId> makeTrivialObject() {
    return emptyRBSetOfHasUniqueId();
  }

  @Override
  public RBSetOfHasUniqueId<TestHasUniqueId> makeNontrivialObject() {
    return rbSetOfHasUniqueIdOf(
        testHasUniqueId(uniqueId("idA"), unitFraction(0.11)),
        testHasUniqueId(uniqueId("idB"), unitFraction(0.22)),
        testHasUniqueId(uniqueId("idC"), unitFraction(0.33)));
  }

  @Override
  public RBSetOfHasUniqueId<TestHasUniqueId> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbSetOfHasUniqueIdOf(
        testHasUniqueId(uniqueId("idA"), unitFraction(0.11 + e)),
        testHasUniqueId(uniqueId("idB"), unitFraction(0.22 + e)),
        testHasUniqueId(uniqueId("idC"), unitFraction(0.33 + e)));
  }

  @Override
  protected boolean willMatch(RBSetOfHasUniqueId<TestHasUniqueId> expected, RBSetOfHasUniqueId<TestHasUniqueId> actual) {
    return rbSetOfHasUniqueIdMatcher(expected, f -> testHasUniqueIdMatcher(f)).matches(actual);
  }

  public static <V extends HasUniqueId<V>> TypeSafeMatcher<RBSetOfHasUniqueId<V>> rbSetOfHasUniqueIdMatcher(
      RBSetOfHasUniqueId<V> expected, MatcherGenerator<V> valuesMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawMap(), f -> rbMapMatcher(f, valuesMatcherGenerator)));
  }

}
