package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.BitSet2D.bitSet2D;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BitSet2DTest extends RBTestMatcher<BitSet2D> {

  @Test
  public void happyPath() {
    BitSet2D bitSet2D = bitSet2D(2, 3);
    bitSet2D.set(1, 0);
    bitSet2D.set(1, 2);
    assertFalse(bitSet2D.get(0, 0));
    assertFalse(bitSet2D.get(0, 1));
    assertFalse(bitSet2D.get(0, 2));
    assertTrue(bitSet2D.get(1, 0));
    assertFalse(bitSet2D.get(1, 1));
    assertTrue(bitSet2D.get(1, 2));
  }

  @Test
  public void cardinality() {
    BitSet2D bitSet2D = bitSet2D(2, 3);
    bitSet2D.set(1, 0);
    bitSet2D.set(1, 2);
    //    0  1  2
    // 0  _  _  _
    // 1  X  _  X
    assertEquals(0, bitSet2D.rowCardinality(0));
    assertEquals(2, bitSet2D.rowCardinality(1));
    assertEquals(1, bitSet2D.columnCardinality(0));
    assertEquals(0, bitSet2D.columnCardinality(1));
    assertEquals(1, bitSet2D.columnCardinality(2));
    assertEquals(2, bitSet2D.cardinality());
  }

  @Test
  public void setAssumingOff() {
    BitSet2D bitSet2D = bitSet2D(1, 1);
    bitSet2D.setAssumingOff(0, 0);
    assertIllegalArgumentException( () -> bitSet2D.setAssumingOff(0, 0));
  }

  @Test
  public void nothingSetYet_allBitsAreFalse() {
    BitSet2D bitSet2D = bitSet2D(2, 3);
    for (int row = 0; row < 2; row++) {
      for (int column = 0; column < 3; column++) {
        assertFalse(bitSet2D.get(row, column));
      }
    }
  }

  @Test
  public void negativeRow_throws() {
    BitSet2D bitSet2D = bitSet2D(2, 3);
    assertIllegalArgumentException( () -> bitSet2D.get(-1, 0));
  }

  @Test
  public void rowTooBig_throws() {
    BitSet2D bitSet2D = bitSet2D(2, 3);
    assertIllegalArgumentException( () -> bitSet2D.get(2, 0));
  }

  @Test
  public void negativeColumn_throws() {
    BitSet2D bitSet2D = bitSet2D(2, 3);
    assertIllegalArgumentException( () -> bitSet2D.get(0, -1));
  }

  @Test
  public void columnTooBig_throws() {
    BitSet2D bitSet2D = bitSet2D(2, 3);
    assertIllegalArgumentException( () -> bitSet2D.get(0, 3));
  }

  @Override
  public BitSet2D makeTrivialObject() {
    return bitSet2D(1, 1);
  }

  @Override
  public BitSet2D makeNontrivialObject() {
    BitSet2D bitSet2D = bitSet2D(3, 3);
    bitSet2D.set(0, 2);
    bitSet2D.set(1, 1);
    bitSet2D.set(2, 0);
    return bitSet2D;
  }

  @Override
  public BitSet2D makeMatchingNontrivialObject() {
    BitSet2D bitSet2D = bitSet2D(3, 3);
    bitSet2D.set(0, 2);
    bitSet2D.set(1, 1);
    bitSet2D.set(2, 0);
    return bitSet2D;
  }

  @Override
  protected boolean willMatch(BitSet2D expected, BitSet2D actual) {
    return bitSet2DMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<BitSet2D> bitSet2DMatcher(BitSet2D expected) {
    return makeMatcher(expected, actual ->
        expected.getNumRows() == actual.getNumRows()
        && expected.getNumColumns() == actual.getNumColumns()
        && expected.getFlatBitSet().equals(actual.getFlatBitSet()));
  }

}
