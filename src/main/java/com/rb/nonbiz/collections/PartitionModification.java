package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBSets.noSharedItems;
import static com.rb.nonbiz.text.Strings.formatCollectionInDefaultOrder;
import static com.rb.nonbiz.types.PreciseValue.sumToBigDecimal;
import static com.rb.nonbiz.types.UnitFraction.isValidUnitFraction;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

/**
 * This is a set of changes that can be applied to a {@link Partition} to result in a new partition.
 */
public class PartitionModification<K> {

  private final RBMap<K, UnitFraction> keysToIncrease;
  private final RBMap<K, UnitFraction> keysToDecrease;

  private PartitionModification(
      RBMap<K, UnitFraction> keysToIncrease,
      RBMap<K, UnitFraction> keysToDecrease) {
    this.keysToIncrease = keysToIncrease;
    this.keysToDecrease = keysToDecrease;
  }

  public static <K> PartitionModification<K> emptyPartitionModification() {
    return PartitionModificationBuilder.<K>partitionModificationBuilder()
        .setKeysToIncrease(emptyRBMap())
        .setKeysToDecrease(emptyRBMap())
        .build();
  }

  public RBMap<K, UnitFraction> getKeysToIncrease() {
    return keysToIncrease;
  }

  public RBMap<K, UnitFraction> getKeysToDecrease() {
    return keysToDecrease;
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
    return Strings.format("toIncrease: %s ; toDecrease: %s",
        formatCollectionInDefaultOrder(componentsMaker.apply(keysToIncrease)),
        formatCollectionInDefaultOrder(componentsMaker.apply(keysToDecrease)));
  }


  public static class PartitionModificationBuilder<K> implements RBBuilder<PartitionModification<K>> {

    private RBMap<K, UnitFraction> keysToIncrease;
    private RBMap<K, UnitFraction> keysToDecrease;

    private PartitionModificationBuilder() {}

    public static <K> PartitionModificationBuilder<K> partitionModificationBuilder() {
      return new PartitionModificationBuilder<>();
    }

    public PartitionModificationBuilder<K> setKeysToIncrease(RBMap<K, UnitFraction> keysToIncrease) {
      this.keysToIncrease = checkNotAlreadySet(this.keysToIncrease, keysToIncrease);
      return this;
    }

    public PartitionModificationBuilder<K> setKeysToDecrease(RBMap<K, UnitFraction> keysToDecrease) {
      this.keysToDecrease = checkNotAlreadySet(this.keysToDecrease, keysToDecrease);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(keysToIncrease);
      RBPreconditions.checkNotNull(keysToDecrease);

      BigDecimal totalToIncrease = sumToBigDecimal(keysToIncrease.values());
      BigDecimal totalToDecrease = sumToBigDecimal(keysToDecrease.values());

      RBPreconditions.checkArgument(
          Math.abs(totalToIncrease.subtract(totalToDecrease).doubleValue()) < 1e-8,
          "The net total amounts to increase %s (minus decrease %s) must be zero",
          totalToIncrease, totalToDecrease);

      // We could also check totalToDecrease, but if this passes, then totalToDecrease will pass, because of the
      // previous precondition.
      RBPreconditions.checkArgument(
          isValidUnitFraction(totalToIncrease),
          "We can't have a total decrease (of increase) by more than 100% (%s); there's no partition it could get applied to",
          totalToIncrease);
      
      RBPreconditions.checkArgument(
          keysToIncrease.values().stream().noneMatch(increaseFraction -> increaseFraction.isAlmostZero(1e-8)),
          "keysToIncrease must not have any 0 differences implied: %s",
          keysToIncrease);

      RBPreconditions.checkArgument(
          keysToDecrease.values().stream().noneMatch(decreaseFraction -> decreaseFraction.isAlmostZero(1e-8)),
          "keysToDecrease must not have any 0 differences implied: %s",
          keysToDecrease);

      RBPreconditions.checkArgument(
          noSharedItems(keysToIncrease.keySet(), keysToDecrease.keySet()),
          "We cannot have keys that are meant to both increase and decrease: %s vs. %s",
          keysToIncrease, keysToDecrease);
    }

    @Override
    public PartitionModification<K> buildWithoutPreconditions() {
      return new PartitionModification<>(keysToIncrease, keysToDecrease);
    }

  }

}
