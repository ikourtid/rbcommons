package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.Strings;

import java.util.Optional;
import java.util.function.Supplier;

import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.text.Strings.formatOptional;

/**
 * Like an {@link Optional}, except with the explicit assumption that if you ever try to access it and its value is
 * empty / undefined, you will get an exception.
 *
 * <p> With a plain {@link Optional}, the semantics are such that an empty value is valid.
 * This is different: an empty value is valid, but accessing it is not. </p>
 *
 * <p> The initial use case (June 2023) is this. There is an object that exists in tax-aware investing logic, but
 * not in tax-unaware. That object needs to be passed down a chain of methods. If we were to pass an {@link Optional},
 * it would be like saying 'empty is also valid'. Eventually, the final callee would do a getOrThrow, and the
 * 'this item must be present' semantics would become explicit to someone looking at the code. However, the method
 * signatures wouldn't be clear about the fact that this isn't just a plain {@link Optional} and that a value
 * has to be defined - it can't be empty. Therefore, by passing one of these, it's clear that an empty value is
 * possible, but whoever is looking at this will get an exception if the item is undefined. </p>
 */
public class RBSupplier<T> implements Supplier<T> {

  private final Optional<T> item;

  private RBSupplier(Optional<T> item) {
    this.item = item;
  }

  public static <T> RBSupplier<T> rbSupplierReturningValidValue(T item) {
    return new RBSupplier<>(Optional.of(item));
  }

  public static <T> RBSupplier<T> rbSupplierThrowingException() {
    return new RBSupplier<>(Optional.empty());
  }

  @Override
  public T get() {
    return getOrThrow(
        item,
        "Requesting an uncomputed item from a RBPrecomputedItemSupplier");
  }

  @VisibleForTesting
  Optional<T> getRawOptionalUnsafe() {
    return item;
  }

  @Override
  public String toString() {
    return Strings.format("[RBS %s RBS]", formatOptional(item));
  }

}