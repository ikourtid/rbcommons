package com.rb.nonbiz.math.vectorspaces;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.vectorspaces.MatrixColumnIndex.matrixColumnIndex;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class MatrixColumnIndexTest extends RBTestMatcher<MatrixColumnIndex> {

  @Test
  public void isNegative_throws() {
    assertIllegalArgumentException( () -> matrixColumnIndex(-1));
    assertIllegalArgumentException( () -> matrixColumnIndex(-999));
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
