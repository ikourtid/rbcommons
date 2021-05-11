package com.rb.nonbiz.collections;

import com.rb.nonbiz.types.HasLongRepresentation;
import com.rb.nonbiz.util.RBPreconditions;
import gnu.trove.set.hash.TLongHashSet;

/**
 * This is one of the rare mutable data classes in the codebase.
 * Typically, we will build a MutableRBSet and then 'lock' it into an RBSet in the same method, and then return it.
 * We should (almost) never pass around a MutableRBSet.
 */
public class MutableHasLongSet<T extends HasLongRepresentation> {

  // optimizes for speed vs space
  protected static final float DEFAULT_LOAD_FACTOR = 0.5f;

  private final TLongHashSet rawSet;

  protected MutableHasLongSet(TLongHashSet rawSet) {
    this.rawSet = rawSet;
  }

  public static <K extends HasLongRepresentation> MutableHasLongSet<K> newMutableHasLongSetWithExpectedSize(int expectedSize) {
    int initialCapacity = (int) (expectedSize / DEFAULT_LOAD_FACTOR);
    return new MutableHasLongSet<>(new TLongHashSet(initialCapacity, DEFAULT_LOAD_FACTOR));
  }

  TLongHashSet getRawSet() {
    return rawSet;
  }

  public int size() {
    return rawSet.size();
  }

  public boolean isEmpty() {
    return rawSet.isEmpty();
  }

  public boolean contains(T key) {
    return rawSet.contains(key.asLong());
  }

  public void clear() {
    rawSet.clear();
  }

  /**
   * Adds an item to the set.
   * It's OK if the item is already in the set.
   *
   * Consider using #addAssumingAbsent if adding the same item twice is not valid behavior,
   * and you want the extra safety check.
   */
  public boolean add(T item) {
    return rawSet.add(item.asLong());
  }

  /**
   * Adds an item to the set.
   * Throws if it's already there.
   *
   * If adding the same item twice is not valid behavior, use this instead of plain #add for extra safety.
   */
  public boolean addAssumingAbsent(T item) {
    RBPreconditions.checkArgument(
        !contains(item),
        "Set already contains %s ; contents are %s",
        item, rawSet);
    return add(item);
  }

  @Override
  public String toString() {
    return rawSet.toString();
  }

}
