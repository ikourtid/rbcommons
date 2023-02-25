package com.rb.nonbiz.text;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.rb.biz.guice.RBClock;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.types.LongCounter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

  /**
   * This is a hack to prevent {@link StackOverflowError}. If {@link PrintsInstruments#toString(InstrumentMaster, LocalDate)}
   * throws an exception, then that exception itself will call {@link SmartFormatter}, and there can be an infinite
   * loop. We could try / catch {@link StackOverflowError} but that's dumb, because it will eat up a lot of space
   * in the stack, possibly disabling the entire application. And we don't have a good way of measuring the stack
   * depth by passing around a parameter, because the new calls to {@link SmartFormatter} will be made from other
   * places in the code, which won't know that we're already called {@link SmartFormatter}.
   *
   * The {@link ThreadLocal} is necessary to avoid that we don't have cross-contamination across threads. Note that the
   * methods that use stackDepthByThread will first increment it, and only decrement it after the string to be returned
   * has been generated, which means that no more exceptions (which could cause the {@link StackOverflowError} can
   * be thrown.
   *
   * I think this is not perfect, and will result in the value of stackDepthByThread (for this thread) being non-0
   * after an exception is thrown. However, we almost always terminate if there is an exception in our code, so this
   * should not be a problem. Worst case, if the count hits the upper limit, then any strings being printed will not
   * do the automatic conversion of instrument ids to symbols, which is the entire point of {@link SmartFormatter}.
   *
   */
  private ThreadLocal<LongCounter> stackDepthByThread = ThreadLocal.withInitial( () -> longCounter());
  private final Lock lock = new ReentrantLock();

  // The maximum stack depth (only counting calls to SmartFormatter) beyond which we will consider having
  // an infinite recursion.
  private final int MAX_STACK_DEPTH = 10;

  /**
   * This is useful in situations where the same thread can throw exceptions but not terminate, such as when
   * we process JSON API requests in the JSON API site. We need to call this before every optimization request.
   */
  void reset() {
    lock.lock();
    try {
      stackDepthByThread = ThreadLocal.withInitial(() -> longCounter());
    } finally {
      lock.unlock();
    }
  }

  String formatWithDatePrepended(String template, Object ... args) {
    return formatHelper(true, template, args);
  }

  String format(String template, Object ... args) {
    return formatHelper(false, template, args);
  }

  String formatSingleObject(Object obj) {
    if (instrumentMaster == null || rbClock == null) {
      return obj.toString();
    }

    if (obj == null) {
      return "<null>";
    }

    lock.lock();
    try {
      LongCounter stackDepth = stackDepthByThread.get().increment(); // see definition of stackDepthByThread for more
      String toReturn = PrintsInstruments.class.isAssignableFrom(obj.getClass()) && stackDepth.get() < MAX_STACK_DEPTH
          ? ((PrintsInstruments) obj).toString(instrumentMaster, rbClock.today())
          : obj.toString();
      stackDepth.decrement();
      return toReturn;
    } finally {
      lock.unlock();
    }
  }

  // As the name makes it amply clear, do not use this in prod code. Unfortunately there's no better way to test
  // the 'stack overflow prevention' logic.
  @VisibleForTesting
  long unsafeTestOnlyGetStackDepth() {
    return stackDepthByThread.get().get();
  }

  private String formatHelper(boolean prependDate, String template, Object ... args) {
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

    lock.lock();
    try {
      LongCounter stackDepth = stackDepthByThread.get().increment(); // see definition of stackDepthByThread for more

      if (stackDepth.get() < MAX_STACK_DEPTH) {
        Object[] newArgs = new Object[args.length];
        Arrays.setAll(newArgs, i -> formatSingleObject(args[i]));
        sb.append(Strings.format(template, newArgs));
      } else {
        // We are probably in an infinite recursion by this point
        sb.append(Strings.format(template, args));
      }
      String toReturn = sb.toString();
      stackDepth.decrement();
      return toReturn;
    } finally {
      lock.unlock();
    }
  }

}
