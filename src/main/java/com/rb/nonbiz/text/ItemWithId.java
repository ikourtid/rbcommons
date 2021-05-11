package com.rb.nonbiz.text;

import static com.rb.nonbiz.text.UniqueId.uniqueId;

/**
 * This is just a generic way to attach a {@link UniqueId} to an item.
 */
public class ItemWithId<T> implements HasUniqueId {

  private final UniqueId<T> uniqueId;
  private final T item;

  private ItemWithId(UniqueId<T> uniqueId, T item) {
    this.uniqueId = uniqueId;
    this.item = item;
  }

  public static <T> ItemWithId<T> itemWithId(String idString, T item) {
    return new ItemWithId<>(uniqueId(idString), item);
  }

  @Override
  public UniqueId<T> getUniqueId() {
    return uniqueId;
  }

  public T getItem() {
    return item;
  }

  @Override
  public String toString() {
    return uniqueId.toString();
  }

}
