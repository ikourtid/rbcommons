package com.rb.nonbiz.collections;

import com.rb.biz.types.Price;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.rb.biz.types.Price.averagePrice;
import static com.rb.biz.types.Price.price;
import static com.rb.nonbiz.collections.RBOptionalTransformers.*;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static org.junit.Assert.assertEquals;

public class RBOptionalTransformersTest {

  @Test
  public void testTransformOptional() {
    assertOptionalEmpty(transformOptional(Optional.empty(), s -> s + "_"));
    assertOptionalEquals(
        "x_",
        transformOptional(Optional.of("x"), s -> s + "_"));
  }

  @Test
  public void testTransformOptional2() {
    assertOptionalEmpty(transformOptional2(Optional.empty(), s -> Optional.of(s + "_")));
    assertOptionalEquals(
        "x_",
        transformOptional2(Optional.of("x"), s -> Optional.of(s + "_")));
  }

  @Test
  public void testTransformPairOfOptionals_overloadThatTransformsOnlyIfBothArePresent() {
    BiFunction<Integer, Boolean, String> transformer = (i, b) -> Strings.format("%s_%s", i, b);
    assertOptionalEmpty(transformPairOfOptionals(Optional.empty(), Optional.empty(), transformer));
    assertOptionalEmpty(transformPairOfOptionals(Optional.empty(), Optional.of(true), transformer));
    assertOptionalEmpty(transformPairOfOptionals(Optional.of(123), Optional.empty(), transformer));
    assertOptionalEquals("123_true", transformPairOfOptionals(Optional.of(123), Optional.of(true), transformer));
  }

  @Test
  public void testTransformPairOfOptionals_overloadThatTransformsIfEitherIsPresent() {
    BiFunction<Optional<Integer>, Optional<Boolean>, Optional<String>> maker =
        (optionalA, optionalB) -> transformPairOfOptionals(
            optionalA,
            optionalB,
            (i, b) -> Strings.format("%s_%s", i, b),
            i -> Strings.format("%s_", i),
            b -> Strings.format("_%s", b));
    assertOptionalEmpty(maker.apply(Optional.empty(), Optional.empty()));
    assertOptionalEquals("_true",    maker.apply(Optional.empty(), Optional.of(true)));
    assertOptionalEquals("123_",     maker.apply(Optional.of(123), Optional.empty()));
    assertOptionalEquals("123_true", maker.apply(Optional.of(123), Optional.of(true)));
  }

  @Test
  public void testTransformPairOfOptionalsAssumingBothOrNeitherAreEmpty() {
    BiFunction<Optional<Integer>, Optional<Boolean>, Optional<String>> maker =
        (optionalA, optionalB) -> transformPairOfOptionalsAssumingBothOrNeitherAreEmpty(
            optionalA,
            optionalB,
            (i, b) -> Strings.format("%s_%s", i, b));
    assertOptionalEmpty(maker.apply(Optional.empty(), Optional.empty()));
    assertIllegalArgumentException( () -> maker.apply(Optional.empty(), Optional.of(true)));
    assertIllegalArgumentException( () -> maker.apply(Optional.of(123), Optional.empty()));
    assertOptionalEquals("123_true", maker.apply(Optional.of(123), Optional.of(true)));
  }

  @Test
  public void testTransformPairOfOptionals_mostGeneralOverload() {
    BiFunction<Optional<Integer>, Optional<Boolean>, String> maker =
        (optionalA, optionalB) -> transformPairOfOptionals(
            optionalA,
            optionalB,
            (i, b) -> Strings.format("%s_%s", i, b),
            i -> Strings.format("%s_", i),
            b -> Strings.format("_%s", b),
            () -> "n/a");
    assertEquals("n/a",      maker.apply(Optional.empty(), Optional.empty()));
    assertEquals("_true",    maker.apply(Optional.empty(), Optional.of(true)));
    assertEquals("123_",     maker.apply(Optional.of(123), Optional.empty()));
    assertEquals("123_true", maker.apply(Optional.of(123), Optional.of(true)));
  }

