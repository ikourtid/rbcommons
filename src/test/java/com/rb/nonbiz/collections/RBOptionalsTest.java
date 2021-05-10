package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.investing.strategy.optbased.GlobalObjective;
import com.rb.biz.investing.strategy.optbased.rebal.lp.NormalizedObjectiveValue;
import com.rb.biz.types.Money;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Pointer;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.biz.investing.strategy.optbased.rebal.lp.NormalizedObjectiveValue.normalizedGlobalObjectiveValue;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptionalDouble;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptionalImpreciseValueToDoubleOrZero;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptionalImpreciseValueToOptionalDouble;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptionalPreciseValueToDoubleOrZero;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptionalPreciseValueToOptionalDouble;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformPairOfOptionalDoubles;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformPairOfOptionalInts;
import static com.rb.nonbiz.collections.RBOptionals.*;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalIntEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalIntEquals;
import static com.rb.nonbiz.types.Pointer.initializedPointer;
import static com.rb.nonbiz.types.Pointer.uninitializedPointer;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class RBOptionalsTest {

  @Test
  public void testGetOrThrow() {
    assertIllegalArgumentException( () -> getOrThrow(Optional.empty(), "Optional is empty"));
    assertEquals("ABC", getOrThrow(Optional.of("ABC"), "Optional is empty"));
  }

  @Test
  public void testGetIntOrThrow() {
    assertIllegalArgumentException( () -> getIntOrThrow(OptionalInt.empty(), "Optional is empty"));
    assertEquals(123, getIntOrThrow(OptionalInt.of(123), "Optional is empty"));
  }

  @Test
  public void testOptionalsEqual() {
    assertTrue(optionalsEqual(Optional.empty(), Optional.empty()));
    assertTrue(optionalsEqual(Optional.of("a"), Optional.of("a")));
    assertFalse(optionalsEqual(Optional.of("a"), Optional.of("b")));
    assertFalse(optionalsEqual(Optional.of("a"), Optional.empty()));
    assertFalse(optionalsEqual(Optional.empty(), Optional.of("b")));
    assertFalse(optionalsEqual(Optional.of("a"), Optional.of(123)));
  }

  @Test
  public void testToListOfOptionals() {
    assertThat(
        toListOfOptionals(emptyList()),
        orderedListMatcher(
            emptyList(),
            s -> typeSafeEqualTo(s)));
    assertThat(
        toListOfOptionals(ImmutableList.of(11, 22)),
        orderedListMatcher(
            ImmutableList.of(Optional.of(11), Optional.of(22)),
            oi -> typeSafeEqualTo(oi)));
    assertThat(
        toListOfOptionals(ImmutableList.of("a", "b")),
        orderedListMatcher(
            ImmutableList.of(Optional.of("a"), Optional.of("b")),
            s -> typeSafeEqualTo(s)));
  }

  @Test
  public void testOptionalDoubleOfNullable() {
    assertFalse(optionalDoubleOfNullable(null).isPresent());
    assertTrue( optionalDoubleOfNullable(1.23).isPresent());
    assertEquals(4.56, optionalDoubleOfNullable(4.56).getAsDouble(), 1e-8);
  }

  @Test
  public void testToSpecializedOptionalDouble() {
    assertFalse(toSpecializedOptionalDouble(Optional.empty()).isPresent());
    assertTrue( toSpecializedOptionalDouble(Optional.of(1.23)).isPresent());
    assertEquals(4.56, toSpecializedOptionalDouble(Optional.of(4.56)).getAsDouble(), 1e-8);
  }

  @Test
  public void testOptionalIntOfNullable() {
    assertOptionalIntEmpty(optionalIntOfNullable(null));
    assertTrue(optionalIntOfNullable(123).isPresent());
    assertOptionalIntEquals(456, optionalIntOfNullable(456));
  }

  @Test
  public void testToSpecializedOptionalInt() {
    assertFalse(toSpecializedOptionalInt(Optional.empty()).isPresent());
    assertTrue( toSpecializedOptionalInt(Optional.of(123)).isPresent());
    assertEquals(456, toSpecializedOptionalInt(Optional.of(456)).getAsInt());
  }


  @Test
  public void lazyGetIfPresent_emptyInputs_returnsEmptyList() {
    assertOptionalEquals(emptyList(), lazyGetIfPresent(emptyList()));
    assertOptionalEquals(emptyList(), lazyGetIfPresent());
  }

  @Test
  public void lazyGetIfPresent_multipleInputs_returnsListOnlyIfAllPresent_otherwiseEmptyOptional() {
    Supplier<Optional<Integer>> supplier1 = () -> Optional.of(1);
    Supplier<Optional<Integer>> supplier2 = () -> Optional.of(2);
    Supplier<Optional<Integer>> supplier3 = () -> Optional.of(3);
    Supplier<Optional<Integer>> emptySupplier = () -> Optional.empty();
    Supplier<Optional<Integer>> unreachableSupplier = () -> {
      throw new IllegalArgumentException();
    };

    assertOptionalEquals(ImmutableList.of(1, 2, 3), lazyGetIfPresent(                 supplier1, supplier2, supplier3));
    assertOptionalEquals(ImmutableList.of(1, 2, 3), lazyGetIfPresent(ImmutableList.of(supplier1, supplier2, supplier3)));

    assertOptionalEmpty(lazyGetIfPresent(                 supplier1, supplier2, emptySupplier));
    assertOptionalEmpty(lazyGetIfPresent(ImmutableList.of(supplier1, supplier2, emptySupplier)));

    assertOptionalEmpty(lazyGetIfPresent(                 supplier1, emptySupplier, unreachableSupplier));
    assertOptionalEmpty(lazyGetIfPresent(ImmutableList.of(supplier1, emptySupplier, unreachableSupplier)));

    assertOptionalEmpty(lazyGetIfPresent(                 emptySupplier, unreachableSupplier, unreachableSupplier));
    assertOptionalEmpty(lazyGetIfPresent(ImmutableList.of(emptySupplier, unreachableSupplier, unreachableSupplier)));

    assertOptionalEmpty(lazyGetIfPresent(                          emptySupplier));
    assertOptionalEmpty(lazyGetIfPresent(singletonList(emptySupplier)));

    assertOptionalEquals(singletonList(1), lazyGetIfPresent(                          supplier1));
    assertOptionalEquals(singletonList(1), lazyGetIfPresent(singletonList(supplier1)));
  }

  @Test
  public void testLazyGetPairIfPresent() {
    assertOptionalEquals(
        pairOfSameType("a", "b"),
        lazyGetPairIfPresent("a", "b", str -> Optional.ofNullable(str)));
    assertOptionalEmpty(lazyGetPairIfPresent(null, "b", str -> Optional.ofNullable(str)));
    assertOptionalEmpty(lazyGetPairIfPresent("a", null, str -> Optional.ofNullable(str)));
    assertOptionalEmpty(lazyGetPairIfPresent(null, null, str -> Optional.ofNullable(str)));
  }

  @Test
  public void testFilterPresentOptionals() {
    assertEmpty(filterPresentOptionals(emptyList()));
    assertFalse(filterPresentOptionals(emptyIterator()).hasNext());

    assertEquals(
        ImmutableList.of("a", "b"),
        filterPresentOptionals(Optional.of("a"), Optional.empty(), Optional.of("b")));
    assertThat(
        filterPresentOptionals(Optional.of("a"), Optional.empty(), Optional.of("b")).iterator(),
        iteratorEqualityMatcher(ImmutableList.of("a", "b").iterator()));
    assertEquals(
        ImmutableList.of("a", "b"),
        filterPresentOptionals(ImmutableList.of(Optional.of("a"), Optional.empty(), Optional.of("b"))));
    assertThat(
        filterPresentOptionals(ImmutableList.of(Optional.of("a"), Optional.empty(), Optional.of("b"))).iterator(),
        iteratorEqualityMatcher(ImmutableList.of("a", "b").iterator()));
  }

  @Test
  public void testFilterPresentOptionalsInStream() {
    assertEquals(
        emptyList(),
        filterPresentOptionalsInStream(Collections.<Optional<String>>emptyList().stream())
            .collect(Collectors.toList()));

    assertEquals(
        ImmutableList.of("a", "b"),
        filterPresentOptionalsInStream(Stream.of(Optional.of("a"), Optional.empty(), Optional.of("b")))
            .collect(Collectors.toList()));
    assertEquals(
        ImmutableList.of("a", "b"),
        filterPresentOptionalsInStream(Stream.of(Optional.of("a"), Optional.of("b")))
            .collect(Collectors.toList()));
    assertEquals(
        emptyList(),
        filterPresentOptionalsInStream(Stream.of(Optional.empty(), Optional.empty(), Optional.empty()))
            .collect(Collectors.toList()));

  }

  @Test
  public void testFilterPresentOptionalDoubles() {
    assertEmpty(filterPresentOptionals(emptyList()));

    assertEquals(
        ImmutableList.of(1.1, 3.3),
        filterPresentOptionalDoubles(OptionalDouble.of(1.1), OptionalDouble.empty(), OptionalDouble.of(3.3)));
    assertEquals(
        ImmutableList.of(1.1, 3.3),
        filterPresentOptionalDoubles(ImmutableList.<OptionalDouble>of(OptionalDouble.of(1.1), OptionalDouble.empty(), OptionalDouble.of(3.3))));
  }

  @Test
  public void testVisitPairOfOptionals_overloadThatTransformsOnlyIfBothArePresent() {
    Pointer<String> value = uninitializedPointer();

    TriConsumer<String, Optional<String>, Optional<Integer>> asserter = (expectedResult, stringOptional, intOptional) -> {
      visitPairOfOptionals(
          stringOptional,
          intOptional,
          (s, i) -> value.set(Strings.format("%s_%s", s, i)),
          s -> value.set(Strings.format("%s_", s)),
          i -> value.set(Strings.format("_%s", i)),
          () -> value.set("neither"));
      assertEquals(expectedResult, value.getOrThrow());
    };

    asserter.accept("a_1",     Optional.of("a"), Optional.of(1));
    asserter.accept("a_",      Optional.of("a"), Optional.empty());
    asserter.accept("_1",      Optional.empty(), Optional.of(1));
    asserter.accept("neither", Optional.empty(), Optional.empty());
  }

  @Test
  public void testTransformPairOfOptionalDoubles() {
    BiFunction<Double, Double, Double> transformer = (v1, v2) -> v1 * 1_000 + v2;
    assertFalse(transformPairOfOptionalDoubles(OptionalDouble.empty(), OptionalDouble.empty(), transformer).isPresent());
    assertFalse(transformPairOfOptionalDoubles(OptionalDouble.empty(), OptionalDouble.of(3.4), transformer).isPresent());
    assertFalse(transformPairOfOptionalDoubles(OptionalDouble.of(1.2), OptionalDouble.empty(), transformer).isPresent());
    assertEquals(
        1_203.4,
        transformPairOfOptionalDoubles(OptionalDouble.of(1.2), OptionalDouble.of(3.4), transformer).getAsDouble(),
        1e-8);
  }

  @Test
  public void testTransformPairOfOptionalInts() {
    BinaryOperator<Integer> transformer = (v1, v2) -> v1 * 1_000 + v2;
    assertFalse(transformPairOfOptionalInts(OptionalInt.empty(), OptionalInt.empty(), transformer).isPresent());
    assertFalse(transformPairOfOptionalInts(OptionalInt.empty(), OptionalInt.of(34), transformer).isPresent());
    assertFalse(transformPairOfOptionalInts(OptionalInt.of(12), OptionalInt.empty(), transformer).isPresent());
    assertEquals(
        12_034,
        transformPairOfOptionalInts(OptionalInt.of(12), OptionalInt.of(34), transformer).getAsInt());
  }

  @Test
  public void testDoubleSpecificTransformers() {
    assertEquals("12.3", transformOptionalDouble(OptionalDouble.of(12.3), v -> Strings.format("%s", v)).get());
    assertFalse(transformOptionalDouble(OptionalDouble.empty(), v -> Strings.format("%s", v)).isPresent());

    assertEquals(OptionalDouble.of(45.6), transformOptionalPreciseValueToOptionalDouble(Optional.of(money(45.6))));
    assertEquals(OptionalDouble.empty(),  transformOptionalPreciseValueToOptionalDouble(Optional.<Money>empty()));

    assertEquals(OptionalDouble.of(78.9), transformOptionalImpreciseValueToOptionalDouble(Optional.of(normalizedGlobalObjectiveValue(78.9))));
    assertEquals(OptionalDouble.empty(),  transformOptionalImpreciseValueToOptionalDouble(Optional.<NormalizedObjectiveValue<GlobalObjective>>empty()));

    assertEquals(45.6, transformOptionalPreciseValueToDoubleOrZero(Optional.of(money(45.6))), 1e-8);
    assertEquals(0.0,  transformOptionalPreciseValueToDoubleOrZero(Optional.<Money>empty()), 1e-8);

    assertEquals(78.9, transformOptionalImpreciseValueToDoubleOrZero(Optional.of(normalizedGlobalObjectiveValue(78.9))), 1e-8);
    assertEquals(0.0,  transformOptionalImpreciseValueToDoubleOrZero(Optional.<NormalizedObjectiveValue<GlobalObjective>>empty()), 1e-8);
  }

  /**
   * I suppose you can just read the documentation on Optional#orElse vs Optional#orElseGet,
   * but this makes the point more explicitly.
   */
  @Test
  public void orElse_vs_orElseGet_onlySecondShortCircuits() {
    Pointer<Boolean> shortCircuits = initializedPointer(true);
    Supplier<Integer> intSupplier = () -> {
      shortCircuits.set(false);
      return 456;
    };

    assertEquals(123, (int) Optional.of(123).orElseGet(intSupplier));
    assertTrue(shortCircuits.getOrThrow());

    assertEquals(123, (int) Optional.of(123).orElse(intSupplier.get()));
    assertFalse(shortCircuits.getOrThrow());
  }

  @Test
  public void testIfBothOptional_generalVersion() {
    Pointer<String> pointer = uninitializedPointer();

    BiConsumer<Optional<String>, Optional<String>> biConsumer = (s1, s2) ->
        ifBothPresent(s1, s2, (v1, v2) -> pointer.set(Strings.format("%s_%s", v1, v2)));

    biConsumer.accept(Optional.empty(), Optional.empty());
    assertFalse(pointer.isInitialized());

    biConsumer.accept(Optional.of("a"), Optional.empty());
    assertFalse(pointer.isInitialized());

    biConsumer.accept(Optional.empty(), Optional.of("b"));
    assertFalse(pointer.isInitialized());

    biConsumer.accept(Optional.of("a"), Optional.of("b"));
    assertTrue(pointer.isInitialized());
    assertEquals("a_b", pointer.getOrThrow());
  }

  @Test
  public void testIfBothOptional_doubleVersion() {
    Pointer<String> pointer = uninitializedPointer();

    BiConsumer<OptionalDouble, OptionalDouble> biConsumer = (s1, s2) ->
        ifBothPresent(s1, s2, (v1, v2) -> pointer.set(Strings.format("%s_%s", v1, v2)));

    biConsumer.accept(OptionalDouble.empty(), OptionalDouble.empty());
    assertFalse(pointer.isInitialized());

    biConsumer.accept(OptionalDouble.of(1.1), OptionalDouble.empty());
    assertFalse(pointer.isInitialized());

    biConsumer.accept(OptionalDouble.empty(), OptionalDouble.of(2.2));
    assertFalse(pointer.isInitialized());

    biConsumer.accept(OptionalDouble.of(1.1), OptionalDouble.of(2.2));
    assertTrue(pointer.isInitialized());
    assertEquals("1.1_2.2", pointer.getOrThrow());
  }

  @Test
  public void testIfBothOptional_intVersion() {
    Pointer<String> pointer = uninitializedPointer();

    BiConsumer<OptionalInt, OptionalInt> biConsumer = (s1, s2) ->
        ifBothPresent(s1, s2, (v1, v2) -> pointer.set(Strings.format("%s_%s", v1, v2)));

    biConsumer.accept(OptionalInt.empty(), OptionalInt.empty());
    assertFalse(pointer.isInitialized());

    biConsumer.accept(OptionalInt.of(1), OptionalInt.empty());
    assertFalse(pointer.isInitialized());

    biConsumer.accept(OptionalInt.empty(), OptionalInt.of(2));
    assertFalse(pointer.isInitialized());

    biConsumer.accept(OptionalInt.of(1), OptionalInt.of(2));
    assertTrue(pointer.isInitialized());
    assertEquals("1_2", pointer.getOrThrow());
  }

  @Test
  public void testToStringOrEmpty() {
    assertEquals("",    toStringOrEmpty(Optional.empty()));
    assertEquals("ABC", toStringOrEmpty(Optional.of("ABC")));
    assertEquals("123", toStringOrEmpty(Optional.of(123)));
  }

  @Test
  public void testTransformIfNonNull() {
    assertNull(transformIfNonNull(v -> v + "_").apply(null));
    assertEquals("X_", transformIfNonNull(v -> v + "_").apply("X"));
  }

}
