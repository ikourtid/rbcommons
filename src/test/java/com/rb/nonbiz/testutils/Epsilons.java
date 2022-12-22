package com.rb.nonbiz.testutils;

import com.rb.nonbiz.collections.DoubleMap;
import com.rb.nonbiz.testutils.EpsilonDescriptor.GeneralEpsilonDescriptor;
import com.rb.nonbiz.testutils.EpsilonDescriptor.GetterSpecificEpsilonDescriptor;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.OptionalDouble;

import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.DoubleMap.emptyDoubleMap;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstAndRest;
import static com.rb.nonbiz.testutils.EpsilonDescriptor.ClassWideEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.EpsilonDescriptor.GeneralEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.EpsilonDescriptor.GetterSpecificEpsilonDescriptor.eps;

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
  private final DoubleMap<EpsilonDescriptor<?>> epsilons;

  private Epsilons(
      OptionalDouble defaultEpsilonOverride,
      DoubleMap<EpsilonDescriptor<?>> epsilons) {
    this.defaultEpsilonOverride = defaultEpsilonOverride;
    this.epsilons = epsilons;
  }

  public static Epsilons emptyEpsilons() {
    return new Epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        emptyDoubleMap());
  }

  /**
   * Use this for cases where you want to use the same numeric epsilon everywhere in the matchers,
   * without needing to have an inclusion list of {@link EpsilonDescriptor}s that describe the exact places
   * where the epsilons get used.
   */
  public static Epsilons useEpsilonEverywhere(double epsilonToUseEverywhere) {
    RBPreconditions.checkArgument(epsilonToUseEverywhere >= 0);
    return new Epsilons(
        OptionalDouble.of(epsilonToUseEverywhere),
        emptyDoubleMap());
  }

  public static Epsilons epsilons(
      EpsilonDescriptor<?> epsilonDescriptor1, double epsilon1) {
    return new Epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(singletonRBMap(
            epsilonDescriptor1, epsilon1)));
  }

  public static Epsilons epsilons(
      EpsilonDescriptor<?> epsilonDescriptor1, double epsilon1,
      EpsilonDescriptor<?> epsilonDescriptor2, double epsilon2) {
    return new Epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapOf(
            epsilonDescriptor1, epsilon1,
            epsilonDescriptor2, epsilon2)));
  }

  public static Epsilons epsilons(
      EpsilonDescriptor<?> epsilonDescriptor1, double epsilon1,
      EpsilonDescriptor<?> epsilonDescriptor2, double epsilon2,
      EpsilonDescriptor<?> epsilonDescriptor3, double epsilon3) {
    return new Epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapOf(
            epsilonDescriptor1, epsilon1,
            epsilonDescriptor2, epsilon2,
            epsilonDescriptor3, epsilon3)));
  }

  public static Epsilons epsilons(
      EpsilonDescriptor<?> epsilonDescriptor1, double epsilon1,
      EpsilonDescriptor<?> epsilonDescriptor2, double epsilon2,
      EpsilonDescriptor<?> epsilonDescriptor3, double epsilon3,
      EpsilonDescriptor<?> epsilonDescriptor4, double epsilon4) {
    return new Epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapOf(
            epsilonDescriptor1, epsilon1,
            epsilonDescriptor2, epsilon2,
            epsilonDescriptor3, epsilon3,
            epsilonDescriptor4, epsilon4)));
  }

  public static Epsilons epsilons(
      EpsilonDescriptor<?> epsilonDescriptor1, double epsilon1,
      EpsilonDescriptor<?> epsilonDescriptor2, double epsilon2,
      EpsilonDescriptor<?> epsilonDescriptor3, double epsilon3,
      EpsilonDescriptor<?> epsilonDescriptor4, double epsilon4,
      EpsilonDescriptor<?> epsilonDescriptor5, double epsilon5) {
    return new Epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapOf(
            epsilonDescriptor1, epsilon1,
            epsilonDescriptor2, epsilon2,
            epsilonDescriptor3, epsilon3,
            epsilonDescriptor4, epsilon4,
            epsilonDescriptor5, epsilon5)));
  }

  public static Epsilons epsilons(
      EpsilonDescriptor<?> epsilonDescriptor1, double epsilon1,
      EpsilonDescriptor<?> epsilonDescriptor2, double epsilon2,
      EpsilonDescriptor<?> epsilonDescriptor3, double epsilon3,
      EpsilonDescriptor<?> epsilonDescriptor4, double epsilon4,
      EpsilonDescriptor<?> epsilonDescriptor5, double epsilon5,
      EpsilonDescriptor<?> epsilonDescriptor6, double epsilon6) {
    return new Epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapOf(
            epsilonDescriptor1, epsilon1,
            epsilonDescriptor2, epsilon2,
            epsilonDescriptor3, epsilon3,
            epsilonDescriptor4, epsilon4,
            epsilonDescriptor5, epsilon5,
            epsilonDescriptor6, epsilon6)));
  }

  /**
   * Use this in situations where you want to use the same numeric value (e.g. 1e-7) for all epsilons
   * in the contexts specified by the {@link EpsilonDescriptor}s passed in.
   */
  public static Epsilons sharedEpsilons(
      double epsilon,
      EpsilonDescriptor<?> first,
      EpsilonDescriptor<?> ... rest) {
    return new Epsilons(
        NO_DEFAULT_EPSILON_OVERRIDE,
        doubleMap(rbMapFromStream(
            concatenateFirstAndRest(first, rest),
            v -> v,
            v -> epsilon)));
  }

  public int size() {
    return epsilons.size();
  }

  /**
   * See ClassWideEpsilonDescriptor
   */
  public double get(Class<?> clazz) {
    return epsilons.getRawMap().getOrDefault(eps(clazz), getDefaultEpsilon());
  }

  /**
   * @see GetterSpecificEpsilonDescriptor
   */
  public double get(Class<?> clazz, Class<?> getterReturnType) {
    return epsilons.getRawMap().getOrDefault(eps(clazz, getterReturnType), getDefaultEpsilon());
  }

  /**
   * Use this for cases when you want to have a need for more than one epsilon per test matcher class,
   * and you want to distinguish between them. This is similar to situations with jmock where there is more than one
   * mock object of the same class; we do the same thing there.
   *
   * @see GeneralEpsilonDescriptor
   */
  public double get(Class<?> clazz, String suffix) {
    return epsilons.getRawMap().getOrDefault(eps(clazz, suffix), getDefaultEpsilon());
  }

  private double getDefaultEpsilon() {
    return defaultEpsilonOverride.orElse(DEFAULT_EPSILON);
  }

}
