package com.rb.nonbiz.text;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * This is basically a string, except that using a separate class makes the semantics a bit more explicit.
 *
 * <p> Also, we don't allow empty ids, or ids with spaces - so it's easier for this to be part of a filename. </p>
 */
public class UniqueId<T> implements Comparable<UniqueId<T>> {

  private final String stringId;

  private UniqueId(String stringId) {
    this.stringId = stringId;
  }

  public static <T> UniqueId<T> uniqueId(String stringId) {
    RBPreconditions.checkArgument(
        !stringId.isEmpty(),
        "You cannot have an empty UniqueStringId");
    return new UniqueId<>(stringId);
  }

  public static <T> UniqueId<T> uniqueIdWithoutSpaces(String stringId) {
    RBPreconditions.checkArgument(
        !stringId.isEmpty(),
        "You cannot have an empty UniqueStringId");
    RBPreconditions.checkArgument(
        stringId.indexOf(' ') < 0,
        "Disallowed spaces appear in unique ID '%s'",
        stringId);
    return new UniqueId<>(stringId);
  }

  /**
   * Use this e.g. for cases where an inner object has a unique id, and you want your outer object
   * to reuse the same id (string). Since {@code UniqueId<T>} is generic on T, you can't just swap one id for the other.
   *
   * We can't use a cast for this, so we need a separate method.
   * But this is more explicit.
   */
  public static <T1, T2> UniqueId<T2> toUniqueIdOfDifferentType(UniqueId<T1> id1) {
    return uniqueId(id1.getStringId());
  }

  public String getStringId() {
    return stringId;
  }

  @Override
  public String toString() {
    return stringId;
  }

  // IDE-generated
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UniqueId<?> that = (UniqueId<?>) o;

    return stringId.equals(that.stringId);
  }

  // IDE-generated
  @Override
  public int hashCode() {
    return stringId.hashCode();
  }

  @Override
  public int compareTo(UniqueId<T> other) {
    return stringId.compareTo(other.stringId);
  }

}
