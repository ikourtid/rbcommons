package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSets.noSharedItems;
import static com.rb.nonbiz.text.Strings.formatCollectionInDefaultOrder;
import static com.rb.nonbiz.text.Strings.formatOptional;
import static com.rb.nonbiz.types.PreciseValue.sumToBigDecimal;
import static com.rb.nonbiz.types.UnitFraction.isValidUnitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

/**
 * This is a set of changes that can be applied to a {@link Partition} to result in a new partition.
 * The semantics of how these can be applied to the original partition (call it <em>P</em>) are as follows:
 *   <ul>
 *      <li> {@link #getKeysToAdd()} :
 *      assert these don't exist in the original partition; use the new weights. </li>
 *      <li> {@link #getKeysToIncrease()}:
 *      assert these exist in the original partition; add the supplied weights to the existing ones. </li>
 *      <li> {@link #getKeysToRemove()} :
 *      assert these exist in the original partition with a weight equal to the supplied one (subject to
 *      {@link #getEpsilonForRemovalSanityChecks()} ); these keys will not appear in the modified partition. </li>
 *      <li> {@link #getKeysToDecrease()} :
 *      assert these exist in the original partition; subtract the supplied weights from the existing ones. </li>
 *   </ul>
 *
 *   <p> See the getters for explanations on the epsilons. They are not the same, intentionally. If, for example,
 *   each individual item to be removed has precision in the single bps (e.g. due to rounding), i.e. it is
 *   imprecise to 0.5 bps, then it's possible that many such < 0.5 bps deviations (assuming they don't cancel out)
 *   add up to much more than 1 bps, in which case {@link #getEpsilonForNetAdditionSanityCheck()} should be larger
 *   than {@link #getEpsilonForRemovalSanityChecks()}. </p>
 */
public class DetailedPartitionModification<K> {

  private final RBMap<K, UnitFraction> keysToAdd;
  private final RBMap<K, UnitFraction> keysToIncrease;
  private final RBMap<K, UnitFraction> keysToRemove;
  private final RBMap<K, UnitFraction> keysToDecrease;
  private final UnitFraction epsilonForRemovalSanityChecks;
  private final Optional<UnitFraction> epsilonForNetAdditionSanityCheck;

  private DetailedPartitionModification(
      RBMap<K, UnitFraction> keysToAdd,
      RBMap<K, UnitFraction> keysToIncrease,
      RBMap<K, UnitFraction> keysToRemove,
      RBMap<K, UnitFraction> keysToDecrease,
      UnitFraction epsilonForRemovalSanityChecks,
      Optional<UnitFraction> epsilonForNetAdditionSanityCheck) {
    this.keysToAdd = keysToAdd;
    this.keysToIncrease = keysToIncrease;
    this.keysToRemove = keysToRemove;
    this.keysToDecrease = keysToDecrease;
    this.epsilonForRemovalSanityChecks = epsilonForRemovalSanityChecks;
    this.epsilonForNetAdditionSanityCheck = epsilonForNetAdditionSanityCheck;
  }

  public static <K> DetailedPartitionModification<K> emptyDetailedPartitionModification() {
    return DetailedPartitionModificationBuilder.<K>detailedPartitionModificationBuilder()
        .noKeysToAdd()
        .noKeysToIncrease()
        .noKeysToRemove()
        .noKeysToDecrease()
        .useStandardEpsilonForRemovalSanityChecks()
        .useStandardEpsilonForNetAdditionSanityCheck()
        .build();
  }

  public RBMap<K, UnitFraction> getKeysToAdd() {
    return keysToAdd;
  }

  public RBMap<K, UnitFraction> getKeysToIncrease() {
    return keysToIncrease;
  }

