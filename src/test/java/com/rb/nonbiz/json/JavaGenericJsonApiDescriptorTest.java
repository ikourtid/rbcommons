package com.rb.nonbiz.json;

import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.JavaGenericJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.JavaGenericJsonApiDescriptor.javaGenericJsonApiDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static org.junit.Assert.fail;

public class JavaGenericJsonApiDescriptorTest extends RBTestMatcher<JavaGenericJsonApiDescriptor> {

  @Test
  public void prohibitsCertainTypes() {
    fail("FIXME IAK YAML");
  }

  @Override
  public JavaGenericJsonApiDescriptor makeTrivialObject() {
    return javaGenericJsonApiDescriptor(PreciseValue.class, Money.class);
  }

  @Override
  public JavaGenericJsonApiDescriptor makeNontrivialObject() {
    return javaGenericJsonApiDescriptor(ClosedRange.class, UnitFraction.class);
  }

  @Override
  public JavaGenericJsonApiDescriptor makeMatchingNontrivialObject() {
    return javaGenericJsonApiDescriptor(ClosedRange.class, UnitFraction.class);
  }

  @Override
  protected boolean willMatch(JavaGenericJsonApiDescriptor expected, JavaGenericJsonApiDescriptor actual) {
    return javaGenericJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JavaGenericJsonApiDescriptor> javaGenericJsonApiDescriptorMatcher(
      JavaGenericJsonApiDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getOuterClass()),
        matchList(       v -> v.getGenericArgumentClasses(), f -> typeSafeEqualTo(f)));
  }

}
