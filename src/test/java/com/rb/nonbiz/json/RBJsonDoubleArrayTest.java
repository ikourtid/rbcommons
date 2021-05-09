package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.RBJsonDoubleArray.RBJsonDoubleArrayBuilder.rbJsonDoubleArrayBuilder;
import static com.rb.nonbiz.json.RBJsonDoubleArray.emptyRBJsonDoubleArray;
import static com.rb.nonbiz.testmatchers.Match.matchDoubleList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class RBJsonDoubleArrayTest extends RBTestMatcher<RBJsonDoubleArray> {

  @Override
  public RBJsonDoubleArray makeTrivialObject() {
    return emptyRBJsonDoubleArray();
  }

  @Override
  public RBJsonDoubleArray makeNontrivialObject() {
    return rbJsonDoubleArrayBuilder()
        .add(1.1)
        .add(-7.7)
        .add(0)
        .build();
  }

  @Override
  public RBJsonDoubleArray makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbJsonDoubleArrayBuilder()
        .add(1.1 + e)
        .add(-7.7 + e)
        .add(0 + e)
        .build();
  }

  @Override
  protected boolean willMatch(RBJsonDoubleArray expected, RBJsonDoubleArray actual) {
    return rbJsonDoubleArrayMatcher(expected, 1e-8).matches(actual);
  }

  public static TypeSafeMatcher<RBJsonDoubleArray> rbJsonDoubleArrayMatcher(RBJsonDoubleArray expected, double epsilon) {
    return makeMatcher(expected,
        matchDoubleList(v -> v.getRawDoublesList(), epsilon));
  }

}
