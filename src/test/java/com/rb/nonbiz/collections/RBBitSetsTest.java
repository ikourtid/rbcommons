package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.BitSet;
import java.util.List;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBBitSets.bitSetOf;
import static com.rb.nonbiz.collections.RBBitSets.emptyBitSet;
import static com.rb.nonbiz.collections.RBBitSets.filterListUsingBitSet;
import static com.rb.nonbiz.collections.RBBitSets.singletonBitSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBBitSetsTest {

  @Test
  public void testBitSetConstructors() {
    // Note that BitSet is not like a regular collection, where if you specify an index that's too high you'll get some
    // exception. Instead, you'll just get 'false' back. So there's no way (to our knowledge) to create a bitset with
    // e.g. space for 5 bits, and somehow get back '5' from that object. There are methods for size(),
    // length(), and cardinality(), but none of them do this.

    assertEquals(0, emptyBitSet().length());
    assertFalse(emptyBitSet().get(0));
    assertFalse(emptyBitSet().get(1));

    assertFalse(singletonBitSet(false).get(0));
    assertFalse(singletonBitSet(false).get(1));
    assertFalse(singletonBitSet(false).get(2));

    assertTrue( singletonBitSet(true) .get(0));
    assertFalse(singletonBitSet(false).get(1));
    assertFalse(singletonBitSet(false).get(2));

    BitSet bitSet = bitSetOf(true, false, true);
    assertTrue( bitSet.get(0));
    assertFalse(bitSet.get(1));
    assertTrue( bitSet.get(2));
    assertFalse(bitSet.get(3));
  }

  @Test
  public void testFilterListUsingBitSet() {
    assertEquals(emptyList(), filterListUsingBitSet(emptyList(), emptyBitSet()));
    // singletonBitSet(false) behaves like emptyBitSet(); hard to avoid this, given how BitSet works,
    // where the only notion of length seems to be the distance to the last bit that's set to true.
    assertEquals(emptyList(), filterListUsingBitSet(emptyList(), singletonBitSet(false)));
    assertEquals(emptyList(), filterListUsingBitSet(emptyList(), bitSetOf(false, false)));

    assertIllegalArgumentException( () -> filterListUsingBitSet(emptyList(), singletonBitSet(true)));

    Function<BitSet, List<String>> maker = bitSet -> filterListUsingBitSet(ImmutableList.of("a", "b", "c"), bitSet);

    rbSetOf(
        singletonBitSet(true),
        bitSetOf(true, false),
        bitSetOf(true, false, false),
        bitSetOf(true, false, false, false))
        .forEach(bitSet -> assertEquals(singletonList("a"), maker.apply(bitSet)));

    rbSetOf(
        bitSetOf(true, true), // See above. This becaves just like bitSetOf(true, true, false) below
        bitSetOf(true, true, false),
        bitSetOf(true, true, false, false))
        .forEach(bitSet -> assertEquals(ImmutableList.of("a", "b"), maker.apply(bitSet)));

    rbSetOf(
        bitSetOf(false, true),
        bitSetOf(false, true, false),
        bitSetOf(false, true, false, false))
        .forEach(bitSet -> assertEquals(singletonList("b"), maker.apply(bitSet)));

    rbSetOf(
        bitSetOf(false, true, true),
        bitSetOf(false, true, true, false))
        .forEach(bitSet -> assertEquals(ImmutableList.of("b", "c"), maker.apply(bitSet)));

    rbSetOf(
        bitSetOf(true, false, true),
        bitSetOf(true, false, true, false))
        .forEach(bitSet -> assertEquals(ImmutableList.of("a", "c"), maker.apply(bitSet)));

    rbSetOf(
        bitSetOf(true, true, true),
        bitSetOf(true, true, true, false))
        .forEach(bitSet -> assertEquals(ImmutableList.of("a", "b", "c"), maker.apply(bitSet)));

    // BitSet is too large
    assertIllegalArgumentException( () -> maker.apply(bitSetOf(true, true, true, true)));
    assertIllegalArgumentException( () -> maker.apply(bitSetOf(false, true, false, true)));
    assertIllegalArgumentException( () -> maker.apply(bitSetOf(true, false, true, true)));
  }

}
