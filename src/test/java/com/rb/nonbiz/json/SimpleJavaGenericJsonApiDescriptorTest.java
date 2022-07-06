package com.rb.nonbiz.json;

import com.rb.biz.types.Money;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleJavaGenericJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleClassJsonApiDescriptor.simpleClassJsonApiDescriptor;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleJavaGenericJsonApiDescriptor.simpleJavaGenericJsonApiDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.fail;

public class SimpleJavaGenericJsonApiDescriptorTest extends RBTestMatcher<SimpleJavaGenericJsonApiDescriptor> {

  @Test
  public void prohibitsCertainTypes() {
    fail("FIXME IAK YAML");
  }

  @Override
  public SimpleJavaGenericJsonApiDescriptor makeTrivialObject() {
    return simpleJavaGenericJsonApiDescriptor(PreciseValue.class, Money.class);
  }

  @Override
  public SimpleJavaGenericJsonApiDescriptor makeNontrivialObject() {
    return simpleJavaGenericJsonApiDescriptor(ClosedRange.class, UnitFraction.class);
  }

  @Override
  public SimpleJavaGenericJsonApiDescriptor makeMatchingNontrivialObject() {
    return simpleJavaGenericJsonApiDescriptor(ClosedRange.class, UnitFraction.class);
  }

  @Override
  protected boolean willMatch(SimpleJavaGenericJsonApiDescriptor expected, SimpleJavaGenericJsonApiDescriptor actual) {
    return yearlyTimeSeriesJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SimpleJavaGenericJsonApiDescriptor> yearlyTimeSeriesJsonApiDescriptorMatcher(
      SimpleJavaGenericJsonApiDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getOuterClass()),
        matchUsingEquals(v -> v.getInnerClass()));
  }

}
