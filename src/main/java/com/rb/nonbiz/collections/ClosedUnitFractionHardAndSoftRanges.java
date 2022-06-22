package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.newRBSet;

/**
 * This is a generalization of the following use case:
 *
 * <p> Say an asset class is specified by an advisor to be between 30% to 40% of total (hard range). </p>
 *
 * <p> If a portfolio is outside the hard range, we should force it to be a bit inside of the 30%-40% range -
 * i.e. not exactly at the edge (e.g. 30%).
 * For example, we can use the 'soft range' of 31% to 39%, post-trading. </p>
 *
 * <p> Here's why: If an asset class at 29% was traded to be exactly at 30%,
 * then if it dropped in price more than the other asset classes on the next day and went to 29.99% of the total,
 * we'd have to trade again. </p>
 *
 * <p> It's like how a house heating thermostat may turn on the heat when the temperature goes to 68,
 * but only turn it off when it goes to 70. It is to prevent oscillations around a single cutoff point. </p>
 *
 * @see ClosedUnitFractionHardAndSoftRange
 */
public class ClosedUnitFractionHardAndSoftRanges<K> {

  private final RBMap<K, ClosedUnitFractionHardAndSoftRange> rawMap;

  private ClosedUnitFractionHardAndSoftRanges(
      RBMap<K, ClosedUnitFractionHardAndSoftRange> rawMap) {
    this.rawMap = rawMap;
  }

  public static <K> ClosedUnitFractionHardAndSoftRanges<K> closedUnitFractionHardAndSoftRanges(
      RBMap<K, ClosedUnitFractionHardAndSoftRange> rawMap) {
    // The following is additionally very convenient when we convert back and forth from JSON,
    // because we do not like our API having empty entries just to represent unrestricted asset classes,
    // since that will likely happen a lot.
    RBPreconditions.checkArgument(
        rawMap.values().stream().noneMatch(range -> range.isUnrestricted()),
        "If a range is unrestricted, don't add it in: %s",
        rawMap);
    return new ClosedUnitFractionHardAndSoftRanges<>(rawMap);
  }

  public static <K> ClosedUnitFractionHardAndSoftRanges<K> emptyClosedUnitFractionHardAndSoftRanges() {
    return closedUnitFractionHardAndSoftRanges(emptyRBMap());
  }

  public RBMap<K, ClosedUnitFractionHardAndSoftRange> getRawMap() {
    return rawMap;
  }

  public ClosedUnitFractionHardAndSoftRanges<K> copyWithOverrideIfUnrestricted(
      K key, ClosedUnitFractionHardAndSoftRange newHardAndSoftRange) {
    if (newHardAndSoftRange.isUnrestricted()) {
      // This avoids the precondition in the static constructor.
      RBPreconditions.checkArgument(
          getSharedKeys().contains(key),
          "Trying to override key %s which is not present in the map",
          key);
      // The semantics used by ClosedUnitFractionHardAndSoftRanges specify that a missing key means that
      // there is no range, i.e. the range is [0, 1]. Therefore, if the new range is unrestricted,
      // we have to remove the key, so that key's absence will indicate that the range is unrestricted.
      return closedUnitFractionHardAndSoftRanges(rawMap.copyWithKeyRemoved(key));
    }
    return closedUnitFractionHardAndSoftRanges(rawMap.copyWithOverridesApplied(singletonRBMap(
        key, newHardAndSoftRange)));
  }

  public Optional<ClosedUnitFractionHardAndSoftRange> getOptionalHardAndSoftRange(K key) {
    return rawMap.getOptional(key);
  }

  public RBSet<K> getSharedKeys() {
    return newRBSet(rawMap.keySet());
  }

  @Override
  public String toString() {
    return rawMap.isEmpty()
        ? Strings.format("[CUFHASRs]")
        : Strings.format("[CUFHASRs %s CUFHASRs]", rawMap);
  }

}
