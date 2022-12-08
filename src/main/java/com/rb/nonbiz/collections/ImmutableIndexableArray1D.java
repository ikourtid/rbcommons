package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.MutableIndexableArray1D.mutableIndexableArray1D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;

/**
 * Just like {@link MutableIndexableArray1D}, except immutable.
 *
 * <p> Also, just like {@link ImmutableIndexableArray2D}, except this is 1D. </p>
 *
 * <p> Note that someone can modify the underlying object if they have a handle to it via getRawArrayUnsafe()
 * but at least they can't modify it through THIS object; there's no set() method here.
 * Plus, getRawArrayUnsafe is package-private, for extra safety. </p>
 *
 * @see MutableIndexableArray1D
 * @see ImmutableIndexableArray2D
 */
public class ImmutableIndexableArray1D<K, V> {

  private final MutableIndexableArray1D<K, V> mutableArray1D;

  private ImmutableIndexableArray1D(MutableIndexableArray1D<K, V> mutableArray1D) {
    this.mutableArray1D = mutableArray1D;
  }

  public static <K, V> ImmutableIndexableArray1D<K, V> immutableIndexableArray1D(
      MutableIndexableArray1D<K, V> mutableArray1D) {
    return new ImmutableIndexableArray1D<>(mutableArray1D);
  }

  public static <K, V> ImmutableIndexableArray1D<K, V> immutableIndexableArray1D(
      ArrayIndexMapping<K> arrayIndexMapping, V[] rawArray) {
    return immutableIndexableArray1D(mutableIndexableArray1D(arrayIndexMapping, rawArray));
  }

  public static <K, V> ImmutableIndexableArray1D<K, V> emptyImmutableIndexableArray1D(V[] emptyArray) {
    RBPreconditions.checkArgument(emptyArray.length == 0);
    return immutableIndexableArray1D(simpleArrayIndexMapping(), emptyArray);
  }

  @VisibleForTesting // this is here to help the test matcher; do not access the raw mutableArray1D directly.
  MutableIndexableArray1D<K, V> getRawMutableArray1D() {
    return mutableArray1D;
  }

  public V get(K key) {
    return mutableArray1D.get(key);
  }

  public boolean containsKey(K key) {
    return mutableArray1D.containsKey(key);
  }

  public int getIndexOfObject(K key) {
    return mutableArray1D.getIndexOfObject(key);
  }

  public V getByIndex(int index) {
    return mutableArray1D.getByIndex(index);
  }

  public void set(K key, V value) {
    mutableArray1D.set(key, value);
  }

  public void set(int index, V value) {
    mutableArray1D.set(index, value);
  }

  public int size() {
    return mutableArray1D.size();
  }

  public boolean isEmpty() {
    return mutableArray1D.size() == 0;
  }

  public K getKey(int index) {
    return mutableArray1D.getKey(index);
  }

  public List<K> getKeys() {
    return mutableArray1D.getKeys();
  }

  public List<V> getValues() {
    return mutableArray1D.getValues();
  }

  public RBSet<K> getKeysRBSet() {
    return mutableArray1D.getKeysRBSet();
  }

  public Stream<V> valuesStream() {
    return mutableArray1D.valuesStream();
  }

  public void forEachEntry(BiConsumer<K, V> biConsumer) {
    mutableArray1D.forEachEntry(biConsumer);
  }

  public ImmutableIndexableArray1D<K, V> filterKeys(
      IntFunction<V[]> arrayInstantiator, // we need this unfortunately, because arrays don't play well with generics
      Predicate<K> mustKeepKey) {
    List<K> keysToKeep  = newArrayListWithExpectedSize(size()); // using size() a hint; it's an upper bound
    List<V> itemsToKeep = newArrayListWithExpectedSize(size()); // using size() a hint; it's an upper bound
    for (int i = 0; i < size(); i++) {
      K key = getKey(i);
      if (mustKeepKey.test(key)) {
        keysToKeep.add(key);
        itemsToKeep.add(mutableArray1D.getByIndex(i));
      }
    }
    V[] rawArray = arrayInstantiator.apply(itemsToKeep.size());
    itemsToKeep.toArray(rawArray);
    return keysToKeep.isEmpty()
        ? emptyImmutableIndexableArray1D(rawArray)
        : immutableIndexableArray1D(simpleArrayIndexMapping(keysToKeep), rawArray);
  }

  /**
   * Sometimes we can to create a different object that has the same exact keys, but different values.
   * This is an efficient way to do this because the new object will share the ArrayIndexMapping of this object,
   * so there's no need to copy it, which gives us space and time efficiency benefits.
   */
  public <V2> ImmutableIndexableArray1D<K, V2> copyWithValuesReplaced(V2[] valuesForNewObject) {
    return immutableIndexableArray1D(mutableArray1D.copyWithValuesReplaced(valuesForNewObject));
  }

  public <V2> ImmutableIndexableArray1D<K, V2> copyWithEntriesTransformed(TriFunction<Integer, K, V, V2> transformer) {
    return immutableIndexableArray1D(mutableArray1D.copyWithEntriesTransformed(transformer));
  }

  public <V2> ImmutableIndexableArray1D<K, V2> copyWithValuesTransformed(Function<V, V2> transformer) {
    return immutableIndexableArray1D(mutableArray1D.copyWithValuesTransformed(transformer));
  }

  @Override
  public String toString() {
    return mutableArray1D.toString();
  }

}
