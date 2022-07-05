package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import com.rb.nonbiz.types.HasLongRepresentation;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.RBSet.newRBSet;


/**
 * This is a special implementation of an {@link RBSet} (SORT OF) for the cases where the key class
 * implements {@link HasLongRepresentation}. In those cases, we can key off of the long, and can therefore use
 * specialized classes which are faster and more space-efficient (which can also result in speed increases
 * due to fewer cache misses, presumably).
 *
 * <p> This is used in IidSet in particular, but can also be used for other cases
 * of HasLongRepresentation (AssetId, Investable, AssetClass, etc.) </p>
 *
 * <p> I originally wanted to make this look just like RBSet, so that the code can be agnostic
 * about whether it's using a special implementation for the cases
 * However, it's just not possible. The GNU Trove objects that we use for the raw underlying set
 * do not really support a java.util.Set-like interface. </p>
 *
 * <p> Therefore, any code that uses this will need to treat this specially (i.e. not like an RBSet). </p>
 *
 * @see RBSet
 * @see HasLongMap
 */
public abstract class HasLongSet<T extends HasLongRepresentation> implements Iterable<T> {

  private static final TLongHashSet EMPTY_INSTANCE = new TLongHashSet(0);

  private final TLongHashSet rawSet;
  private transient List<T> sortedItems; // stored so we don't have to recompute it multiple times

  protected HasLongSet(TLongHashSet rawSet) {
    this.rawSet = rawSet.isEmpty() ? EMPTY_INSTANCE : rawSet;
    this.sortedItems = null;
  }

  protected HasLongSet(TLongHashSet rawSet, List<T> sortedItems) {
    this.rawSet = rawSet.isEmpty() ? EMPTY_INSTANCE : rawSet;
    this.sortedItems = sortedItems;
  }

  protected abstract T instantiateItem(long asLong);

  @VisibleForTesting
  public TLongHashSet getRawSetUnsafe() {
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

  public boolean containsAll(HasLongSet<T> other) {
    return other.stream().allMatch(hasLong -> rawSet.contains(hasLong.asLong()));
  }

  // Avoid using this, when possible. However, there are some classes that are generic on the type and need RBSet<T>
  // where T does not necessarily implement HasLongRepresentation (e.g. SetInclusionExclusionInstructions),
  // where we have to convert to an RBSet.
  public RBSet<T> toRBSet() {
    return newRBSet(iterator());
  }

  public List<T> toSortedList() {
    // Synchronization-wise, we should be fine; either sortedItems is null, in which case sortedStream()
    // will calculate and cache it & return it, or it's not null, which means we can return it
    // (since Java reference writes are atomic, and sortedItems only gets written to once).
    return sortedItems != null
        ? sortedItems
        : sortedStream().collect(Collectors.toList());
  }

  @Override
  public Iterator<T> iterator() {
    // See comments in #stream below:
    // If we already have the items in sorted order, we might as well return them in order.
    return sortedItems != null
        ? sortedItems.iterator()
        : Iterators.transform(longsIterator(), v -> instantiateItem(v));
  }

  public Stream<T> stream() {
    // If we already have the sorted items available, we might as well return those.
    // This shouldn't cause a problem with synchronization, because if sortedItems isn't null,
    // then it won't be modified again. I think in old processors it was possible to non-atomically give a value
    // to a pointer, in which case sortedItems would be a bad pointer, but I don't think that will happen here.
    // The idea is that either this is null (in which case no harm is done), or it's not, which means that
    // sortedStream finished its job giving a value to sortedItems.
    // It looks like indeed "Reads and writes are atomic for reference variables":
    // https://docs.oracle.com/javase/tutorial/essential/concurrency/atomic.html
    // I could have added more synchronization, but it might slow things down by a tiny bit.
    return sortedItems != null
        ? sortedStream()
        : LongStream.of(rawSet.toArray()).mapToObj(v -> instantiateItem(v));
  }

  public Stream<T> sortedStream() {
    // We first check outside the synchronization, so for the most cases where sortedItems is NOT null,
    // we won't bother acquiring the lock. This is just a small performance optimization.
    if (sortedItems == null) {
      synchronized (this) {
        if (sortedItems == null) {
          // this looks suspicious because it sorts the items in place,
          // but #toArray returns a copy of the underlying items, so we're fine.
          // Since 'items' is a new array that we can modify anyway, we might as well use Arrays.sort;
          // it is probably faster than LongStream#sorted, since the latter probably has to allocate some space
          // to do the sorting and do some copying.
          long[] items = rawSet.toArray();
          Arrays.sort(items);
          sortedItems = Arrays
              .stream(items)
              .mapToObj(v -> instantiateItem(v))
              .collect(Collectors.toList());
        }
      }
    }
    return sortedItems.stream();
  }

  /**
   * We could presumably run through the set faster if we use the native TLongIterator,
   * but I don't want to expose that in order to keep the code somewhat general.
   */
  public Iterator<Long> longsIterator() {
    // We can't use Iterators.transform here because this is a specialized TLongIterator, not a plain iterator.
    return new Iterator<Long>() {
      TLongIterator rawIterator = rawSet.iterator();

      @Override
      public boolean hasNext() {
        return rawIterator.hasNext();
      }

      @Override
      public Long next() {
        return rawIterator.next();
      }
    };
  }

  /**
   * Avoid using this. We still expose it though, so some internal code can be faster.
   */
  public TLongIterator rawTroveIterator() {
    return rawSet.iterator();
  }

  @Override
  public String toString() {
    return rawSet.toString();
  }

}
