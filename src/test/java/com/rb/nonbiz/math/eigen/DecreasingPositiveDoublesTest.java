package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRestDoubles;
import static com.rb.nonbiz.testmatchers.Match.matchDoubleList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;

public class DecreasingPositiveDoublesTest extends RBTestMatcher<DecreasingPositiveDoubles> {

  public static DecreasingPositiveDoubles decreasingPositiveDoubles(double first, double...rest) {
    return DecreasingPositiveDoubles.decreasingPositiveDoubles(concatenateFirstAndRestDoubles(first, rest));
  }

  @Test
  public void increasingElements_throws() {
    assertIllegalArgumentException( () -> decreasingPositiveDoubles(1.0, 2.0));

    DecreasingPositiveDoubles doesNotThrow;
    doesNotThrow = decreasingPositiveDoubles(1.0, 1.0);
    doesNotThrow = decreasingPositiveDoubles(2.0, 1.0);
  }

  @Test
  public void nonPositiveElement_throws() {
    assertIllegalArgumentException( () -> decreasingPositiveDoubles(2.0, 1.0, -1e-9));
    assertIllegalArgumentException( () -> decreasingPositiveDoubles(2.0, 1.0, 0.0));
  }

  @Test
  public void emptyDoublesList_throws() {
    assertIllegalArgumentException( () -> DecreasingPositiveDoubles.decreasingPositiveDoubles(emptyList()));
  }

  @Override
  public DecreasingPositiveDoubles makeTrivialObject() {
    return decreasingPositiveDoubles(1.0);
  }

  @Override
  public DecreasingPositiveDoubles makeNontrivialObject() {
    return decreasingPositiveDoubles(1.2345, 0.6789);
  }

  @Override
  public DecreasingPositiveDoubles makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return decreasingPositiveDoubles(1.2345 + e, 0.6789 + e);
  }

  @Override
  protected boolean willMatch(DecreasingPositiveDoubles expected, DecreasingPositiveDoubles actual) {
    return decreasingPositiveDoublesMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<DecreasingPositiveDoubles> decreasingPositiveDoublesMatcher(DecreasingPositiveDoubles expected) {
    return makeMatcher(expected,
        matchDoubleList(v -> v.getRawList(), 1e-8));
  }

}
