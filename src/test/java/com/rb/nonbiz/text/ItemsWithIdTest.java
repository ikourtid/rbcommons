package com.rb.nonbiz.text;

import org.junit.Test;

import java.util.List;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.ItemWithId.itemWithId;
import static com.rb.nonbiz.text.ItemsWithId.itemsWithUniqueIds;

public class ItemsWithIdTest {

  @Test
  public void throwsIfIdsAreNotUnique() {
    List<ItemWithId<Integer>> doesNotThrow;
    doesNotThrow = itemsWithUniqueIds(itemWithId("a", 1));
    doesNotThrow = itemsWithUniqueIds(itemWithId("a", 1), itemWithId("b", 1));
    assertIllegalArgumentException( () -> itemsWithUniqueIds(
        itemWithId("a", 1),
        itemWithId("a", 1)));
    assertIllegalArgumentException( () -> itemsWithUniqueIds(
        itemWithId("a", 1),
        itemWithId("b", 1),
        itemWithId("a", 1)));
  }

}
