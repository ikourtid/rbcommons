package com.rb.nonbiz.types;

import com.rb.nonbiz.functional.TriConsumer;
import org.junit.Test;

import java.util.OptionalInt;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.RBIntegers.asAlmostExactIntOrThrow;
import static com.rb.nonbiz.types.RBIntegers.maxAllowingOptionalInt;
import static com.rb.nonbiz.types.RBIntegers.minAllowingOptionalInt;
import static org.junit.Assert.assertEquals;

public class RBIntegersTest {

  @Test
  public void testMaxAllowingOptionalInt() {
    TriConsumer<OptionalInt, Integer, Integer> asserter = (optionalValue1, value2, expectedResult) -> {
      assertEquals(expectedResult.intValue(), maxAllowingOptionalInt(optionalValue1, value2));
      assertEquals(expectedResult.intValue(), maxAllowingOptionalInt(value2, optionalValue1));
    };

    asserter.accept(OptionalInt.of(22), 11, 22);
    asserter.accept(OptionalInt.of(22), 33, 33);

    asserter.accept(OptionalInt.empty(), -33, -33);
    asserter.accept(OptionalInt.empty(),   0,   0);
    asserter.accept(OptionalInt.empty(),  33,  33);
  }

  @Test
  public void testMinAllowingOptionalInt() {
    TriConsumer<OptionalInt, Integer, Integer> asserter = (optionalValue1, value2, expectedResult) -> {
      assertEquals(expectedResult.intValue(), minAllowingOptionalInt(optionalValue1, value2));
      assertEquals(expectedResult.intValue(), minAllowingOptionalInt(value2, optionalValue1));
    };

    asserter.accept(OptionalInt.of(22), 11, 11);
    asserter.accept(OptionalInt.of(22), 33, 22);

    asserter.accept(OptionalInt.empty(), -33, -33);
    asserter.accept(OptionalInt.empty(),   0,   0);
    asserter.accept(OptionalInt.empty(),  33,  33);
  }

  @Test
  public void testAsAlmostExactIntOrThrow() {
    Epsilon epsilon = epsilon(0.02);

    assertIllegalArgumentException( () -> asAlmostExactIntOrThrow(-1.03, epsilon));
    assertEquals(-1, asAlmostExactIntOrThrow(-1.01, epsilon));
    assertEquals(-1, asAlmostExactIntOrThrow(-1.0, epsilon));
    assertEquals(-1, asAlmostExactIntOrThrow(-0.99, epsilon));
    assertIllegalArgumentException( () -> asAlmostExactIntOrThrow(-0.97, epsilon));
    assertIllegalArgumentException( () -> asAlmostExactIntOrThrow(-0.03, epsilon));
    assertEquals(0, asAlmostExactIntOrThrow(-0.01, epsilon));
    assertEquals(0, asAlmostExactIntOrThrow(0.0, epsilon));
    assertEquals(0, asAlmostExactIntOrThrow(0.01, epsilon));
    assertIllegalArgumentException( () -> asAlmostExactIntOrThrow(0.03, epsilon));
    assertIllegalArgumentException( () -> asAlmostExactIntOrThrow(0.97, epsilon));
    assertEquals(1, asAlmostExactIntOrThrow(0.99, epsilon));
    assertEquals(1, asAlmostExactIntOrThrow(1.0, epsilon));
    assertEquals(1, asAlmostExactIntOrThrow(1.01, epsilon));
    assertIllegalArgumentException( () -> asAlmostExactIntOrThrow(1.03, epsilon));
    assertIllegalArgumentException( () -> asAlmostExactIntOrThrow(1.97, epsilon));
    assertEquals(2, asAlmostExactIntOrThrow(1.99, epsilon));
    assertEquals(2, asAlmostExactIntOrThrow(2.0, epsilon));
    assertEquals(2, asAlmostExactIntOrThrow(2.01, epsilon));
  }

}
