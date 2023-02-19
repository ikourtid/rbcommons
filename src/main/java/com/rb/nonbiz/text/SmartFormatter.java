package com.rb.nonbiz.text;

import com.google.inject.Inject;

public class SmartFormatter {

  @Inject static SmartFormatterHelper smartFormatterHelper;

  public static String smartFormatWithTimePrepended(String template, Object... args) {
    return smartFormatterHelper.formatWithTimePrepended(template, args);
  }

  public static String smartFormat(String template, Object... args) {
    return smartFormatterHelper.format(template, args);
  }


}
