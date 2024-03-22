package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.math.vectorspaces.RBMatrix;
import com.rb.nonbiz.types.Epsilon;

import java.util.Iterator;
import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.ImmutableIndexableArray2D.immutableIndexableArray2D;
import static com.rb.nonbiz.collections.MutableDoubleIndexableArray2D.mutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.util.RBSimilarityPreconditions.checkBothSame;

/**
 * <p> A map that can be indexed with two keys. </p>
 *
 * <p> This is more efficient than using a map of maps, and also ensures that the map is 'rectangular',
 * i.e. there are values for every combination of the keys in the 1st and 2nd dimension's keyset. </p>
 *
 * <p> Similar to {@link MutableDoubleIndexableArray2D}, except this is immutable. </p>
 *
 * <p> Note that someone can modify the underlying object if they have a handle to it via getRawArrayUnsafe()
 * but at least they can't modify it through THIS object; there's no set() method here. </p>
 */
public class ImmutableDoubleIndexableArray2D<R, C> implements IndexableDoubleDataStore2D<R, C> {

  private final MutableDoubleIndexableArray2D<R, C> mutableArray2D;

  private ImmutableDoubleIndexableArray2D(MutableDoubleIndexableArray2D<R, C> mutableArray2D) {
    this.mutableArray2D = mutableArray2D;
  }

  public static <R, C> ImmutableDoubleIndexableArray2D<R, C> immutableDoubleIndexableArray2D(
      MutableDoubleIndexableArray2D<R, C> mutableArray2D) {
    return new ImmutableDoubleIndexableArray2D<>(mutableArray2D);
  }

  public static <R, C> ImmutableDoubleIndexableArray2D<R, C> immutableDoubleIndexableArray2D(
      double[][] rawArray, ArrayIndexMapping<R> rowMapping, ArrayIndexMapping<C> columnMapping) {
    return immutableDoubleIndexableArray2D(mutableDoubleIndexableArray2D(
        rawArray, rowMapping, columnMapping));
  }

  public static <R, C> ImmutableDoubleIndexableArray2D<R, C> emptyImmutableDoubleIndexableArray2D() {
    return immutableDoubleIndexableArray2D(
        new double[][] { },
        simpleArrayIndexMapping(),
        simpleArrayIndexMapping());
  }

  @Override
  public double getByIndex(int rowIndex, int columnIndex) {
    return mutableArray2D.getByIndex(rowIndex, columnIndex);
  }

  @Override
  public ArrayIndexMapping<R> getRowMapping() {
    return mutableArray2D.getRowMapping();
  }

  @Override
  public ArrayIndexMapping<C> getColumnMapping() {
    return mutableArray2D.getColumnMapping();
  }

  public RBMatrix toRBMatrix() {
    return mutableArray2D.toRBMatrix();
  }

  /**
   * Returns true if and only if the # of rows is the same as the # of columns.
   *
   * <p> For this method, it doesn't matter what the row keys and column keys are,
   * and it doesn't even matter if they are of the same type. </p>
   */
  public boolean isSquare() {
    return getNumRows() == getNumColumns();
  }

  /**
   * Returns true if and only if the # of rows is the same as the # of columns,
   * and also the row keys are the same as the column keys, and appear in the same numeric order.
   *
   * <p> Note that the row and value types must be the same, and that type must implement a non-trivial equals
   * and hashCode method, which is true here (it's not an assumption), because otherwise they could not be keys
   * to the {@link ArrayIndexMapping} that specifies the positions of the row keys and column keys in the 2d array. </p>
   *
   * <p> Here is an example that would return true: </p>
   * <pre>
   *    A B
   *  A 1 2
   *  B 3 4
   * </pre>
   */
  public boolean isSquareWithRowKeysSameAsColumnKeys() {
    if (!isSquare()) {
      return false;
    }
    int sharedSize = checkBothSame(
        getNumRows(),
        getNumColumns(),
        "Internal error: This should never throw by this point");
    ArrayIndexMapping<R> rowMapping = getRowMapping();
    ArrayIndexMapping<C> columnMapping = getColumnMapping();
    return IntStream.range(0, sharedSize)
        .allMatch(i -> rowMapping.getKey(i).equals(columnMapping.getKey(i)));
  }

