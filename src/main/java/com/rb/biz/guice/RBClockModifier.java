package com.rb.biz.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.LocalDateTime;

/**
 * Lets you modify the clock.
 *
 * <p> This was meant to be a layer of safety: if you need to modify the time, you need to explicitly inject
 * an RBClockModifier, whereas most code would just be injecting an RBClock to just look at the time. </p>
 *
 * <p> However, on Feb 2023, {@link RBClock} was changed to an interface, which means that its
 * RBClock#overwriteCurrentTime method could not be package-private anymore. So this class does not offer that
 * extra safety anymore. FIXME IAK </p>
 */
@Singleton
public class RBClockModifier {

  private static final RBClockModifierToken RB_CLOCK_MODIFIER_TOKEN = new RBClockModifierToken();

  public final static class RBClockModifierToken {

    private RBClockModifierToken() {}

  }

  @Inject RBClock rbClock;

  public void overwriteCurrentTime(LocalDateTime newTime) {
    rbClock.overwriteCurrentTime(RB_CLOCK_MODIFIER_TOKEN, newTime);
  }

}
