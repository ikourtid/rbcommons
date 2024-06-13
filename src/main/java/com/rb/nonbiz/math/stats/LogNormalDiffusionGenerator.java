package com.rb.nonbiz.math.stats;

import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import java.util.function.DoubleFunction;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;

/**
 * Generates a list of log-normally distributed cumulatively multiplied values for the specified size,
 * using a supplied random number generator that generates normally distributed numbers.
 *
 * <p> The best example is price diffusion, which is especially useful when generating synthetic market
 * data for a Monte Carlo simulation. E.g. price starts at 100, and then randomly moves up by e.g. 1.05 or down
 * by 1 / 1.05 (NOT 0.95 - that's the point of the log normal distribution. </p>
 *
 * <p> Log normal distributions are the best model for stock prices, because they can't go negative.
 * Intuitively, it's like saying that a stock is just as likely to double as it is to halve.
 * You might say "then why not just buy the stock and make instant profits, since on average it will gain?".
 * But that's a long discussion. </p>
 */
public class LogNormalDiffusionGenerator {

  public <T extends RBNumeric<T>> List<T> generate(
      DoubleFunction<T> instantiator,
      T initialValue,
      MutableNormalDistributionGenerator mutableNormalDistributionGenerator,
      int size) {
    RBPreconditions.checkArgument(
        size > 0,
        "Size must be positive; was %s",
        size);

    List<T> values = newArrayListWithExpectedSize(size);
    values.add(initialValue);

    double cumulativeMultiplier = initialValue.doubleValue();

    // Starting at i = 1 because i = 0 is covered already by 'initialValue'.
    for (int i = 1; i < size; i++) {
      // Generating a log normal distribution out of a normal distribution.
      double normalMovement = mutableNormalDistributionGenerator.nextDouble();
      double logNormalMovement = Math.exp(normalMovement);
      cumulativeMultiplier *= logNormalMovement;
      values.add(instantiator.apply(cumulativeMultiplier));
    }

    return values;
  }

}