  /**
   * Items to remove, together with their existing weights, which will be used for sanity checking purposes.
   *
   * <p> This could have been a plain {@link RBSet}, since the original / existing weights aren't really needed
   * for determining the final partition. However, specifying the existing weights is useful for having
   * construction preconditions that confirm that the total additions add up to the same percentage as
   * deletions. Also, in scenarios where this is used in tests to convey expected behavior, specifying the
   * existing weights will add extra clarity to the tests. </p>
   */
  public RBMap<K, UnitFraction> getKeysToRemove() {
    return keysToRemove;
  }

  public RBMap<K, UnitFraction> getKeysToDecrease() {
    return keysToDecrease;
  }

  public UnitFraction getEpsilonForRemovalSanityChecks() {
    return epsilonForRemovalSanityChecks;
  }

  /**
   * There is a precondition that the total fractions to add or increase must equal the total fractions
   * to subtract or increase. This makes sense, so that the total of everything still remains at 100%.
   * However, in some cases such as tests (where we want to avoid too much precision because it clutters up the
   * test results), the difference may be larger than the usual epsilon of 1e-8. In such cases,
   * {@link #getEpsilonForNetAdditionSanityCheck()} allow us to specify a larger (and possibly also smaller)
   * epsilon. The reason it is optional is that if it is present, then the
   * {@link SingleDetailedPartitionModificationApplier} will know about it so that it can normalize the weights
   * to add up to exactly 100%.
   */
  public Optional<UnitFraction> getEpsilonForNetAdditionSanityCheck() {
    return epsilonForNetAdditionSanityCheck;
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
    return Strings.format(
        "[DPM toAdd= %s ; toIncrease= %s ; toRemove= %s ; toDecrease= %s ; "
        + "epsilonForRemovalSanityChecks= %s ; epsilonForNetAdditionSanityCheck= %s DPM]",
        formatCollectionInDefaultOrder(componentsMaker.apply(keysToAdd)),
        formatCollectionInDefaultOrder(componentsMaker.apply(keysToIncrease)),
        formatCollectionInDefaultOrder(componentsMaker.apply(keysToRemove)),
        formatCollectionInDefaultOrder(componentsMaker.apply(keysToDecrease)),
        epsilonForRemovalSanityChecks,
        formatOptional(epsilonForNetAdditionSanityCheck));
  }


  public static class DetailedPartitionModificationBuilder<K> implements RBBuilder<DetailedPartitionModification<K>> {

    private RBMap<K, UnitFraction> keysToAdd;
    private RBMap<K, UnitFraction> keysToIncrease;
    private RBMap<K, UnitFraction> keysToRemove;
    private RBMap<K, UnitFraction> keysToDecrease;
    private UnitFraction epsilonForRemovalSanityChecks;
    private Optional<UnitFraction> epsilonForNetAdditionSanityCheck;

    private DetailedPartitionModificationBuilder() {}

    public static <K> DetailedPartitionModificationBuilder<K> detailedPartitionModificationBuilder() {
      return new DetailedPartitionModificationBuilder<>();
    }

    public DetailedPartitionModificationBuilder<K> setKeysToAdd(RBMap<K, UnitFraction> keysToAdd) {
      this.keysToAdd = checkNotAlreadySet(this.keysToAdd, keysToAdd);
      return this;
    }

    public DetailedPartitionModificationBuilder<K> noKeysToAdd() {
      return setKeysToAdd(emptyRBMap());
    }

    public DetailedPartitionModificationBuilder<K> setKeysToIncrease(RBMap<K, UnitFraction> keysToIncrease) {
      this.keysToIncrease = checkNotAlreadySet(this.keysToIncrease, keysToIncrease);
      return this;
    }

    public DetailedPartitionModificationBuilder<K> noKeysToIncrease() {
      return setKeysToIncrease(emptyRBMap());
    }

    public DetailedPartitionModificationBuilder<K> setKeysToRemove(RBMap<K, UnitFraction> keysToRemove) {
      this.keysToRemove = checkNotAlreadySet(this.keysToRemove, keysToRemove);
      return this;
    }

