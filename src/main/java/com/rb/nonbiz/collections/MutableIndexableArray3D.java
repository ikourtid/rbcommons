package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.search.Filter;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An indexable array is like a regular array, except that you can also access it based on some
 * more meaningful key - not just an integer index.
 *
 * Why not use a map instead? This is a performance optimization for cases where 3rd party libraries
 * operate using arrays. This way, we won't have to convert back and forth from arrays (needed by the 3rd party library)
 * to our own implementation (e.g. maps of arrays, maps of maps, etc.)
 *
 * Also, if you know you're dealing with square data (all row/column combinations valid)
 * there's only 2 mappings that are stored. Contrast with storing an {@code RBMap<R, RBMap<C, Double>>}
 * where you have O(n) mappings separately stored in the N different submaps.
 * Finally, an IndexableArray2D makes the 'square data' semantics explicit,
 * unlike a map of maps which could be jagged.
 *
 * IndexableArray2D is a general implementation that works on objects.
 * There are several specialized implementations for primitives; e.g. see DoubleIndexableArray2D.
 */
public class MutableIndexableArray3D<X, Y, Z, V> {

  private final V[][][] rawArray;
  private final ArrayIndexMapping<X> xMapping;
  private final ArrayIndexMapping<Y> yMapping;
  private final ArrayIndexMapping<Z> zMapping;

  private MutableIndexableArray3D(
      V[][][] rawArray,
      ArrayIndexMapping<X> xMapping,
      ArrayIndexMapping<Y> yMapping,
      ArrayIndexMapping<Z> zMapping) {
    this.rawArray = rawArray;
    this.xMapping = xMapping;
    this.yMapping = yMapping;
    this.zMapping = zMapping;
  }

  public static <X, Y, Z, V> MutableIndexableArray3D<X, Y, Z, V> mutableIndexableArray3D(
      V[][][] rawArray,
      ArrayIndexMapping<X> xMapping,
      ArrayIndexMapping<Y> yMapping,
      ArrayIndexMapping<Z> zMapping) {
    long numEmpty = Stream.of(xMapping, yMapping, zMapping)
        .filter(mapping -> mapping.isEmpty())
        .count();
    if (numEmpty == 0) {
      // normal case of a non-empty 3d array
      RBSimilarityPreconditions.checkBothSame(rawArray.length,       xMapping.size(), "xMapping has the wrong size in this 3d array");
      RBSimilarityPreconditions.checkBothSame(rawArray[0].length,    yMapping.size(), "yMapping has the wrong size in this 3d array");
      RBSimilarityPreconditions.checkBothSame(rawArray[0][0].length, zMapping.size(), "zMapping has the wrong size in this 3d array");
    } else {
      RBPreconditions.checkArgument(
          numEmpty == 3,
          "If only %s (but not all 3) of the ArrayIndexMappings are empty, we'd have e.g. a 5x0x4 array, which is wrong",
          numEmpty);
    }
    return new MutableIndexableArray3D<>(rawArray, xMapping, yMapping, zMapping);
  }

  public V get(X xKey, Y yKey, Z zKey) {
    return rawArray[xMapping.getIndexOrThrow(xKey)][yMapping.getIndexOrThrow(yKey)][zMapping.getIndexOrThrow(zKey)];
  }

  public V getByIndex(int xIndex, int yIndex, int zIndex) {
    return rawArray[xIndex][yIndex][zIndex];
  }

  public void set(X xKey, Y yKey, Z zKey, V value) {
    rawArray[xMapping.getIndexOrThrow(xKey)][yMapping.getIndexOrThrow(yKey)][zMapping.getIndexOrThrow(zKey)] = value;
  }

  public X getXKey(int xIndex) {
    return xMapping.getKey(xIndex);
  }

  public Y getYKey(int yIndex) {
    return yMapping.getKey(yIndex);
  }

  public Z getZKey(int zIndex) {
    return zMapping.getKey(zIndex);
  }

  public int getSizeInX() {
    return xMapping.size();
  }

  public int getSizeInY() {
    return yMapping.size();
  }

  public int getSizeInZ() {
    return zMapping.size();
  }

  public boolean containsKeys(X xKey, Y yKey, Z zKey) {
    return xMapping.getOptionalIndex(xKey).isPresent()
        && yMapping.getOptionalIndex(yKey).isPresent()
        && zMapping.getOptionalIndex(zKey).isPresent();
  }

  @VisibleForTesting // this makes the matcher easier
  V[][][] getRawArrayUnsafe() {
    return rawArray;
  }

  public ArrayIndexMapping<X> getXMapping() {
    return xMapping;
  }

  public ArrayIndexMapping<Y> getYMapping() {
    return yMapping;
  }

  public ArrayIndexMapping<Z> getZMapping() {
    return zMapping;
  }

  /**
   * XYZ is meant to be a generalization of 'row major iterator' etc. in 3 dimensions.
   * We could add different orderings, if we ever need to.
   */
  public <T> Stream<T> toTransformedStreamInXYZOrder(QuadriFunction<X, Y, Z, V, T> valueTransformer) {
    return getXMapping().getAllKeys().stream()
        .flatMap(x -> getYMapping().getAllKeys().stream()
            .flatMap(y -> getZMapping().getAllKeys().stream()
                .map(z -> valueTransformer.apply(x, y, z, get(x, y, z)))));
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
    xFilter.ifPresent(x -> RBPreconditions.checkArgument(getXMapping().getOptionalIndex(x).isPresent()));
    yFilter.ifPresent(y -> RBPreconditions.checkArgument(getYMapping().getOptionalIndex(y).isPresent()));
    zFilter.ifPresent(z -> RBPreconditions.checkArgument(getZMapping().getOptionalIndex(z).isPresent()));
    // We need to use suppliers because e.g. there will be multiple streams going over y.
    Stream<X> xStream           =       xFilter.transformOrDefault(v -> Stream.of(v), getXMapping().getAllKeys().stream());
    Supplier<Stream<Y>> yStream = () -> yFilter.transformOrDefault(v -> Stream.of(v), getYMapping().getAllKeys().stream());
    Supplier<Stream<Z>> zStream = () -> zFilter.transformOrDefault(v -> Stream.of(v), getZMapping().getAllKeys().stream());
    return xStream.flatMap(x ->
        yStream.get().flatMap(y ->
            zStream.get().map(z -> get(x, y, z))));
  }

  @Override
  public String toString() {
    return Strings.format("%s %s %s %s",
        xMapping, yMapping, zMapping, Arrays.deepToString(rawArray));
  }

}
