package com.rb.nonbiz.testutils;

import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.testutils.EpsilonDescriptor.GeneralEpsilonDescriptor;
import org.junit.Test;

import java.util.OptionalDouble;
import java.util.function.DoubleFunction;

import static com.rb.nonbiz.collections.DoubleMap.singletonDoubleMap;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.EpsilonDescriptor.ClassWideEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.EpsilonDescriptor.GetterSpecificEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.Epsilons.epsilons;
import static com.rb.nonbiz.testutils.Epsilons.useEpsilonEverywhere;
import static org.junit.Assert.assertEquals;

/**
 * Normally, our test matchers use 1e-8 (DEFAULT_EPSILON). However, sometimes we want to override the epsilons.
 * This helps you accomplish that.
 *
 * see OrdersTest#testEpsilonsInfra for how this gets used.
 */
public class EpsilonsTest {

  @Test
  public void testEpsilonsFunctionality() {
    class Class1 {};
    class Class1A {};
    class Class2 {};
    class Class2A {};
    class Class3 {};
    class Class4 {};
    class DummyClass {};

    Epsilons epsilons = epsilons(
        // Class1 has both class-wide and getter-specific epsilons
        eps(Class1.class), 0.17,
        eps(Class1.class, Class1A.class), 0.18,

        // Class2 only has a getter-specific epsilon
        eps(Class2.class, Class2A.class), 0.28,

        // Class3 only has a class-wide epsilon
        eps(Class3.class), 0.37,

        // Class4 only has a key that's accessible by a string 'path'
        GeneralEpsilonDescriptor.eps(Class4.class, "key_for_4"), 0.49);

    assertEquals(5, epsilons.size());

    // using a smaller epsilon than the usual 1e-8 here for double comparisons,
    // because the return value itself will sometimes be 1e-8, the default epsilon.
    double e = 1e-9;

    assertEquals(0.17, epsilons.get(Class1.class),                   e);
    assertEquals(0.18, epsilons.get(Class1.class, Class1A.class),    e);
    assertEquals(1e-8, epsilons.get(Class1.class, DummyClass.class), e);

    assertEquals(1e-8, epsilons.get(Class2.class),                   e);
    assertEquals(0.28, epsilons.get(Class2.class, Class2A.class),    e);
    assertEquals(1e-8, epsilons.get(Class2.class, DummyClass.class), e);

    assertEquals(0.37, epsilons.get(Class3.class),                   e);
    assertEquals(1e-8, epsilons.get(Class3.class, DummyClass.class), e);

    assertEquals(0.49, epsilons.get(Class4.class, "key_for_4"),      e);
    assertEquals(1e-8, epsilons.get(Class4.class, "wrong_key"),      e);

    assertEquals(1e-8, epsilons.get(DummyClass.class),               e);
  }

  @Test
  public void invalidDefaultEpsilon_throws() {
    class DummyClass {};
    EpsilonDescriptor<?> dummyEpsilonDesriptor = eps(DummyClass.class);
    RBSet.<DoubleFunction<Epsilons>>rbSetOf(
            v -> useEpsilonEverywhere(v),
            v -> epsilons(OptionalDouble.of(0.123), singletonDoubleMap(dummyEpsilonDesriptor, v)),
            v -> epsilons(OptionalDouble.of(v),     singletonDoubleMap(dummyEpsilonDesriptor, v)),
            v -> epsilons(OptionalDouble.of(v),     singletonDoubleMap(dummyEpsilonDesriptor, 0.123)))
        .forEach(maker -> {
          Epsilons doesNotThrow;
          assertIllegalArgumentException(() -> maker.apply(-1e-9));
          doesNotThrow = maker.apply(0);
          doesNotThrow = maker.apply(1e-9);
          doesNotThrow = maker.apply(99);
          assertIllegalArgumentException(() -> maker.apply(101));
        });
  }

}
