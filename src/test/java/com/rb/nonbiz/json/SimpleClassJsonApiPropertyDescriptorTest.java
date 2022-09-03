package com.rb.nonbiz.json;

import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.subclassDiscriminatorPropertyDescriptor;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentationTest.jsonPropertySpecificDocumentationMatcher;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleClassJsonApiPropertyDescriptorTest extends RBTestMatcher<SimpleClassJsonApiPropertyDescriptor> {

  @Test
  public void testSpecialCaseConstructor_subclassDiscriminatorPropertyDescriptor() {
    assertThat(
        subclassDiscriminatorPropertyDescriptor("fooBar"),
        simpleClassJsonApiPropertyDescriptorMatcher(
            simpleClassJsonApiPropertyDescriptor(
                String.class,
                jsonPropertySpecificDocumentation("The value must always be 'fooBar'."))));
  }

  @Test
  public void prohibitsCertainTypes() {
    rbSetOf(
        BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class, // we always want to describe a specific subclass of PreciseValue
        ImpreciseValue.class, // same

        Strings.class,      // a common misspelling of String.class
        InstrumentId.class, // should use JsonTicker instead
        Symbol.class,       // should use JsonTicker instead

        // These 4 have their own JsonApiPropertyDescriptor classes, which we should be using.
        UniqueId.class,
        IidMap.class,
        RBMap.class,
        RBSet.class)
        .forEach(clazz ->
            assertIllegalArgumentException( () -> simpleClassJsonApiPropertyDescriptor(clazz)));
  }

  @Override
  public SimpleClassJsonApiPropertyDescriptor makeTrivialObject() {
    return simpleClassJsonApiPropertyDescriptor(Double.class);
  }

  @Override
  public SimpleClassJsonApiPropertyDescriptor makeNontrivialObject() {
    return simpleClassJsonApiPropertyDescriptor(
        ClosedRange.class, jsonPropertySpecificDocumentation("xyz"));
  }

  @Override
  public SimpleClassJsonApiPropertyDescriptor makeMatchingNontrivialObject() {
    return simpleClassJsonApiPropertyDescriptor(
        ClosedRange.class, jsonPropertySpecificDocumentation("xyz"));
  }

  @Override
  protected boolean willMatch(SimpleClassJsonApiPropertyDescriptor expected, SimpleClassJsonApiPropertyDescriptor actual) {
    return simpleClassJsonApiPropertyDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SimpleClassJsonApiPropertyDescriptor> simpleClassJsonApiPropertyDescriptorMatcher(
      SimpleClassJsonApiPropertyDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getClassBeingDescribed()),
        matchOptional(   v -> v.getPropertySpecificDocumentation(), f -> jsonPropertySpecificDocumentationMatcher(f)));
  }

}
