package com.rb.nonbiz.types;

import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

/**
 * Use this when you modify a value e.g. inside some inner method in a lambda.
 * We should never pass around a Pointer, or store it. It's not meant to be a C++ pointer!
 *
 * @see ObjectReference if you know for sure this will have a value, and you can supply it at initialization time;
 * it is slightly clearer.
 */
public class Pointer<T> {

  private T object;

  /** This is here to prevent initialization via a non-static constructor */
  private Pointer() {
  }

  public static <T> Pointer<T> uninitializedPointer() {
    return new Pointer<>();
  }

  public static <T> Pointer<T> initializedPointer(T initialValue) {
    Pointer<T> pointer = new Pointer<>();
    pointer.set(initialValue);
    return pointer;
  }

  public void set(T value) {
    this.object = value;
  }

  public void setAssumingUninitialized(T value) {
    RBPreconditions.checkArgument(
        this.object == null,
        "In setAssumingUninitialized: attempt to give value of %s to already initialized pointer whose value is %s",
        value, this.object);
    this.object = value;
  }

  public Pointer<T> modifyExisting(T value, BinaryOperator<T> operator) {
    RBPreconditions.checkArgument(object != null);
    this.object = operator.apply(object, value);
    return this;
  }

  public void initializeOrModifyExisting(T value, BinaryOperator<T> operator) {
    this.object = (object == null)
        ? value
        : operator.apply(object, value);
  }

  public T getOrThrow() {
    RBPreconditions.checkArgument(object != null);
    return object;
  }

  public Optional<T> getOptional() {
    return Optional.ofNullable(object);
  }

  public boolean isInitialized() {
    return object != null;
  }

  public void ifInitialized(Consumer<T> valueConsumer) {
    if (isInitialized()) {
      valueConsumer.accept(object);
    }
  }

}
