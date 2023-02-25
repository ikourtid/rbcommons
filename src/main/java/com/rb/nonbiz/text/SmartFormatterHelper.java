package com.rb.nonbiz.text;

import com.google.inject.Inject;
import com.rb.biz.guice.RBClock;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.types.LongCounter;

import java.util.Arrays;

import static com.rb.nonbiz.types.LongCounter.longCounter;

/**
 * This class is to help exception messages automatically print information about the {@link Symbol}
 * corresponding to an {@link InstrumentId}, without the caller needing to do anything special.
 *
 * <p> Unfortunately, because of Guice dependency injection, we need some trickery to get this to work.
 * This class has to be public, but its methods are intentionally package private, and its name is intentionally
 * unorthodox (it ends with 'Helper'), so that callers will know not to use it. Instead, callers are supposed
 * to use {@link SmartFormatter}. </p>
 */
public class SmartFormatterHelper {

  @Inject InstrumentMaster instrumentMaster;
  @Inject RBClock rbClock;

  private final ThreadLocal<LongCounter> stackDepthByThread = ThreadLocal.withInitial( () -> longCounter());

  String formatWithDatePrepended(String template, Object ... args) {
    return formatHelper(true, template, args);
  }

  String format(String template, Object ... args) {
    return formatHelper(false, template, args);
  }

  synchronized String formatSingleObject(Object obj) {
    LongCounter stackDepth = stackDepthByThread.get();
    stackDepth.increment();

    if (instrumentMaster == null || rbClock == null) {
      return obj.toString();
    }

    if (obj == null) {
      return "<null>";
    }
    String toReturn = PrintsInstruments.class.isAssignableFrom(obj.getClass()) && stackDepth.get() < 10
        ? ((PrintsInstruments) obj).toString(instrumentMaster, rbClock.today())
        : obj.toString();

    stackDepth.decrement();
    return toReturn;
  }

  synchronized private String formatHelper(boolean prependDate, String template, Object ... args) {
    LongCounter stackDepth = stackDepthByThread.get();

    StringBuilder sb = new StringBuilder();
    // We never allow for values to stay null. This is an exception. Otherwise,
    // every unit test for code that logs would have to set the RBClock, which is a pain,
    // OR we would have to hook up Guice modules for every RBTest - also a pain, and also would make the tests slower.
    if (prependDate) {
      sb.append(rbClock == null ? "0000-00-00 " : Strings.format("%s ", rbClock.today()));
    }

    if (instrumentMaster == null || rbClock == null) {
      sb.append(Strings.format(template, args));
      return sb.toString();
    }

    if (stackDepth.get() >= 10) {
      sb.append(Strings.format(template, args));
    } else {
      Object[] newArgs = new Object[args.length];
      Arrays.setAll(newArgs, i -> formatSingleObject(args[i]));
      sb.append(Strings.format(template, newArgs));
    }
    String toReturn = sb.toString();
    stackDepth.decrement();
    return toReturn;
  }

}
