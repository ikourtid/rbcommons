package com.rb.nonbiz.text;

import com.google.inject.Inject;
import com.rb.biz.guice.RBClock;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;

import java.util.Arrays;

public class PrintMessageFormatterForInstruments {

  @Inject static InstrumentMaster instrumentMaster;
  @Inject static RBClock rbClock;

  public String formatWithTimePrepended(String template, Object... args) {
    return formatHelper(true, template, args);
  }

  public String format(String template, Object... args) {
    return formatHelper(false, template, args);
  }

  private String formatHelper(boolean prependTime, String template, Object ... args) {
    StringBuilder sb = new StringBuilder();
    // We never allow for values to stay null. This is an exception. Otherwise,
    // every unit test for code that logs would have to set the RBClock, which is a pain,
    // OR we would have to hook up Guice modules for every RBTest - also a pain, and also would make the tests slower.
    if (prependTime) {
      sb.append(rbClock == null ? "0000-00-00 " : Strings.format("%s ", rbClock.today()));
    }

    if (instrumentMaster == null || rbClock == null) {
      sb.append(Strings.format(template, args));
      return sb.toString();
    }

    Object[] newArgs = new Object[args.length];
    Arrays.setAll(newArgs, i ->
        PrintsInstruments.class.isAssignableFrom(args[i].getClass())
            ? ((PrintsInstruments) args[i]).toString(instrumentMaster, rbClock.today())
            : args[i]);
    sb.append(Strings.format(template, newArgs));
    return sb.toString();
  }

}
