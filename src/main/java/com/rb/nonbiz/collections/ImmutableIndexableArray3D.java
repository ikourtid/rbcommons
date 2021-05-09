package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.search.Filter;

import java.util.function.Function;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.MutableIndexableArray3D.mutableIndexableArray3D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;

/**
 * Just like MutableIndexableArray3D, except immutable.
 *
 * Note that someone can modify the underlying object if they have a handle to it via getRawArrayUnsafe()
 * but at least they can't modify it through THIS object; there's no set() method here.
 * Plus, getRawArrayUnsafe is package-private, for extra safety.
 */
public class ImmutableIndexableArray3D<X, Y, Z, V> {

  private final MutableIndexableArray3D<X, Y, Z, V> mutableArray3D;

  private ImmutableIndexableArray3D(MutableIndexableArray3D<X, Y, Z, V> mutableArray3D) {
    this.mutableArray3D = mutableArray3D;
  }

  public static <X, Y, Z, V> ImmutableIndexableArray3D<X, Y, Z, V> immutableIndexableArray3D(
      MutableIndexableArray3D<X, Y, Z, V> mutableArray3D) {
    return new ImmutableIndexableArray3D<>(mutableArray3D);
  }

  public static <X, Y, Z, V> ImmutableIndexableArray3D<X, Y, Z, V> immutableIndexableArray3D(
      V[][][] rawArray,
      ArrayIndexMapping<X> xMapping,
      ArrayIndexMapping<Y> yMapping,
      ArrayIndexMapping<Z> zMapping) {
    return immutableIndexableArray3D(mutableIndexableArray3D(
        rawArray, xMapping, yMapping, zMapping));
  }

  public static <X, Y, Z, V> ImmutableIndexableArray3D<X, Y, Z, V> emptyImmutableIndexableArray3D(V[][][] emptyArray) {
    return immutableIndexableArray3D(
        emptyArray,
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  public V get(X xKey, Y yKey, Z zKey) {
    return mutableArray3D.get(xKey, yKey, zKey);
  }

  public V getByIndex(int xIndex, int yIndex, int zIndex) {
    return mutableArray3D.getByIndex(xIndex, yIndex, zIndex);
  }

  public X getXKey(int xIndex) {
    return mutableArray3D.getXKey(xIndex);
  }

  public Y getYKey(int yIndex) {
    return mutableArray3D.getYKey(yIndex);
  }

  public Z getZKey(int zIndex) {
    return mutableArray3D.getZKey(zIndex);
  }

  public int getSizeInX() {
    return mutableArray3D.getSizeInX();
  }

  public int getSizeInY() {
    return mutableArray3D.getSizeInY();
  }

  public int getSizeInZ() {
    return mutableArray3D.getSizeInZ();
  }

  public ArrayIndexMapping<X> getXMapping() {
    return mutableArray3D.getXMapping();
  }

  public ArrayIndexMapping<Y> getYMapping() {
    return mutableArray3D.getYMapping();
  }

  public ArrayIndexMapping<Z> getZMapping() {
    return mutableArray3D.getZMapping();
  }

  /**
   * XYZ is meant to be a generalization of 'row major iterator' etc. in 3 dimensions.
   * We could add different orderings, if we ever need to.
   */
  public <T> Stream<T> toTransformedStreamInXYZOrder(QuadriFunction<X, Y, Z, V, T> valueTransformer) {
    return mutableArray3D.toTransformedStreamInXYZOrder(valueTransformer);
  }

  /**
   * Transforms this to a 3d array of the same sizes and with the same keys,
   * but with different values, which are transformed based on a combination of the keys (x, y, z)
   * and values.
   *
   * @see #transformValues
   */
  @SuppressWarnings("unchecked") // for the array creation
  public <V1> ImmutableIndexableArray3D<X, Y, Z, V1> transformEntries(
      QuadriFunction<X, Y, Z, V, V1> transformer) {
    V1[][][] newRawArray = (V1[][][]) new Object[getSizeInX()][getSizeInY()][getSizeInZ()];
    MutableIndexableArray3D<X, Y, Z, V1> newMutableArray = mutableIndexableArray3D(
        newRawArray,
        getXMapping(),
        getYMapping(),
        getZMapping());
    getXMapping().getAllKeys()
        .forEach(x -> getYMapping().getAllKeys()
            .forEach(y -> getZMapping().getAllKeys()
                .forEach(z -> newMutableArray.set(x, y, z, transformer.apply(x, y, z, get(x, y, z))))));
    return immutableIndexableArray3D(newMutableArray);
  }

  /**
   * Like #transformEntries, but the transformation does not care about (x, y, z) - just the value in that array cell.
   * @see #transformEntries
   */
  @SuppressWarnings("unchecked") // for the array creation
  public <V1> ImmutableIndexableArray3D<X, Y, Z, V1> transformValues(
      Function<V, V1> transformer) {
    V1[][][] newRawArray = (V1[][][]) new Object[getSizeInX()][getSizeInY()][getSizeInZ()];
    MutableIndexableArray3D<X, Y, Z, V1> newMutableArray = mutableIndexableArray3D(
        newRawArray,
        getXMapping(),
        getYMapping(),
        getZMapping());
    getXMapping().getAllKeys()
        .forEach(x -> getYMapping().getAllKeys()
            .forEach(y -> getZMapping().getAllKeys()
                .forEach(z -> newMutableArray.set(x, y, z, transformer.apply(get(x, y, z))))));
    return immutableIndexableArray3D(newMutableArray);
  }

  /**
   * Returns values (in XYZ order), but not always all:
   * for each dimension, if you specify a 'filter' (single value), then we'll only return that,
   * otherwise we'll look at all values in each dimension.
   * If all filters are Optional.empty, then it will return all values in XYZ order as a stream.
   *
   * I mean 'filter' as in 'database query select filter'.
   * xFilter = Optional.of("a") means 'return all elements that have "a" in the x dimension.
   * xFilter = Optional.empty() means "do not restrict / filter on the x dimension; return all items (that pass the
   *           other filters) without regard to their value in the x coordinate".
   */
  public Stream<V> filteredValuesStreamXYZ(Filter<X> xFilter, Filter<Y> yFilter, Filter<Z> zFilter) {
    return mutableArray3D.filteredValuesStreamXYZ(xFilter, yFilter, zFilter);
  }

  public boolean containsKeys(X xKey, Y yKey, Z zKey) {
    return mutableArray3D.containsKeys(xKey, yKey, zKey);
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  MutableIndexableArray3D<X, Y, Z, V> getMutableArray3D() {
    return mutableArray3D;
  }

  @Override
  public String toString() {
    return mutableArray3D.toString();
  }

}
