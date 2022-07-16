package com.rb.nonbiz.json;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleClassJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleClassJsonApiDescriptor.simpleClassJsonApiDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class SimpleClassJsonApiDescriptorTest extends RBTestMatcher<SimpleClassJsonApiDescriptor> {

  @Test
  public void prohibitsCertainTypes() {
    rbSetOf(
        BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class, // we always want to describe a specific subclass of PreciseValue
        ImpreciseValue.class, // same

        Strings.class,      // a common misspelling of String.class
        InstrumentId.class, // should use JsonTicker instead
        Symbol.class,       // should use JsonTicker instead

        // These 4 have their own JsonApiDescriptor classes, which we should be using.
        UniqueId.class,
        IidMap.class,
        RBMap.class,
        RBSet.class)
        .forEach(clazz ->
            assertIllegalArgumentException( () -> simpleClassJsonApiDescriptor(clazz)));
  }

  @Override
  public SimpleClassJsonApiDescriptor makeTrivialObject() {
    return simpleClassJsonApiDescriptor(Double.class);
  }

  @Override
  public SimpleClassJsonApiDescriptor makeNontrivialObject() {
    return simpleClassJsonApiDescriptor(ClosedRange.class);
  }

  @Override
  public SimpleClassJsonApiDescriptor makeMatchingNontrivialObject() {
    return simpleClassJsonApiDescriptor(ClosedRange.class);
  }

  @Override
  protected boolean willMatch(SimpleClassJsonApiDescriptor expected, SimpleClassJsonApiDescriptor actual) {
    return simpleClassJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SimpleClassJsonApiDescriptor> simpleClassJsonApiDescriptorMatcher(
      SimpleClassJsonApiDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getClassBeingDescribed()));
  }

}
