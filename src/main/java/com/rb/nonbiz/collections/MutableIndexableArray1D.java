package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;

/**
 * An indexable array is like a regular array, except that you can also access it based on some
 * more meaningful key - not just an integer index.
 *
 * Why not use a map instead? This is a performance optimization for cases where 3rd party libraries
 * operate using arrays. This way, we won't have to convert back and forth from arrays (needed by the 3rd party library)
 * to our own implementation (e.g. maps of arrays, maps of maps, etc.)
 */
public class MutableIndexableArray1D<K, V> {

  private final ArrayIndexMapping<K> arrayIndexMapping;
  private final V[] rawArray;

  private MutableIndexableArray1D(ArrayIndexMapping<K> arrayIndexMapping, V[] rawArray) {
    this.arrayIndexMapping = arrayIndexMapping;
    this.rawArray = rawArray;
  }

  public static <K, V> MutableIndexableArray1D<K, V> mutableIndexableArray1D(ArrayIndexMapping<K> arrayIndexMapping, V[] rawArray) {
    RBPreconditions.checkArgument(
        arrayIndexMapping.size() == rawArray.length,
        "IndexableArray1D has %s objects in the mapping, but %s items in the raw array passed in",
        arrayIndexMapping.size(), rawArray.length);
    return new MutableIndexableArray1D<>(arrayIndexMapping, rawArray);
  }

  @VisibleForTesting
  public static <K, V> MutableIndexableArray1D<K, V> emptyMutableIndexableArray1D() {
    return mutableIndexableArray1D(simpleArrayIndexMapping(), (V[]) new Object[] {});
  }

  public V get(K key) {
    int index = arrayIndexMapping.getIndex(key);
    return rawArray[index];
  }

  public boolean containsKey(K key) {
    return arrayIndexMapping.getOptionalIndex(key).isPresent();
  }

  public int getIndexOfObject(K key) {
    return arrayIndexMapping.getIndex(key);
  }

  public V getByIndex(int index) {
    RBPreconditions.checkArgument(
        0 <= index && index < rawArray.length,
        "Invalid index of %s ; should be between 0 and %s , inclusive",
        index, rawArray.length - 1);
    return rawArray[index];
  }

  public void set(K key, V value) {
    int index = arrayIndexMapping.getIndex(key);
    rawArray[index] = value;
  }

  public void set(int index, V value) {
    RBPreconditions.checkArgument(
        0 <= index && index < rawArray.length,
        "Invalid index of %s ; should be between 0 and %s , inclusive",
        index, rawArray.length - 1);
    rawArray[index] = value;
  }

  public int size() {
    return rawArray.length;
  }

  public K getKey(int index) {
    return arrayIndexMapping.getKey(index);
  }

  @VisibleForTesting
  ArrayIndexMapping<K> getRawArrayIndexMapping() {
    return arrayIndexMapping;
  }

  @VisibleForTesting
  V[] getRawArrayUnsafe() {
    return rawArray;
  }

  public List<K> getKeys() {
    return IntStream
        .range(0, size())
        .mapToObj(i -> getKey(i))
        .collect(Collectors.toList());
  }

  public RBSet<K> getKeysRBSet() {
    return newRBSet(IntStream
        .range(0, size())
        .mapToObj(i -> getKey(i))
        .collect(Collectors.toSet()));
  }

  public List<V> getValues() {
    return IntStream.range(0, size())
        .mapToObj(i -> getByIndex(i))
        .collect(Collectors.toList());
  }

  public Stream<V> valuesStream() {
    return IntStream.range(0, size()).mapToObj(i -> getByIndex(i));
  }

  public void forEachEntry(BiConsumer<K, V> biConsumer) {
    IntStream.range(0, size())
        .forEach(i -> biConsumer.accept(getKey(i), getByIndex(i)));
  }

  /**
   * Sometimes we can to create a different object that has the same exact keys, but different values.
   * This is an efficient way to do this because the new object will share the ArrayIndexMapping of this object,
   * so there's no need to copy it, which gives us space and time efficiency benefits.
   */
  public <V2> MutableIndexableArray1D<K, V2> copyWithValuesReplaced(V2[] valuesForNewObject) {
    return mutableIndexableArray1D(arrayIndexMapping, valuesForNewObject);
  }

  @Override
  public String toString() {
    return Joiner.on(" ; ").join(
        IntStream
            .range(0, size())
            .mapToObj(i -> Strings.format("%s -> %s", getKey(i), getByIndex(i)))
            .iterator());
  }

}
