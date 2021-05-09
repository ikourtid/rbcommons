package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.vectorspaces.MatrixRowIndex.matrixRowIndex;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class MatrixRowIndexTest extends RBTestMatcher<MatrixRowIndex> {

  @Test
  public void isNegative_throws() {
    assertIllegalArgumentException( () -> matrixRowIndex(-1));
    assertIllegalArgumentException( () -> matrixRowIndex(-999));
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
        matchUsingEquals(v -> v.asInt()));
  }

}