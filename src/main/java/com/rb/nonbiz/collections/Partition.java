package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBStreams.sumAsBigDecimals;
import static com.rb.nonbiz.collections.SignedPartition.signedPartition;
import static com.rb.nonbiz.types.PreciseValue.sumToBigDecimal;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

/**
 * Use this whenever you want to represent having N items with proportions that sum to 1,
 * where the items all have a positive proportion.
 * A traditional asset allocation that does not allow short positions is a good example.
 *
 * #see LinearCombination
 * #see FlatLinearCombination
 */
public class Partition<K> {

  private final RBMap<K, UnitFraction> fractions;

  private Partition(RBMap<K, UnitFraction> fractions) {
    this.fractions = fractions;
  }

  public static <K> Partition<K> partition(RBMap<K, UnitFraction> fractions) {
    for (UnitFraction unitFraction : fractions.values()) {
      if (unitFraction.isZero()) {
        throw new IllegalArgumentException(
            "Fractions in partitions cannot be zero. If you don't want something, just don't put it into the partition");
      }
    }
    double sum = sumAsBigDecimals(fractions.values()).doubleValue();
    RBPreconditions.checkArgument(
        Math.abs(sum - 1) <= 1e-8,
        "Fractions sum to %s which is not near 1 within an epsilon of 1e-8: %s",
        sum, fractions);
    return new Partition<>(fractions);
  }

  public static <K, V extends PreciseValue<V>> Partition<K> partitionFromWeights(RBMap<K, V> weightsMap) {
    BigDecimal sum = sumToBigDecimal(weightsMap.values());
    if (sum.signum() != 1) {
      throw new IllegalArgumentException(Strings.format("Sum of weights must be >0. Input was %s", weightsMap));
    }
    MutableRBMap<K, UnitFraction> fractionsMap = newMutableRBMapWithExpectedSize(weightsMap.size());
    weightsMap.forEachEntry( (key, value) -> {
      BigDecimal bd = value.asBigDecimal();
      if (bd.signum() == 0) {
        return;
      }
      RBPreconditions.checkArgument(
          bd.signum() == 1,
          "Cannot create a PreciseValue weights partition if it includes a negative or zero weight of %s : input was %s",
          bd, weightsMap);
      fractionsMap.putAssumingAbsent(key, unitFraction(bd.divide(sum, DEFAULT_MATH_CONTEXT)));
    });
    return new Partition<>(newRBMap(fractionsMap));
  }

  /**
   * Creates a partition by normalizing a map of weights which may not sum to 100%.
   *
   */
  public static <K> Partition<K> partitionFromWeights(DoubleMap<K> weightsMap) {
    // epsilon of 1e-12 is tighter than the usual 1e-8; note that doubles normally have precision of around 1e-14 to 1e-15.
    double e = 1e-12;
    double sum = weightsMap.sum();
    if (sum <= e) {
      throw new IllegalArgumentException(Strings.format("Sum of weights must be >0 (actually, 1e-12). Input was %s", weightsMap));
    }
    MutableRBMap<K, UnitFraction> fractionsMap = newMutableRBMapWithExpectedSize(weightsMap.size());
    weightsMap.getRawMap().forEachEntry( (key, weight) -> {
      if (Math.abs(weight) < e) {
        return;
      }
      RBPreconditions.checkArgument(
          weight > e,
          "Cannot create a PreciseValue weights partition if it includes a negative or zero weight of %s : input was %s",
          weight, weightsMap);
      fractionsMap.putAssumingAbsent(key, unitFraction(weight / sum));
    });
    return new Partition<>(newRBMap(fractionsMap));
  }

  public static <K> Partition<K> singletonPartition(K key) {
    return partition(singletonRBMap(
        key, UNIT_FRACTION_1));
  }

  public boolean containsOnlyKey(K key) {
    return fractions.containsOnlyKey(key);
  }

  public Set<K> keySet() {
    return fractions.keySet();
  }

  public Set<Entry<K, UnitFraction>> entrySet() {
    return fractions.entrySet();
  }

  public boolean containsKey(K key) {
    return fractions.containsKey(key);
  }

  public UnitFraction getFraction(K key) {
    Optional<UnitFraction> fraction = fractions.getOptional(key);
    if (!fraction.isPresent()) {
      throw new IllegalArgumentException(Strings.format(
          "Key %s is not contained in partition's keys : %s",
          key, Joiner.on(',').join(fractions.keySet())));
    }
    return fraction.get();
  }

  @VisibleForTesting
  public RBMap<K, UnitFraction> getRawFractionsMap() {
    return fractions;
  }

  public UnitFraction getOrZero(K key) {
    return fractions.getOrDefault(key, UNIT_FRACTION_0);
  }

  public UnitFraction getOrThrow(K key) {
    return fractions.getOrThrow(key);
  }

  public SignedPartition<K> toSignedPartition() {
    return signedPartition(fractions.transformValuesCopy(f -> f.toSignedFraction()));
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
    return toStringInOrder(precision, comparing(e -> e.getValue(), reverseOrder()), keyToObject);
  }

  public String toStringInOrder(int precision, Comparator<Entry<K, UnitFraction>> comparator, Function<K, String> keyToObject) {
    Iterator<String> components = fractions.entrySet()
        .stream()
        .sorted(comparator)
        .map(e -> String.format("%s %s", e.getValue().toPercentString(precision), keyToObject.apply(e.getKey())))
        .iterator();
    return Joiner.on(" ; ").join(components);
  }

  public int size() {
    return fractions.size();
  }

}
