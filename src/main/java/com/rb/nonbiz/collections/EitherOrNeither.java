package com.rb.nonbiz.collections;


import com.rb.nonbiz.collections.Either.Visitor;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

/**
 * Similar to Either, except that this also allows for the possibility that neither 'left' nor 'right' items exist.
 *
 * @see Either
 * @see EitherOrBoth
 */
public class EitherOrNeither<L, R> {

  private final Optional<L> left;
  private final Optional<R> right;

  private EitherOrNeither(Optional<L> left, Optional<R> right) {
    this.left = left;
    this.right = right;
  }

  public static <L, R> EitherOrNeither<L, R> eitherOrNeitherFromOptionalEither(Optional<Either<L, R>> optionalEither) {
    if (!optionalEither.isPresent()) {
      return neither();
    }
    return optionalEither.get().visit(new Visitor<L, R, EitherOrNeither<L, R>>() {
      @Override
      public EitherOrNeither<L, R> visitLeft(L left) {
        return EitherOrNeither.left(left);
      }

      @Override
      public EitherOrNeither<L, R> visitRight(R right) {
        return EitherOrNeither.right(right);
      }
    });
  }

  public static <L, R> EitherOrNeither<L, R> eitherOrNeither(Optional<L> left, Optional<R> right) {
    RBPreconditions.checkArgument(
        !left.isPresent() || !right.isPresent(),
        "You must have at most a left or right element in an EitherOrNeither, but you had both");
    return new EitherOrNeither<L, R>(left, right);
  }

  public static <L, R> EitherOrNeither<L, R> left(L left) {
    return new EitherOrNeither<L, R>(Optional.of(left), Optional.empty());
  }

  public static <L, R> EitherOrNeither<L, R> right(R right) {
    return new EitherOrNeither<L, R>(Optional.empty(), Optional.of(right));
  }

  public static <L, R> EitherOrNeither<L, R> neither() {
    return new EitherOrNeither<L, R>(Optional.empty(), Optional.empty());
  }

  // Avoid using this; it's here to help the matcher
  Optional<L> getLeft() {
    return left;
  }

  // Avoid using this; it's here to help the matcher
  Optional<R> getRight() {
    return right;
  }

  public interface EitherOrNeitherVisitor<L, R, T> {

    T visitLeft(L left);
    T visitRight(R right);
    T visitNeither();

  }

  public <T> T visit(EitherOrNeitherVisitor<L, R, T> visitor) {
    return left.isPresent() ? visitor.visitLeft(left.get())
        : right.isPresent() ? visitor.visitRight(right.get())
        : visitor.visitNeither();
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toSimpleString() {
    return toString(false);
  }

  private String toString(boolean clarifyType) {
    return visit(new EitherOrNeitherVisitor<L, R, String>() {
      @Override
      public String visitLeft(L left) {
        return clarifyType
            ? Strings.format("either-or-neither left only: %s", left)
            : left.toString();
      }

      @Override
      public String visitRight(R right) {
        return clarifyType
            ? Strings.format("either-or-neither right only: %s", right)
            : right.toString();
      }

      @Override
      public String visitNeither() {
        return clarifyType
            ? "either-or-neither: neither"
            : "neither";
      }
    });
  }

}
