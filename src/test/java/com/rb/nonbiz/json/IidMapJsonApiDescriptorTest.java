package com.rb.nonbiz.json;

import com.rb.biz.types.Money;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.IidMapJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.IidMapJsonApiDescriptor.iidMapJsonApiDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class IidMapJsonApiDescriptorTest extends RBTestMatcher<IidMapJsonApiDescriptor> {

  @Test
  public void prohibitsCertainTypes() {
    rbSetOf(
        BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class, // we always want to describe a specific subclass of PreciseValue
        ImpreciseValue.class, // same

        Strings.class,      // a common misspelling of String.class
        InstrumentId.class, // sounds weird to have an IidMap that maps to an instrument
        Symbol.class,       // same as above

        // These 2 have their own JsonApiDescriptor classes, which we should be using.
        UniqueId.class,
        RBSet.class,

        // It's unlikely that we'll be mapping to another map, although not impossible.
        IidMap.class,
        RBMap.class)
        .forEach(clazz ->
            assertIllegalArgumentException( () -> iidMapJsonApiDescriptor(clazz)));
  }

  @Override
  public IidMapJsonApiDescriptor makeTrivialObject() {
    return iidMapJsonApiDescriptor(Money.class);
  }

  @Override
  public IidMapJsonApiDescriptor makeNontrivialObject() {
    return iidMapJsonApiDescriptor(ClosedRange.class);
  }

  @Override
  public IidMapJsonApiDescriptor makeMatchingNontrivialObject() {
    return iidMapJsonApiDescriptor(ClosedRange.class);
  }

  @Override
  protected boolean willMatch(IidMapJsonApiDescriptor expected, IidMapJsonApiDescriptor actual) {
    return iidMapJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<IidMapJsonApiDescriptor> iidMapJsonApiDescriptorMatcher(
      IidMapJsonApiDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getValueClass()));
  }

}
