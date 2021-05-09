package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.RBSets.TwoRBSetsVisitor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.rb.nonbiz.collections.HasLongSets.noSharedHasLongs;
import static com.rb.nonbiz.collections.HasLongSets.noSharedHasLongsInCollection;
import static com.rb.nonbiz.collections.HasLongSets.toMutableHasLongSetIfUnique;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondThirdAndRest;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;

public class IidSets {

  /**
   * Useful when you are iterating over 2 IidSets, and want to cleanly separate out the behavior when an instrument
   * appears only on the left, only on the right, or on both sets.
   *
   * You could always just do this with a bunch of ifs, but this is cleaner and more functional-looking.
   *
   * @see TwoRBSetsVisitor
   */
  public interface TwoIidSetsVisitor {

    void visitInstrumentInLeftSetOnly(InstrumentId itemInLeftSetOnly);
    void visitInstrumentInRightSetOnly(InstrumentId itemInRightSetOnly);
    void visitInstrumentInBothSets(InstrumentId itemInBothSets);

  }

  public static boolean noSharedIids(IidSet set1, IidSet set2) {
    return noSharedHasLongs(set1, set2);
  }

  public static boolean noSharedIids(Set<InstrumentId> set1, IidSet set2) {
    return noSharedHasLongs(set1, set2);
  }

  public static boolean noSharedIids(IidSet set1, Set<InstrumentId> set2) {
    return noSharedHasLongs(set1, set2);
  }

  public static boolean noSharedIidsInCollection(
      Collection<IidSet> hasLongSets) {
    return noSharedHasLongsInCollection(hasLongSets);
  }

  public static boolean noSharedIids(IidSet first, IidSet second, IidSet third, IidSet ... rest) {
    return noSharedIidsInCollection(concatenateFirstSecondThirdAndRest(first, second, third, rest));
  }

  /**
   * Returns empty if the instrument IDs in the list are not unique, otherwise it returns them as a IidSet.
   */
  public static Optional<IidSet> toIidSetIfUnique(List<InstrumentId> items) {
    return toIidSetIfUnique(items.iterator(), items.size());
  }

  /**
   * Returns empty if the InstrumentIds are not unique, otherwise it returns them as a IidSet.
   */
  public static Optional<IidSet> toIidSetIfUnique(Iterator<InstrumentId> items, int sizeHint) {
    return transformOptional(
        toMutableHasLongSetIfUnique(items, sizeHint),
        v -> newIidSet(v));
  }

  /**
   * When you have 2 sets, this lets you perform 3 different actions for instruments that are only in the left set,
   * only in the right, or in both sets.
   *
   * It lets you specify a bit more cleanly (via the TwoRBSetsVisitor) what to do in those 3 different cases.
   */
  public static void visitInstrumentsOfTwoSets(IidSet leftSet, IidSet rightSet, TwoIidSetsVisitor visitor) {
    for (InstrumentId instrument1 : leftSet) {
      if (rightSet.contains(instrument1)) {
        visitor.visitInstrumentInBothSets(instrument1);
      } else {
        visitor.visitInstrumentInLeftSetOnly(instrument1);
      }
    }
    for (InstrumentId instrument2 : rightSet) {
      if (!leftSet.contains(instrument2)) {
        visitor.visitInstrumentInRightSetOnly(instrument2);
      }
    }
  }

}
