package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;

public class RBOptionalTransformers {

  /**
   * Transforms an {@code Optional<A>} into an {@code Optional<B>}, or keeps it empty.
   *
   * This is identical to the standard java Optional#map,
   * except that it assumes the result of the transformer is not null
   * by using Optional.of, whereas #map uses Optional.ofNullable.
   * Also, this syntax is a bit more explicit; #map needs a little more context because it may get confused for the
   * stream map method.
   */
  public static <A, B> Optional<B> transformOptional(Optional<A> optionalA, Function<A, B> transformer) {
    return optionalA.isPresent()
        ? Optional.of(transformer.apply(optionalA.get()))
        : Optional.empty();
  }

  public static <T> Optional<T> transformOptionalInt(OptionalInt optionalA, IntFunction<T> transformer) {
    return optionalA.isPresent()
        ? Optional.of(transformer.apply(optionalA.getAsInt()))
        : Optional.empty();
  }

  public static <T> Optional<T> transformOptionalLong(OptionalLong optionalA, LongFunction<T> transformer) {
    return optionalA.isPresent()
        ? Optional.of(transformer.apply(optionalA.getAsLong()))
        : Optional.empty();
  }

  public static <T> Optional<T> transformOptionalDouble(OptionalDouble optionalA, DoubleFunction<T> transformer) {
    return optionalA.isPresent()
        ? Optional.of(transformer.apply(optionalA.getAsDouble()))
        : Optional.empty();
  }

  /**
   * Just like transformOptional, but for the special case of converting an optional PreciseValue to a double optional.
   */
  public static <T extends PreciseValue<T>> OptionalDouble transformOptionalPreciseValueToOptionalDouble(Optional<T> optional) {
    return optional.isPresent()
        ? OptionalDouble.of(optional.get().doubleValue())
        : OptionalDouble.empty();
  }

  /**
   * Just like transformOptional, but for the special case of converting an optional ImpreciseValue to a double optional.
   */
  public static <T extends ImpreciseValue<T>> OptionalDouble transformOptionalImpreciseValueToOptionalDouble(Optional<T> optional) {
    return optional.isPresent()
        ? OptionalDouble.of(optional.get().doubleValue())
        : OptionalDouble.empty();
  }

  public static <T extends PreciseValue<T>> double transformOptionalPreciseValueToDoubleOrZero(Optional<T> optional) {
    return transformOptionalPreciseValueToOptionalDouble(optional).orElse(0.0);
  }

  public static <T extends ImpreciseValue<T>> double transformOptionalImpreciseValueToDoubleOrZero(Optional<T> optional) {
    return transformOptionalImpreciseValueToOptionalDouble(optional).orElse(0.0);
  }

  /**
   * This is a horrible name, but I can't think of a succinct name which also makes this look related to
   * transformOptional.
   *
   * Similar to transformOptional, except that the transformer method returns an {@code Optional<B>}, not B.
   */
  public static <A, B> Optional<B> transformOptional2(Optional<A> optionalA, Function<A, Optional<B>> transformer) {
    return optionalA.isPresent()
        ? transformer.apply(optionalA.get())
        : Optional.empty();
  }

  /**
   * Transforms 2 optionals into
   * a) a new non-empty optional, if both are nonempty
   * b) Optional.empty otherwise
   *
   * @see RBOptionalTransformers#transformPairOfOptionals
   * (there is &gt; 1 overload)
   */
  public static <A, B, C> Optional<C> transformPairOfOptionals(
      Optional<A> optionalA, Optional<B> optionalB, BiFunction<A, B, C> transformer) {
    return optionalA.isPresent() && optionalB.isPresent()
        ? Optional.of(transformer.apply(optionalA.get(), optionalB.get()))
        : Optional.empty();
  }

