package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
import static com.rb.nonbiz.testmatchers.Match.matchIntegerValue;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MatrixRowIndexTest extends RBTestMatcher<MatrixRowIndex> {

  @Test
  public void isNegative_throws() {
    assertIllegalArgumentException( () -> matrixRowIndex(-1));
    assertIllegalArgumentException( () -> matrixRowIndex(-999));
  }

  @Test
  public void testCompareTo(){
    assertEquals( 0, matrixRowIndex(1).compareTo(matrixRowIndex(1)));
    assertEquals( 1, matrixRowIndex(2).compareTo(matrixRowIndex(1)));
    assertEquals(-1, matrixRowIndex(1).compareTo(matrixRowIndex(2)));
  }

  @Test
  public void testEquals() {
    assertEquals(matrixRowIndex(0), matrixRowIndex(0));
    assertEquals(matrixRowIndex(1), matrixRowIndex(1));
    assertEquals(matrixRowIndex(11), matrixRowIndex(11));
    assertNotEquals(matrixRowIndex(0), matrixRowIndex(1));
    assertNotEquals(matrixRowIndex(1), matrixRowIndex(0));
    // Below the values match but classes don't.
    assertNotEquals(matrixRowIndex(1), matrixColumnIndex(1));
    assertNotEquals(matrixColumnIndex(1), matrixRowIndex(1));
  }

  @Test
  public void testHashcode() {
    assertEquals(0, matrixRowIndex(0).hashCode());
    assertEquals(2, matrixRowIndex(2).hashCode());
  }

  @Override
  public MatrixRowIndex makeTrivialObject() {
    return matrixRowIndex(0);
  }

  @Override
  public MatrixRowIndex makeNontrivialObject() {
    return matrixRowIndex(123);
  }

  @Override
  public MatrixRowIndex makeMatchingNontrivialObject() {
    return matrixRowIndex(123);
  }

  @Override
  protected boolean willMatch(MatrixRowIndex expected, MatrixRowIndex actual) {
    return matrixRowIndexMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<MatrixRowIndex> matrixRowIndexMatcher(MatrixRowIndex expected) {
    return makeMatcher(expected,
        matchIntegerValue(v -> v));
  }

}