package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Collection;

/**
 * This abstraction is useful so we can ask the question "how far from the beginning and the end is
 * a specific item in an ordered collection?". For example, if we have stocks A1, A2, A3, A4, A5
 * in instrument class A, then A2 has an index from the start of 1 (using array-style, 0-based indexing),
 * and an index from the end of 3.
 */
public class IndexInOrderedCollection<T> {

  private final int indexFromStart;
  private final int collectionSize;

  private IndexInOrderedCollection(int indexFromStart, int collectionSize) {
    this.indexFromStart = indexFromStart;
    this.collectionSize = collectionSize;
  }

  public static <T> IndexInOrderedCollection<T> indexInSingletonCollection() {
    return IndexInOrderedCollectionBuilder.<T>indexInOrderedCollectionBuilder()
        .setIndexFromStart(0)
        .setCollectionSize(1)
        .build();
  }

  public static <T> IndexInOrderedCollection<T> indexInOrderedCollection(int index, Collection<? extends T> collection) {
    return IndexInOrderedCollectionBuilder.<T>indexInOrderedCollectionBuilder()
        .setIndexFromStart(index)
        .setCollectionSize(collection.size())
        .build();
  }

  public int getIndexFromStart() {
    return indexFromStart;
  }

  public int getIndexFromEnd() {
    // e.g. if collectionSize = 3 and indexFromStart = 0 (first), then indexFromEnd = 2 (last, from the end backwards),
    // and if indexFromStart = 2 (last), then indexFromEnd = 0 (first, from the end backwards).
    return collectionSize - indexFromStart - 1;
  }

  public int getCollectionSize() {
    return collectionSize;
  }

  @Override
  public String toString() {
    return Strings.format("[IIOC %s ; size= %s IIOC]");
  }

  
  public static class IndexInOrderedCollectionBuilder<T> implements RBBuilder<IndexInOrderedCollection<T>> {

    private Integer indexFromStart;
    private Integer collectionSize;

    private IndexInOrderedCollectionBuilder() {}
    
    public static <T> IndexInOrderedCollectionBuilder<T> indexInOrderedCollectionBuilder() {
      return new IndexInOrderedCollectionBuilder<>();
    }
    
    public IndexInOrderedCollectionBuilder<T> setIndexFromStart(int indexFromStart) {
      this.indexFromStart = checkNotAlreadySet(this.indexFromStart, indexFromStart);
      return this;
    }

    public IndexInOrderedCollectionBuilder<T> setCollectionSize(int collectionSize) {
      this.collectionSize = checkNotAlreadySet(this.collectionSize, collectionSize);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(indexFromStart);
      RBPreconditions.checkNotNull(collectionSize);
      RBPreconditions.checkArgument(
          indexFromStart >= 0,
          "indexFromStart must be >=0 but was %s",
          indexFromStart);
      RBPreconditions.checkArgument(
          indexFromStart < collectionSize,
          "indexFromStart is %s , which is not a valid index in an ordered collection of size %s",
          indexFromStart, collectionSize);
    }

    @Override
    public IndexInOrderedCollection<T> buildWithoutPreconditions() {
      return new IndexInOrderedCollection<>(indexFromStart, collectionSize);
    }

  }

}
