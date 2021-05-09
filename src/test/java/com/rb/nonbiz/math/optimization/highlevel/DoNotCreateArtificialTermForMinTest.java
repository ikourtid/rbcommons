package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBSingleValueClassTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.highlevel.DoNotCreateArtificialTermForMin.doNotCreateArtificialTermForMin;
import static com.rb.nonbiz.testmatchers.RBMatchers.alwaysMatchingMatcher;

public class DoNotCreateArtificialTermForMinTest extends RBSingleValueClassTestMatcher<DoNotCreateArtificialTermForMin> {

  @Override
  public DoNotCreateArtificialTermForMin makeOnlyObject() {
    return doNotCreateArtificialTermForMin();
  }

  @Override
  protected boolean willMatch(DoNotCreateArtificialTermForMin expected, DoNotCreateArtificialTermForMin actual) {
    return doNotCreateArtificialTermForMinMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<DoNotCreateArtificialTermForMin> doNotCreateArtificialTermForMinMatcher(
      DoNotCreateArtificialTermForMin expected) {
    return alwaysMatchingMatcher(); // no fields, so this always matches
  }

}
