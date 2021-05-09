package com.rb.nonbiz.collections;


import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.collections.EitherOrBoth.EitherOrBothVisitor;

import java.util.List;
import java.util.Optional;

import static com.rb.nonbiz.collections.EitherOrBoth.eitherOrBoth;
import static java.util.Collections.singletonList;

/**
 * @see EitherOrBoth
 * This is similar, except that the left and right side of the 'either' are of the same type.
 */
public class EitherOrBothOfSameType<T> {

  private final EitherOrBoth<T, T> rawValue;

  private EitherOrBothOfSameType(EitherOrBoth<T, T> rawValue) {
    this.rawValue = rawValue;
  }

  public static <T> EitherOrBothOfSameType<T> eitherOrBothOfSameType(Optional<T> left, Optional<T> right) {
    return new EitherOrBothOfSameType<>(eitherOrBoth(left, right));
  }

  public static <T> EitherOrBothOfSameType<T> leftOnly(T left) {
    return new EitherOrBothOfSameType<>(EitherOrBoth.leftOnly(left));
  }

  public static <T> EitherOrBothOfSameType<T> rightOnly(T right) {
    return new EitherOrBothOfSameType<>(EitherOrBoth.rightOnly(right));
  }

  public static <T> EitherOrBothOfSameType<T> both(T left, T right) {
    return new EitherOrBothOfSameType<>(EitherOrBoth.both(left, right));
  }

  // Avoid using this; it's here to help the matcher

  public EitherOrBoth<T, T> getRawValue() {
    return rawValue;
  }

  public <T2> T2 visit(EitherOrBothVisitor<T, T, T2> eitherOrBothVisitor) {
    return rawValue.visit(eitherOrBothVisitor);
  }

  public List<T> asList() {
    return rawValue.visit(new EitherOrBothVisitor<T, T, List<T>>() {
      @Override
      public List<T> visitLeftOnly(T leftOnly) {
        return singletonList(leftOnly);
      }

      @Override
      public List<T> visitRightOnly(T rightOnly) {
        return singletonList(rightOnly);
      }

      @Override
      public List<T> visitBoth(T left, T right) {
        return ImmutableList.of(left, right);
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

  // IDE-generated
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EitherOrBothOfSameType<?> that = (EitherOrBothOfSameType<?>) o;
    return rawValue.equals(that.rawValue);
  }

  // not IDE-generated
  @Override
  public int hashCode() {
    return rawValue.hashCode();
  }

}
