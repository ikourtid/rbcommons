package com.rb.nonbiz.collections;


import com.google.common.collect.Iterators;
import com.rb.nonbiz.text.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static java.util.Collections.singletonList;

/**
 * Various static methods pertaining to java.util.Optional objects.
 */
public class RBOptionals {

  public static <T> T getOrThrow(Optional<T> optional, String template, Object... args) {
    if (optional.isPresent()) {
      return optional.get();
    }
    throw new IllegalArgumentException(Strings.format(template, args));
  }

  public static int getIntOrThrow(OptionalInt optional, String template, Object... args) {
    if (optional.isPresent()) {
      return optional.getAsInt();
    }
    throw new IllegalArgumentException(Strings.format(template, args));
  }

  /**
   * If you are implementing an equals override for an object, chances are you'll need this overload vs the above.
   * The two types A and B may be the same - or not.
   */
  public static <A, B> boolean optionalsEqual(Optional<A> o1, Optional<B> o2) {
    if (!o1.isPresent()) {
      return !o2.isPresent();
    }
    if (!o2.isPresent()) {
      return false;
    }
    A v1 = o1.get();
    B v2 = o2.get();
    return v1.equals(v2);
  }

  public static boolean allOptionalsPresent(
      Optional<?> first, Optional<?> second, Optional<?> third, Optional<?> ... rest) {
    return first.isPresent() && second.isPresent() && third.isPresent()
        && Arrays.stream(rest).allMatch(v -> v.isPresent());
  }

  public static boolean allOptionalsEmpty(
      Optional<?> first, Optional<?> second, Optional<?> third, Optional<?> ... rest) {
    return !first.isPresent() && !second.isPresent() && !third.isPresent()
        && Arrays.stream(rest).noneMatch(v -> v.isPresent());
  }

  /**
   * Gives you a nice, structured way to run different code based the 2 x 2 cases where two optionals
   * are empty or non-empty,
   * without having a complicated web of 'if' statements to figure out which
   */
  public static <A, B> void visitPairOfOptionals(
      Optional<A> optionalA,
      Optional<B> optionalB,
      BiConsumer<A, B> bothPresent,
      Consumer<A> onlyAPresent,
      Consumer<B> onlyBPresent,
      Runnable neitherPresent) {
    if (optionalA.isPresent()) {
      A a = optionalA.get();
      if (optionalB.isPresent()) {
        bothPresent.accept(a, optionalB.get());
      } else {
        onlyAPresent.accept(a);
      }
    } else {
      if (optionalB.isPresent()) {
        onlyBPresent.accept(optionalB.get());
      } else {
        neitherPresent.run();
      }
    }
  }

  public static <T> List<Optional<T>> toListOfOptionals(List<T> originalList) {
    return originalList
        .stream()
        .map(v -> Optional.of(v))
        .collect(Collectors.toList());
  }

  public static List<OptionalDouble> toListOfOptionalDoubles(List<Double> originalList) {
    return originalList
        .stream()
        .map(v -> OptionalDouble.of(v))
        .collect(Collectors.toList());
  }

  // Surprisingly, this doesn't already exist.
  public static OptionalDouble optionalDoubleOfNullable(Double v) {
    return v == null
        ? OptionalDouble.empty()
        : OptionalDouble.of(v);
  }

  // Surprisingly, this doesn't already exist.
  public static OptionalDouble toSpecializedOptionalDouble(Optional<Double> optional) {
    return optional.isPresent()
        ? OptionalDouble.of(optional.get())
        : OptionalDouble.empty();
  }

  // Surprisingly, this doesn't already exist.
  public static OptionalInt optionalIntOfNullable(Integer v) {
    return v == null
        ? OptionalInt.empty()
        : OptionalInt.of(v);
  }

  // Surprisingly, this doesn't already exist.
  public static OptionalInt toSpecializedOptionalInt(Optional<Integer> optional) {
    return optional.isPresent()
        ? OptionalInt.of(optional.get())
        : OptionalInt.empty();
  }

  /**
   * Creates a mini-stream of 0 or 1 items, depending on whether an optional is empty or not.
   *
   * Apparently a future version of Java (not 8) will support that, but now I have to write it out.
   */
  public static <T> Stream<T> optionalToStream(Optional<T> optional) {
    return optional.isPresent()
        ? singletonList(optional.get()).stream()
        : Collections.<T>emptyList().stream();
  }

  /**
   * This is convenient for cases where you want to retrieve N optional items,
   * but you only want to bother retrieving the K+1-th item if all items up to K were Optional#isPresent.
   */
  public static <T> Optional<List<T>> lazyGetIfPresent(List<Supplier<Optional<T>>> suppliers) {
    List<T> itemsPresent = newArrayListWithExpectedSize(suppliers.size());
    for (Supplier<Optional<T>> supplier : suppliers) {
      Optional<T> item = supplier.get();
      if (!item.isPresent()) {
        return Optional.empty();
      }
      itemsPresent.add(item.get());
    }
    return Optional.of(itemsPresent);
  }

  public static <T> Optional<List<T>> lazyGetIfPresent(Supplier<Optional<T>>...suppliersArray) {
    return lazyGetIfPresent(Arrays.asList(suppliersArray));
  }