    public DetailedPartitionModificationBuilder<K> noKeysToRemove() {
      return setKeysToRemove(emptyRBMap());
    }

    public DetailedPartitionModificationBuilder<K> setKeysToDecrease(RBMap<K, UnitFraction> keysToDecrease) {
      this.keysToDecrease = checkNotAlreadySet(this.keysToDecrease, keysToDecrease);
      return this;
    }

    public DetailedPartitionModificationBuilder<K> noKeysToDecrease() {
      return setKeysToDecrease(emptyRBMap());
    }

    public DetailedPartitionModificationBuilder<K> setEpsilonForRemovalSanityChecks(
        UnitFraction epsilonForRemovalSanityChecks) {
      this.epsilonForRemovalSanityChecks = checkNotAlreadySet(
          this.epsilonForRemovalSanityChecks, epsilonForRemovalSanityChecks);
      return this;
    }

    public DetailedPartitionModificationBuilder<K> useStandardEpsilonForRemovalSanityChecks() {
      return setEpsilonForRemovalSanityChecks(unitFraction(1e-8));
    }

    public DetailedPartitionModificationBuilder<K> setEpsilonForNetAdditionSanityCheck(
        UnitFraction epsilonForNetAdditionSanityCheck) {
      this.epsilonForNetAdditionSanityCheck = checkNotAlreadySet(
          this.epsilonForNetAdditionSanityCheck, Optional.of(epsilonForNetAdditionSanityCheck));
      return this;
    }

    public DetailedPartitionModificationBuilder<K> useStandardEpsilonForNetAdditionSanityCheck() {
      this.epsilonForNetAdditionSanityCheck = checkNotAlreadySet(
          this.epsilonForNetAdditionSanityCheck, Optional.empty());
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(keysToAdd);
      RBPreconditions.checkNotNull(keysToIncrease);
      RBPreconditions.checkNotNull(keysToRemove);
      RBPreconditions.checkNotNull(keysToDecrease);
      RBPreconditions.checkNotNull(epsilonForRemovalSanityChecks);
      RBPreconditions.checkNotNull(epsilonForNetAdditionSanityCheck);

      BigDecimal totalAdditions = sumToBigDecimal(keysToIncrease.values())
          .add(sumToBigDecimal(keysToAdd.values()));
      BigDecimal totalSubtractions = sumToBigDecimal(keysToDecrease.values())
          .add(sumToBigDecimal(keysToRemove.values()));

      RBPreconditions.checkArgument(
          Math.abs(totalAdditions.subtract(totalSubtractions).doubleValue())
              < transformOptional(epsilonForNetAdditionSanityCheck, v -> v.doubleValue()).orElse(1e-8),
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
              map.values().stream().noneMatch(changeFraction -> changeFraction.isAlmostZero(1e-8)),
              "%s must not have any 0 differences implied: %s",
              mapName, map);

      singleChecker.accept(keysToAdd,      "keysToAdd");
      singleChecker.accept(keysToIncrease, "keysToIncrease");
      singleChecker.accept(keysToDecrease, "keysToDecrease");
      singleChecker.accept(keysToRemove,   "keysToRemove");

      RBPreconditions.checkArgument(
          noSharedItems(
              newRBSet(keysToAdd.keySet()),
              newRBSet(keysToIncrease.keySet()),
              newRBSet(keysToRemove.keySet()),
              newRBSet(keysToDecrease.keySet())),
          "We cannot have keys in more than one category: add= %s ; increase= %s ; remove= %s ; decrease= %s",
          keysToAdd, keysToIncrease, keysToRemove, keysToDecrease);
    }

    @Override
    public DetailedPartitionModification<K> buildWithoutPreconditions() {
      return new DetailedPartitionModification<>(
          keysToAdd, keysToIncrease, keysToRemove, keysToDecrease,
          epsilonForRemovalSanityChecks, epsilonForNetAdditionSanityCheck);
    }

  }

}
