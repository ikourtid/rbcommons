package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSets.noSharedItems;
import static com.rb.nonbiz.text.Strings.formatCollectionInDefaultOrder;
import static com.rb.nonbiz.types.PreciseValue.sumToBigDecimal;
import static com.rb.nonbiz.types.UnitFraction.isValidUnitFraction;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

/**
 * This is a set of changes that can be applied to a {@link Partition} to result in a new partition.
 * The semantics of how these can be applied to the original partition (call it <em>P</em>) are as follows:
 *   <ul>
 *      <li> {@link #getKeysToAddOrIncrease()} :
 *      if a key don't exist in the original partition, use the new weight, otherwise
 *      add the supplied weight to the existing ones. </li>
 *      <li> {@link #getKeysToRemoveOrDecrease()} :
 *      assert all keys here exist in the original partition. Reduce their original weights by the
 *      supplied weights. If the result is 0, then don't add it to the {@link Partition} (per that class's semantics).
 *      If the result is negative, throw an exception. </li>
 *   </ul>
 */
public class SimplePartitionModification<K> {

  private final RBMap<K, UnitFraction> keysToAddOrIncrease;
  private final RBMap<K, UnitFraction> keysToRemoveOrDecrease;

  private SimplePartitionModification(
      RBMap<K, UnitFraction> keysToAddOrIncrease,
      RBMap<K, UnitFraction> keysToRemoveOrDecrease) {
    this.keysToAddOrIncrease = keysToAddOrIncrease;
    this.keysToRemoveOrDecrease = keysToRemoveOrDecrease;
  }

  public static <K> SimplePartitionModification<K> emptySimplePartitionModification() {
    return SimplePartitionModificationBuilder.<K>simplePartitionModificationBuilder()
        .noKeysToAddOrIncrease()
        .noKeysToRemoveOrDecrease()
        .build();
  }

  public RBMap<K, UnitFraction> getKeysToAddOrIncrease() {
    return keysToAddOrIncrease;
  }

  public RBMap<K, UnitFraction> getKeysToRemoveOrDecrease() {
    return keysToRemoveOrDecrease;
  }

  @Override
  public String toString() {
    return toString(2);
  }

  public String toString(int precision) {
    return toStringInDecreasingMembershipOrder(precision, key -> key.toString());
  }

  public String toStringInIncreasingKeyOrder(int precision, Comparator<K> comparator, Function<K, String> keyToObject) {
    return toStringInOrder(precision, (e1, e2) -> comparator.compare(e1.getKey(), e2.getKey()), keyToObject);
  }

  public String toStringInDecreasingMembershipOrder(int precision, Function<K, String> keyToObject) {
    return toStringInOrder(precision, comparingByValue(reverseOrder()), keyToObject);
  }

  public String toStringInOrder(
      int precision,
      Comparator<Entry<K, UnitFraction>> comparator,
      Function<K, String> keyToObject) {
    Function<RBMap<K, UnitFraction>, List<String>> componentsMaker = keysToIncreaseOrDecrease ->
        keysToIncreaseOrDecrease
            .entrySet()
            .stream()
            .sorted(comparator)
            .map(e -> String.format("%s %s", e.getValue().toPercentString(precision), keyToObject.apply(e.getKey())))
            .collect(Collectors.toList());
    return Strings.format("[SPM toAddOrIncrease: %s ; toRemoveOrDecrease: %s SPM]",
        formatCollectionInDefaultOrder(componentsMaker.apply(keysToAddOrIncrease)),
        formatCollectionInDefaultOrder(componentsMaker.apply(keysToRemoveOrDecrease)));
  }


  public static class SimplePartitionModificationBuilder<K> implements RBBuilder<SimplePartitionModification<K>> {

    private RBMap<K, UnitFraction> keysToAddOrIncrease;
    private RBMap<K, UnitFraction> keysToRemoveOrDecrease;

    private SimplePartitionModificationBuilder() {}

    public static <K> SimplePartitionModificationBuilder<K> simplePartitionModificationBuilder() {
      return new SimplePartitionModificationBuilder<>();
    }

    public SimplePartitionModificationBuilder<K> setKeysToAddOrIncrease(RBMap<K, UnitFraction> keysToAddOrIncrease) {
      this.keysToAddOrIncrease = checkNotAlreadySet(this.keysToAddOrIncrease, keysToAddOrIncrease);
      return this;
    }

    public SimplePartitionModificationBuilder<K> noKeysToAddOrIncrease() {
      return setKeysToAddOrIncrease(emptyRBMap());
    }

    public SimplePartitionModificationBuilder<K> setKeysToRemoveOrDecrease(RBMap<K, UnitFraction> keysToRemoveOrDecrease) {
      this.keysToRemoveOrDecrease = checkNotAlreadySet(this.keysToRemoveOrDecrease, keysToRemoveOrDecrease);
      return this;
    }

    public SimplePartitionModificationBuilder<K> noKeysToRemoveOrDecrease() {
      return setKeysToRemoveOrDecrease(emptyRBMap());
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(keysToAddOrIncrease);
      RBPreconditions.checkNotNull(keysToRemoveOrDecrease);

      BigDecimal totalAdditions = sumToBigDecimal(keysToAddOrIncrease.values());
      BigDecimal totalSubtractions = sumToBigDecimal(keysToRemoveOrDecrease.values());

      RBPreconditions.checkArgument(
          Math.abs(totalAdditions.subtract(totalSubtractions).doubleValue()) < 1e-8,
          "The net total amounts to increase %s (minus decrease %s) must be zero or almost zero",
          totalAdditions, totalSubtractions);

      // We could also check totalKnownDeletions, but if this passes, then totalKnownDeletions will pass, because of the
      // previous precondition.
      RBPreconditions.checkArgument(
          isValidUnitFraction(totalAdditions),
          "We can't have a total decrease (of increase) by more than 100% (%s); there's no partition it could get applied to",
          totalAdditions);

      BiConsumer<RBMap<K, UnitFraction>, String> singleChecker = (map, mapName) ->
          RBPreconditions.checkArgument(
              map.values().stream().noneMatch(fractionToAdd -> fractionToAdd.isAlmostZero(1e-8)),
              "%s must not have any 0 differences implied: %s",
              mapName, map);

      singleChecker.accept(keysToAddOrIncrease,    "keysToAdd");
      singleChecker.accept(keysToRemoveOrDecrease, "keysToIncrease");

      RBPreconditions.checkArgument(
          noSharedItems(
              newRBSet(keysToAddOrIncrease.keySet()),
              newRBSet(keysToRemoveOrDecrease.keySet())),
          "We cannot have keys in more than one category: addOrIncrease= %s ; removeOrDecrease= %s",
          keysToAddOrIncrease, keysToRemoveOrDecrease);
    }

    @Override
    public SimplePartitionModification<K> buildWithoutPreconditions() {
      return new SimplePartitionModification<>(keysToAddOrIncrease, keysToRemoveOrDecrease);
    }

  }

}
