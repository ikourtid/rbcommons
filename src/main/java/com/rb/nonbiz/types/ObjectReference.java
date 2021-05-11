package com.rb.nonbiz.types;

/**
 * Use this when you modify a value e.g. inside some inner method in a lambda.
 *
 * <p> We should never pass around a {@code Pointer}, or store it. It's not meant to be a C++ pointer! </p>
 *
 * <p> See {@link Pointer} which you have to use instead of an {@code ObjectReference}
 * if you can't supply a value at initialization time. </p>
 */
public class ObjectReference<T> {

  private T object;

  private ObjectReference(T initialValue) {
    this.object = initialValue;
  }

  public static <T> ObjectReference<T> objectReference(T initialValue) {
    return new ObjectReference(initialValue);
  }

  public void set(T value) {
    this.object = value;
  }

  public T get() {
    return object;
  }

}
