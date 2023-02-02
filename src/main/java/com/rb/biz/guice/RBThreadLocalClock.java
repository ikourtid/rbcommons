package com.rb.biz.guice;

import com.google.inject.Singleton;
import com.rb.biz.guice.RBClockModifier.RBClockModifierToken;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A {@link RBClock} that's particularly useful when running multiple backtests in parallel.
 */
@Singleton
public class RBThreadLocalClock implements RBClock {

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

  public RBThreadLocalClock() {
    this(START_TIME);
  }

  public RBThreadLocalClock(LocalDateTime now) {
    getClock().now = now;
  }

  @Override
  public LocalDateTime now() {
    return getClock().now;
  }

  @Override
  public ZonedDateTime nowOnEastCoast() {
    // All production machines should be on East Coast time, because that's kind of standard,
    // as NYSE is on the East Coast, and all exchanges follow by and large the same trading hours (9:30 - 4).
    return now().atZone(EAST_COAST_ZONE_ID);
  }

  /**
   * This is only meant to be called (and can only be called) by RBClockModifier.
   */
  @Override
  public void overwriteCurrentTime(RBClockModifierToken rbClockModifierToken, LocalDateTime newTime) {
    RBPreconditions.checkNotNull(rbClockModifierToken); // see RBClock#overwriteCurrentTime for an explanation
    getClock().now = newTime;
  }

}
