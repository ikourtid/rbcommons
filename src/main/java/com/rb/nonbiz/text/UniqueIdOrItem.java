package com.rb.nonbiz.text;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.Either;

/**
 * Represents either an object of type T, or an ID of that object. If the latter, the assumption is that the user
 * of this class knows how to retrieve that object.
 *
 * This sounds like a pointer, and you may wonder why one wouldn't just use a reference (pointer) to an existing
 * object instead. One example is ESG and optimization. We may or may not want to have an EsgScorableMetric
 * participate in the objective function. If we do want to min/maximize some EsgScorableMetric in the optimization,
 * and that metric already appears in the list of constraints, we don't want to specify it again. This isn't just an
 * issue of avoiding the inconvenience of specifying the metric twice in the investor settings:
 * if we were able to specify the same EsgScorableMetric separately for the objective function with the same UniqueId,
 * we wouldn't have an easy way of confirming that it is identical to the EsgScorableMetric that appears inside
 * the constraints under the same UniqueId.
 */
public class UniqueIdOrItem<T extends HasUniqueId<T>> {

  public interface Visitor<T extends HasUniqueId<T>, V> {

    V visitIsUniqueId(UniqueId<T> uniqueId);
    V visitActualItem(T actualItem);

  }


  private final Either<UniqueId<T>, T> rawEither;

  private UniqueIdOrItem(Either<UniqueId<T>, T> rawEither) {
    this.rawEither = rawEither;
  }

  public static <T extends HasUniqueId<T>> UniqueIdOrItem<T> uniqueIdInsteadOfItem(UniqueId<T> uniqueId) {
    return new UniqueIdOrItem<>(Either.left(uniqueId));
  }

  public static <T extends HasUniqueId<T>> UniqueIdOrItem<T> itemInsteadOfUniqueId(T item) {
    return new UniqueIdOrItem<>(Either.right(item));
  }

  public <V> V visit(Visitor<T, V> visitor) {
    return rawEither.visit(new Either.Visitor<UniqueId<T>, T, V>() {
      @Override
      public V visitLeft(UniqueId<T> uniqueId) {
        return visitor.visitIsUniqueId(uniqueId);
      }

      @Override
      public V visitRight(T actualItem) {
        return visitor.visitActualItem(actualItem);
      }
    });
  }

  // do not use this; it's here for the test matcher.
  @VisibleForTesting
  Either<UniqueId<T>, T> getRawEither() {
    return rawEither;
  }

  @Override
  public String toString() {
    return Strings.format("[UIOI %s UIOI]", rawEither);
  }

}
