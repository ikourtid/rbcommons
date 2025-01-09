package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Money;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.math.stats.ZScore;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Pointer;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import static com.rb.nonbiz.math.stats.ZScore.zScore;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.optionalMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalIntEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalIntEquals;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_LONG;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Pointer.initializedPointer;
import static com.rb.nonbiz.types.Pointer.uninitializedPointer;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
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
  public void testCountPresentOptionals() {
    assertEquals(2, countPresentOptionals(Optional.of("abc"), Optional.of(123)));
    assertEquals(1, countPresentOptionals(Optional.empty(), Optional.of(123)));
    assertEquals(0, countPresentOptionals(Optional.empty(), Optional.empty()));

    assertEquals(1, countPresentOptionals(Optional.of(123)));
    assertEquals(0, countPresentOptionals(Optional.empty()));
  }

  @Test
  public void testMakeNonEmptyOptionalIf() {
    assertOptionalEmpty(makeNonEmptyOptionalIf(false, unitFraction(0.123)));
    assertOptionalEmpty(makeNonEmptyOptionalIf(false, () -> unitFraction(0.123)));

    assertOptionalNonEmpty(
        makeNonEmptyOptionalIf(true, unitFraction(0.123)),
        preciseValueMatcher(unitFraction(0.123), DEFAULT_EPSILON_1e_8));
    assertOptionalNonEmpty(
        makeNonEmptyOptionalIf(true, () -> unitFraction(0.123)),
        preciseValueMatcher(unitFraction(0.123), DEFAULT_EPSILON_1e_8));

    // Obviously this throws, because the 2nd argument gets evaluated, and causes an exception.
    assertIllegalArgumentException( () ->
        makeNonEmptyOptionalIf(false, unitFraction(-999)));

    // However, this doesn't throw, because the invalid UnitFraction sits inside a Supplier.
    assertOptionalEmpty(
        makeNonEmptyOptionalIf(false, () -> unitFraction(-999)));
  }

  @Test
  public void testGetOrThrow() {
    assertIllegalArgumentException( () -> getOrThrow(Optional.empty(), DUMMY_STRING));
    assertEquals("ABC", getOrThrow(Optional.of("ABC"), DUMMY_STRING));
  }

  @Test
  public void testGetIntOrThrow() {
    assertIllegalArgumentException( () -> getIntOrThrow(OptionalInt.empty(), DUMMY_STRING));
    assertEquals(123, getIntOrThrow(OptionalInt.of(123), DUMMY_STRING));
  }

  @Test
  public void testGetDoubleOrThrow() {
    assertIllegalArgumentException( () -> getDoubleOrThrow(OptionalDouble.empty(), DUMMY_STRING));
    assertEquals(1.23, getDoubleOrThrow(OptionalDouble.of(1.23), DUMMY_STRING), 1e-8);
  }

  @Test
  public void testOptionalsHaveSameIsPresent() {
    assertTrue(optionalsHaveSameIsPresent(Optional.empty(), Optional.empty()));
    assertTrue(optionalsHaveSameIsPresent(Optional.of(123), Optional.of(123)));
    assertTrue(optionalsHaveSameIsPresent(Optional.of(123), Optional.of(456)));

    assertFalse(optionalsHaveSameIsPresent(Optional.empty(), Optional.of(123)));
    assertFalse(optionalsHaveSameIsPresent(Optional.of(123), Optional.empty()));
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
  public void testOptionalsEqual_usingSuppliedEquality() {
    // Contrast with previous test #testOptionalsEqual()
    BiPredicate<String, String> sameFirstLetter = (str1, str2) ->
        str1.substring(0, 1).equals(str2.substring(0, 1));

    // Same as in the previous test, except that the type has to be the same.
    assertTrue(optionalsEqual(Optional.empty(), Optional.empty(), sameFirstLetter));
    assertTrue(optionalsEqual(Optional.of("a"), Optional.of("a"), sameFirstLetter));
    assertFalse(optionalsEqual(Optional.of("a"), Optional.of("b"), sameFirstLetter));
    assertFalse(optionalsEqual(Optional.of("a"), Optional.empty(), sameFirstLetter));
    assertFalse(optionalsEqual(Optional.empty(), Optional.of("b"), sameFirstLetter));

    // ... but also true if the two optionals are present and both start with the same letter.
    assertTrue(optionalsEqual(Optional.of("aX"), Optional.of("aY"), sameFirstLetter));
    assertFalse(optionalsEqual(Optional.of("aX"), Optional.of("bY"), sameFirstLetter));
    assertFalse(optionalsEqual(Optional.of("a"), Optional.empty(), sameFirstLetter));
    assertFalse(optionalsEqual(Optional.empty(), Optional.of("b"), sameFirstLetter));
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
  public void testIfPresentOrElse() {
    Pointer<String> result = uninitializedPointer();
    Consumer<Optional<String>> consumer = optional ->
        ifPresentOrElse(
            optional,
            v -> result.set('_' + v),
            () -> result.set("?"));

    consumer.accept(Optional.of("x"));
    assertEquals("_x", result.getOrThrow());

    consumer.accept(Optional.empty());
    assertEquals("?", result.getOrThrow());
  }

  @Test
  public void testIfLongPresentOrElse() {
    Pointer<String> result = uninitializedPointer();
    Consumer<OptionalLong> consumer = optional ->
        ifLongPresentOrElse(
            optional,
            v -> result.set('_' + Long.toString(v)),
            () -> result.set("?"));

    consumer.accept(OptionalLong.of(123));
    assertEquals("_123", result.getOrThrow());

    consumer.accept(OptionalLong.empty());
    assertEquals("?", result.getOrThrow());
  }

  @Test
  public void testIfIntPresentOrElse() {
    Pointer<String> result = uninitializedPointer();
    Consumer<OptionalInt> consumer = optional ->
        ifIntPresentOrElse(
            optional,
            v -> result.set('_' + Long.toString(v)),
            () -> result.set("?"));

    consumer.accept(OptionalInt.of(123));
    assertEquals("_123", result.getOrThrow());

    consumer.accept(OptionalInt.empty());
    assertEquals("?", result.getOrThrow());
  }

  @Test
  public void lazyGetIfPresent_emptyInputs_returnsEmptyList() {
    assertOptionalEquals(emptyList(), lazyGetIfPresent(emptyList()));
    assertOptionalEquals(emptyList(), lazyGetIfPresent());
  }

  @Test
  public void test_lazyGetFirstPresentOptionalOrElse() {
    Pointer<Integer> count = uninitializedPointer();
    assertEquals(
        "A",
        lazyGetFirstPresentOptionalOrElse(
            () -> Optional.of("A"),
            () -> { count.modifyExisting(1, Integer::sum); return Optional.of("B"); },
            () -> { count.set(123); return "C"; }));
    assertFalse("The suppliers for B and C will not be evaluated", count.isInitialized());

    assertEquals(
        "A",
        lazyGetFirstPresentOptionalOrElse(
            () -> Optional.of("A"),
            () -> { count.set(123); return Optional.empty(); },
            () -> { count.set(123); return "C"; }));
    assertFalse("The suppliers for B and C will not be evaluated", count.isInitialized());

    assertEquals(
        "B",
        lazyGetFirstPresentOptionalOrElse(
            () -> Optional.empty(),
            () -> Optional.of("B"),
            () -> { count.set(123); return "C"; }));
    assertFalse("The supplier for C will not be evaluated", count.isInitialized());

    assertEquals(
        "C",
        lazyGetFirstPresentOptionalOrElse(
            () -> Optional.empty(),
            () -> Optional.empty(),
            () -> "C"));
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
  public void testFilterPresentOptionalDoublesInStream() {
    BiConsumer<Stream<OptionalDouble>, List<Double>> asserter = (inputList, expectedReturnValue) ->
        assertThat(
            filterPresentOptionalDoublesInStream(inputList)
                .boxed()
                .collect(Collectors.toList()),
            doubleListMatcher(expectedReturnValue, DEFAULT_EPSILON_1e_8));

    asserter.accept(
        Stream.empty(),
        emptyList());
    asserter.accept(
        Stream.of(OptionalDouble.of(1.1), OptionalDouble.empty(), OptionalDouble.of(2.2)),
        ImmutableList.of(1.1, 2.2));
    asserter.accept(
        Stream.of(OptionalDouble.of(1.1), OptionalDouble.of(2.2)),
        ImmutableList.of(1.1, 2.2));
    asserter.accept(
        Stream.of(OptionalDouble.empty(), OptionalDouble.empty(), OptionalDouble.empty()),
        emptyList());
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

    assertEquals(OptionalDouble.of(78.9), transformOptionalImpreciseValueToOptionalDouble(Optional.of(zScore(78.9))));
    assertEquals(OptionalDouble.empty(),  transformOptionalImpreciseValueToOptionalDouble(Optional.<ZScore>empty()));

    assertEquals(45.6, transformOptionalPreciseValueToDoubleOrZero(Optional.of(money(45.6))), 1e-8);
    assertEquals(0.0,  transformOptionalPreciseValueToDoubleOrZero(Optional.<Money>empty()), 1e-8);

    assertEquals(78.9, transformOptionalImpreciseValueToDoubleOrZero(Optional.of(zScore(78.9))), 1e-8);
    assertEquals(0.0,  transformOptionalImpreciseValueToDoubleOrZero(Optional.<ZScore>empty()), 1e-8);
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

  @Test
  public void test_allOptionalsPresent_allOptionalsEmpty() {
    Optional<String> empty1 = Optional.empty();
    Optional<Integer> empty2 = Optional.empty();
    Optional<Boolean> empty3 = Optional.empty();
    Optional<Long> empty4 = Optional.empty();

    Optional<String> present1 = Optional.of(DUMMY_STRING);
    Optional<Integer> present2 = Optional.of(DUMMY_POSITIVE_INTEGER);
    Optional<Boolean> present3 = Optional.of(false);
    Optional<Long> present4 = Optional.of(DUMMY_LONG);

    assertTrue(allOptionalsPresent(present1, present2, present3, present4));
    assertFalse(allOptionalsPresent(empty1, present2, present3, present4));
    assertFalse(allOptionalsPresent(present1, empty2, present3, present4));
    assertFalse(allOptionalsPresent(present1, present2, empty3, present4));
    assertFalse(allOptionalsPresent(present1, present2, present3, empty4));
    assertFalse(allOptionalsPresent(empty1, empty2, empty3, empty4));

    assertTrue(allOptionalsPresent(present1, present2, present3));
    assertFalse(allOptionalsPresent(empty1, present2, present3));
    assertFalse(allOptionalsPresent(present1, empty2, present3));
    assertFalse(allOptionalsPresent(present1, present2, empty3, present4));
    assertFalse(allOptionalsPresent(empty1, empty2, empty3));

    assertFalse(allOptionalsEmpty(present1, present2, present3, present4));
    assertFalse(allOptionalsEmpty(empty1, present2, present3, present4));
    assertFalse(allOptionalsEmpty(present1, empty2, present3, present4));
    assertFalse(allOptionalsEmpty(present1, present2, empty3, present4));
    assertFalse(allOptionalsEmpty(present1, present2, present3, empty4));
    assertTrue(allOptionalsEmpty(empty1, empty2, empty3, empty4));

    assertFalse(allOptionalsEmpty(present1, present2, present3));
    assertFalse(allOptionalsEmpty(empty1, present2, present3));
    assertFalse(allOptionalsEmpty(present1, empty2, present3));
    assertFalse(allOptionalsEmpty(present1, present2, empty3, present4));
    assertTrue(allOptionalsEmpty(empty1, empty2, empty3));
  }

  @Test
  public void test_findFirstPresentOptional() {
    BiConsumer<List<Integer>, Optional<String>> asserter = (list, expected) ->
        assertThat(
            findFirstPresentOptional(
                list,
                v -> v > 5 ? Optional.of(v.toString()) : Optional.empty()),
            optionalMatcher(expected, f -> typeSafeEqualTo(f)));
    asserter.accept(emptyList(),            Optional.empty());
    asserter.accept(singletonList(4),       Optional.empty());
    asserter.accept(ImmutableList.of(3, 4), Optional.empty());

    asserter.accept(singletonList(6),          Optional.of("6"));
    asserter.accept(ImmutableList.of(6, 3, 4), Optional.of("6"));
    asserter.accept(ImmutableList.of(3, 6, 4), Optional.of("6"));
    asserter.accept(ImmutableList.of(3, 4, 6), Optional.of("6"));

    asserter.accept(ImmutableList.of(3, 4, 6, 7), Optional.of("6"));
    asserter.accept(ImmutableList.of(3, 4, 7, 6), Optional.of("7"));
    asserter.accept(ImmutableList.of(3, 7, 4, 6), Optional.of("7"));
    asserter.accept(ImmutableList.of(7, 3, 4, 6), Optional.of("7"));
  }

  @Test
  public void test_findOnlyPresentOptional_twoArgOverload() {
    assertIllegalArgumentException( () -> findOnlyPresentOptional(Optional.empty(), Optional.empty()));
    assertIllegalArgumentException( () -> findOnlyPresentOptional(Optional.of(1), Optional.of(2)));

    assertEquals(1, findOnlyPresentOptional(Optional.of(1),   Optional.empty()).intValue());
    assertEquals(1, findOnlyPresentOptional(Optional.empty(), Optional.of(1))  .intValue());
  }

  @Test
  public void test_findOnlyPresentOptional_threeArgOverload() {
    assertIllegalArgumentException( () -> findOnlyPresentOptional(Optional.empty(), Optional.empty(), Optional.empty()));
    assertIllegalArgumentException( () -> findOnlyPresentOptional(Optional.of(1),   Optional.of(2),   Optional.empty()));
    assertIllegalArgumentException( () -> findOnlyPresentOptional(Optional.of(1),   Optional.empty(), Optional.of(2)));
    assertIllegalArgumentException( () -> findOnlyPresentOptional(Optional.empty(), Optional.of(1),   Optional.of(2)));
    assertIllegalArgumentException( () -> findOnlyPresentOptional(Optional.of(1),   Optional.of(2),   Optional.of(3)));

    assertEquals(1, findOnlyPresentOptional(Optional.of(1),   Optional.empty(), Optional.empty()).intValue());
    assertEquals(1, findOnlyPresentOptional(Optional.empty(), Optional.of(1),   Optional.empty()).intValue());
    assertEquals(1, findOnlyPresentOptional(Optional.empty(), Optional.empty(), Optional.of(1))  .intValue());
  }

  @Test
  public void test_findZeroOrOnePresentOptional() {
    assertIllegalArgumentException( () -> findZeroOrOnePresentOptional(Optional.of(1),   Optional.of(2),   Optional.empty()));
    assertIllegalArgumentException( () -> findZeroOrOnePresentOptional(Optional.of(1),   Optional.empty(), Optional.of(2)));
    assertIllegalArgumentException( () -> findZeroOrOnePresentOptional(Optional.empty(), Optional.of(1),   Optional.of(2)));
    assertIllegalArgumentException( () -> findZeroOrOnePresentOptional(Optional.of(1),   Optional.of(2),   Optional.of(3)));

    assertOptionalEmpty(findZeroOrOnePresentOptional(Optional.empty(), Optional.empty(), Optional.empty()));

    assertOptionalEquals(1, findZeroOrOnePresentOptional(Optional.of(1),   Optional.empty(), Optional.empty()));
    assertOptionalEquals(1, findZeroOrOnePresentOptional(Optional.empty(), Optional.of(1),   Optional.empty()));
    assertOptionalEquals(1, findZeroOrOnePresentOptional(Optional.empty(), Optional.empty(), Optional.of(1)));
  }

  @Test
  public void testOptionalSatisfies() {
    BiConsumer<Optional<String>, Boolean> asserter = (optional, expectedResult) ->
      assertEquals(
          expectedResult,
          optionalSatisfies(optional, v -> v.length() >= 2));

    asserter.accept(Optional.empty(), false);
    asserter.accept(Optional.of(""), false);
    asserter.accept(Optional.of("x"), false);
    asserter.accept(Optional.of("xy"), true);
    asserter.accept(Optional.of("xyz"), true);
  }

}
