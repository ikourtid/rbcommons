package com.rb.biz.investing.namedfactormodel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.biz.investing.namedfactormodel.OrthogonalLoadingIndex.orthogonalLoadingIndex;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class OrthogonalLoadingIndexTest extends RBTestMatcher<OrthogonalLoadingIndex> {

  @Test
  public void implementsEquals() {
    assertEquals(makeTrivialObject(), makeTrivialObject());
    assertEquals(makeNontrivialObject(), makeNontrivialObject());
    assertNotEquals(makeTrivialObject(), makeNontrivialObject());
  }

  @Test
  public void indexMustBeValid_otherwiseThrows() {
    assertIllegalArgumentException( () -> orthogonalLoadingIndex(-123));
    assertIllegalArgumentException( () -> orthogonalLoadingIndex(-1));
    OrthogonalLoadingIndex doesNotThrow;
    doesNotThrow = orthogonalLoadingIndex(0);
    doesNotThrow = orthogonalLoadingIndex(1);
    doesNotThrow = orthogonalLoadingIndex(100);
    assertIllegalArgumentException( () -> orthogonalLoadingIndex(1_000));
  }

  @Override
  public OrthogonalLoadingIndex makeTrivialObject() {
    return orthogonalLoadingIndex(0);
  }

  @Override
  public OrthogonalLoadingIndex makeNontrivialObject() {
    return orthogonalLoadingIndex(123);
  }

  @Override
  public OrthogonalLoadingIndex makeMatchingNontrivialObject() {
    return orthogonalLoadingIndex(123);
  }

  @Override
  protected boolean willMatch(OrthogonalLoadingIndex expected, OrthogonalLoadingIndex actual) {
    return orthogonalLoadingIndexMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<OrthogonalLoadingIndex> orthogonalLoadingIndexMatcher(OrthogonalLoadingIndex expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getRawOrthogonalLoadingIndex()));
  }

}
