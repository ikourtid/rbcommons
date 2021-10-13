package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IndexInOrderedCollection.IndexInOrderedCollectionBuilder;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.IndexInOrderedCollection.indexInOrderedCollection;
import static com.rb.nonbiz.collections.IndexInOrderedCollection.indexInSingletonCollection;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

public class IndexInOrderedCollectionTest extends RBTestMatcher<IndexInOrderedCollection<InstrumentId>> {

  @Test
  public void collectionMustHaveAtLeastOneItem_throws() {
    assertIllegalArgumentException( () -> makeIndexInOrderedCollection(0, -123));
    assertIllegalArgumentException( () -> makeIndexInOrderedCollection(0, -1));
    assertIllegalArgumentException( () -> makeIndexInOrderedCollection(0, 0));
    IndexInOrderedCollection<InstrumentId> doesNotThrow;
    doesNotThrow = makeIndexInOrderedCollection(0, 1);
    doesNotThrow = makeIndexInOrderedCollection(0, 123);
  }

  @Test
  public void indexIsTooBig_throws() {
    assertIllegalArgumentException( () -> makeIndexInOrderedCollection(1, 1));
    assertIllegalArgumentException( () -> makeIndexInOrderedCollection(2, 2));
    assertIllegalArgumentException( () -> makeIndexInOrderedCollection(123, 123));

    IndexInOrderedCollection<InstrumentId> doesNotThrow;
    doesNotThrow = makeIndexInOrderedCollection(0, 1);
    doesNotThrow = makeIndexInOrderedCollection(1, 2);
    doesNotThrow = makeIndexInOrderedCollection(122, 123);
  }

  @Test
  public void testGetIndexFromEnd() {
    assertEquals(0, indexInSingletonCollection().getIndexFromEnd());
    assertEquals(0, makeIndexInOrderedCollection(0, 1).getIndexFromEnd());
    assertEquals(1, makeIndexInOrderedCollection(0, 2).getIndexFromEnd());
    assertEquals(0, makeIndexInOrderedCollection(1, 2).getIndexFromEnd());

    assertEquals(3, makeIndexInOrderedCollection(0, 4).getIndexFromEnd());
    assertEquals(2, makeIndexInOrderedCollection(1, 4).getIndexFromEnd());
    assertEquals(1, makeIndexInOrderedCollection(2, 4).getIndexFromEnd());
    assertEquals(0, makeIndexInOrderedCollection(3, 4).getIndexFromEnd());
  }

  private IndexInOrderedCollection<InstrumentId> makeIndexInOrderedCollection(int indexFromStart, int collectionSize) {
    return IndexInOrderedCollectionBuilder.<InstrumentId>indexInOrderedCollectionBuilder()
        .setIndexFromStart(indexFromStart)
        .setCollectionSize(collectionSize)
        .build();
  }

  @Test
  public void testCollectionCTOR() {
    ImmutableList<String> stringList5 = ImmutableList.of("A", "B", "C", "D", "E");
    IndexInOrderedCollection<String> index3InOrderedCollectionSize5 = indexInOrderedCollection(3, stringList5);

    assertEquals(5, index3InOrderedCollectionSize5.getCollectionSize());
    assertEquals(3, index3InOrderedCollectionSize5.getIndexFromStart());
    assertEquals(1, index3InOrderedCollectionSize5.getIndexFromEnd());

    assertIllegalArgumentException( () -> indexInOrderedCollection(-1, stringList5));
    assertIllegalArgumentException( () -> indexInOrderedCollection( 6, stringList5));
  }

  @Override
  public IndexInOrderedCollection<InstrumentId> makeTrivialObject() {
    return indexInSingletonCollection();
  }

  @Override
  public IndexInOrderedCollection<InstrumentId> makeNontrivialObject() {
    return IndexInOrderedCollectionBuilder.<InstrumentId>indexInOrderedCollectionBuilder()
        .setIndexFromStart(11)
        .setCollectionSize(33)
        .build();
  }

  @Override
  public IndexInOrderedCollection<InstrumentId> makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return IndexInOrderedCollectionBuilder.<InstrumentId>indexInOrderedCollectionBuilder()
        .setIndexFromStart(11)
        .setCollectionSize(33)
        .build();
  }

  @Override
  protected boolean willMatch(IndexInOrderedCollection<InstrumentId> expected,
                              IndexInOrderedCollection<InstrumentId> actual) {
    return indexInOrderedCollectionMatcher(expected).matches(actual);
  }

  public static <T>TypeSafeMatcher<IndexInOrderedCollection<T>> indexInOrderedCollectionMatcher(
      IndexInOrderedCollection<T> expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getIndexFromStart()),
        matchUsingEquals(v -> v.getCollectionSize()));
  }

}
