package com.rb.nonbiz.testutils;

import com.rb.nonbiz.collections.DoubleMap;
import com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.GeneralMatcherEpsilonDescriptor;
import com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.GetterSpecificMatcherEpsilonDescriptor;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.OptionalDouble;

import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.DoubleMap.emptyDoubleMap;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstAndRest;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.ClassWideMatcherEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.GeneralMatcherEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.GetterSpecificMatcherEpsilonDescriptor.eps;

/**
 * Normally, our test matchers use 1e-8 (DEFAULT_EPSILON). However, sometimes we want to override the epsilons.
 * This helps you accomplish that.
 *
 * see OrdersTest#testEpsilonsInfra for how this gets used. Also, {@link EpsilonsTest}.
 */
public class Epsilons {

  private static final double DEFAULT_EPSILON = 1e-8;
  private static final OptionalDouble NO_DEFAULT_EPSILON_OVERRIDE = OptionalDouble.empty();

  private final OptionalDouble defaultEpsilonOverride;
  private final DoubleMap<MatcherEpsilonDescriptor<?>> matcherEpsilonDescriptors;

  private Epsilons(
      OptionalDouble defaultEpsilonOverride,
      DoubleMap<MatcherEpsilonDescriptor<?>> matcherEpsilonDescriptors) {
    this.defaultEpsilonOverride = defaultEpsilonOverride;
    this.matcherEpsilonDescriptors = matcherEpsilonDescriptors;
  }

  public static Epsilons epsilons(
      OptionalDouble defaultEpsilonOverride,
      DoubleMap<MatcherEpsilonDescriptor<?>> epsilonsMap) {
    // Ideally, we'd be using something more typesafe than a double to denote an epsilon, which disallows invalid
    // epsilons. But unfortunately it's too late to do this by now (Dec 2022).
    // So let's just check here.
    defaultEpsilonOverride.ifPresent(v -> RBPreconditions.checkArgument(
        isValidEpsilon(v),
        "The default epsilon is invalid: %s",
        v));

    epsilonsMap.getRawMap().forEachEntry( (epsilonDescriptor, epsilon) ->
        RBPreconditions.checkArgument(
            isValidEpsilon(epsilon),
            "The epsilon with descriptor %s is invalid: %s",
            epsilonDescriptor, epsilon));

    return new Epsilons(defaultEpsilonOverride, epsilonsMap);
  }

  private static boolean isValidEpsilon(double epsilon) {
    // There's nothing special about 100, but it's rare to use an epsilon bigger than 1, let alone 100.
    // This is particularly useful for cases where one misspells e.g. 1e-6 to 1e6.
    return 0 <= epsilon && epsilon < 100;
  }

  public static Epsilons emptyEpsilons() {
    return epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        emptyDoubleMap());
  }

  /**
   * Use this for cases where you want to use the same numeric epsilon everywhere in the matchers,
   * without needing to have an inclusion list of {@link MatcherEpsilonDescriptor}s that describe the exact places
   * where the epsilons get used.
   */
  public static Epsilons useEpsilonEverywhere(double epsilonToUseEverywhere) {
    RBPreconditions.checkArgument(epsilonToUseEverywhere >= 0);
    return epsilons(
        OptionalDouble.of(epsilonToUseEverywhere),
        emptyDoubleMap());
  }

  public static Epsilons epsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, double epsilon1) {
    return epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(singletonRBMap(
            matcherEpsilonDescriptor1, epsilon1)));
  }

  public static Epsilons epsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, double epsilon1,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor2, double epsilon2) {
    return epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapOf(
            matcherEpsilonDescriptor1, epsilon1,
            matcherEpsilonDescriptor2, epsilon2)));
  }

  public static Epsilons epsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, double epsilon1,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor2, double epsilon2,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor3, double epsilon3) {
    return epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapOf(
            matcherEpsilonDescriptor1, epsilon1,
            matcherEpsilonDescriptor2, epsilon2,
            matcherEpsilonDescriptor3, epsilon3)));
  }

  public static Epsilons epsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, double epsilon1,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor2, double epsilon2,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor3, double epsilon3,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor4, double epsilon4) {
    return epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapOf(
            matcherEpsilonDescriptor1, epsilon1,
            matcherEpsilonDescriptor2, epsilon2,
            matcherEpsilonDescriptor3, epsilon3,
            matcherEpsilonDescriptor4, epsilon4)));
  }

  public static Epsilons epsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, double epsilon1,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor2, double epsilon2,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor3, double epsilon3,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor4, double epsilon4,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor5, double epsilon5) {
    return epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapOf(
            matcherEpsilonDescriptor1, epsilon1,
            matcherEpsilonDescriptor2, epsilon2,
            matcherEpsilonDescriptor3, epsilon3,
            matcherEpsilonDescriptor4, epsilon4,
            matcherEpsilonDescriptor5, epsilon5)));
  }

  public static Epsilons epsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, double epsilon1,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor2, double epsilon2,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor3, double epsilon3,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor4, double epsilon4,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor5, double epsilon5,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor6, double epsilon6) {
    return epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapOf(
            matcherEpsilonDescriptor1, epsilon1,
            matcherEpsilonDescriptor2, epsilon2,
            matcherEpsilonDescriptor3, epsilon3,
            matcherEpsilonDescriptor4, epsilon4,
            matcherEpsilonDescriptor5, epsilon5,
            matcherEpsilonDescriptor6, epsilon6)));
  }

  /**
   * Use this in situations where you want to use the same numeric value (e.g. 1e-7) for all epsilons
   * in the contexts specified by the {@link MatcherEpsilonDescriptor}s passed in.
   */
  public static Epsilons sharedEpsilons(
      double epsilon,
      MatcherEpsilonDescriptor<?> first,
      MatcherEpsilonDescriptor<?>... rest) {
    return epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapFromStream(
            concatenateFirstAndRest(first, rest),
            v -> v,
            v -> epsilon)));
  }

  public int size() {
    return matcherEpsilonDescriptors.size();
  }

  /**
   * See ClassWideEpsilonDescriptor
   */
  public double get(Class<?> clazz) {
    return matcherEpsilonDescriptors.getRawMap().getOrDefault(eps(clazz), getDefaultEpsilon());
  }

  /**
   * @see GetterSpecificMatcherEpsilonDescriptor
   */
  public double get(Class<?> clazz, Class<?> getterReturnType) {
    return matcherEpsilonDescriptors.getRawMap().getOrDefault(eps(clazz, getterReturnType), getDefaultEpsilon());
  }

  /**
   * Use this for cases when you want to have a need for more than one epsilon per test matcher class,
   * and you want to distinguish between them. This is similar to situations with jmock where there is more than one
   * mock object of the same class; we do the same thing there.
   *
   * @see GeneralMatcherEpsilonDescriptor
   */
  public double get(Class<?> clazz, String suffix) {
    return matcherEpsilonDescriptors.getRawMap().getOrDefault(eps(clazz, suffix), getDefaultEpsilon());
  }

  private double getDefaultEpsilon() {
    return defaultEpsilonOverride.orElse(DEFAULT_EPSILON);
  }

}
