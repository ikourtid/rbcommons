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
import static com.rb.nonbiz.collections.NonZeroDeviations.nonZeroDeviations;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBStreams.sumBigDecimals;
import static java.math.BigDecimal.ROUND_HALF_EVEN;
import static java.util.Comparator.comparing;

/**
 * A collection of items with weights that sum to 0.
 *
 * In a way, this is similar to SignedPartition, except that the signedFractions must sum to 0, not 1.
 */
public class Deviations<K> {

  private final RBMap<K, SignedFraction> signedFractions;

  private Deviations(RBMap<K, SignedFraction> signedFractions) {
    this.signedFractions = signedFractions;
  }

  public static <K> Deviations<K> deviations(RBMap<K, SignedFraction> signedFractions) {
    SignedFraction sum = SignedFraction.sum(signedFractions.values());
    RBPreconditions.checkArgument(
        sum.isAlmostZero(1e-8),
        "Signed fractions for Deviations must add to 0, not %s ; input was %s",
        sum, signedFractions);
    return new Deviations<>(signedFractions);
  }

  /**
   * Unlike Partition and SignedPartition, where you can't have 0 items summing to 1,
   * an empty Deviations object does make sense. First, the "weights of 0 items" sum to 0 (trivially).
   * However, this also makes sense intuitively since Deviations can be used e.g. to record the differences
   * between 2 partitions: if there is no difference, the deviations object will be empty.
   */
  public static <K> Deviations<K> emptyDeviations() {
    return new Deviations<>(emptyRBMap());
  }

  public RBMap<K, SignedFraction> getRawSignedFractionsMap() {
    return signedFractions;
  }

  /**
   * This will throw if there is any item with a 0 recorded deviation.
   */
  public NonZeroDeviations<K> toNonZeroDeviationsOrThrow() {
    return nonZeroDeviations(signedFractions);
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

  public boolean isEmpty() {
    return signedFractions.isEmpty();
  }

  @Override
  public String toString() {
    return toString(4, key -> key.toString());
  }

  public String toString(int precision) {
    return toString(precision, key -> key.toString());
  }

  public String toString(int precision, Function<K, ? extends Object> keyToObject) {
    List<String> components = signedFractions.entrySet()
        .stream()
        .sorted(comparing(v -> v.getValue().asBigDecimal().abs()))
        .map(e -> String.format("%s %s", e.getValue().toPercentString(precision), keyToObject.apply(e.getKey())))
        .collect(Collectors.toList());
    return Strings.format("MAD_bps= %s ; %s",
        getMeanAbsoluteDeviation().multiply(BigDecimal.valueOf(10_000)).setScale(1, ROUND_HALF_EVEN),
        Joiner.on(" ; ").join(components));
  }

}
