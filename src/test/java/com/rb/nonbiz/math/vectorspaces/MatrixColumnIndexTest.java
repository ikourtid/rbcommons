package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MatrixColumnIndexTest extends RBTestMatcher<MatrixColumnIndex> {

  @Test
  public void isNegative_throws() {
    assertIllegalArgumentException( () -> matrixColumnIndex(-1));
    assertIllegalArgumentException( () -> matrixColumnIndex(-999));
  }

  @Test
  public void testEquals() {
    assertEquals(matrixColumnIndex(0), matrixColumnIndex(0));
    assertEquals(matrixColumnIndex(1), matrixColumnIndex(1));
    assertEquals(matrixColumnIndex(11), matrixColumnIndex(11));
    assertNotEquals(matrixColumnIndex(0), matrixColumnIndex(1));
    assertNotEquals(matrixColumnIndex(1), matrixColumnIndex(0));
    // Below the values match but classes don't.
    assertNotEquals(matrixRowIndex(1), matrixColumnIndex(1));
    assertNotEquals(matrixColumnIndex(1), matrixRowIndex(1));
  }

  @Test
  public void testCompareTo(){
    assertEquals( 0, matrixColumnIndex(1).compareTo(matrixColumnIndex(1)));
    assertEquals( 1, matrixColumnIndex(2).compareTo(matrixColumnIndex(1)));
    assertEquals(-1, matrixColumnIndex(1).compareTo(matrixColumnIndex(2)));
  }

  @Test
  public void testHashcode() {
    assertEquals(0, matrixColumnIndex(0).hashCode());
    assertEquals(2, matrixColumnIndex(2).hashCode());
  }

  @Override
  public MatrixColumnIndex makeTrivialObject() {
    return matrixColumnIndex(0);
  }

  @Override
  public MatrixColumnIndex makeNontrivialObject() {
    return matrixColumnIndex(123);
  }

  @Override
  public MatrixColumnIndex makeMatchingNontrivialObject() {
    return matrixColumnIndex(123);
  }

  @Override
  protected boolean willMatch(MatrixColumnIndex expected, MatrixColumnIndex actual) {
    return matrixColumnIndexMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<MatrixColumnIndex> matrixColumnIndexMatcher(MatrixColumnIndex expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.asInt()));
  }

}
