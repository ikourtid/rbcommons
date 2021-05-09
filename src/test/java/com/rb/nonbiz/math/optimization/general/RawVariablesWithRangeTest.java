package com.rb.nonbiz.math.optimization.general;

import com.google.common.collect.Range;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;

import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.general.RawVariableWithRange.rawVariableWithRange;
import static com.rb.nonbiz.math.optimization.general.RawVariableWithRangeTest.variableWithRangeMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.unorderedCollectionMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static java.util.Comparator.comparing;

public class RawVariablesWithRangeTest extends RBTestMatcher<RawVariablesWithRange> {

  public static RawVariablesWithRange rawVariablesWithRange(RawVariableWithRange...varsWithRange) {
    return RawVariablesWithRange.rawVariablesWithRange(Arrays.asList(varsWithRange));
  }

  @Override
  public RawVariablesWithRange makeTrivialObject() {
    return rawVariablesWithRange(rawVariableWithRange(rawVariable("x", 0), Range.all()));
  }

  @Override
  public RawVariablesWithRange makeNontrivialObject() {
    return rawVariablesWithRange(
        rawVariableWithRange(rawVariable("x", 0), Range.closed(7.0, 8.0)),
        rawVariableWithRange(rawVariable("y", 1), Range.closed(7.1, 8.1)),
        rawVariableWithRange(rawVariable("z", 2), Range.closed(7.2, 8.2)),
        rawVariableWithRange(rawVariable("w", 3), Range.closed(7.3, 8.3)));
  }

  @Override
  public RawVariablesWithRange makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rawVariablesWithRange(
        rawVariableWithRange(rawVariable("w", 3), Range.closed(7.3 + e, 8.3 + e)),
        rawVariableWithRange(rawVariable("z", 2), Range.closed(7.2 + e, 8.2 + e)),
        rawVariableWithRange(rawVariable("y", 1), Range.closed(7.1 + e, 8.1 + e)),
        rawVariableWithRange(rawVariable("x", 0), Range.closed(7.0 + e, 8.0 + e)));
  }

  @Override
  protected boolean willMatch(RawVariablesWithRange expected, RawVariablesWithRange actual) {
    return rawVariablesWithRangeMatcher(expected).matches(actual);
  }

  // The order does not matter. I could have used an RBMap to avoid this, but there is no equals/hashcode
  // for Variable, so I didn't want to rely on pointer semantics.
  public static TypeSafeMatcher<RawVariablesWithRange> rawVariablesWithRangeMatcher(RawVariablesWithRange expected) {
    return makeMatcher(expected, actual ->
        unorderedCollectionMatcher(
            expected.getListOfRawVariablesWithRange(),
            vwr -> variableWithRangeMatcher(vwr),
            comparing(vwr -> vwr.getRawVariable().getArrayIndex()))
            .matches(actual.getListOfRawVariablesWithRange()));
  }

}
