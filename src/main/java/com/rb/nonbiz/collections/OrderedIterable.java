package com.rb.nonbiz.collections;

import java.util.Comparator;
import java.util.Iterator;


/**
 * {@code Iterable<T>} will return items in either an undefined order, or some fixed order,
 * which may not always be the order you want.
 *
 * <p> But what if you want to iterate over that iterable using more than one ordering? For example, if we iterate
 * over a collection of TaxLot objects of different instruments, maybe we want to iterate over tax lots (of
 * different instruments) in instrument ID order, and after that iterate in date order. </p>
 *
 * <p> This interface addresses such scenarios. The alternative would be for the class that holds a {@code Collection<TaxLot>}
 * (in this particular example) to expose such a collection, and let the caller sort and iterate over it.
 * This interface is a bit cleaner, because it keeps us from having to expose that collection. </p>
 *
 * <p> Note that T does not necessarily implement Comparable. Comparable is useful for situations where there is a single,
 * globally meaningful ordering. We use OrderedIterable when the exact opposite is
 * happening, i.e. there is no single ordering. Otherwise, we could just store items in that order, and just implement
 * plain old {@code Iterable<T>}. </p>
 */
public interface OrderedIterable<T> {

  Iterator<T> orderedIterator(Comparator<T> comparator);

}
