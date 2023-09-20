package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableSet;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSetWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.text.SmartFormatter.smartFormatCollection;
import static com.rb.nonbiz.text.Strings.sizePrefix;

/**
 * Similar to java.util.Set. However, it is meant to be immutable.
 *
 * Always prefer RBSet to a java.util.Set, especially on an interface, but even inside a method's body, when possible.
 *
 * Guava ImmutableSet implements the Set interface, but its add() method will throw at runtime.
 * However, RBSet intentionally has NO methods to modify it. That offers compile-time safety.
 *
 * @see RBSets for some handy static methods.
 * @see MutableRBSet for a class that helps you initialize an RBSet.
 */
public class RBSet<T> implements Iterable<T> {

  private static final RBSet EMPTY_INSTANCE = new RBSet<>(ImmutableSet.of());

  private final Set<T> rawImmutableSet;

  public static <T> RBSet<T> newRBSet(MutableRBSet<T> mutableRBSet) {
    return mutableRBSet.isEmpty()
        ? EMPTY_INSTANCE // small performance optimization
        : new RBSet<>(mutableRBSet.asSet());
  }

  public static <T> RBSet<T> newRBSet(T...items) {
    if (items.length == 0) {
      return EMPTY_INSTANCE; // small performance optimization
    }
    MutableRBSet<T> mutableSet = newMutableRBSetWithExpectedSize(items.length);
    for (T item : items) {
      mutableSet.addAssumingAbsent(item);
    }
    return newRBSet(mutableSet);
  }

  public static <T> RBSet<T> newRBSet(Collection<T> items) {
    if (items.isEmpty()) {
      return EMPTY_INSTANCE; // small performance optimization
    }
    MutableRBSet<T> mutableSet = newMutableRBSetWithExpectedSize(items.size());
    for (T item : items) {
      mutableSet.addAssumingAbsent(item);
    }
    return newRBSet(mutableSet);
  }

  public static <T> RBSet<T> newRBSet(Iterator<T> items) {
    if (!items.hasNext()) {
      return EMPTY_INSTANCE; // small performance optimization
    }
    MutableRBSet<T> mutableSet = newMutableRBSet();
    while (items.hasNext()) {
      mutableSet.addAssumingAbsent(items.next());
    }
    return newRBSet(mutableSet);
  }

  public static <T> RBSet<T> newRBSet(Stream<T> items) {
    return newRBSet(items.iterator());
  }

  public static <T> RBSet<T> newRBSetFromPossibleDuplicates(T...items) {
    if (items.length == 0) {
      return EMPTY_INSTANCE; // small performance optimization
    }
    MutableRBSet<T> mutableSet = newMutableRBSetWithExpectedSize(items.length);
    for (T item : items) {
      mutableSet.add(item);
    }
    return newRBSet(mutableSet);
  }

  public static <T> RBSet<T> newRBSetFromPossibleDuplicates(Collection<T> items) {
    if (items.isEmpty()) {
      return EMPTY_INSTANCE; // small performance optimization
    }
    MutableRBSet<T> mutableSet = newMutableRBSetWithExpectedSize(items.size());
    for (T item : items) {
      mutableSet.add(item);
    }
    return newRBSet(mutableSet);
  }

  public static <T> RBSet<T> newRBSetFromPossibleDuplicates(Iterator<T> items) {
    if (!items.hasNext()) {
      return EMPTY_INSTANCE; // small performance optimization
    }
    MutableRBSet<T> mutableSet = newMutableRBSet();
    while (items.hasNext()) {
      mutableSet.add(items.next());
    }
    return newRBSet(mutableSet);
  }

  public static <T> RBSet<T> newRBSetFromPossibleDuplicates(Stream<T> items) {
    return newRBSetFromPossibleDuplicates(items.iterator());
  }

  private RBSet(Set<T> rawSet) {
    this.rawImmutableSet = ImmutableSet.copyOf(rawSet);
  }

