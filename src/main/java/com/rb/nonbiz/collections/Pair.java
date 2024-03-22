package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

/**
 * This is handy for those cases where you don't want to create a separate named private data class for internal use
 * within an outer class.
 *
 * <p> There is no way to enforce this in Java, but don't use Pair in public interfaces; create a named class instead. </p>
 */
public class Pair<L, R> {

  private final L left;
  private final R right;

  private Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public static <L, R> Pair<L, R> pair(L left, R right) {
    return new Pair<>(left, right);
  }

  public L getLeft() {
    return left;
  }

  public R getRight() {
    return right;
  }

  // IDE-generated. This class is primitive-value-ish enough to merit an equals/hashcode override.
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Pair<?, ?> pair = (Pair<?, ?>) o;

    if (!left.equals(pair.left)) return false;
    return right.equals(pair.right);
  }

  // IDE-generated. This class is primitive-value-ish enough to merit an equals/hashcode override.
  @Override
  public int hashCode() {
    int result = left.hashCode();
    result = 31 * result + right.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return Strings.format("( %s , %s )", left, right);
  }

}
