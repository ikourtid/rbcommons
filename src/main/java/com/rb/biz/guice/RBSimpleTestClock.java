package com.rb.biz.guice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Tells us what time it is.
 * We wrap all calls to 'what time is it?' so that we can replace 'wall clock' time (real) with
 * whatever time it happens to be during some multi-year simulation that we run.
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
   * This is intentionally package-private, so that users of this method (which will be outside this package)
   * cannot call it. It is only meant to be called by RBClockModifier.
   * @see RBClockModifier
   */
  @Override
  public void overwriteCurrentTime(LocalDateTime newTime) {
    now = newTime;
  }

}
