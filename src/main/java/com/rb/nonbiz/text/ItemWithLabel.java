package com.rb.nonbiz.text;

import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

/**
 * Just a pair of an item and a label. Useful in cases where the item itself does not implement
 * {@link HasHumanReadableLabel} / doesn't store one internally.
 *
 * @param <T>
 */
public class ItemWithLabel<T> implements HasHumanReadableLabel {

  private final HumanReadableLabel label;
  private final T item;

  private ItemWithLabel(HumanReadableLabel label, T item) {
    this.label = label;
    this.item = item;
  }

  public static <T> ItemWithLabel<T> itemWithLabel(String labelString, T item) {
    return new ItemWithLabel<>(label(labelString), item);
  }

  public static <T> ItemWithLabel<T> itemWithLabel(HumanReadableLabel label, T item) {
    return new ItemWithLabel<>(label, item);
  }

  @Override
  public HumanReadableLabel getHumanReadableLabel() {
    return label;
  }

  public T getItem() {
    return item;
  }

  @Override
  public String toString() {
    return Strings.format("%s %s", label, item);
  }

}