  private RBSet(ImmutableSet<T> rawImmutableSet) {
    this.rawImmutableSet = rawImmutableSet;
  }

  public static <T> RBSet<T> rbSet(Set<T> rawSet) {
    return rawSet.isEmpty()
        ? EMPTY_INSTANCE
        : new RBSet<>(rawSet);
  }

  /**
   * Unlike ImmutableSet#of, there is no 0-item override for rbSetOf.
   * This is to force you to use emptyRBSet, which is more explicit and makes reading tests easier.
   * Likewise for singletonRBSet().
   */
  public static <T> RBSet<T> emptyRBSet() {
    return EMPTY_INSTANCE;
  }

  /**
   * Unlike ImmutableSet#of, there is no single-item override for rbSetOf.
   * This is to force you to use singletonRBSet, which is more explicit and makes reading tests easier.
   * Likewise for emptyRBSet().
   */
  public static <T> RBSet<T> singletonRBSet(T item) {
    return new RBSet<>(ImmutableSet.of(item));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2) {
    return new RBSet<>(ImmutableSet.of(t1, t2));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2, T t3) {
    return new RBSet<>(ImmutableSet.of(t1, t2, t3));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2, T t3, T t4) {
    return new RBSet<>(ImmutableSet.of(t1, t2, t3, t4));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2, T t3, T t4, T t5) {
    return new RBSet<>(ImmutableSet.of(t1, t2, t3, t4, t5));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2, T t3, T t4, T t5, T t6) {
    return new RBSet<>(ImmutableSet.of(t1, t2, t3, t4, t5, t6));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2, T t3, T t4, T t5, T t6, T t7) {
    return new RBSet<>(ImmutableSet.of(t1, t2, t3, t4, t5, t6, t7));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8) {
    return new RBSet<>(ImmutableSet.of(t1, t2, t3, t4, t5, t6, t7, t8));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9) {
    return new RBSet<>(ImmutableSet.of(t1, t2, t3, t4, t5, t6, t7, t8, t9));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9, T t10) {
    return new RBSet<>(ImmutableSet.of(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9, T t10, T t11) {
    return new RBSet<>(ImmutableSet.of(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11));
  }

  public static <T> RBSet<T> rbSetOf(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9, T t10, T t11, T t12) {
    return new RBSet<>(ImmutableSet.of(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12));
  }

  /**
   * Use this if you want the standard java.util.Set interface, e.g. to use it in some library that
   * can manipulate sets.
   *
   * Be careful with this, because you are exposing the guts of the RBSet, and you have no guarantee that the
   * caller won't modify the set, which would break the convention that RBSet is immutable.
   */
  public Set<T> asSet() {
    return rawImmutableSet;
  }

  public int size() {
    return rawImmutableSet.size();
  }

  public boolean isEmpty() {
    return rawImmutableSet.isEmpty();
  }

  public boolean contains(T t) {
    return rawImmutableSet.contains(t);
  }

  @Override
  public Iterator<T> iterator() {
    return rawImmutableSet.iterator();
  }

  public boolean containsAll(Collection<? extends T> c) {
    return rawImmutableSet.containsAll(c);
  }

  public Stream<T> stream() {
    return rawImmutableSet.stream();
  }

  public RBSet<T> filter(Predicate<T> predicate) {
    return newRBSet(
        stream()
            .filter(predicate)
            .collect(Collectors.toSet()));
  }

  /**
   * Transform a set, but throw an exception if the transformed values aren't all unique.
   *
   * <p> For example, if we transform an {@link RBSet} of ints, and the transformer is i -> "x", meaning we always
   * map to a constant string, then running this method on a set of 2 or more items will always result in
   * an exception. </p>
   */
  public <T1> RBSet<T1> transform(Function<T, T1> transformer) {
    MutableRBSet<T1> transformedSet = newMutableRBSetWithExpectedSize(this.size());
    this.stream().forEach(originalItem -> {
      T1 transformedItem = transformer.apply(originalItem);
      RBPreconditions.checkArgument(
          !transformedSet.contains(transformedItem),
          "This transformation is not one-to-one; this transformed item appears twice: %s",
          transformedItem);
      transformedSet.add(transformedItem);
    });
    return newRBSet(transformedSet);
  }

  /**
   * Transform a set, but throw an exception if the transformed values aren't all unique. Uniqueness is determined by
   * the equals and hashCode methods passed.
   *
   * <p> This is useful for situations where we want a partial equality (i.e. not compare every member),
   * or where there's no equality operation that's well-defined enough to add as a method in the data class,
   * which is something we usually avoid to do. One rare example is if the data class stores verb classes in it,
   * which cannot really be compared, except for their {@link Class} object.</p>
   *
   * <p> Example: T1 is a string filepath, and - for the transformed strings - we only want to compare the
   * last component ('basename' in UNIX command line terms). In this case, not only can we not modify the
   * equals/hashCode on the String class (it's standard Java), but even if it's a class we have control over,
   * we don't want to have such a custom equals method just for this type of comparison. What if we want to
   * compare the full string in other contexts? </p>
   *
   * <p> This is a bit inelegant. An RBSet is supposed to denote two things: that the order is irrelevant,
   * and that all items inside it are not equal, per Object#hashCode() and {@link Object#equals(Object)}.
   * This methods assumes only that the order is irrelevant. The problem is that there is no existing data class that
   * fits here: {@link RBSet} makes both assumptions, and {@link List} makes neither assumption. We could add a data
   * class for that case, with a name like UnorderedCollectionAllowingDuplicates or something.
   * However, it's not worth the complication, because there aren't enough uses currently
   * (Sep 2022). So we have to stretch the semantics of the {@link RBSet} a bit in the cases where we use this method,
   * so that we can expect items that are not equal in the intuitive sense (i.e. where object comparison is just the
   * default pointer comparison you get from Java), but are equal if we were to use the {@link BiPredicate} passed in.
   * </p>
   */
  public <T1> RBSet<T1> transformAllowingDuplicates(
      Function<T, T1> transformer,
      BiPredicate<T1, T1> equalsImplementation,
      Function<T1, Integer> hashCodeImplementation) {
    // Uncommon, but we need to create a class inside this method, which we only use in here.
    class WrapperObject {
      final T1 transformedValue;

      WrapperObject(T1 transformedValue) {
        this.transformedValue = transformedValue;
      }

      @Override
      public int hashCode() {
        return hashCodeImplementation.apply(transformedValue);
      }

      // The IDE gives us a warning about #equals not checking the type of obj, but we don't have access to
      // the class of T1 at runtime (due to Java type erasure), and passing that in is overkill; we don't
      // really need to check here, because this temporary object is internal to this method.
      @SuppressWarnings({ "EqualsWhichDoesntCheckParameterClass", "unchecked" })
      @Override
      public boolean equals(Object obj) {
        return equalsImplementation.test( ((WrapperObject) obj).transformedValue, transformedValue);
      }
    }

    MutableRBSet<WrapperObject> temporarySet = newMutableRBSetWithExpectedSize(this.size());
    // Not using addAssumingAbsent; adding a duplicate will not cause an exception here (that is, duplicates
    // subject to the equalsImplementation and hashCodeImplementation passed in).
    // Because the underlying MutableRBSet will not store two WrapperObjects with the same equals/hashCode,
    // adding a duplicate (again, duplicate per equalsImplementation & hashCodeImplementation) will just
    // replace the previous one, but we don't care, because it's a duplicate anyway.
    // This way, when we're done with all this, all objects will be unique/non-duplicate.
    forEach(originalValue -> temporarySet.add(new WrapperObject(transformer.apply(originalValue))));

    // Finally, we have to remove this wrapper and return a set of the transformed value inside the wrapper.
    return newRBSet(temporarySet).transform(wrapperObject -> wrapperObject.transformedValue);
  }

  /**
   * Transform a set, and don't throw if two or more of the original values map to the same transformed value.
   *
   * <p> For example, if we transform an {@link RBSet} of ints, and the transformer is i -> "x", meaning we always
   * map to a constant string, then running this method on a set of 1 or more items will result in the
   * singleton set "x", and there will be no exception. </p>
   */
  public <T1> RBSet<T1> transformAllowingDuplicates(Function<T, T1> transformer) {
    return new RBSet<>(this.stream().map(transformer).collect(Collectors.toSet()));
  }

  /**
   * This converts this set into an RBMap with the same keys,
   * and whose corresponding values are some function of the key.
   *
   * See also #orderedToRBMap.
   */
  public <V> RBMap<T, V> toRBMap(Function<T, V> valueGenerator) {
    MutableRBMap<T, V> mutableMap = newMutableRBMapWithExpectedSize(size());
    for (T key : rawImmutableSet) {
      mutableMap.putAssumingAbsent(key, valueGenerator.apply(key));
    }
    return newRBMap(mutableMap);
  }

  /**
   * Converts this set into an RBMap whose keys are a subset of the items in this set
   * and whose corresponding values are some function of the key.
   * The valueGenerator passed must return Optional.empty() if a key is not supposed to go into
   * a resulting map, and Optional.of(V) if it is.
   *
   * See also #orderedToRBMap.
   */
  public <V> RBMap<T, V> toRBMapWithFilteredKeys(Function<T, Optional<V>> optionalValueGenerator) {
    MutableRBMap<T, V> mutableMap = newMutableRBMap();
    rawImmutableSet.forEach(key ->
        optionalValueGenerator.apply(key)
            .ifPresent(v -> mutableMap.put(key, v)));
    return newRBMap(mutableMap);
  }

  /**
   * This converts this set into an RBMap with the same keys,
   * and whose corresponding values are some function of the key,
   * but also (unlike toRBMap) whose corresponding keys are also a function of the original set item.
   *
   * See also #orderedToRBMap.
   */
  public <V, K2> RBMap<K2, V> toRBMapWithTransformedKeys(Function<T, K2> keyGenerator, Function<T, V> valueGenerator) {
    MutableRBMap<K2, V> mutableMap = newMutableRBMapWithExpectedSize(size());
    for (T key : rawImmutableSet) {
      mutableMap.putAssumingAbsent(keyGenerator.apply(key), valueGenerator.apply(key));
    }
    return newRBMap(mutableMap);
  }

  /**
   * This converts this set into an RBMap with the same keys,
   * and whose corresponding values are some function of the key.
   * However, the ordering that the valueGenerator is run on the keys is fixed.
   *
   * This matters when valueGenerator has a side effect, such as increasing some internal counter.
   *
   * See also #toRBMap.
   */
  public <V> RBMap<T, V> orderedToRBMap(Function<T, V> valueGenerator, Comparator<T> comparator) {
    MutableRBMap<T, V> mutableMap = newMutableRBMapWithExpectedSize(size());
    rawImmutableSet.stream().sorted(comparator)
        .forEach(key -> mutableMap.put(key, valueGenerator.apply(key)));
    return newRBMap(mutableMap);
  }

  public List<T> toSortedList() {
    return rawImmutableSet.stream().sorted().collect(Collectors.toList());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RBSet<?> rbSet = (RBSet<?>) o;

    return rawImmutableSet.equals(rbSet.rawImmutableSet);
  }

  @Override
  public int hashCode() {
    return rawImmutableSet.hashCode();
  }

  @Override
  public String toString() {
    return sizePrefix(rawImmutableSet.size()) + smartFormatCollection(rawImmutableSet);
  }

}
