package com.rb.nonbiz.json;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.UniqueIdJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.PreciseValue;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleClassJsonApiDescriptor.simpleClassJsonApiDescriptor;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.UniqueIdJsonApiDescriptor.uniqueIdJsonApiDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class UniqueIdJsonApiDescriptorTest extends RBTestMatcher<UniqueIdJsonApiDescriptor> {

  @Test
  public void prohibitsCertainTypes() {
    // This is not exhaustive, but these are classes that it never makes sense to have a UniqueId for.
    rbSetOf(
        BigDecimal.class,
        PreciseValue.class,

        Strings.class,
        InstrumentId.class,
        Symbol.class,

        UniqueId.class,
        IidMap.class,
        RBMap.class,
        RBSet.class)
        .forEach(clazz ->
            assertIllegalArgumentException( () -> simpleClassJsonApiDescriptor(clazz)));
  }

  @Override
  public UniqueIdJsonApiDescriptor makeTrivialObject() {
    // It doesn't ever really make sense to use UniqueId<Double> in the code,
    // but the data classes that we normally use unique IDs for are not in the same repo as this code.
    return uniqueIdJsonApiDescriptor(Double.class);
  }

  @Override
  public UniqueIdJsonApiDescriptor makeNontrivialObject() {
    // It doesn't ever really make sense to use UniqueId<ClosedRange> in the code,
    // but the data classes that we normally use unique IDs for are not in the same repo as this code.
    return uniqueIdJsonApiDescriptor(ClosedRange.class);
  }

  @Override
  public UniqueIdJsonApiDescriptor makeMatchingNontrivialObject() {
    // see above
    return uniqueIdJsonApiDescriptor(ClosedRange.class);
  }

  @Override
  protected boolean willMatch(UniqueIdJsonApiDescriptor expected, UniqueIdJsonApiDescriptor actual) {
    return uniqueIdJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<UniqueIdJsonApiDescriptor> uniqueIdJsonApiDescriptorMatcher(
      UniqueIdJsonApiDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getClassOfId()));
  }

}
