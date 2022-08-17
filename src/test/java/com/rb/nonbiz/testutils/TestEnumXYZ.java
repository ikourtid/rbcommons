package com.rb.nonbiz.testutils;

import com.rb.nonbiz.text.HasHumanReadableDocumentation;
import com.rb.nonbiz.text.csv.HumanReadableDocumentation;

import static com.rb.nonbiz.text.csv.HumanReadableDocumentation.humanReadableDocumentation;

/**
 * A test-only enum for cases where we want to test something out without relying on a specific enum.
 */
public enum TestEnumXYZ implements HasHumanReadableDocumentation {

  X("test documentation for X"),
  Y("test documentation for Y"),
  Z("test documentation for Z");

  private final HumanReadableDocumentation documentation;

  TestEnumXYZ(String documentation) {
    this.documentation = humanReadableDocumentation(documentation);
  }

  @Override
  public HumanReadableDocumentation getDocumentation() {
    return documentation;
  }

}
