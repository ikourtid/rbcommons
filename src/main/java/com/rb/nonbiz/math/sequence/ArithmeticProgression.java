package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.math.sequence.ArithmeticProgression.ArithmeticProgressionBuilder.arithmeticProgressionBuilder;

/**
 * A functional description of an infinite sequence of numbers that increase by a constant amount,
 * e.g. 100, 105, 110, 115, etc.
 * It is sort of like a Stream in that it can be infinite, and like an array or list, in that you can ask for the
 * values in position n >= 0.
 */
public class ArithmeticProgression extends DoubleSequence {

  private final double initialValue;
  private final double commonDifference;

  private ArithmeticProgression(double initialValue, double commonDifference) {
    this.initialValue = initialValue;
    this.commonDifference = commonDifference;
  }

  public static ArithmeticProgression singleValueArithmeticProgression(double initialValue) {
    return arithmeticProgressionBuilder()
        .setInitialValue(initialValue)
        .setCommonDifference(0)
        .build();
  }

  public double getInitialValue() {
    return initialValue;
  }

  public double getCommonDifference() {
    return commonDifference;
  }

  @Override
  public <T> T visit(Visitor<T> visitor) {
    return visitor.visitArithmeticProgression(this);
  }

  @Override
  public Double getUnsafe(int nonNegativeN) {
    return initialValue + nonNegativeN * commonDifference;
  }

  @Override
  public String toString() {
    return Strings.format("[AP init= %s ; diff %s AP]", initialValue, commonDifference);
  }


  public static class ArithmeticProgressionBuilder implements RBBuilder<ArithmeticProgression> {

    private Double initialValue;
    private Double commonDifference;

    private ArithmeticProgressionBuilder() {}

    public static ArithmeticProgressionBuilder arithmeticProgressionBuilder() {
      return new ArithmeticProgressionBuilder();
    }

    public ArithmeticProgressionBuilder setInitialValue(double initialValue) {
      this.initialValue = checkNotAlreadySet(this.initialValue, initialValue);
      return this;
    }

    public ArithmeticProgressionBuilder setCommonDifference(double commonDifference) {
      this.commonDifference = checkNotAlreadySet(this.commonDifference, commonDifference);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(initialValue);
      RBPreconditions.checkNotNull(commonDifference);
    }

    @Override
    public ArithmeticProgression buildWithoutPreconditions() {
      return new ArithmeticProgression(initialValue, commonDifference);
    }

  }

}
