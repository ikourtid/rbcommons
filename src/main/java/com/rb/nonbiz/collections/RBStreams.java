package com.rb.nonbiz.collections;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.collections.RBIterables.consecutiveNonOverlappingPairs;
import static com.rb.nonbiz.collections.RBIterables.consecutivePairs;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;

/**
 * Various static utilities pertaining to Java 8 streams.
 */
public class RBStreams {

  /**
   * Creates a stream that results from applying some function of 2 args to the 'column paste' result of 2 iterators.
   *
   * Throws if the two iterators aren't of the same size.
   */
  public static <T1, T2, R> Stream<R> pasteIntoStream(
      Iterator<T1> iterator1, Iterator<T2> iterator2, BiFunction<T1, T2, R> function) {
    Builder<R> builder = Stream.<R>builder();
    while (iterator1.hasNext()) {
      RBPreconditions.checkArgument(
          iterator2.hasNext(),
          "There are more items for the 1st arg than for the 2nd arg");
      T1 item1 = iterator1.next();
      T2 item2 = iterator2.next();
      builder.add(function.apply(item1, item2));
    }
    RBPreconditions.checkArgument(
        !iterator2.hasNext(),
        "There are more items for the 2nd arg than for the 1st arg");
    return builder.build();
  }

  /**
   * Creates a stream that results from applying some function of 2 args
   * to the i-th item on the left collection and i-th item on the right collection, for all i.
   *
   * Throws if the two iterators aren't of the same size.
   */
  public static <T1, T2, R> Stream<R> pasteIntoStream(
      Collection<T1> collection1, Collection<T2> collection2, BiFunction<T1, T2, R> function) {
    return pasteIntoStream(collection1.iterator(), collection2.iterator(), function);
  }

  /**
   * Creates a stream that results from applying some function of 2 args
   * to the i-th item on the left list and i-th item on the right list, for all i.
   *
   * Throws if the two iterators aren't of the same size.
   */
  public static <T1, T2, R> Stream<R> pasteIntoStream(
      List<T1> list1, List<T2> list2, BiFunction<T1, T2, R> function) {
    return pasteIntoStream(list1.iterator(), list2.iterator(), function);
  }


  /**
   * Creates a stream that results from applying some function of 3 args
   * to the i-th item on the 3 iterators, for all i.
   *
   * Throws if the three iterators aren't of the same size.
   */
  public static <T1, T2, T3, R> Stream<R> pasteIntoStream(
      Iterator<T1> iterator1, Iterator<T2> iterator2, Iterator<T3> iterator3,
      TriFunction<T1, T2, T3, R> function) {
    Builder<R> builder = Stream.<R>builder();
    while (iterator1.hasNext()) {
      RBPreconditions.checkArgument(
          iterator2.hasNext(),
          "There are more items for the 1st arg than for the 2nd arg");
      RBPreconditions.checkArgument(
          iterator3.hasNext(),
          "There are more items for the 1st arg than for the 3rd arg");
      T1 item1 = iterator1.next();
      T2 item2 = iterator2.next();
      T3 item3 = iterator3.next();
      builder.add(function.apply(item1, item2, item3));
    }
    RBPreconditions.checkArgument(
        !iterator2.hasNext(),
        "There are more items for the 2nd arg than for the 1st arg");
    RBPreconditions.checkArgument(
        !iterator3.hasNext(),
        "There are more items for the 3nd arg than for the 1st arg");
    return builder.build();
  }

  /**
   * Creates a stream that results from applying some function of 3 args
   * to the i-th item on the 3 collections, for all i.
   *
   * Throws if the three collections aren't of the same size.
   */
  public static <T1, T2, T3, R> Stream<R> pasteIntoStream(
      Collection<T1> collection1, Collection<T2> collection2, Collection<T3> collection3,
      TriFunction<T1, T2, T3, R> function) {
    return pasteIntoStream(collection1.iterator(), collection2.iterator(), collection3.iterator(), function);
  }