  /**
   *
   * Similar functionality as lazyGetIfPresent, except that it's only for a pair, and it is also less general,
   * as you must use the same transformer function for both left and right item (vs arbitrary suppliers).
   */
  public static <A, B> Optional<PairOfSameType<B>> lazyGetPairIfPresent(A left, A right, Function<A, Optional<B>> transformer) {
    Optional<B> maybeLeftResult = transformer.apply(left);
    if (!maybeLeftResult.isPresent()) {
      return Optional.empty();
    }
    Optional<B> maybeRightResult = transformer.apply(right);
    if (!maybeRightResult.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(pairOfSameType(maybeLeftResult.get(), maybeRightResult.get()));
  }

  public static <T> Stream<T> filterPresentOptionalsInStream(Stream<Optional<T>> streamOfOptionals) {
    return streamOfOptionals
        .filter(v -> v.isPresent())
        .map(v -> v.get());
  }

  /**
   * Returns a list that includes only the present items from the optionals passed in (in order),
   * without the Optional wrapper.
   */
  @SafeVarargs
  public static <T> List<T> filterPresentOptionals(Optional<T> first, Optional<T>...rest) {
    return filterPresentOptionalsInStream(
        concatenateFirstAndRest(first, rest)
            .stream())
        .collect(Collectors.toList());
  }

  /**
   * Returns a list of doubles that includes only the present items from the optionals passed in (in order),
   * This applies to the special case of OptionalDouble.
   */
  @SafeVarargs
  public static List<Double> filterPresentOptionalDoubles(OptionalDouble first, OptionalDouble...rest) {
    return concatenateFirstAndRest(first, rest)
        .stream()
        .filter(opt -> opt.isPresent())
        .map(opt -> opt.getAsDouble())
        .collect(Collectors.toList());
  }

  /**
   * Returns a list that includes only the present items from the input list,
   * without the optional wrapper.
   */
  public static <T> List<T> filterPresentOptionals(List<Optional<T>> original) {
    return original
        .stream()
        .filter(opt -> opt.isPresent())
        .map(opt -> opt.get())
        .collect(Collectors.toList());
  }

  /**
   * Returns a list that includes only the present items from the input iterator,
   * without the optional wrapper.
   */
  public static <T> Iterator<T> filterPresentOptionals(Iterator<Optional<T>> original) {
    return Iterators.transform(
        Iterators.filter(original, v -> v.isPresent()),
        v -> v.get());
  }

  /**
   * Returns a list of doubles that includes only the present items from the input list of OptionalDouble items.
   * This applies to the special case of OptionalDouble.
   */
  public static List<Double> filterPresentOptionalDoubles(List<OptionalDouble> original) {
    return original
        .stream()
        .filter(opt -> opt.isPresent())
        .map(opt -> opt.getAsDouble())
        .collect(Collectors.toList());
  }

  public static <V1, V2> void ifBothPresent(Optional<V1> value1, Optional<V2> value2, BiConsumer<V1, V2> biConsumer) {
    value1.ifPresent(v1 ->
        value2.ifPresent(v2 ->
            biConsumer.accept(v1, v2)));
  }

  public static void ifBothPresent(OptionalDouble value1, OptionalDouble value2, BiConsumer<Double, Double> biConsumer) {
    value1.ifPresent(v1 ->
        value2.ifPresent(v2 ->
            biConsumer.accept(v1, v2)));
  }

  public static void ifBothPresent(OptionalInt value1, OptionalInt value2, BiConsumer<Integer, Integer> biConsumer) {
    value1.ifPresent(v1 ->
        value2.ifPresent(v2 ->
            biConsumer.accept(v1, v2)));
  }

  /**
   * If present, return .toString() value. Otherwise return the empty string.
   */
  public static <T> String toStringOrEmpty(Optional<T> optional) {
    return optional.map(v -> v.toString()).orElse("");
  }

  /**
   * If the value is null, leave it null, otherwise transform it.
   * We normally don't deal with nulls, but there are cases where we need to (e.g. 3rd party libraries that use null).
   * This is not really Optional-related, but we have a lot of 'transform' methods for optionals, so this is a good place
   * to put this in.
   */
  public static <V1, V2> Function<V1, V2> transformIfNonNull(Function<V1, V2> transformer) {
    return v -> v == null ? null : transformer.apply(v);
  }

  /**
   * If the value is null, return Optional.empty(), otherwise return Optional.of(some transformation of it).
   * We normally don't deal with nulls, but there are cases where we need to (e.g. 3rd party libraries that use null).
   * This is not really Optional-related, but we have a lot of 'transform' methods for optionals, so this is a good place
   * to put this in.
   */
  public static <V1, V2> Optional<V2> transformFromNullable(V1 value, Function<V1, V2> transformer) {
    return value == null
        ? Optional.empty()
        : Optional.of(transformer.apply(value));
  }

  /**
   * Java 1.9+ has this as a method under Optional, but since we're currently (May 2020) using Java 1.8,
   * this is handy to have as a static method.
   */
  public static <T> void ifPresentOrElse(Optional<T> optional, Consumer<? super T> action, Runnable emptyAction) {
    if (optional.isPresent()) {
      action.accept(optional.get());
    } else {
      emptyAction.run();
    }
  }

}
