package com.rb.nonbiz.functional;

/**
 * Sometimes we use Optional inside a data class to denote a missing value.
 * We can have a data class implement this interface to declare that intention, and allow handling the missing value
 * without dealing with a 'raw' optional. It's a bit cleaner this way.
 *
 * <p> We could also have called this interface {@code HasOptional<T>}, have the interface method be called
 * getOptional(), and ditch the visitor. But it would have been a bit less clear. </p>
 */
public interface AllowsMissingValues<V> {

  interface Visitor<T, V> {

    T visitPresentValue(V presentValue);
    T visitMissingValue();

  }

  <T> T visit(Visitor<T, V> visitor);

}
