package com.rb.nonbiz.testutils;

import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.ClassPlusStringKeyMatcherEpsilonDescriptor;
import com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.GetterSpecificMatcherEpsilonDescriptor;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.Epsilon;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstAndRest;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.ClassPlusStringKeyMatcherEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.ClassWideMatcherEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.GetterSpecificMatcherEpsilonDescriptor.eps;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

/**
 * Normally, our test matchers use 1e-8 (DEFAULT_EPSILON). However, sometimes we want to override the epsilons.
 * This helps you accomplish that.
 *
 * see OrdersTest#testEpsilonsInfra for how this gets used. Also, {@link MatcherEpsilonsTest}.
 */
public class MatcherEpsilons {

  private static final Optional<Epsilon> NO_DEFAULT_EPSILON_OVERRIDE = Optional.empty();

  private final Optional<Epsilon> defaultEpsilonOverride;
  private final RBMap<MatcherEpsilonDescriptor<?>, Epsilon> matcherEpsilonDescriptors;

  private MatcherEpsilons(
      Optional<Epsilon> defaultEpsilonOverride,
      RBMap<MatcherEpsilonDescriptor<?>, Epsilon> matcherEpsilonDescriptors) {
    this.defaultEpsilonOverride = defaultEpsilonOverride;
    this.matcherEpsilonDescriptors = matcherEpsilonDescriptors;
  }

  public static MatcherEpsilons matcherEpsilons(
      Optional<Epsilon> defaultEpsilonOverride,
      RBMap<MatcherEpsilonDescriptor<?>, Epsilon> matcherEpsilonDescriptors) {
    return new MatcherEpsilons(defaultEpsilonOverride, matcherEpsilonDescriptors);
  }

  public static MatcherEpsilons emptyMatcherEpsilons() {
    return matcherEpsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        emptyRBMap());
  }

  /**
   * Use this for cases where you want to use the same numeric epsilon everywhere in the matchers,
   * without needing to have an inclusion list of {@link MatcherEpsilonDescriptor}s that describe the exact places
   * where the epsilons get used.
   */
  public static MatcherEpsilons useEpsilonInAllMatchers(Epsilon epsilonToUseEverywhere) {
    return matcherEpsilons(
        Optional.of(epsilonToUseEverywhere),
        emptyRBMap());
  }

  public static MatcherEpsilons matcherEpsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, Epsilon epsilon1) {
    return matcherEpsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        singletonRBMap(
            matcherEpsilonDescriptor1, epsilon1));
  }

  public static MatcherEpsilons matcherEpsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, Epsilon epsilon1,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor2, Epsilon epsilon2) {
    return matcherEpsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        rbMapOf(
            matcherEpsilonDescriptor1, epsilon1,
            matcherEpsilonDescriptor2, epsilon2));
  }

  public static MatcherEpsilons matcherEpsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, Epsilon epsilon1,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor2, Epsilon epsilon2,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor3, Epsilon epsilon3) {
    return matcherEpsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        rbMapOf(
            matcherEpsilonDescriptor1, epsilon1,
            matcherEpsilonDescriptor2, epsilon2,
            matcherEpsilonDescriptor3, epsilon3));
  }

  public static MatcherEpsilons matcherEpsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, Epsilon epsilon1,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor2, Epsilon epsilon2,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor3, Epsilon epsilon3,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor4, Epsilon epsilon4) {
    return matcherEpsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        rbMapOf(
            matcherEpsilonDescriptor1, epsilon1,
            matcherEpsilonDescriptor2, epsilon2,
            matcherEpsilonDescriptor3, epsilon3,
            matcherEpsilonDescriptor4, epsilon4));
  }

  public static MatcherEpsilons matcherEpsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, Epsilon epsilon1,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor2, Epsilon epsilon2,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor3, Epsilon epsilon3,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor4, Epsilon epsilon4,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor5, Epsilon epsilon5) {
    return matcherEpsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        rbMapOf(
            matcherEpsilonDescriptor1, epsilon1,
            matcherEpsilonDescriptor2, epsilon2,
            matcherEpsilonDescriptor3, epsilon3,
            matcherEpsilonDescriptor4, epsilon4,
            matcherEpsilonDescriptor5, epsilon5));
  }

  public static MatcherEpsilons matcherEpsilons(
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor1, Epsilon epsilon1,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor2, Epsilon epsilon2,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor3, Epsilon epsilon3,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor4, Epsilon epsilon4,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor5, Epsilon epsilon5,
      MatcherEpsilonDescriptor<?> matcherEpsilonDescriptor6, Epsilon epsilon6) {
    return matcherEpsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        rbMapOf(
            matcherEpsilonDescriptor1, epsilon1,
            matcherEpsilonDescriptor2, epsilon2,
            matcherEpsilonDescriptor3, epsilon3,
            matcherEpsilonDescriptor4, epsilon4,
            matcherEpsilonDescriptor5, epsilon5,
            matcherEpsilonDescriptor6, epsilon6));
  }

  /**
   * Use this in situations where you want to use the same numeric value (e.g. 1e-7) for all epsilons
   * in the contexts specified by the {@link MatcherEpsilonDescriptor}s passed in.
   */
  public static MatcherEpsilons sharedMatcherEpsilons(
      Epsilon epsilon,
      MatcherEpsilonDescriptor<?> first,
      MatcherEpsilonDescriptor<?>... rest) {
    return matcherEpsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        rbMapFromStream(
            concatenateFirstAndRest(first, rest),
            v -> v,
            v -> epsilon));
  }

  public int size() {
    return matcherEpsilonDescriptors.size();
  }

  /**
   * See ClassWideEpsilonDescriptor
   */
  public Epsilon get(Class<?> clazz) {
    return matcherEpsilonDescriptors.getOrDefault(eps(clazz), getDefaultEpsilon());
  }

  /**
   * @see GetterSpecificMatcherEpsilonDescriptor
   */
  public Epsilon get(Class<?> clazz, Class<?> getterReturnType) {
    return matcherEpsilonDescriptors.getOrDefault(
        eps(clazz, getterReturnType),
        matcherEpsilonDescriptors.getOrDefault(
            eps(clazz),
            getDefaultEpsilon()));
  }

  /**
   * Use this for cases when you want to have a need for more than one epsilon per test matcher class,
   * and you want to distinguish between them. This is similar to situations with jmock where there is more than one
   * mock object of the same class; we do the same thing there.
   *
   * @see ClassPlusStringKeyMatcherEpsilonDescriptor
   */
  public Epsilon get(Class<?> clazz, UniqueId<Epsilon> epsilonId) {
    return matcherEpsilonDescriptors.getOrDefault(
        eps(clazz, epsilonId),
        matcherEpsilonDescriptors.getOrDefault(
            eps(clazz),
            getDefaultEpsilon()));
  }

  private Epsilon getDefaultEpsilon() {
    return defaultEpsilonOverride.orElse(DEFAULT_EPSILON_1e_8);
  }

}
