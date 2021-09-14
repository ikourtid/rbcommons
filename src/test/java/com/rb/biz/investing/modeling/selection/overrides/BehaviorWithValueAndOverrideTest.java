package com.rb.biz.investing.modeling.selection.overrides;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class BehaviorWithValueAndOverrideTest {

  public static <T extends Comparable<T>> TypeSafeMatcher<BehaviorWithValueAndOverride<T>>
  behaviorWithValueAndOverrideMatcher(BehaviorWithValueAndOverride<T> expected) {
    // Normally, we would use a generalVisitorMatcher for these, but this is simpler, since all
    // subclasses of BehaviorWithValueAndOverride have no data fields.
    // Otherwise, the visitor methods (e.g. visitXYZ() etc) would need to also have an argument of type XYZ,
    // which is overkill, since XYZ has no data fields.
    return makeMatcher(expected, actual ->
        actual.getClass().equals(expected.getClass()));
  }

}
