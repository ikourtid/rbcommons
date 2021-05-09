package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Optional;

import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarTest.highLevelVarMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarWithWeight.highLevelVarWithWeight;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_DOUBLE;

public class HighLevelVarWithWeightTest extends RBTestMatcher<HighLevelVarWithWeight> {

  @Test
  public void weightCannotBeZeroOrNearZero() {
    double e = 1e-9; // epsilon
    TestSuperVar dummySuperVar = new TestSuperVar(DUMMY_DOUBLE, DUMMY_DOUBLE, DUMMY_DOUBLE);
    assertIllegalArgumentException( () -> highLevelVarWithWeight(dummySuperVar, -e));
    assertIllegalArgumentException( () -> highLevelVarWithWeight(dummySuperVar, 0));
    assertIllegalArgumentException( () -> highLevelVarWithWeight(dummySuperVar, e));
  }

  @Override
  public HighLevelVarWithWeight makeTrivialObject() {
    return highLevelVarWithWeight(new TestSuperVar(1.0, 1.0, 0.0), 1.0);
  }

  @Override
  public HighLevelVarWithWeight makeNontrivialObject() {
    return highLevelVarWithWeight(new TestSuperVar(1.1, 2.2, 3.3), 4.4);
  }

  @Override
  public HighLevelVarWithWeight makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return highLevelVarWithWeight(new TestSuperVar(1.1 + e, 2.2 + e, 3.3 + e), 4.4 + e);
  }

  @Override
  protected boolean willMatch(HighLevelVarWithWeight expected, HighLevelVarWithWeight actual) {
    return highLevelVarWithWeightMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<HighLevelVarWithWeight> highLevelVarWithWeightMatcher(HighLevelVarWithWeight expected) {
    return highLevelVarWithWeightMatcher(expected, Optional.empty());
  }

  // We need the 2nd arg to avoid infinite recursion, as an HighLevelVar's constraints may contain itself.
  public static TypeSafeMatcher<HighLevelVarWithWeight> highLevelVarWithWeightMatcher(HighLevelVarWithWeight expected,
                                                                                      Optional<HighLevelVar> doNotMatchThisVar) {
    return makeMatcher(expected, actual ->
        (doNotMatchThisVar.isPresent() && doNotMatchThisVar.get().equals(expected.getHighLevelVar())
            ? true
            : highLevelVarMatcher(expected.getHighLevelVar()).matches(actual.getHighLevelVar()))

            && doubleAlmostEqualsMatcher(expected.getWeight(), 1e-8).matches(actual.getWeight()));
  }

}
