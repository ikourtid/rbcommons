package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.IntConsumer;

import static com.rb.nonbiz.collections.ArrayIndexMappingTest.arrayIndexMappingMatcher;
import static com.rb.nonbiz.collections.BitSet2D.bitSet2D;
import static com.rb.nonbiz.collections.BitSet2DTest.bitSet2DMatcher;
import static com.rb.nonbiz.collections.MutableIndexableBitSet2D.mutableIndexableBitSet2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

/**
 * The test is generic, but the publicly exposed matcher (which gets used elsewhere) isn't, so that's good.
 */
public class MutableIndexableBitSet2DTest extends RBTestMatcher<MutableIndexableBitSet2D<String, Boolean>> {

  @Test
  public void testSetAssumingOff() {
    BitSet2D bitSet2D = bitSet2D(1, 1);
    bitSet2D.setAssumingOff(0, 0);
    bitSet2D.set(0, 0);
    assertIllegalArgumentException( () -> bitSet2D.setAssumingOff(0, 0));
  }

  @Test
  public void testCardinality() {
    BitSet2D bitSet2D = bitSet2D(3, 3);
    IntConsumer cardinalityGetter = expectedCardinality -> assertEquals(
        expectedCardinality,
        mutableIndexableBitSet2D(
            bitSet2D,
            simpleArrayIndexMapping("a", "b", "c"),
            simpleArrayIndexMapping(false, true))
            .cardinality());
    cardinalityGetter.accept(0);
    bitSet2D.set(0, 2);
    cardinalityGetter.accept(1);
    bitSet2D.set(1, 1);
    cardinalityGetter.accept(2);
    bitSet2D.set(2, 0);
    cardinalityGetter.accept(3);
    bitSet2D.set(2, 2);
    cardinalityGetter.accept(4);
    bitSet2D.set(2, 2);
    cardinalityGetter.accept(4);
  }

  @Override
  public MutableIndexableBitSet2D<String, Boolean> makeTrivialObject() {
    return mutableIndexableBitSet2D(
        bitSet2D(1, 1),
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  @Override
  public MutableIndexableBitSet2D<String, Boolean> makeNontrivialObject() {
    BitSet2D bitSet2D = bitSet2D(3, 3);
    bitSet2D.set(0, 2);
    bitSet2D.set(1, 1);
    bitSet2D.set(2, 0);
    return mutableIndexableBitSet2D(
        bitSet2D,
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  public MutableIndexableBitSet2D<String, Boolean> makeMatchingNontrivialObject() {
    BitSet2D bitSet2D = bitSet2D(3, 3);
    bitSet2D.set(0, 2);
    bitSet2D.set(1, 1);
    bitSet2D.set(2, 0);
    return mutableIndexableBitSet2D(
        bitSet2D,
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(false, true));
  }

  @Override
  protected boolean willMatch(MutableIndexableBitSet2D<String, Boolean> expected,
                              MutableIndexableBitSet2D<String, Boolean> actual) {
    return mutableIndexableBitSet2DMatcher(expected).matches(actual);
  }

  public static <R, C> TypeSafeMatcher<MutableIndexableBitSet2D<R, C>> mutableIndexableBitSet2DMatcher(
      MutableIndexableBitSet2D<R, C> expected) {
    return makeMatcher(expected,
        match(v -> v.getRawBitSet(),     f -> bitSet2DMatcher(f)),
        match(v -> v.getRowMapping(),    f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))),
        match(v -> v.getColumnMapping(), f -> arrayIndexMappingMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

}
