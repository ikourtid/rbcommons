package com.rb.nonbiz.util;

import org.junit.Test;

import static com.rb.nonbiz.testmatchers.RBValueMatchers.enumMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.util.EnumStringRoundTripConversionInfo.enumStringRoundTripConversionInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class EnumStringRoundTripConversionInfoTest {

  private enum TestEnum implements JsonRoundTripStringConvertibleEnum<TestEnum> {

    ENUM_VALUE_1("_1"),
    ENUM_VALUE_2("_2");

    private final String uniqueStableString;

    TestEnum(String uniqueStableString) {
      this.uniqueStableString = uniqueStableString;
    }

    @Override
    public String toUniqueStableString() {
      return uniqueStableString;
    }

  }


  private enum BadEnum implements JsonRoundTripStringConvertibleEnum<BadEnum> {

    BAD_ENUM_VALUE_1("x"),
    BAD_ENUM_VALUE_2("x");

    private final String uniqueStableString;

    BadEnum(String uniqueStableString) {
      this.uniqueStableString = uniqueStableString;
    }

    @Override
    public String toUniqueStableString() {
      return uniqueStableString;
    }

  }


  private enum EmptyEnum implements JsonRoundTripStringConvertibleEnum<EmptyEnum> {

    ;  // no enum values defined

    @Override
    public String toUniqueStableString() {
      return "noEnums";
    }

  }

  private enum EnumWithWhitespaceUniqueStableString
      implements JsonRoundTripStringConvertibleEnum<EnumWithWhitespaceUniqueStableString> {

    ENUM_VALUE_1("_1"),
    ENUM_VALUE_2(" \t \r \n ");  // not a great uniqueStableString

    private final String uniqueStableString;

    EnumWithWhitespaceUniqueStableString(String uniqueStableString) {
      this.uniqueStableString = uniqueStableString;
    }

    @Override
    public String toUniqueStableString() {
      return uniqueStableString;
    }
  }


  private enum EnumWithNonAsciiUniqueStableString
      implements JsonRoundTripStringConvertibleEnum<EnumWithNonAsciiUniqueStableString> {

    ENUM_VALUE_1("_1"),
    ENUM_VALUE_2("ABC\u00A0XYZ");  // contains a non-breaking space

    private final String uniqueStableString;

    EnumWithNonAsciiUniqueStableString(String uniqueStableString) {
      this.uniqueStableString = uniqueStableString;
    }

    @Override
    public String toUniqueStableString() {
      return uniqueStableString;
    }
  }


  @Test
  public void testEmptyEnum_throws() {
    // EnumStringRoundTripConversionInfo throws unless the enum class has at least one enum value
    assertIllegalArgumentException( () -> enumStringRoundTripConversionInfo(EmptyEnum.class));
  }

  @Test
  public void allWhitespaceUniqueStableString_throws() {
    // UniqueStableStrings can't be entirely whitespace
    assertIllegalArgumentException( () -> enumStringRoundTripConversionInfo(EnumWithWhitespaceUniqueStableString.class));
  }

  @Test
  public void UniqueStableString_hasNonAsciiChar_throws() {
    // UniqueStableStrings can't have non-ascii chars
    assertIllegalArgumentException( () -> enumStringRoundTripConversionInfo(EnumWithNonAsciiUniqueStableString.class));
  }

  @Test
  public void generalCase() {
    EnumStringRoundTripConversionInfo<TestEnum> conversionInfo = enumStringRoundTripConversionInfo(TestEnum.class);

    assertEquals("_1", TestEnum.ENUM_VALUE_1.toUniqueStableString());
    assertEquals("_2", TestEnum.ENUM_VALUE_2.toUniqueStableString());

    assertThat(conversionInfo.getEnumValue("_1"), enumMatcher(TestEnum.ENUM_VALUE_1));
    assertThat(conversionInfo.getEnumValue("_2"), enumMatcher(TestEnum.ENUM_VALUE_2));

    assertIllegalArgumentException( () -> conversionInfo.getEnumValue("_3"));
    assertIllegalArgumentException( () -> conversionInfo.getEnumValue(""));
  }

  @Test
  public void badEnum_multipleEnumValuesMapToSameString_throws() {
    // BadEnum has two enum values, both with "x" as their "unique" string representation.
    assertIllegalArgumentException( () -> enumStringRoundTripConversionInfo(BadEnum.class));
  }

}
