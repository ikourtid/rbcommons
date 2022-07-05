package com.rb.nonbiz.json;

import com.rb.nonbiz.json.DataClassJsonApiDescriptor.PseudoEnumJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.HumanReadableLabelTest;
import com.rb.nonbiz.text.SimpleHumanReadableLabel;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.PseudoEnumJsonApiDescriptor.pseudoEnumJsonApiDescriptor;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.HumanReadableLabelTest.humanReadableLabelMatcher;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class PseudoEnumJsonApiDescriptorTest extends RBTestMatcher<PseudoEnumJsonApiDescriptor> {

  @Test
  public void mustHaveAtLeastOneItem() {
    assertIllegalArgumentException( () -> pseudoEnumJsonApiDescriptor(emptyRBMap()));
    PseudoEnumJsonApiDescriptor doesNotThrow = pseudoEnumJsonApiDescriptor(singletonRBMap(
        "a", label("x")));
  }

  @Test
  public void mustHaveNonEmptyExplanations() {
    Function<String, PseudoEnumJsonApiDescriptor> maker = description -> pseudoEnumJsonApiDescriptor(singletonRBMap(
        "a", label(description)));

    assertIllegalArgumentException( () -> maker.apply(""));
    PseudoEnumJsonApiDescriptor doesNotThrow = maker.apply("foo");
  }

  @Override
  public PseudoEnumJsonApiDescriptor makeTrivialObject() {
    return pseudoEnumJsonApiDescriptor(singletonRBMap(
        "a", label("x")));
  }

  @Override
  public PseudoEnumJsonApiDescriptor makeNontrivialObject() {
    return pseudoEnumJsonApiDescriptor(rbMapOf(
        "item1", label("explanation 1"),
        "item2", label("explanation 2"),
        "item3", label("explanation 3")));
  }

  @Override
  public PseudoEnumJsonApiDescriptor makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return pseudoEnumJsonApiDescriptor(rbMapOf(
        "item1", label("explanation 1"),
        "item2", label("explanation 2"),
        "item3", label("explanation 3")));
  }

  @Override
  protected boolean willMatch(PseudoEnumJsonApiDescriptor expected, PseudoEnumJsonApiDescriptor actual) {
    return pseudoEnumJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<PseudoEnumJsonApiDescriptor> pseudoEnumJsonApiDescriptorMatcher(
      PseudoEnumJsonApiDescriptor expected) {
    return makeMatcher(expected,
        // We almost never match HumanReadableLabel.
        // However, this rule applies to labels that are attached to various runtime objects
        // (e.g. daily time series) and which are intended for Rowboat developers to read.
        // In this case here, the label is destined for 3rd party developers. So its contents matter.
        matchRBMap(v -> v.getValidValuesToExplanations(), f -> humanReadableLabelMatcher(f)));
  }

}
