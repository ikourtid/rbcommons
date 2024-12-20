package com.rb.nonbiz.search;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.OneOf3;
import com.rb.nonbiz.collections.OneOf3.Visitor;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.collections.OneOf3.only1stOf3;
import static com.rb.nonbiz.collections.OneOf3.only2ndOf3;
import static com.rb.nonbiz.collections.OneOf3.only3rdOf3;

/**
 * A generalization of {@link BinarySearchResult} that also allows for the possibility that we could not find
 * valid initial lower and upper bounds to do the binary search.
 */
public class PossiblyShortCircuitedBinarySearchResult<X, Y> {

  public interface Visitor<T, X, Y> {

    T visitNotShortCircuitedBinarySearchResult(
        BinarySearchResult<X, Y> binarySearchResult);
    T visitResultWhenOnlyValidLowerBoundExists(
        ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y> shortCircuitedBinarySearchResultForLowerBoundOnly);
    T visitResultWhenOnlyValidUpperBoundExists(
        ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y> shortCircuitedBinarySearchResultForUpperBoundOnly);

  }
  
  
  private final OneOf3<
      BinarySearchResult<X, Y>,
      ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y>,
      ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y>> rawOneOf3;

  private PossiblyShortCircuitedBinarySearchResult(
      OneOf3<
          BinarySearchResult<X, Y>,
          ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y>,
          ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y>> rawOneOf3) {
    this.rawOneOf3 = rawOneOf3;
  }

  public static <X, Y> PossiblyShortCircuitedBinarySearchResult<X, Y> notShortCircuitedBinarySearchResult(
      BinarySearchResult<X, Y> binarySearchResult) {
    return new PossiblyShortCircuitedBinarySearchResult<>(only1stOf3(binarySearchResult));
  }

  public static <X, Y> PossiblyShortCircuitedBinarySearchResult<X, Y> resultWhenOnlyValidLowerBoundExists(
      ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y> shortCircuitedBinarySearchResultForLowerBoundOnly) {
    return new PossiblyShortCircuitedBinarySearchResult<>(only2ndOf3(shortCircuitedBinarySearchResultForLowerBoundOnly));
  }

  public static <X, Y> PossiblyShortCircuitedBinarySearchResult<X, Y> resultWhenOnlyValidUpperBoundExists(
      ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y> shortCircuitedBinarySearchResultForUpperBoundOnly) {
    return new PossiblyShortCircuitedBinarySearchResult<>(only3rdOf3(shortCircuitedBinarySearchResultForUpperBoundOnly));
  }

  public <T> T visit(Visitor<T, X, Y> visitor) {
    return rawOneOf3.visit(new OneOf3.Visitor<
        BinarySearchResult<X, Y>,
        ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y>,
        ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y>,
        T>() {

      @Override
      public T visitOnly1stOf3(
          BinarySearchResult<X, Y> binarySearchResult) {
        return visitor.visitNotShortCircuitedBinarySearchResult(binarySearchResult);
      }

      @Override
      public T visitOnly2ndOf3(
          ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y> shortCircuitedBinarySearchResultForLowerBoundOnly) {
        return visitor.visitResultWhenOnlyValidLowerBoundExists(shortCircuitedBinarySearchResultForLowerBoundOnly);
      }

      @Override
      public T visitOnly3rdOf3(
          ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y> shortCircuitedBinarySearchResultForUpperBoundOnly) {
        return visitor.visitResultWhenOnlyValidUpperBoundExists(shortCircuitedBinarySearchResultForUpperBoundOnly);
      }
    });
  }

  // Do not use this; it's here for the test matcher
  @VisibleForTesting
  OneOf3<
      BinarySearchResult<X, Y>,
      ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y>,
      ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y>> getRawOneOf3() {
    return rawOneOf3;
  }

  @Override
  public String toString() {
    return Strings.format("[PSCBSR %s PSCBSR]", visit(new Visitor<String, X, Y>() {
      @Override
      public String visitNotShortCircuitedBinarySearchResult(BinarySearchResult<X, Y> binarySearchResult) {
        return binarySearchResult.toString();
      }

      @Override
      public String visitResultWhenOnlyValidLowerBoundExists(
          ShortCircuitedBinarySearchResultForLowerBoundOnly<X, Y> shortCircuitedBinarySearchResultForLowerBoundOnly) {
        return shortCircuitedBinarySearchResultForLowerBoundOnly.toString();
      }

      @Override
      public String visitResultWhenOnlyValidUpperBoundExists(
          ShortCircuitedBinarySearchResultForUpperBoundOnly<X, Y> shortCircuitedBinarySearchResultForUpperBoundOnly) {
        return shortCircuitedBinarySearchResultForUpperBoundOnly.toString();
      }
    }));
  }

}
