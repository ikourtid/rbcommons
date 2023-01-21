package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.nonbiz.collections.ArrayIndexMappingTest.arrayIndexMappingMatcher;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMappingClosedRange;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMappingFromZeroWithSizeN;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class SimpleArrayIndexMappingTest {

  @Test
  public void collectionHasGuaranteedOrder_mappingPreservesOrder() {
    SimpleArrayIndexMapping<String> mapping = simpleArrayIndexMapping(ImmutableList.of("A", "B", "C"));
    assertEquals("A", mapping.getKey(0));
    assertEquals("B", mapping.getKey(1));
    assertEquals("C", mapping.getKey(2));
    assertEquals(0, mapping.getIndexOrThrow("A"));
    assertEquals(1, mapping.getIndexOrThrow("B"));
    assertEquals(2, mapping.getIndexOrThrow("C"));
  }

  @Test
  public void objectsAreInAnArray_mappingPreservesOrder() {
    SimpleArrayIndexMapping<String> mapping = simpleArrayIndexMapping(new String[] { "A", "B", "C" });
    assertEquals("A", mapping.getKey(0));
    assertEquals("B", mapping.getKey(1));
    assertEquals("C", mapping.getKey(2));
    assertEquals(0, mapping.getIndexOrThrow("A"));
    assertEquals(1, mapping.getIndexOrThrow("B"));
    assertEquals(2, mapping.getIndexOrThrow("C"));
  }

  @Test
  public void collectionDoesNotHaveGuaranteedOrder_mappingChoosesSomeOrder() {
    SimpleArrayIndexMapping<String> mapping = simpleArrayIndexMapping(ImmutableSet.of("A", "B", "C"));
    assertEquals(
        ImmutableSet.of("A", "B", "C"),
        ImmutableSet.of(mapping.getKey(0), mapping.getKey(1), mapping.getKey(2)));
    assertEquals(
        ImmutableSet.of(0, 1, 2),
        ImmutableSet.of(mapping.getIndexOrThrow("A"), mapping.getIndexOrThrow("B"), mapping.getIndexOrThrow("C")));
  }

  public void testSimpleArrayIndexMappingClosedRange() {
    BiConsumer<ClosedRange<Integer>, ArrayIndexMapping<Integer>> asserter = (range, expectedResult) ->
        assertThat(
            simpleArrayIndexMappingClosedRange(range, x -> x),
            arrayIndexMappingMatcher(
                expectedResult, f -> typeSafeEqualTo(f)));
    asserter.accept(closedRange( 0, 0), simpleArrayIndexMapping(0));
    asserter.accept(closedRange(2, 2), simpleArrayIndexMapping(2));
    asserter.accept(closedRange(-1, 1), simpleArrayIndexMapping(-1, 0, 1));
    asserter.accept(closedRange( 0, 4), simpleArrayIndexMapping( 0, 1, 2, 3, 4));
    asserter.accept(closedRange( 2, 5), simpleArrayIndexMapping( 2, 3, 4, 5));
  }

  @Test
  public void testSimpleArrayIndexMappingFromZeroWithSizeN() {
    assertIllegalArgumentException( () -> simpleArrayIndexMappingFromZeroWithSizeN(-1, x -> x));
    assertIllegalArgumentException( () -> simpleArrayIndexMappingFromZeroWithSizeN(0, x -> x));
    BiConsumer<Integer, ArrayIndexMapping<Integer>> asserter = (size, expectedResult) ->
        assertThat(
            simpleArrayIndexMappingFromZeroWithSizeN(size, x -> x),
            arrayIndexMappingMatcher(
                expectedResult, f -> typeSafeEqualTo(f)));
    asserter.accept(1, simpleArrayIndexMapping(0));
    asserter.accept(2, simpleArrayIndexMapping(0, 1));
    asserter.accept(3, simpleArrayIndexMapping(0, 1, 2));

    // Test non-identity function on Strings
    SimpleArrayIndexMapping<String> times2Mapping = simpleArrayIndexMappingFromZeroWithSizeN(6, x -> "_" + 2 * x);
    assertEquals("_10", times2Mapping.getLast());
    assertEquals("_0", times2Mapping.getFirst());
    assertEquals("_6", times2Mapping.getKey(3));
    assertEquals(6, times2Mapping.size());
    // Reverse lookup
    assertEquals(3, times2Mapping.getIndexOrThrow("_6"));
  }

  @Test
  public void noObjectsInMapping_throws() {
    assertIllegalArgumentException( () -> simpleArrayIndexMapping(emptyList()));
  }

  @Test
  public void keyAppearsTwice_throws() {
    assertIllegalArgumentException( () -> simpleArrayIndexMapping(ImmutableList.of("a", "a")));
    assertIllegalArgumentException( () -> simpleArrayIndexMapping(ImmutableList.of("a", "b", "a")));
  }

}