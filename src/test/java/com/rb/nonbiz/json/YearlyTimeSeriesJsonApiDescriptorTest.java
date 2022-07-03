package com.rb.nonbiz.json;

import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.YearlyTimeSeriesJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.YearlyTimeSeriesJsonApiDescriptor.yearlyTimeSeriesJsonApiDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class YearlyTimeSeriesJsonApiDescriptorTest extends RBTestMatcher<YearlyTimeSeriesJsonApiDescriptor> {

  @Override
  public YearlyTimeSeriesJsonApiDescriptor makeTrivialObject() {
    return yearlyTimeSeriesJsonApiDescriptor(Money.class);
  }

  @Override
  public YearlyTimeSeriesJsonApiDescriptor makeNontrivialObject() {
    return yearlyTimeSeriesJsonApiDescriptor(ClosedRange.class);
  }

  @Override
  public YearlyTimeSeriesJsonApiDescriptor makeMatchingNontrivialObject() {
    return yearlyTimeSeriesJsonApiDescriptor(ClosedRange.class);
  }

  @Override
  protected boolean willMatch(YearlyTimeSeriesJsonApiDescriptor expected, YearlyTimeSeriesJsonApiDescriptor actual) {
    return yearlyTimeSeriesJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<YearlyTimeSeriesJsonApiDescriptor> yearlyTimeSeriesJsonApiDescriptorMatcher(
      YearlyTimeSeriesJsonApiDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getYearlyTimeSeriesValueClass()));
  }

}
