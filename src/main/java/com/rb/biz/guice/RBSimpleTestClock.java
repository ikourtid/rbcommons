package com.rb.biz.guice;

import com.rb.biz.guice.RBClockModifier.RBClockModifierToken;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDateTime;

/**
 * A simple {@link RBClock} that just stores a {@link LocalDateTime} and doesn't do any tricks with
 * pre-generating thread-local clocks like {@link RBThreadLocalClock} does. We should always use this
 * unless there's a specific need (e.g. when running parallel backtests) to use {@link RBThreadLocalClock}.
 */
public class RBSimpleTestClock implements RBClock {

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
   * This is only meant to be called (and can only be called) by {@link RBClockModifier}.
   */
  @Override
  public void overwriteCurrentTime(RBClockModifierToken rbClockModifierToken, LocalDateTime newTime) {
    RBPreconditions.checkNotNull(rbClockModifierToken); // see RBClock#overwriteCurrentTime for an explanation
    now = newTime;
  }

}
