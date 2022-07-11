package com.rb.nonbiz.json;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.DataClassJsonApiDescriptor.RBMapJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.RBMapJsonApiDescriptor.rbMapJsonApiDescriptor;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.SimpleClassJsonApiDescriptor.simpleClassJsonApiDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class RBMapJsonApiDescriptorTest extends RBTestMatcher<RBMapJsonApiDescriptor> {

  @Test
  public void prohibitsCertainTypesAsKeys() {
    rbSetOf(
        BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class, // we always want to describe a specific subclass of PreciseValue
        ImpreciseValue.class, // same

        Strings.class,      // a common misspelling of String.class,
        InstrumentId.class, // should use JsonTicker instead
        Symbol.class,       // should use JsonTicker instead

        // These 3 have their own JsonApiDescriptor classes, which we should be using.
        IidMap.class,
        RBMap.class,
        RBSet.class)
        .forEach(keyClass ->
            // Using Double.class as a simple key that we know would normally work.
            // This way, it's clearer that what makes the test fail is the key class.
            assertIllegalArgumentException( () -> rbMapJsonApiDescriptor(keyClass, Double.class)));
  }

  @Test
  public void prohibitsCertainTypesAsValues() {
    rbSetOf(
        BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class, // we always want to describe a specific subclass of PreciseValue

        Strings.class,      // a common misspelling of String.class,
        InstrumentId.class, // should use JsonTicker instead
        Symbol.class,       // should use JsonTicker instead

        // These 3 have their own JsonApiDescriptor classes, which we should be using.
        IidMap.class,
        RBMap.class,
        RBSet.class)
        .forEach(valueClass ->
            // Using String.class as a simple key that we know would normally work.
            // This way, it's clearer that what makes the test fail is the value class.
            assertIllegalArgumentException( () -> rbMapJsonApiDescriptor(String.class, valueClass)));
  }

  @Override
  public RBMapJsonApiDescriptor makeTrivialObject() {
    return rbMapJsonApiDescriptor(String.class, Double.class);
  }

  @Override
  public RBMapJsonApiDescriptor makeNontrivialObject() {
    return rbMapJsonApiDescriptor(String.class, ClosedRange.class);
  }

  @Override
  public RBMapJsonApiDescriptor makeMatchingNontrivialObject() {
    return rbMapJsonApiDescriptor(String.class, ClosedRange.class);
  }

  @Override
  protected boolean willMatch(RBMapJsonApiDescriptor expected, RBMapJsonApiDescriptor actual) {
    return rbMapJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RBMapJsonApiDescriptor> rbMapJsonApiDescriptorMatcher(RBMapJsonApiDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getRbMapKeyClass()),
        matchUsingEquals(v -> v.getRBMapValueClass()));
  }

}