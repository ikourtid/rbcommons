package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.json.RBJsonLocalDateArray.RBJsonLocalDateArrayBuilder.rbJsonLocalDateArrayBuilder;
import static com.rb.nonbiz.json.RBJsonLocalDateArray.emptyRBJsonLocalDateArray;
import static com.rb.nonbiz.testmatchers.Match.matchListUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY0;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY1;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY2;

public class RBJsonLocalDateArrayTest extends RBTestMatcher<RBJsonLocalDateArray> {

  @Test
  public void datesOutOfOrder_throws() {
    assertIllegalArgumentException( () -> rbJsonLocalDateArrayBuilder().add(DAY1).add(DAY0).build());
  }

  @Override
  public RBJsonLocalDateArray makeTrivialObject() {
    return emptyRBJsonLocalDateArray();
  }

  @Override
  public RBJsonLocalDateArray makeNontrivialObject() {
    return rbJsonLocalDateArrayBuilder()
        .add(DAY0)
        .add(DAY1)
        .add(DAY2)
        .build();
  }

  @Override
  public RBJsonLocalDateArray makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return rbJsonLocalDateArrayBuilder()
        .add(DAY0)
        .add(DAY1)
        .add(DAY2)
        .build();
  }

  @Override
  protected boolean willMatch(RBJsonLocalDateArray expected, RBJsonLocalDateArray actual) {
    return rbJsonLocalDateArrayMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RBJsonLocalDateArray> rbJsonLocalDateArrayMatcher(RBJsonLocalDateArray expected) {
    return makeMatcher(expected,
        matchListUsingEquals(v -> v.getRawLocalDatesList()));
  }

}
