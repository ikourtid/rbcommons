package com.rb.nonbiz.util;

import org.junit.Test;

import static com.rb.nonbiz.testmatchers.RBValueMatchers.enumMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.util.EnumStringRoundTripConversionInfo.enumStringRoundTripConversionInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class EnumStringRoundTripConversionInfoTest {

  private enum TestEnum implements RoundTripStringConvertibleEnum<TestEnum> {

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


  private enum BadEnum implements RoundTripStringConvertibleEnum<BadEnum> {

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
    assertIllegalArgumentException( () -> enumStringRoundTripConversionInfo(BadEnum.class));
  }

}
