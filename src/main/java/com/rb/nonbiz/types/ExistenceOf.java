package com.rb.nonbiz.types;

import java.util.Collection;

/**
 * Just a typesafe wrapper around boolean to denote whether T is empty or not.
 * T should conceptually be a collection, but it doesn't have to implement the Collection interface.
 */
public class ExistenceOf<T> {

  private final boolean rawValue;

  private ExistenceOf(boolean rawValue) {
    this.rawValue = rawValue;
  }

  public static <T> ExistenceOf<T> existenceOf(boolean rawValue) {
    return new ExistenceOf<>(rawValue);
  }

  public static <T extends Collection<? super T>> ExistenceOf<T> existenceOfItems(T collection) {
    return existenceOf(!collection.isEmpty());
  }

  public boolean asBoolean() {
    return rawValue;
  }

}
