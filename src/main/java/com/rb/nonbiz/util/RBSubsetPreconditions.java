package com.rb.nonbiz.util;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.IidSet;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.RBSets;
import com.rb.nonbiz.text.Strings;

import java.util.Set;

import static com.rb.nonbiz.collections.IidSetOperations.differenceOfIidSets;
import static com.rb.nonbiz.collections.IidSetOperations.isSubsetOf;
import static com.rb.nonbiz.collections.RBSet.newRBSet;

public class RBSubsetPreconditions {

  // You could just use RBPreconditions.checkArgument(IidSets.isSubsetOf(expectedSubset, expectedSuperset))
  // but this takes care of printing what the problem was, if there is a problem.
  public static void checkIidsAreSubset(
      IidSet expectedSubset, IidSet expectedSuperset, String format, Object...args) {
    if (isSubsetOf(expectedSubset, expectedSuperset)) {
      return;
    }
    IidSet badInstrumentIds = differenceOfIidSets(expectedSubset, expectedSuperset);
    throw new IllegalArgumentException(Strings.format(
        "%s iids ( %s ) are in left but not right set, so %s -item iid left set ( %s ) is not a subset of %s -item right iid set ( %s ): %s",
        badInstrumentIds.size(), badInstrumentIds,
        expectedSubset.size(), expectedSubset,
        expectedSuperset.size(), expectedSuperset,
        Strings.format(format, args)));
  }

  public static void checkIidsAreSubset(
      IidSet expectedSubset, Set<InstrumentId> expectedSuperset, String format, Object...args) {
    if (isSubsetOf(expectedSubset, expectedSuperset)) {
      return;
    }
    IidSet badInstrumentIds = differenceOfIidSets(expectedSubset, expectedSuperset);
    throw new IllegalArgumentException(Strings.format(
        "%s iids in the %s -iid left set (expected subset) are not in the %s -iid right set (expected superset)."
            + " Iids: %s ; left set= %s ; right set= %s : %s",
        badInstrumentIds.size(), expectedSubset.size(), expectedSuperset.size(),
        badInstrumentIds, expectedSubset, expectedSuperset,
        Strings.format(format, args)));
  }

  // You could just to RBPreconditions.checkArgument(RBSets.isSubsetOf(expectedSubset, expectedSuperset))
  // but this takes care of printing what the problem was, if there is a problem.
  public static <T> void checkIsSubset(
      RBSet<T> expectedSubset, RBSet<T> expectedSuperset, String format, Object...args) {
    if (RBSets.isSubsetOf(expectedSubset, expectedSuperset)) {
      return;
    }
    RBSet<T> badItems = RBSets.difference(expectedSubset, expectedSuperset);
    throw new IllegalArgumentException(Strings.format(
        "%s : %s items in the %s -item left set (expected subset) are not in the %s -item right set (expected superset)."
            + " Items: %s ; left set= %s ; right set= %s : %s",
        Strings.format(format, args),
        badItems.size(), expectedSubset.size(), expectedSuperset.size(),
        badItems, expectedSubset, expectedSuperset));
  }

  public static <T> void checkIsSubset(
      Set<T> expectedSubset, Set<T> expectedSuperset, String format, Object...args) {
    checkIsSubset(newRBSet(expectedSubset), newRBSet(expectedSuperset), format, args);
  }

  public static <T> void checkIsSubset(
      RBSet<T> expectedSubset, Set<T> expectedSuperset, String format, Object...args) {
    checkIsSubset(expectedSubset, newRBSet(expectedSuperset), format, args);
  }

  public static <T> void checkIsSubset(
      Set<T> expectedSubset, RBSet<T> expectedSuperset, String format, Object...args) {
    checkIsSubset(newRBSet(expectedSubset), expectedSuperset, format, args);
  }

}
