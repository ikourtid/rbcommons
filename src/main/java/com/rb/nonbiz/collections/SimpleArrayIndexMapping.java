package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.RBArrays.arrayIterator;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBOptionals.toSpecializedOptionalInt;
import static java.util.Collections.emptyList;

/**
 * @see ArrayIndexMapping
 *
 * This implements ArrayIndexMapping.
 * The only advantage this implementation gives us is that memory efficiency: we can share this mapping
 * across many IndexableArray objects.
 * There are no time efficiency benefits here, because looking up the numeric index for a particular object key
 * still gets done via a regular map lookup.
 */
public class SimpleArrayIndexMapping<T> implements ArrayIndexMapping<T> {

  private final List<T> objectsInOrder;
  private final RBMap<T, Integer> arrayIndices;

  private SimpleArrayIndexMapping(List<T> objectsInOrder, RBMap<T, Integer> arrayIndices) {
    this.objectsInOrder = objectsInOrder;
    this.arrayIndices = arrayIndices;
  }

  public static <T> SimpleArrayIndexMapping<T> simpleArrayIndexMapping(Collection<T> objects) {
    return simpleArrayIndexMapping(objects.iterator());
  }

  public static <T> SimpleArrayIndexMapping<T> simpleArrayIndexMapping(Iterator<T> objects) {
    RBPreconditions.checkArgument(
        objects.hasNext(),
        "You probably don't want an SimpleArrayIndexMapping with nothing in it");
    MutableRBMap<T, Integer> arrayIndices = newMutableRBMap();
    List<T> objectsInOrder = newArrayList();
    int i = 0;
    while (objects.hasNext()) {
      T object = objects.next();
      objectsInOrder.add(object);
      // putAssumingAbsent would have been more explicit here, but the single precondition below
      // is better for performance.
      arrayIndices.put(object, i);
      i++;
    }
    RBSimilarityPreconditions.checkBothSame(
        objectsInOrder.size(),
        arrayIndices.size(),
        "1 or more keys in the SimpleArrayIndexMapping were equal");
    return new SimpleArrayIndexMapping<T>(objectsInOrder, newRBMap(arrayIndices));
  }

  /**
   * This is for the special case where we want a trivial mapping, which is useful in multidimensional cases
   * where e.g. we want 1 of the 2 dimensions of a 2-d indexable array to have keys (like strings, UniqueId, etc.)
   * but the other one to be 'unindexed', i.e. just be treated like an array. It's like having a spreadsheet with
   * row headers but no column headers, or vice versa.
   */
  public static SimpleArrayIndexMapping<Integer> simpleArrayIndexMappingFromZeroTo(int maxValueInclusive) {
    return simpleArrayIndexMapping(IntStream
        .rangeClosed(0, maxValueInclusive)
        .iterator());
  }

  @VisibleForTesting
  @SafeVarargs
  public static <T> SimpleArrayIndexMapping<T> simpleArrayIndexMapping(T... objects) {
    if (objects.length == 0) {
      // You should use emptySimpleArrayIndexMapping() explicitly; see Issue #549
      return emptySimpleArrayIndexMapping();
    }
    return simpleArrayIndexMapping(arrayIterator(objects));
  }

  @VisibleForTesting
  public static <T> SimpleArrayIndexMapping<T> emptySimpleArrayIndexMapping() {
    return new SimpleArrayIndexMapping<>(emptyList(), emptyRBMap());
  }

  @Override
  public T getKey(int index) {
    if (index < 0 || index >= objectsInOrder.size()) {
      throw new IllegalArgumentException(Strings.format(
          "Index %s is not valid; should be 0 to %s , inclusive, corresponding to items %s through %s",
          index, objectsInOrder.size() - 1, objectsInOrder.get(0), objectsInOrder.get(objectsInOrder.size() - 1)));
    }
    return objectsInOrder.get(index);
  }

  public boolean containsObject(T object) {
    return arrayIndices.containsKey(object);
  }

  @Override
  public int getIndex(T key) {
    Integer index = arrayIndices.getOrThrow(
        key,
        "Object %s is not in mapping; I only have mappings for %s",
        key, arrayIndices.keySet());
    return index;
  }

  @Override
  public OptionalInt getOptionalIndex(T key) {
    return toSpecializedOptionalInt(arrayIndices.getOptional(key));
  }

  @Override
  public int size() {
    return arrayIndices.size();
  }

  /** This only makes sense if there's an ordering of the objects, such as with dates, but not with instruments */
  public T getFirst() {
    return getKey(0);
  }

  /** This only makes sense if there's an ordering of the objects, such as with dates, but not with instruments */
  public T getLast() {
    return getKey(size() - 1);
  }

  @Override
  public String toString() {
    return Strings.format("[SAIM %s SAIM]", Joiner.on(' ').join(objectsInOrder));
  }

}
