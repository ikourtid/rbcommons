package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.SignedFraction;
import com.rb.nonbiz.types.UnitFraction;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.Partition.partition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.PreciseValue.sumToBigDecimal;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

/**
 * Use this whenever you want to represent having <i>N</i> things with proportions that sum to 1,
 * but where the weights are allowed to also be negative or {@code >} 1.
 *
 * <p> This is unlike a plain {@link Partition}, where the weights have to be a {@link UnitFraction} objects,
 * i.e. in the interval [0, 1]).
 *
 * @see Partition
 */
public class SignedPartition<K> {

  private final RBMap<K, SignedFraction> signedFractions;

  private SignedPartition(RBMap<K, SignedFraction> signedFractions) {
    this.signedFractions = signedFractions;
  }

  public static <K> SignedPartition<K> signedPartition(RBMap<K, SignedFraction> signedFractions) {
    for (SignedFraction signedFraction : signedFractions.values()) {
      if (signedFraction.isAlmostZero(DEFAULT_EPSILON_1e_8)) {
        throw new IllegalArgumentException(
            "Signed fractions in partitions cannot be zero. " +
                "If you don't want something, just don't put it into the partition");
      }
    }
    BigDecimal sum = sumToBigDecimal(signedFractions.values());
    if (sum.subtract(BigDecimal.ONE).abs(DEFAULT_MATH_CONTEXT).compareTo(BigDecimal.valueOf(1e-8)) > 0) {
      throw new IllegalArgumentException(
          String.format("Fractions should add to EXACTLY 1 but add to %.30f", sum));
    }
    return new SignedPartition<K>(signedFractions);
  }

  /**
   * Creates a SignedPartition by normalized the weights passed in to sum to 1.
   * The weights passed in are allowed not to sum to 1.
   */
  public static <K, V extends PreciseValue<V>> SignedPartition<K> signedPartitionFromWeights(RBMap<K, V> weightsMap) {
    BigDecimal sum = sumToBigDecimal(weightsMap.values());
    if (sum.signum() != 1) {
      throw new IllegalArgumentException(smartFormat("Sum of weights must be >0. Input was %s", weightsMap));
    }
    MutableRBMap<K, SignedFraction> signedFractionsMap = newMutableRBMapWithExpectedSize(weightsMap.size());
    weightsMap.forEachEntry( (key, value) -> {
      BigDecimal bd = value.asBigDecimal();
      if (bd.signum() == 0) {
        return;
      }
      signedFractionsMap.putAssumingAbsent(key, signedFraction(bd.divide(sum, DEFAULT_MATH_CONTEXT)));
    });
    return new SignedPartition<K>(newRBMap(signedFractionsMap));
  }

  /**
   * Creates a SignedPartition that's 100% of this item.
   */
  public static <K> SignedPartition<K> singletonSignedPartition(K key) {
    return signedPartition(singletonRBMap(
        key, SIGNED_FRACTION_1));
  }

  public Set<K> keySet() {
    return signedFractions.keySet();
  }

  public Set<Entry<K, SignedFraction>> entrySet() {
    return signedFractions.entrySet();
  }

  public boolean containsKey(K key) {
    return signedFractions.containsKey(key);
  }

  public SignedFraction getFraction(K key) {
    Optional<SignedFraction> signedFraction = signedFractions.getOptional(key);
    if (!signedFraction.isPresent()) {
      throw new IllegalArgumentException(smartFormat(
          "Key %s is not contained in partition's keys : %s",
          key, Joiner.on(',').join(signedFractions.keySet())));
    }
    return signedFraction.get();
  }

  @VisibleForTesting
  public RBMap<K, SignedFraction> getRawFractionsMap() {
    return signedFractions;
  }

  public SignedFraction getOrZero(K key) {
    return signedFractions.getOrDefault(key, SIGNED_FRACTION_0);
  }

  /**
   * Converts this {@code SignedPartition<K>} to a {@code Partition<K>}. If any of the weights is negative or 0, this will throw.
   */
  public Partition<K> toPartitionOfUnsigned() {
    return partition(signedFractions.transformValuesCopy(sf -> sf.toUnitFraction()));
  }

  @Override
  public String toString() {
    return toString(0);
  }

  public String toString(int precision) {
    return toStringInDecreasingMembershipOrder(precision, key -> key.toString());
  }

  public String toStringInIncreasingKeyOrder(int precision, Comparator<K> comparator, Function<K, String> keyToObject) {
    return toStringInOrder(precision, (e1, e2) -> comparator.compare(e1.getKey(), e2.getKey()), keyToObject);
  }

  public String toStringInDecreasingMembershipOrder(int precision, Function<K, String> keyToObject) {
    return toStringInOrder(precision, comparing(e -> e.getValue(), reverseOrder()), keyToObject);
  }

  public String toStringInDecreasingAbsMembershipOrder(int precision, Function<K, String> keyToObject) {
    return toStringInOrder(precision, comparing(e -> e.getValue().asBigDecimal().abs(), reverseOrder()), keyToObject);
  }

  public String toStringInOrder(int precision, Comparator<Entry<K, SignedFraction>> comparator, Function<K, String> keyToObject) {
    List<String> components = signedFractions.entrySet()
        .stream()
        .sorted(comparator)
        .map(e -> String.format("%s %s", e.getValue().toPercentString(precision), keyToObject.apply(e.getKey())))
        .collect(Collectors.toList());
    return Joiner.on(" ; ").join(components);
  }

  public int size() {
    return signedFractions.size();
  }

}
