package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.eigen.EigenDimensionIndex.eigenDimensionIndex;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class EigenDimensionIndexTest extends RBTestMatcher<EigenDimensionIndex> {

  @Test
  public void implementsEquals() {
    assertEquals(makeTrivialObject(), makeTrivialObject());
    assertEquals(makeNontrivialObject(), makeNontrivialObject());
  }

  @Test
  public void indexMustBeValid_otherwiseThrows() {
    assertIllegalArgumentException( () -> eigenDimensionIndex(-123));
    assertIllegalArgumentException( () -> eigenDimensionIndex(-1));
    EigenDimensionIndex doesNotThrow;
    doesNotThrow = eigenDimensionIndex(0);
    doesNotThrow = eigenDimensionIndex(1);
    doesNotThrow = eigenDimensionIndex(100);
    assertIllegalArgumentException( () -> eigenDimensionIndex(1_000));
  }

  @Override
  public EigenDimensionIndex makeTrivialObject() {
    return eigenDimensionIndex(0);
  }

  @Override
  public EigenDimensionIndex makeNontrivialObject() {
    return eigenDimensionIndex(123);
  }

  @Override
  public EigenDimensionIndex makeMatchingNontrivialObject() {
    return eigenDimensionIndex(123);
  }

  @Override
  protected boolean willMatch(EigenDimensionIndex expected, EigenDimensionIndex actual) {
    return eigenDimensionIndexMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<EigenDimensionIndex> eigenDimensionIndexMatcher(EigenDimensionIndex expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getRawEigenDimensionIndex()));
  }

}