  /**
   * Returns true if this is a symmetric matrix, subject to an epsilon.
   *
   * For example, this is symmetric:
   *
   * <pre>
   *   A B C
   * A 1 2 3
   * B 2 4 5
   * C 3 5 6
   * </pre>
   *
   * <p> Note that this class allows us to refer to items by row key and column key. Take a look at this: </p>
   *
   * <pre>
   *   A B C
   * B 2 4 5
   * A 1 2 3
   * C 3 5 6
   * </pre>
   *
   * <p> If you look at the raw 2d array, it's not symmetric. However, all I did here is move the B row to be the first;
   * the data are still the same. So e.g. both (A, B) and (B, A) have the same value of 2. So, based on this looser
   * definition of symmetric, the raw 2d array may not be symmetric, but (X, Y) = (Y, X) for every X and Y. </p>
   *
   * <p> This method will not consider the latter matrix to be symmetric: it will require that the raw storage
   * (i.e. using numeric indices) is also symmetric. </p>
   */
  public boolean isLogicallyAndPhysicallySymmetric(Epsilon epsilon) {
    if (!isSquareWithRowKeysSameAsColumnKeys()) {
      return false;
    }

    int sharedSize = checkBothSame(
        getNumRows(),
        getNumColumns(),
        "Internal error: This should never throw by this point");
    // We usually like to use Streams and fluent code, but this could be a big operation (for a large matrix),
    // plus it's clear enough to look at for loops when iterating over a matrix.
    for (int i = 0; i < sharedSize; i++) {
      for (int j = i + 1; j < sharedSize; j++) {
        double aboveDiagonal = getByIndex(i, j);
        double belowDiagonal = getByIndex(j, i);
        if (!epsilon.valuesAreWithin(aboveDiagonal, belowDiagonal)) {
          return false;
        }
      }
    }
    return true;
  }

  @VisibleForTesting // Don't use this; it helps the matcher code be simpler
  MutableDoubleIndexableArray2D<R, C> getMutableArray2D() {
    return mutableArray2D;
  }

  @SuppressWarnings("unchecked")
  public <V> ImmutableIndexableArray2D<R, C, V> transform(
      TriFunction<R, C, Double, V> transformer) {
    ArrayIndexMapping<R> rowKeysMapping = mutableArray2D.getRowMapping();
    ArrayIndexMapping<C> columnKeysMapping = mutableArray2D.getColumnMapping();
    // Unfortunately I haven't found a good alternative to instantiating a 2d array.
    V[][] newRawArray = (V[][]) new Object[rowKeysMapping.size()][columnKeysMapping.size()];
    for (int r = 0; r < rowKeysMapping.size(); r++) {
      R rowKey = rowKeysMapping.getKey(r);
      for (int c = 0; c < columnKeysMapping.size(); c++) {
        C columnKey = columnKeysMapping.getKey(c);
        newRawArray[r][c] = transformer.apply(rowKey, columnKey, mutableArray2D.getByIndex(r, c));
      }
    }
    return immutableIndexableArray2D(newRawArray, rowKeysMapping, columnKeysMapping);
  }

  @Override
  public String toString() {
    return mutableArray2D.toString();
  }

  public Iterator<Double> singleRowIterator(R rowKey) {
    return mutableArray2D.singleRowIterator(rowKey);
  }

  public Iterator<Double> singleColumnIterator(C columnKey) {
    return mutableArray2D.singleColumnIterator(columnKey);
  }

  public Iterator<Double> rowMajorIterator() {
    return mutableArray2D.rowMajorIterator();
  }

  public boolean isEmpty() {
    return mutableArray2D.isEmpty();
  }

}
