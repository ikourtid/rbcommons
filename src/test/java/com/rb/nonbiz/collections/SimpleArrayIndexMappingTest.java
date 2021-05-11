package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class SimpleArrayIndexMappingTest {

  @Test
  public void collectionHasGuaranteedOrder_mappingPreservesOrder() {
    SimpleArrayIndexMapping<String> mapping = simpleArrayIndexMapping(ImmutableList.of("A", "B", "C"));
    assertEquals("A", mapping.getKey(0));
    assertEquals("B", mapping.getKey(1));
    assertEquals("C", mapping.getKey(2));
    assertEquals(0, mapping.getIndex("A"));
    assertEquals(1, mapping.getIndex("B"));
    assertEquals(2, mapping.getIndex("C"));
  }

  @Test
  public void objectsAreInAnArray_mappingPreservesOrder() {
    SimpleArrayIndexMapping<String> mapping = simpleArrayIndexMapping(new String[] { "A", "B", "C" });
    assertEquals("A", mapping.getKey(0));
    assertEquals("B", mapping.getKey(1));
    assertEquals("C", mapping.getKey(2));
    assertEquals(0, mapping.getIndex("A"));
    assertEquals(1, mapping.getIndex("B"));
    assertEquals(2, mapping.getIndex("C"));
  }

  @Test
  public void collectionDoesNotHaveGuaranteedOrder_mappingChoosesSomeOrder() {
    SimpleArrayIndexMapping<String> mapping = simpleArrayIndexMapping(ImmutableSet.of("A", "B", "C"));
    assertEquals(
        ImmutableSet.of("A", "B", "C"),
        ImmutableSet.of(mapping.getKey(0), mapping.getKey(1), mapping.getKey(2)));
    assertEquals(
        ImmutableSet.of(0, 1, 2),
        ImmutableSet.of(mapping.getIndex("A"), mapping.getIndex("B"), mapping.getIndex("C")));
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