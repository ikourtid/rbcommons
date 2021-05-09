package com.rb.nonbiz.collections;

/**
 * Conceptually, this is like java.lang.Void, except that this has a non-null instance.
 */
public class RBVoid {

  private static RBVoid INSTANCE = new RBVoid();

  private RBVoid() {}

  public static RBVoid rbVoid() {
    return INSTANCE;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    // There is only one instance of RBVoid, so we can just check for equality
    return obj == INSTANCE;
  }

}
