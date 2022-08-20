package com.rb.nonbiz.json;

import com.rb.biz.types.Money;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.IidSet;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBMapWithDefault;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.json.JsonApiPropertyDescriptor.JavaGenericJsonApiPropertyDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.JavaGenericJsonApiPropertyDescriptor.javaGenericJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptorTest.jsonApiPropertyDescriptorMatcher;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentationTest.jsonPropertySpecificDocumentationMatcher;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;

public class JavaGenericJsonApiPropertyDescriptorTest extends RBTestMatcher<JavaGenericJsonApiPropertyDescriptor> {

  @Test
  public void prohibitsCertainTypes() {
    rbSetOf(
        BigDecimal.class,
        PreciseValue.class,
        ImpreciseValue.class,

        Strings.class,
        InstrumentId.class,
        Symbol.class,

        RBSet.class,
        IidSet.class,
        IidMap.class,
        RBMap.class).forEach(badOuterClass -> {
          JsonApiPropertyDescriptor dummy = simpleClassJsonApiPropertyDescriptor(String.class);
          assertIllegalArgumentException( () -> javaGenericJsonApiPropertyDescriptor(badOuterClass, dummy));
          assertIllegalArgumentException( () -> javaGenericJsonApiPropertyDescriptor(badOuterClass, dummy, dummy));
        }
    );
  }

  @Test
  public void genericArgumentClassHasPropertySpecificDocumentation_throws() {
    assertIllegalArgumentException( () -> javaGenericJsonApiPropertyDescriptor(
        ClosedRange.class,
        simpleClassJsonApiPropertyDescriptor(Money.class, jsonPropertySpecificDocumentation(DUMMY_STRING))));
    JavaGenericJsonApiPropertyDescriptor doesNotThrow = javaGenericJsonApiPropertyDescriptor(
        ClosedRange.class,
        simpleClassJsonApiPropertyDescriptor(Money.class));
  }

  @Override
  public JavaGenericJsonApiPropertyDescriptor makeTrivialObject() {
    return javaGenericJsonApiPropertyDescriptor(RBMapWithDefault.class, simpleClassJsonApiPropertyDescriptor(Money.class));
  }

  @Override
  public JavaGenericJsonApiPropertyDescriptor makeNontrivialObject() {
    // This is not a realistic example, because a ClosedRange only has 1 generic argument, not 2.
    // However, there's no way to ever sanity-check that in the production code, because of Java type erasure.
    // Let's use this here so that makeNontrivialObject represents a general case of multiple generic arguments.
    return javaGenericJsonApiPropertyDescriptor(
        ClosedRange.class,
        jsonPropertySpecificDocumentation("xyz"),
        simpleClassJsonApiPropertyDescriptor(Double.class),
        simpleClassJsonApiPropertyDescriptor(UnitFraction.class));
  }

  @Override
  public JavaGenericJsonApiPropertyDescriptor makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return javaGenericJsonApiPropertyDescriptor(
        ClosedRange.class,
        jsonPropertySpecificDocumentation("xyz"),
        simpleClassJsonApiPropertyDescriptor(Double.class),
        simpleClassJsonApiPropertyDescriptor(UnitFraction.class));
  }

  @Override
  protected boolean willMatch(JavaGenericJsonApiPropertyDescriptor expected, JavaGenericJsonApiPropertyDescriptor actual) {
    return javaGenericJsonApiPropertyDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JavaGenericJsonApiPropertyDescriptor> javaGenericJsonApiPropertyDescriptorMatcher(
      JavaGenericJsonApiPropertyDescriptor expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getOuterClass()),
        matchList(       v -> v.getGenericArgumentClassDescriptors(), f -> jsonApiPropertyDescriptorMatcher(f)),
        matchOptional(   v -> v.getPropertySpecificDocumentation(),   f -> jsonPropertySpecificDocumentationMatcher(f)));
  }

}
