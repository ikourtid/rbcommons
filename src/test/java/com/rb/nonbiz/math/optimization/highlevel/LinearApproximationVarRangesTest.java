package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.ContiguousNonDiscreteRangeCollectionTest.contiguousNonDiscreteRangeCollectionMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRanges.linearApproximationVarRanges;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class LinearApproximationVarRangesTest extends RBTestMatcher<LinearApproximationVarRanges>  {

  @Test
  public void rangeIsTiny_throws() {
    assertIllegalArgumentException( () -> linearApproximationVarRanges(ImmutableList.of(1.1, 1.1 + 1e-9)));
    assertIllegalArgumentException( () -> linearApproximationVarRanges(ImmutableList.of(1.1, 1.2, 1.2 + 1e-9)));
    assertIllegalArgumentException( () -> linearApproximationVarRanges(ImmutableList.of(1.1, 1.1 + 1e-9, 1.2)));
  }

  @Override
  public LinearApproximationVarRanges makeTrivialObject() {
    return linearApproximationVarRanges(ImmutableList.of(1.1, 2.2));
  }

  @Override
  public LinearApproximationVarRanges makeNontrivialObject() {
    return linearApproximationVarRanges(ImmutableList.of(1.1, 2.2, 3.3, 4.4));
  }

  @Override
  public LinearApproximationVarRanges makeMatchingNontrivialObject() {
    double e = 1e-9;
    return linearApproximationVarRanges(ImmutableList.of(1.1 + e, 2.2 + e, 3.3 + e, 4.4 + e));
  }

  @Override
  protected boolean willMatch(LinearApproximationVarRanges expected, LinearApproximationVarRanges actual) {
    return linearApproximationVarRangesMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<LinearApproximationVarRanges> linearApproximationVarRangesMatcher(
      LinearApproximationVarRanges expected) {
    return makeMatcher(expected,
        match(v -> v.getHingePoints(), f -> doubleListMatcher(f, 1e-8)),
        match(v -> v.getRanges(),      f -> contiguousNonDiscreteRangeCollectionMatcher(f,
            f2 -> doubleAlmostEqualsMatcher(f2, 1e-8))));
  }

}
