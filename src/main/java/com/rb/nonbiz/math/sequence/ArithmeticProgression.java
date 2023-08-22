package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PositiveMultiplier;

import java.util.function.UnaryOperator;

/**
 * A functional description of an infinite sequence of numbers that increase by a constant amount,
 * e.g. 100, 105, 110, 115, etc.
 *
 * <p> It is sort of like a Stream in that it can be infinite, and like an array or list, in that you can ask for the
 * values in position n >= 0. </p>
 *
 * <p> Note that the generic item T does not itself have to be numeric. For example, we could have an
 * arithmetic progression of the first numeric item in a pair class: ("a", 10), ("a", 12), ("a", 14), etc.
 * However, this generality comes at a cost: the user has to specify the unary operator that gives you item
 * <em> n + 1 </em> from item <em> n </em>. This means that we cannot have a precondition here that the
 * commonDifference is being properly applied by that operator; there is no way to check.
 * However, we will store the commonDifference so that we can (de)serialize this class to/from JSON.
 * The serialization will not have the operator, of course, because we can't serialize lambdas,
 * but the caller of the deserializer code will have the correct context in the code to create the lambda;
 * there won't be a need for anything in the JSON serialization. </p>
 */
public class ArithmeticProgression<T> extends SimpleSequence<T> {

  private final double commonDifference;

  private ArithmeticProgression(T initialValue, double commonDifference, UnaryOperator<T> nextItemGenerator) {
    super(initialValue, nextItemGenerator);
    this.commonDifference = commonDifference;
  }

  public static <T> ArithmeticProgression<T> arithmeticProgression(
      T initialValue, double commonDifference, UnaryOperator<T> nextItemGenerator) {
    return new ArithmeticProgression<>(initialValue, commonDifference, nextItemGenerator);
  }

  public static ArithmeticProgression<Double> doubleArithmeticProgression(
      double initialValue, double commonDifference) {
    return new ArithmeticProgression<>(initialValue, commonDifference, v -> v + commonDifference);
  }

  public static <T> ArithmeticProgression<T> singleValueArithmeticProgression(T initialValue) {
    return arithmeticProgression(
        initialValue,
        0.0,
        // This trick allows us to keep this constructor's method signature simple; we know that it's always going
        // to be the same value, so we can just use the identity function, and not force the user to specify one.
        v -> initialValue);
  }

  public double getCommonDifference() {
    return commonDifference;
  }

  @Override
  public <T2> T2 visit(Visitor<T, T2> visitor) {
    return visitor.visitArithmeticProgression(this);
  }

  @Override
  public String toString() {
    return Strings.format("[AP init= %s ; diff %s AP]", getInitialValue(), commonDifference);
  }

}
