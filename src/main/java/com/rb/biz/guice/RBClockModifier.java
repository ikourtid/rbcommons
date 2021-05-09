package com.rb.biz.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lets you modify the clock.
 *
 * We *could* have such a method on RBClock itself.
 * However, this provides a layer of safety: if you need to modify the time, you need to explicitly inject
 * an RBClockModifier, whereas most code would just be injecting an RBClock to just look at the time.
 */
@Singleton
public class RBClockModifier {

  private static final int DEFAULT_BACKTEST_TIME_HOURS = 16;

  @Inject RBClock rbClock;

  public void overwriteCurrentTime(LocalDateTime newTime) {
    rbClock.overwriteCurrentTime(newTime);
  }

  public void overwriteDateDefaultBacktestTime(LocalDate newDate) {
    overwriteCurrentTime(newDate.atStartOfDay().plusHours(DEFAULT_BACKTEST_TIME_HOURS));
  }

}
