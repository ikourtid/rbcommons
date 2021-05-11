package com.rb.nonbiz.math.sequence;

/**
 * For {@link Sequence<Double>}, we have created special implementations. It is useful to have a visitor
 * because it allows us to compare two such objects, because otherwise a plain {@link Sequence} is not a data class
 * and is not easy to compare given the interface that allows it to be return values for arbitrary numeric indices.
 */
public abstract class DoubleSequence implements Sequence<Double> {

  public interface Visitor<T> {

    T visitArithmeticProgression(ArithmeticProgression arithmeticProgression);
    T visitGeometricProgression(GeometricProgression geometricProgression);

  }

  public abstract <T> T visit(Visitor<T> visitor);

}
