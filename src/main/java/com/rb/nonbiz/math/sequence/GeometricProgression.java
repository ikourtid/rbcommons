package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PositiveMultiplier;

import java.util.function.UnaryOperator;

import static com.rb.nonbiz.types.PositiveMultiplier.POSITIVE_MULTIPLIER_1;

/**
 * A functional description of an infinite sequence of numbers that are off by a multiple,
 * e.g. 300, 330, 363, etc.
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
 * The serialized version will not store the operator, of course, because we can't serialize lambdas,
 * but the caller of the deserializer code will have the correct context in the code to create the lambda;
 * there won't be a need for anything in the JSON serialization. </p>
 *
 * <p> We only allow the common ratio in a geometric progression to be positive, otherwise the signs will alternate.
 * The general mathematical abstraction does allow negative ratios, but in practice we are unlikely to ever want
 * to use that in our applications - it would almost always be due to some error. At least this is the case for the
 * first use case (August 2023): the API can specify expected future net gains with a geometric progression (e.g.
 * $10k every year, increasing by 5% each time). Therefore, we opted to enforce a positive ratio,
 * at the expense of making this class's usage more restrictive. </p>
 */
public class GeometricProgression<T> extends SimpleSequence<T> {

  private final PositiveMultiplier commonRatio;

  private GeometricProgression(T initialValue, PositiveMultiplier commonRatio, UnaryOperator<T> nextItemGenerator) {
    super(initialValue, nextItemGenerator);
    this.commonRatio = commonRatio;
  }

  public static <T> GeometricProgression<T> geometricProgression(
      T initialValue, PositiveMultiplier commonRatio, UnaryOperator<T> nextItemGenerator) {
    return new GeometricProgression<>(initialValue, commonRatio, nextItemGenerator);
  }

  public static GeometricProgression<Double> doubleGeometricProgression(
      double initialValue, PositiveMultiplier commonRatio) {
    return new GeometricProgression<>(initialValue, commonRatio, v -> v * commonRatio.doubleValue());
  }

  public static <T> GeometricProgression<T> constantValueGeometricProgression(T initialValue) {
    return geometricProgression(
        initialValue,
        POSITIVE_MULTIPLIER_1,
        // This trick allows us to keep this constructor's method signature simple; we know that it's always going
        // to be the same value, so we can just use the identity function, and not force the user to specify one.
        v -> initialValue);
  }

  public PositiveMultiplier getCommonRatio() {
    return commonRatio;
  }

  @Override
  public <T2> T2 visit(Visitor<T, T2> visitor) {
    return visitor.visitGeometricProgression(this);
  }

  @Override
  public String toString() {
    return Strings.format("[GP init= %s ; ratio= %s GP]", getInitialValue(), commonRatio);
  }

}


