package com.rb.nonbiz.collections;


import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionals.optionalsEqual;

/**
 * A mini-collection that represents a pair where either the 'left' or the 'right' object exists,
 * i.e. we're not allowed to have neither (or both) be specified.
 */
public class Either<L, R> {

  private final Optional<L> left;
  private final Optional<R> right;

  private Either(Optional<L> left, Optional<R> right) {
    this.left = left;
    this.right = right;
  }

  public static <L, R> Either<L, R> left(L left) {
    return new Either<>(Optional.of(left), Optional.empty());
  }

  public static <L, R> Either<L, R> right(R right) {
    return new Either<>(Optional.empty(), Optional.of(right));
  }

  public interface Visitor<L, R, T> {

    T visitLeft(L left);
    T visitRight(R right);

  }

  /**
   * See visitBothAssumingSameSide.
   */
  public interface BothVisitor<L, R, T> {

    T visitLefts(L left1, L left2);
    T visitRights(R right1, R right2);

  }

  public <T> T visit(Visitor<L, R, T> visitor) {
    return left.isPresent()
        ? visitor.visitLeft(left.get())
        : visitor.visitRight(right.get());
  }

  /**
   * When you are looking at 2 'eithers' of the same type,
   * this helps you separate out when both 'eithers' only have a value on the left side,
   * vs when both 'eithers' only have a value on the right side.
   * If the non-empty side is not the same for both 'eithers', this will throw.
   */
  public static <L, R, T> T visitBothAssumingSameSide(
      Either<L, R> item1, Either<L, R> item2, BothVisitor<L, R, T> bothVisitor) {
    if (item1.left.isPresent()) {
      RBPreconditions.checkArgument(
          item2.left.isPresent(),
          "BothVisitor encountered a case where item1 ( %s ) is left and item2 ( %s ) is right",
          item1.left, item2.right);
      return bothVisitor.visitLefts(item1.left.get(), item2.left.get());
    }
    if (item1.right.isPresent()) {
      RBPreconditions.checkArgument(
          item2.right.isPresent(),
          "BothVisitor encountered a case where item1 ( %s ) is right and item2 ( %s ) is left",
          item1.right, item2.left);
      return bothVisitor.visitRights(item1.right.get(), item2.right.get());
    }
    throw new IllegalArgumentException(Strings.format(
        "Internal error; should not get here; item1= %s ; item2= %s",
        item1, item2));
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toSimpleString() {
    return toString(false);
  }

  private String toString(boolean clarifyItIsAnEither) {
    return visit(new Visitor<L, R, String>() {
      @Override
      public String visitLeft(L left) {
        return clarifyItIsAnEither
            ? Strings.format("either-left: %s", left.toString())
            : left.toString();
      }

      @Override
      public String visitRight(R right) {
        return clarifyItIsAnEither
            ? Strings.format("either-right: %s", right.toString())
            : right.toString();
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

    Either<?, ?> other = (Either<?, ?>) o;
    if (left.getClass() != other.left.getClass()) {
      return false;
    }

    return optionalsEqual(left, other.left) && optionalsEqual(right, other.right);
  }

  // not IDE-generated
  @Override
  public int hashCode() {
    return visit(new Visitor<L, R, Integer>() {
      @Override
      public Integer visitLeft(L left) {
        return left.hashCode();
      }

      @Override
      public Integer visitRight(R right) {
        return right.hashCode();
      }
    });
  }

}
