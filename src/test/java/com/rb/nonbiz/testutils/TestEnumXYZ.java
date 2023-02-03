package com.rb.nonbiz.testutils;

import com.rb.nonbiz.text.HasHumanReadableDocumentation;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.util.EnumStringRoundTripConversionInfo;
import com.rb.nonbiz.util.JsonRoundTripStringConvertibleEnum;

import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.util.EnumStringRoundTripConversionInfo.enumStringRoundTripConversionInfo;

/**
 * A test-only enum for cases where we want to test something out without relying on a specific enum.
 */
public enum TestEnumXYZ implements JsonRoundTripStringConvertibleEnum<TestEnumXYZ>, HasHumanReadableDocumentation {

  X("_X", "test documentation for X"),
  Y("_Y", "test documentation for Y"),
  Z("_Z", "test documentation for Z");

  private static final EnumStringRoundTripConversionInfo<TestEnumXYZ> CONVERSION_INFO =
      enumStringRoundTripConversionInfo(TestEnumXYZ.class);

  public static TestEnumXYZ fromUniqueStableString(String uniqueStableString) {
    return CONVERSION_INFO.getEnumValue(uniqueStableString);
  }

  private final String uniqueStableStringRepresentation;
  private final HumanReadableDocumentation documentation;

  TestEnumXYZ(String uniqueStableStringRepresentation, String documentation) {
    this.uniqueStableStringRepresentation = uniqueStableStringRepresentation;
    this.documentation = documentation(documentation);
  }

  @Override
  public HumanReadableDocumentation getDocumentation() {
    return documentation;
  }

  @Override
  public String toUniqueStableString() {
    return uniqueStableStringRepresentation;
  }

}
