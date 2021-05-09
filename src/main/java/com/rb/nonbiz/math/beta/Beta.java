package com.rb.nonbiz.math.beta;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

/**
 * This is just a typesafe double.
 */
public class Beta {

  public static final Beta BETA_OF_1 = beta(1.0);

  private final double value;
  private final Optional<BetaBackground> betaBackground;

  private Beta(double value, Optional<BetaBackground> betaBackground) {
    this.value = value;
    this.betaBackground = betaBackground;
  }

  public static Beta beta(double value) {
    return beta(value, Optional.empty());
  }

  public static Beta betaWithBackground(double value, BetaBackground betaBackground) {
    return beta(value, Optional.of(betaBackground));
  }

  /**
   * Throws if not within a more-than-reasonable range.
   * I observed, in very few cases, betas of around -20 when generating,
   * but this is when I wasn't able to exclude weird stuff like 3x leveraged
   * So let's keep this precondition loose, since we're in a data class,
   * and then the verb classes can handle this more intelligently.
   */
  public static Beta beta(double value, Optional<BetaBackground> betaBackground) {
    RBPreconditions.checkArgument(
        -30 < value && value < 30,
        "It is very unlikely that you'll ever see a beta that's not within [-30, 30]; saw %s",
        value);
    return new Beta(value, betaBackground);
  }

  public double getValue() {
    return value;
  }

  public Optional<BetaBackground> getBetaBackground() {
    return betaBackground;
  }

  @Override
  public String toString() {
    return Strings.format("[B %s %s B]", value, betaBackground);
  }

}
