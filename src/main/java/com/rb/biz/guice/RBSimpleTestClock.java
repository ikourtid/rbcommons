package com.rb.biz.guice;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A simple {@link RBClock} that just stores a {@link LocalDateTime} and doesn't do any tricks with
 * pre-generating thread-local clocks like {@link RBThreadLocalClock} does. We should always use this
 * unless there's a specific need (e.g. when running parallel backtests) to use {@link RBThreadLocalClock}.
 */
public class RBSimpleTestClock implements RBClock {

  public static final ZoneId EAST_COAST_ZONE_ID = ZoneId.of("America/New_York");
  private static final LocalDateTime START_TIME = LocalDateTime.of(1974, 4, 4, 4, 4, 4, 4);

  private LocalDateTime now;

  public RBSimpleTestClock() {
    this(START_TIME);
  }

  public RBSimpleTestClock(LocalDateTime now) {
    this.now = now;
  }

  @Override
  public LocalDateTime now() {
    return this.now;
  }

  /**
   * It is only meant to be called by RBClockModifier.
   * @see RBClockModifier
   */
  @Override
  public void overwriteCurrentTime(LocalDateTime newTime) {
    now = newTime;
  }

}
