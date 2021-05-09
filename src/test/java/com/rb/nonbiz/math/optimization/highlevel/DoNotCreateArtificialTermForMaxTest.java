package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBSingleValueClassTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.highlevel.DoNotCreateArtificialTermForMax.doNotCreateArtificialTermForMax;
import static com.rb.nonbiz.testmatchers.RBMatchers.alwaysMatchingMatcher;

public class DoNotCreateArtificialTermForMaxTest extends RBSingleValueClassTestMatcher<DoNotCreateArtificialTermForMax> {

  @Override
  public DoNotCreateArtificialTermForMax makeOnlyObject() {
    return doNotCreateArtificialTermForMax();
  }

  @Override
  protected boolean willMatch(DoNotCreateArtificialTermForMax expected, DoNotCreateArtificialTermForMax actual) {
    return doNotCreateArtificialTermForMaxMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<DoNotCreateArtificialTermForMax> doNotCreateArtificialTermForMaxMatcher(
      DoNotCreateArtificialTermForMax expected) {
    return alwaysMatchingMatcher(); // no fields, so this always matches
  }

}
