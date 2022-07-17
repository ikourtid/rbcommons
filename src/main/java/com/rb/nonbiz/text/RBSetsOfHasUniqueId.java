package com.rb.nonbiz.text;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromCollection;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.text.RBSetOfHasUniqueId.rbSetOfHasUniqueId;

/**
 * Utilities dealing with {@link RBSetOfHasUniqueId}.
 *
 * @see RBSetsOfHasUniqueId
 */
public class RBSetsOfHasUniqueId {

  public static <V extends HasUniqueId<V>> RBSetOfHasUniqueId<V> singletonRBSetOfHasUniqueId(V onlyItem) {
    return rbSetOfHasUniqueId(singletonRBMap(onlyItem.getUniqueId(), onlyItem));
  }

  @SafeVarargs
  public static <V extends HasUniqueId<V>> RBSetOfHasUniqueId<V> rbSetOfHasUniqueIdOf(V first, V second, V ... rest) {
    return rbSetOfHasUniqueId(
        rbMapFromCollection(
            concatenateFirstSecondAndRest(first, second, rest), v -> v.getUniqueId()));
  }

}
