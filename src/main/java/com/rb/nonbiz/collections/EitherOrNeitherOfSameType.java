package com.rb.nonbiz.collections;


import com.rb.nonbiz.collections.EitherOrNeither.EitherOrNeitherVisitor;

import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.collections.EitherOrNeither.eitherOrNeither;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Like {@link EitherOrNeither}, except that both items are of the same type.
 */
public class EitherOrNeitherOfSameType<T> {

  private final EitherOrNeither<T, T> rawValue;

  private EitherOrNeitherOfSameType(EitherOrNeither<T, T> rawValue) {
    this.rawValue = rawValue;
  }

  public static <T> EitherOrNeitherOfSameType<T> eitherOrNeitherOfSameType(Optional<T> left, Optional<T> right) {
    return new EitherOrNeitherOfSameType<>(eitherOrNeither(left, right));
  }

  public static <T> EitherOrNeitherOfSameType<T> left(T left) {
    return new EitherOrNeitherOfSameType<>(EitherOrNeither.left(left));
  }

  public static <T> EitherOrNeitherOfSameType<T> right(T right) {
    return new EitherOrNeitherOfSameType<>(EitherOrNeither.right(right));
  }

  public static <T> EitherOrNeitherOfSameType<T> neither() {
    return new EitherOrNeitherOfSameType<>(EitherOrNeither.neither());
  }

  // Avoid using this; it's here to help the matcher

  public EitherOrNeither<T, T> getRawValue() {
    return rawValue;
  }

  public <T2> T2 visit(EitherOrNeitherVisitor<T, T, T2> eitherOrNeitherVisitor) {
    return rawValue.visit(eitherOrNeitherVisitor);
  }

  public List<T> asList() {
    return rawValue.visit(new EitherOrNeitherVisitor<T, T, List<T>>() {
      @Override
      public List<T> visitLeft(T left) {
        return singletonList(left);
      }

      @Override
      public List<T> visitRight(T right) {
        return singletonList(right);
      }

      @Override
      public List<T> visitNeither() {
        return emptyList();
      }
    });
  }

  @Override
  public String toString() {
    return rawValue.toString();
  }

  public String toSimpleString() {
    return rawValue.toSimpleString();
  }

}
