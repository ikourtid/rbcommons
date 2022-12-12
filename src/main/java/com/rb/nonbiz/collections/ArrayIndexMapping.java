package com.rb.nonbiz.collections;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A map is a mapping of key to value. You can further break down this abstraction
 * a) the mapping from key to int (starting at 0)
 * b) the collection of values being mapped
 *
 * This class represents just (a).
 * If we share only the mapping part across multiple maps, then we only need to specify the values in each map.
 * This trick allows us to create of maps that are
 * a) more memory-efficient: all the objects are in an array (i.e. consecutive)
 * b) faster, because there are no map lookups (even O(1) ones like in a hashmap) - only array lookups
 *    (in the cases where we know the int positions - e.g. because we stored them).
 * c) possibly faster, if the implementing class does some tricks to make lookup faster. This is the case with
 *    MarketDaysIntMapping.
 *
 * The various classes with 'IndexableArray' in their name use this.
 *
 * Of course, this is only applicable when the keys are unique.
 */
public interface ArrayIndexMapping<T> {

  /**
   * This should throw if numeric index does not appear in the mapping.
   */
  T getKey(int index);

  /**
   * This should throw if the key is not in the mapping.
   */
  int getIndex(T key);

  /**
   * #getIndex is just #getOptionalIndex(object).get(), but it's good to make it explicit
   * when you need the Optional.
   */
  OptionalInt getOptionalIndex(T key);

  int size();

  default boolean isEmpty() {
    return size() == 0;
  }

  default List<T> getAllKeys() {
    return IntStream.range(0, size())
        .mapToObj(i -> getKey(i))
        .collect(Collectors.toList());
  }

  /**
   * Only for the cases where the generic type T is an integer, and this object is non-empty,
   * this will return true if the mapping is the identity function, i.e. number N maps to array position N.
   *
   * <p> Note that this will return false if the mapping is empty, just to be safe, since (due to the way Java
   * type erasure works) we'd have no way of checking what type T is. </p>
   */
  default boolean isTrivialIdentityIntegerMapping() {
    int size = size();
    if (size == 0) {
      return false;
    }
    // OK, so there's at least one item in the mapping. Let's get it.
    T key = getKey(0);
    // We don't like using reflection and instanceof this way, but ArrayIndexMapping does not store the class object
    // of its type T, so we have to resort to this.
    if (!(key instanceof Integer)) {
      return false;
    }
    // There's no way for this class to have different types of keys, because it is generic on T, and all keys are
    // forced to be of type T. Therefore, if the first key is an int, then all must be ints by this point in the code.
    // We don't need to run that check for all of them.
    return IntStream.range(0, size)
        .allMatch(i -> i == (int) getKey(i));
  }

}
