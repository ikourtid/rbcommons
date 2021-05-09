package com.rb.nonbiz.math.optimization.general;

import com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.AllRawVariablesInOrderBuilder;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder.AllRawVariablesInOrderBuilder.allRawVariablesInOrderBuilder;
import static com.rb.nonbiz.math.optimization.general.RawVariableTest.rawVariableMatcher;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class AllRawVariablesInOrderBuilderTest extends RBTestMatcher<AllRawVariablesInOrderBuilder> {

  @Test
  public void noVariables_throws() {
    assertIllegalArgumentException( () -> allRawVariablesInOrderBuilder().build());
  }

  @Override
  public AllRawVariablesInOrderBuilder makeTrivialObject() {
    AllRawVariablesInOrderBuilder builder = allRawVariablesInOrderBuilder();
    builder.addRawVariable("x");
    return builder;
  }

  @Override
  public AllRawVariablesInOrderBuilder makeNontrivialObject() {
    AllRawVariablesInOrderBuilder builder = allRawVariablesInOrderBuilder();
    builder.addRawVariable("a");
    builder.addRawVariable("b");
    builder.addRawVariable("c");
    return builder;
  }

  @Override
  public AllRawVariablesInOrderBuilder makeMatchingNontrivialObject() {
    AllRawVariablesInOrderBuilder builder = allRawVariablesInOrderBuilder();
    builder.addRawVariable("a");
    builder.addRawVariable("b");
    builder.addRawVariable("c");
    return builder;
  }

  @Override
  protected boolean willMatch(AllRawVariablesInOrderBuilder expected, AllRawVariablesInOrderBuilder actual) {
    return allRawVariablesInOrderBuilderMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<AllRawVariablesInOrderBuilder> allRawVariablesInOrderBuilderMatcher(
      AllRawVariablesInOrderBuilder expected) {
    return makeMatcher(expected,
      matchList(v -> v.getOrderedRawVariables(), f -> rawVariableMatcher(f)));
  }

}
