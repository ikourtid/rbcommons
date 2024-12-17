package com.rb.nonbiz.collections;

import com.google.common.collect.Sets;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static com.rb.nonbiz.text.Strings.sizePrefix;

/**
 * A mutable version of an {@link RBSet}.
 *
 * <p> This is one of the rare mutable data classes in the codebase.
 * Typically, we will build a MutableRBSet and then 'lock' its values into an RBSet in the same method, and then return it.
 * We should (almost) never pass around a MutableRBSet. </p>
 */
public class MutableRBSet<T> {

  private final Set<T> rawSet;

  private MutableRBSet(Set<T> rawSet) {
    this.rawSet = rawSet;
  }

  public static <T> MutableRBSet<T> newMutableRBSet() {
    return new MutableRBSet<>(Sets.newHashSet());
  }

  public static <T> MutableRBSet<T> newMutableRBSetWithExpectedSize(int expectedSize) {
    return new MutableRBSet<>(Sets.newHashSetWithExpectedSize(expectedSize));
  }

  public Set<T> asSet() {
    return rawSet;
  }

  public int size() {
    return rawSet.size();
  }

  public boolean isEmpty() {
    return rawSet.isEmpty();
  }

  public boolean contains(T t) {
    return rawSet.contains(t);
  }

  public Iterator<T> iterator() {
    return rawSet.iterator();
  }

  /**
   * Adds an item to the set.
   * It's OK if the item is already in the set.
   *
   * <p> Consider using #addAssumingAbsent if adding the same item twice is not valid behavior,
   * and you want the extra safety check. </p>
   */
  public boolean add(T t) {
    return rawSet.add(t);
  }

  /**
   * Adds an item to the set.
   * Throws if it's already there.
   *
   * <p> If adding the same item twice is not valid behavior, use this instead of plain #add for extra safety. </p>
   */
  public boolean addAssumingAbsent(T t) {
    RBPreconditions.checkArgument(
        !rawSet.contains(t),
        "Set already contains %s ; contents are %s",
        t, rawSet);
    return add(t);
  }

  /**
   * Adds an item to the set.
   * Throws if it's already there.
   *
   * <p> You may say - why not just use plain 'add', which will just replace the object?
   * There's a nuance in cases where there are two objects which, if compared using equals, will return true,
   * but which are actually different objects. We should be defining equals in a way that this shouldn't happen,
   * but at a minimum, this is more explicit. </p>
   */
  public boolean addIfAbsent(T t) {
    if (rawSet.contains(t)) {
      return false;
    }
    return add(t);
  }

  public boolean remove(T t) {
    return rawSet.remove(t);
  }

  public boolean containsAll(Collection<? extends T> c) {
    return rawSet.containsAll(c);
  }

  public boolean addAll(Collection<? extends T> c) {
    return rawSet.addAll(c);
  }

  public boolean retainAll(Collection<? extends T> c) {
    return rawSet.retainAll(c);
  }

  public boolean removeAll(Collection<? extends T> c) {
    return rawSet.removeAll(c);
  }

  public void clear() {
    rawSet.clear();
  }

  @Override
  public String toString() {
    return sizePrefix(rawSet.size()) + rawSet.toString();
  }

}
