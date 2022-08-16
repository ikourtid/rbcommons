package com.rb.nonbiz.json;

import com.rb.biz.types.Money;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.IidMapJsonApiPropertyDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.IidMapJsonApiPropertyDescriptor.iidMapJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.JavaGenericJsonApiPropertyDescriptor.javaGenericJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptorTest.dataClassJsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class IidMapJsonApiPropertyDescriptorTest extends RBTestMatcher<IidMapJsonApiPropertyDescriptor> {

  @Test
  public void prohibitsCertainTypes() {
    rbSetOf(
        BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class, // we always want to describe a specific subclass of PreciseValue
        ImpreciseValue.class, // same

        Strings.class,      // a common misspelling of String.class
        InstrumentId.class, // sounds weird to have an IidMap that maps to an instrument
        Symbol.class,       // same as above

        // These 2 have their own JsonApiPropertyDescriptor classes, which we should be using.
        UniqueId.class,
        RBSet.class,

        // It's unlikely that we'll be mapping to another map, although not impossible.
        IidMap.class,
        RBMap.class)
        .forEach(clazz ->
            assertIllegalArgumentException( () -> iidMapJsonApiPropertyDescriptor(simpleClassJsonApiPropertyDescriptor(clazz))));
  }

  @Override
  public IidMapJsonApiPropertyDescriptor makeTrivialObject() {
    return iidMapJsonApiPropertyDescriptor(simpleClassJsonApiPropertyDescriptor(Money.class));
  }

  @Override
  public IidMapJsonApiPropertyDescriptor makeNontrivialObject() {
    return iidMapJsonApiPropertyDescriptor(
        javaGenericJsonApiPropertyDescriptor(UniqueId.class, simpleClassJsonApiPropertyDescriptor(ClosedRange.class)));
  }

  @Override
  public IidMapJsonApiPropertyDescriptor makeMatchingNontrivialObject() {
    return iidMapJsonApiPropertyDescriptor(
        javaGenericJsonApiPropertyDescriptor(UniqueId.class, simpleClassJsonApiPropertyDescriptor(ClosedRange.class)));
  }

  @Override
  protected boolean willMatch(IidMapJsonApiPropertyDescriptor expected, IidMapJsonApiPropertyDescriptor actual) {
    return iidMapJsonApiPropertyDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<IidMapJsonApiPropertyDescriptor> iidMapJsonApiPropertyDescriptorMatcher(
      IidMapJsonApiPropertyDescriptor expected) {
    return makeMatcher(expected,
        match(v -> v.getValueClassDescriptor(), f -> dataClassJsonApiPropertyDescriptorMatcher(f)));
  }

}
