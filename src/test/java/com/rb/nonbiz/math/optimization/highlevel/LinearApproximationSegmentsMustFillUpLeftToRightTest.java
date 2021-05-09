package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.highlevel.ContiguousLinearApproximationIndividualSegmentSuperVarsTest.contiguousLinearApproximationIndividualSegmentSuperVarsMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationSegmentsMustFillUpLeftToRight.linearApproximationSegmentsMustFillUpLeftToRight;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class LinearApproximationSegmentsMustFillUpLeftToRightTest
  extends RBTestMatcher<LinearApproximationSegmentsMustFillUpLeftToRight> {

  @Override
  public LinearApproximationSegmentsMustFillUpLeftToRight makeTrivialObject() {
    return linearApproximationSegmentsMustFillUpLeftToRight(
        new ContiguousLinearApproximationIndividualSegmentSuperVarsTest().makeTrivialObject());
  }

  @Override
  public LinearApproximationSegmentsMustFillUpLeftToRight makeNontrivialObject() {
    return linearApproximationSegmentsMustFillUpLeftToRight(
        new ContiguousLinearApproximationIndividualSegmentSuperVarsTest().makeNontrivialObject());
  }

  @Override
  public LinearApproximationSegmentsMustFillUpLeftToRight makeMatchingNontrivialObject() {
    return linearApproximationSegmentsMustFillUpLeftToRight(
        new ContiguousLinearApproximationIndividualSegmentSuperVarsTest().makeMatchingNontrivialObject());
  }

  @Override
  protected boolean willMatch(LinearApproximationSegmentsMustFillUpLeftToRight expected,
                              LinearApproximationSegmentsMustFillUpLeftToRight actual) {
    return linearApproximationSegmentsMustFillUpLeftToRightMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<LinearApproximationSegmentsMustFillUpLeftToRight>
  linearApproximationSegmentsMustFillUpLeftToRightMatcher(
      LinearApproximationSegmentsMustFillUpLeftToRight expected) {
    return makeMatcher(expected,
        match(v -> v.getContiguousLinearApproximationIndividualSegmentSuperVars(),
            f -> contiguousLinearApproximationIndividualSegmentSuperVarsMatcher(f)));
  }

}
