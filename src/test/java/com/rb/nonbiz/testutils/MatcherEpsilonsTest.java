package com.rb.nonbiz.testutils;

import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.types.Epsilon;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.ClassPlusStringKeyMatcherEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.ClassPlusStringKeyMatcherEpsilonDescriptor.epsilonId;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.ClassWideMatcherEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.MatcherEpsilonDescriptor.GetterSpecificMatcherEpsilonDescriptor.eps;
import static com.rb.nonbiz.testutils.MatcherEpsilons.matcherEpsilons;
import static com.rb.nonbiz.testutils.MatcherEpsilons.useEpsilonInAllMatchers;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.junit.Assert.assertEquals;

/**
 * Normally, our test matchers use 1e-8 (DEFAULT_EPSILON). However, sometimes we want to override the epsilons.
 * This helps you accomplish that.
 *
 * see OrdersTest#testEpsilonsInfra for how this gets used.
 */
public class MatcherEpsilonsTest {

  @Test
  public void testEpsilonsFunctionality() {
    class Class1 {};
    class Class1A {};
    class Class2 {};
    class Class2A {};
    class Class3 {};
    class Class4 {};
    class DummyClass {};

    MatcherEpsilons matcherEpsilons = matcherEpsilons(
        // Class1 has both class-wide and getter-specific epsilons
        eps(Class1.class),                epsilon(0.17),
        eps(Class1.class, Class1A.class), epsilon(0.18),

        // Class2 only has a getter-specific epsilon
        eps(Class2.class, Class2A.class), epsilon(0.28),

        // Class3 only has a class-wide epsilon
        eps(Class3.class),                epsilon(0.37),

        // Class4 only has a key that's accessible by a string epsilon ID
        eps(Class4.class, epsilonId("key_for_4")), epsilon(0.49));

    assertEquals(5, matcherEpsilons.size());

    // using a smaller epsilon than the usual 1e-8 here for double comparisons,
    // because the return value itself will sometimes be 1e-8, the default epsilon.
    Epsilon e = epsilon(1e-9);

    assertAlmostEquals(epsilon(0.17),        matcherEpsilons.get(Class1.class),                         e);
    assertAlmostEquals(epsilon(0.18),        matcherEpsilons.get(Class1.class, Class1A.class),          e);
    assertAlmostEquals(epsilon(0.17),        matcherEpsilons.get(Class1.class, DummyClass.class),       e);

    assertAlmostEquals(DEFAULT_EPSILON_1e_8, matcherEpsilons.get(Class2.class),                         e);
    assertAlmostEquals(epsilon(0.28),        matcherEpsilons.get(Class2.class, Class2A.class),          e);
    assertAlmostEquals(DEFAULT_EPSILON_1e_8, matcherEpsilons.get(Class2.class, DummyClass.class),       e);

    assertAlmostEquals(epsilon(0.37),        matcherEpsilons.get(Class3.class),                         e);
    assertAlmostEquals(epsilon(0.37),        matcherEpsilons.get(Class3.class, DummyClass.class),       e);

    assertAlmostEquals(epsilon(0.49),        matcherEpsilons.get(Class4.class, epsilonId("key_for_4")), e);
    assertAlmostEquals(DEFAULT_EPSILON_1e_8, matcherEpsilons.get(Class4.class, epsilonId("wrong_key")), e);

    assertAlmostEquals(DEFAULT_EPSILON_1e_8, matcherEpsilons.get(DummyClass.class),                     e);
  }

  @Test
  public void invalidDefaultEpsilon_throws() {
    class DummyClass {};
    MatcherEpsilonDescriptor<?> dummyEpsilonDesriptor = eps(DummyClass.class);
    RBSet.<Function<Epsilon, MatcherEpsilons>>rbSetOf(
            v -> useEpsilonInAllMatchers(v),
            v -> matcherEpsilons(Optional.of(epsilon(0.123)), singletonRBMap(dummyEpsilonDesriptor, v)),
            v -> matcherEpsilons(Optional.of(v),              singletonRBMap(dummyEpsilonDesriptor, v)),
            v -> matcherEpsilons(Optional.of(v),              singletonRBMap(dummyEpsilonDesriptor, epsilon(0.123))))
        .forEach(maker -> {
          MatcherEpsilons doesNotThrow;
          assertIllegalArgumentException(() -> maker.apply(epsilon(-1e-9)));
          doesNotThrow = maker.apply(epsilon(0));
          doesNotThrow = maker.apply(epsilon(1e-9));
          doesNotThrow = maker.apply(epsilon(99));
          assertIllegalArgumentException(() -> maker.apply(epsilon(101)));
        });
  }

  @Test
  public void getterSpecificEpsilonRequestedButNotDefined_defaultsToClassSpecificEpsilon() {
    class Class1 {};
    class Class1A {};
    class Class1B {};
    class Class2 {};

    // using a smaller epsilon than the usual 1e-8 here for double comparisons,
    // because the return value itself will sometimes be 1e-8, the default epsilon.
    Epsilon e = epsilon(1e-9);

    MatcherEpsilons matcherEpsilons = matcherEpsilons(
        Optional.of(epsilon(0.16)),
        rbMapOf(
            eps(Class1.class),                epsilon(0.17),
            eps(Class1.class, Class1A.class), epsilon(0.18)));

    // Class1A-getter-specific epsilon exists
    assertAlmostEquals(epsilon(0.18), matcherEpsilons.get(Class1.class, Class1A.class), e);

    // No Class1B-getter-specific epsilon exists; defaulting to classwide epsilon for Class1
    assertAlmostEquals(epsilon(0.17), matcherEpsilons.get(Class1.class, Class1B.class), e);
    assertAlmostEquals(epsilon(0.17), matcherEpsilons.get(Class1.class),                e);

    assertAlmostEquals(epsilon(0.16), matcherEpsilons.get(Class1A.class), e);
    assertAlmostEquals(epsilon(0.16), matcherEpsilons.get(Class1B.class), e);
    assertAlmostEquals(epsilon(0.16), matcherEpsilons.get(Class2.class), e);
  }

  @Test
  public void stringBasedEpsilonRequestedButNotDefined_defaultsToClassSpecificEpsilon() {
    class Class1 {};
    class Class2 {};

    // using a smaller epsilon than the usual 1e-8 here for double comparisons,
    // because the return value itself will sometimes be 1e-8, the default epsilon.
    Epsilon e = epsilon(1e-9);

    MatcherEpsilons matcherEpsilons = matcherEpsilons(
        Optional.of(epsilon(0.16)),
        rbMapOf(
            eps(Class1.class),                  epsilon(0.17),
            eps(Class1.class, epsilonId("1A")), epsilon(0.18)));

    // string-specific epsilon exists for string "1A"
    assertAlmostEquals(epsilon(0.18), matcherEpsilons.get(Class1.class, epsilonId("1A")), e);

    // No string-specific epsilon exists for string "1B"; defaulting to classwide epsilon for Class1
    assertAlmostEquals(epsilon(0.17), matcherEpsilons.get(Class1.class, epsilonId("1B")), e);
    assertAlmostEquals(epsilon(0.17), matcherEpsilons.get(Class1.class),                  e);

    assertAlmostEquals(epsilon(0.16), matcherEpsilons.get(Class2.class),                  e);
  }

}
