package com.rb.nonbiz.json;

import com.rb.biz.types.Money;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.IidMapJsonApiPropertyDescriptor;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.RBMapJsonApiPropertyDescriptor;
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
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.RBMapJsonApiPropertyDescriptor.rbMapJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptorTest.dataClassJsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentationTest.jsonPropertySpecificDocumentationMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;

public class RBMapJsonApiPropertyDescriptorTest extends RBTestMatcher<RBMapJsonApiPropertyDescriptor> {

  @Test
  public void prohibitsCertainTypesAsKeys() {
    rbSetOf(
        BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class, // we always want to describe a specific subclass of PreciseValue
        ImpreciseValue.class, // same

        Strings.class,      // a common misspelling of String.class
        InstrumentId.class, // should use JsonTicker instead
        Symbol.class,       // should use JsonTicker instead

        // These 3 have their own JsonApiPropertyDescriptor classes, which we should be using.
        IidMap.class,
        RBMap.class,
        RBSet.class)
        .forEach(keyClass ->
            // Using Double.class as a simple key that we know would normally work.
            // This way, it's clearer that what makes the test fail is the key class.
            assertIllegalArgumentException( () -> rbMapJsonApiPropertyDescriptor(
                simpleClassJsonApiPropertyDescriptor(keyClass), simpleClassJsonApiPropertyDescriptor(Double.class))));
  }

  @Test
  public void prohibitsCertainTypesAsValues() {
    rbSetOf(
        BigDecimal.class,   // I don't think we ever use a BigDecimal directly in the code; it's usually some PreciseValue
        PreciseValue.class, // we always want to describe a specific subclass of PreciseValue

        Strings.class,      // a common misspelling of String.class
        InstrumentId.class, // should use JsonTicker instead
        Symbol.class,       // should use JsonTicker instead

        // These 3 have their own JsonApiPropertyDescriptor classes, which we should be using.
        IidMap.class,
        RBMap.class,
        RBSet.class)
        .forEach(valueClass ->
            // Using String.class as a simple key that we know would normally work.
            // This way, it's clearer that what makes the test fail is the value class.
            assertIllegalArgumentException( () -> rbMapJsonApiPropertyDescriptor(
                simpleClassJsonApiPropertyDescriptor(String.class), simpleClassJsonApiPropertyDescriptor(valueClass))));
  }

  @Test
  public void keyOrValueClassContainPropertySpecificDocumentation_throws() {
    assertIllegalArgumentException( () -> rbMapJsonApiPropertyDescriptor(
        simpleClassJsonApiPropertyDescriptor(
            String.class,
            jsonPropertySpecificDocumentation(documentation(DUMMY_STRING))),
        simpleClassJsonApiPropertyDescriptor(
            Money.class,
            jsonPropertySpecificDocumentation(documentation(DUMMY_STRING)))));

    assertIllegalArgumentException( () -> rbMapJsonApiPropertyDescriptor(
        simpleClassJsonApiPropertyDescriptor(
            String.class,
            jsonPropertySpecificDocumentation(documentation(DUMMY_STRING))),
        simpleClassJsonApiPropertyDescriptor(
            Money.class)));

    assertIllegalArgumentException( () -> rbMapJsonApiPropertyDescriptor(
        simpleClassJsonApiPropertyDescriptor(
            String.class),
        simpleClassJsonApiPropertyDescriptor(
            Money.class,
            jsonPropertySpecificDocumentation(documentation(DUMMY_STRING)))));

    RBMapJsonApiPropertyDescriptor doesNotThrow = rbMapJsonApiPropertyDescriptor(
        simpleClassJsonApiPropertyDescriptor(String.class),
        simpleClassJsonApiPropertyDescriptor(Money.class));
  }

  @Override
  public RBMapJsonApiPropertyDescriptor makeTrivialObject() {
    return rbMapJsonApiPropertyDescriptor(simpleClassJsonApiPropertyDescriptor(String.class), simpleClassJsonApiPropertyDescriptor(Double.class));
  }

  @Override
  public RBMapJsonApiPropertyDescriptor makeNontrivialObject() {
    return rbMapJsonApiPropertyDescriptor(
        javaGenericJsonApiPropertyDescriptor(UniqueId.class, simpleClassJsonApiPropertyDescriptor(String.class)),
        javaGenericJsonApiPropertyDescriptor(ClosedRange.class, simpleClassJsonApiPropertyDescriptor(Double.class)),
        jsonPropertySpecificDocumentation(documentation("xyz")));
  }

  @Override
  public RBMapJsonApiPropertyDescriptor makeMatchingNontrivialObject() {
    return rbMapJsonApiPropertyDescriptor(
        javaGenericJsonApiPropertyDescriptor(UniqueId.class, simpleClassJsonApiPropertyDescriptor(String.class)),
        javaGenericJsonApiPropertyDescriptor(ClosedRange.class, simpleClassJsonApiPropertyDescriptor(Double.class)),
        jsonPropertySpecificDocumentation(documentation("xyz")));
  }

  @Override
  protected boolean willMatch(RBMapJsonApiPropertyDescriptor expected, RBMapJsonApiPropertyDescriptor actual) {
    return rbMapJsonApiPropertyDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RBMapJsonApiPropertyDescriptor> rbMapJsonApiPropertyDescriptorMatcher(
      RBMapJsonApiPropertyDescriptor expected) {
    return makeMatcher(expected,
        match(           v -> v.getKeyClassDescriptor(),   f -> dataClassJsonApiPropertyDescriptorMatcher(f)),
        match(           v -> v.getValueClassDescriptor(), f -> dataClassJsonApiPropertyDescriptorMatcher(f)),
        matchOptional(   v -> v.getPropertySpecificDocumentation(), f -> jsonPropertySpecificDocumentationMatcher(f)));
  }

}
