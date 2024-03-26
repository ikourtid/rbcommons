package com.rb.nonbiz.text;

import com.google.common.collect.Iterators;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;

/**
 * Static utility methods pertaining to {@link ItemWithId}.
 */
public class ItemsWithId {

  /**
   * This ascertains that all items have different ids, and also packages everything into a list.
   *
   * @see ItemWithId
   */
  @SafeVarargs
  public static <T> List<ItemWithId<T>> itemsWithUniqueIds(ItemWithId<T> first, ItemWithId<T> ... rest) {
    List<ItemWithId<T>> list = concatenateFirstAndRest(first, rest);
    RBPreconditions.checkUnique(
        Iterators.transform(list.iterator(), itemWithId -> itemWithId.getUniqueId()));
    return list;
  }

}
