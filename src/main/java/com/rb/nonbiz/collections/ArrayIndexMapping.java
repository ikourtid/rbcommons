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

}
