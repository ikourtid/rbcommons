package com.rb.nonbiz.types;

/**
 * 
 * @param <T>
 */
public interface HasIntuitiveComparisons<T> {

  boolean isGreaterThan(T other);

  boolean isGreaterThanOrEqualTo(T other);

  boolean isLessThan(T other);

  boolean isLessThanOrEqualTo(T other);

}
