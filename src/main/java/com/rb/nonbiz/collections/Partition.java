package com.rb.nonbiz.collections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.rb.biz.types.Money;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.SignedFraction;
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
import static com.rb.nonbiz.text.SmartFormatter.smartFormat;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.PreciseValue.sumToBigDecimal;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

/**
 * Use this whenever you want to represent having <i>N</i> items with proportions that sum to 1,
 * where the items all have a positive weight.
 *
 * <p> An asset allocation that does not allow short positions is a good example. </p>
 *
 * @see LinearCombination
 * @see FlatLinearCombination
 */
public class Partition<K> {

  private final RBMap<K, UnitFraction> fractions;

  private Partition(RBMap<K, UnitFraction> fractions) {
    this.fractions = fractions;
  }

  /**
   * Creates a {@link Partition} from an {@link RBMap} of {@link UnitFraction}s.
   */
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

  /**
   * Creates a partition by normalizing a map of positive weights which may not sum to 100%.
   *
   * <p> There is a similar overload that takes in {@link DoubleMap}, where the concept of summing to 100% makes more sense.
   * Here, since we're dealing with {@link PreciseValue}, it's a bit meaningless to care about 1 vs. not 1 if the weights
   * are of type e.g. {@link Money}. But for certain other types ({@link UnitFraction}, {@link SignedFraction}, etc.)
   * it does make sense. However, here's no way to distinguish that given how generics work in Java. So this is reasonable. </p>
   */
  public static <K, V extends PreciseValue<V>> Partition<K> partitionFromPositiveWeightsWhichMayNotSumTo1(
      RBMap<K, V> weightsMap) {
    BigDecimal sum = sumToBigDecimal(weightsMap.values());
    RBPreconditions.checkArgument(
        sum.signum() == 1,
      "Sum of weights must be >0. Input was %s", weightsMap);
    return partitionFromPositiveWeights(weightsMap, sum);
  }

  /**
   * @see #partitionFromPositiveWeightsWhichMaySumToBelow1(RBMap, Epsilon)
   */
  public static <K, V extends PreciseValue<V>> Partition<K> partitionFromPositiveWeightsWhichMaySumToBelow1(
      RBMap<K, V> weightsMap) {
    return partitionFromPositiveWeightsWhichMaySumToBelow1(weightsMap, epsilon(1e-12));
  }

  /**
   * Creates a partition by normalizing a map of positive weights which may sum to below 100%.
   *
   * <p> There is a similar overload that takes in {@link DoubleMap}, where the concept of summing to 100% makes more sense.
   * Here, since we're dealing with {@link PreciseValue}, it's a bit meaningless to care about 1 vs. not 1 if the weights
   * are of type e.g. Money. But for certain other types ({@link UnitFraction}, {@link SignedFraction}, etc.)
   * it does make sense. However, here's no way to distinguish that given how generics work in Java.
   * So this is reasonable. </p>
   */
  public static <K, V extends PreciseValue<V>> Partition<K> partitionFromPositiveWeightsWhichMaySumToBelow1(
      RBMap<K, V> weightsMap, Epsilon epsilon) {
    BigDecimal sum = sumToBigDecimal(weightsMap.values());
    RBPreconditions.checkArgument(
        sum.signum() == 1,
        "Sum of weights must be >0. Input was %s",
        weightsMap);
    RBPreconditions.checkArgument(
        sum.doubleValue() <= 1 + epsilon.doubleValue(),
        "Sum of weights must be <= 1 + epsilon of %s ; was %s . Input was %s",
        sum, epsilon, weightsMap);

    return partitionFromPositiveWeights(weightsMap, sum);
  }

  /**
   * Creates a partition by normalizing a map of positive weights.
   */
  private static <K, V extends PreciseValue<V>> Partition<K> partitionFromPositiveWeights(
      RBMap<K, V> weightsMap, BigDecimal precomputedSum) {
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
      fractionsMap.putAssumingAbsent(key, unitFraction(bd.divide(precomputedSum, DEFAULT_MATH_CONTEXT)));
    });
    return new Partition<>(newRBMap(fractionsMap));
  }

  /**
   * Creates a partition by normalizing a map of positive weights which may not sum to 100%.
   */
  public static <K> Partition<K> partitionFromPositiveWeightsWhichMayNotSumTo1(DoubleMap<K> weightsMap) {
    // epsilon of 1e-12 is tighter than the usual 1e-8; note that doubles normally have precision of around 1e-14 to 1e-15.
    return partitionFromPositiveWeightsWhichMayNotSumTo1(weightsMap, epsilon(1e-12));
  }

  /**
   * Creates a partition by normalizing a map of positive weights which may not sum to 100%.
   */
  public static <K> Partition<K> partitionFromPositiveWeightsWhichMayNotSumTo1(
      DoubleMap<K> weightsMap, Epsilon epsilon) {
    double sum = weightsMap.sum();
    RBPreconditions.checkArgument(
        sum > epsilon.doubleValue(),
        "Sum of weights must be >0 (actually, %s ). Input was %s",
        epsilon, weightsMap);
    return partitionFromPositiveWeights(weightsMap, sum, epsilon);
  }


  /**
   * Creates a partition by normalizing a map of weights which may sum to below 100%, but not above.
   */
  public static <K> Partition<K> partitionFromPositiveWeightsWhichMaySumToBelow1(DoubleMap<K> weightsMap) {
    // epsilon of 1e-12 is tighter than the usual 1e-8; note that doubles normally have precision of around 1e-14 to 1e-15.
    return partitionFromPositiveWeightsWhichMaySumToBelow1(weightsMap, epsilon(1e-12));
  }

  /**
   * Creates a partition by normalizing a map of weights which may sum to below 100%, but not above.
   */
  public static <K> Partition<K> partitionFromPositiveWeightsWhichMaySumToBelow1(
      DoubleMap<K> weightsMap, Epsilon epsilon) {
    double sum = weightsMap.sum();
    RBPreconditions.checkArgument(
        sum > epsilon.doubleValue(),
        "Sum of weights must be >0 (actually, %s ). Input was %s",
        epsilon, weightsMap);
    // Adding epsilon here to avoid
    RBPreconditions.checkArgument(
        sum < 1 + epsilon.doubleValue(),
        "Sum of weights must be <= 1 (actually, 1 + %s ). Input was %s",
        epsilon, weightsMap);
    return partitionFromPositiveWeights(weightsMap, sum, epsilon);
  }

  private static <K> Partition<K> partitionFromPositiveWeights(
      DoubleMap<K> weightsMap, double precomputedSum, Epsilon epsilon) {
    MutableRBMap<K, UnitFraction> fractionsMap = newMutableRBMapWithExpectedSize(weightsMap.size());
    weightsMap.getRawMap().forEachEntry( (key, weight) -> {
      if (epsilon.isAlmostZero(weight)) {
        return;
      }
      RBPreconditions.checkArgument(
          weight > epsilon.doubleValue(),
          "Cannot create a PreciseValue weights partition if it includes a negative or zero weight of %s : input was %s",
          weight, weightsMap);
      fractionsMap.putAssumingAbsent(key, unitFraction(weight / precomputedSum));
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
      throw new IllegalArgumentException(smartFormat(
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
    return toStringInOrder(precision, comparingByValue(reverseOrder()), keyToObject);
  }

  public String toStringInOrder(
      int precision,
      Comparator<Entry<K, UnitFraction>> comparator,
      Function<K, String> keyToObject) {
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
