package com.rb.nonbiz.collections;


import com.google.common.collect.Iterators;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional2;
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;
import static java.util.Collections.singletonList;

/**
 * Various static methods pertaining to java.util.Optional objects.
 */
public class RBOptionals {

  public static <T> T getOrThrow(Optional<T> optional, String template, Object... args) {
    if (optional.isPresent()) {
      return optional.get();
    }
    throw new IllegalArgumentException(smartFormat(template, args));
  }

  public static int getIntOrThrow(OptionalInt optional, String template, Object... args) {
    if (optional.isPresent()) {
      return optional.getAsInt();
    }
    throw new IllegalArgumentException(smartFormat(template, args));
  }

  public static long getLongOrThrow(OptionalLong optional, String template, Object... args) {
    if (optional.isPresent()) {
      return optional.getAsLong();
    }
    throw new IllegalArgumentException(smartFormat(template, args));
  }

  public static double getDoubleOrThrow(OptionalDouble optional, String template, Object... args) {
    if (optional.isPresent()) {
      return optional.getAsDouble();
    }
    throw new IllegalArgumentException(smartFormat(template, args));
  }

  /**
   * Returns true if two optionals (of the same generic type) are both present, OR both empty.
   *
   * <p> Returns false otherwise. </p>
   */
  public static <T> boolean optionalsHaveSameIsPresent(Optional<T> optional1, Optional<T> optional2) {
    return optional1.isPresent() == optional2.isPresent();
  }

  /**
   * If you are implementing an equals override for an object, chances are you'll need this overload vs the above.
   * The two types A and B may be the same - or not.
   */
  public static <A, B> boolean optionalsEqual(Optional<A> o1, Optional<B> o2) {
    return optionalsEqual(o1, o2, (v1, v2) -> v1.equals(v2));
  }

