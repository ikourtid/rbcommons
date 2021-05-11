package com.rb.nonbiz.types;

import java.util.function.Supplier;

import static com.rb.nonbiz.types.Pointer.uninitializedPointer;

/**
 * Use this as a wrapper around an expensive-to-calculate value.
 * Like a plain Supplier, this will not calculate the expensive value if that value is never requested.
 * However, it also stores the value, so any subsequent requests will return the same value, unlike a Supplier,
 * which would always run the code inside it.
 *
 * In other words, the supplier will be called either 0 or 1 times - no more.
 */
public class LazyValue<T> {

  private final Pointer<T> pointer;
  private final Supplier<T> supplier;

  private LazyValue(Supplier<T> supplier) {
    this.pointer = uninitializedPointer();
    this.supplier = supplier;
  }

  public static <T> LazyValue<T> lazyValue(Supplier<T> supplier) {
    return new LazyValue<>(supplier);
  }

  public T get() {
    if (!pointer.isInitialized()) {
      pointer.set(supplier.get());
    }
    return pointer.getOrThrow();
  }

}
