package com.rb.nonbiz.text;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.HumanReadableLabelTest.humanReadableLabelMatcher;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.emptyLabel;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.simpleFileNameHumanReadableLabel;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.simpleNonEmptyHumanReadableLabelWithoutSpaces;

public class SimpleHumanReadableLabelTest extends RBTestMatcher<SimpleHumanReadableLabel> {

  @Test
  public void testSpecialLabel() {
    assertIllegalArgumentException( () -> simpleNonEmptyHumanReadableLabelWithoutSpaces(""));
    assertIllegalArgumentException( () -> simpleNonEmptyHumanReadableLabelWithoutSpaces(" "));
    assertIllegalArgumentException( () -> simpleNonEmptyHumanReadableLabelWithoutSpaces("a b"));
    assertIllegalArgumentException( () -> simpleNonEmptyHumanReadableLabelWithoutSpaces("a "));
    assertIllegalArgumentException( () -> simpleNonEmptyHumanReadableLabelWithoutSpaces(" b"));
  }

  @Test
  public void testSimpleFileNameLabel() {
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel(""));
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel(" "));
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel("a b"));
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel("/abc"));
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel("./abc"));
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel("../abc"));
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel("3-a"));
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel("abc&xyz"));
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel("abc\txyz"));
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel("abc\\"));
    assertIllegalArgumentException( () -> simpleFileNameHumanReadableLabel("abc()"));

    SimpleHumanReadableLabel doesNotThrow;
    doesNotThrow = simpleFileNameHumanReadableLabel("a");
    doesNotThrow = simpleFileNameHumanReadableLabel("3a");
    doesNotThrow = simpleFileNameHumanReadableLabel(".a");
    doesNotThrow = simpleFileNameHumanReadableLabel("_a");
    doesNotThrow = simpleFileNameHumanReadableLabel("ABC_xyz_123");
    doesNotThrow = simpleFileNameHumanReadableLabel("abc.def");
  }

  @Override
  public SimpleHumanReadableLabel makeTrivialObject() {
    return emptyLabel();
  }

  @Override
  public SimpleHumanReadableLabel makeNontrivialObject() {
    return label("abc");
  }

  @Override
  public SimpleHumanReadableLabel makeMatchingNontrivialObject() {
    return label("abc");
  }

  @Override
  protected boolean willMatch(SimpleHumanReadableLabel expected, SimpleHumanReadableLabel actual) {
    return humanReadableLabelMatcher(expected).matches(actual);
  }

}
