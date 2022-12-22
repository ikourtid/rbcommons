package com.rb.nonbiz.math.vectorspaces;

/**
 * This must get implemented by any simple class that's meant to be a thin wrapper around an array index.
 *
 * <p> Currently (Dec 2022), this is {@link MatrixRowIndex} and {@link MatrixColumnIndex}, but if we were to add
 * a simple 'array index' wrapper (to avoid using int), that would also have to implement this interface </p>
 */
public interface IsArrayIndex {

  int intValue();

}
