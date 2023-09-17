package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBBitSets.bitSetOf;
import static com.rb.nonbiz.collections.RBBitSets.emptyBitSet;
import static com.rb.nonbiz.collections.RBBitSets.filterListUsingBitSet;
import static com.rb.nonbiz.collections.RBBitSets.singletonBitSet;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import java.util.BitSet;
import java.util.List;
import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

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
    assertIllegalArgumentException( () -> filterListUsingBitSet(emptyList(), singletonBitSet(true)));
    assertIllegalArgumentException( () -> filterListUsingBitSet(emptyList(), singletonBitSet(false)));

    Function<BitSet, List<String>> maker = bitSet -> filterListUsingBitSet(ImmutableList.of("a", "b", "c"), bitSet);
    assertIllegalArgumentException( () -> maker.apply(bitSetOf(true,  true)));
    assertIllegalArgumentException( () -> maker.apply(bitSetOf(true,  false)));
    assertIllegalArgumentException( () -> maker.apply(bitSetOf(false, true)));
    assertIllegalArgumentException( () -> maker.apply(bitSetOf(false, false)));
    assertIllegalArgumentException( () -> maker.apply(bitSetOf(true, true, true, true)));
    assertIllegalArgumentException( () -> maker.apply(bitSetOf(false, true, false, true)));
    assertIllegalArgumentException( () -> maker.apply(bitSetOf(true, false, true, false)));

    assertEquals(emptyList(),                     maker.apply(bitSetOf(false, false, false)));
    assertEquals(ImmutableList.of("a", "c"),      maker.apply(bitSetOf(true, false, true)));
    assertEquals(singletonList("b"),              maker.apply(bitSetOf(false, true, false)));
    assertEquals(ImmutableList.of("a", "b", "c"), maker.apply(bitSetOf(true, true, true)));
  }

}