  @Test
  public void testTransformPairOfOptionalDoubles_mostGeneralOverload() {
    BiFunction<OptionalDouble, OptionalDouble, String> maker =
        (optionalA, optionalB) -> transformPairOfOptionalDoubles(
            optionalA,
            optionalB,
            (i, b) -> Strings.format("%s_%s", i, b),
            i -> Strings.format("%s_", i),
            b -> Strings.format("_%s", b),
            () -> "n/a");
    assertEquals("n/a",     maker.apply(OptionalDouble.empty(), OptionalDouble.empty()));
    assertEquals("_2.2",    maker.apply(OptionalDouble.empty(), OptionalDouble.of(2.2)));
    assertEquals("1.1_",    maker.apply(OptionalDouble.of(1.1), OptionalDouble.empty()));
    assertEquals("1.1_2.2", maker.apply(OptionalDouble.of(1.1), OptionalDouble.of(2.2)));
  }


  @Test
  public void testTransformPairOfOptionalInts_mostGeneralOverload() {
    BiFunction<OptionalInt, OptionalInt, String> maker =
        (optionalA, optionalB) -> transformPairOfOptionalInts(
            optionalA,
            optionalB,
            (i, b) -> Strings.format("%s_%s", i, b),
            i -> Strings.format("%s_", i),
            b -> Strings.format("_%s", b),
            () -> "n/a");
    assertEquals("n/a",   maker.apply(OptionalInt.empty(), OptionalInt.empty()));
    assertEquals("_22",   maker.apply(OptionalInt.empty(), OptionalInt.of(22)));
    assertEquals("11_",   maker.apply(OptionalInt.of(11),  OptionalInt.empty()));
    assertEquals("11_22", maker.apply(OptionalInt.of(11),  OptionalInt.of(22)));
  }


  @Test
  public void testTransformOptionalWithPredicate() {
    Predicate<Integer> isEven = i -> i %2 == 0;
    assertOptionalEmpty(transformOptionalWithPredicate(Optional.empty(), isEven));
    assertOptionalEmpty(transformOptionalWithPredicate(Optional.of(1), isEven));
    assertOptionalNonEmpty(
        transformOptionalWithPredicate(Optional.of(2), isEven),
        typeSafeEqualTo(2));
  }

  @Test
  public void testTransformAtLeastOnePresentOptionalOfThrow() {
    BiFunction<Optional<Price>, Optional<Price>, Price> maker = (price1, price2) ->
        transformAtLeastOnePresentOptionalOrThrow(price1, price2, (v1, v2) -> averagePrice(v1, v2));
    assertIllegalArgumentException( () -> maker.apply(Optional.empty(), Optional.empty()));
    assertEquals(price(10), maker.apply(Optional.of(price(9)), Optional.of(price(11))));
    assertEquals(price(9),  maker.apply(Optional.of(price(9)), Optional.empty()));
    assertEquals(price(11), maker.apply(Optional.empty(), Optional.of(price(11))));
  }

  @Test
  public void testTransformOptionalNumbers() {
    // transformOptionalInt...present and missing, same return type and different return type.
    assertEquals(Optional.of(20), transformOptionalInt(OptionalInt.of(10),   v -> 2 * v));
    assertEquals(Optional.empty(), transformOptionalInt(OptionalInt.empty(),    v -> 2 * v));
    assertEquals(Optional.of("40"), transformOptionalInt(OptionalInt.of(10), v -> Integer.toString(4 * v)));
    assertEquals(Optional.empty(), transformOptionalInt(OptionalInt.empty(),    v -> Integer.toString(4 * v)));

    // transformOptionalDouble...present and missing, same return type and different return type.
    assertEquals(Optional.of(20.0), transformOptionalDouble(OptionalDouble.of(10.0), v -> 2.0 * v));
    assertEquals(Optional.empty(), transformOptionalDouble(OptionalDouble.empty(), v -> 2.0 * v));
    assertEquals(Optional.of("40.0"), transformOptionalDouble(OptionalDouble.of(10), v -> new DecimalFormat("#0.0").format(4 * v)));
    assertEquals(Optional.empty(), transformOptionalDouble(OptionalDouble.empty(), v -> new DecimalFormat("#0.0").format(4 * v)));

    // transformOptionalLong...present and missing, same return type and different return type.
    assertEquals(Optional.of(Long.valueOf(20)), transformOptionalLong(OptionalLong.of(10), v -> 2 * v));
    assertEquals(Optional.empty(), transformOptionalLong(OptionalLong.empty(),            v -> 2 * v));
    assertEquals(Optional.of("40"), transformOptionalLong(OptionalLong.of(10),         v -> new DecimalFormat("#0").format(4 * v)));
    assertEquals(Optional.empty(), transformOptionalLong(OptionalLong.empty(),            v -> new DecimalFormat("#0").format(4 * v)));
  }

}
