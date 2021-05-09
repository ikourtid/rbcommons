package com.rb.nonbiz.text;

import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Consumer;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class HumanReadableLabelTest {

  @Test
  public void test_checkNonEmptyLabelWithNoWhitespace() {
    Consumer<String> assertFails = labelText ->
        assertIllegalArgumentException( () -> HumanReadableLabel.checkNonEmptyLabelWithNoWhitespace(labelText));
    assertFails.accept("");
    assertFails.accept("a b");
    assertFails.accept("a ");
    assertFails.accept(" b");

    assertFails.accept("a\tb");
    assertFails.accept("a\t");
    assertFails.accept("\tb");

    assertFails.accept("a\nb");
    assertFails.accept("a\n");
    assertFails.accept("\nb");

    assertFails.accept("a\rb");
    assertFails.accept("a\r");
    assertFails.accept("\rb");

    HumanReadableLabel.checkNonEmptyLabelWithNoWhitespace("a");
    HumanReadableLabel.checkNonEmptyLabelWithNoWhitespace("a_b_c");
  }

  /**
   * Unlike other matchers, this one is meant to be used judiciously.
   * Human-readable labels are exactly that... they are not meant to affect logic.
   * So you don't really need to have expectations all the time.
   * It's sort of like why we never mock logging calls.
   */
  public static TypeSafeMatcher<HumanReadableLabel> humanReadableLabelMatcher(HumanReadableLabel expected) {
    return makeMatcher(expected, actual ->
        expected.getLabelText().equals(actual.getLabelText()));
  }

}
