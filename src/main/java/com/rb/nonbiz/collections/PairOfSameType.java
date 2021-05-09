package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.collections.Pair.pair;

/**
 * See Pair class. This is for the special case where both items in the pair are of the same type.
 *
 * This is handy for those cases where you don't want to create a separate named private data class for internal use
 * within an outer class.
 *
 * Don't use PairOfSameType in public interfaces; create a named class instead.
 */
public class PairOfSameType<T> {

  private final Pair<T, T> rawPair;

  private PairOfSameType(Pair<T, T> rawPair) {
    this.rawPair = rawPair;
  }

  public static <T> PairOfSameType<T> pairOfSameType(T left, T right) {
    return new PairOfSameType<>(pair(left, right));
  }

  public T getLeft() {
    return rawPair.getLeft();
  }

  public T getRight() {
    return rawPair.getRight();
  }

  // IDE-generated. This class is primitive-value-ish enough to merit an equals/hashcode override.
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PairOfSameType<?> that = (PairOfSameType<?>) o;

    return rawPair.equals(that.rawPair);

  }

  // IDE-generated. This class is primitive-value-ish enough to merit an equals/hashcode override.
  @Override
  public int hashCode() {
    return rawPair.hashCode();
  }

  @Override
  public String toString() {
    return Strings.format("[POST %s POST]", rawPair);
  }

}