  /**
   * Transforms 2 optionals into
   * a) a new non-empty optional, if both are nonempty
   * b) Optional.empty otherwise
   *
   * <p> Throws if exactly 1 of the 2 are empty. </p>
   */
  public static <A, B, C> Optional<C> transformPairOfOptionalsAssumingBothOrNeitherAreEmpty(
      Optional<A> optionalA,
      Optional<B> optionalB,
      BiFunction<A, B, C> bothPresent) {
    if (!optionalA.isPresent() && !optionalB.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(bothPresent.apply(
        getOrThrow(optionalA, "optionalA should be non-empty at this point: %s", optionalA),
        getOrThrow(optionalB, "optionalB should be non-empty at this point: %s", optionalB)));
  }

  /**
   * Transforms 2 optionals into
   * a) a new non-empty optional, if either one is nonempty
   * b) Optional.empty otherwise
   *
   * @see RBOptionalTransformers#transformPairOfOptionals
   * (there is &gt; 1 overload)
   */
  public static <A, B, C> Optional<C> transformPairOfOptionals(
      Optional<A> optionalA,
      Optional<B> optionalB,
      BiFunction<A, B, C> bothPresent,
      Function<A, C> onlyAPresent,
      Function<B, C> onlyBPresent) {
    if (optionalA.isPresent()) {
      A a = optionalA.get();
      return Optional.of(optionalB.isPresent()
          ? bothPresent.apply(a, optionalB.get())
          : onlyAPresent.apply(a));
    }
    return transformOptional(optionalB, b -> onlyBPresent.apply(b));
  }

  /**
   * This is the most general case. It lets us handle the 4 cases of 2 optionals being empty/non-empty
   * by using a visitor, which removes the need to have a bunch of if statements.
   */
  public static <A, B, C> C transformPairOfOptionals(
      Optional<A> optionalA,
      Optional<B> optionalB,
      BiFunction<A, B, C> bothPresent,
      Function<A, C> onlyAPresent,
      Function<B, C> onlyBPresent,
      Supplier<C> neitherPresent) {
    if (optionalA.isPresent()) {
      A a = optionalA.get();
      return optionalB.isPresent()
          ? bothPresent.apply(a, optionalB.get())
          : onlyAPresent.apply(a);
    }
    // OK, so A is not present.
    return optionalB.isPresent()
        ? onlyBPresent.apply(optionalB.get())
        : neitherPresent.get();
  }

  /**
   * This is the most general case. It lets us handle the 4 cases of 2 optionals being empty/non-empty
   * by using a visitor, which removes the need to have a bunch of if statements.
   */
  public static <T> T transformPairOfOptionalDoubles(
      OptionalDouble optionalA,
      OptionalDouble optionalB,
      BiFunction<Double, Double, T> bothPresent,
      DoubleFunction<T> onlyAPresent,
      DoubleFunction<T> onlyBPresent,
      Supplier<T> neitherPresent) {
    if (optionalA.isPresent()) {
      double a = optionalA.getAsDouble();
      return optionalB.isPresent()
          ? bothPresent.apply(a, optionalB.getAsDouble())
          : onlyAPresent.apply(a);
    }
    // OK, so A is not present.
    return optionalB.isPresent()
        ? onlyBPresent.apply(optionalB.getAsDouble())
        : neitherPresent.get();
  }

  /**
   * This is the most general case. It lets us handle the 4 cases of 2 optionals being empty/non-empty
   * by using a visitor, which removes the need to have a bunch of if statements.
   */
  public static <T> T transformPairOfOptionalInts(
      OptionalInt optionalA,
      OptionalInt optionalB,
      BiFunction<Integer, Integer, T> bothPresent,
      IntFunction<T> onlyAPresent,
      IntFunction<T> onlyBPresent,
      Supplier<T> neitherPresent) {
    if (optionalA.isPresent()) {
      int a = optionalA.getAsInt();
      return optionalB.isPresent()
          ? bothPresent.apply(a, optionalB.getAsInt())
          : onlyAPresent.apply(a);
    }
    // OK, so A is not present.
    return optionalB.isPresent()
        ? onlyBPresent.apply(optionalB.getAsInt())
        : neitherPresent.get();
  }

  /**
   * Transforms 2 OptionalDouble objects into
   * a) a new non-empty OptionalDouble, if both are nonempty
   * b) OptionalDouble.empty otherwise
   */
  public static OptionalDouble transformPairOfOptionalDoubles(
      OptionalDouble optionalA, OptionalDouble optionalB, BiFunction<Double, Double, Double> transformer) {
    return optionalA.isPresent() && optionalB.isPresent()
        ? OptionalDouble.of(transformer.apply(optionalA.getAsDouble(), optionalB.getAsDouble()))
        : OptionalDouble.empty();
  }

  /**
   * Transforms 2 OptionalInt objects into
   * a) a new non-empty OptionalInt, if both are nonempty
   * b) OptionalInt.empty otherwise
   */
  public static OptionalInt transformPairOfOptionalInts(
      OptionalInt optionalA, OptionalInt optionalB, BinaryOperator<Integer> transformer) {
    return optionalA.isPresent() && optionalB.isPresent()
        ? OptionalInt.of(transformer.apply(optionalA.getAsInt(), optionalB.getAsInt()))
        : OptionalInt.empty();
  }

  /**
   * For a non-empty optional, it will return it if it passes the predicate. Otherwise it will return empty.
   * For an empty optional, it will return Optional.empty().
   */
  public static <T> Optional<T> transformOptionalWithPredicate(Optional<T> optional, Predicate<T> predicate) {
    if (!optional.isPresent()) {
      return Optional.empty();
    }
    return predicate.test(optional.get())
        ? optional
        : Optional.empty();
  }

  /**
   * When exactly one of the two optionals is present, returns its value (unwrapped from the optional).
   * When both are present, returns a transformed value.
   * When neither is present, throws.
   */
  public static <T> T transformAtLeastOnePresentOptionalOrThrow(
      Optional<T> optional1, Optional<T> optional2, BinaryOperator<T> whenBothPresent) {
    return transformAtLeastOnePresentOptionalOrThrow(optional1, optional2, whenBothPresent,
        () -> "transformAtLeastOnePresentOptionalOfThrow: we can't have both optionals be empty");
  }

  public static <T> T transformAtLeastOnePresentOptionalOrThrow(
      Optional<T> optional1, Optional<T> optional2, BinaryOperator<T> whenBothPresent,
      Supplier<String> errorMessageWhenBothAbsent) {
    if (optional1.isPresent() && optional2.isPresent()) {
      return whenBothPresent.apply(optional1.get(), optional2.get());
    }
    if (!optional1.isPresent() && !optional2.isPresent()) {
      throw new IllegalArgumentException(errorMessageWhenBothAbsent.get());
    }
    return optional1.orElseGet(optional2::get);
  }

  /**
   * Converts an {@link OptionalInt} to an {@link Optional} of {@link Integer}.
   */
  public static Optional<Integer> toNonSpecializedOptional(OptionalInt optionalInt) {
    return optionalInt.isPresent()
        ? Optional.of(optionalInt.getAsInt())
        : Optional.empty();
  }

  /**
   * Converts an {@link OptionalLong} to an {@link Optional} of {@link Long}.
   */
  public static Optional<Long> toNonSpecializedOptional(OptionalLong optionalLong) {
    return optionalLong.isPresent()
        ? Optional.of(optionalLong.getAsLong())
        : Optional.empty();
  }

  /**
   * Converts an {@link OptionalDouble} to an {@link Optional} of {@link Double}.
   */
  public static Optional<Double> toNonSpecializedOptional(OptionalDouble optionalDouble) {
    return optionalDouble.isPresent()
        ? Optional.of(optionalDouble.getAsDouble())
        : Optional.empty();
  }

}
