package com.rb.nonbiz.math.sequence;

import java.util.function.UnaryOperator;

/**
 * This is for special implementations for well-known sequences, such as geometric and arithmetic.
 * It is useful to have a visitor because it allows us to compare two such objects,
 * because otherwise a plain {@link Sequence} is not a data class
 * and is not easy to compare given the interface that allows it to be return values for arbitrary numeric indices.
 */
public abstract class SimpleSequence<T> extends Sequence<T> {

  protected SimpleSequence(T initialValue, UnaryOperator<T> nextItemGenerator) {
    super(initialValue, nextItemGenerator);
  }

  public interface Visitor<T, T2> {

    T2 visitConstantSequence(ConstantSequence<T> constantSequence);
    T2 visitArithmeticProgression(ArithmeticProgression<T> arithmeticProgression);
    T2 visitGeometricProgression(GeometricProgression<T> geometricProgression);

  }

  public abstract <T2> T2 visit(Visitor<T, T2> visitor);

}