  /**
   * Similar to the 2-argument method, except that, instead of relying on #equals and #hashCode,
   * an arbitrary equality operator can be passed in.
   *
   * <p> This is useful for situations where we want a partial equality (i.e. not compare every member),
   * or where there's no equality operation that's well-defined enough to add as a method in the data class,
   * which is something we usually avoid to do. One rare example is if the data class stores verb classes in it,
   * which cannot really be compared, except for their {@link Class} object. </p>
   */
  public static <A, B> boolean optionalsEqual(Optional<A> o1, Optional<B> o2, BiPredicate<A, B> equalityPredicate) {
    if (!o1.isPresent()) {
      return !o2.isPresent();
    }
    if (!o2.isPresent()) {
      return false;
    }
    A v1 = o1.get();
    B v2 = o2.get();
    return equalityPredicate.test(v1, v2);
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
   * Returns true if the optional is present and its value satisfies the supplied predicate.
   */
  public static <T> boolean optionalSatisfies(Optional<T> optional, Predicate<T> predicate) {
    return optional.isPresent() && predicate.test(optional.get());
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

  /**
   * Extracts a value out of 2 optionals, both of the same type, where exactly one is empty and the other one present.
   *
   * <p> Throws unless exactly 1 of the 2 are empty. </p>
   */
  public static <A, B> B extractFromPairOfOptionalsOfSameTypeAssumingExactlyOneIsEmpty(
      Optional<A> optional1,
      Optional<A> optional2,
      Function<A, B> extractor) {
    if (optional1.isPresent()) {
      RBPreconditions.checkArgument(!optional2.isPresent());
      return extractor.apply(optional1.get());
    }
    return extractor.apply(getOrThrow(optional2, "The 1st optional was empty, so the 2nd has to be present"));
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
   * If the first supplier returns non-empty, return that value.
   * Then, if the second supplier returns non-empty, return that value.
   * Otherwise, return the value from the last supplier.
   *
   * We could have done this with varargs, but then we'd have to put the supplier of the non-optional as the
   * 1st arg, and that would be inconsistent with the logical ordering of the steps as the caller would see them,
   * because the 1st supplier would really be the last step.
   */
  public static <T> T lazyGetFirstPresentOptionalOrElse(
      Supplier<Optional<T>> supplier1,
      Supplier<Optional<T>> supplier2,
      Supplier<T> supplier3) {
    Optional<T> value1 = supplier1.get();
    if (value1.isPresent()) {
      return value1.get();
    }
    Optional<T> value2 = supplier2.get();
    if (value2.isPresent()) {
      return value2.get();
    }
    return supplier3.get();
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

  public static DoubleStream filterPresentOptionalDoublesInStream(Stream<OptionalDouble> streamOfOptionals) {
    return streamOfOptionals
        .filter(v -> v.isPresent())
        .mapToDouble(v -> v.getAsDouble());
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
   * Returns the count of non-empty (present) optionals.
   */
  public static int countPresentOptionals(Optional<?> first, Optional<?> ... rest) {
    int count = first.isPresent() ? 1 : 0;
    for (Optional<?> restItem : rest) {
      if (restItem.isPresent()) {
        count++;
      }
    }
    return count;
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
   * If present, return .toString() value. Otherwise return the empty string.
   */
  public static <T extends PrintsInstruments> String toStringOrEmpty(
      Optional<T> optional, InstrumentMaster instrumentMaster, LocalDate date) {
    return optional.map(v -> v.toString(instrumentMaster, date)).orElse("");
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
   * Return a non-empty optional of the 2nd argument if the boolean is true,
   * otherwise return an empty optional.
   */
  public static <V> Optional<V> makeNonEmptyOptionalIf(boolean makeNonEmptyOptional, V value) {
    return makeNonEmptyOptional ? Optional.of(value) : Optional.empty();
  }

  /**
   * Return a non-empty optional of the 2nd argument if the boolean is true,
   * otherwise return an empty optional.
   *
   * <p> Use this instead of the other overload in the cases where the value in the supplier is slow to calculate,
   * or if it would cause an exception in the cases where it doesn't end up getting used. That's because the Java
   * interpreter will evaluate the 2nd argument because it has to be passed in. </p>
   */
  public static <V> Optional<V> makeNonEmptyOptionalIf(boolean makeNonEmptyOptional, Supplier<V> valueSupplier) {
    return makeNonEmptyOptional ? Optional.of(valueSupplier.get()) : Optional.empty();
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

  /**
   * Java 1.9+ has this as a method under OptionalInt, but since we're currently (May 2020) using Java 1.8,
   * this is handy to have as a static method.
   */
  public static void ifIntPresentOrElse(OptionalInt optional, IntConsumer action, Runnable emptyAction) {
    if (optional.isPresent()) {
      action.accept(optional.getAsInt());
    } else {
      emptyAction.run();
    }
  }

  /**
   * Java 1.9+ has this as a method under OptionalLong, but since we're currently (May 2020) using Java 1.8,
   * this is handy to have as a static method.
   */
  public static void ifLongPresentOrElse(OptionalLong optional, LongConsumer action, Runnable emptyAction) {
    if (optional.isPresent()) {
      action.accept(optional.getAsLong());
    } else {
      emptyAction.run();
    }
  }

  public static <V1, V2> Optional<V2> findFirstPresentOptional(
      Collection<V1> collection,
      Function<V1, Optional<V2>> transformer) {
    for (V1 item : collection) {
      Optional<V2> transformed = transformer.apply(item);
      if (transformed.isPresent()) {
        return transformed;
      }
    }
    return Optional.empty();
  }

  /**
   * Throws if neither or both optionals passed in are empty.
   * Otherwise, it returns the value inside the (only) non-empty optional.
   */
  public static <T> T findOnlyPresentOptional(Optional<T> opt1, Optional<T> opt2) {
    return getOrThrow(
        findZeroOrOnePresentOptionalsFromStream(Stream.of(opt1, opt2)),
        "Expected to find exactly one present optional: inputs were %s %s",
        opt1, opt2);
  }

  /**
   * Throws unless exactly one of the 3 optionals passed in is present.
   * Otherwise, it returns the value inside the (only) non-empty optional.
   */
  public static <T> T findOnlyPresentOptional(Optional<T> opt1, Optional<T> opt2, Optional<T> opt3) {
    return getOrThrow(
        findZeroOrOnePresentOptionalsFromStream(Stream.of(opt1, opt2, opt3)),
        "Expected to find exactly one present optional: inputs were %s %s %s",
        opt1, opt2, opt3);
  }

  /**
   * Throws if more than one of the optionals passed in is present.
   * If all are empty, returns empty optional, otherwise it returns the only non-empty optional.
   */
  public static <T> Optional<T> findZeroOrOnePresentOptional(Optional<T> opt1, Optional<T> opt2, Optional<T> opt3) {
    return findZeroOrOnePresentOptionalsFromStream(Stream.of(opt1, opt2, opt3));
  }

  public static <T> Optional<T> findZeroOrOnePresentOptionalsFromStream(Stream<Optional<T>> stream) {
    int numPresent = 0;
    T toReturn = null;

    Iterator<Optional<T>> iter = stream.iterator();
    while (iter.hasNext()) {
      Optional<T> next = iter.next();
      if (next.isPresent()) {
        numPresent++;
        toReturn = next.get();
      }
    }
    RBPreconditions.checkArgument(
        numPresent <= 1,
        "Expected to find up to 1 present optional; found %s of them",
        numPresent);
    return Optional.ofNullable(toReturn);
  }

  /**
   * Returns whether a 'path' / 'chain' of optionals results in a present optional.
   *
   * <p> We mean 'path' in the sense of a file path, but for an object that contains subobjects, etc.
   * This is easier to explain if you see the tests. </p>
   */
  public static <A, B> boolean pathOfOptionalsPresent(Optional<A> optional, Function<A, Optional<B>> extractor) {
    return transformOptional2(
        optional,
        v -> extractor.apply(v))
        .isPresent();
  }

  /**
   * Returns whether a 'path' / 'chain' of optionals results in a present optional.
   *
   * <p> We mean 'path' in the sense of a file path, but for an object that contains subobjects, etc.
   * This is easier to explain if you see the tests. </p>
   */
  public static <A, B, C> boolean pathOfOptionalsPresent(
      Optional<A> optional,
      Function<A, Optional<B>> extractor1,
      Function<B, Optional<C>> extractor2) {
    return transformOptional2(
        optional,
        v -> transformOptional2(
            extractor1.apply(v),
            v2 -> extractor2.apply(v2)))
        .isPresent();
  }

}
