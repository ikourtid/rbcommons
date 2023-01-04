package com.rb.nonbiz.collections;


import com.google.common.base.Joiner;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.SignedFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static com.rb.nonbiz.collections.Deviations.deviations;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBStreams.sumBigDecimals;
import static com.rb.nonbiz.types.Epsilon.epsilon;

/**
 * Just like Deviations, except that this explicitly disallows items with a 0 fraction.
 *
 * You can decide whether to use this (vs plain Deviations) depending on whether you care about recording information
 * about missing items. For example, say you want to store the differences between the ideal and the actual
 * partition. If you care about having entries for items that have equal fractions in both partitions (and therefore
 * have a 0 deviation), then use plain Deviations. Use this when you want to explicitly disallow 0 deviations.
 */
public class NonZeroDeviations<K> {

  private final RBMap<K, SignedFraction> signedFractions;

  private NonZeroDeviations(RBMap<K, SignedFraction> signedFractions) {
    this.signedFractions = signedFractions;
  }

  public static <K> NonZeroDeviations<K> nonZeroDeviations(RBMap<K, SignedFraction> signedFractions) {
    SignedFraction sumOfSignedFractions = SignedFraction.sum(signedFractions.values());
    RBPreconditions.checkArgument(
        sumOfSignedFractions.isAlmostZero(epsilon(1e-7)), // 1e-8 doesn't work always
        "Signed fractions for NonZeroDeviations must add to 0 but they sum to %s. Input was %s",
        sumOfSignedFractions.toPercentString(10), signedFractions);
    RBPreconditions.checkArgument(
        signedFractions.values().stream().noneMatch(f -> f.isZero()),
        "No signed fraction for NonZeroDeviations can be 0. Input was %s",
        signedFractions);
    return new NonZeroDeviations<>(signedFractions);
  }

  /**
   * Unlike Partition and SignedPartition, where you can't have 0 items summing to 1,
   * an empty NonZeroDeviations object does make sense. First, the "weights of 0 items" sum to 0 (trivially).
   * However, this also makes sense intuitively since NonZeroDeviations can be used e.g. to record the differences
   * between 2 partitions: if there is no difference, the deviations object will be empty.
   */
  public static <K> NonZeroDeviations<K> emptyNonZeroDeviations() {
    return new NonZeroDeviations<>(emptyRBMap());
  }

  public RBMap<K, SignedFraction> getRawSignedFractionsMap() {
    return signedFractions;
  }

  public BigDecimal getMeanAbsoluteDeviation() {
    return getMeanDeviationHelper(sf -> sf.asBigDecimal().abs());
  }

  public BigDecimal getMeanSquaredDeviation() {
    return getMeanDeviationHelper(sf -> sf.asBigDecimal().multiply(sf.asBigDecimal(), DEFAULT_MATH_CONTEXT));
  }

  private BigDecimal getMeanDeviationHelper(Function<SignedFraction, BigDecimal> deviationGetter) {
    return isEmpty()
        ? BigDecimal.ZERO
        : sumBigDecimals(
        signedFractions.values()
            .stream()
            .map(deviationGetter))
        .divide(BigDecimal.valueOf(signedFractions.size()), DEFAULT_MATH_CONTEXT);
  }

  public Deviations<K> toDeviations() {
    return deviations(signedFractions);
  }

  @Override
  public String toString() {
    return toString(0, key -> key.toString());
  }

  public String toString(int precision) {
    return toString(precision, key -> key.toString());
  }

  public String toString(int precision, Function<K, ? extends Object> keyToObject) {
    List<String> components = signedFractions.entrySet()
        .stream()
        .sorted((e1, e2) -> -1 * e1.getValue().compareTo(e2.getValue()))
        .map(e -> String.format("%s %s", e.getValue().toPercentString(precision), keyToObject.apply(e.getKey())))
        .collect(Collectors.toList());
    return Strings.format("MAD= %s ; dev²= %s ; %s",
        getMeanAbsoluteDeviation(),
        getMeanSquaredDeviation(),
        Joiner.on(" ; ").join(components));
  }

  public boolean isEmpty() {
    return signedFractions.isEmpty();
  }

}
