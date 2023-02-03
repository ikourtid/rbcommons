package com.rb.biz.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.LocalDateTime;

/**
 * Lets you modify the clock.
 *
 * <p> This is meant to be a layer of safety: if you need to modify the time, you need to explicitly inject
 * an RBClockModifier, whereas most code would just be injecting an RBClock to just look at the time. </p>
 *
 * <p> Although there is a {@link RBClock#overwriteCurrentTime(RBClockModifierToken, LocalDateTime)} method,
 * it can only be called from {@link RBClockModifier}, because of the trick of passing a
 * {@link RBClockModifierToken} which no other code can instantiate except for {@link RBClockModifier}. </p>
 */
@Singleton
public class RBClockModifier {

  private static final RBClockModifierToken RB_CLOCK_MODIFIER_TOKEN = new RBClockModifierToken();

  /**
   * This class holds nothing and does nothing. See {@link RBClock#overwriteCurrentTime} for more.
   */
  public final static class RBClockModifierToken {

    private RBClockModifierToken() {}

  }

  @Inject RBClock rbClock;

  public void overwriteCurrentTime(LocalDateTime newTime) {
    rbClock.overwriteCurrentTime(RB_CLOCK_MODIFIER_TOKEN, newTime);
  }

}
