package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.math.sequence.GeometricProgression.GeometricProgressionBuilder.geometricProgressionBuilder;

/**
 * A functional description of an infinite sequence of numbers that are off by a multiple,
 * e.g. 300, 330, 363, etc.
 * It is sort of like a Stream in that it can be infinite, and like an array or list, in that you can ask for the
 * values in position n >= 0.
 */
public class GeometricProgression extends DoubleSequence {

  private final double initialValue;
  private final double commonRatio;

  private GeometricProgression(double initialValue, double commonRatio) {
    this.initialValue = initialValue;
    this.commonRatio = commonRatio;
  }

  public static GeometricProgression singleValueGeometricProgression(double initialValue) {
    return geometricProgressionBuilder()
        .setInitialValue(initialValue)
        .setCommonRatio(1)
        .build();
  }

  public double getInitialValue() {
    return initialValue;
  }

  public double getCommonRatio() {
    return commonRatio;
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitGeometricProgression(this);
  }

  @Override
  public Double getUnsafe(int nonNegativeN) {
    return initialValue * Math.pow(commonRatio, nonNegativeN);
  }

  @Override
  public String toString() {
    return Strings.format("[GP init= %s ; ratio %s GP]", initialValue, commonRatio);
  }


  public static class GeometricProgressionBuilder implements RBBuilder<GeometricProgression> {

    private Double initialValue;
    private Double commonRatio;

    private GeometricProgressionBuilder() {}

    public static GeometricProgressionBuilder geometricProgressionBuilder() {
      return new GeometricProgressionBuilder();
    }

    public GeometricProgressionBuilder setInitialValue(double initialValue) {
      this.initialValue = checkNotAlreadySet(this.initialValue, initialValue);
      return this;
    }

    public GeometricProgressionBuilder setCommonRatio(double commonRatio) {
      this.commonRatio = checkNotAlreadySet(this.commonRatio, commonRatio);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(initialValue);
      RBPreconditions.checkNotNull(commonRatio);

      // If this precondition hits, it means you probably want singleValueArithmeticProgression for clarity.
      RBPreconditions.checkArgument(
          initialValue != 0,
          "If the value at n=0 is 0, then all other values will also be 0 and you probably didn't mean this: %s %s",
          initialValue, commonRatio);
      RBPreconditions.checkArgument(
          commonRatio != 0,
          "The common ratio in a geometric progression can't be 0: %s %s",
          initialValue, commonRatio);
      // This precondition could have been folded under the previous one, but the reasoning is slightly different,
      // so I'll keep it separate.
      RBPreconditions.checkArgument(
          commonRatio > 0,
          "We only allow the common ratio in a geometric progression to be positive, otherwise the signs will alternate: %s %s",
          initialValue, commonRatio);
    }

    @Override
    public GeometricProgression buildWithoutPreconditions() {
      return new GeometricProgression(initialValue, commonRatio);
    }

  }

}
