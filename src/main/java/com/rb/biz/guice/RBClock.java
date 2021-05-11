package com.rb.biz.guice;

import com.google.inject.Singleton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Tells us what time it is.
 * We wrap all calls to 'what time is it?' so that we can replace 'wall clock' time (real) with
 * whatever time it happens to be during some multi-year simulation that we run.
 */
@Singleton
public class RBClock {

  public static final ZoneId EAST_COAST_ZONE_ID = ZoneId.of("America/New_York");
  private static final LocalDateTime START_TIME = LocalDateTime.of(1974, 4, 4, 4, 4, 4, 4);

  private static final Map<Long, ThreadLocalClock> clocks = newHashMap();

  private static class ThreadLocalClock {

    private LocalDateTime now;

    private ThreadLocalClock(LocalDateTime now) {
      this.now = now;
    }

  }

  synchronized public ThreadLocalClock getClock() {
    long threadId = Thread.currentThread().getId();
    ThreadLocalClock clock = clocks.get(threadId);
    if (clock == null) {
      clock = new ThreadLocalClock(START_TIME);
      clocks.put(threadId, clock);
    }
    return clock;
  }

  public RBClock() {
    this(START_TIME);
  }

  public RBClock(LocalDateTime now) {
    getClock().now = now;
  }

  public LocalDateTime now() {
    return getClock().now;
  }

  public ZonedDateTime nowOnEastCoast() {
    // All production machines should be on East Coast time, because that's kind of standard,
    // as NYSE is on the East Coast, and all exchanges follow by and large the same trading hours (9:30 - 4).
    return now().atZone(EAST_COAST_ZONE_ID);
  }

  public LocalDate today() {
    ThreadLocalClock clock = getClock();
    LocalDateTime now = clock.now;
    return now.toLocalDate();
  }

  /**
   * This is intentionally package-private, so that users of this method (which will be outside this package)
   * cannot call it. It is only meant to be called by RBClockModifier.
   * @see RBClockModifier
   */
  void overwriteCurrentTime(LocalDateTime newTime) {
    getClock().now = newTime;
  }

}