  /**
   * Creates a stream that results from applying some function of 3 args
   * to the i-th item on the 3 lists, for all i.
   *
   * Throws if the three lists aren't of the same size.
   */
  public static <T1, T2, T3, R> Stream<R> pasteIntoStream(
      List<T1> list1, List<T2> list2, List<T3> list3, TriFunction<T1, T2, T3, R> function) {
    return pasteIntoStream(list1.iterator(), list2.iterator(), list3.iterator(), function);
  }

  public static BigDecimal sumBigDecimals(Stream<BigDecimal> stream) {
    return stream.reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static BigDecimal sumBigDecimals(Collection<BigDecimal> collection) {
    return sumBigDecimals(collection.iterator());
  }

  public static BigDecimal sumBigDecimals(Iterator<BigDecimal> iterator) {
    BigDecimal sum = BigDecimal.ZERO;
    while (iterator.hasNext()) {
      sum = sum.add(iterator.next());
    }
    return sum;
  }

  public static BigDecimal multiplyBigDecimals(Iterator<BigDecimal> iterator) {
    BigDecimal product = BigDecimal.ONE;
    while (iterator.hasNext()) {
      product = product.multiply(iterator.next(), DEFAULT_MATH_CONTEXT);
    }
    return product;
  }

  /**
   * Adds a bunch of PreciseValue objects and returns the result as a BigDecimal.
   */
  public static <T extends PreciseValue<? super T>> BigDecimal sumAsBigDecimals(Stream<T> stream) {
    return sumBigDecimals(stream
        .map(pv -> pv.asBigDecimal()));
  }

  /**
   * Adds a bunch of PreciseValue objects and returns the result as a BigDecimal.
   */
  public static <T extends PreciseValue<? super T>> BigDecimal sumAsBigDecimals(Collection<T> collection) {
    return sumBigDecimals(collection
        .stream()
        .map(pv -> pv.asBigDecimal()));
  }

  /**
   * Returns the sum of a bunch of BigDecimal objects.
   * Throws if 1 or more is negative.
   */
  public static BigDecimal sumNonNegativeBigDecimals(Stream<BigDecimal> stream) {
    return sumNonNegativeBigDecimals(stream.iterator());
  }

  /**
   * Returns the sum of a bunch of BigDecimal objects.
   * Throws if 1 or more is negative.
   */
  public static BigDecimal sumNonNegativeBigDecimals(Iterator<BigDecimal> iterator) {
    return sumBigDecimals(Iterators.transform(iterator, v -> {
      RBPreconditions.checkArgument(
          v.signum() >= 0,
          "Negative value of %s is not allowed here",
          v);
      return v;
    }));
  }

  public static <T extends PreciseValue<T>> double sumNonNegativePreciseValuesToDouble(Collection<T> collection) {
    return sumNonNegativePreciseValuesToDouble(collection.stream());
  }

  public static <T extends PreciseValue<T>> double sumNonNegativePreciseValuesToDouble(Stream<T> stream) {
    return stream
        .mapToDouble(v -> {
          BigDecimal bd = v.asBigDecimal();
          RBPreconditions.checkArgument(
              bd.signum() >= 0,
              "Negative value of %s is not allowed in this non-negative stream",
              v);
          return bd.doubleValue();
        })
        .sum();
  }

  /**
   * Adds a bunch of BigDecimal objects, and returns the result as a double.
   *
   * Throws if any are negative.
   *
   * If you don't care about the extra precision (e.g. because you have to use some math logic that only uses
   * doubles and not BigDecimals), and just want a double, then use this. It's faster.
   */
  public static double sumNonNegativeBigDecimalsToDouble(Stream<BigDecimal> stream) {
    return stream
        .mapToDouble(v -> {
          RBPreconditions.checkArgument(
              v.signum() >= 0,
              "Negative value of %s is not allowed in this non-negative stream",
              v);
          return v.doubleValue();
        })
        .sum();
  }

  public static double averageOfDoubleStream(DoubleStream doubleStream) {
    return averageOfDoubleIterator(doubleStream.iterator());
  }

  public static double averageOfDoubleIterator(Iterator<Double> iterator) {
    double count = 0;
    double sum = 0;
    if (!iterator.hasNext()) {
      throw new IllegalArgumentException("You cannot take the average of a stream with 0 items");
    }
    while (iterator.hasNext()) {
      sum += iterator.next();
      count++;
    }
    return sum / count;
  }

  /**
   * This does not have the performance benefits of a stream, as it precomputes each value (i.e. no lazy evaluation).
   * However,
   * a) it's nicer syntactically, as it doesn't expose the user to {@code PairOfSameType<T>}
   * b) it's shorter to write
   */
  public static <T, V> Stream<V> consecutivePairsStream(Iterable<T> iterable,
                                                        BiFunction<T, T, V> consecutivePairTransformer) {
    return consecutivePairs(iterable)
        .stream()
        .map(pair -> consecutivePairTransformer.apply(pair.getLeft(), pair.getRight()));
  }

  /**
   * This does not have the performance benefits of a stream, as it precomputes each value (i.e. no lazy evaluation).
   * However,
   * a) it's nicer syntactically, as it doesn't expose the user to {@code PairOfSameType<T>}
   * b) it's shorter to write
   */
  public static <T, V> Stream<V> consecutiveNonOverlappingPairsStream(
      Iterable<T> iterable,
      BiFunction<T, T, V> pairTransformer) {
    return consecutiveNonOverlappingPairs(iterable)
        .stream()
        .map(pair -> pairTransformer.apply(pair.getLeft(), pair.getRight()));
  }

  public static <T> boolean streamItemsAreUnique(Stream<T> stream) {
    // This works because add returns true if the item didn't already exist
    Set<T> items = Sets.newHashSet();
    return stream.allMatch(item -> items.add(item));
  }

  /**
   * Returns the item we encounter that is non-unique (out of possibly many) in the stream passed in.
   *
   * <p> Uniqueness is determined not by running equals on the stream item, but on a field of the
   * item that we extract (e.g. some numeric ID), based on the extractor lambda passed in. </p>
   *
   * <p> Note that we didn't say 'returns the first non-unique item'. If the stream has items X1, Y1, X2, X3,
   * and uniqueness is determined e.g. by the first letter, then this will return X2, not X1. </p>
   *
   * @param <T> The datatype of the item
   * @param <F> The datatype of the field of the item that comparison is based on
   */
  public static <T, F> Optional<T> findFirstNonUniqueItemEncountered(
      Stream<T> stream,
      Function<T, F> uniquenessFieldExtractor) {
    // This works because add returns true if the item didn't already exist
    Set<F> items = Sets.newHashSet();
    Iterator<T> iter = stream.iterator();
    while (iter.hasNext()) {
      T item = iter.next();
      boolean isUnique = items.add(uniquenessFieldExtractor.apply(item));
      if (!isUnique) {
        return Optional.of(item);
      }
    }
    return Optional.empty();
  }

  /**
   * For any items whose key appears more than once (with the key determined by a supplied lambda),
   * returns those items in a map, grouped by their key, whose values are the list of all items that share that key.
   *
   * @param <T> The datatype of the item
   * @param <F> The datatype of the field of the item that comparison is based on
   */
  public static <T, F> RBMap<F, List<T>> findDuplicateStreamItems(
      Stream<T> stream,
      Function<T, F> uniquenessKeyExtractor) {
    // This works because add returns true if the item didn't already exist
    return newRBMap(stream.collect(Collectors.groupingBy(uniquenessKeyExtractor)))
        .filterValues(list -> list.size() >= 2);
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  @SafeVarargs
  public static <K> Stream<K> concatenateFirstAndRest(K first, K...rest) {
    return Stream.concat(
        Stream.of(first),
        Arrays.stream(rest));
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  @SafeVarargs
  public static DoubleStream concatenateFirstAndRestDoubles(double first, double...rest) {
    return DoubleStream.concat(
        DoubleStream.of(first),
        Arrays.stream(rest));
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least two items. But it can be used anywhere.
   */
  @SafeVarargs
  public static <K> Stream<K> concatenateFirstSecondAndRest(K first, K second, K...rest) {
    return Stream.concat(
        Stream.of(first, second),
        Arrays.stream(rest));
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least two items. But it can be used anywhere.
   */
  @SafeVarargs
  public static DoubleStream concatenateFirstSecondAndRestDoubles(double first, double second, double...rest) {
    return DoubleStream.concat(
        DoubleStream.of(first, second),
        Arrays.stream(rest));
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least three items. But it can be used anywhere.
   */
  @SafeVarargs
  public static <K> Stream<K> concatenateFirstSecondThirdAndRest(K first, K second, K third, K...rest) {
    return Stream.concat(
        Stream.of(first, second, third),
        Arrays.stream(rest));
  }

  /**
   * If e.g. the keys are "a", "b", "c", this returns a stream of ("a", "b"), ("a", "c"), ("b", "c")
   */
  public static <K> Stream<PairOfSameType<K>> streamForEachUniqueUnorderedPairInList(List<K> items) {
    RBPreconditions.checkArgument(
        items.size() >= 2,
        "We must have at least 2 items if we want to look at all the pairs: we had %s",
        items);
    return IntStream.range(0, items.size())
        .boxed()
        .flatMap(i1 -> IntStream.range(i1 + 1, items.size())
            .mapToObj(i2 -> pairOfSameType(items.get(i1), items.get(i2))));
  }

  /**
   * E.g. if you pass in keys A, B, C, D, this will operate on AB/BA, AC/CA, AD/DA, BC/CB, BD/DB, CD/DC.
   * It expressly avoids looking at any pair (X, X), hence 'unique' (though that's not a great adjective here).
   */
  public static <K> Stream<PairOfSameType<K>> streamForEachUniquePair(RBSet<K> items) {
    RBPreconditions.checkArgument(
        items.size() >= 2,
        "We must have at least 2 items if we want to look at all the pairs: we had %s",
        items);
    return items
        .stream()
        .flatMap(left -> items
            .stream()
            .filter(right -> !left.equals(right))
            .map(right -> pairOfSameType(left, right)));
  }

  /**
   * Tells you if a stream is empty, but note that it consumes the stream so you can't reuse it!
   */
  public static <T> boolean streamIsEmpty(Stream<T> stream) {
    return !stream.iterator().hasNext();
  }

  /**
   * A bit like Iterables.getOnlyElement, except that it allows for a default value if there's no element in the stream,
   * and also allows for a clean precondition message if there are 2 or more elements.
   */
  public static <T> T getOnlyElementOrDefault(
      Stream<T> stream, T defaultValue, String format, Object ... args) {
    Iterator<T> iter = stream.iterator();

    if (!iter.hasNext()) {
      return defaultValue;
    }
    T onlyValue = iter.next();
    RBPreconditions.checkArgument(
        !iter.hasNext(),
        format,
        args);
    return onlyValue;
  }

  /**
   * If the stream has 1 item, returns it as non-empty optional.
   * If it has 0 items, returns empty optional.
   * Otherwise throws (with the supplied message) if there are 2 or more elements.
   */
  public static <T> Optional<T> getOptionalOnlyElement(
      Stream<T> stream, String format, Object ... args) {
    Iterator<T> iter = stream.iterator();

    if (!iter.hasNext()) {
      return Optional.empty();
    }
    T onlyValue = iter.next();
    RBPreconditions.checkArgument(
        !iter.hasNext(),
        format,
        args);
    return Optional.of(onlyValue);
  }

  /**
   * {@link Stream#concat(Stream, Stream)} only takes two arguments. You should still use it if you want to
   * concatenate only 2 streams, but for 3 or more, the code will look unnecessarily nested with multiple calls to it.
   * This solves that problem.
   */
  @SafeVarargs
  public static <T> Stream<T> concatenateStreams(Stream<T> first, Stream<T> second, Stream<T> third, Stream<T> ... rest) {
    return Stream.concat(
        Stream.concat(
            Stream.concat(
                first,
                second),
            third),
        Arrays.stream(rest).flatMap(v -> v));
  }

}
