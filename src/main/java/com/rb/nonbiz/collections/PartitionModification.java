package com.rb.nonbiz.collections;

import com.google.common.base.Joiner;
import com.rb.nonbiz.types.SignedFraction;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

/**
 * This is a set of changes that can be applied to a {@link Partition} to result in a new partition.
 */
public class PartitionModification<K> {

  private final RBMap<K, SignedFraction> signedFractionsMap;

  private PartitionModification(RBMap<K, SignedFraction> signedFractionsMap) {
    this.signedFractionsMap = signedFractionsMap;
  }

  public static <K> PartitionModification<K> partitionModification(RBMap<K, SignedFraction> signedFractionsMap) {
    SignedFraction sumOfDifferences = SignedFraction.sum(signedFractionsMap.values());
    RBPreconditions.checkArgument(
        sumOfDifferences.isAlmostZero(1e-8),
        "A PartitionModification must have all modifications (positive and negative) sum to 0, but sum was %s",
        sumOfDifferences);

    return new PartitionModification<>(signedFractionsMap);
  }

  public static <K> PartitionModification<K> emptyPartitionModification() {
    return partitionModification(emptyRBMap());
  }

  public RBMap<K, SignedFraction> getSignedFractionsMap() {
    return signedFractionsMap;
  }

  public SignedFraction getOrZero(K key) {
    return signedFractionsMap.getOrDefault(key, SIGNED_FRACTION_0);
  }

  public SignedFraction getOrThrow(K key) {
    return signedFractionsMap.getOrThrow(key);
  }

  public int size() {
    return signedFractionsMap.size();
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
      Comparator<Entry<K, SignedFraction>> comparator,
      Function<K, String> keyToObject) {
    Iterator<String> components = signedFractionsMap.entrySet()
        .stream()
        .sorted(comparator)
        .map(e -> String.format("%s %s", e.getValue().toPercentString(precision), keyToObject.apply(e.getKey())))
        .iterator();
    return Joiner.on(" ; ").join(components);
  }

}
