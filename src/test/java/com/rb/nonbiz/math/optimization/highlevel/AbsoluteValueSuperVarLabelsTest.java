package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.highlevel.AbsoluteValueSuperVarLabels.absoluteValueSuperVarLabels;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.HumanReadableLabelTest.humanReadableLabelMatcher;

public class AbsoluteValueSuperVarLabelsTest extends RBTestMatcher<AbsoluteValueSuperVarLabels> {

  @Override
  public AbsoluteValueSuperVarLabels makeTrivialObject() {
    return absoluteValueSuperVarLabels("x");
  }

  @Override
  public AbsoluteValueSuperVarLabels makeNontrivialObject() {
    return absoluteValueSuperVarLabels("abc");
  }

  @Override
  public AbsoluteValueSuperVarLabels makeMatchingNontrivialObject() {
    return absoluteValueSuperVarLabels("abc");
  }

  @Override
  protected boolean willMatch(AbsoluteValueSuperVarLabels expected, AbsoluteValueSuperVarLabels actual) {
    return absoluteValueSuperVarLabelsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<AbsoluteValueSuperVarLabels> absoluteValueSuperVarLabelsMatcher(
      AbsoluteValueSuperVarLabels expected) {
    return makeMatcher(expected,
        match(v -> v.getPositiveLabel(),      f -> humanReadableLabelMatcher(f)),
        match(v -> v.getNegativeLabel(),      f -> humanReadableLabelMatcher(f)),
        match(v -> v.getSignedLabel(),        f -> humanReadableLabelMatcher(f)),
        match(v -> v.getAbsoluteValueLabel(), f -> humanReadableLabelMatcher(f)),
        match(v -> v.getEqualityLabel(),      f -> humanReadableLabelMatcher(f)));
  }

}
