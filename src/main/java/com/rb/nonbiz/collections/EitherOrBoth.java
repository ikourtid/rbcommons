package com.rb.nonbiz.collections;


import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionals.optionalsEqual;

/**
 * Similar to Either, except that this also allows for the possibility that both 'left' and 'right' items exist.
 *
 * @see Either
 * @see EitherOrNeither
 */
public class EitherOrBoth<L, R> {

  private final Optional<L> left;
  private final Optional<R> right;

  private EitherOrBoth(Optional<L> left, Optional<R> right) {
    this.left = left;
    this.right = right;
  }

  public static <L, R> EitherOrBoth<L, R> eitherOrBoth(Optional<L> left, Optional<R> right) {
    RBPreconditions.checkArgument(
        left.isPresent() || right.isPresent(),
        "You must have at least a left or right element in an EitherOrBoth, but you had neither");
    return new EitherOrBoth<L, R>(left, right);
  }

  public static <L, R> EitherOrBoth<L, R> leftOnly(L left) {
    return new EitherOrBoth<L, R>(Optional.of(left), Optional.empty());
  }

  public static <L, R> EitherOrBoth<L, R> rightOnly(R right) {
    return new EitherOrBoth<L, R>(Optional.empty(), Optional.of(right));
  }

  public static <L, R> EitherOrBoth<L, R> both(L left, R right) {
    return new EitherOrBoth<L, R>(Optional.of(left), Optional.of(right));
  }

  // Avoid using this; it's here to help the matcher
  Optional<L> getLeft() {
    return left;
  }

  // Avoid using this; it's here to help the matcher
  Optional<R> getRight() {
    return right;
  }

  public interface EitherOrBothVisitor<L, R, T> {
    T visitLeftOnly(L leftOnly);
    T visitRightOnly(R rightOnly);
    T visitBoth(L left, R right);
  }

  public <T> T visit(EitherOrBothVisitor<L, R, T> eitherOrBothVisitor) {
    return !left.isPresent()
        ? eitherOrBothVisitor.visitRightOnly(right.get())
        : !right.isPresent()
        ? eitherOrBothVisitor.visitLeftOnly(left.get())
        : eitherOrBothVisitor.visitBoth(left.get(), right.get());
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toSimpleString() {
    return toString(false);
  }

  private String toString(boolean clarifyType) {
    return visit(new EitherOrBothVisitor<L, R, String>() {
      @Override
      public String visitLeftOnly(L leftOnly) {
        return clarifyType
            ? Strings.format("either-or-both left only: %s", leftOnly)
            : left.toString();
      }

      @Override
      public String visitRightOnly(R rightOnly) {
        return clarifyType
            ? Strings.format("either-or-both right only: %s", rightOnly)
            : right.toString();
      }

      @Override
      public String visitBoth(L left, R right) {

        return clarifyType
            ? Strings.format("either-or-both: both: %s %s", left, right)
            : Strings.format("%s %s", left, right);
      }
    });
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EitherOrBoth<?, ?> other = (EitherOrBoth<?, ?>) o;
    if (left.getClass() != other.left.getClass()) {
      return false;
    }

    return optionalsEqual(left, other.left) && optionalsEqual(right, other.right);
  }

  // not IDE-generated
  @Override
  public int hashCode() {
    return !left.isPresent()
        ? right.get().hashCode()
        : !right.isPresent()
        ? left.get().hashCode()
        : left.get().hashCode() * 31 + right.get().hashCode();
  }

}
