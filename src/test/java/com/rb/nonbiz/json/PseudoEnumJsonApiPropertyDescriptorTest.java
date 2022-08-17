package com.rb.nonbiz.json;

import com.rb.nonbiz.json.JsonApiPropertyDescriptor.PseudoEnumJsonApiPropertyDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.PseudoEnumJsonApiPropertyDescriptor.pseudoEnumJsonApiPropertyDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.HumanReadableDocumentation.humanReadableDocumentation;
import static com.rb.nonbiz.text.HumanReadableDocumentationTest.humanReadableDocumentationMatcher;
import static com.rb.nonbiz.text.HumanReadableLabelTest.humanReadableLabelMatcher;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

public class PseudoEnumJsonApiPropertyDescriptorTest extends RBTestMatcher<PseudoEnumJsonApiPropertyDescriptor> {

  @Test
  public void mustHaveAtLeastOneItem() {
    assertIllegalArgumentException( () -> pseudoEnumJsonApiPropertyDescriptor(emptyRBMap()));
    PseudoEnumJsonApiPropertyDescriptor doesNotThrow = pseudoEnumJsonApiPropertyDescriptor(singletonRBMap(
        "a", humanReadableDocumentation("x")));
  }

  @Test
  public void mustHaveNonEmptyExplanations() {
    Function<String, PseudoEnumJsonApiPropertyDescriptor> maker = description -> pseudoEnumJsonApiPropertyDescriptor(singletonRBMap(
        "a", humanReadableDocumentation(description)));

    assertIllegalArgumentException( () -> maker.apply(""));
    PseudoEnumJsonApiPropertyDescriptor doesNotThrow = maker.apply("foo");
  }

  @Override
  public PseudoEnumJsonApiPropertyDescriptor makeTrivialObject() {
    return pseudoEnumJsonApiPropertyDescriptor(singletonRBMap(
        "a", humanReadableDocumentation("x")));
  }

  @Override
  public PseudoEnumJsonApiPropertyDescriptor makeNontrivialObject() {
    return pseudoEnumJsonApiPropertyDescriptor(rbMapOf(
        "item1", humanReadableDocumentation("explanation 1"),
        "item2", humanReadableDocumentation("explanation 2"),
        "item3", humanReadableDocumentation("explanation 3")));
  }

  @Override
  public PseudoEnumJsonApiPropertyDescriptor makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return pseudoEnumJsonApiPropertyDescriptor(rbMapOf(
        "item1", humanReadableDocumentation("explanation 1"),
        "item2", humanReadableDocumentation("explanation 2"),
        "item3", humanReadableDocumentation("explanation 3")));
  }

  @Override
  protected boolean willMatch(PseudoEnumJsonApiPropertyDescriptor expected, PseudoEnumJsonApiPropertyDescriptor actual) {
    return pseudoEnumJsonApiPropertyDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<PseudoEnumJsonApiPropertyDescriptor> pseudoEnumJsonApiPropertyDescriptorMatcher(
      PseudoEnumJsonApiPropertyDescriptor expected) {
    return makeMatcher(expected,
        // We almost never match HumanReadableLabel.
        // However, this rule applies to labels that are attached to various runtime objects
        // (e.g. daily time series) and which are intended for Rowboat developers to read.
        // In this case here, this is a HumanReadableDocumentation, and it is intended for 3rd party developers.
        // So its contents matter.
        matchRBMap(v -> v.getValidValuesToExplanations(), f -> humanReadableDocumentationMatcher(f)));
  }

}
