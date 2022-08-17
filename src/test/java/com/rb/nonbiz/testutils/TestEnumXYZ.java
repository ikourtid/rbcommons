package com.rb.nonbiz.testutils;

import com.rb.nonbiz.text.HasHumanReadableDocumentation;
import com.rb.nonbiz.text.HumanReadableDocumentation;

import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;

/**
 * A test-only enum for cases where we want to test something out without relying on a specific enum.
 */
public enum TestEnumXYZ implements HasHumanReadableDocumentation {

  X("test documentation for X"),
  Y("test documentation for Y"),
  Z("test documentation for Z");

  private final HumanReadableDocumentation documentation;

  TestEnumXYZ(String documentation) {
    this.documentation = documentation(documentation);
  }

  @Override
  public HumanReadableDocumentation getDocumentation() {
    return documentation;
  }

}
