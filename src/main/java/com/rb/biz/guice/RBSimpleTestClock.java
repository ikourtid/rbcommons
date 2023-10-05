package com.rb.biz.guice;

import com.google.common.annotations.VisibleForTesting;
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

  /**
   * Unlike most data classes, RBClock gets injected (Guice), so we have to have a public no-arg constructor.
   * Other code is discouraged from using this. At any rate, you usually need to specify a starting time,
   * so this is not very useful. This is marked {@link VisibleForTesting} just so we can flag any prod usages,
   * but in reality we don't even want any direct usages in tests. It's just here for Guice.
   */
  @VisibleForTesting
  public RBSimpleTestClock() {
    this(START_TIME);
  }

  private RBSimpleTestClock(LocalDateTime now) {
    this.now = now;
  }

  public static RBSimpleTestClock rbSimpleTestClock(LocalDateTime now) {
    return new RBSimpleTestClock(now);
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
