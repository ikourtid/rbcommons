package com.rb.nonbiz.util;

import com.google.common.annotations.VisibleForTesting;

/**
 * A builder class (which should implement RBBuilder) is useful:
 * a) in cases where there are multiple fields of the same type, relying on argument order may be error-prone.
 * b) adds clarity over what a field actually is - vs relying on argument order.
 * c) in cases there are reasonable defaults for some fields in an object. At the limit, an object with n fields
 *    (where all n have default values) could need 2^n different constructors, in the absence of a builder.
 *
 * The RBBuilder interface isn't strictly needed because we don't need to ever pass a builder of some class that's
 * not already known, i.e. we never pass around an {@code RBBuilder<T>}. At most, we'd pass a specific class that implements
 * RBBuilder.
 * However, implementing RBBuilder forces you to follow certain conventions whenever you implement a builder.
 *
 * It is very rare that you would need a getter on a builder.
 * Ideally, you should build the object first, and then just call that built object's getter.
 *
 * Use the default method name format that IntelliJ gives you; e.g.
 * * setFooBarBaz if something can only be set once
 * * addFooBarBaz if sime items get collected in e.g. some list
 *
 * We have a convention where builder methods such as setXYZ assume that they have only been called once, and throw
 * an exception otherwise. This is for extra safety in tests, because you can find yourself diagnosing confusing problems
 * if a field is set twice with different values; the behavior would depend on which value you set last.
 * For cases where you explicitly want to change a value that was already set previously by a setXYZ method,
 * the convention should be to create a method resetXYZ, which should assume XYZ has been given a value, and throw
 * an exception otherwise. That would be the opposite behavior of setXYZ.
 */
public interface RBBuilder<T> {

  /**
   * Throw an exception if the contents are not OK for building a real object
   * (e.g. fields missing, fields inconsistent with each other, etc.)
   */
  void sanityCheckContents();

  default T build() {
    sanityCheckContents();
    return buildWithoutPreconditions();
  }

  /**
   * You almost never want to set the same field twice in a builder.
   * If you ever do that, it's likely an indication that you set another field 0 times, most likely
   * because you cut-and-pasted some code.
   * So you should almost use this inside your builder setters. Search for usage for examples.
   */
  default <V> V checkNotAlreadySet(V currentFieldValue, V newFieldValue) {
    RBPreconditions.checkArgument(
        currentFieldValue == null,
        "You are trying to set a value twice in a builder, which is probably a bug: from %s to %s",
        currentFieldValue, newFieldValue);
    return newFieldValue;
  }

  /**
   * Similar to checkNotAlreadySet, but it's the opposite.
   * This is to be used inside any reset* method, whose goal is to update an existing value in the builder.
   * It's a rare enough scenario that merits separately named builder method names starting with reset*.
   */
  default <V> V checkAlreadySet(V currentFieldValue, V newFieldValue) {
    RBPreconditions.checkArgument(
        currentFieldValue != null,
        "You are trying to reset a value to %s in a builder, but the value has not already been set",
        newFieldValue);
    return newFieldValue;
  }

  /**
   * Sometimes when testing we need to create partial objects for convenience,
   * without checking that all fields are valid - e.g. some may be left unspecified altogether.
   */
  @VisibleForTesting
  T buildWithoutPreconditions();

}
